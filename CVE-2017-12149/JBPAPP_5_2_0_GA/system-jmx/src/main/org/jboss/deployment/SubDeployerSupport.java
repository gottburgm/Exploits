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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.management.Notification;

import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.bootstrap.spi.util.ServerConfigUtil;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.util.file.JarUtils;
import org.jboss.util.stream.Streams;

/**
 * An abstract {@link SubDeployer}.
 *
 * Provides registration with {@link MainDeployer} as well as
 * implementations of init, create, start, stop and destroy that
 * generate JMX notifications on completion of the method.
 *
 * @version <tt>$Revision: 81033 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 */
public abstract class SubDeployerSupport extends ServiceMBeanSupport
   implements SubDeployerExt, SubDeployerExtMBean
{
   /**
    * Holds the native library <em>suffix</em> for this system.
    * 
    * Determined by examining the result of System.mapLibraryName(specialToken).
    * The special token defaults to "XxX", but can be changed by setting the
    * system property: <tt>org.jboss.deployment.SubDeployerSupport.nativeLibToken</tt>.
    */
   protected static final String nativeSuffix;

   /**
    * Holds the native library <em>prefix</em> for this system.
    *
    * @see #nativeSuffix
    */
   protected static final String nativePrefix;

   /** A proxy to the MainDeployer. */
   protected MainDeployerMBean mainDeployer;

   /** The temporary directory into which deployments are unpacked */
   protected File tempDeployDir;

   /** The list of enhancedSuffixes for this subdeployer */
   protected String[] enhancedSuffixes;
   
   /** The suffixes of interest to this subdeployer */
   protected String[] suffixes;
   
   /** The relative order of this subdeployer - not really used */
   protected int relativeOrder = -1;
   
   /** The temporary directory where native libs are unpacked. */
   private File tempNativeDir;

   /** Whether to load native libraries */
   private boolean loadNative = false;
   
   /**
    * The <code>createService</code> method is one of the ServiceMBean lifecyle operations.
    * (no jmx tag needed from superinterface)
    * 
    * @exception Exception if an error occurs
    */
   protected void createService() throws Exception
   {
      // get the temporary directories to use
      ServerConfig config = ServerConfigLocator.locate();
      tempNativeDir = config.getServerNativeDir();
      tempDeployDir = config.getServerTempDeployDir();
      loadNative = ServerConfigUtil.isLoadNative();

      // Setup the proxy to mainDeployer
      mainDeployer = (MainDeployerMBean)
         MBeanProxyExt.create(MainDeployerMBean.class,
                           MainDeployerMBean.OBJECT_NAME,
                           server);
   }

   /**
    * Performs SubDeployer registration.
    */
   protected void startService() throws Exception
   {
      // Register with the main deployer
      mainDeployer.addDeployer(this);
   }

   /**
    * Performs SubDeployer deregistration.
    */
   protected void stopService() throws Exception
   {
      // Unregister with the main deployer
      mainDeployer.removeDeployer(this);
   }

   /**
    * Clean up.
    */
   protected void destroyService() throws Exception
   {
      // Help the GC
      mainDeployer = null;
      tempNativeDir = null;
   }

   /**
    * Set an array of suffixes of interest to this subdeployer.
    * No need to register twice suffixes that may refer to
    * unpacked deployments (e.g. .sar, .sar/).
    * 
    * @param suffixes array of suffix strings
    */
   protected void setSuffixes(String[] suffixes)
   {
      this.suffixes = suffixes;
   }
   
   /**
    * Set the relative order of the specified suffixes
    * all to the same value.
    * 
    * @param relativeOrder the relative order of the specified suffixes
    */
   protected void setRelativeOrder(int relativeOrder)
   {
      this.relativeOrder = relativeOrder;
   }
   
   /**
    * Set the enhanced suffixes list for this deployer,
    * causing also the supported suffixes list to be updated.
    * 
    * Each enhanced suffix entries has the form:
    * 
    *    [order:]suffix
    * 
    * No need to register twice suffixes that may refer to
    * unpacked deployments (e.g. .sar, .sar/).
    * 
    * @param enhancedSuffixes
    */
   public void setEnhancedSuffixes(String[] enhancedSuffixes)
   {
      if (enhancedSuffixes != null)
      {
         int len = enhancedSuffixes.length;
         suffixes = new String[len];
         
         for (int i = 0; i < len; i++)
         {
            // parse each enhancedSuffix
            SuffixOrderHelper.EnhancedSuffix e =
               new SuffixOrderHelper.EnhancedSuffix(enhancedSuffixes[i]);
            
            suffixes[i] = e.suffix;
         }
      }
      this.enhancedSuffixes = enhancedSuffixes;
   }
   
   /**
    * Get an array of enhancedSuffixes
    * 
    * @return array of enhanced suffix strings
    */
   public String[] getEnhancedSuffixes()
   {
      return enhancedSuffixes;
   }
   
   /**
    * Get an array of suffixes of interest to this subdeployer
    * 
    * @return array of suffix strings
    */
   public String[] getSuffixes()
   {
      return suffixes;
   }
   
   /**
    * Get the relative order of the specified suffixes
    * 
    * @return the relative order of the specified suffixes
    */
   public int getRelativeOrder()
   {
      return relativeOrder;
   }

   /**
    * A default implementation that uses the suffixes registered
    * through either setSuffixes() or setEnhancedSuffixes(), to
    * decide if a module is deployable by this deployer.
    * 
    * If (according to DeploymentInfo) the deployment refers to
    * a directory, but not an xml or script deployment, then
    * the deployment suffix will be checked also against the
    * registered suffixes + "/".
    *
    * @param sdi the DeploymentInfo to check
    * @return whether the deployer can handle the deployment
    */
   public boolean accepts(DeploymentInfo sdi)
   {
      String[] acceptedSuffixes = getSuffixes();
      if (acceptedSuffixes == null)
      {
         return false;
      }
      else
      {
         String urlPath = sdi.url.getPath();
         String shortName = sdi.shortName;
         boolean checkDir = sdi.isDirectory && !(sdi.isXML || sdi.isScript);
         
         for (int i = 0; i < acceptedSuffixes.length; i++)
         {
            // First check the urlPath the might end in "/"
            // then check the shortName where "/" is removed
            if (urlPath.endsWith(acceptedSuffixes[i]) ||
                  (checkDir && shortName.endsWith(acceptedSuffixes[i])))
            {
               return true;
            }
         }
         return false;
      }
   }

   /**
    * Sub-classes should override this method to provide
    * custom 'init' logic.
    *
    * <p>This method calls the processNestedDeployments(di) method and then
    * issues a JMX notification of type SubDeployer.INIT_NOTIFICATION.
    * This behaviour can overridden by concrete sub-classes.  If further
    * initialization needs to be done, and you wish to preserve the
    * functionality, be sure to call super.init(di) at the end of your
    * implementation.
    */
   public void init(DeploymentInfo di) throws DeploymentException
   {
      processNestedDeployments(di);
      
      emitNotification(SubDeployer.INIT_NOTIFICATION, di);
   }

   /**
    * Sub-classes should override this method to provide
    * custom 'create' logic.
    *
    * This method issues a JMX notification of type SubDeployer.CREATE_NOTIFICATION.
    */
   public void create(DeploymentInfo di) throws DeploymentException
   {
      emitNotification(SubDeployer.CREATE_NOTIFICATION, di);
   }

   /**
    * Sub-classes should override this method to provide
    * custom 'start' logic.
    *
    * This method issues a JMX notification of type SubDeployer.START_NOTIFICATION.
    */
   public void start(DeploymentInfo di) throws DeploymentException
   {
      emitNotification(SubDeployer.START_NOTIFICATION, di);
   }

   /**
    * Sub-classes should override this method to provide
    * custom 'stop' logic.
    *
    * This method issues a JMX notification of type SubDeployer.START_NOTIFICATION.
    */
   public void stop(DeploymentInfo di) throws DeploymentException
   {
      emitNotification(SubDeployer.STOP_NOTIFICATION, di);
   }

   /**
    * Sub-classes should override this method to provide
    * custom 'destroy' logic.
    *
    * This method issues a JMX notification of type SubDeployer.DESTROY_NOTIFICATION.
    */
   public void destroy(DeploymentInfo di) throws DeploymentException
   {
      emitNotification(SubDeployer.DESTROY_NOTIFICATION, di);
   }

   /**
    * Simple helper to emit a subdeployer notification containing DeploymentInfo
    */
   protected void emitNotification(String type, DeploymentInfo di)
   {
      Notification notification = new Notification(type, this, getNextNotificationSequenceNumber());
      notification.setUserData(di);
      sendNotification(notification);      
   }
   
   /**
    * The <code>processNestedDeployments</code> method searches for any nested and
    * deployable elements.  Only Directories and Zipped archives are processed,
    * and those are delegated to the addDeployableFiles and addDeployableJar
    * methods respectively.  This method can be overridden for alternate
    * behaviour.
    */
   protected void processNestedDeployments(DeploymentInfo di) throws DeploymentException
   {
      log.debug("looking for nested deployments in : " + di.url);
      if (di.isXML)
      {
         // no nested archives in an xml file
         return;
      }

      if (di.isDirectory)
      {
         File f = new File(di.url.getFile());
         if (!f.isDirectory())
         {
            // something is screwy
            throw new DeploymentException
               ("Deploy file incorrectly reported as a directory: " + di.url);
         }

         addDeployableFiles(di, f);
      }
      else
      {
         try
         {
            // Obtain a jar url for the nested jar
            URL nestedURL = JarUtils.extractNestedJar(di.localUrl, this.tempDeployDir);
            JarFile jarFile = new JarFile(nestedURL.getFile());
            addDeployableJar(di, jarFile);
         }
         catch (Exception e)
         {
            log.warn("Failed to add deployable jar: " + di.localUrl, e);

            //
            // jason: should probably throw new DeploymentException
            //        ("Failed to add deployable jar: " + jarURLString, e);
            //        rather than make assumptions to what type of deployable
            //        file this was that failed...
            //

            return;
         }
      }
   }

   /**
    * This method returns true if the name is a recognized archive file.
    * 
    * It will query the MainDeployer that keeps a dynamically updated
    * list of known archive extensions. 
    *
    * @param name The "short-name" of the URL.  It will have any trailing '/'
    *        characters removed, and any directory structure has been removed.
    * @param url The full url.
    *
    * @return true iff the name ends in a known archive extension: .jar, .sar,
    *         .ear, .rar, .zip, .wsr, .war, or if the name matches the native
    *         library conventions.
    */
   protected boolean isDeployable(String name, URL url)
   {
      // any file under META-INF is not deployable; this method is called
      // also for zipped content, e.g. dir1/dir2.sar/META-INF/bla.xml 
      if (url.getPath().indexOf("META-INF") != -1)
      {
         return false;
      }
      String[] acceptedSuffixes = mainDeployer.getSuffixOrder();
      for (int i = 0; i < acceptedSuffixes.length; i++)
      {
         if (name.endsWith(acceptedSuffixes[i]))
         {
            return true;
         }
      }
      // this is probably obsolete
      return (name.endsWith(nativeSuffix) && name.startsWith(nativePrefix));
   }

   /**
    * This method recursively searches the directory structure for any files
    * that are deployable (@see isDeployable).  If a directory is found to
    * be deployable, then its subfiles and subdirectories are not searched.
    *
    * @param di the DeploymentInfo
    * @param dir The root directory to start searching.
    */
   protected void addDeployableFiles(DeploymentInfo di, File dir)
      throws DeploymentException
   {
      File[] files = dir.listFiles();
      for (int i = 0; i < files.length; i++)
      {
         File file = files[i];
         String name = file.getName();
         try
         {
            URL url = file.toURL();
            if (isDeployable(name, url))
            {
               deployUrl(di, url, name);
               // we don't want deployable units processed any further
               continue;
            }
         }
         catch (MalformedURLException e)
         {
            log.warn("File name invalid; ignoring: " + file, e);
         }
         if (file.isDirectory())
         {
            addDeployableFiles(di, file);
         }
      }
   }

   /**
    * This method searches the entire jar file for any deployable files
    * (@see isDeployable).
    *
    * @param di the DeploymentInfo
    * @param jarFile the jar file to process.
    */
   protected void addDeployableJar(DeploymentInfo di, JarFile jarFile)
      throws DeploymentException
   {
      String urlPrefix = "jar:"+di.localUrl.toString()+"!/";
      for (Enumeration e = jarFile.entries(); e.hasMoreElements();)
      {
         JarEntry entry = (JarEntry)e.nextElement();
         String name = entry.getName();
         try
         {
            URL url = new URL(urlPrefix+name);
            if (isDeployable(name, url))
            {
               // Obtain a jar url for the nested jar
               URL nestedURL = JarUtils.extractNestedJar(url, this.tempDeployDir);
               deployUrl(di, nestedURL, name);
            }
         }
         catch (MalformedURLException mue)
         {
            //
            // jason: why are we eating this exception?
            //
            log.warn("Jar entry invalid; ignoring: " + name, mue);
         }
         catch (IOException ex)
         {
            log.warn("Failed to extract nested jar; ignoring: " + name, ex);
         }
      }
   }

   protected void deployUrl(DeploymentInfo di, URL url, String name)
      throws DeploymentException
   {
      log.debug("nested deployment: " + url);
      try
      {
         //
         // jason: need better handling for os/arch specific libraries
         //        should be able to have multipule native libs in an archive
         //        one for each supported platform (os/arch), we only want to
         //        load the one for the current platform.
         //
         //        This probably means explitly listing the libraries in a
         //        deployment descriptor, which could probably also be used
         //        to explicitly map the files, as it might be possible to
         //        share a native lib between more than one version, no need
         //        to duplicate the file, metadata can be used to tell us
         //        what needs to be done.
         //
         //        Also need this mapping to get around the different values
         //        which are used by vm vendors for os.arch and such...
         //

         if (name.endsWith(nativeSuffix) && name.startsWith(nativePrefix))
         {
            File destFile = new File(tempNativeDir, name);
            log.info("Loading native library: " + destFile.toString());

            File parent = destFile.getParentFile();
            if (!parent.exists()) {
               parent.mkdirs();
            }

            InputStream in = url.openStream();
            OutputStream out = new FileOutputStream(destFile);
            Streams.copyb(in, out);

            out.flush();
            out.close();
            in.close();

            if (loadNative)
               System.load(destFile.toString());
         }
         else
         {
            new DeploymentInfo(url, di, getServer());
         }
      }
      catch (Exception ex)
      {
         throw new DeploymentException
            ("Could not deploy sub deployment "+name+" of deployment "+di.url, ex);
      }
   }

   /////////////////////////////////////////////////////////////////////////
   //                     Class Property Configuration                    //
   /////////////////////////////////////////////////////////////////////////

   /**
    * Static configuration properties for this class.  Allows easy access
    * to change defaults with system properties.
    */
   protected static class ClassConfiguration
      extends org.jboss.util.property.PropertyContainer
   {
      private String nativeLibToken = "XxX";

      public ClassConfiguration()
      {
         // properties will be settable under our enclosing classes group
         super(SubDeployerSupport.class);

         // bind the properties & the access methods
         bindMethod("nativeLibToken");
      }

      public void setNativeLibToken(final String token)
      {
         this.nativeLibToken = token;
      }

      public String getNativeLibToken()
      {
         return nativeLibToken;
      }
   }

   /** The singleton class configuration object for this class. */
   protected static final ClassConfiguration CONFIGURATION = new ClassConfiguration();

   //
   // jason: the following needs to be done after setting up the
   //        class config reference, so it is moved it down here.
   //

   /**
    * Determine the native library suffix and prefix.
    */
   static
   {
      // get the token to use from config, incase the default needs
      // to be changed to resolve problem with a specific platform
      String token = CONFIGURATION.getNativeLibToken();

      // then determine what the prefix and suffixes are for this platform
      String nativex = System.mapLibraryName(token);
      int xPos = nativex.indexOf(token);
      nativePrefix = nativex.substring(0, xPos);
      nativeSuffix = nativex.substring(xPos + 3);
   }
}
