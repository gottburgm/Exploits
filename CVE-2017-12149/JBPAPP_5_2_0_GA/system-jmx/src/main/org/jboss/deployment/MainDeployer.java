/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.deployment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;

import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.util.file.Files;
import org.jboss.util.file.JarUtils;
import org.jboss.util.stream.Streams;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * The legacy component for deployer management. This now simply delegates to the
 * Main
 *
 * @deprecated see org.jboss.deployers.spi.deployment.MainDeployer
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author adrian@jboss.org
 * @author ales.justin@jboss.org
 * @version $Revision: 112199 $
 */
public class MainDeployer extends ServiceMBeanSupport
   implements Deployer, MainDeployerMBean
{
   /** The controller */
   private KernelController controller;
   private DeployerClient delegate;
   private Map<URL, String> contextMap = Collections.synchronizedMap(new HashMap<URL, String>());

   /** The deployment factory */
   private VFSDeploymentFactory deploymentFactory = VFSDeploymentFactory.getInstance();

   /**
    * The variable <code>serviceController</code> is used by the
    * checkIncompleteDeployments method to ask for lists of mbeans
    * with deployment problems.
    */
   private ObjectName serviceController;

   /** Deployers **/
   private final LinkedList deployers = new LinkedList();

   /** A Map of URL -> DeploymentInfo */
   private final Map deploymentMap = Collections.synchronizedMap(new HashMap());

   /** A list of all deployments that have deployers. */
   private final List deploymentList = new ArrayList();

   /** A list of all deployments that do not have deployers. */
   private final List waitingDeployments = new ArrayList();

   /** A helper for sorting deployment URLs. */
   private final DeploymentSorter sorter = new DeploymentSorter();

   /** A helper for sorting deploymentInfos */
   private final Comparator infoSorter = new DeploymentInfoComparator(sorter);

   /** Helper class handling the SuffixOrder and EnhancedSuffixOrder attributes */
   private final SuffixOrderHelper suffixOrderHelper = new SuffixOrderHelper(sorter);

   /** Should local copies be made of resources on the local file system */
   private boolean copyFiles = true;

   /** The temporary directory for deployments. */
   private File tempDir;

   /** The string naming the tempDir **/
   private String tempDirString;

   /**
    * Explict no-args contsructor for JMX.
    */
   public MainDeployer()
   {
      // Is there a better place to obtain startup information?
      String localCopy = System.getProperty("jboss.deploy.localcopy");
      if (localCopy != null && (
          localCopy.equalsIgnoreCase("false") ||
          localCopy.equalsIgnoreCase("no") ||
          localCopy.equalsIgnoreCase("off")))
      {
         log.debug("Disabling local copies of file: urls");
         copyFiles = false;
      }
   }

   public DeployerClient getKernelMainDeployer()
   {
      return delegate;
   }
   public void setKernelMainDeployer(DeployerClient delegate)
   {
      this.delegate = (org.jboss.deployers.client.spi.main.MainDeployer) delegate;
   }

   public KernelController getController()
   {
      return controller;
   }

   public void setController(KernelController controller)
   {
      this.controller = controller;
   }

   /** Get the flag indicating whether directory content will be deployed
    *
    * @return the file copy flag
    * @jmx.managed-attribute
    */
   public boolean getCopyFiles()
   {
      return copyFiles;
   }
   /** Set the flag indicating whether directory content will be deployed. The
    * default value is taken from the jboss.deploy.localcopy system
    * property.
    *
    * @param copyFiles the local copy flag value
    * @jmx.managed-attribute
    */
   public void setCopyFiles(boolean copyFiles)
   {
      this.copyFiles = copyFiles;
   }

   /** Get the temp directory
    *
    * @return the path to the local tmp directory
    * @jmx.managed-attribute
    */
   public File getTempDir()
   {
      return tempDir;
   }
   /** Set the temp directory
    *
    * @param tempDir the path to the local tmp directory
    * @jmx.managed-attribute
    */
   public void setTempDir(File tempDir)
   {
      this.tempDir = tempDir;
   }

   /** Get the temp directory
    *
    * @return the path to the local tmp directory
    * @jmx.managed-attribute
    */
   public String getTempDirString()
   {
      return tempDirString;
   }

   /** Get the ordering of the deployment suffixes
    *
    * @return the ordering of the deployment suffixes
    * @jmx.managed-attribute
    */
   public String[] getSuffixOrder()
   {
      return suffixOrderHelper.getSuffixOrder();
   }

   /** Get the enhanced suffix order
    *
    * @return the enhanced suffix order
    * @jmx.managed-attribute
    */
   public String[] getEnhancedSuffixOrder()
   {
      return suffixOrderHelper.getEnhancedSuffixes();
   }

   /** Set the enhanced suffix order
    *
    * @param enhancedSuffixOrder the enhanced suffix order
    * @jmx.managed-attribute
    */
   public void setEnhancedSuffixOrder(String[] enhancedSuffixOrder)
   {
      suffixOrderHelper.setEnhancedSuffixes(enhancedSuffixOrder);
   }

   /**
    * Describe <code>setServiceController</code> method here.
    *
    * @param serviceController an <code>ObjectName</code> value
    * @jmx.managed-attribute
    */
   public void setServiceController(final ObjectName serviceController)
   {
      this.serviceController = serviceController;
   }

   /**
    * The <code>listDeployed</code> method returns a collection of DeploymemtInfo
    * objects for the currently deployed packages.
    *
    * @return a <code>Collection</code> value
    * @jmx.managed-operation
    */
   public Collection listDeployed()
   {      
      return getAllDeployments();
   }

   /**
    * The <code>listDeployedModules</code> method returns a collection of
    * SerializableDeploymentInfo objects for the currently deployed packages.
    *
    * @return a <code>Collection</code> value
    * @jmx.managed-operation
    */
   public Collection listDeployedModules()
   {
      return getAllDeployments();
   }

   /**
    * Describe <code>listDeployedAsString</code> method here.
    *
    * @return a <code>String</code> value
    * @jmx.managed-operation
    */
   public String listDeployedAsString()
   {      
      return "<pre>" + listDeployed() + "</pre>";           
   }

   /**
    * The <code>listIncompletelyDeployed</code> method returns a list of packages that have
    * not deployed completely. The toString method will include any exception in the status
    * field.
    *
    * @return a <code>Collection</code> value
    * @jmx.managed-operation
    */
   public Collection listIncompletelyDeployed()
   {
      List id = new ArrayList();
      List copy;
      synchronized (deploymentList)
      {
         copy = new ArrayList(deploymentList);
      }
      for (Iterator i = copy.iterator(); i.hasNext();)
      {
         DeploymentInfo di = (DeploymentInfo)i.next();
         if (!"Deployed".equals(di.status) && !"Starting".equals(di.status))
         {
            id.add(di);
         } // end of if ()

      } // end of for ()
      return id;
   }

   /**
    * The <code>listWaitingForDeployer</code> method returns a collection
    * of the packages that currently have no identified deployer.
    *
    * @return a <code>Collection</code> value
    * @jmx.managed-operation
    */
   public Collection listWaitingForDeployer()
   {
      synchronized (waitingDeployments)
      {
         return new ArrayList(waitingDeployments);
      }
   }

   /**
    * The <code>addDeployer</code> method registers a deployer with the main deployer.
    * Any waiting packages are tested to see if the new deployer will deploy them.
    *
    * @param deployer a <code>SubDeployer</code> value
    * @jmx.managed-operation
    */
   public void addDeployer(final SubDeployer deployer)
   {
      log.debug("Adding deployer: " + deployer);
      ObjectName deployerName = deployer.getServiceName();

      synchronized(deployers)
      {
         deployers.addFirst(deployer);
         try
         {
            String[] suffixes = (String[]) server.getAttribute(deployerName, "EnhancedSuffixes");
            suffixOrderHelper.addEnhancedSuffixes(suffixes);
         }
         catch(Exception e)
         {
            log.debug(deployerName + " does not support EnhancedSuffixes");
            suffixOrderHelper.addSuffixes(deployer.getSuffixes(), deployer.getRelativeOrder());
         }
      }

      // Send a notification about the deployer addition
      Notification msg = new Notification(ADD_DEPLOYER, this, getNextNotificationSequenceNumber());
      msg.setUserData(deployerName);
      sendNotification(msg);

      synchronized (waitingDeployments)
      {
         List copy = new ArrayList(waitingDeployments);
         waitingDeployments.clear();
         for (Iterator i = copy.iterator(); i.hasNext();)
         {
            DeploymentInfo di = (DeploymentInfo)i.next();
            log.debug("trying to deploy with new deployer: " + di.shortName);
            try
            {
               di.setServer(server);
               deploy(di);
            }
            catch (DeploymentException e)
            {
               log.error("DeploymentException while trying to deploy a package with a new deployer", e);
            } // end of try-catch
         } // end of for ()
      }
   }

   /**
    * The <code>removeDeployer</code> method unregisters a deployer with the MainDeployer.
    * Deployed packages deployed with this deployer are undeployed.
    *
    * @param deployer a <code>SubDeployer</code> value
    * @jmx.managed-operation
    */
   public void removeDeployer(final SubDeployer deployer)
   {
      log.debug("Removing deployer: " + deployer);
      ObjectName deployerName = deployer.getServiceName();
      boolean removed = false;

      synchronized(deployers)
      {
         removed = deployers.remove(deployer);
         try
         {
            String[] suffixes = (String[]) server.getAttribute(deployerName, "EnhancedSuffixes");
            suffixOrderHelper.removeEnhancedSuffixes(suffixes);
         }
         catch(Exception e)
         {
            log.debug(deployerName + " does not support EnhancedSuffixes");
            suffixOrderHelper.removeSuffixes(deployer.getSuffixes(), deployer.getRelativeOrder());
         }
      }

      // Send a notification about the deployer removal
      if (removed)
      {
         Notification msg = new Notification(REMOVE_DEPLOYER, this, getNextNotificationSequenceNumber());
         msg.setUserData(deployerName);
         sendNotification(msg);
      }

      List copy;
      synchronized (deploymentList)
      {
         copy = new ArrayList(deploymentList);
      }
      for (Iterator i = copy.iterator(); i.hasNext(); )
      {
         DeploymentInfo di = (DeploymentInfo)i.next();
         if (di.deployer == deployer)
         {
            undeploy(di);
            di.deployer = null;
            synchronized (waitingDeployments)
            {
               waitingDeployments.add(di);
            }
         }
      }
   }

   /**
    * The <code>listDeployers</code> method returns a collection of ObjectNames of
    * deployers registered with the MainDeployer.
    *
    * @return a <code>Collection<ObjectName></code> value
    * @jmx.managed-operation
    */
   public Collection listDeployers()
   {
      ArrayList deployerNames = new ArrayList();
      synchronized(deployers)
      {
         for(int n = 0; n < deployers.size(); n ++)
         {
            SubDeployer deployer = (SubDeployer) deployers.get(n);
            ObjectName name = deployer.getServiceName();
            deployerNames.add(name);
         }
      }
      return deployerNames;
   }

   // ServiceMBeanSupport overrides ---------------------------------

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }

   /**
    * The <code>createService</code> method is one of the ServiceMBean lifecyle operations.
    * (no jmx tag needed from superinterface)
    * @exception Exception if an error occurs
    */
   protected void createService() throws Exception
   {
      ServerConfig config = ServerConfigLocator.locate();
      // Get the temp directory location
      File basedir = config.getServerTempDir();
      // Set the local copy temp dir to tmp/deploy
      tempDir = new File(basedir, "deploy");
      // Delete any existing content
      Files.delete(tempDir);
      // Make sure the directory exists
      tempDir.mkdirs();

      // used in inLocalCopyDir
      tempDirString = tempDir.toURL().toString();

      // handles SuffixOrder & RelativeSuffixOrder attributes
      suffixOrderHelper.initialize();
   }

   /**
    * The <code>shutdown</code> method undeploys all deployed packages in
    * reverse order of their deployement.
    *
    * @jmx.managed-operation
    */
   public void shutdown()
   {
      // if we shutdown in the middle of a scan, it still might be possible that we try to redeploy
      // things we are busy killing...
      int deployCounter = 0;

      // undeploy everything in sight
      List copy;
      synchronized (deploymentList)
      {
         copy = new ArrayList(deploymentList);
      }
      for (ListIterator i = copy.listIterator(copy.size()); i.hasPrevious(); )
      {
         try
         {
            undeploy((DeploymentInfo)i.previous(), true);
            deployCounter++;
         }
         catch (Exception e)
         {
            log.info("exception trying to undeploy during shutdown", e);
         }

      }
      // Help GC
      this.deployers.clear();
      this.deploymentMap.clear();
      this.deploymentList.clear();
      this.waitingDeployments.clear();
      this.tempDir = null;

      log.debug("Undeployed " + deployCounter + " deployed packages");
   }


   /**
    * Describe <code>redeploy</code> method here.
    *
    * @param urlspec a <code>String</code> value
    * @exception DeploymentException if an error occurs
    * @exception MalformedURLException if an error occurs
    * @jmx.managed-operation
    */
   public void redeploy(String urlspec)
      throws DeploymentException, MalformedURLException
   {
      URL url;
      try
      {
         url = new URL(urlspec);
      }
      catch (MalformedURLException e)
      {
         File file = new File(urlspec);
         url = file.toURL();
      }
      
      redeploy(url);
   }

   /**
    * Describe <code>redeploy</code> method here.
    *
    * @param url an <code>URL</code> value
    * @exception DeploymentException if an error occurs
    * @jmx.managed-operation
    */
   public void redeploy(URL url) throws DeploymentException
   {
      String deploymentName = contextMap.get(url);
      
      if(isDeployed(url))
      {
         // then check deploymentName, if it is null then create
         if(deploymentName == null)
         {
            deploymentName = getDeploymentName(url);
         }
                 
         if (deploymentName != null)
         {
            try
            {
               Deployment deployment = delegate.getDeployment(deploymentName);
               delegate.addDeployment(deployment);
               delegate.process();
               delegate.checkComplete(deployment);
            }
            catch (org.jboss.deployers.spi.DeploymentException e)
            {
               throw new DeploymentException(e);
            }
         }
      }
      else
      {
         deploy(url);
      }
   }

   /**
    * Describe <code>redeploy</code> method here.
    *
    * @param sdi a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    * @jmx.managed-operation
    */
   public void redeploy(DeploymentInfo sdi) throws DeploymentException
   {
      try
      {
         undeploy(sdi);
      }
      catch (Throwable t)
      {
         log.info("Throwable from undeployment attempt: ", t);
      } // end of try-catch
      sdi.setServer(server);
      deploy(sdi);
   }

   /**
    * The <code>undeploy</code> method undeploys a package identified by a string
    * representation of a URL.
    *
    * @param urlspec the stringfied url to undeploy
    * @jmx.managed-operation
    */
   public void undeploy(String urlspec)
      throws DeploymentException, MalformedURLException
   {
      URL url;
      try
      {
         url = new URL(urlspec);
      }
      catch (MalformedURLException e)
      {
         File file = new File(urlspec);
         url = file.toURL();
      }
      
      undeploy(url);
   }

   /**
    * The <code>undeploy</code> method undeploys a package identified by a URL
    *
    * @param url the url to undeploy
    * @jmx.managed-operation
    */
   public void undeploy(URL url) throws DeploymentException
   {
      String deploymentName = contextMap.remove(url);
      
      if(deploymentName == null)
      {         
            deploymentName = getDeploymentName(url);       
      }
      
      if (deploymentName != null)
      {
         try
         {
            delegate.removeDeployment(deploymentName);
            delegate.process();
         }
         catch(Exception e)
         {
            DeploymentException ex = new DeploymentException("Error during undeploy of: "+url, e);
            throw ex;
         }
      }
      else
      {
         log.warn("undeploy '" + url + "' : package not deployed");
      }
   }

   /**
    * The <code>undeploy</code> method undeploys a package represented by a
    * DeploymentInfo object.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @jmx.managed-operation
    */
   public void undeploy(DeploymentInfo di)
   {
      undeploy(di, false);
   }
   protected void undeploy(DeploymentInfo di, boolean isShutdown)
   {
      log.debug("Undeploying "+di.url);
      stop(di);
      destroy(di);
   }

   /**
    * The <code>stop</code> method  is the first internal step of undeployment
    *
    * @param di a <code>DeploymentInfo</code> value
    */
   private void stop(DeploymentInfo di)
   {
      // Stop all sub-deployments
      ArrayList reverseSortedSubs = new ArrayList(di.subDeployments);
      Collections.sort(reverseSortedSubs, infoSorter);
      Collections.reverse(reverseSortedSubs);
      for (Iterator subs = reverseSortedSubs.iterator(); subs.hasNext();)
      {
         DeploymentInfo sub = (DeploymentInfo) subs.next();
         log.debug("Stopping sub deployment: "+sub.url);
         stop(sub);
      }
      // Lastly stop this deployment itself
      try
      {
         // Tell the respective deployer to undeploy this one
         if (di.deployer != null)
         {
            di.deployer.stop(di);
            di.status="Stopped";
            di.state = DeploymentState.STOPPED;
         }
      }
      catch (Throwable t)
      {
         log.error("Deployer stop failed for: " + di.url, t);
      }

   }

   /**
    * The <code>destroy</code> method is the second and final internal undeployment step.
    *
    * @param di a <code>DeploymentInfo</code> value
    */
   private void destroy(DeploymentInfo di)
   {
      // Destroy all sub-deployments
      ArrayList reverseSortedSubs = new ArrayList(di.subDeployments);
      Collections.sort(reverseSortedSubs, infoSorter);
      Collections.reverse(reverseSortedSubs);
      for (Iterator subs = reverseSortedSubs.iterator(); subs.hasNext();)
      {
         DeploymentInfo sub = (DeploymentInfo) subs.next();
         log.debug("Destroying sub deployment: "+sub.url);
         destroy(sub);
      }
      // Lastly destroy the deployment itself
      try
      {
         // Tell the respective deployer to undeploy this one
         if (di.deployer != null)
         {
            di.deployer.destroy(di);
            di.status="Destroyed";
            di.state = DeploymentState.DESTROYED;
         }
      }
      catch (Throwable t)
      {
         log.error("Deployer destroy failed for: " + di.url, t);
         di.state = DeploymentState.FAILED;
      }

      try
      {
         // remove from local maps
         synchronized (deploymentList)
         {
            deploymentMap.remove(di.url);
            if (deploymentList.lastIndexOf(di) != -1)
            {
               deploymentList.remove(deploymentList.lastIndexOf(di));
            }
         }
         synchronized (waitingDeployments)
         {
            waitingDeployments.remove(di);
         }
         // Nuke my stuff, this includes the class loader
         di.cleanup();

         log.debug("Undeployed "+di.url);
      }
      catch (Throwable t)
      {
         log.error("Undeployment cleanup failed: " + di.url, t);
      }
   }

   /**
    * The <code>deploy</code> method deploys a package identified by a
    * string representation of a URL.
    *
    * @param urlspec a <code>String</code> value
    * @exception MalformedURLException if an error occurs
    * @jmx.managed-operation
    */
   public void deploy(String urlspec)
      throws DeploymentException, MalformedURLException
   {
      if( server == null )
         throw new DeploymentException("The MainDeployer has been unregistered");

      URL url;
      try
      {
         url = new URL(urlspec);
      }
      catch (MalformedURLException e)
      {
         File file = new File(urlspec);
         url = file.toURL();
      }

      deploy(url);
   }  

   /**
    * The <code>deploy</code> method deploys a package identified by a URL
    *
    * @param url an <code>URL</code> value
    * @jmx.managed-operation
    */
   public void deploy(URL url) throws DeploymentException
   {
      log.info("deploy, url="+url);
      String deploymentName = contextMap.get(url);
      // if it does not exist create a new deployment
      if (deploymentName == null)
      {
         try
         {
            VirtualFile file = VFS.createNewRoot(url);
            VFSDeployment deployment = deploymentFactory.createVFSDeployment(file);
            delegate.addDeployment(deployment);
            deploymentName = deployment.getName();
            delegate.process();
            // TODO: JBAS-4292
            contextMap.put(url, deploymentName);
            delegate.checkComplete(deployment);
         }
         catch(Exception e)
         {
            log.warn("Failed to deploy: "+url, e);
            DeploymentException ex = new DeploymentException("Failed to deploy: "+url, e);
            throw ex;
         }
      }
   }
      
   /**
    * The <code>deploy</code> method deploys a package represented by a DeploymentInfo object.
    *
    * @param deployment a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    * @jmx.managed-operation
    */
   public void deploy(DeploymentInfo deployment)
      throws DeploymentException
   {
      // If we are already deployed return
      if (isDeployed(deployment.url))
      {
         log.info("Package: " + deployment.url + " is already deployed");
         return;
      }
      log.debug("Starting deployment of package: " + deployment.url);

      boolean inited = false;
      try
      {
         inited = init(deployment);
      }
      catch (Throwable t)
      {
         log.error("Could not initialise deployment: " + deployment.url, t);
         DeploymentException.rethrowAsDeploymentException("Could not initialise deployment: " + deployment.url, t);
      }
      if ( inited )
      {
         create(deployment);
         start(deployment);
         log.debug("Deployed package: " + deployment.url);
      } // end of if ()
      else
      {
         log.debug("Deployment of package: " + deployment.url + " is waiting for an appropriate deployer.");
      } // end of else
   }


   /**
    * The <code>init</code> method is the first internal deployment step.
    * The tasks are to copy the code if necessary,
    * set up classloaders, and identify the deployer for the package.
    *
    * @param deployment a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    */
   private boolean init(DeploymentInfo deployment) throws DeploymentException
   {
      // If we are already deployed return
      if (isDeployed(deployment.url))
      {
         log.info("Package: " + deployment.url + " is already deployed");
         return false;
      }
      log.debug("Starting deployment (init step) of package at: " + deployment.url);
      try
      {
         // Create a local copy of that File, the sdi keeps track of the copy directory
         if (deployment.localUrl == null)
         {
            makeLocalCopy(deployment);
            URL[] localCl = new URL[]{deployment.localUrl};
            deployment.localCl = new URLClassLoader(localCl);
         }

         // What deployer is able to deploy this file
         findDeployer(deployment);

         if(deployment.deployer == null)
         {
            deployment.state = DeploymentState.INIT_WAITING_DEPLOYER;
            log.debug("deployment waiting for deployer: " + deployment.url);
            synchronized (waitingDeployments)
            {
               if (waitingDeployments.contains(deployment) == false)
                  waitingDeployments.add(deployment);
            }
            return false;
         }
         deployment.state = DeploymentState.INIT_DEPLOYER;
         //we have the deployer, continue deployment.
         deployment.deployer.init(deployment);
         // initialize the unified classloaders for this deployment
         deployment.createClassLoaders();
         deployment.state = DeploymentState.INITIALIZED;

         // Add the deployment to the map so we can detect circular deployments
         synchronized (deploymentList)
         {
            deploymentMap.put(deployment.url, deployment);
         }

         // create subdeployments as needed
         parseManifestLibraries(deployment);

         log.debug("found " + deployment.subDeployments.size() + " subpackages of " + deployment.url);
         // get sorted subDeployments
         ArrayList sortedSubs = new ArrayList(deployment.subDeployments);
         Collections.sort(sortedSubs, infoSorter);
         for (Iterator lt = sortedSubs.listIterator(); lt.hasNext();)
         {
            init((DeploymentInfo) lt.next());
         }
      }
      catch (Exception e)
      {
         deployment.state = DeploymentState.FAILED;
         DeploymentException.rethrowAsDeploymentException("exception in init of " + deployment.url, e);
      }
      finally
      {
         // whether you do it or not, for the autodeployer
         try
         {
            URL url = deployment.localUrl == null ? deployment.url : deployment.localUrl;

            long lastModified = -1;

            if (url.getProtocol().equals("file"))
               lastModified = new File(url.getFile()).lastModified();
            else
               lastModified = url.openConnection().getLastModified();

            deployment.lastModified=lastModified;
            deployment.lastDeployed=System.currentTimeMillis();
         }
         catch (IOException ignore)
         {
            deployment.lastModified=System.currentTimeMillis();
            deployment.lastDeployed=System.currentTimeMillis();
         }

         synchronized (deploymentList)
         {
            // Do we watch it? Watch only urls outside our copy directory.
            if (!inLocalCopyDir(deployment.url) && deploymentList.contains(deployment) == false)
            {
               deploymentList.add(deployment);
               log.debug("Watching new file: " + deployment.url);
            }
         }
      }
      return true;
   }

   /**
    * The <code>create</code> method is the second internal deployment step.
    * It should set up all information not
    * requiring other components.  for instance, the ejb Container is created,
    * and the proxy bound into jndi.
    *
    * @param deployment a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    */
   private void create(DeploymentInfo deployment) throws DeploymentException
   {
      log.debug("create step for deployment " + deployment.url);
      try
      {
         ArrayList sortedSubs = new ArrayList(deployment.subDeployments);
         Collections.sort(sortedSubs, infoSorter);
         for (Iterator lt = sortedSubs.listIterator(); lt.hasNext();)
         {
            create((DeploymentInfo) lt.next());
         }
         deployment.state = DeploymentState.CREATE_SUBDEPLOYMENTS;

         // Deploy this SDI, if it is a deployable type
         if (deployment.deployer != null)
         {
            try
            {
               deployment.state = DeploymentState.CREATE_DEPLOYER;
               deployment.deployer.create(deployment);
               // See if all mbeans are created...
               deployment.state = DeploymentState.CREATED;
               deployment.status="Created";
               log.debug("Done with create step of deploying " + deployment.shortName);
            }
            catch (Throwable t)
            {
               log.error("Could not create deployment: " + deployment.url, t);
               throw t;
            }
         }
         else
         {
            log.debug("Still no deployer for package in create step: "  + deployment.shortName);
         } // end of else
      }
      catch (Throwable t)
      {
         log.trace("could not create deployment: " + deployment.url, t);
         deployment.status = "Deployment FAILED reason: " + t.getMessage();
         deployment.state = DeploymentState.FAILED;
         DeploymentException.rethrowAsDeploymentException("Could not create deployment: " + deployment.url, t);
      }
   }

   /**
    * The <code>start</code> method is the third and final internal deployment step.
    * The purpose is to set up relationships between components.
    * for instance, ejb links are set up here.
    *
    * @param deployment a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    */
   private void start(DeploymentInfo deployment) throws DeploymentException
   {
      deployment.status = "Starting";
      log.debug("Begin deployment start " + deployment.url);
      try
      {
         ArrayList sortedSubs = new ArrayList(deployment.subDeployments);
         Collections.sort(sortedSubs, infoSorter);
         for (Iterator lt = sortedSubs.listIterator(); lt.hasNext();)
         {
            start((DeploymentInfo) lt.next());
         }
         deployment.state = DeploymentState.START_SUBDEPLOYMENTS;

         // Deploy this SDI, if it is a deployable type
         if (deployment.deployer != null)
         {
            try
            {
               deployment.state = DeploymentState.START_DEPLOYER;
               deployment.deployer.start(deployment);
               // See if all mbeans are started...
               Object[] args = {deployment, DeploymentState.STARTED};
               String[] sig = {"org.jboss.deployment.DeploymentInfo",
                  "org.jboss.deployment.DeploymentState"};
               server.invoke(serviceController, "validateDeploymentState",args, sig);
               deployment.status = "Deployed";
               log.debug("End deployment start on package: "+ deployment.shortName);
            }
            catch (Throwable t)
            {
               log.error("Could not start deployment: " + deployment.url, t);
               throw t;
            }
         }
         else
         {
            log.debug("Still no deployer for package in start step: "  + deployment.shortName);
         } // end of else
      }
      catch (Throwable t)
      {
         log.trace("could not start deployment: " + deployment.url, t);
         deployment.state = DeploymentState.FAILED;
         deployment.status = "Deployment FAILED reason: " + t.getMessage();
         DeploymentException.rethrowAsDeploymentException("Could not create deployment: " + deployment.url, t);
      }
   }

   /**
    * The <code>findDeployer</code> method attempts to find a deployer for the DeploymentInfo
    * supplied as a parameter.
    *
    * @param sdi a <code>DeploymentInfo</code> value
    */
   private void findDeployer(DeploymentInfo sdi)
   {
      // If there is already a deployer use it
      if( sdi.deployer != null )
      {
         log.debug("using existing deployer "+sdi.deployer);
         return;
      }

      //
      // To deploy directories of beans one should just name the directory
      // mybean.ear/bla...bla, so that the directory gets picked up by the right deployer
      //
      synchronized(deployers)
      {
         for (Iterator iterator = deployers.iterator(); iterator.hasNext(); )
         {
            SubDeployer deployer = (SubDeployer) iterator.next();
            if (deployer.accepts(sdi))
            {
               sdi.deployer = deployer;
               log.debug("using deployer "+deployer);
               return;
            }
         }
      }
      log.debug("No deployer found for url: " + sdi.url);
   }

   /**
    * The <code>parseManifestLibraries</code> method looks into the manifest for classpath
    * goo, and tries to deploy referenced packages.
    *
    * @param sdi a <code>DeploymentInfo</code> value
    */
   private void parseManifestLibraries(DeploymentInfo sdi)
   {
      String classPath = null;

      Manifest mf = sdi.getManifest();

      if( mf != null )
      {
         Attributes mainAttributes = mf.getMainAttributes();
         classPath = mainAttributes.getValue(Attributes.Name.CLASS_PATH);
      }

      if (classPath != null)
      {
         StringTokenizer st = new StringTokenizer(classPath);
         log.debug("resolveLibraries: "+classPath);

         while (st.hasMoreTokens())
         {
            URL lib = null;
            String tk = st.nextToken();
            log.debug("new manifest entry for sdi at "+sdi.shortName+" entry is "+tk);

            try
            {
               if (sdi.isDirectory)
               {
                  File parentDir = new File(sdi.url.getPath()).getParentFile();
                  lib = new File(parentDir, tk).toURL();
               }
               else
               {
                  lib = new URL(sdi.url, tk);
               }

               // Only deploy this if it is not already being deployed
               if ( deploymentMap.containsKey(lib) == false )
               {
                  /* Test that the only deployer for this is the JARDeployer.
                   Any other type of deployment cannot be initiated through
                   a manifest reference.
                  */
                  DeploymentInfo mfRef = new DeploymentInfo(lib, null, getServer());
                  makeLocalCopy(mfRef);
                  URL[] localURL = {mfRef.localUrl};
                  mfRef.localCl = new java.net.URLClassLoader(localURL);
                  findDeployer(mfRef);
                  SubDeployer deployer = mfRef.deployer;
                  if(deployer != null && (deployer instanceof JARDeployer) == false)
                  {
                     // Its a non-jar deployment that must be deployed seperately
                     log.warn("Found non-jar deployer for " + tk + ": " + deployer);
                  }

                  // add the library
                  sdi.addLibraryJar(lib);
               }
            }
            catch (Exception ignore)
            {
               log.debug("The manifest entry in "+sdi.url+" references URL "+lib+
                  " which could not be opened, entry ignored", ignore);
            }
         }
      }
   }

   /**
    * Downloads the jar file or directory the src URL points to.
    * In case of directory it becomes packed to a jar file.
    */
   private void makeLocalCopy(DeploymentInfo sdi)
   {
      try
      {
         if (sdi.url.getProtocol().equals("file") && (!copyFiles || sdi.isDirectory))
         {
            // If local copies have been disabled, do nothing
            sdi.localUrl = sdi.url;
            return;
         }
         // Are we already in the localCopyDir?
         else if (inLocalCopyDir(sdi.url))
         {
            sdi.localUrl = sdi.url;
            return;
         }
         else
         {
            String shortName = sdi.shortName;
            File localFile = File.createTempFile("tmp", shortName, tempDir);
            sdi.localUrl = localFile.toURL();
            copy(sdi.url, localFile);
         }
      }
      catch (Exception e)
      {
         log.error("Could not make local copy for " + sdi.url, e);
      }
   }

   private boolean inLocalCopyDir(URL url)
   {
      int i = 0;
      String urlTest = url.toString();
      if( urlTest.startsWith("jar:") )
         i = 4;

      return urlTest.startsWith(tempDirString, i);
   }

   protected void copy(URL src, File dest) throws IOException
   {
      log.debug("Copying " + src + " -> " + dest);

      // Validate that the dest parent directory structure exists
      File dir = dest.getParentFile();
      if (!dir.exists())
      {
         boolean created = dir.mkdirs();
         if( created == false )
            throw new IOException("mkdirs failed for: "+dir.getAbsolutePath());
      }

      // Remove any existing dest content
      if( dest.exists() == true )
      {
         boolean deleted = Files.delete(dest);
         if( deleted == false )
            throw new IOException("delete of previous content failed for: "+dest.getAbsolutePath());
      }

      if (src.getProtocol().equals("file"))
      {
         File srcFile = new File(src.getFile());
         if (srcFile.isDirectory())
         {
            log.debug("Making zip copy of: " + srcFile);
            // make a jar archive of the directory
            OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
            JarUtils.jar(out, srcFile.listFiles());
            out.close();
            return;
         }
      }

      InputStream in = new BufferedInputStream(src.openStream());
      OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
      Streams.copy(in, out);
      out.flush();
      out.close();
      in.close();
   }

   /**
    * The <code>start</code> method starts a package identified by a URL
    *
    * @param urlspec an <code>URL</code> value
    * @jmx.managed-operation
    */
   public void start(String urlspec)
      throws DeploymentException, MalformedURLException
   {
      throw new DeploymentException("Not supported");
   }

   /**
    * The <code>stop</code> method stops a package identified by a URL
    *
    * @param urlspec an <code>URL</code> value
    * @jmx.managed-operation
    */
   public void stop(String urlspec)
      throws DeploymentException, MalformedURLException
   {
      throw new DeploymentException("Not supported");
   }

   /**
    * The <code>isDeployed</code> method tells you if a package identified by a string
    * representation of a URL is currently deployed.
    *
    * @param url a <code>String</code> value
    * @return a <code>boolean</code> value
    * @exception MalformedURLException if an error occurs
    * @jmx.managed-operation
    */
   public boolean isDeployed(String url)
      throws MalformedURLException
   {
      URL realUrl;
      try
      {
         realUrl = new URL(url);
      }
      catch (MalformedURLException e)
      {
         File file = new File(url);
         realUrl = file.toURL();
      }
      return isDeployed(realUrl);
   }

   /**
    * The <code>isDeployed</code> method tells you if a packaged identified by
    * a URL is deployed.
    * @param url an <code>URL</code> value
    * @return a <code>boolean</code> value
    * @jmx.managed-operation
    */
   public boolean isDeployed(URL url)
   {
      try
      {
         String name = contextMap.get(url);
         if (name == null)
         {                        
            name = getDeploymentName(url);
            if(name == null)
               return false;
         }
   
         return checkDeployed(name);
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Is deployed.
    *
    * @param name the name of the deployment
    * @return true if deployed, false otherwise
    */
   protected boolean checkDeployed(String name)
   {
      org.jboss.deployers.spi.DeploymentState deploymentState = delegate.getDeploymentState(name);
      log.debug("isDeployed, url="+name+", state="+deploymentState);
      return deploymentState == org.jboss.deployers.spi.DeploymentState.DEPLOYED;
   }

   /**
    * The <code>getDeployment</code> method returns the Deployment
    * object for the URL supplied.
    *
    * @param url an <code>URL</code> value
    * @return a <code>Deployment</code> value
    * @jmx.managed-operation
    */
   public Deployment getDeployment(URL url)
   {
      String name = contextMap.get(url);
      if (name == null)
         return null;

      Deployment dc = delegate.getDeployment(name);
      log.debug("getDeployment, url="+url+", dc="+dc);
      return dc;
   }

   /**
    * The <code>getDeploymentContext</code> method returns the DeploymentContext
    * object for the URL supplied.
    *
    * @param url an <code>URL</code> value
    * @return a <code>DeploymentContext</code> value
    * @jmx.managed-operation
    */
   @Deprecated
   public DeploymentContext getDeploymentContext(URL url)
   {
      String name = contextMap.get(url);
      if (name == null)
         return null;

      MainDeployerStructure structure = (MainDeployerStructure) delegate;
      DeploymentContext dc = structure.getDeploymentContext(name);
      log.debug("getDeploymentContext, url="+url+", dc="+dc);
      return dc;
   }

   /**
    * The <code>getDeploymentUnit</code> method returns the DeploymentUnit
    * object for the URL supplied.
    *
    * @param url an <code>URL</code> value
    * @return a <code>DeploymentUnit</code> value
    * @jmx.managed-operation
    */
   public DeploymentUnit getDeploymentUnit(URL url)
   {
      String name = contextMap.get(url);
      if (name == null)
         return null;

      MainDeployerStructure structure = (MainDeployerStructure) delegate;
      DeploymentUnit du = structure.getDeploymentUnit(name);
      log.debug("getDeploymentUnit, url="+url+", du="+du);
      return du;
   }

   /**
    * The <code>getWatchUrl</code> method returns the URL that, when modified,
    * indicates that a redeploy is needed.
    *
    * @param url an <code>URL</code> value
    * @return a <code>URL</code> value
    * @jmx.managed-operation
    */
   public URL getWatchUrl(URL url)
   {
      return url;
   }

   /** Check the current deployment states and generate a IncompleteDeploymentException
    * if there are mbeans waiting for depedencies.
    * @exception IncompleteDeploymentException
    * @jmx.managed-operation
    */
   public void checkIncompleteDeployments() throws DeploymentException
   {
      try
      {
         delegate.checkComplete();
      }
      catch (Exception e)
      {
         throw new DeploymentException("Deployments are incomplete", e);
      }
   }

   /**
    * @param parent
    * @param map
    */
   private void fillParentAndChildrenSDI(DeploymentInfo parent, Map map)
   {
      Set subDeployments = parent.subDeployments;
      Iterator it = subDeployments.iterator();
      while (it.hasNext())
      {
         DeploymentInfo child = (DeploymentInfo) it.next();
         SerializableDeploymentInfo sdichild = returnSDI(child, map);
         sdichild.parent = returnSDI(parent, map);
         sdichild.parent.subDeployments.add(sdichild);
         fillParentAndChildrenSDI(child, map);
      }
   }

   private SerializableDeploymentInfo returnSDI(DeploymentInfo di, Map map)
   {
       SerializableDeploymentInfo sdi = (SerializableDeploymentInfo) map.get(di.url);
       if( sdi == null )
       {
           sdi = new SerializableDeploymentInfo(di);
           map.put(di.url, sdi);
        }
       return sdi;
   }

   // Helper Methods for JBPAPP-6716 - we are calling the KernelController because if we had used injection we would need to change the MBean interface,
   // which could cause backwards compatibilty issues for customers who are utilizing the current EAP 5.x MainDeployerMBean interface       
   private DeploymentInfo createDeploymentInfo(String urlPath, org.jboss.deployment.DeploymentState state, String status) throws DeploymentException, MalformedURLException
   {            
      if(urlPath.startsWith("vfs"))
      {
         urlPath = "file" + urlPath.substring(urlPath.indexOf(":"));
      }
      
      DeploymentInfo di = new DeploymentInfo(new URL(urlPath), null, null);
      di.state = state;
      di.status = status;
      
      // this is a hack to prevent SerializableDeploymentInfo from having a null pointer
      di.deployer = new JARDeployer();
      
      return di;
   }
         
   private Collection getAllDeployments()
   {
      Date start = new Date();   
      Collection<Deployment> deployments = delegate.getTopLevel();
      
      List<DeploymentInfo> deploymentInfos = new ArrayList<DeploymentInfo>(deployments.size() + contextMap.size());
                                 
      for(Deployment deployment : deployments)
      {
         try
         {
            org.jboss.deployers.spi.DeploymentState deploymentState = delegate.getDeploymentState(deployment.getName());                  
            org.jboss.deployment.DeploymentState state = null;
            String status = deploymentState.toString();
            
            // convert deployment state to DeploymentInfo state
            if(deploymentState == org.jboss.deployers.spi.DeploymentState.DEPLOYED)
            {
               state = org.jboss.deployment.DeploymentState.STARTED;               
            }
            else if(deploymentState == org.jboss.deployers.spi.DeploymentState.ERROR)
            {
               state = org.jboss.deployment.DeploymentState.FAILED;               
            }
            else if(deploymentState == org.jboss.deployers.spi.DeploymentState.UNDEPLOYED)
            {
               state = org.jboss.deployment.DeploymentState.STOPPED;               
            }
                                    
            deploymentInfos.add(createDeploymentInfo(deployment.getName(), state, status));            
         }
         catch(Exception e)
         {
            log.warn("Unable to convert deployment: " + deployment + " to DeploymentInfo", e);
         }
      }
            
      log.trace("getAllDeployments: " + (( new Date().getTime() - start.getTime() ) /1000));
      return deploymentInfos;
   }
      
   private String getDeploymentName(URL url)
   {
      try
      {
         VirtualFile file = VFS.createNewRoot(url);         
         VFSDeployment deployment = deploymentFactory.createVFSDeployment(file);
         return deployment.getName();
      }
      catch(IOException io)
      {
         // return null since the url was not valid
         return null;
      }      
   }  
   // End of : Helper Methods for JBPAPP-6716
}
