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
import java.io.FileFilter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jboss.util.Strings;

/**
 * This deployer exists to prevent deployment of packages whose deployers are not yet
 * deployed.  It will accept only jar/zip format files or directories that don't 
 * have a META-INF directory, or if they do, don't have any .xml files there.  It
 * assumes any package with a META-INF/*.xml file needs a specialized deployer.
 *
 * @todo find a way to scan just the META-INF files, not the whole jar.
 *
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $
 */
public class JARDeployer extends SubDeployerSupport
   implements JARDeployerMBean
{
   /**
    * The suffixes we accept, along with their relative order.
    * 
    * For JARDeployer, this is only used to populate the MainDeployer's
    * SuffixOrder list, it is not used to actually match deployment suffixes
    */
   private static final String[] DEFAULT_ENHANCED_SUFFIXES = new String[] {
         "700:.jar",   // normal .jar
         "750:.zip",   // normal .zip
         "900:.last"   // make content of .last dirs/archives deploy last
   };
   
   private String[] descriptorNames = {
      ".xml"
   };

   /**
    * Default CTOR
    */
   public JARDeployer()
   {
      super.setEnhancedSuffixes(DEFAULT_ENHANCED_SUFFIXES);      
   }
   
   public String[] getDescriptorNames()
   {
      return descriptorNames;
   }
   
   public void setDescriptorNames(String[] descriptorNames)
   {
      this.descriptorNames = descriptorNames;
   }
   
   // ServiceMBeanSupport methods
   
   protected void stopService()
   {
      // This can't be run right now since the JARDeployer is started before the MainDeployer, 
      // so the MainDeployer is stopped first... so the JARDeployer can't unregister.

      // super.stopService();
   }

   // SubDeployer implementation

   /**
    * The <code>accepts</code> method is called by MainDeployer to 
    * determine which deployer is suitable for a DeploymentInfo.
    *
    * @todo find a way to scan just the META-INF files, not the whole jar.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @return a <code>boolean</code> value
    */
   public boolean accepts(DeploymentInfo di)
   {
      boolean trace = log.isTraceEnabled();
      
      try 
      {
         // Reject extensions not configured in this subdeployer
         // but do consider accepting non-dotted deployments,
         // like deploy-hasingleton
         if (di.shortName.indexOf('.') != -1 && super.accepts(di) == false)
         {
            return false;
         }
         
         // Reject deployments with a WEB-INF/ directory
         URL wdDir = di.localCl.findResource("WEB-INF/");
         if (wdDir != null) 
         {
            return false;
         }
         
         // Since a META-INF directory exists within rt.jar, we can't just do a 
         // getResource (it will always return rt.jar's version).
         // The method we want is findResource, but it is marked protected in
         // ClassLoader.  Fortunately, URLClassLoader exposes it which makes
         // this hack possible.  Anybody have a better way to check a URL
         // for the existance of a META-INF??
         URL ddDir;
         try 
         {
            ddDir = di.localCl.findResource("META-INF/");
            if (ddDir == null) 
            {
               log.debug("No META-INF or WEB-INF resource found, assuming it if for us");
               return true;
            }
         } 
         catch (ClassCastException e) 
         {
             // assume there is a META-INF...
             ddDir = new URL(di.url, "META-INF/");
         }

         if (ddDir.getProtocol().equals("file")) 
         {
            log.trace("File protocol: "+ddDir);
            File file = new File(ddDir.getFile());
            if (!file.exists()) 
            {
               log.warn("File not found: " + file);
               return true;
            }
            
            // Scan for any xml files in the META-INF dir
            File[] entries = file.listFiles(
               new FileFilter()
               {
                  public boolean accept(File pathname)
                  {
                     boolean accept = false;
                     String name = pathname.getName();
                     for(int n = 0; accept == false && n < descriptorNames.length; n ++)
                     {
                        String d = descriptorNames[n];
                        accept = name.endsWith(d);
                     }
                     return accept;
                  }
               }
            );
            log.debug("XML entries found: " + entries.length);
            return entries.length == 0;            
         } // end of if ()
         else if (ddDir.getProtocol().equals("jar") == true)
         {
            log.trace("jar protocol: " + ddDir);
            JarFile jarFile = null;
      
            try
            {
               URLConnection con = ddDir.openConnection();
               JarURLConnection jarConn = (JarURLConnection) con;
               /* Need to set caching to false otherwise closing the jarfile
               ends up conflicting with other users of the cached jar.
               */
               jarConn.setUseCaches(false);
               jarFile = jarConn.getJarFile();

               // Scan for any xml files in the META-INF dir
               if (trace)
                  log.trace("Descriptor names=" + Arrays.asList(descriptorNames));
               for (Enumeration e = jarFile.entries(); e.hasMoreElements();)
               {
                  JarEntry entry = (JarEntry)e.nextElement();
                  String name = entry.getName();
                  if (trace) 
                     log.trace("Looking at entry: '" + name + "'");
                  
                  // JBAS-2949 - Look for xml descriptors directly
                  // under META-INF/, not in META-INF/ subdirectories
                  if (name.startsWith("META-INF/") && Strings.count(name, "/") == 1) 
                  {
                     for (int n = 0; n < descriptorNames.length; n ++)
                     {
                        if (name.endsWith(descriptorNames[n]))
                        {
                           log.debug("Found entry: '" + name + "', matching: '"
                                 + descriptorNames[n] + "', rejecting jar");
                           
                           // Do not accept this as jar file
                           return false;
                        }
                     }
                  }
               } 
            }
            catch (Exception e)
            {
               log.warn("Looking inside jar failed; ignoring", e);
               return false;
            }
            finally
            {
               if (jarFile != null)
                  jarFile.close();
               jarFile = null;
            }

            log.debug("No xml files found");
            return true;
         }
         else
         {
            log.debug("Unrecognized protocol: " + ddDir.getProtocol());
         }

         return false;
      }
      catch (Exception e) 
      {
         log.trace("Ignored error", e);
         return false;
      }
   }
}
