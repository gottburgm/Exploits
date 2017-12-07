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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.mx.loading.LoaderRepositoryFactory;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.net.protocol.URLLister;
import org.jboss.net.protocol.URLListerFactory;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.util.Strings;
import org.jboss.util.stream.Streams;
import org.jboss.util.xml.JBossEntityResolver;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/**
 * This is the main Service Deployer API.
 *
 * @see org.jboss.system.Service
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:David.Maplesden@orion.co.nz">David Maplesden</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis<a/>
 * @version $Revision: 81033 $
 */
public class SARDeployer extends SubDeployerSupport
   implements SARDeployerMBean
{
   /** The suffixes we accept, along with their relative order */
   private static final String[] DEFAULT_ENHANCED_SUFFIXES = new String[] {
         "050:.deployer",
         "050:-deployer.xml",
         "150:.sar",
         "150:-service.xml"
   };
   
   /** The deployment descriptor to look for */
   private static final String JBOSS_SERVICE = "META-INF/jboss-service.xml";

   /** A proxy to the ServiceController. */
   private ServiceControllerMBean serviceController;

   /** The server data directory. */
   private File dataDir;

   /** The server configuration base URL. For example,
    file:/<jboss_dist_root>/server/default. Relative service
    descriptor codebase elements are relative to this URL.
    */
   private URL serverHomeURL;

   /** A HashMap<ObjectName, DeploymentInfo> for the deployed services */
   private HashMap serviceDeploymentMap = new HashMap();
   
   /**
    * A Map<String, List<String>> of the suffix to accepted archive META-INF descriptor name
    * @todo externalize this
    */
   private Map suffixToDescriptorMap = new ConcurrentReaderHashMap();

   /** A flag indicating if the parser used for the service descriptor should be configured for namespaces */
   private boolean useNamespaceAwareParser;

   /**
    * Default CTOR
    */
   public SARDeployer()
   {
      setEnhancedSuffixes(DEFAULT_ENHANCED_SUFFIXES);
      // Add the .har to META-INF/{jboss-service.xml,hibernate-service.xml} mapping
      ArrayList tmp = new ArrayList();
      tmp.add(JBOSS_SERVICE);
      tmp.add("META-INF/hibernate-service.xml");
      suffixToDescriptorMap.put(".har", tmp);
   }

   public boolean isUseNamespaceAwareParser()
   {
      return useNamespaceAwareParser;
   }

   public void setUseNamespaceAwareParser(boolean useNamespaceAwareParser)
   {
      this.useNamespaceAwareParser = useNamespaceAwareParser;
   }

   /**
    * Get the associated service DeploymentInfo if found, null otherwise
    * 
    * @param serviceName a service object name
    * @return The associated service DeploymentInfo if found, null otherwise
    * @jmx.managed-operation
    */
   public DeploymentInfo getService(ObjectName serviceName)
   {
      DeploymentInfo di = null;
      synchronized( serviceDeploymentMap )
      {
         di = (DeploymentInfo) serviceDeploymentMap.get(serviceName);
      }
      return di;
   }

   /**
    * Describe <code>init</code> method here.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    * @jmx.managed-operation
    */
   public void init(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         if (di.url.getPath().endsWith("/"))
         {
            // the URL is a unpacked collection, watch the deployment descriptor
            di.watch = new URL(di.url, JBOSS_SERVICE);
         }
         else
         {
            // just watch the original URL
            di.watch = di.url;
         }

         // Get the document if not already present
         parseDocument(di);

         // Check for a custom loader-repository for scoping
         NodeList loaders = di.document.getElementsByTagName("loader-repository");
         if( loaders.getLength() > 0 )
         {
            Element loader = (Element) loaders.item(0);
            LoaderRepositoryConfig config = LoaderRepositoryFactory.parseRepositoryConfig(loader);
            di.setRepositoryInfo(config);
         }

         // In case there is a dependent classpath defined parse it
         parseXMLClasspath(di);

         // Copy local directory if local-directory element is present
         NodeList lds = di.document.getElementsByTagName("local-directory");
         log.debug("about to copy " + lds.getLength() + " local directories");

         for (int i = 0; i< lds.getLength(); i++)
         {
            Element ld = (Element)lds.item(i);
            String path = ld.getAttribute("path");
            log.debug("about to copy local directory at " + path);

            // Get the url of the local copy from the classloader.
            log.debug("copying from " + di.localUrl + path + " -> " + dataDir);

            inflateJar(di.localUrl, dataDir, path);
         }
      }
      catch (DeploymentException de)
      {
         throw de;
      }
      catch (Exception e)
      {
         throw new DeploymentException(e);
      }

      // invoke super-class initialization
      super.init(di);
   }

   /**
    * Describe <code>create</code> method here.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    * @jmx.managed-operation
    */
   public void create(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         // install the MBeans in this descriptor
         log.debug("Deploying SAR, create step: url " + di.url);

         // Register the SAR UCL as an mbean so we can use it as the service loader
         ObjectName uclName = di.ucl.getObjectName();
         if( getServer().isRegistered(uclName) == false )
         {
            log.debug("Registering service UCL="+uclName);
            getServer().registerMBean(di.ucl, uclName);
         }

         List mbeans = di.mbeans;
         mbeans.clear();
         List descriptorMbeans = serviceController.install(di.document.getDocumentElement(), uclName);
         mbeans.addAll(descriptorMbeans);

         // create the services
         for (Iterator iter = di.mbeans.iterator(); iter.hasNext(); )
         {
            ObjectName service = (ObjectName)iter.next();

            // The service won't be created until explicitly dependent mbeans are created
            serviceController.create(service);
            synchronized( this.serviceDeploymentMap )
            {
               serviceDeploymentMap.put(service, di);
            }
         }

         // Generate a JMX notification for the create stage
         super.create(di);
      }
      catch(DeploymentException e)
      {
         log.debug("create operation failed for package "+ di.url, e);
         destroy(di);
         throw e;
      }
      catch (Exception e)
      {
         log.debug("create operation failed for package "+ di.url, e);
         destroy(di);
         throw new DeploymentException("create operation failed for package "
            + di.url, e);
      }
   }

   /**
    * The <code>start</code> method starts all the mbeans in this DeploymentInfo..
    *
    * @param di a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    * @jmx.managed-operation
    */
   public void start(DeploymentInfo di) throws DeploymentException
   {
      log.debug("Deploying SAR, start step: url " + di.url);
      try
      {
         // start the services

         for (Iterator iter = di.mbeans.iterator(); iter.hasNext(); )
         {
            ObjectName service = (ObjectName)iter.next();

            // The service won't be started until explicitely dependent mbeans are started
            serviceController.start(service);
         }
         // Generate a JMX notification for the start stage
         super.start(di);
      }
      catch (Exception e)
      {
         stop(di);
         destroy(di);
         throw new DeploymentException("start operation failed on package "
            + di.url, e);
      }
   }

   /** The stop method invokes stop on the mbeans associatedw ith the deployment
    * in reverse order relative to create.
    *
    * @param di the <code>DeploymentInfo</code> value to stop.
    * @jmx.managed-operation
    */
   public void stop(DeploymentInfo di)
   {
      log.debug("undeploying document " + di.url);

      List services = di.mbeans;
      int lastService = services.size();

      // stop services in reverse order.
      for (ListIterator i = services.listIterator(lastService); i.hasPrevious();)
      {
         ObjectName name = (ObjectName)i.previous();
         log.debug("stopping mbean " + name);
         try
         {
            serviceController.stop(name);
         }
         catch (Exception e)
         {
            log.error("Could not stop mbean: " + name, e);
         } // end of try-catch
      }

      // Generate a JMX notification for the stop stage
      try
      {
         super.stop(di);
      }
      catch(Exception ignore)
      {
      }
   }

   /** The destroy method invokes destroy on the mbeans associated with
    * the deployment in reverse order relative to create.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @jmx.managed-operation
    */
   public void destroy(DeploymentInfo di)
   {
      List services = di.mbeans;
      int lastService = services.size();

      for (ListIterator i = services.listIterator(lastService); i.hasPrevious();)
      {
         ObjectName name = (ObjectName)i.previous();
         log.debug("destroying mbean " + name);
         synchronized( serviceDeploymentMap )
         {
            serviceDeploymentMap.remove(name);
         }

         try
         {
            serviceController.destroy(name);
         }
         catch (Exception e)
         {
            log.error("Could not destroy mbean: " + name, e);
         } // end of try-catch
      }

      for (ListIterator i = services.listIterator(lastService); i.hasPrevious();)
      {
         ObjectName name = (ObjectName)i.previous();
         log.debug("removing mbean " + name);
         try
         {
            serviceController.remove(name);
         }
         catch (Exception e)
         {
            log.error("Could not remove mbean: " + name, e);
         } // end of try-catch
      }

      // Unregister the SAR UCL
      try
      {
         ObjectName uclName = di.ucl.getObjectName();
         if( getServer().isRegistered(uclName) == true )
         {
            log.debug("Unregistering service UCL="+uclName);
            getServer().unregisterMBean(uclName);
         }
      }
      catch(Exception ignore)
      {
      }

      // Generate a JMX notification for the destroy stage
      try
      {
         super.destroy(di);
      }
      catch(Exception ignore)
      {
      }
   }

   // ServiceMBeanSupport overrides  --------------------------------
   
   /**
    * The startService method gets the mbeanProxies for MainDeployer
    * and ServiceController, used elsewhere.
    *
    * @exception Exception if an error occurs
    */
   protected void startService() throws Exception
   {
      super.startService();

      // get the controller proxy
      serviceController = (ServiceControllerMBean)
         MBeanProxyExt.create(ServiceControllerMBean.class,
            ServiceControllerMBean.OBJECT_NAME, server);

      // Get the data directory, install url & library url
      ServerConfig config = ServerConfigLocator.locate();
      dataDir = config.getServerDataDir();
      serverHomeURL = config.getServerHomeURL();
   }

   /**
    * This method stops all the applications in this server.
    */
   protected void stopService() throws Exception
   {
      // deregister with MainDeployer
      super.stopService();
      
      // Help GC
      serviceController = null;      
      serverHomeURL = null;
      dataDir = null;
   }

   
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }
   
   // Protected -----------------------------------------------------
   
   protected File[] listFiles(final String urlspec) throws Exception
   {
      URL url = Strings.toURL(urlspec);

      // url is already canonical thanks to Strings.toURL
      File dir = new File(url.getFile());

      File[] files = dir.listFiles(new java.io.FileFilter()
         {
            public boolean accept(File pathname)
            {
               String name = pathname.getName().toLowerCase();
               return (name.endsWith(".jar") || name.endsWith(".zip"));
            }
         });

      return files;
   }

   /**
    * @param di
    * @throws Exception
    */
   protected void parseXMLClasspath(DeploymentInfo di)
      throws Exception
   {
      ArrayList classpath = new ArrayList();
      URLListerFactory listerFactory = new URLListerFactory();

      NodeList children = di.document.getDocumentElement().getChildNodes();
      for (int i = 0; i < children.getLength(); i++)
      {
         if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
         {
            Element classpathElement = (Element)children.item(i);
            if (classpathElement.getTagName().equals("classpath"))
            {
               log.debug("Found classpath element: " + classpathElement);
               if (!classpathElement.hasAttribute("codebase"))
               {
                  throw new DeploymentException
                     ("Invalid classpath element missing codebase: " + classpathElement);
               }
               String codebase = classpathElement.getAttribute("codebase").trim();
               // Replace any system property references like ${x}
               codebase = StringPropertyReplacer.replaceProperties(codebase);

               String archives = null;
               if (classpathElement.hasAttribute("archives"))
               {
                  archives = classpathElement.getAttribute("archives").trim();
                  // Replace any system property references like ${x}
                  archives = StringPropertyReplacer.replaceProperties(archives);
                  if ("".equals(archives))
                  {
                     archives = null;
                  }
               }

               // Convert codebase to a URL
               // "." is resolved relative to the deployment
               // other URLs are resolved relative to SERVER_HOME
               URL codebaseUrl;
               if (".".equals(codebase))
               {
                  codebaseUrl = new URL(di.url, "./");
               }
               else
               {
                  if (archives != null && codebase.endsWith("/") == false)
                  {
                     codebase += "/";
                  }
                  codebaseUrl = new URL(serverHomeURL, codebase);
               }
               log.debug("codebase URL is " + codebaseUrl);

               if (archives == null)
               {
                  // archives not supplied so add the codebase itself
                  classpath.add(codebaseUrl);
                  log.debug("added codebase to classpath");
               }
               else
               {
                  // obtain a URLLister for the codebase and use it to obtain
                  // the list of URLs to add
                  log.debug("listing codebase for archives matching " + archives);
                  URLLister lister = listerFactory.createURLLister(codebaseUrl);
                  log.debug("URLLister class is " + lister.getClass().getName());
                  classpath.addAll(lister.listMembers(codebaseUrl, archives));
               }
            } // end of if ()

         } // end of if ()
      } //end of for

      // Ok, now we've found the list of urls we need... deploy their classes.
      Iterator jars = classpath.iterator();
      while (jars.hasNext())
      {
         URL neededURL = (URL) jars.next();
         di.addLibraryJar(neededURL);
         log.debug("deployed classes for " + neededURL);
      }
   }

   /** Parse the META-INF/jboss-service.xml descriptor
    */
   protected void parseDocument(DeploymentInfo di)
      throws Exception
   {
      InputStream stream = null;
      try
      {
         if (di.document == null)
         {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(useNamespaceAwareParser);
            DocumentBuilder parser = factory.newDocumentBuilder();
            URL docURL = di.localUrl;
            URLClassLoader localCL = di.localCl;
            // Load jboss-service.xml from the jar or directory
            if (di.isXML == false)
            {
               // Check the suffix to descriptor mapping
               String[] descriptors = getDescriptorName(di);
               for(int n = 0; n < descriptors.length; n ++)
               {
                  String descriptor = descriptors[n];
                  docURL = localCL.findResource(descriptor);
                  if( docURL != null )
                  {
                     // If this is a unpacked deployment, update the watch url
                     if (di.url.getPath().endsWith("/"))
                     {
                        di.watch = new URL(di.url, descriptor);
                        log.debug("Updated watch URL to: "+di.watch);
                     }
                     break;
                  }
               }
               // No descriptors, use the default META-INF/jboss-service.xml
               if( docURL == null )
                  docURL = localCL.findResource(JBOSS_SERVICE);
            }
            // Validate that the descriptor was found
            if (docURL == null)
               throw new DeploymentException("Failed to find META-INF/jboss-service.xml for archive " + di.shortName);

            stream = docURL.openStream();
            InputSource is = new InputSource(stream);
            is.setSystemId(docURL.toString());
            parser.setEntityResolver(new JBossEntityResolver());
            di.document = parser.parse(is);
         }
         else
         {
            log.debug("Using existing deployment.document");
         }
      }
      finally
      {
         // Close the stream to get around "Too many open files"-errors
         try
         {
            stream.close();
         }
         catch (Exception ignore)
         {
         }
      }
   }

   /**
    * The <code>inflateJar</code> copies the jar entries
    * from the jar url jarUrl to the directory destDir.
    * It can be used on the whole jar, a directory, or
    * a specific file in the jar.
    *
    * @param url    the <code>URL</code> if the directory or entry to copy.
    * @param destDir   the <code>File</code> value of the directory in which to
    *                  place the inflated copies.
    *
    * @exception DeploymentException if an error occurs
    * @exception IOException if an error occurs
    */
   protected void inflateJar(URL url, File destDir, String path)
      throws DeploymentException, IOException
   {
      String filename = url.getFile();
      JarFile jarFile = new JarFile(filename);
      try
      {
         for (Enumeration e = jarFile.entries(); e.hasMoreElements(); )
         {
            JarEntry entry = (JarEntry)e.nextElement();
            String name = entry.getName();

            if (path == null || name.startsWith(path))
            {
               File outFile = new File(destDir, name);
               if (!outFile.exists())
               {
                  if (entry.isDirectory())
                  {
                     outFile.mkdirs();
                  }
                  else
                  {
                     Streams.copyb(jarFile.getInputStream(entry),
                                   new FileOutputStream(outFile));
                  }
               } // end of if (outFile.exists())
            } // end of if (matches path)
         }
      }
      finally
      {
         jarFile.close();
      }
   }

   // Private -------------------------------------------------------
   
   /**
    * Parse the deployment url for its suffix and return a list of the accepted service
    * descriptor names to look for.
    * 
    * @param sdi - the sar deployment info
    * @return the array of sar archive/directory relative names of the service descriptor. If
    * there is no suffix to descriptor mapping, the default of {JBOSS_SERVICE} will be
    * returned.
    */
   private String[] getDescriptorName(DeploymentInfo sdi)
   {
      String[] descriptorNames = {JBOSS_SERVICE};
      String shortName = sdi.shortName;
      int dot = shortName.lastIndexOf('.');
      if( dot >= 0 )
      {
         String suffix = shortName.substring(dot);
         List descriptors = (List) suffixToDescriptorMap.get(suffix);
         if( descriptors != null )
         {
            descriptorNames = new String[descriptors.size()];
            descriptors.toArray(descriptorNames);
         }
      }
      return descriptorNames;
   }
}
