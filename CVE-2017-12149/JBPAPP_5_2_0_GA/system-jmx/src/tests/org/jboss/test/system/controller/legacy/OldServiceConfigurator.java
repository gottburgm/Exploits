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
package org.jboss.test.system.controller.legacy;

import java.beans.PropertyEditor;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.common.beans.property.BeanUtils;
import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ConfigurationException;
import org.jboss.system.ServiceContext;
import org.jboss.util.Classes;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.util.xml.DOMWriter;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SingletonSchemaResolverFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Service configuration helper.
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:hiram@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version <tt>$Revision: 113110 $</tt>
 */
public class OldServiceConfigurator
{

   // Private Data --------------------------------------------------
   
   /** The MBean server which this service is registered in. */
   private final MBeanServer server;
   
   /** The parent service controller */
   private final OldServiceController serviceController;
   
   /** The ServiceCreator */
   private final OldServiceCreator serviceCreator;
   

   /**
    * The instance logger.
    */
   private final Logger log = Logger.getLogger(getClass());

   // Constructor ---------------------------------------------------
   
   /**
    * Constructor
    * 
    * @param server
    * @param serviceController
    * @param serviceCreator
    */
   public OldServiceConfigurator(MBeanServer server,
         OldServiceController serviceController, OldServiceCreator serviceCreator)
   {
      this.server = server;
      this.serviceController = serviceController;
      this.serviceCreator = serviceCreator;
   }

   // Public --------------------------------------------------------
   
   /**
    * The <code>install</code> method iterates through the mbean tags in the
    * supplied xml configuration and creates and configures the mbeans shown.
    * The mbean configuration can be nested.
    * @param config the xml <code>Element</code> containing the configuration of
    * the mbeans to create and configure.
    * @param loaderName the class loader
    * @return a <code>List</code> of ObjectNames of created mbeans.
    * @throws DeploymentException if an error occurs
    */
   public List<ObjectName> install(Element config, ObjectName loaderName) throws DeploymentException
   {
      List<ObjectName> mbeans = new ArrayList<ObjectName>();
      try
      {
         if (config.getTagName().equals("mbean"))
         {
            internalInstall(config, mbeans, loaderName, true);
         }
         else
         {
            NodeList nl = config.getChildNodes();

            for (int i = 0; i < nl.getLength(); i++)
            {
               if (nl.item(i).getNodeType() == Node.ELEMENT_NODE)
               {
                  Element element = (Element) nl.item(i);
                  if (element.getTagName().equals("mbean"))
                  {
                     Element mbean = (Element) nl.item(i);
                     internalInstall(mbean, mbeans, loaderName, true);
                  } // end of if ()
               } // end of if ()
            }//end of for
         } //end of else
         return mbeans;
      }
      catch (Exception e)
      {
         for (ListIterator li = mbeans.listIterator(mbeans.size()); li.hasPrevious();)
         {
            ObjectName mbean = (ObjectName) li.previous();
            try
            {
               serviceCreator.remove(mbean);
            }
            catch (Exception n)
            {
               log.error("exception removing mbean after failed deployment: " + mbean, n);
            }
         }

         if (e instanceof DeploymentException)
            throw (DeploymentException) e;

         throw new DeploymentException(e);
      }
   }

   /**
    * Builds a string that consists of the configuration elements of the
    * currently running MBeans registered in the server.
    * 
    * @param objectNames the object names
    * @return the xml string
    * @throws Exception Failed to construct configuration.
    * @todo replace with more sophisticated mbean persistence mechanism.
    */
   public String getConfiguration(ObjectName[] objectNames)
      throws Exception
   {
      Writer out = new StringWriter();

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.newDocument();

      Element serverElement = doc.createElement("server");

      // Store attributes as XML
      for (int j = 0; j < objectNames.length; j++)
      {
         Element mbeanElement = internalGetConfiguration(doc, objectNames[j]);
         serverElement.appendChild(mbeanElement);
      }

      doc.appendChild(serverElement);

      // Write configuration
      new DOMWriter(out).setPrettyprint(true).print(doc);

      out.close();

      // Return configuration
      return out.toString();
   }
   
   // Protected -----------------------------------------------------
   
   /**
    * The <code>configure</code> method configures an mbean based on the xml
    * element configuration passed in.  Three formats are supported:
    * &lt;attribute name="(name)"&gt;(value)&lt;/attribute&gt; &lt;depends
    * optional-attribute-name="(name)"&gt;(object name of mbean
    * referenced)&lt;/depends&gt; &lt;depends-list optional-attribute-name="(name)"&gt;
    * [list of]  &lt;/depends-list-element&gt;(object name)&lt;/depends-list-element&gt;
    * &lt;/depends-list&gt;
    *
    * The last two can include nested mbean configurations or ObjectNames.
    * SIDE-EFFECT: adds all mbeans this one depends on to the ServiceContext
    * structures.
    * 
    * @param objectName the object name
    * @param loaderName the classloader name
    * @param mbeanElement an <code>Element</code> value
    * @param mbeans the current list of mbeans
    * @throws Exception if an error occurs
    */
   protected void configure(ObjectName objectName, ObjectName loaderName,
      Element mbeanElement, List<ObjectName> mbeans)
      throws Exception
   {
      // Set configuration to MBeans from XML

      MBeanInfo info;
      try
      {
         info = server.getMBeanInfo(objectName);
      }
      catch (InstanceNotFoundException e)
      {
         // The MBean is no longer available
         throw new DeploymentException("trying to configure nonexistent mbean: " + objectName);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not get mbeanInfo", JMXExceptionDecoder.decode(e));
      } // end of catch

      if (info == null)
      {
         throw new DeploymentException("MBeanInfo is null for mbean: " + objectName);
      } // end of if ()

      // Get the classloader for loading attribute classes.
      ClassLoader cl = server.getClassLoader(loaderName);
      // Initialize the mbean using the configuration supplied defaults
      MBeanAttributeInfo[] attributes = info.getAttributes();
      HashMap<String, MBeanAttributeInfo> attributeMap = new HashMap<String, MBeanAttributeInfo>();
      for (int i = 0; i < attributes.length; i++)
      {
         MBeanAttributeInfo attr = attributes[i];
         attributeMap.put(attr.getName(), attr);
      }

      NodeList attrs = mbeanElement.getChildNodes();
      for (int j = 0; j < attrs.getLength(); j++)
      {
         // skip over non-element nodes
         if (attrs.item(j).getNodeType() != Node.ELEMENT_NODE)
         {
            continue;
         }

         Element element = (Element) attrs.item(j);

         boolean replace = true;

         // Set attributes
         if (element.getTagName().equals("attribute"))
         {
            String attributeName = element.getAttribute("name");
            boolean trim = true;
            String replaceAttr = element.getAttribute("replace");
            if (replaceAttr.length() > 0)
               replace = Boolean.valueOf(replaceAttr).booleanValue();
            String trimAttr = element.getAttribute("trim");
            if (trimAttr.length() > 0)
               trim = Boolean.valueOf(trimAttr).booleanValue();
            String serialDataType = element.getAttribute("serialDataType");

            // Get the MBeanAttributeInfo
            MBeanAttributeInfo attr = attributeMap.get(attributeName);
            if (attr == null)
               throw new DeploymentException("No Attribute found with name: " + attributeName);

            if (element.hasChildNodes())
            {
               Object value = null;
               // Unmarshall the attribute value based on the serialDataType
               if (serialDataType.equals("javaBean"))
                  value = parseJavaBeanSerialData(attr, cl, element, replace, trim);
               else if (serialDataType.equals("jbxb"))
                  value = parseJbxbSerialData(attr, cl, element, replace, trim);
               else
                  value = parseTextSerialData(attr, cl, element, replace, trim);
               
               log.debug(attributeName + " set to " + value + " in " + objectName);
               setAttribute(objectName, new Attribute(attributeName, value));
            }//if has children

         }
         //end of "attribute
         else if (element.getTagName().equals("depends"))
         {
            if (!element.hasChildNodes())
            {
               throw new DeploymentException("No ObjectName supplied for depends in  " + objectName);
            }

            String mbeanRefName = element.getAttribute("optional-attribute-name");
            if ("".equals(mbeanRefName))
               mbeanRefName = null;
            else
               mbeanRefName = StringPropertyReplacer.replaceProperties(mbeanRefName);

            String proxyType = element.getAttribute("proxy-type");
            if ("".equals(proxyType))
               proxyType = null;
            else
               proxyType = StringPropertyReplacer.replaceProperties(proxyType);

            // Get the mbeanRef value
            ObjectName dependsObjectName = processDependency(objectName, loaderName, element, mbeans, replace);
            log.debug("considering " + ((mbeanRefName == null) ? "<anonymous>" : mbeanRefName.toString()) + " with object name " + dependsObjectName);

            if (mbeanRefName != null)
            {
               Object attribute = dependsObjectName;
               if (proxyType != null)
               {
                  if (mbeanRefName == null)
                     throw new DeploymentException("You cannot use a proxy-type without an optional-attribute-name");
                  if (proxyType.equals("attribute"))
                  {
                     MBeanAttributeInfo attr = attributeMap.get(mbeanRefName);
                     if (attr == null)
                        throw new DeploymentException("No Attribute found with name: " + mbeanRefName);
                     proxyType = attr.getType();
                  }
                  Class proxyClass = cl.loadClass(proxyType);
                  attribute = MBeanProxyExt.create(proxyClass, dependsObjectName,
                     server, true);
               }

               //if if doesn't exist or has wrong type, we'll get an exception
               setAttribute(objectName, new Attribute(mbeanRefName, attribute));
            } // end of if ()
         }
         //end of depends
         else if (element.getTagName().equals("depends-list"))
         {
            String dependsListName = element.getAttribute("optional-attribute-name");
            if ("".equals(dependsListName))
            {
               dependsListName = null;
            } // end of if ()

            NodeList dependsList = element.getChildNodes();
            ArrayList<ObjectName> dependsListNames = new ArrayList<ObjectName>();
            for (int l = 0; l < dependsList.getLength(); l++)
            {
               if (dependsList.item(l).getNodeType() != Node.ELEMENT_NODE)
               {
                  continue;
               }

               Element dependsElement = (Element) dependsList.item(l);
               if (dependsElement.getTagName().equals("depends-list-element"))
               {
                  if (!dependsElement.hasChildNodes())
                  {
                     throw new DeploymentException("Empty depends-list-element!");
                  } // end of if ()

                  // Get the depends value
                  ObjectName dependsObjectName = processDependency(objectName, loaderName, dependsElement, mbeans, replace);
                  if (!dependsListNames.contains(dependsObjectName))
                  {
                     dependsListNames.add(dependsObjectName);
                  } // end of if ()
               }

            } // end of for ()
            if (dependsListName != null)
            {
               setAttribute(objectName, new Attribute(dependsListName, dependsListNames));
            } // end of if ()
         }//end of depends-list
      }
   }

   // Private -------------------------------------------------------
   
   private ObjectName internalInstall(Element mbeanElement, List<ObjectName> mbeans,
         ObjectName loaderName, boolean replace) throws Exception
      {
         ObjectInstance instance = null;
         ObjectName mbeanName = parseObjectName(mbeanElement, replace);

         instance = serviceCreator.install(mbeanName, loaderName, mbeanElement);

         // just in case it changed...
         mbeanName = instance.getObjectName();

         mbeans.add(mbeanName);
         if (mbeanName != null)
         {
            ServiceContext ctx = serviceController.createServiceContext(mbeanName);
            try
            {
               configure(mbeanName, loaderName, mbeanElement, mbeans);
               ctx.state = ServiceContext.CONFIGURED;
               ctx.problem = null;
            }
            catch (Throwable e) // TODO Backport to JBoss4!!!!
            {
               ctx.state = ServiceContext.FAILED;
               ctx.problem = e;
               log.info("Problem configuring service " + mbeanName, e);
               //throw e;
            }
         }

         return mbeanName;
      }
   
   /**
    * Configure the mbean attribute using the element text and PropertyEditor for the
    * attribute type. 
    * @param attr - the mbean attribute
    * @param cl - the class loader to use
    * @param element - the mbean attribute element from the jboss-service descriptor
    * @param replace - flag indicating if ${x} system property refs should be replaced
    * @param trim - flag indicating if the element text shold be trimmed 
    * @return the configured attribute value
    * @throws Exception
    */ 
   private Object parseTextSerialData(MBeanAttributeInfo attr, ClassLoader cl,
      Element element, boolean replace, boolean trim)
      throws Exception
   {
      // Get the attribute value
      String attributeName = attr.getName();
      String attributeText = getElementContent(element, trim, replace);
      String typeName = attr.getType();

      // see if it is a primitive type first
      Class typeClass = Classes.getPrimitiveTypeForName(typeName);
      if (typeClass == null)
      {
         // nope try look up
         try
         {
            typeClass = cl.loadClass(typeName);
         }
         catch (ClassNotFoundException e)
         {
            throw new DeploymentException
               ("Class not found for attribute: " + attributeName, e);
         }
      }

      Object value = null;

      /* Attributes of type Element are passed as is after optionally
      performing system property replacement
      */
      if (typeClass.equals(Element.class))
      {
         // Use the first child Element of this element as the value
         NodeList nl = element.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++)
         {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE)
            {
               value = n;
               break;
            }
         }
         // Replace any ${x} references in the element text
         if (replace)
         {
            PropertyEditor editor = PropertyEditorFinder.getInstance().find(typeClass);
            if (editor == null)
            {
               log.warn("Cannot perform property replace on Element");
            }
            else
            {
               editor.setValue(value);
               String text = editor.getAsText();
               text = StringPropertyReplacer.replaceProperties(text);
               editor.setAsText(text);
               value = editor.getValue();
            }
         }
      }

      if (value == null)
      {
         PropertyEditor editor = PropertyEditorFinder.getInstance().find(typeClass);
         if (editor == null)
         {
            throw new DeploymentException
               ("No property editor for attribute: " + attributeName +
               "; type=" + typeClass);
         }

         // JBAS-1709, temporarily switch the TCL so that property
         // editors have access to the actual deployment ClassLoader.
         ClassLoader tcl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(cl);
         try 
         {
            editor.setAsText(attributeText);
            value = editor.getValue();
         }
         finally 
         {
            Thread.currentThread().setContextClassLoader(tcl);
         }
      }
      return value;
   }

   /**
    * Configure the mbean attribute as a javabean with the bean properties given by
    * nested property elements.
    * @param attr - the mbean attribute
    * @param cl - the class loader to use
    * @param element - the mbean attribute element from the jboss-service descriptor
    * @param replace - flag indicating if ${x} system property refs should be replaced
    * @param trim - flag indicating if the element text shold be trimmed 
    * @return the configured attribute java bean
    * @throws Exception
    */ 
   private Object parseJavaBeanSerialData(MBeanAttributeInfo attr, ClassLoader cl,
      Element element, boolean replace, boolean trim)
      throws Exception
   {
      // Extract the property elements
      String attributeClassName = element.getAttribute("attributeClass");
      if( attributeClassName == null || attributeClassName.length() == 0 )
         attributeClassName = attr.getType();
      Class attributeClass = cl.loadClass(attributeClassName);
      // Create the bean instance
      Object bean = attributeClass.newInstance();
      // Get the JavaBean properties
      NodeList properties = element.getElementsByTagName("property");
      Properties beanProps = new Properties();
      for(int n = 0; n < properties.getLength(); n ++)
      {
         // Skip over non-element nodes
         Node node = properties.item(n);
         if (node.getNodeType() != Node.ELEMENT_NODE)
         {
            continue;
         }
         Element property = (Element) node;
         String name = property.getAttribute("name");
         String value = getElementContent(property, trim, replace);
         beanProps.setProperty(name, value);
      }

      // Apply the properties to the bean
      BeanUtils.mapJavaBeanProperties(bean, beanProps);
      return bean;
   }

   /**
    * Configure the mbean attribute as a bean with the bean unmarshalled from
    * the attribute xml contents using the JBossXB unmarshaller.
    * 
    * @param attr - the mbean attribute
    * @param cl - the class loader to use
    * @param element - the mbean attribute element from the jboss-service descriptor
    * @param replace - ignored
    * @param trim - ignored 
    * @return the configured attribute bean
    * @throws Exception
    */ 
   private Object parseJbxbSerialData(MBeanAttributeInfo attr, ClassLoader cl,
      Element element, boolean replace, boolean trim)
      throws Exception
   {
      // Get the attribute element content in a parsable form
      StringBuffer buffer = getElementContent(element);

      // Parse the attribute element content
      SchemaBindingResolver resolver = SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      StringReader reader = new StringReader(buffer.toString());
      Object bean = unmarshaller.unmarshal(reader, resolver);
      return bean;
   }

   private ObjectName processDependency(ObjectName container, ObjectName loaderName,
      Element element, List<ObjectName> mbeans, boolean replace)
      throws Exception
   {
      ObjectName dependsObjectName = null;
      NodeList nl = element.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
      {
         Node childNode = nl.item(i);
         if (childNode.getNodeType() == Node.ELEMENT_NODE)
         {
            Element child = (Element) childNode;
            if (child.getTagName().equals("mbean"))
            {
               dependsObjectName = internalInstall(child, mbeans, loaderName, replace);
               break;
            }
            else
            {
               throw new DeploymentException("Non mbean child element in depends tag: " + child);
            } // end of else
         } // end of if ()
      } // end of for ()

      if (dependsObjectName == null)
      {
         String name = getElementContent(element, true, replace);
         dependsObjectName = ObjectNameFactory.create(name);
      }
      if (dependsObjectName == null)
      {
         throw new DeploymentException("No object name found for attribute!");
      } // end of if ()

      serviceController.registerDependency(container, dependsObjectName);

      return dependsObjectName;
   }

   /**
    * A helper to deal with those pesky JMX exceptions.
    */
   private void setAttribute(ObjectName name, Attribute attr)
      throws Exception
   {
      try
      {
         server.setAttribute(name, attr);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Exception setting attribute " +
            attr + " on mbean " + name, JMXExceptionDecoder.decode(e));
      }
   }

   private Element internalGetConfiguration(Document doc, ObjectName name)
      throws Exception
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
      for (Iterator i = sc.iDependOn.iterator(); i.hasNext();)
      {
         ServiceContext needs = (ServiceContext) i.next();
         Element dependsElement = doc.createElement("depends");
         dependsElement.appendChild(doc.createTextNode(needs.objectName.toString()));
         mbeanElement.appendChild(dependsElement);
      }

      return mbeanElement;
   }

   /**
    * Parse an object name from the given element attribute 'name'.
    * @param element Element to parse name from.
    * @return Object name.
    * @throws ConfigurationException Missing attribute 'name' (thrown if 'name'
    * is null or "").
    * @throws Exception
    */
   private ObjectName parseObjectName(final Element element, boolean replace)
      throws Exception
   {
      String name = element.getAttribute("name");

      if (name == null || name.trim().equals(""))
      {
         throw new DeploymentException("MBean attribute 'name' must be given.");
      }

      if (replace)
         name = StringPropertyReplacer.replaceProperties(name);

      return new ObjectName(name);
   }

   private String getElementContent(Element element, boolean trim, boolean replace)
      throws Exception
   {
      NodeList nl = element.getChildNodes();
      String attributeText = "";
      for (int i = 0; i < nl.getLength(); i++)
      {
         Node n = nl.item(i);
         if (n instanceof Text)
         {
            attributeText += ((Text) n).getData();
         }
      } // end of for ()
      if (trim)
         attributeText = attributeText.trim();
      if (replace)
         attributeText = StringPropertyReplacer.replaceProperties(attributeText);
      return attributeText;
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
    * @throws IOException for an IO problem
    * @throws TransformerException for an error during the transformation
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