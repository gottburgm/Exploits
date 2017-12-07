/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.deployers.plugins.scanner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.logging.Logger;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;


/**
 * A DeploymentScanner built on the ProfileService and MainDeployer.
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85526 $
 */
public class VFSDeploymentScannerImpl
   implements Runnable
{
   private static final Logger log = Logger.getLogger(VFSDeploymentScannerImpl.class);
   // Private Data --------------------------------------------------
   private MainDeployer mainDeployer;

   /** The deployment factory */
   private VFSDeploymentFactory deploymentFactory = VFSDeploymentFactory.getInstance();
   
   /** */
   private VirtualFileFilter filter;
   /** The ExecutorService/ThreadPool for performing scans */
   private ScheduledExecutorService scanExecutor;
   private ScheduledFuture activeScan;

   /** The URIfied ServerHomeURL */
   private URI serverHomeURI;
   
   /** The list of URIs to scan */
   private List<URI> uriList = Collections.synchronizedList(new ArrayList<URI>());
   
   /** The list of VirtualFiles to scan */
   private List<VirtualFile> vdfList = Collections.synchronizedList(new ArrayList<VirtualFile>());
   
   /** Whether to search for files inside directories whose names containing no dots */
   private boolean doRecursiveSearch = true;
   /** Period in ms between deployment scans */
   private long scanPeriod = 5000;
   private int scanCount;

   /** A set of scanned VirtualFiles which have been deployed */
   private Map<VirtualFile, DeploymentInfo> deployedMap = new ConcurrentHashMap<VirtualFile, DeploymentInfo>();

   // Constructor ---------------------------------------------------
   
   public VFSDeploymentScannerImpl()
   {
      // empty
   }
   
   // Attributes ----------------------------------------------------

   public void setMainDeployer(MainDeployer deployer)
   {
      this.mainDeployer = deployer;
   }

   public VirtualFileFilter getFilterInstance()
   {
      return filter;
   }
   public void setFilterInstance(VirtualFileFilter filter)
   {
      this.filter = filter;
   }

   /**
    * @return Returns the scanExecutor.
    */
   public ScheduledExecutorService getScanExecutor()
   {
      return this.scanExecutor;
   }

   /**
    * @param scanExecutor The scanExecutor to set.
    */
   public void setScanExecutor(ScheduledExecutorService scanExecutor)
   {
      this.scanExecutor = scanExecutor;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployment.scanner.VFSDeploymentScanner#getScanPeriod()
    */
   public long getScanPeriod()
   {
      return scanPeriod;
   }
   /* (non-Javadoc)
    * @see org.jboss.deployment.scanner.VFSDeploymentScanner#setScanPeriod(long)
    */
   public void setScanPeriod(long period)
   {
      this.scanPeriod = period;
   }

   /** 
    * Are deployment scans enabled.
    * 
    * @return whether scan is enabled
    */
   public boolean isScanEnabled()
   {
      return activeScan != null;
   }

   public synchronized int getScanCount()
   {
      return scanCount;
   }
   public synchronized void resetScanCount()
   {
      this.scanCount = 0;
   }

   /**
    * Enable/disable deployment scans.
    * @param scanEnabled true to enable scans, false to disable.
    */
   public synchronized void setScanEnabled(boolean scanEnabled)
   {
      if( scanEnabled == true && activeScan == null )
      {
         activeScan = this.scanExecutor.scheduleWithFixedDelay(this, 0,
               scanPeriod, TimeUnit.MILLISECONDS);
      }
      else if( scanEnabled == false && activeScan != null )
      {
         activeScan.cancel(true);
         activeScan = null;
      }
   }

   /**
    * Set the urls to scan 
    * 
    * @param listspec the urls
    * @throws URISyntaxException
    * @throws IOException
    */
   public void setURIs(final String listspec) throws URISyntaxException, IOException
   {
      if (listspec == null)
      {
         this.uriList.clear();
         return;
      }
      List<URI> list = new LinkedList<URI>();

      StringTokenizer stok = new StringTokenizer(listspec, ",");
      while (stok.hasMoreTokens())
      {
         String urispec = stok.nextToken().trim();
   
         log.debug("Adding URI from spec: " + urispec);
   
         URI uri = makeURI(urispec);

         log.debug("URI: " + uri);

         list.add(uri);
      }
      setURIList(list);
   }

   /**
    * Set uris to scan
    * 
    * @param list the urls to scan
    * @throws IOException
    */
   public void setURIList(final List<URI> list) throws IOException
   {
      if (list == null)
      {
         return;
      }

      // start out with a fresh list
      uriList.clear();
   
      for(int n = 0; n < list.size(); n ++)
      {
         URI uri = list.get(n);
         if (uri == null)
         {
            throw new IllegalArgumentException("list element["+n+"] is null");
         }
         addURI(uri);
      }
      log.debug("URI list: " + uriList);
   }
   
   public List<URI> getURIList()
   {
      return new ArrayList<URI>(uriList);
   }

   public void setRecursiveSearch(boolean recurse)
   {
      doRecursiveSearch = recurse;
   }
   
   public boolean getRecursiveSearch()
   {
      return doRecursiveSearch;
   }

   // Operations ----------------------------------------------------
   
   public void addURI(final URI uri) throws IOException
   {
      if (uri == null)
      {
         throw new NullPointerException("uri argument cannot be null");
      }
      if( uriList.add(uri) == true )
      {
         log.debug("Added URI: " + uri);
         VirtualFile vf = VFS.getRoot(uri);
         vdfList.add(vf);
      }      
   }

   public void removeURI(final URI uri)
      throws IOException
   {
      if (uri == null)
      {
         throw new NullPointerException("uri argument cannot be null");
      }
      VirtualFile vf = VFS.getRoot(uri);
      vdfList.remove(vf);
      boolean success = uriList.remove(uri);
      
      if (success)
      {
         log.debug("Removed URI: " + uri);
      }
   }
   
   public boolean hasURI(final URI uri)
   {
      if (uri == null)
      {
         throw new NullPointerException("uri argument cannot be null");
      }
      return uriList.contains(uri);
   }

   public void start() throws Exception
   {
      // synchronize uriList and vdfList because only at this point
      // setVirtualFileFactory() injection has been performed
      vdfList.clear();
      for (URI uri : uriList)
      {
         VirtualFile vf = VFS.getRoot(uri);
         vdfList.add(vf);
      }

      // Default to a single thread executor
      if( scanExecutor == null )
      {
         scanExecutor = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactory()
            {
               public Thread newThread(Runnable r)
               {
                  return new Thread(r, "VFSDeploymentScanner");
               }
            }
        );
      }
      activeScan = scanExecutor.scheduleWithFixedDelay(this, 0,
            scanPeriod, TimeUnit.MILLISECONDS);
   }

   /**
    * Executes scan 
    *
    */
   public void run()
   {
      try
      {
         scan();
      }
      catch(Throwable e)
      {
         log.warn("Scan failed", e);
      }
      finally
      {
         incScanCount();
      }
   }

   public void stop()
   {
      if( activeScan != null )
      {
         activeScan.cancel(true);
         activeScan = null;
      }
   }

   public synchronized void scan() throws Exception
   {
      if (vdfList == null)
      {
         throw new IllegalStateException("not initialized");
      }
      boolean trace = log.isTraceEnabled();

      // Scan for deployments
      log.debug("Begin deployment scan");

      // VirtualFiles to deploy
      List<VirtualFile> toDeployList = new LinkedList<VirtualFile>();
      synchronized (vdfList)
      {
         for (VirtualFile vf : vdfList)
         {
            if( trace )
               log.trace("Checking file: "+vf);
            if (vf.isLeaf())
            {
               // treat this as a deployable unit
               toDeployList.add(vf);
            }
            else
            {
               // process (possibly recursively) the dir
               addDeployments(toDeployList, vf);
            }
         }
      }

      if (trace)
      {
         log.trace("toDeployList: "+toDeployList);
      }
      LinkedList<VirtualFile> toRemoveList = new LinkedList<VirtualFile>();
      LinkedList<VirtualFile> toCheckForUpdateList = new LinkedList<VirtualFile>();

      synchronized (deployedMap)
      {
         // remove previously deployed URLs no longer needed
         Iterator<VirtualFile> iter = deployedMap.keySet().iterator();
         while( iter.hasNext() )
         {
            VirtualFile vf = iter.next();
            if (toDeployList.contains(vf))
            {
               toCheckForUpdateList.add(vf);
            }
            else
            {
               toRemoveList.add(vf);
            }
         }
      }

      // ********
      // Undeploy
      // ********
   
      for (VirtualFile vf : toRemoveList)
      {
         undeploy(vf);
      }

      // ********
      // Redeploy
      // ********

      // compute the DeployedURL list to update
      ArrayList<VirtualFile> toUpdateList = new ArrayList<VirtualFile>(toCheckForUpdateList.size());
      for (VirtualFile vf : toUpdateList)
      {
         DeploymentInfo info = deployedMap.get(vf);
         long modified = vf.getLastModified();
         Long prevLastDeployed = info.lastModified;;
         if (prevLastDeployed.compareTo(modified) < 0)
         {
            info.lastModified = modified;
            if (trace)
            {
               log.trace("Re-deploying " + vf);
            }
            toUpdateList.add(vf);
         }
      }

      // sort to update list
      //Collections.sort(toUpdateList, sorter);

      // Undeploy in order
      for (int i = toUpdateList.size() - 1; i >= 0; i--)
      {
         VirtualFile vf = toUpdateList.get(i);
         undeploy(vf);
      }

      // Deploy in order
      for (int i = 0; i < toUpdateList.size(); i++)
      {
         VirtualFile vf = toUpdateList.get(i);
         deploy(vf);
      }
   
      // ******
      // Deploy
      // ******

      //Collections.sort(toDeployList, sorter);
      for (Iterator i = toDeployList.iterator(); i.hasNext();)
      {
         VirtualFile vf = (VirtualFile)i.next();
         
         // if vf is not deployed already, deploy it
         if (!deployedMap.containsKey(vf))
         {
            deploy(vf);
         }

         // vf must have been deployed by now, so remove it from list 
         i.remove();
         
      }   
      log.debug("End deployment scan");
   }

   /**
    * Inc the scanCount and to a notifyAll.
    *
    */
   protected synchronized void incScanCount()
   {
      scanCount ++;
      notifyAll();
   }

   // Private -------------------------------------------------------
   
    /**
     * A helper to make a URI from a full/partial urispec
     */
   private URI makeURI(String urispec) throws URISyntaxException
   {
      // First replace URI with appropriate properties
      urispec = StringPropertyReplacer.replaceProperties(urispec);
      return serverHomeURI.resolve(urispec);
   }
   
   /**
    * A helper to find all deployments under a directory vf
    * and add them to the supplied list.
    * 
    * We may recurse.
    */
   private void addDeployments(List<VirtualFile> list, VirtualFile root)
      throws IOException
   {
      List<VirtualFile> components = root.getChildren();
      
      for (VirtualFile vf : components)
      {
         if (vf.isLeaf())
         {
            // the first arg in filter.accept is not used!
            if (filter == null || filter.accepts(vf))
            {
               list.add(vf);
            }            
         }
         else
         {
            if (vf.getName().indexOf('.') == -1 && this.doRecursiveSearch)
            {
               // recurse if not '.' in name and recursive search is enabled
               addDeployments(list, vf);
            }
            else
            {
               list.add(vf);
            }
         }
      }
   }

   /**
    * A helper to deploy the given vf using the deployer.
    */
   private void deploy(final VirtualFile vf)
   {
      // If the deployer is null simply ignore the request
      log.debug("Deploying: " + vf);
      Deployment deployment = deploymentFactory.createVFSDeployment(vf);
      try
      {
         mainDeployer.addDeployment(deployment);
         mainDeployer.process();
      }
      catch (Exception e)
      {
         log.warn("Failed to deploy: " + vf, e);
         // TODO: somehow need to ignore bad deployments to avoid repeated errors
         return;
      }

      /* TODO: this differs from the previous behavior. We would need a type to metainf location
       if we want to watch the same file as jboss4. But since not all files have a deployment
       descriptor, we need to be able to watch the deployment root anyway.
      */
      try
      {
         DeploymentInfo info = new DeploymentInfo(deployment, vf.getLastModified());
         if (!deployedMap.containsKey(vf))
         {
            deployedMap.put(vf, info);
         }
      }
      catch(IOException e)
      {
         log.warn("Failed to obtain lastModified for: "+vf, e);
      }
   }

   /**
    * A helper to undeploy the given vf using the deployer.
    */
   private void undeploy(final VirtualFile vf)
   {
      try
      {
         log.debug("Undeploying: " + vf);
         DeploymentInfo info = deployedMap.remove(vf);
         mainDeployer.removeDeployment(info.deployment);
      }
      catch (Exception e)
      {
         log.error("Failed to undeploy: " + vf, e);
      }
   }

   /**
    * DeploymentInfo.
    */
   private class DeploymentInfo
   {
      /** The deployment */
      Deployment deployment;
      
      /** The last modified time */
      long lastModified;
      
      /**
       * Create a new DeploymentInfo.
       * 
       * @param deployment the deployment
       * @param lastModified the last modified
       */
      public DeploymentInfo(Deployment deployment, long lastModified)
      {
         if (deployment == null)
            throw new IllegalArgumentException("Null deployment");
         this.deployment = deployment;
         this.lastModified = lastModified;
      }
   }
}
