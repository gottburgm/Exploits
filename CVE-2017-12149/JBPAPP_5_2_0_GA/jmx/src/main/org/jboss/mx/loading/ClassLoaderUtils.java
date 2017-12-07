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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.lang.reflect.Method;

import org.jboss.logging.Logger;
import org.jboss.mx.loading.LoadMgr3.PkgClassLoader;

/** Utility methods for class loader to package names, etc.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81022 $
 */
public class ClassLoaderUtils
{
   private static Logger log = Logger.getLogger(ClassLoaderUtils.class);

   /** The singleton instance of repository classloader comparator */
   private static final Comparator repositoryClassLoaderComparator = new RepositoryClassLoaderComparator();

   /** Format a string buffer containing the Class, Interfaces, CodeSource,
    and ClassLoader information for the given object clazz.

    @param clazz the Class
    @param results - the buffer to write the info to
    */
   public static void displayClassInfo(Class clazz, StringBuffer results)
   {
      // Print out some codebase info for the ProbeHome
      ClassLoader cl = clazz.getClassLoader();
      results.append("\n"+clazz.getName()+"("+Integer.toHexString(clazz.hashCode())+").ClassLoader="+cl);
      ClassLoader parent = cl;
      while( parent != null )
      {
         results.append("\n.."+parent);
         URL[] urls = getClassLoaderURLs(parent);
         int length = urls != null ? urls.length : 0;
         for(int u = 0; u < length; u ++)
         {
            results.append("\n...."+urls[u]);
         }
         if( parent != null )
            parent = parent.getParent();
      }
      CodeSource clazzCS = clazz.getProtectionDomain().getCodeSource();
      if( clazzCS != null )
         results.append("\n++++CodeSource: "+clazzCS);
      else
         results.append("\n++++Null CodeSource");

      results.append("\nImplemented Interfaces:");
      Class[] ifaces = clazz.getInterfaces();
      for(int i = 0; i < ifaces.length; i ++)
      {
         Class iface = ifaces[i];
         results.append("\n++"+iface+"("+Integer.toHexString(iface.hashCode())+")");
         ClassLoader loader = ifaces[i].getClassLoader();
         results.append("\n++++ClassLoader: "+loader);
         ProtectionDomain pd = ifaces[i].getProtectionDomain();
         CodeSource cs = pd.getCodeSource();
         if( cs != null )
            results.append("\n++++CodeSource: "+cs);
         else
            results.append("\n++++Null CodeSource");
      }
   }

   /** Use reflection to access a URL[] getURLs or URL[] getClasspath method so
    that non-URLClassLoader class loaders, or class loaders that override
    getURLs to return null or empty, can provide the true classpath info.
    */
   public static URL[] getClassLoaderURLs(ClassLoader cl)
   {
      URL[] urls = {};
      try
      {
         Class returnType = urls.getClass();
         Class[] parameterTypes = {};
         Class clClass = cl.getClass();
         Method getURLs = clClass.getMethod("getURLs", parameterTypes);
         if( returnType.isAssignableFrom(getURLs.getReturnType()) )
         {
            Object[] args = {};
            urls = (URL[]) getURLs.invoke(cl, args);
         }
         if( urls == null || urls.length == 0 )
         {
            Method getCp = clClass.getMethod("getClasspath", parameterTypes);
            if( returnType.isAssignableFrom(getCp.getReturnType()) )
            {
               Object[] args = {};
               urls = (URL[]) getCp.invoke(cl, args);               
            }
         }
      }
      catch(Exception ignore)
      {
      }
      return urls;
   }


   /** Get all of the URLClassLoaders from cl on up the hierarchy
    *
    * @param cl the class loader to start from
    * @return The possibly empty array of URLClassLoaders from cl through
    *    its parent class loaders
    */
   public static URLClassLoader[] getClassLoaderStack(ClassLoader cl)
   {
      ArrayList stack = new ArrayList();
      while( cl != null )
      {
         if( cl instanceof URLClassLoader )
         {
            stack.add(cl);
         }
         cl = cl.getParent();
      }
      URLClassLoader[] ucls = new URLClassLoader[stack.size()];
      stack.toArray(ucls);
      return ucls;
   }

   /** Translates a dot class name (java.lang.String) into a path form
    * suitable for a jar entry (java/lang/String.class)
    *
    * @param className java.lang.String
    * @return java/lang/String.class
    */
   public static String getJarClassName(String className)
   {
      String jarClassName = className.replace('.', '/');
      return jarClassName + ".class";
   }

   /** Parse a class name into its package prefix. This has to handle
      array classes whose name is prefixed with [L.
    */
   public static String getPackageName(String className)
   {
      int startIndex = 0;
      // Strip any leading "[+L" found in array class names
      if( className.length() > 0 && className.charAt(0) == '[' )
      {
         // Move beyond the [...[L prefix
         startIndex = className.indexOf('L') + 1;
      }
	   // Now extract the package name
      String pkgName = "";
      int endIndex = className.lastIndexOf('.');
      if( endIndex > 0 )
         pkgName = className.substring(startIndex, endIndex);
      return pkgName;
   }

   /** Parse a class name into its resource form. This has to handle
      array classes whose name is prefixed with [L.
    */
   public static String getResourceName(String className)
   {
      int startIndex = 0;
      // Strip any leading "[+L" found in array class names
      if( className.length() > 0 && className.charAt(0) == '[' )
      {
         // Move beyond the [...[L prefix
         startIndex = className.indexOf('L') + 1;
      }
	   // Now extract the package name
      String resName = "";
      int endIndex = className.lastIndexOf('.');
      if( endIndex > 0 )
         resName = className.substring(startIndex, endIndex);
      return resName.replace('.', '/');
   }

   /**
    * Create a new package set
    * 
    * @return the new package set
    */
   public static Set newPackageSet()
   {
      return new TreeSet(repositoryClassLoaderComparator);
   }

   /**
    * Clone a package set
    * 
    * @param toClone set to clone
    * @return the cloned package set
    */
   public static Set clonePackageSet(Object toClone)
   {
      TreeSet original = (TreeSet) toClone;
      return (Set) original.clone();
   }
      
   /** Augment the package name associated with a UCL.
    * @param cl the UCL that loads from url
    * @param packagesMap the Map<cl, String[]> to update
    * @param url the URL to parse for package names
    * @param prevPkgNames the set of pckage names already associated with cl
    * @return the updated unique set of package names
    * @throws Exception
    */
   public static void updatePackageMap(URL url, PkgNameListener listener)
      throws Exception
   {
      ClassPathIterator cpi = new ClassPathIterator(url);
      updatePackageMap(cpi, listener);
   }

   /** Augment the class names associated with a UCL.
    * @param cl the UCL that loads from url
    * @param classNamesMap the Map<cl, String[]> to update
    * @param url the URL to parse for class names
    * @param prevClassNames the set of pckage names already associated with cl
    * @return the updated list of class names
    * @throws Exception
    */
   public static String[] updateClassNamesMap(Object cl,
      Map classNamesMap, URL url, String[] prevClassNames)
      throws Exception
   {
      ClassPathIterator cpi = new ClassPathIterator(url);
      HashSet classNameSet = null;
      if (prevClassNames == null)
         classNameSet = new HashSet();
      else
         classNameSet = new HashSet(Arrays.asList(prevClassNames));
      return updateClassNamesMap(cl, classNamesMap, cpi, classNameSet);
   }

   static void updatePackageMap(ClassPathIterator cpi, PkgNameListener listener)
      throws Exception
   {
      ClassPathEntry entry;
      while( (entry = cpi.getNextEntry()) != null )
      {
         String name = entry.getName();
         // First look for a META-INF/INDEX.LIST entry
         if( name.equals("META-INF/INDEX.LIST") )
         {
            readJarIndex(cpi, listener);
            // We are done
            break;
         }

         // Skip empty directory entries
         if( entry.isDirectory() == true )
            continue;

         String pkgName = entry.toPackageName();
         listener.addPackage(pkgName);
      }
      cpi.close();
   }

   static String[] updateClassNamesMap(Object cl, Map classNamesMap,
      ClassPathIterator cpi, HashSet classNameSet)
      throws Exception
   {
      boolean trace = log.isTraceEnabled();
      ClassPathEntry entry;
      while( (entry = cpi.getNextEntry()) != null )
      {
         String name = entry.getName();
         // Skip empty directory entries
         if( entry.isDirectory() == true )
            continue;
         // Skip non .class files
         if( name.endsWith(".class") == false )
            continue;

         addClass(name, classNamesMap, cl, trace);
         classNameSet.add(name);
      }
      cpi.close();

      // Return an array of the package names
      String[] classNames = new String[classNameSet.size()];
      classNameSet.toArray(classNames);
      return classNames;
   }

   /** Read the JDK 1.3+ META-INF/INDEX.LIST entry to obtain the package
    names without having to iterate through all entries in the jar.
    */
   private static void readJarIndex(ClassPathIterator cpi, PkgNameListener listener)
      throws Exception
   {
      boolean trace = log.isTraceEnabled();
      InputStream zis = cpi.getInputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(zis));
      String line;
      // Skip the jar index header
      while( (line = br.readLine()) != null )
      {
         if( line.length() == 0 )
            break;
      }

      // Read the main jar section
      String jarName = br.readLine();
      if( trace )
         log.trace("Reading INDEX.LIST for jar: "+jarName);
      while( (line = br.readLine()) != null )
      {
         if( line.length() == 0 )
            break;
         String pkgName = line.replace('/', '.');
         listener.addPackage(pkgName);
      }
      br.close();
   }

   /** Add a class name to the UCL map.
    * @param jarClassName the class name in the jar (java/lang/String.class)
    * @param classNamesMap the UCL class name mappings
    * @param cl the UCL
    * @param trace the logging trace level flag
    */
   private static void addClass(String jarClassName, Map classNamesMap,
      Object cl, boolean trace)
   {
      LinkedList ucls = (LinkedList) classNamesMap.get(jarClassName);
      if( ucls == null )
      {
         ucls = new LinkedList();
         ucls.add(cl);
         classNamesMap.put(jarClassName, ucls);
      }
      else
      {
         boolean uclIsMapped = ucls.contains(cl);
         if( uclIsMapped == false )
         {
            log.debug("Multiple class loaders found for class: "+jarClassName
               + ", duplicate UCL: "+cl);
            ucls.add(cl);
         }
      }
      if( trace )
         log.trace("Indexed class: "+jarClassName+", UCL: "+ucls.get(0));
   }

   public static interface PkgNameListener
   {
      public void addPackage(String name);
   }

   /**
   */
   static class FileIterator
   {
      LinkedList subDirectories = new LinkedList();
      FileFilter filter;
      File[] currentListing;
      int index = 0;

      FileIterator(File start)
      {
         String name = start.getName();
         // Don't recurse into wars
         boolean isWar = name.endsWith(".war");
         if( isWar )
            currentListing = new File[0];
         else
            currentListing = start.listFiles();
      }
      FileIterator(File start, FileFilter filter)
      {
         String name = start.getName();
         // Don't recurse into wars
         boolean isWar = name.endsWith(".war");
         if( isWar )
            currentListing = new File[0];
         else
            currentListing = start.listFiles(filter);
         this.filter = filter;
      }

      File getNextEntry()
      {
         File next = null;
         if( index >= currentListing.length && subDirectories.size() > 0 )
         {
            do
            {
               File nextDir = (File) subDirectories.removeFirst();
               currentListing = nextDir.listFiles(filter);
            } while( currentListing.length == 0 && subDirectories.size() > 0 );
            index = 0;
         }
         if( index < currentListing.length )
         {
            next = currentListing[index ++];
            if( next.isDirectory() )
               subDirectories.addLast(next);
         }
         return next;
      }
   }

   /**
    */
   static class ClassPathEntry
   {
      String name;
      ZipEntry zipEntry;
      File fileEntry;

      ClassPathEntry(ZipEntry zipEntry)
      {
         this.zipEntry = zipEntry;
         this.name = zipEntry.getName();
      }
      ClassPathEntry(File fileEntry, int rootLength)
      {
         this.fileEntry = fileEntry;
         this.name = fileEntry.getPath().substring(rootLength);
      }

      String getName()
      {
         return name;
      }
      /** Convert the entry path to a package name
       */
      String toPackageName()
      {
         String pkgName = name;
         char separatorChar = zipEntry != null ? '/' : File.separatorChar;
         int index = name.lastIndexOf(separatorChar);
         if( index > 0 )
         {
            pkgName = name.substring(0, index);
            pkgName = pkgName.replace(separatorChar, '.');
         }
         else
         {
            // This must be an entry in the default package (e.g., X.class)
            pkgName = "";
         }
         return pkgName;
      }

      boolean isDirectory()
      {
         boolean isDirectory = false;
         if( zipEntry != null )
            isDirectory = zipEntry.isDirectory();
         else
            isDirectory = fileEntry.isDirectory();
         return isDirectory;
      }
   }

   /** An iterator for jar entries or directory structures.
   */
   static class ClassPathIterator
   {
      ZipInputStream zis;
      FileIterator fileIter;
      File file;
      int rootLength;

      ClassPathIterator(URL url) throws IOException
      {
         String protocol = url != null ? url.getProtocol() : null;
         if( protocol == null )
         {
         }
         else if( protocol.equals("file") )
         {
            File tmp = new File(url.getFile());
            if( tmp.isDirectory() )
            {
               rootLength = tmp.getPath().length() + 1;
               fileIter = new FileIterator(tmp);
            }
            else
            {
               // Assume this is a jar archive
               InputStream is = new FileInputStream(tmp);
               zis = new ZipInputStream(is);
            }
         }
         else
         {
            // Assume this points to a jar
            InputStream is = url.openStream();
            zis = new ZipInputStream(is);
         }
      }

      ClassPathEntry getNextEntry() throws IOException
      {
         ClassPathEntry entry = null;
         if( zis != null )
         {
            ZipEntry zentry = zis.getNextEntry();
            if( zentry != null )
               entry = new ClassPathEntry(zentry);
         }
         else if( fileIter != null )
         {
            File fentry = fileIter.getNextEntry();
            if( fentry != null )
               entry = new ClassPathEntry(fentry, rootLength);
            file = fentry;
         }

         return entry;
      }

      InputStream getInputStream() throws IOException
      {
         InputStream is = zis;
         if( zis == null )
         {
            is = new FileInputStream(file);
         }
         return is;
      }

      void close() throws IOException
      {
         if( zis != null )
            zis.close();
      }

   }
   
   /**
    * A comparator for comparing repository classloaders
    */
   private static class RepositoryClassLoaderComparator implements Comparator
   {
      /**
       * Compares two repository classloaders, they are ordered by:
       * 1) parent->child delegation rules in the loader repository
       * 2) added order inside the loader repository
       */
      public int compare(Object o1, Object o2)
      {
         if (o1 instanceof PkgClassLoader)
         {
            PkgClassLoader pkg1 = (PkgClassLoader) o1;
            PkgClassLoader pkg2 = (PkgClassLoader) o2;
            RepositoryClassLoader rcl1 = pkg1.ucl;
            RepositoryClassLoader rcl2 = pkg2.ucl;
            // We use the package classloader ordering before the repository order
            int test = (pkg1.order - pkg2.order);
            if (test != 0)
               return test;
            else
               return rcl1.getAddedOrder() - rcl2.getAddedOrder();
         }
         else
         {
            RepositoryClassLoader rcl1 = (RepositoryClassLoader) o1;
            RepositoryClassLoader rcl2 = (RepositoryClassLoader) o2;
            return rcl1.getAddedOrder() - rcl2.getAddedOrder();
            
            // REVIEW: Alternative to using the pkgClassLoader is
            //         ordering based on the loader repository
            
            //LoaderRepository lr1 = rcl1.getLoaderRepository();
            //LoaderRepository lr2 = rcl2.getLoaderRepository();

            // Are the loader repositories ordered?
            //int test = lr1.compare(lr2);
            //if (test != 0)
            //   return test;
            //else
            //   return rcl1.getAddedOrder() - rcl2.getAddedOrder();
         }
      }
   }
}
