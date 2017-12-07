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
package org.jboss.system;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.logging.Logger;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceMetaDataParser;
import org.jboss.system.metadata.ServiceValueContext;
import org.jboss.util.xml.DOMWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Service configuration helper.
 * 
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:hiram@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version <tt>$Revision: 86940 $</tt>
 */
public class ServiceConfigurator
{
   /** The MBean server which this service is registered in. */
   private final MBeanServer server;
   
   /** The parent service controller */
   private final ServiceController serviceController;
   
   /** The ServiceCreator */
   private final ServiceCreator serviceCreator;

   /** Instance logger. */
   private static final Logger log = Logger.getLogger(ServiceConfigurator.class);

   /**
    * Rethrow an error as an exception
    * 
    * @param context the context
    * @param t the original throwable
    * @return never
    * @throws Exception always
    */
   public static Exception rethrow(String context, Throwable t) throws Exception
   {
      if (t instanceof Error)
         throw (Error) t;
      else if (t instanceof Exception)
         throw (Exception) t;
      throw new RuntimeException(context, t);
   }

   /**
    * Configure an MBean
    * 
    * @param server the server
    * @param controller the service controller
    * @param objectName the object name
    * @param classLoaderName the classloader object name
    * @param attrs the attributes
    * @throws Exception for any error
    */
   public static void configure(MBeanServer server, ServiceController controller, ObjectName objectName, ObjectName classLoaderName, Collection<ServiceAttributeMetaData> attrs) throws Exception
   {
      server = checkMBeanServer(server, controller);
      ClassLoader cl = server.getClassLoader(classLoaderName);
      configure(server, controller, objectName, cl, attrs);
   }
   
   /**
    * Configure an MBean
    * 
    * @param server the server
    * @param controller the service controller
    * @param objectName the object name
    * @param cl the classloader
    * @param attrs the attributes
    * @throws Exception for any error
    */
   public static void configure(MBeanServer server, ServiceController controller, ObjectName objectName, ClassLoader cl, Collection<ServiceAttributeMetaData> attrs) throws Exception
   {
      ServiceValueContext valueContext = new ServiceValueContext(server, controller, cl);
      server = checkMBeanServer(server, controller);
      
      HashMap<String, MBeanAttributeInfo> attributeMap = getAttributeMap(server, objectName);

      for (ServiceAttributeMetaData attribute : attrs)
      {
         String attributeName = attribute.getName();
         if (attributeName == null || attributeName.length() == 0)
            throw new RuntimeException("No or empty attribute name for " + objectName);
         MBeanAttributeInfo attributeInfo = attributeMap.get(attributeName);
         if (attributeInfo == null)
         {
            throw new RuntimeException("No Attribute found with name: " + attributeName + " for " + objectName
                  +", attributes: "+attributeMap.keySet());
         }

         valueContext.setAttributeInfo(attributeInfo);
         Object value = null;
         ClassLoader previous = SecurityActions.setContextClassLoader(cl);
         try
         {
            value = attribute.getValue(valueContext);
         }
         finally
         {
            SecurityActions.resetContextClassLoader(previous);
         }
         try
         {
            if (log.isDebugEnabled())
            {
               Object outputValue = value;
               if (attributeName.toLowerCase().indexOf("password") != -1)
                  outputValue = "****";
               log.debug(attributeName + " set to " + outputValue + " in " + objectName);
            }
            server.setAttribute(objectName, new Attribute(attributeName, value));
         }
         catch (Throwable t)
         {
            throw new RuntimeException("Exception setting attribute " + attributeName + " on mbean " + objectName, JMXExceptionDecoder.decode(t));
         }
      }
   }

   /**
    * Check the server/controller parameters
    * 
    * @param server the server
    * @param controller the controller
    * @return the server
    */
   private static MBeanServer checkMBeanServer(MBeanServer server, ServiceController controller)
   {
      if (server != null)
         return server;
      
      if (controller != null)
         return controller.getMBeanServer();
      
      throw new IllegalArgumentException("Either the server or controller must be passed");
   }

   /**
    * Get an attribute map for the MBean
    * 
    * @param server the server
    * @param objectName the object name
    * @return a map of attribute name to attribute info
    * @throws Exception for any error
    */
   public static HashMap<String, MBeanAttributeInfo> getAttributeMap(MBeanServer server, ObjectName objectName) throws Exception
   {
      MBeanInfo info;
      try
      {
         info = server.getMBeanInfo(objectName);
      }
      catch (InstanceNotFoundException e)
      {
         // The MBean is no longer available
         throw new RuntimeException("Trying to configure nonexistent mbean: " + objectName);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not get mbeanInfo", JMXExceptionDecoder.decode(e));
      }
      if (info == null)
         throw new RuntimeException("MBeanInfo is null for mbean: " + objectName);
      MBeanAttributeInfo[] attributes = info.getAttributes();
      HashMap<String, MBeanAttributeInfo> attributeMap = new HashMap<String, MBeanAttributeInfo>();
      for (int i = 0; i < attributes.length; i++)
      {
         MBeanAttributeInfo attr = attributes[i];
         attributeMap.put(attr.getName(), attr);
      }
      
      return attributeMap;
   }
   
   /**
    * Constructor
    * 
    * @deprecated the service controller no longer uses the service configurator and vice-versa
    * @param server the mbean server
    * @param serviceController the servie controller
    * @param serviceCreator the service creator
    */
   public ServiceConfigurator(MBeanServer server, ServiceController serviceController, ServiceCreator serviceCreator)
   {
      if (server == null)
         throw new IllegalArgumentException("Null server");
      if (serviceCreator == null)
         throw new IllegalArgumentException("Null serverCreator");
      
      this.server = server;
      this.serviceController = serviceController;
      this.serviceCreator = serviceCreator;
   }
   
   /**
    * The <code>install</code> method iterates through the mbean tags in the
    * supplied xml configuration and creates and configures the mbeans shown.
    * The mbean configuration can be nested.
    * 
    * @deprecated the service controller no longer uses the service configurator and vice-versa
    * @param config the xml <code>Element</code> containing the configuration of
    * the mbeans to create and configure.
    * @param loaderName the classloader's ObjectName
    * @return a <code>List</code> of ObjectNames of created mbeans.
    * @throws Exception if an error occurs
    */
   public List<ObjectName> install(Element config, ObjectName loaderName) throws Exception
   {
      // Parse the xml
      ServiceMetaDataParser parser = new ServiceMetaDataParser(config);
      List<ServiceMetaData> metaDatas = parser.parse();

      // Track the registered object names
      List<ObjectName> result = new ArrayList<ObjectName>(metaDatas.size());

      // Go through each mbean in the passed xml
      for (ServiceMetaData metaData : metaDatas)
      {
         ObjectName objectName = metaData.getObjectName();
         Collection<ServiceAttributeMetaData> attrs = metaData.getAttributes();
         // Install and configure the mbean
         try
         {
            ServiceCreator.install(server, objectName, metaData, null);
            result.add(objectName);
            configure(server, null, objectName, loaderName, attrs);
         }
         catch (Throwable t)
         {
            // Something went wrong
            for (ObjectName name : result)
            {
               try
               {
                  serviceCreator.remove(name);
               }
               catch (Exception e)
               {
                  log.error("Error removing mbean after failed deployment: " + name, e);
               }
            }
            throw rethrow("Error during install", t);
         }
      }
      return result;
   }

   /**
    * Builds a string that consists of the configuration elements of the
    * currently running MBeans registered in the server.
    * 
    * @todo replace with more sophisticated mbean persistence mechanism.
    * @param server the MBeanServer
    * @param serviceController the service controller
    * @param objectNames the object names to retrieve
    * @return the xml string
    * @throws Exception Failed to construct configuration.
    */
   public static String getConfiguration(MBeanServer server, ServiceController serviceController, ObjectName[] objectNames) throws Exception
   {
      Writer out = new StringWriter();

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.newDocument();

      Element serverElement = doc.createElement("server");

      // Store attributes as XML
      for (int j = 0; j < objectNames.length; j++)
      {
         Element mbeanElement = internalGetConfiguration(doc, server, serviceController, objectNames[j]);
         serverElement.appendChild(mbeanElement);
      }

      doc.appendChild(serverElement);

      // Write configuration
      new DOMWriter(out).setPrettyprint(true).print(doc);

      out.close();

      // Return configuration
      return out.toString();
   }

   private static Element internalGetConfiguration(Document doc, MBeanServer server, ServiceController serviceController, ObjectName name) throws Exception
   {
      Element mbeanElement = doc.createElement("mbean");
      mbeanElement.setAttribute("name", name.toString());

      MBeanInfo info = server.getMBeanInfo(name);
      mbeanElement.setAttribute("code", info.getClassName());
      MBeanAttributeInfo[] attributes = info.getAttributes();
      boolean trace = log.isTraceEnabled();
      for (int i = 0; i < attributes.length; i++)
      {
         if (trace)
            log.trace("considering attribute: " + attributes[i]);
         if (attributes[i].isReadable() && attributes[i].isWritable())
         {
            Element attributeElement = null;
            if (attributes[i].getType().equals("javax.management.ObjectName"))
            {
               attributeElement = doc.createElement("depends");
               attributeElement.setAttribute("optional-attribute-name", attributes[i].getName());
            }
            else
            {
               attributeElement = doc.createElement("attribute");
               attributeElement.setAttribute("name", attributes[i].getName());
            }
            Object value = server.getAttribute(name, attributes[i].getName());

            if (value != null)
            {
               if (value instanceof Element)
               {
                  attributeElement.appendChild(doc.importNode((Element) value, true));
               }
               else
               {
                  attributeElement.appendChild(doc.createTextNode(value.toString()));
               }
            }
            mbeanElement.appendChild(attributeElement);
         }
      }

      ServiceContext sc = serviceController.getServiceContext(name);
      for (ServiceContext needs : sc.iDependOn)
      {
         Element dependsElement = doc.createElement("depends");
         dependsElement.appendChild(doc.createTextNode(needs.objectName.toString()));
         mbeanElement.appendChild(dependsElement);
      }

      return mbeanElement;
   }

   /**
    * Builds a string that consists of the configuration elements of the
    * currently running MBeans registered in the server.
    * 
    * TODO replace with more sophisticated mbean persistence mechanism.
    * @param objectNames the object names
    * @return the xml string
    * @throws Exception Failed to construct configuration.
    */
   public String getConfiguration(ObjectName[] objectNames) throws Exception
   {
      return getConfiguration(server, serviceController, objectNames);
   }

   /**
    * A utility method that transforms the contents of the argument element into
    * a StringBuffer representation that can be reparsed.
    * 
    * [FIXME] This is not a general DOMUtils method because of its funny contract. It does not 
    * support multiple child elements neither can it deal with text content.
    * 
    * @param element - the parent dom element whose contents are to be extracted as an xml document string. 
    * @return the xml document string.
    * @throws IOException for an error during IO
    * @throws TransformerException for an erro during transformation
    */
   public static StringBuffer getElementContent(Element element) throws IOException, TransformerException
   {
      NodeList children = element.getChildNodes();
      Element content = null;
      for (int n = 0; n < children.getLength(); n++)
      {
         Node node = children.item(n);
         if (node.getNodeType() == Node.ELEMENT_NODE)
         {
            content = (Element)node;
            break;
         }
      }
      if (content == null)
         return null;

      // Get a parsable representation of this elements content
      DOMSource source = new DOMSource(content);
      TransformerFactory tFactory = TransformerFactory.newInstance();
      Transformer transformer = tFactory.newTransformer();
      StringWriter sw = new StringWriter();
      StreamResult result = new StreamResult(sw);
      transformer.transform(source, result);
      sw.close();
      return sw.getBuffer();
   }
}