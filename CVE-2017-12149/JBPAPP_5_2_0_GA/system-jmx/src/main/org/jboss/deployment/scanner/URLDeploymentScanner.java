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
package org.jboss.deployment.scanner;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.deployment.DefaultDeploymentSorter;
import org.jboss.deployment.IncompleteDeploymentException;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.net.protocol.URLLister;
import org.jboss.net.protocol.URLListerFactory;
import org.jboss.net.protocol.URLLister.URLFilter;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.util.NullArgumentException;
import org.jboss.util.StringPropertyReplacer;

/**
 * A URL-based deployment scanner.  Supports local directory
 * scanning for file-based urls.
 *
 * @jmx:mbean extends="org.jboss.deployment.scanner.DeploymentScannerMBean"
 *
 * @version <tt>$Revision: 81033 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 */
public class URLDeploymentScanner extends AbstractDeploymentScanner
   implements DeploymentScanner, URLDeploymentScannerMBean
{
   /** A set of deployment URLs to skip **/
   protected Set skipSet = Collections.synchronizedSet(new HashSet());
   
   /** The list of URLs to scan. */
   protected List urlList = Collections.synchronizedList(new ArrayList());

   /** A set of scanned urls which have been deployed. */
   protected Set deployedSet = Collections.synchronizedSet(new HashSet());

   /** Helper for listing local/remote directory URLs */
   protected URLListerFactory listerFactory = new URLListerFactory();
   
   /** The server's home directory, for relative paths. */
   protected File serverHome;

   protected URL serverHomeURL;

   /** A sorter urls from a scaned directory to allow for coarse dependency
    ordering based on file type
    */
   protected Comparator sorter;

   /** Allow a filter for scanned directories */
   protected URLFilter filter;
   
   protected IncompleteDeploymentException lastIncompleteDeploymentException;
   
   /** Whether to search inside directories whose names containing no dots */
   protected boolean doRecursiveSearch = true;
   
   /**
    * @jmx:managed-attribute
    */
   public void setRecursiveSearch (boolean recurse)
   {
      doRecursiveSearch = recurse;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public boolean getRecursiveSearch ()
   {
      return doRecursiveSearch;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void setURLList(final List list)
   {
      if (list == null)
         throw new NullArgumentException("list");

      // start out with a fresh list
      urlList.clear();

      Iterator iter = list.iterator();
      while (iter.hasNext())
      {
         URL url = (URL)iter.next();
         if (url == null)
            throw new NullArgumentException("list element");

         addURL(url);
      }
      
      log.debug("URL list: " + urlList);
   }

   /**
    * @jmx:managed-attribute
    *
    * @param classname    The name of a Comparator class.
    */
   public void setURLComparator(String classname)
      throws ClassNotFoundException, IllegalAccessException,
      InstantiationException
   {
      sorter = (Comparator)Thread.currentThread().getContextClassLoader().loadClass(classname).newInstance();
   }

   /**
    * @jmx:managed-attribute
    */
   public String getURLComparator()
   {
      if (sorter == null)
         return null;
      return sorter.getClass().getName();
   }

   /**
    * @jmx:managed-attribute
    *
    * @param classname    The name of a FileFilter class.
    */
   public void setFilter(String classname)
   throws ClassNotFoundException, IllegalAccessException, InstantiationException
   {
      Class filterClass = Thread.currentThread().getContextClassLoader().loadClass(classname);
      filter = (URLFilter) filterClass.newInstance();
   }

   /**
    * @jmx:managed-attribute
    */
   public String getFilter()
   {
      if (filter == null)
         return null;
      return filter.getClass().getName();
   }

   
   /**
    * @jmx:managed-attribute
    *
    * @param filter The URLFilter instance
    */
   public void setFilterInstance(URLFilter filter)
   {
      this.filter = filter;
   }

   /**
    * @jmx:managed-attribute
    */
   public URLFilter getFilterInstance()
   {
      return filter;
   }

   /**
    * @jmx:managed-attribute
    */
   public List getURLList()
   {
      // too bad, List isn't a cloneable
      return new ArrayList(urlList);
   }

   /**
    * @jmx:managed-operation
    */
   public void addURL(final URL url)
   {
      if (url == null)
         throw new NullArgumentException("url");
      
      try
      {
         // check if this is a valid url
         url.openConnection().connect();
      }
      catch (IOException e)
      {
         // either a bad configuration (non-existent url) or a transient i/o error
         log.warn("addURL(), caught " + e.getClass().getName() + ": " + e.getMessage());
      }
      urlList.add(url);
      
      log.debug("Added url: " + url);
   }

   /**
    * @jmx:managed-operation
    */
   public void removeURL(final URL url)
   {
      if (url == null)
         throw new NullArgumentException("url");

      boolean success = urlList.remove(url);
      if (success)
      {
         log.debug("Removed url: " + url);
      }
   }

   /**
    * @jmx:managed-operation
    */
   public boolean hasURL(final URL url)
   {
      if (url == null)
         throw new NullArgumentException("url");

      return urlList.contains(url);
   }

   /**
    * Temporarily ignore changes (addition, updates, removal) to a particular
    * deployment, identified by its deployment URL. The deployment URL is different
    * from the 'base' URLs that are scanned by the scanner (e.g. the full path to
    * deploy/jmx-console.war vs. deploy/). This can be used to avoid an attempt
    * by the scanner to deploy/redeploy/undeploy a URL that is being modified.
    * 
    * To re-enable scanning of changes for a URL, use resumeDeployment(URL, boolean).
    *
    * @jmx:managed-operation
    */
   public void suspendDeployment(URL url)
   {
      if (url == null)
         throw new NullArgumentException("url");
      
      if (skipSet.add(url))
         log.debug("Deployment URL added to skipSet: " + url);
      else
         throw new IllegalStateException("Deployment URL already suspended: " + url);
   }

   /**
    * Re-enables scanning of a particular deployment URL, previously suspended
    * using suspendDeployment(URL). If the markUpToDate flag is true then the
    * deployment module will be considered up-to-date during the next scan.
    * If the flag is false, at the next scan the scanner will check the
    * modification date to decide if the module needs deploy/redeploy/undeploy.
    *
    * @jmx:managed-operation
    */
   public void resumeDeployment(URL url, boolean markUpToDate)
   {
      if (url == null)
         throw new NullArgumentException("url");
      
      if (skipSet.contains(url))
      {
         if (markUpToDate)
         {
            // look for the deployment and mark it as uptodate
            for (Iterator i = deployedSet.iterator(); i.hasNext(); )
            {
               DeployedURL deployedURL = (DeployedURL)i.next();
               if (deployedURL.url.equals(url))
               {
                  // the module could have been removed..
                  log.debug("Marking up-to-date: " + url);                  
                  deployedURL.deployed();
                  break;
               }
            }
         }
         // don't skip this url anymore
         skipSet.remove(url);
         log.debug("Deployment URL removed from skipSet: " + url);
      }
      else
      {
         throw new IllegalStateException("Deployment URL not suspended: " + url);
      }
   }

   /**
    * Lists all urls deployed by the scanner, each URL on a new line.
    *
    * @jmx:managed-operation
    */
   public String listDeployedURLs()
   {
      StringBuffer sbuf = new StringBuffer();
      for (Iterator i = deployedSet.iterator(); i.hasNext(); )
      {
         URL url = ((DeployedURL)i.next()).url;
         if (sbuf.length() > 0)
         {
            sbuf.append("\n").append(url);
         } 
         else 
         {
            sbuf.append(url);
         }
      }
      return sbuf.toString();
   }

   /////////////////////////////////////////////////////////////////////////
   //                  Management/Configuration Helpers                   //
   /////////////////////////////////////////////////////////////////////////

   /**
    * @jmx:managed-attribute
    */
   public void setURLs(final String listspec) throws MalformedURLException
   {
      if (listspec == null)
         throw new NullArgumentException("listspec");

      List list = new LinkedList();

      StringTokenizer stok = new StringTokenizer(listspec, ",");
      while (stok.hasMoreTokens())
      {
         String urlspec = stok.nextToken().trim();
         log.debug("Adding URL from spec: " + urlspec);

         URL url = makeURL(urlspec);
         log.debug("URL: " + url);
         
         list.add(url);
      }

      setURLList(list);
   }

   /**
    * A helper to make a URL from a full url, or a filespec.
    */
   protected URL makeURL(String urlspec) throws MalformedURLException
   {
      // First replace URL with appropriate properties
      //
      urlspec = StringPropertyReplacer.replaceProperties (urlspec);
      return new URL(serverHomeURL, urlspec);
   }

   /**
    * @jmx:managed-operation
    */
   public void addURL(final String urlspec) throws MalformedURLException
   {
      addURL(makeURL(urlspec));
   }

   /**
    * @jmx:managed-operation
    */
   public void removeURL(final String urlspec) throws MalformedURLException
   {
      removeURL(makeURL(urlspec));
   }

   /**
    * @jmx:managed-operation
    */
   public boolean hasURL(final String urlspec) throws MalformedURLException
   {
      return hasURL(makeURL(urlspec));
   }

   /**
    * A helper to deploy the given URL with the deployer.
    */
   protected void deploy(final DeployedURL du)
   {
      // If the deployer is null simply ignore the request
      if (deployer == null)
         return;
      
      try
      {
         if (log.isTraceEnabled())
            log.trace("Deploying: " + du);

         deployer.deploy(du.url);
      }
      catch (IncompleteDeploymentException e)
      {
         lastIncompleteDeploymentException = e;
      }
      catch (Exception e)
      {
         log.debug("Failed to deploy: " + du, e);
      }

      du.deployed();

      if (!deployedSet.contains(du))
      {
         deployedSet.add(du);
      }
   }

   /**
    * A helper to undeploy the given URL from the deployer.
    */
   protected void undeploy(final DeployedURL du)
   {
      try
      {
         if (log.isTraceEnabled())
            log.trace("Undeploying: " + du);

         deployer.undeploy(du.url);
         deployedSet.remove(du);
      }
      catch (Exception e)
      {
         log.error("Failed to undeploy: " + du, e);
      }
   }

   /**
    * Checks if the url is in the deployed set.
    */
   protected boolean isDeployed(final URL url)
   {
      DeployedURL du = new DeployedURL(url);
      return deployedSet.contains(du);
   }

   public synchronized void scan() throws Exception
   {
      lastIncompleteDeploymentException = null;
      if (urlList == null)
         throw new IllegalStateException("not initialized");

      updateSorter();

      boolean trace = log.isTraceEnabled();
      List urlsToDeploy = new LinkedList();

      // Scan for deployments
      if (trace)
      {
         log.trace("Scanning for new deployments");
      }
      synchronized (urlList)
      {
         for (Iterator i = urlList.iterator(); i.hasNext();)
         {
            URL url = (URL) i.next();
            try
            {
               if (url.toString().endsWith("/"))
               {
                  // treat URL as a collection
                  URLLister lister = listerFactory.createURLLister(url);
                  
                  // listMembers() will throw an IOException if collection url does not exist
                  urlsToDeploy.addAll(lister.listMembers(url, filter, doRecursiveSearch));
               } 
               else
               {
                  // treat URL as a deployable unit
                  
                  // throws IOException if this URL does not exist
                  url.openConnection().connect();
                  urlsToDeploy.add(url);
               }
            }
            catch (IOException e)
            {
               // Either one of the configured URLs is bad, i.e. points to a non-existent
               // location, or it ends with a '/' but it is not a directory (so it
               // is really user's fault), OR some other hopefully transient I/O error
               // happened (e.g. out of file descriptors?) so log a warning.
               log.warn("Scan URL, caught " + e.getClass().getName() + ": " + e.getMessage());
               
               // We need to return because at least one of the listed URLs will
               // return no results, and so all deployments starting from that point
               // (e.g. deploy/) will get undeployed, see JBAS-3107.
               // On the other hand, in case of a bad configuration nothing will get
               // deployed. If really want independence of e.g. 2 deploy urls, more
               // than one URLDeploymentScanners can be setup.
               return;
            }
         }
      }

      if (trace)
      {
         log.trace("Updating existing deployments");
      }
      LinkedList urlsToRemove = new LinkedList();
      LinkedList urlsToCheckForUpdate = new LinkedList();
      synchronized (deployedSet)
      {
         // remove previously deployed URLs no longer needed
         for (Iterator i = deployedSet.iterator(); i.hasNext();)
         {
            DeployedURL deployedURL = (DeployedURL) i.next();
            
            if (skipSet.contains(deployedURL.url))
            {
               if (trace)
                  log.trace("Skipping update/removal check for: " + deployedURL.url);
            }
            else
            {
               if (urlsToDeploy.contains(deployedURL.url))
               {
                  urlsToCheckForUpdate.add(deployedURL);
               }
               else
               {
                  urlsToRemove.add(deployedURL);
               }
            }
         }
      }

      // ********
      // Undeploy
      // ********

      for (Iterator i = urlsToRemove.iterator(); i.hasNext();)
      {
         DeployedURL deployedURL = (DeployedURL) i.next();
         if (trace)
         {
            log.trace("Removing " + deployedURL.url);
         }
         undeploy(deployedURL);
      }

      // ********
      // Redeploy
      // ********

      // compute the DeployedURL list to update
      ArrayList urlsToUpdate = new ArrayList(urlsToCheckForUpdate.size());
      for (Iterator i = urlsToCheckForUpdate.iterator(); i.hasNext();)
      {
         DeployedURL deployedURL = (DeployedURL) i.next();
         if (deployedURL.isModified())
         {
            if (trace)
            {
               log.trace("Re-deploying " + deployedURL.url);
            }
            urlsToUpdate.add(deployedURL);
         }
      }

      // sort to update list
      Collections.sort(urlsToUpdate, new Comparator()
      {
         public int compare(Object o1, Object o2)
         {
            return sorter.compare(((DeployedURL) o1).url, ((DeployedURL) o2).url);
         }
      });

      // Undeploy in order
      for (int i = urlsToUpdate.size() - 1; i >= 0;i--)
      {
         undeploy((DeployedURL) urlsToUpdate.get(i));
      }

      // Deploy in order
      for (int i = 0; i < urlsToUpdate.size();i++)
      {
         deploy((DeployedURL) urlsToUpdate.get(i));
      }

      // ******
      // Deploy
      // ******

      Collections.sort(urlsToDeploy, sorter);
      for (Iterator i = urlsToDeploy.iterator(); i.hasNext();)
      {
         URL url = (URL) i.next();
         DeployedURL deployedURL = new DeployedURL(url);
         if (deployedSet.contains(deployedURL) == false)
         {
            if (skipSet.contains(url))
            {
               if (trace)
                  log.trace("Skipping deployment of: " + url);
            }
            else
            {
               if (trace)
                  log.trace("Deploying " + deployedURL.url);
               
               deploy(deployedURL);
            }
         }
         i.remove();
         // Check to see if mainDeployer suffix list has changed.
         // if so, then resort
         if (i.hasNext() && updateSorter())
         {
            Collections.sort(urlsToDeploy, sorter);
            i = urlsToDeploy.iterator();
         }
      }

      // Validate that there are still incomplete deployments
      if (lastIncompleteDeploymentException != null)
      {
         try
         {
            Object[] args = {};
            String[] sig = {};
            getServer().invoke(getDeployer(),
                               "checkIncompleteDeployments", args, sig);
         }
         catch (Exception e)
         {
            Throwable t = JMXExceptionDecoder.decode(e);
            log.error(t);
         }
      }
   }

   protected boolean updateSorter()
   {
      // Check to see if mainDeployer suffix list has changed.
      if (sorter instanceof DefaultDeploymentSorter)
      {
         DefaultDeploymentSorter defaultSorter = (DefaultDeploymentSorter)sorter;
         if (defaultSorter.getSuffixOrder() != mainDeployer.getSuffixOrder())
         {
            defaultSorter.setSuffixOrder(mainDeployer.getSuffixOrder());
            return true;
         }
      }
      return false;
   }

   /////////////////////////////////////////////////////////////////////////
   //                     Service/ServiceMBeanSupport                     //
   /////////////////////////////////////////////////////////////////////////

   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      // get server's home for relative paths, need this for setting
      // attribute final values, so we need to do it here
      ServerConfig serverConfig = ServerConfigLocator.locate();
      serverHome = serverConfig.getServerHomeDir();
      serverHomeURL = serverConfig.getServerHomeURL();

      return super.preRegister(server, name);
   }

   protected void createService() throws Exception
   {
      // Perform a couple of sanity checks
      if (this.filter == null)
      {
         throw new IllegalStateException("'FilterInstance' attribute not configured");
      }
      if (this.sorter == null)
      {
         throw new IllegalStateException("'URLComparator' attribute not configured");
      }
      // ok, proceed with normal createService()
      super.createService();
   }
   
   /////////////////////////////////////////////////////////////////////////
   //                           DeployedURL                               //
   /////////////////////////////////////////////////////////////////////////

   /**
    * A container and help class for a deployed URL.
    * should be static at this point, with the explicit scanner ref, but I'm (David) lazy.
    */
   protected class DeployedURL
   {
      public URL url;
      /** The url to check to decide if we need to redeploy */
      public URL watchUrl;
      
      public long deployedLastModified;

      public DeployedURL(final URL url)
      {
         this.url = url;
      }

      public void deployed()
      {
         deployedLastModified = getLastModified();
      }
      public boolean isFile()
      {
         return url.getProtocol().equals("file");
      }

      public File getFile()
      {
         return new File(url.getFile());
      }

      public boolean isRemoved()
      {
         if (isFile())
         {
            File file = getFile();
            return !file.exists();
         }
         return false;
      }

      public long getLastModified()
      {
         if (watchUrl == null)
         {
            try
            {
               Object o = getServer().invoke(
                     getDeployer(),
                     "getWatchUrl",
                     new Object[] { url },
                     new String[] { URL.class.getName() }
                     );
               watchUrl = o == null ? url : (URL)o;
               getLog().debug("Watch URL for: " + url + " -> " + watchUrl);
            }
            catch (Exception e)
            {
               watchUrl = url;
               getLog().debug("Unable to obtain watchUrl from deployer. Use url: " + url, e);
            }
         }

         try
         {
            URLConnection connection;
            if (watchUrl != null)
            {
               connection = watchUrl.openConnection();
            } 
            else
            {
               connection = url.openConnection();
            }
            long lastModified = connection.getLastModified();

            return lastModified;
         }
         catch (java.io.IOException e)
         {
            log.warn("Failed to check modification of deployed url: " + url, e);
         }
         return -1;
      }

      public boolean isModified()
      {
         long lastModified = getLastModified();
         if (lastModified == -1)
         {
            // ignore errors fetching the timestamp - see bug 598335
            return false;
         }
         return deployedLastModified != lastModified;
      }

      public int hashCode()
      {
         return url.hashCode();
      }

      public boolean equals(final Object other)
      {
         if (other instanceof DeployedURL)
         {
            return ((DeployedURL)other).url.equals(this.url);
         }
         return false;
      }

      public String toString()
      {
         return super.toString() +
         "{ url=" + url +
         ", deployedLastModified=" + deployedLastModified +
         " }";
      }
   }
}
