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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.embedded.adapters.ServerConfig;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.plugins.deployment.xml.BeanXMLDeployer;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;

/**
 * Basic bootstrap class for embeddable JBoss
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class Bootstrap
{
   private static final Logger log = Logger.getLogger(Bootstrap.class);

   public static final String BOOTSTRAP_RESOURCE_PATH="jboss.embedded.bootstrap.resource.path";
   public static final String BOOTSTRAP_RESOURCE_FILE="conf/bootstrap-beans.xml";

   protected Kernel kernel;
   protected ClassLoader loader = Thread.currentThread().getContextClassLoader();
   protected MainDeployer mainDeployer;
   protected boolean started;
   protected boolean ignoreShutdownErrors;

   private static Bootstrap instance;

   /**
    * For those applications that need a singelton Bootstrap instance
    *
    * @return the bootstrap
    */
   public static synchronized Bootstrap getInstance()
   {
      if (instance == null)
         instance = new Bootstrap(createKernel());

      return instance;
   }

   public Bootstrap()
   {
      this.kernel = createKernel();
   }

   public Bootstrap(Kernel kernel)
   {
      this.kernel = kernel;
   }

   public boolean isIgnoreShutdownErrors()
   {
      return ignoreShutdownErrors;
   }

   public void setIgnoreShutdownErrors(boolean ignoreShutdownErrors)
   {
      this.ignoreShutdownErrors = ignoreShutdownErrors;
   }

   public boolean isStarted()
   {
      return started;
   }

   public Kernel getKernel()
   {
      return kernel;
   }

   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }

   public ClassLoader getLoader()
   {
      return loader;
   }

   public void setLoader(ClassLoader loader)
   {
      this.loader = loader;
   }

   protected static Kernel createKernel()
   {
      BasicBootstrap bootstrap1 = new BasicBootstrap();
      bootstrap1.run();
      return bootstrap1.getKernel();
   }

   protected void deployBaseBootstrapUrl(URL url) throws Throwable
   {
      BeanXMLDeployer deployer = new BeanXMLDeployer(kernel);
      deployer.deploy(url);
   }

   protected void bootstrapURL(URL url) throws DeploymentException
   {
      try
      {
         // ServerConfig has to be created and installed before boostrap as we may want to use
         // system properties set up in ServerConfig
         ServerConfig config = new ServerConfig();
         AbstractBeanMetaData bmd = new AbstractBeanMetaData("ServerConfig", ServerConfig.class.getName());
         kernel.getController().install(bmd, config);
         deployBaseBootstrapUrl(url);
         mainDeployer = (MainDeployer)kernel.getRegistry().getEntry("MainDeployer").getTarget();
      }
      catch (Throwable throwable)
      {
         throw new RuntimeException("Unable to bootstrap: ", throwable);
      }
      mainDeployer.checkComplete();
      started = true;
   }

   /**
    *
    * Specify top classpath resource directory where base JBoss Embedded directory structure is.

    * The Embedded JBoss directory structure is determined by extrapolating a directory from a base
    * classpath resource.

    * The absolute directory will be determined by doing
    *   classloader.getResource(bootstrapResourcePath + "conf/bootstrap-beans.xml")
    *
    *
    * @param bootstrapResourcePath
    * @throws DeploymentException
    */
   public void bootstrap(String bootstrapResourcePath) throws DeploymentException
   {
      if (bootstrapResourcePath == null)
      {
         bootstrapResourcePath = "";
      }
      else if (!bootstrapResourcePath.equals("") && !bootstrapResourcePath.endsWith("/"))
      {
         bootstrapResourcePath += "/";
      }
      System.setProperty(BOOTSTRAP_RESOURCE_PATH, bootstrapResourcePath);
      bootstrapResourcePath += BOOTSTRAP_RESOURCE_FILE;
      URL url = loader.getResource(bootstrapResourcePath);
      if (url == null)
         throw new DeploymentException("Unable to find bootstrap file: " + bootstrapResourcePath + " in classpath");

      bootstrapURL(url);
   }

   /**
    * Will obtain resource path from jboss.embedded.bootstrap.resource.path System Property.
    * Otherwise it just invoked bootstrap(String bootstrapResourcePath) with ""
    *
    * @throws DeploymentException
    */
   public void bootstrap() throws DeploymentException
   {
      String path = System.getProperty(BOOTSTRAP_RESOURCE_PATH, "");
      bootstrap(path);
   }

   /**
    * Shutdown the kernel and all deployments
    *
    */
   public void shutdown()
   {
      try
      {
         mainDeployer.shutdown();
      }
      catch (Exception e)
      {
         if (!ignoreShutdownErrors)
            throw new RuntimeException(e);
         else
            log.error("Failed to shutdown Bootstrap", e);
      }
   }

   /**
    * Look in java.class.path for any .jar or class directories whose base file/dir match
    * any base file/dir names in the comma delimited path parameter
    *
    * If classpath is:
    *
    * /home/wburke/jars/foo.jar
    *
    * and path is:
    *
    * "foo.jar"
    *
    * This will be a match and that .jar file will be deployed
    *
    * @param path can be comma delimited
    * @throws DeploymentException
    */
   public void scanClasspath(String path) throws DeploymentException
   {
      DeploymentGroup group = createDeploymentGroup();
      group.addClasspath(path);
      group.process();
   }

   /**
    * Undeploy something deployed via scanSclasspath()
    *
    * @param path
    * @throws DeploymentException
    */
   public void undeployClasspath(String path) throws DeploymentException
   {
      List<URL> paths = DeploymentGroup.getClassPaths(path);
      for (URL url : paths)
      {
         undeploy(url);
      }
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
   public void deployResourceBase(String baseResource) throws DeploymentException
   {
      DeploymentGroup group = createDeploymentGroup();
      group.addResourceBase(baseResource);
      group.process();
   }

   /**
    *
    *
    * @param baseResource
    * @throws DeploymentException
    */
   public void deployResourceBases(String baseResource) throws DeploymentException
   {
      DeploymentGroup group = createDeploymentGroup();
      group.addResourceBases(baseResource);
      group.process();
   }

   /**
    * Find the .class resource of the given class
    * Deploy a URL pointing to the classpath the resource is located in.
    *
    * i.e.
    *
    * classpath is "/home/wburke/lib/tutorial.jar"
    * tutorial.jar has "META-INF/persistence.xml" resource within it.
    *
    * addResourceBase("META-INF/persistence.xml") will try and deploy tutorial.jar
    *
    * classloader.getResource("META-INF/persistence.xml") is used to determine the base location
    *
    *
    * @param baseResource
    * @throws DeploymentException
    */
   public void deployResourceBase(Class baseResource) throws DeploymentException
   {
      DeploymentGroup group = createDeploymentGroup();
      group.addResourceBase(baseResource);
      group.process();
   }

   public void deploy(URL url) throws DeploymentException
   {
      DeploymentGroup group = createDeploymentGroup();
      group.add(url);
      group.process();
   }

   public void deploy(VirtualFile file) throws DeploymentException
   {
      DeploymentGroup group = createDeploymentGroup();
      group.add(file);
      group.process();
   }

   /**
    * Deploy a resource found by getResource() on the kernel's classloader
    *
    *
    * @param resource
    * @throws DeploymentException
    */
   public void deployResource(String resource) throws DeploymentException
   {
      DeploymentGroup group = createDeploymentGroup();
      group.addResource(resource);
      group.process();
   }

   /**
    * Define a deploy directory and deploy all files within it.  The recurse parameter tells whether to recurse into
    * sub directories for deployments
    *
    *
    * @param url
    * @param recurse
    * @throws DeploymentException
    * @throws IOException
    */
   public void deployDirectory(URL url, boolean recurse) throws DeploymentException, IOException
   {
      DeploymentGroup group = createDeploymentGroup();
      group.addDirectory(url, recurse);
      group.process();
   }

   /**
    *
    * Find a deploy directory from a base resource
    *
    * @param resource
    * @param recurse
    * @throws DeploymentException
    * @throws IOException
    */
   public void deployDirectoryFromResource(String resource, boolean recurse) throws DeploymentException, IOException
   {
      DeploymentGroup group = createDeploymentGroup();
      group.addDirectoryByResource(resource, recurse);
      group.process();
   }

   /**
    * opposite of deployResourceBase()
    *
    *
    *
    * @param baseResource
    * @throws DeploymentException
    */
   public void undeployResourceBase(String baseResource) throws DeploymentException
   {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      if (loader != null)
         classLoader = loader;

      URL url = classLoader.getResource(baseResource);
      if (url == null)
         throw new RuntimeException("Could not find baseResource: " + baseResource);

      undeployResourceBase(url, baseResource);
   }

   private void undeployResourceBase(URL url, String baseResource)
           throws DeploymentException
   {
      String urlString = url.toString();
      int idx = urlString.lastIndexOf(baseResource);
      urlString = urlString.substring(0, idx);
      URL deployUrl;
      try
      {
         deployUrl = new URL(urlString);
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      undeploy(deployUrl);
   }

   public void undeployResourceBases(String baseResource) throws DeploymentException
   {
      try
      {
         Enumeration<URL> urls = loader.getResources(baseResource);
         while (urls.hasMoreElements())
         {
            URL url = urls.nextElement();
            undeployResourceBase(url, baseResource);
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * opposite of deployResourceBase()
    *
    *
    *
    * @param baseResource
    * @throws DeploymentException
    */
   public void undeployResourceBase(Class baseResource) throws DeploymentException
   {
      String resource = baseResource.getName().replace('.', '/') + ".class";
      undeployResourceBase(resource);
   }
   /**
    * opposite of deployResource
    *
    * @param resource
    * @throws DeploymentException
    */
   public void undeployResource(String resource) throws DeploymentException
   {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      if (loader != null)
         classLoader = loader;

      URL url = classLoader.getResource(resource);
      if (url == null)
         throw new NullPointerException("Resource was null: " + resource);

      undeploy(url);
   }

   public void undeploy(URL url) throws DeploymentException
   {
      VirtualFile vf = DeploymentGroup.getVirtualFile(url);
      undeploy(vf);
   }

   public void undeploy(VirtualFile vf)
           throws DeploymentException
   {
      mainDeployer.removeDeployment(vf.getName());
      mainDeployer.process();
   }

   public void undeployDirectory(URL url, boolean recurse) throws DeploymentException, IOException
   {
      List<VirtualFile> files = DeploymentGroup.getDeployerDirUrls(null, url, recurse);
      for (VirtualFile vf : files)
         mainDeployer.removeDeployment(vf.getName());
      mainDeployer.process();
   }

   public void undeployDirectoryFromResource(String resource, boolean recurse) throws DeploymentException, IOException
   {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      if (loader != null) classLoader = loader;
      List<VirtualFile> files = DeploymentGroup.getDeployerDirUrlsFromResource(null, classLoader, resource, recurse);
      for (VirtualFile vf : files)
         mainDeployer.removeDeployment(vf.getName());
      mainDeployer.process();
   }

   public DeploymentGroup createDeploymentGroup()
   {
      DeploymentGroup group = new DeploymentGroup();
      group.setClassLoader(loader);
      group.setMainDeployer(mainDeployer);
      group.setKernel(kernel);
      return group;
   }

   public static void main(String[] args) throws Exception
   {
      getInstance().bootstrap();
      for (String arg : args)
      {
         getInstance().scanClasspath(arg);
      }
      System.out.println("Running...");
      Thread t = new Thread();
      t.setDaemon(false);
      t.start();
   }
 }