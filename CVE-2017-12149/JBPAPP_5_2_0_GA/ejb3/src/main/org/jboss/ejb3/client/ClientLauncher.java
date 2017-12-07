/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.client;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.classloader.plugins.system.DefaultClassLoaderSystem;
import org.jboss.classloader.spi.ClassLoaderDomain;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloader.spi.ParentPolicy;
import org.jboss.classloading.spi.dependency.ClassLoading;
import org.jboss.classloading.spi.vfs.dependency.VFSClassLoaderPolicyModule;
import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory;
import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory10;
import org.jboss.client.AppClientLauncher;
import org.jboss.dependency.spi.ControllerMode;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployment.dependency.JndiDependencyMetaData;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.AbstractBootstrap;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployment;
import org.jboss.kernel.plugins.deployment.xml.BasicXMLDeployer;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.logging.Logger;
import org.jboss.metadata.client.jboss.JBossClientMetaData;
import org.jboss.remoting.Remoting;
import org.jboss.xb.binding.JBossXBException;

/**
 * This class launches a JavaEE 5 application client.
 * 
 * The first argument is either a jar file containing the client deployment files or the application client class name.
 * The manifest file Main-Class attribute must point to the application client class.
 * It must also contain an application client deployment descriptor file (META-INF/application-client.xml).
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class ClientLauncher
   implements AppClientLauncher
{
   private static final Logger log = Logger.getLogger(ClientLauncher.class);
   private static final String VERSION = "$Revision: 85945 $";
   private static Throwable exception;
   /** The kernel for the client container */
   private static Kernel kernel;

   /** The deployer */
   private static BasicXMLDeployer deployer;
   /** The mainClass used by the ClientContainer */
   private static Class<?> theMainClass;
   /** Should the remoting class loader delegate to the tcl */
   private static boolean remotingDelgatesToTCL = true;
   /** Additional classpath elements to client container classpath */
   private static List<String> extraClassPath = new ArrayList<String>();

   /**
    * Access the client main class as loaded by the MC/client container. This
    * class should be used to ensure that any static variable set in the class
    * as a by-product of invoking the client main(String[]) method are seen.
    * @return client main class object
    */
   public static Class<?> getTheMainClass()
   {
      return theMainClass;
   }

   /**
    * Convenience method to load the XML descriptor for the given applicationClientName.
    * This does a lookup against the server for the xml metadata the client
    * deployer bound.
    * 
    * @return the JBossClientMetaData
    * @throws IOException 
    * @throws JBossXBException 
    */
   public static JBossClientMetaData loadXML(String applicationClientName) throws NamingException
   {
      log.warn("FIXME: using an unsupported hack to get metadata");
      InitialContext ctx = new InitialContext();
      JBossClientMetaData metaData = (JBossClientMetaData) ctx.lookup(applicationClientName + "/metaData");
      return metaData;
   }
   public static List<String> loadClassPath(String applicationClientName)
      throws NamingException
   {
      InitialContext ctx = new InitialContext();
      List<String> cp = (List<String>) ctx.lookup(applicationClientName + "/classPathEntries");
      return cp;
   }

   /**
    * Calls {@link #launch(String, String, String[], Properties)}
    */
   public void launch(String clientClass, String clientName, String[] args)
         throws Throwable
   {
      launch(clientClass, clientName, args, null);
   }

   /**
    * Convenience method for launching a client container.
    * 
    * @param xml
    * @param mainClassName
    * @param applicationClientName
    * @param args
    * @throws Exception
    */
   public static void launch(JBossClientMetaData xml, String mainClassName, String applicationClientName, String args[])
      throws Throwable
   {
      List<String> cp = Collections.emptyList();
      launch(xml, cp, mainClassName, applicationClientName, args, null);
   }
   /**
    * The AppClientLauncher method for launching a client container.
    * 
    * @param mainClassName - the class whose main(String[]) will be invoked
    * @param clientName - the client name that maps to the server side JNDI ENC
    * @param args - the args to pass to main method
    * @param jndiEnv - jndi properties to pass to set as System properties
    * @throws Throwable
    */
   public static void launch(String mainClassName, String clientName, String args[],
         Properties jndiEnv)
      throws Throwable
   {
      // Set the RMIClassLoaderSpi implementation to JBossRMIClassLoader
      System.setProperty("java.rmi.server.RMIClassLoaderSpi", JBossRMIClassLoader.class.getName());
      // This is loaded lazily via a jndi dependency
      JBossClientMetaData xml = null;
      List<String> cp = loadClassPath(clientName);
      launch(xml, cp, mainClassName, clientName, args, jndiEnv);
   }

   /**
    * The client launcher entry point that create an mc to launch the client container.
    * @param clientClass
    * @param clientName
    * @param cp
    * @param args
    * @throws Throwable
    */
   public static void launch(JBossClientMetaData xml, List<String> classPath,
         String mainClassName, String applicationClientName, String args[],
         Properties jndiEnv)
      throws Throwable
   {
      log.info("ClientLauncher, version: "+VERSION);
      // Init the kernel and deployers
      args = init(args);

      // Pass in the jndi env properties so InitialContext() works
      if(jndiEnv != null)
      {
         for(Object key : jndiEnv.keySet())
         {
            String name = (String) key;
            System.setProperty(name, jndiEnv.getProperty(name));
         }
      }
      // Have the remoting class loader delegate to the tcl
      if(remotingDelgatesToTCL)
         System.setProperty(Remoting.CLASSLOADING_PARENT_FIRST_DELEGATION_PROP, "false");

      // Setup the 
      ArrayList<BeanMetaDataFactory> beanFactories = new ArrayList<BeanMetaDataFactory>();
      ArrayList<BeanMetaData> beans = new ArrayList<BeanMetaData>();

      // Add the common launcher beans, ClassLoaderSystem
      BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder("ClassLoaderSystem", ClassLoaderSystem.class.getName());
      builder.setFactoryClass(ClientLauncher.class.getName());
      builder.setFactoryMethod("getClassLoaderSystem");
      BeanMetaData classLoaderSystemBMD = builder.getBeanMetaData();
      addBeanMetaData(beanFactories, beans, classLoaderSystemBMD);

      // ClassLoading
      builder = BeanMetaDataBuilderFactory.createBuilder("ClassLoading", ClassLoading.class.getName());
      builder.addMethodInstallCallback("addModule", ControllerState.CONFIGURED);
      builder.addMethodUninstallCallback("removeModule", ControllerState.CONFIGURED);
      BeanMetaData classLoadingBMD = builder.getBeanMetaData();
      addBeanMetaData(beanFactories, beans, classLoadingBMD);

      try
      {
         builder = BeanMetaDataBuilderFactory.createBuilder("ClientContainer",
               "org.jboss.ejb3.client.ClientContainer");
         VFSClassLoaderFactory factory = new VFSClassLoaderFactory("ClientLauncherClassPath");
         ArrayList<String> roots = new ArrayList<String>();
         // Create the classpath
         log.info("Setting up classpath from: ");
         for(String path : classPath)
         {
            log.info(path);
            roots.add(path);
         }
         for(String path : extraClassPath)
         {
            log.info(path);
            roots.add(path);
         }
         log.info("End classpath");
         factory.setRoots(roots);
         beanFactories.add(factory);
         // ClientContainer(xml, mainClass, applicationClientName, jndiEnv);
         String classLoaderName = factory.getContextName();
         if(classLoaderName == null)
            classLoaderName = factory.getName() + ":" + factory.getVersion();
         String metaDataJndiName = applicationClientName + "/metaData";
         ValueMetaData xmlMD = new JndiDependencyValueMetaData(metaDataJndiName, jndiEnv, classLoaderName);
         builder.addConstructorParameter(JBossClientMetaData.class.getName(), xmlMD);
         //builder.addConstructorParameter(JBossClientMetaData.class.getName(), xml);
         builder.addConstructorParameter(Class.class.getName(), mainClassName);
         builder.addConstructorParameter(String.class.getName(), applicationClientName);
         builder.addConstructorParameter(Properties.class.getName(), jndiEnv);
         // Use vfs class loader as the ClientContainer class loader
         ValueMetaData classLoader = builder.createInject(classLoaderName);
         builder.setClassLoader(classLoader);
         BeanMetaData clientContainerMD = builder.getBeanMetaData();

         AbstractKernelDeployment deployment = new AbstractKernelDeployment();
         deployment.setName(factory.getName() + ":" + factory.getVersion());
         addBeanMetaData(beanFactories, beans, clientContainerMD);
         deployment.setBeanFactories(beanFactories);
         if(beans.size() > 0)
            deployment.setBeans(beans);
         deploy(deployment);
         validate();

         KernelController controller = kernel.getController();
         // ClientContainer
         KernelControllerContext context = (KernelControllerContext) controller.getContext("ClientContainer", ControllerState.INSTALLED);
         if (context == null)
            throw new Exception("ClientContainer bean was not created");
         Object client = context.getTarget();
         KernelControllerContext cclContext = (KernelControllerContext) controller.getContext(classLoaderName, ControllerState.INSTALLED);
         if (cclContext == null)
            throw new Exception(classLoaderName+" bean was not created");
         ClassLoader ccLoader = (ClassLoader) cclContext.getTarget();
         if (ccLoader == null )
            throw new Exception(classLoaderName+" bean was not created");
         if (client.getClass().getClassLoader() != ccLoader)
            log.warn(client.getClass().getClassLoader()+" != "+ccLoader);
         Class<?> clientContainerClass = ccLoader.loadClass("org.jboss.ejb3.client.ClientContainer");
         if (clientContainerClass.getClassLoader() != ccLoader)
            log.warn(clientContainerClass.getClassLoader()+" != "+ccLoader);

         // Invoke main on the underlying client main class through the ClientContainer
         ClassLoader prevLoader = Thread.currentThread().getContextClassLoader();
         try
         {
            // Get the mainClass
            Class<?> empty[] = {};
            Method getMainClass = clientContainerClass.getDeclaredMethod("getMainClass", empty);
            theMainClass = (Class<?>) getMainClass.invoke(client, null);
            // Invoke main
            Thread.currentThread().setContextClassLoader(ccLoader);
            Class<?> parameterTypes[] = { args.getClass() };
            Method invokeMain = clientContainerClass.getDeclaredMethod("invokeMain", parameterTypes);
            invokeMain.invoke(client, (Object) args);
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(prevLoader);
         }

         // 
         undeploy(deployment);
      }
      catch(Throwable e)
      {
         exception = e;
         throw e;
      }
   }

   /**
    * Create a ClassLoaderSystem with the default ClassLoaderDomain set to use
    * a AFTER ParentPolicy.
    *
    * @return ClassLoaderSystem instance
    */
   public static ClassLoaderSystem getClassLoaderSystem()
   {
      DefaultClassLoaderSystem system = new DefaultClassLoaderSystem();
      ClassLoaderDomain defaultDomain = system.getDefaultDomain();
      defaultDomain.setParentPolicy(ParentPolicy.AFTER);
      return system;
   }

   private static void addBeanMetaData(
         ArrayList<BeanMetaDataFactory> beanFactories,
         ArrayList<BeanMetaData> beans, BeanMetaData bmd)
   {
      // TODO Auto-generated method stub
      if(bmd instanceof BeanMetaDataFactory)
      {
         BeanMetaDataFactory bmdf = (BeanMetaDataFactory) bmd;
         beanFactories.add(bmdf);
      }
      else
      {
         // Have to use the deprecated beans
         beans.add(bmd);
      }
   }

   /**
    * Initialize the mc kernel and deployer as well as extract any client
    * launcher specific args from the input arguments.
    * 
    * @param args - the input args to both the launcher and the client main. The
    * launcher specific arguments are:
    *   -remotingDelgatesToTCL : true if remoting should first delegate to the the tcl based class loader
    *   -extraClassPath : comma separated list of vfsurls for additional classpath
    * @return the remaining arguments to pass to the client main.
    * @throws Throwable
    */
   private static String[] init(String[] args) throws Throwable
   {
      // Extract any launcher args from the input
      String[] newArgs = parseArgs(args);
      // Bootstrap the kernel
      AbstractBootstrap bootstrap = new BasicBootstrap();
      bootstrap.run();
      kernel = bootstrap.getKernel();
      
      // Create the deployer
      deployer = createDeployer();

      return newArgs;
   }
   /**
    * Extract the launcher specific arguments from the arguments array.
    * @see #init(String[])
    * @param args - the input args to both the launcher and the client main.
    * @return the remaining arguments to pass to the client main.
    */
   private static String[] parseArgs(String[] args)
   {
      ArrayList<String> tmp = new ArrayList<String>();
      for(int n = 0; n < args.length; n ++)
      {
         String arg = args[n];
         if(arg.equalsIgnoreCase("-remotingDelgatesToTCL"))
         {
            remotingDelgatesToTCL = Boolean.parseBoolean(args[++ n]);
         }
         else if(arg.equalsIgnoreCase("-extraClassPath"))
         {
            // Split classpath elements based on ','
            String cparg = args[++ n];
            String[] cp = cparg.split(",");
            for(String path : cp)
            {
               extraClassPath.add(path);
            }
            log.debug("Set extraClassPath to: "+extraClassPath);
         }
         else
         {
            tmp.add(arg);
         }
      }
      String[] newArgs = new String[tmp.size()];
      tmp.toArray(newArgs);
      return newArgs;
   }

   private static BasicXMLDeployer createDeployer()
   {
      return new BasicXMLDeployer(kernel, ControllerMode.AUTOMATIC);
   }

   /**
    * Deploy a deployment
    *
    * @param deployment the deployment
    * @throws Exception for any error  
    */
   private static void deploy(KernelDeployment deployment) throws Exception
   {
      log.debug("Deploying " + deployment);
      try
      {
         deployer.deploy(deployment);
         log.debug("Deployed " + deployment);
      }
      catch (Exception e)
      {
         throw e;
      }
      catch (Error e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Error deploying deployment: " + deployment, t);
      }
   }
   /**
    * Undeploy a deployment
    * 
    * @param deployment the deployment
    */
   private static void undeploy(KernelDeployment deployment)
   {
      log.debug("Undeploying " + deployment.getName());
      try
      {
         deployer.undeploy(deployment);
         log.trace("Undeployed " + deployment.getName());
      }
      catch (Throwable t)
      {
         log.warn("Error during undeployment: " + deployment.getName(), t);
      }
   }

   /**
    * Validate
    * 
    * @throws Exception for any error
    */
   private static void validate() throws Exception
   {
      try
      {
         deployer.validate();
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw e;
      }
      catch (Error e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t);
      }
   }

   public static void main(String[] args)
   {
      String[] roots = {
            "vfszip:/home/svn/JBossHead/jboss-head/build/output/jboss-5.0.0.CR2/server/cts/tmp/jsr88/assembly_classpath_appclient.ear/assembly_classpath_appclient_client.jar",
            "vfszip:/Users/svn/JBossHead/jboss-head/build/output/jboss-5.0.0.CR2/server/cts/tmp/jsr88/assembly_classpath_appclient.ear/libs/direct_classpath_util.jar",
            "vfszip:/Users/svn/JBossHead/jboss-head/build/output/jboss-5.0.0.CR2/server/cts/tmp/jsr88/assembly_classpath_appclient.ear/libs/indirect_classpath_util.jar"
      };
      VFSClassLoaderFactory10 factory = new VFSClassLoaderFactory10();
      factory.setRoots(Arrays.asList(roots));
      VFSClassLoaderPolicyModule module = new VFSClassLoaderPolicyModule(factory, "AppClientLoaderModule");
   
   }

   /**
    * RMIClassLoaderSpi that uses the thread context class loader
    * 
    * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
    * @author Scott.Stark@jboss.org
    * @version $Revision: 85945 $
    */
   public static class JBossRMIClassLoader
      extends RMIClassLoaderSpi
   {
      // Attributes ----------------------------------------------------
   
      /**
       * The JVM implementation (we delegate most work to it)
       */
      RMIClassLoaderSpi delegate = RMIClassLoader.getDefaultProviderInstance();
      
      // Constructors --------------------------------------------------
   
      /**
       * Required constructor
       */
      public JBossRMIClassLoader()
      {
      }
      
      // RMIClassLoaderSpi Implementation ------------------------------
   
      /*
       * Ignore the JVM, use the thread context classloader for proxy caching
       */
      public Class<?> loadProxyClass(String codebase, String[] interfaces, ClassLoader ignored)
         throws MalformedURLException, ClassNotFoundException
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         return delegate.loadProxyClass(codebase, interfaces, loader);
      }

      /*
       * Just delegate
       */
      public Class<?> loadClass(String codebase, String name, ClassLoader ignored)
         throws MalformedURLException, ClassNotFoundException
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         return delegate.loadClass(codebase, name, loader);
      }

      /*
       * Just delegate
       */
      public ClassLoader getClassLoader(String codebase)
         throws MalformedURLException
      {
         return delegate.getClassLoader(codebase);
      }
   
      /*
       * Try to delegate an default to the java.rmi.server.codebase on any
       * failure.
       */
      public String getClassAnnotation(Class<?> cl)
      {
         String annotation = null;
         try
         {
            annotation = delegate.getClassAnnotation(cl);
         }
         catch(Throwable t)
         {
            // Try the java.rmi.server.codebase property
            annotation = System.getProperty("java.rmi.server.codebase");
         }
         return annotation;
      }
   }

}
