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

package org.jboss.naming;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.Enumeration;

import org.jboss.bootstrap.spi.Server;
import org.jboss.logging.Logger;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class NamingProviderURLWriter
{
   public static final String DEFAULT_PERSIST_FILE_NAME = "jnp-service.url";
   
   private static final Logger log = Logger.getLogger(NamingProviderURLWriter.class);
   
   private String bootstrapUrl;
   private String bindAddress;
   private int port;
   private Server server;
   private URI outputDir;
   private String filename = DEFAULT_PERSIST_FILE_NAME;

   public URI getOutputDirURI()
   {
      return outputDir;
   }

   public void setOutputDirURL(URI dir)
   {
      this.outputDir = dir;
   }

   public String getOutputFileName()
   {
      return filename == null ? DEFAULT_PERSIST_FILE_NAME : filename;
   }

   public void setOutputFileName(String name)
   {
      this.filename = name;
   }

   public void setServer(Server server)
   {
      this.server = server;
   }

   public String getBootstrapURL()
   {
      return bootstrapUrl;
   }
   
   public void setBootstrapURL(String url)
   {
      this.bootstrapUrl = url;      
   }

   public String getBootstrapAddress()
   {
      return bindAddress;
   }

   public void setBootstrapAddress(String bindAddress)
   {
      this.bindAddress = bindAddress;
   }

   public int getBootstrapPort()
   {
      return port;
   }

   public void setBootstrapPort(int port)
   {
      this.port = port;
   }

   public void start() throws Exception
   {
      establishBootStrapURL();
      
      if (bootstrapUrl != null)
      {
         File base = null;
         if (outputDir == null)
         {
            if (server != null)
            {
               base =  server.getConfig().getServerDataDir();
               outputDir = base.toURI();
            }
         }
         else
         {
            base = new File(outputDir);
         }
         
         if (base != null)
         {
            base.mkdirs();
            
            File file = new File(base, getOutputFileName());
            if (file.exists())
            {
               file.delete();
            }
            PrintWriter writer = null;
            try
            {               
               if (log.isTraceEnabled())
               {
                  log.trace("Creating file " + file);
               }
               file.createNewFile();
               file.deleteOnExit();
               
               writer = new PrintWriter(file);
               writer.println(bootstrapUrl);
               writer.flush();
            }
            catch (Exception e)
            {
               handleOutputFileCreationException(file.toURI(), e);
            }
            finally
            {
               if (writer != null)
               {
                  writer.close();
               }
            }
         }
         else
         {
            log.warn("No directory specified for " + getOutputFileName() + 
                  " cannot write the naming service url. Please configure either " +
                  "the 'server' property or the 'outputDir' property.");
         }
      }
      else
      {
         log.debug("No URLs to write");
      }
   }

   /** JBAS-7674 Try to intelligently deal with case where Windows has the file locked */
   private void handleOutputFileCreationException(URI outputFile, Exception e)
   {
      boolean handled = false;
      FileReader fr = null;
      try
      {
         File f = new File(outputFile);
         if (f.exists())
         {
            fr = new FileReader(f);
            String existing = new BufferedReader(fr).readLine();
            if (existing != null && this.bootstrapUrl.equals(existing.trim()))
            {
               log.info("Experienced a problem (" + e + 
                        ") updating the naming service URL file " + outputFile + 
                        " but the current contents are correct.");
            }
            else
            {
               log.error("Cannot update naming service URL file " + outputFile, e);
            }
            handled = true;
         }
      }
      catch (Exception ignored)
      {
         ;
      }
      finally
      {
         if (fr != null)
         {
            try
            {
               fr.close();
            }
            catch (Exception ignored) {}
         }
      }
      
      if (!handled)
      {
         log.error("Cannot create a naming service URL file at " + outputFile, e);
      }
      
   }

   public void stop() throws Exception
   {
      if (outputDir != null)
      {
         String outputFile = "";
         try
         {
            File f = new File(new File(outputDir), getOutputFileName());
            outputFile = f.getAbsolutePath();
            f.delete();
         }
         catch (Exception e)
         {
            log.warn("Failed to delete JNP URL file " + outputFile + " due to " + e);
         }
      }
   }

   protected void establishBootStrapURL() throws IOException
   {
      if (getBootstrapURL() == null && getBootstrapAddress() != null)
      {
         InetAddress addr = InetAddress.getByName(getBootstrapAddress());
         if (addr.isAnyLocalAddress())
         {
            addr = findLoopbackAddress();
            if (addr == null)
            {
               addr = InetAddress.getLocalHost();
            }
         }
         
         // Build the bootstrap URL
         StringBuilder sb = new StringBuilder("jnp://");
         if (addr instanceof Inet6Address)
         {
            sb.append('[');
            sb.append(addr.getHostAddress());
            sb.append(']');
         }
         else
         {
            sb.append(addr.getHostAddress());
         }
         sb.append(':');
         sb.append(getBootstrapPort());
         setBootstrapURL(sb.toString());
      }
   }

   private static InetAddress findLoopbackAddress() throws SocketException
   {
      Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
      while (ifaces.hasMoreElements())
      {
         NetworkInterface iface = ifaces.nextElement();
         Enumeration<InetAddress> addrs = iface.getInetAddresses();
         while (addrs.hasMoreElements())
         {
            InetAddress addr = addrs.nextElement();
            if (addr.isLoopbackAddress())
            {
               return addr;
            }
         }
      }
      return null;
   }

}
