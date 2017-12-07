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
package org.jboss.embedded;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.kernel.Kernel;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class DeploymentGroup
{
   private ArrayList<VFSDeployment> deployments = new ArrayList<VFSDeployment>();
   protected MainDeployer mainDeployer;
   protected Kernel kernel;
   private VirtualFileFilter filter;
   private ClassLoader classLoader;

   public void setMainDeployer(MainDeployer mainDeployer)
   {
      this.mainDeployer = mainDeployer;
   }

   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }

   /**
    *
    * File filter that will be used when scanning a directory
    *
    * @param filter
    */
   public void setFilter(VirtualFileFilter filter)
   {
      this.filter = filter;
   }

   public void setClassLoader(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
   }

   /**
    * A helper to find all deployments under a directory vf
    * and add them to the supplied list.
    *
    * We may recurse.
    */
   private static void addDeployments(VirtualFileFilter filter, List<VirtualFile> list, VirtualFile root, boolean recurse)
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
         else if (component.getName().indexOf('.') == -1 && recurse)
         {
            // recurse if not '.' in name and recursive search is enabled
            addDeployments(filter, list, component, true);
         }
         else
         {
            list.add(component);
         }
      }
   }

   /**
    * Ask the mainDeployer to process the set of files you added to the group
    * this will also check the processing
    *
    * @throws org.jboss.deployers.spi.DeploymentException
    */
   public void process() throws DeploymentException
   {
      mainDeployer.process();
      mainDeployer.checkComplete();
   }

   /**
    *  Ask the mainDeployer to undeploy the set of files in the group
    *
    * @throws org.jboss.deployers.spi.DeploymentException
    */
   public void undeploy() throws DeploymentException
   {
      for (VFSDeployment ctx : deployments)
         mainDeployer.removeDeployment(ctx);
      process();
   }

   /**
    * Schedule a VirtualFile to be deployed
    *
    * @param vf
    * @throws DeploymentException
    */
   public void add(VirtualFile vf) throws DeploymentException
   {
      VFSDeploymentFactory factory = VFSDeploymentFactory.getInstance();
      VFSDeployment deployment = factory.createVFSDeployment(vf);
      mainDeployer.addDeployment(deployment);
      deployments.add(deployment);
   }

   /**
    * schedules a URL to be deployed
    *
    * @param url
    * @throws DeploymentException
    */
   public void add(URL url) throws DeploymentException
   {
      VirtualFile file = getVirtualFile(url);
      add(file);
   }

   public static VirtualFile getVirtualFile(URL url)
           throws DeploymentException
   {
      VirtualFile file = null;
      try
      {
         file = VFS.getRoot(url);
      }
      catch (IOException e)
      {
         throw new DeploymentException("Unable to get VirtualFile for url: " + url, e);
      }
      return file;
   }

   /**
    * schedules a list of virtual files to be deployed
    *
    * @param vfs
    * @throws DeploymentException
    */
   public void addVirtualFiles(List<VirtualFile> vfs) throws DeploymentException
   {
      for (VirtualFile vf : vfs)
      {
         add(vf);
      }
   }

   /**
    * schedules a list of urls to be deployed
    *
    * @param urls
    * @throws DeploymentException
    */
   public void addUrls(List<URL> urls) throws DeploymentException
   {
      for (URL url : urls)
      {
         add(url);
      }
   }

   /**
    * Scan all paths/jars in Java CLasspath (found with java.class.path System Property)
    * schedule these jars to be deployed
    *
    * @throws org.jboss.deployers.spi.DeploymentException
    */
   public void addClasspath() throws DeploymentException
   {
      addUrls(getClassPaths());
   }

   public static List<URL> getClassPaths() throws DeploymentException
   {
      List<URL> list = new ArrayList<URL>();
      String classpath = System.getProperty("java.class.path");
      StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);

      while (tokenizer.hasMoreTokens())
      {
         String path = tokenizer.nextToken();
         File fp = new File(path);
         if (!fp.exists()) throw new DeploymentException("File in java.class.path does not exist: " + fp);
         try
         {
            list.add(fp.toURL());
         }
         catch (MalformedURLException e)
         {
            throw new DeploymentException(e);
         }
      }
      return list;
   }

   /**
    * Scan Java Classpath (found with java.class.path)
    * for a specified list of files you want to deploy
    *
    * The files listed should be only the filename.  Do not put relative or absolute paths in filenames.
    * i.e. "myejbs.jar, my-beans.xml"
    *
    * @param paths comma delimited list of files
    * @throws org.jboss.deployers.spi.DeploymentException
    */
   public void addClasspath(String paths) throws DeploymentException
   {
      List<URL> urls = getClassPaths(paths);
      addUrls(urls);
   }

   public static List<URL> getClassPaths(String paths) throws DeploymentException
   {
      ArrayList<URL> list = new ArrayList<URL>();

      String classpath = System.getProperty("java.class.path");
      StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
      String[] split = paths.split(",");
      for (int i = 0; i < split.length; i++)
      {
         split[i] = split[i].trim();
      }

      while (tokenizer.hasMoreTokens())
      {
         String path = tokenizer.nextToken().trim();
         boolean found = false;
         for (String wantedPath : split)
         {
            if (path.endsWith(File.separator + wantedPath))
            {
               found = true;
               break;
            }
         }
         if (!found)
            continue;

         File fp = new File(path);
         if (!fp.exists())
            throw new DeploymentException("File in java.class.path does not exists: " + fp);

         try
         {
            list.add(fp.toURL());
         }
         catch (MalformedURLException e)
         {
            throw new DeploymentException(e);
         }
      }
      return list;
   }

   /**
    * Search for the resource using the group's configured classloader
    * if no classloader, then Thread.currentThread().getContextClassLoader() is used.
    *    classLoader.getResource(String resource)
    *
    * Schedule the resource to be deployed.
    *
    * @param resource
    * @throws DeploymentException
    * @throws NullPointerException
    */
   public void addResource(String resource) throws DeploymentException, NullPointerException
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (classLoader != null)
         loader = classLoader;

      URL url = loader.getResource(resource);
      if (url == null)
         throw new NullPointerException("Resource was null: " + resource);

      add(url);
   }

   /**
    * Deploy the classpath directory or .jar file a classloader resource is located in.
    *
    * i.e.
    *
    * classpath is "/home/wburke/lib/tutorial.jar"
    * tutorial.jar has "META-INF/persistence.xml" resource within it.
    *
    * addResourceBase("META-INF/persistence.xml") will try and deploy tutorial.jar
    *
    *
    * @param baseResource
    * @throws DeploymentException
    */
   public void addResourceBase(String baseResource) throws DeploymentException
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (classLoader != null) loader = classLoader;
      URL url = loader.getResource(baseResource);
      if (url == null) throw new RuntimeException("Could not find baseResource: " + baseResource);
      addBaseResource(url, baseResource);


   }

   protected void addBaseResource(URL url, String baseResource)
           throws DeploymentException
   {
      String urlString = url.toString();
      int idx = urlString.lastIndexOf(baseResource);
      urlString = urlString.substring(0, idx);
      URL deployUrl = null;
      try
      {
         deployUrl = new URL(urlString);
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      add(deployUrl);
   }

   /**
    * Deploy the classpath directories or .jar files a classloader resource is located in.
    * ClassLoader.getResources() is used to find the base resources.
    *
    * i.e.
    *
    * classpath is "/home/wburke/lib/tutorial.jar:/home/wburke/lib/pu.jar"
    * tutorial.jar and pu.jar has "META-INF/persistence.xml" resource within it.
    *
    * addResourceBases("META-INF/persistence.xml") will try and deploy tutorial.jar  and pu.jar because
    * the both have the META-INF/persistence.xml resource within them.
    *
    *
    * @param baseResource
    * @throws DeploymentException
    */
   public void addResourceBases(String baseResource) throws DeploymentException
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (classLoader != null) loader = classLoader;
      try
      {
         Enumeration<URL> urls = loader.getResources(baseResource);
         while (urls.hasMoreElements())
         {
            URL url = urls.nextElement();
            addBaseResource(url, baseResource);
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Find the .class file resource of provided class
    * Return a URL pointing to the classpath the resource is located in.
    *
    * i.e.
    *
    * classpath is "/home/wburke/lib/tutorial.jar"
    * tutorial.jar has "META-INF/persistence.xml" resource within it.
    *
    * addResourceBase("META-INF/persistence.xml") will try and deploy tutorial.jar
    *
    *
    * @param baseResource
    * @throws DeploymentException
    */
   public void addResourceBase(Class baseResource) throws DeploymentException
   {
      String resource = baseResource.getName().replace('.', '/') + ".class";
      addResourceBase(resource);
   }

   /**
    * Search for resources using the group's configured classloader
    * if no classloader, then Thread.currentThread().getContextClassLoader() is used.
    *    classLoader.getResources(String resource)
    *
    * Schedule the resource to be deployed.
    *
    * @param resource
    * @throws DeploymentException
    * @throws IOException
    */
   public void addMultipleResources(String resource) throws DeploymentException, IOException
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (classLoader != null) loader = classLoader;
      Enumeration<URL> urls = loader.getResources(resource);
      while (urls.hasMoreElements())
      {
         add(urls.nextElement());
      }
   }

   /**
    * Searches for a directory as described in the  getDirFromResource() method of this class.
    *
    * schedules all possible files in directory to be deployed
    *
    *
    * @param resource
    * @param recurse whether or not to recurse child directories
    * @throws DeploymentException
    * @throws IOException
    */
   public void addDirectoryByResource(String resource, boolean recurse) throws DeploymentException, IOException
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (classLoader != null) loader = classLoader;
      List<VirtualFile> files = getDeployerDirUrlsFromResource(filter, loader, resource, recurse);
      addVirtualFiles(files);
   }

   /**
    * Searches for a file based on the location of an existing classloader resource
    * as described in the  getDirFromResource() method of this class.
    *
    * schedules this particular file for deployment
    *
    *
    * @param resource
    * @throws DeploymentException
    * @throws IOException
    */
   public void addFileByResource(String resource) throws DeploymentException, IOException
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (classLoader != null) loader = classLoader;
      URL url = getDirFromResource(loader, resource);
      if (url == null)
      {
         throw new DeploymentException("Unable to find file from resource: " + resource);
      }
      String urlStr = url.toString();
      if (urlStr.endsWith("/"))
      {
         urlStr = urlStr.substring(0, urlStr.length() -1);
      }
      url = new URL(urlStr);
      add(url);
   }

   public void addDirectory(URL directory, boolean recurse) throws DeploymentException, IOException
   {
      addVirtualFiles(getDeployerDirUrls(filter, directory, recurse));
   }

   /**
    * Get the deployment units
    * 
    * @return the deployment units
    * @throws IllegalStateException when the units cannot be located
    */
   public List<DeploymentUnit> getDeploymentUnits()
   {
      ArrayList<DeploymentUnit> result = new ArrayList<DeploymentUnit>();
      MainDeployerStructure structure = (MainDeployerStructure) mainDeployer;
      for (VFSDeployment deployment : deployments)
      {
         DeploymentUnit unit = structure.getDeploymentUnit(deployment.getName());
         if (unit == null)
            throw new IllegalStateException("DeploymentUnit not found " + deployment.getName());
         result.add(unit);
      }
      return result;
   }

   public List<VFSDeployment> getDeployments()
   {
      return deployments;
   }

   public static List<VirtualFile> getDeployerDirUrlsFromResource(VirtualFileFilter filter, ClassLoader loader, String resource, boolean recurse)
           throws DeploymentException, IOException
   {
      URL url = getDirFromResource(loader, resource);
      if (url == null)
      {
         throw new DeploymentException("Unable to find deployDir from resource: " + resource);
      }
      List<VirtualFile> files = getDeployerDirUrls(filter, url, recurse);
      return files;
   }

   public static List<VirtualFile> getDeployerDirUrls(VirtualFileFilter filter, URL url, boolean recurse)
           throws DeploymentException, IOException
   {
      VirtualFile file = null;
      try
      {
         file = VFS.getRoot(url);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Unable to find deployDir from url: " + url, e);
      }
      List<VirtualFile> files = new ArrayList<VirtualFile>();
      addDeployments(filter, files, file, recurse);
      return files;
   }

   /**
    * Find the directory that contains a given resource.
    * <p/>
    * The '.' character can be used to specify the current directory.
    * The '..' string can be used to specify a higher relative path
    * <p/>
    * i.e.
    * <p/>
    * getDirFromResource(loader, "org/jboss/Test.class")
    * file:/<root>/org/jboss/
    * <p/>
    * getDirFromResource(loader, "org/jboss/Test.class/..")
    * file:/<root>/org/
    * <p/>
    * getDirFromResource(loader, "org/jboss/Test.class/../acme")
    * file:/<root>/org/acme/
    * <p/>
    * getDirFromResource(loader, "org/jboss/Test.class/./embedded")
    * file:/<root>/org/jboss/embedded/
    *
    * @param loader
    * @param resource
    * @return the url
    */
   public static URL getDirFromResource(ClassLoader loader, String resource)
   {
      int idx = resource.indexOf("/.");
      String base = resource;
      String relative = null;
      if (idx != -1)
      {
         base = resource.substring(0, idx);
         relative = resource.substring(idx + 1);
      }
      URL url = loader.getResource(base);
      if (url == null) return null;
      String urlAsString = url.toString();
      String[] paths = urlAsString.split("/");
      int last = paths.length - 2;
      if (relative != null)
      {
         String[] relativePaths = relative.split("/");
         int relativeStart = 0;
         for (String relativePath : relativePaths)
         {
            if (relativePath.equals(".."))
            {
               last--;
               relativeStart++;
            }
            else if (relativePath.equals("."))
            {
               relativeStart++;
            }
            else
            {
               break;
            }
         }
         urlAsString = "";
         for (int i = 0; i <= last; i++)
         {
            urlAsString += paths[i] + "/";
         }
         for (int i = relativeStart; i < relativePaths.length; i++)
         {
            urlAsString += relativePaths[i] + "/";
         }
      }
      else
      {
         urlAsString = "";
         for (int i = 0; i <= last; i++)
         {
            urlAsString += paths[i] + "/";
         }
      }
      try
      {
         url = new URL(urlAsString);
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      return url;
   }
}
