/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.client;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.logging.Logger;
import org.jboss.naming.client.java.javaURLContextFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A java Main used to launch java ee app clients.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class AppClientMain
{
   // logging support
   private static final Logger log = Logger.getLogger(AppClientMain.class);

   /** The name of the client class whose main(String[]) should be launched */
   public static final String JBOSS_CLIENT_PARAM = "-jbossclient";
   /** The name of the javaee client context used to identify the server side ENC */
   public static final String J2EE_CLIENT_PARAM = "-"+javaURLContextFactory.J2EE_CLIENT_NAME_PROP;
   /** Comma separated list of AppClientLauncher implementation classes */
   public static final String LAUNCHERS_PARAM = "-launchers";
   /** The default list of launchers */
   public static String[] DEFAULT_LAUNCHERS = {ReflectionLauncher.class.getName()};

   /**
    * The main entry
    */
   public static void main(String[] args)
      throws Exception
   {
      log.debug("System Properties");
      Properties sysprops = System.getProperties();
      for (Object key : sysprops.keySet())
         log.debug("  " + key + "=" + sysprops.getProperty((String) key));

      // read the client class from args
      String clientClass = null;
      String clientName = null;
      ArrayList<String> newArgs = new ArrayList<String>();
      String[] launchers = DEFAULT_LAUNCHERS;
      for (int i = 0; i < args.length; i++)
      {
         String arg = args[i];
         log.debug("arg=" + arg);
         
         if( arg.equals(JBOSS_CLIENT_PARAM) )
         {
            clientClass = args[i+1];
            i ++;
         }
         else if( arg.equals(J2EE_CLIENT_PARAM) )
         {
            /* Set the j2ee.client system property so the AppContextFactory
            sees what name the client app JNDI enc is bound under
            */
            clientName = args[i+1];
            System.setProperty(javaURLContextFactory.J2EE_CLIENT_NAME_PROP, clientName);
            log.info(javaURLContextFactory.J2EE_CLIENT_NAME_PROP + "=" + clientName);
            i ++;
         }
         else if( arg.equals(LAUNCHERS_PARAM) )
         {
            launchers = args[i+1].split(",");
            log.info(LAUNCHERS_PARAM + "=" + args[i+1]);
            i ++;
         }
         else
         {
            newArgs.add(args[i]);
         }
      }

      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if( loader == null )
         loader = AppClientMain.class.getClassLoader();
      // Look for a manifest Main-Class
      if (clientClass == null)
      {
         clientClass = getMainClassName(loader);
         throw new IllegalArgumentException("Neither a Main-Class was found in the manifest, "
               +"nor was a " + JBOSS_CLIENT_PARAM + " specified");
      }
      // If J2EE_CLIENT_NAME_PROP was not specified, look in the jar descriptors
      if (clientName == null)
      {
         clientName = getClientName(loader);
      }

      String[] mainArgs = new String [newArgs.size()];
      newArgs.toArray(mainArgs);

      // Try each launcher in the order specified
      for(String launcherName : launchers)
      {
         try
         {
            Class<AppClientLauncher> launcherClass = (Class<AppClientLauncher>) loader.loadClass(launcherName);
            AppClientLauncher launcher = launcherClass.newInstance();
            launcher.launch(clientClass, clientName, mainArgs);
            break;
         }
         catch(Throwable t)
         {
            log.warn("Failed to launch using: "+launcherName, t);
         }
      }
   }

   /**
    * 
    * @param loader - class loader used to load descriptors as resources
    * @return
    */
   private static String getClientName(ClassLoader loader)
      throws Exception
   {
      String clientName = null;
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      // Try META-INF/application-client.xml application-client@id first
      URL appXmlURL = loader.getResource("META-INF/application-client.xml");
      if( appXmlURL != null )
      {
         InputStream is = appXmlURL.openStream();
         Document appXml = builder.parse(is);
         is.close();
         Element root = appXml.getDocumentElement();
         clientName = root.getAttribute("id");
         if( clientName != null )
            return clientName;
      }

      // Try META-INF/jboss-client.xml jndi-name
      URL jbossXmlURL = loader.getResource("META-INF/jboss-client.xml");
      if( appXmlURL != null )
      {
         InputStream is = jbossXmlURL.openStream();
         Document jbossXml = builder.parse(is);
         is.close();
         Element root = jbossXml.getDocumentElement();
         NodeList children = root.getChildNodes();
         for(int n = 0; n < children.getLength(); n ++)
         {
            Node node = children.item(n);
            if( node.getLocalName().equals("jndi-name") )
            {
               clientName = node.getNodeValue();
               return clientName;
            }
         }
      }
      // TODO: annotations on main class
      return null;
   }

   /**
    * Check the jar manifest for a Main-Class value.
    * 
    * @param loader
    * @return Main-Class value or null if none exists.
    * @throws Exception
    */
   private static String getMainClassName(ClassLoader loader)
      throws Exception
   {
      URL mfURL = loader.getResource("META-INF/MANIFEST.MF");
      if(mfURL == null)
      {
         return null;
      }

      InputStream is = mfURL.openStream();
      Manifest mf;
      try
      {
         mf = new Manifest(is);
      }
      finally
      {
         is.close();
      }
      Attributes attrs = mf.getMainAttributes();
      String mainClassName = attrs.getValue(Attributes.Name.MAIN_CLASS);         
      return mainClassName;
   }

}
