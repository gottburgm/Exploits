/*
 * Copyright 1999,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.web.tomcat.service.jasper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import javax.servlet.ServletContext;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.Localizer;
import org.apache.jasper.compiler.TldLocationsCache;
import org.apache.jasper.xmlparser.ParserUtils;
import org.apache.jasper.xmlparser.TreeNode;
import org.jboss.logging.Logger;

/**
 * A prototype TagLibCache that allows one to obtain shared tlds from the
 * jbossweb sar conf/tlds directory.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class TagLibCache extends TldLocationsCache
{
   private static final String WEB_XML = "/WEB-INF/web.xml";
   private static final String JAR_FILE_SUFFIX = ".jar";

   private static Logger log = Logger.getLogger(TagLibCache.class);

   private ServletContext ctx;
   private HashMap mappings;
   private ArrayList tagLibJars;

   public TagLibCache(ServletContext ctx, ArrayList tagLibJars)
   {
      super(ctx, true);
      this.ctx = ctx;
      this.tagLibJars = tagLibJars;
   }

   /**
    * Gets the 'location' of the TLD associated with the given taglib 'uri'.
    *
    * Returns null if the uri is not associated with any tag library 'exposed'
    * in the web application. A tag library is 'exposed' either explicitly in
    * web.xml or implicitly via the uri tag in the TLD of a taglib deployed in a
    * jar file (WEB-INF/lib).
    * @param uri The taglib uri
    * @return An array of two Strings: The first element denotes the real path
    *         to the TLD. If the path to the TLD points to a jar file, then the
    *         second element denotes the name of the TLD entry in the jar file.
    *         Returns null if the uri is not associated with any tag library
    *         'exposed' in the web application.
    */
   public String[] getLocation(String uri) throws JasperException
   {
      if (mappings == null)
         init();
      String[] locations = (String[]) mappings.get(uri);
      return locations;
   }

   private synchronized void init() throws JasperException
   {
     if (mappings != null)
     {
       return;
     }

     HashMap tmpMappings = null;
      try
      {
         tmpMappings = new HashMap();
         processWebDotXml(tmpMappings);
         loadStandardTlds(tmpMappings);
         processTldsInFileSystem("/WEB-INF/", tmpMappings);
      }
      catch (Exception ex)
      {
         String msg = Localizer.getMessage("jsp.error.internal.tldinit", ex.getMessage());
         throw new JasperException(msg, ex);
      }
      finally
      {
        mappings = tmpMappings;
      }
   }

   /*
    * Populates taglib map described in web.xml.
    */
   protected void processWebDotXml(Map tmpMappings) throws Exception
   {

      InputStream is = null;

      try
      {
         // Acquire input stream to web application deployment descriptor
         String altDDName = (String) ctx.getAttribute(Constants.ALT_DD_ATTR);
         if (altDDName != null)
         {
            try
            {
               is = new FileInputStream(altDDName);
            }
            catch (FileNotFoundException e)
            {
               log.warn(Localizer.getMessage("jsp.error.internal.filenotfound",
                  altDDName));
            }
         }
         else
         {
            is = ctx.getResourceAsStream(WEB_XML);
            if (is == null)
            {
               log.warn(Localizer.getMessage("jsp.error.internal.filenotfound",
                  WEB_XML));
            }
         }

         if (is == null)
         {
            return;
         }

         // Parse the web application deployment descriptor
         TreeNode webtld = null;
         // altDDName is the absolute path of the DD
         if (altDDName != null)
         {
            webtld = new ParserUtils().parseXMLDocument(altDDName, is);
         }
         else
         {
            webtld = new ParserUtils().parseXMLDocument(WEB_XML, is);
         }

         // Allow taglib to be an element of the root or jsp-config (JSP2.0)
         TreeNode jspConfig = webtld.findChild("jsp-config");
         if (jspConfig != null)
         {
            webtld = jspConfig;
         }
         Iterator taglibs = webtld.findChildren("taglib");
         while (taglibs.hasNext())
         {

            // Parse the next <taglib> element
            TreeNode taglib = (TreeNode) taglibs.next();
            String tagUri = null;
            String tagLoc = null;
            TreeNode child = taglib.findChild("taglib-uri");
            if (child != null)
               tagUri = child.getBody();
            child = taglib.findChild("taglib-location");
            if (child != null)
               tagLoc = child.getBody();

            // Save this location if appropriate
            if (tagLoc == null)
               continue;
            if (uriType(tagLoc) == NOROOT_REL_URI)
               tagLoc = "/WEB-INF/" + tagLoc;
            String tagLoc2 = null;
            if (tagLoc.endsWith(JAR_FILE_SUFFIX))
            {
               tagLoc = ctx.getResource(tagLoc).toString();
               tagLoc2 = "META-INF/taglib.tld";
            }
            tmpMappings.put(tagUri, new String[]{tagLoc, tagLoc2}); // SYNC
         }
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (Throwable t)
            {
            }
         }
      }
   }

   protected void loadStandardTlds(Map tmpMappings) throws MalformedURLException // SYNC
   {
      if( tagLibJars.size() == 0 )
         return;

      // Locate the conf/web.xml
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL web = loader.getResource("server.xml");
      URL sarURL = new URL(web, ".");
      for(int n = 0; n < tagLibJars.size(); n ++)
      {
         String jarPath = (String) tagLibJars.get(n);
         try
         {
            URL url = new URL(sarURL, jarPath);
            String resourcePath = url.toString();
            log.debug("Scanning for tlds in: "+resourcePath);
            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            scanJar(conn, resourcePath, true, tmpMappings); // SYNC
         }
         catch (Exception e)
         {
            log.debug("Failed to scan: "+jarPath, e);
         }
      }
   }

   /*
    * Searches the filesystem under /WEB-INF for any TLD files, and adds
    * an implicit map entry to the taglib map for any TLD that has a <uri>
    * element.
    */
   protected void processTldsInFileSystem(String startPath, Map tmpMappings) // SYNC
      throws Exception
   {

      Set dirList = ctx.getResourcePaths(startPath);
      if (dirList != null)
      {
         Iterator it = dirList.iterator();
         while (it.hasNext())
         {
            String path = (String) it.next();
            if (path.endsWith("/"))
            {
               processTldsInFileSystem(path, tmpMappings); // SYNC
            }
            if( path.endsWith(".jar") )
            {
               URL resURL = ctx.getResource(path);
               URLConnection conn = resURL.openConnection();
               conn.setUseCaches(false);
               this.scanJar(conn, resURL.toString(), false, tmpMappings); // SYNC
            }
            else if ( path.endsWith(".tld") == true )
            {
               InputStream stream = ctx.getResourceAsStream(path);
               String uri = null;
               try
               {
                  uri = getUriFromTld(path, stream);
               }
               finally
               {
                  if (stream != null)
                  {
                     try
                     {
                        stream.close();
                     }
                     catch (Throwable t)
                     {
                        // do nothing
                     }
                  }
               }
               // Add implicit map entry only if its uri is not already
               // present in the map
               if (uri != null && tmpMappings.get(uri) == null) // SYNC
               {
                  tmpMappings.put(uri, new String[]{path, null}); // SYNC
               }
            }
         }
      }
   }

   /**
    * Scans the given JarInputStream for TLD files located in META-INF (or a
    * subdirectory of it), adding an implicit map entry to the taglib map for
    * any TLD that has a <uri> element.
    * @param conn - the 
    * @param ignore true if any exceptions raised when processing the given JAR
    * should be ignored, false otherwise
    */
   private void scanJar(URLConnection conn, String resourcePath, boolean ignore, Map tmpMappings) // SYNC
      throws JasperException, IOException
   {
      InputStream connIS = conn.getInputStream();
      JarInputStream jis = new JarInputStream(connIS);
      try
      {
         JarEntry entry = jis.getNextJarEntry();
         while( entry != null )
         {
            String name = entry.getName();
            if( name.endsWith(".tld") == false )
            {
               entry = jis.getNextJarEntry();
               continue;
            }

            EntryInputStream eis = new EntryInputStream(jis);
            String uri = getUriFromTld(resourcePath, eis);
            // Add implicit map entry only if its uri is not already
            // present in the map
            if (uri != null && tmpMappings.get(uri) == null) // SYNC
            {
               tmpMappings.put(uri, new String[]{resourcePath, name}); // SYNC
            }
            entry = jis.getNextJarEntry();
         }
      }
      catch (Exception ex)
      {
         if (!ignore)
         {
            throw new JasperException(ex);
         }
      }
      finally
      {
         if( jis != null )
         {
            try
            {
               jis.close();
            }
            catch (Throwable t)
            {
               // ignore
            }
         }

         if (connIS != null)
         {
            try
            {
               connIS.close();
            }
            catch (Throwable t)
            {
               // ignore
            }
         }
      }
   }

   /*
    * Returns the value of the uri element of the given TLD, or null if the
    * given TLD does not contain any such element.
    */
   private String getUriFromTld(String resourcePath, InputStream in)
      throws JasperException
   {
      // Parse the tag library descriptor at the specified resource path
      TreeNode tld = new ParserUtils().parseXMLDocument(resourcePath, in);
      TreeNode uri = tld.findChild("uri");
      if (uri != null)
      {
         String body = uri.getBody();
         if (body != null)
            return body;
      }

      return null;
   }

   /**
    * Used to ignore the close on the jar entry input stream since this
    * closes the jar stream, not just the entry.
    */ 
   static class EntryInputStream extends InputStream
   {
      private JarInputStream jis;
      EntryInputStream(JarInputStream jis)
      {
         this.jis = jis;
      }

      public int read() throws IOException
      {
         return jis.read();
      }

      public int available() throws IOException
      {
         return jis.available();
      }

      public void close() throws IOException
      {
         
      }

      public void reset() throws IOException
      {
         jis.reset();
      }

      public boolean markSupported()
      {
         return jis.markSupported();
      }

      public synchronized void mark(int readlimit)
      {
         jis.mark(readlimit);
      }

      public long skip(long n) throws IOException
      {
         return jis.skip(n);
      }

      public int read(byte b[], int off, int len) throws IOException
      {
         return jis.read(b, off, len);
      }
   }
}
