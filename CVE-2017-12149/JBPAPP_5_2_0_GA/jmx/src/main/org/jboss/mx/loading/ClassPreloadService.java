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
package org.jboss.mx.loading;

import java.net.URL;
import java.io.InputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.Arrays;

import org.jboss.logging.Logger;

/**
 A simple service that can be used preload all classes in the classpath
 of the thread context class loader seen in the start method as the thread
 context class loader. A simple xmbean fragment for deploying the service
 is:

    <mbean code="org.jboss.mx.loading.ClassPreloadService"
      name="jboss.mx:service=ClassPreloadService"
      xmbean-dd="">
      <xmbean>
          <attribute access="read-write"
             getMethod="getIncludePatterns" setMethod="setIncludePatterns">
             <description>The patterns for classpath includes</description>
             <name>IncludePatterns</name>
             <type>[Ljava.lang.String;</type>
          </attribute>
          <attribute access="read-write"
             getMethod="getExcludePatterns" setMethod="setExcludePatterns">
             <description>The patterns for classpath excludes</description>
             <name>ExcludePatterns</name>
             <type>[Ljava.lang.String;</type>
          </attribute>
          <attribute access="read-write"
             getMethod="isSimpleMatch" setMethod="setSimpleMatch">
             <description>A flag indicate if String.endsWith matching of includes/excludes should be used</description>
             <name>SimpleMatch</name>
             <type>[Ljava.lang.String;</type>
          </attribute>
         <operation>
            <name>start</name>
         </operation>
         <attribute>
      </xmbean>
       <attribute name="ExcludePatterns">jbossmq.jar</attribute>
       <attribute name="IncludePatterns"></attribute>
       <attribute name="SimpleMatch">true</attribute>
   </mbean>

 @author Scott.Stark@jboss.org
 @version $Revision: 81022 $
 */
public class ClassPreloadService
{
   static Logger log = Logger.getLogger(ClassPreloadService.class);
   /** The RE expressions for classpath elements to include */
   private String[] includePattern = {};
   /** The RE expressions for classpath elements to exclude */
   private String[] excludePattern = {};
   /** A flag indicate if String.endsWith matching of includes/excludes should be used */
   private boolean simpleMatch;
   boolean trace;

   public String[] getIncludePatterns()
   {
      return includePattern;
   }
   public void setIncludePatterns(String[] includePattern)
   {
      this.includePattern = includePattern;
   }

   public String[] getExcludePatterns()
   {
      return excludePattern;
   }
   public void setExcludePatterns(String[] excludePattern)
   {
      this.excludePattern = excludePattern;
   }

   public boolean isSimpleMatch()
   {
      return simpleMatch;
   }
   public void setSimpleMatch(boolean simpleMatch)
   {
      this.simpleMatch = simpleMatch;
   }

   public URL[] getRawClassPath()
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL[] fullCP = ClassLoaderUtils.getClassLoaderURLs(loader);
      return fullCP;
   }

   /**
    Load all classes seen the TCL classpath. This entails a scan of every
    archive in the TCL classpath URLs for .class entries.
    */
   public void start()
   {
      trace = log.isTraceEnabled();
      log.debug("Starting, includes="+ Arrays.asList(includePattern)
         +", excludes="+excludePattern);
      // Compile the include/exclude patterns
      Pattern[] includes = compileIncludes();
      Pattern[] excludes = compileExcludes();

      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL[] rawCP = ClassLoaderUtils.getClassLoaderURLs(loader);
      URL[] cp = filterCP(rawCP, includes, excludes);

      int loadedClasses = 0;
      int loadErrors = 0;
      for(int n = 0; n < cp.length; n ++)
      {
         URL u = cp[n];
         try
         {
            InputStream is = u.openStream();
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null)
            {
               String name = ze.getName();
               if (name.endsWith(".class"))
               {
                  int length = name.length();
                  String cname = name.replace('/', '.').substring(0, length - 6);
                  try
                  {
                     Class c = loader.loadClass(cname);
                     loadedClasses ++;
                     if (trace)
                        log.trace("loaded class: " + cname);
                  }
                  catch (Throwable e)
                  {
                     loadErrors ++;
                     if( trace )
                        log.trace("Failed to load class, "+e.getMessage());
                  }
               }
               ze = zis.getNextEntry();
            }
            zis.close();
         }
         catch (IOException ignore)
         {
            // Not a jar
         }
      }
      log.info("Loaded "+loadedClasses+" classes, "+loadErrors+" CNFEs");
   }

   public Pattern[] compileIncludes()
   {
      ArrayList tmp = new ArrayList();
      int count = this.includePattern != null ? includePattern.length : 0;
      for(int n = 0; n < count; n ++)
      {
         String p = includePattern[n];
         Pattern pat = Pattern.compile(p);
         tmp.add(pat);
      }
      Pattern[] includes = new Pattern[tmp.size()];
      tmp.toArray(includes);
      return includes;
   }
   public Pattern[] compileExcludes()
   {
      ArrayList tmp = new ArrayList();
      int count = this.excludePattern != null ? excludePattern.length : 0;
      for(int n = 0; n < count; n ++)
      {
         String p = excludePattern[n];
         Pattern pat = Pattern.compile(p);
         tmp.add(pat);
      }
      Pattern[] includes = new Pattern[tmp.size()];
      tmp.toArray(includes);
      return includes;
   }

   public URL[] filterCP(URL[] rawCP, Pattern[] includes, Pattern[] excludes)
   {
      if( trace )
         log.trace("filterCP, rawCP="+Arrays.asList(rawCP));
      ArrayList tmp = new ArrayList();
      int count = rawCP != null ? rawCP.length : 0;
      for(int m = 0; m < count; m ++)
      {
         URL pathURL = rawCP[m];
         String path = pathURL.toString();
         boolean excluded = false;

         // Excludes take priority over includes
         for(int n = 0; n < excludes.length; n ++)
         {
            Pattern p = excludes[n];
            Matcher matcher = p.matcher(path);
            if( simpleMatch && path.endsWith(p.pattern()) )
            {
               excluded = true;
               break;
            }
            else if( matcher.matches() )
            {
               excluded = true;
               break;
            }
         }
         if( excluded )
         {
            log.debug("Excluded: "+pathURL);
            continue;
         }

         // If there are no explicit includes, accept the non-excluded paths
         boolean included = includes.length == 0;
         for(int n = 0; n < includes.length; n ++)
         {
            Pattern p = includes[n];
            Matcher matcher = p.matcher(path);
            if( simpleMatch && path.endsWith(p.pattern()) )
               tmp.add(pathURL);
            else if( matcher.matches() )
               tmp.add(pathURL);
         }
         if( included )
         {
            log.debug("Included: "+pathURL);
            tmp.add(pathURL);
         }
      }
      URL[] cp = new URL[tmp.size()];
      tmp.toArray(cp);
      return cp;
   }
}
