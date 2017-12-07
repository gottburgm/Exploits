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
package org.jboss.system.server.profileservice;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.util.JBossObject;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;

/**
 * A DeploymentScanner build on top of the VFS and ProfileService. This is a
 * first pass to flesh out the APIs/concepts.
 *
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 85526 $
 */
public abstract class VFSScanner extends JBossObject
{
   /** The profile service */
   private ProfileService profileService;

   /** The profile service key */
   private ProfileKey profileKey;

   /** The URIfied ServerHomeURL */
   private URI serverHomeURI;

   /** The list of URIs to scan */
   private List<URI> uriList = new CopyOnWriteArrayList<URI>();

   /** The list of VirtualFiles to scan */
   private List<VirtualFile> vdfList = new CopyOnWriteArrayList<VirtualFile>();

   /** Allow a filter for scanned directories */
   private VirtualFileFilter filter;

   /** Whether to search for files inside directories whose names containing no dots */
   private boolean doRecursiveSearch = true;

   /** A map of deployed virtual files to their names */
   private Map<VirtualFile, String> deployedSet = new ConcurrentHashMap<VirtualFile, String>();

   /** The deployment factory */
   private VFSDeploymentFactory deploymentFactory = VFSDeploymentFactory.getInstance();

    /**
    * Get the profileKey.
    *
    * @return the profileKey.
    */
   public ProfileKey getProfileKey()
   {
      return profileKey;
   }

   /**
    * Set the profileKey.
    *
    * @param profileKey the profileKey.
    */
   public void setProfileKey(ProfileKey profileKey)
   {
      this.profileKey = profileKey;
   }

   /**
    * Get the profileService.
    *
    * @return the profileService.
    */
   public ProfileService getProfileService()
   {
      return profileService;
   }

   /**
    * Set the profileService.
    *
    * @param profileService the profileService.
    */
   public void setProfileService(ProfileService profileService)
   {
      this.profileService = profileService;
   }

   /**
    * Set the uris
    *
    * @param listspec the uris
    * @throws URISyntaxException
    * @throws IOException
    */
   public void setURIs(final String listspec) throws URISyntaxException, IOException
   {
      if (listspec == null)
      {
         throw new NullPointerException("listspec argument cannot be null");
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
    * Set the uri list
    *
    * @param list the list
    * @throws IOException
    */
   public void setURIList(final List<URI> list) throws IOException
   {
      if (list == null)
      {
         throw new NullPointerException("list argument cannot be null");
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

   /**
    * Get the uri list
    *
    * @return the list
    */
   public List<URI> getURIList()
   {
      return new ArrayList<URI>(uriList);
   }

   /**
    * Set whether to do recursive search
    *
    * @param recurse true when recurisve
    */
   public void setRecursiveSearch(boolean recurse)
   {
      doRecursiveSearch = recurse;
   }

   /**
    * Get the recursive search
    *
    * @return true when recursive
    */
   public boolean getRecursiveSearch()
   {
      return doRecursiveSearch;
   }

   /**
    * Set the filter
    *
    * @param classname the filter class name
    * @throws ClassNotFoundException when the class is not found
    * @throws IllegalAccessException when the class's default constructor is not public
    * @throws InstantiationException when there is an error constructing the class
    */
   @SuppressWarnings("unchecked")
   public void setFilter(String classname)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<VirtualFileFilter> filterClass = (Class<VirtualFileFilter>) loader.loadClass(classname);
      filter = filterClass.newInstance();
   }

   /**
    * Get the filer
    *
    * @return the filter
    */
   public String getFilter()
   {
      if (filter == null)
      {
         return null;
      }
      return filter.getClass().getName();
   }

   /**
    * Set the filter instance
    *
    * @param filter ther filter
    */
   public void setFilterInstance(VirtualFileFilter filter)
   {
      this.filter = filter;
   }

   /**
    * Get the filter instance
    *
    * @return the filter
    */
   public VirtualFileFilter getFilterInstance()
   {
      return filter;
   }

   /**
    * Add a uri
    *
    * @param uri the uri
    * @throws IOException for an error accessing the uri
    */
   public void addURI(final URI uri) throws IOException
   {
      if (uri == null)
      {
         throw new NullPointerException("uri argument cannot be null");
      }
      if( uriList.add(uri) == true )
      {
         log.debug("Added URI: " + uri);
         VirtualFile vf = getVFforURI(uri);
         vdfList.add(vf);
      }
   }

   /**
    * Remove a uri
    *
    * @param uri the uri
    * @throws IOException for an error accessing the uri
    */
   public void removeURI(final URI uri)
      throws IOException
   {
      if (uri == null)
      {
         throw new NullPointerException("uri argument cannot be null");
      }
      VirtualFile vf = getVFforURI(uri);
      vdfList.remove(vf);
      boolean success = uriList.remove(uri);

      if (success)
      {
         log.debug("Removed URI: " + uri);
      }
   }

   /**
    * Whether it has the uri
    *
    * @param uri the uri
    * @return when the uri is configured
    */
   public boolean hasURI(final URI uri)
   {
      if (uri == null)
      {
         throw new NullPointerException("uri argument cannot be null");
      }
      return uriList.contains(uri);
   }

   /**
    * Start the scan
    *
    * @throws Exception for any error
    */
   public void start() throws Exception
   {
      // synchronize uriList and vdfList because only at this point
      // setVirtualFileFactory() injection has been performed
      vdfList.clear();
      for (Iterator<URI> i = uriList.iterator(); i.hasNext(); )
      {
         URI uri = i.next();
         VirtualFile vf = this.getVFforURI(uri);
         vdfList.add(vf);
      }
      if( profileKey == null )
      {
         profileKey = new ProfileKey("default");
      }
      scan();
   }

   /**
    * Scan
    *
    * @throws Exception for any error
    */
   public synchronized void scan() throws Exception
   {
      if (vdfList == null)
      {
         throw new IllegalStateException("not initialized");
      }

      boolean trace = log.isTraceEnabled();

      // Scan for deployments
      if (trace)
      {
         log.trace("Scanning for new deployments");
      }

      // VirtualFiles to deploy
      List<VirtualFile> toDeployList = new LinkedList<VirtualFile>();
      synchronized (vdfList)
      {
         for (Iterator i = vdfList.iterator(); i.hasNext();)
         {
            VirtualFile component = (VirtualFile)i.next();
            if (component.isLeaf())
            {
               // treat this as a deployable unit
               toDeployList.add(component);
            }
            else
            {
               // process (possibly recursively) the dir
               addDeployments(toDeployList, component);
            }
         }
      }

      if (trace)
      {
         log.trace("toDeployList");
         for (Iterator i = toDeployList.iterator(); i.hasNext();)
         {
            log.trace(i.next());
         }
      }
      LinkedList<VirtualFile> toRemoveList = new LinkedList<VirtualFile>();

      synchronized (deployedSet)
      {
         // remove previously deployed URLs no longer needed
         for (VirtualFile deployedComponent : deployedSet.keySet())
         {
            if (toDeployList.contains(deployedComponent) == false)
            {
               toRemoveList.add(deployedComponent);
            }
         }
      }

      // ********
      // Undeploy
      // ********

      for (Iterator i = toRemoveList.iterator(); i.hasNext();)
      {
         VirtualFile deployedComponent = (VirtualFile)i.next();
         undeploy(deployedComponent);
      }

      // ******
      // Deploy
      // ******

      for (Iterator i = toDeployList.iterator(); i.hasNext();)
      {
         VirtualFile component = (VirtualFile)i.next();

         // if component is not deployed already, deploy it
         if (!deployedSet.containsKey(component))
         {
            deploy(component);
         }

         // component must have been deployed by now, so remove it from list
         i.remove();

      }

   }

   /**
    * Make a uri
    *
    * @param urispec the uri spec
    * @return the uri
    * @throws URISyntaxException for an error parsing he uri
    */
   private URI makeURI(String urispec) throws URISyntaxException
   {
      // First replace URI with appropriate properties
      urispec = StringPropertyReplacer.replaceProperties(urispec);
      return serverHomeURI.resolve(urispec);
   }

   /**
    * A helper to find all deployments under a directory component
    * and add them to the supplied list.
    *
    * We may recurse.
    *
    * @param list the list of virtual files
    * @param root the root file
    * @throws IOException for any error
    */
   private void addDeployments(List<VirtualFile> list, VirtualFile root)
      throws IOException
   {
      List<VirtualFile> components = root.getChildren();

      for (VirtualFile component : components)
      {
         // Filter the component regardless of its type
         if( filter != null && filter.accepts(component) == false)
            continue;
         if (component.isLeaf())
         {
            list.add(component);
         }
         // TODO replace . in the name with isArchive() == false?
         else if (component.getName().indexOf('.') == -1 && this.doRecursiveSearch)
         {
            // recurse if not '.' in name and recursive search is enabled
            addDeployments(list, component);
         }
         else
         {
            list.add(component);
         }
      }
   }

   /**
    * A helper to deploy the given component using the deployer.
    *
    * @param component the virtual file
    */
   private void deploy(final VirtualFile component)
   {
      // If the deployer is null simply ignore the request
      if (profileService == null)
      {
         return;
      }
      if (log.isTraceEnabled())
      {
         log.trace("Deploying: " + component);
      }

      VFSDeployment deployment = null;
      try
      {
         Profile profile = profileService.getProfile(profileKey);
         deployment = add(profile, component);
      }
      catch (Exception e)
      {
         log.debug("Failed to deploy: " + component, e);
      }

      if (deployment != null && !deployedSet.containsKey(component))
      {
         deployedSet.put(component, deployment.getName());
      }
   }

   /**
    * A helper to undeploy the given component using the deployer.
    *
    * @param component the component
    */
   private void undeploy(final VirtualFile component)
   {
      try
      {
         if (log.isTraceEnabled())
         {
            log.trace("Undeploying: " + component);
         }
         String name = deployedSet.remove(component);
         Profile profile = profileService.getProfile(profileKey);
         remove(profile, name);
      }
      catch (Exception e)
      {
         log.error("Failed to undeploy: " + component, e);
      }
   }

   /**
    * Remove the component
    *
    * @param profile the profile
    * @param file the virtual file
    * @return the deployment context or null if not added, e.g. it already exists
    * @throws Exception for any error
    */
   protected abstract VFSDeployment add(Profile profile, VirtualFile file) throws Exception;

   /**
    * Remove the component
    *
    * @param profile the profile
    * @param name the name
    * @throws Exception for any error
    */
   protected abstract void remove(Profile profile, String name) throws Exception;

   /**
    * Create a deployment
    *
    * @param file the root file
    * @return the deployment
    */
   protected VFSDeployment createDeployment(VirtualFile file)
   {
      if (file == null)
         throw new IllegalArgumentException("Null file");
      return deploymentFactory.createVFSDeployment(file);
   }

   /**
    * Get virtual file for uri.
    *
    * @param uri the uri
    * @return vritual file representing uri
    * @throws IOException for any error
    */
   private VirtualFile getVFforURI(URI uri) throws IOException
   {
      return VFS.getRoot(uri);
   }
}
