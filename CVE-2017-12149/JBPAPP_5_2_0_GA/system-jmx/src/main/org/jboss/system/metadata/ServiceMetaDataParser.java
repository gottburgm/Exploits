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
package org.jboss.system.metadata;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

import org.jboss.dependency.spi.ControllerMode;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.logging.Logger;
import org.jboss.util.StringPropertyReplacer;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * ServiceMetaDataParser
 *
 * This class is based on the old ServiceConfigurator/Creator.
 *
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:hiram@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 110332 $
 */
public class ServiceMetaDataParser
{
   /** The log */
   private static final Logger log = Logger.getLogger(ServiceMetaDataParser.class);
   
   /** The element config */
   private Element config;

   /** The mode */
   private ControllerMode serverMode;
   
   /**
    * Create a new service meta data parser
    * 
    * @param config the xml config
    */
   public ServiceMetaDataParser(Element config)
   {
      if (config == null)
         throw new IllegalArgumentException("Null config");
      
      this.config = config;
   }

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
    * Parse the xml 
    * 
    * @return the list of service meta data
    * @throws Exception for any error
    */
   public List<ServiceMetaData> parse() throws Exception
   {
      List<ServiceMetaData> services = new ArrayList<ServiceMetaData>();
      
      try
      {
         String tagName = config.getTagName();
         if ("mbean".equals(tagName))
            internalParse(services, config, true);
         else
         {
            if ("server".equals(tagName))
               parseServer(config);

            NodeList nl = config.getChildNodes();

            for (int i = 0; i < nl.getLength(); ++i)
            {
               if (nl.item(i).getNodeType() == Node.ELEMENT_NODE)
               {
                  Element element = (Element) nl.item(i);
                  if ("mbean".equals(element.getTagName()))
                  {
                     Element mbean = (Element) nl.item(i);
                     internalParse(services, mbean, true);
                  }
               }
            }
         }
      }
      catch (Throwable t)
      {
         throw rethrow("Unable to parse service configuration", t);
      }
      
      return services;
   }

   /**
    * Parse the server element
    * 
    * @param serverElement the server element
    * @throws Exception for any error
    */
   private void parseServer(Element serverElement) throws Exception
   {
      String modeString = serverElement.getAttribute("mode");
      if (modeString != null)
      {
         modeString = modeString.trim();
         if (modeString.length() != 0)
            serverMode = ControllerMode.getInstance(modeString);
      }
   }

   /**
    * Internal parse
    * 
    * @param services the list of service meta data
    * @param mbeanElement the mbean configuration
    * @param replace whether to replace system properties
    * @return the ObjectName of the parsed mbean
    * @throws Exception for any error
    */
   private ObjectName internalParse(List<ServiceMetaData> services, Element mbeanElement, boolean replace) throws Exception
   {
      ServiceMetaData service = new ServiceMetaData();

      ObjectName mbeanName = parseObjectName(mbeanElement, replace);
      service.setObjectName(mbeanName);
      
      String code = parseCode(mbeanName, mbeanElement);
      service.setCode(code);
      
      ControllerMode mode = parseMode(mbeanName, mbeanElement);
      if (mode == null)
         mode = serverMode;
      service.setMode(mode);

      ServiceConstructorMetaData constructor = parseConstructor(mbeanName, mbeanElement, replace);
      service.setConstructor(constructor);
      
      String interfaceName = parseInterface(mbeanName, mbeanElement);
      service.setInterfaceName(interfaceName);
      
      String xmbeandd = parseXMBeanDD(mbeanName, mbeanElement);
      service.setXMBeanDD(xmbeandd);

      String xmbeanCode = parseXMBeanCode(mbeanName, mbeanElement);
      service.setXMBeanCode(xmbeanCode);
      
      if (xmbeandd != null && xmbeandd.length() == 0)
      {
         Element xmbeanDescriptor = parseXMBeanDescriptor(mbeanName, mbeanElement);
         service.setXMBeanDescriptor(xmbeanDescriptor);
      }
      List<ServiceAnnotationMetaData> annotations = new ArrayList<ServiceAnnotationMetaData>();
      List<ServiceAttributeMetaData> attributes = new ArrayList<ServiceAttributeMetaData>();
      List<ServiceDependencyMetaData> dependencies = new ArrayList<ServiceDependencyMetaData>();

      NodeList attrs = mbeanElement.getChildNodes();
      for (int j = 0; j < attrs.getLength(); j++)
      {
         // skip over non-element nodes
         if (attrs.item(j).getNodeType() != Node.ELEMENT_NODE)
            continue;

         Element element = (Element) attrs.item(j);

         boolean replaceAttribute = true;

         // Set attributes
         if (element.getTagName().equals("attribute"))
         {
            String attributeName = element.getAttribute("name");
            if (attributeName == null)
               throw new RuntimeException("No attribute name for " + mbeanName);
            boolean trim = true;
            String replaceAttr = element.getAttribute("replace");
            if (replaceAttr.length() > 0)
               replaceAttribute = Boolean.valueOf(replaceAttr).booleanValue();
            String trimAttr = element.getAttribute("trim");
            if (trimAttr.length() > 0)
               trim = Boolean.valueOf(trimAttr).booleanValue();
            String serialDataType = element.getAttribute("serialDataType");

            if (element.hasChildNodes())
            {
               // Unmarshall the attribute value based on the serialDataType
               ServiceValueMetaData value = null;
               if (serialDataType.equals("javaBean"))
               {
                  value = new ServiceJavaBeanValueMetaData(element);
               }
               else if (serialDataType.equals("jbxb"))
               {
                  value = new ServiceJBXBValueMetaData(element);
               }
               else
               {
                  NodeList nl = element.getChildNodes();
                  for (int i = 0; i < nl.getLength(); i++)
                  {
                     Node n = nl.item(i);
                     if (n.getNodeType() == Node.ELEMENT_NODE)
                     {
                        Element el = (Element) n;
                        String tagName = el.getTagName();
                        if ("inject".equals(tagName))
                        {
                           value = parseInject(el);
                        }
                        else if ("value-factory".equals(tagName))
                        {
                           value = parseValueFactory(el);
                        }
                        else
                        {
                           value = new ServiceElementValueMetaData((Element) n);
                        }
                        break;
                     }
                  }
                  if (value == null)
                     value = new ServiceTextValueMetaData(getElementTextContent(element, trim, replaceAttribute));
               }
               
               ServiceAttributeMetaData attribute = new ServiceAttributeMetaData();
               attribute.setName(attributeName);
               attribute.setReplace(replaceAttribute);
               attribute.setTrim(trim);
               attribute.setValue(value);
               attributes.add(attribute);
            }
         }
         else if (element.getTagName().equals("depends"))
         {
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
            String dependsObjectName = processDependency(mbeanName, mbeanRefName, element, services, replace);

            if (mbeanRefName != null)
            {
               ServiceValueMetaData value = new ServiceDependencyValueMetaData(dependsObjectName, proxyType);
               ServiceAttributeMetaData attribute = new ServiceAttributeMetaData();
               attribute.setName(mbeanRefName);
               attribute.setValue(value);
               attributes.add(attribute);
            }
            else
            {
               ServiceDependencyMetaData dependency = new ServiceDependencyMetaData();
               dependency.setIDependOn(dependsObjectName);
               dependencies.add(dependency);
            }
         }
         else if (element.getTagName().equals("depends-list"))
         {
            String dependsListName = element.getAttribute("optional-attribute-name");
            if ("".equals(dependsListName))
               dependsListName = null;

            NodeList dependsList = element.getChildNodes();
            ArrayList<String> dependsListNames = new ArrayList<String>();
            for (int l = 0; l < dependsList.getLength(); ++l)
            {
               if (dependsList.item(l).getNodeType() != Node.ELEMENT_NODE)
                  continue;

               Element dependsElement = (Element) dependsList.item(l);
               if (dependsElement.getTagName().equals("depends-list-element"))
               {
                  // Get the depends value
                  String dependsObjectName = processDependency(mbeanName, dependsListName, dependsElement, services, replace);
                  if (dependsListNames.contains(dependsObjectName) == false)
                     dependsListNames.add(dependsObjectName);

                  if (dependsListName == null)
                  {
                     ServiceDependencyMetaData dependency = new ServiceDependencyMetaData();
                     dependency.setIDependOn(dependsObjectName);
                     dependencies.add(dependency);
                  }
               }
            }
            if (dependsListName != null)
            {
               ServiceValueMetaData value = new ServiceDependencyListValueMetaData(dependsListNames);
               ServiceAttributeMetaData attribute = new ServiceAttributeMetaData();
               attribute.setName(dependsListName);
               attribute.setValue(value);
               attributes.add(attribute);
            }
         }
         else if (element.getTagName().equals("alias"))
         {
            List<String> aliases = service.getAliases();
            if (aliases == null)
            {
               aliases = new ArrayList<String>();
               service.setAliases(aliases);
            }
            aliases.add(getElementTextContent(element, true, true));
         }
         else if (element.getTagName().equals("annotation"))
         {
            String ann = getElementTextContent(element, true, true);
            ServiceAnnotationMetaData amd = new ServiceAnnotationMetaData(ann);
            annotations.add(amd);
         }
      }
      
      service.setAttributes(attributes);
      service.setDependencies(dependencies);
      service.setAnnotations(annotations);
      
      services.add(service);
      
      return mbeanName;
   }

   /**
    * Parse an object name from the given element attribute 'name'.
    * 
    * @param mbeanElement the element to parse name from.
    * @return the ObjectName
    * @throws Exception for any error
    */
   private ObjectName parseObjectName(final Element mbeanElement, boolean replace) throws Exception
   {
      String name = mbeanElement.getAttribute("name");

      if (name == null || name.trim().length() == 0)
         throw new RuntimeException("Missing or empty 'name' attribute for mbean.");

      if (replace)
         name = StringPropertyReplacer.replaceProperties(name);

      return new ObjectName(name);
   }

   /**
    * Parse a class name from the given element attribute 'code'.
    * 
    * @param name the mbean name
    * @param mbeanElement the element to parse name from.
    * @return the class name
    * @throws Exception for any error
    */
   private String parseCode(final ObjectName name, final Element mbeanElement) throws Exception
   {
      return mbeanElement.getAttribute("code");
   }

   /**
    * Parse the mode
    * 
    * @param name the mbean name
    * @param mbeanElement the element to parse name from.
    * @return the mode
    * @throws Exception for any error
    */
   private ControllerMode parseMode(final ObjectName name, final Element mbeanElement) throws Exception
   {
      String modeString = mbeanElement.getAttribute("mode");
      if (modeString == null)
         return null;
      modeString = modeString.trim();
      if (modeString.length() == 0)
         return null;
      return ControllerMode.getInstance(modeString);
   }

   /**
    * Parse the constructor element of the given element
    * 
    * @param name the mbean name
    * @param mbeanElement the element to parse name from.
    * @param replace whether to replace system properties
    * @return the constructor meta data
    * @throws Exception for any error
    */
   private ServiceConstructorMetaData parseConstructor(final ObjectName name, final Element mbeanElement, boolean replace) throws Exception
   {
      ServiceConstructorMetaData result = new ServiceConstructorMetaData();

      NodeList list = mbeanElement.getElementsByTagName("constructor");
      if (list.getLength() > 1 && list.item(0).getParentNode() == mbeanElement)
         throw new RuntimeException("only one <constructor> element may be defined for " + name);
      
      if (list.getLength() == 1)
      {
         Element element = (Element) list.item(0);

         // get all of the "arg" elements
         list = element.getElementsByTagName("arg");
         int length = list.getLength();
         String[] params = new String[length];
         String[] signature = new String[length];

         // decode the values into params & signature
         for (int j=0; j<length; ++j)
         {
            Element arg = (Element)list.item(j);
            String typeName = null;
            Attr attr = arg.getAttributeNode("type");
            if (attr != null)
               typeName = attr.getValue();
            String value = null;
            attr = arg.getAttributeNode("value");
            if (attr != null)
               value = attr.getValue();
            signature[j] = typeName;
            params[j] = value;
         }
         
         result.setParams(params);
         result.setSignature(signature);
      }

      return result;
   }

   /**
    * Parse the interface name from the given element attribute 'interface'.
    * 
    * @param name the mbean name
    * @param mbeanElement the element to parse name from.
    * @return the class name
    * @throws Exception for any error
    */
   private String parseInterface(final ObjectName name, final Element mbeanElement) throws Exception
   {
      Attr attr = mbeanElement.getAttributeNode("interface");
      if (attr != null)
         return attr.getValue();
      else
         return null;
   }

   /**
    * Parse the xmbean dds from the given element attribute 'xmbean-dd'.
    * 
    * @param name the mbean name
    * @param mbeanElement the element to parse name from.
    * @return the xmbean dds location
    * @throws Exception for any error
    */
   private String parseXMBeanDD(final ObjectName name, final Element mbeanElement) throws Exception
   {
      Attr attr = mbeanElement.getAttributeNode("xmbean-dd");
      if (attr != null)
         return attr.getValue();
      else
         return null;
   }

   /**
    * Parse the xmbean code from the given element attribute 'xmbean-code'.
    * 
    * @param name the mbean name
    * @param mbeanElement the element to parse name from.
    * @return the xmbean code
    * @throws Exception for any error
    */
   private String parseXMBeanCode(final ObjectName name, final Element mbeanElement) throws Exception
   {
      Attr attr = mbeanElement.getAttributeNode("xmbean-code");
      if (attr != null)
         return attr.getValue();
      else
         return ServiceMetaData.XMBEAN_CODE;
   }

   /**
    * Parse the xmbean descriptor.
    * 
    * @param name the mbean name
    * @param mbeanElement the element to parse name from.
    * @return the xmbean descriptor
    * @throws Exception for any error
    */
   private Element parseXMBeanDescriptor(final ObjectName name, final Element mbeanElement) throws Exception
   {
      NodeList mbeans = mbeanElement.getElementsByTagName("xmbean");
      if (mbeans.getLength() == 0)
         throw new RuntimeException("No nested mbean element given for xmbean for " + name);
      return (Element) mbeans.item(0);
   }

   private ServiceValueMetaData parseInject(Element el)
   {
      ServiceValueMetaData value;
      String dependency = el.getAttribute("bean");
      String property = null;
      Attr attr = el.getAttributeNode("property");
      if (attr != null)
         property = attr.getValue();
      ControllerState requiredState = ControllerState.INSTALLED;
      attr = el.getAttributeNode("state");
      if (attr != null)
         requiredState = new ControllerState(attr.getValue());
      value = new ServiceInjectionValueMetaData(dependency, property, requiredState);
      return value;
   }

   private ServiceValueMetaData parseValueFactory(Element el) throws Exception
   {
      ServiceValueMetaData value;
      String dependency = el.getAttribute("bean");
      
      String method = el.getAttribute("method");
      
      ControllerState requiredState = ControllerState.INSTALLED;
      Attr attr = el.getAttributeNode("state");
      if (attr != null)
         requiredState = new ControllerState(attr.getValue());
      
      List<ServiceValueFactoryParameterMetaData> parameters = new ArrayList<ServiceValueFactoryParameterMetaData>();
      attr = el.getAttributeNode("parameter");
      if (attr != null)
      {
         parameters.add(new ServiceValueFactoryParameterMetaData(attr.getValue()));
      }
      else
      {
         NodeList children = el.getChildNodes();
         for (int j = 0; j < children.getLength(); j++)
         {
            // skip over non-element nodes
            if (children.item(j).getNodeType() != Node.ELEMENT_NODE)
               continue;

            Element child = (Element) children.item(j);
            if ("parameter".equals(child.getTagName()))
            {
               parameters.add(parseValueFactoryParameter(child));
            }
         }
      }
      
      ServiceTextValueMetaData defaultValue = null;
      attr = el.getAttributeNode("default");
      if (attr != null)
      {
         defaultValue = new ServiceTextValueMetaData(attr.getValue());
      }
      value = new ServiceValueFactoryValueMetaData(dependency, method, parameters, requiredState, defaultValue);
      return value;
   }

   private ServiceValueFactoryParameterMetaData parseValueFactoryParameter(Element el) throws Exception
   {
      String parameterType = null;
      Attr attr = el.getAttributeNode("class");
      if (attr != null)
         parameterType = attr.getValue();
      
      String textValue = null;
      String valueType = null;
      
      Node child = el.getFirstChild();
      if (child.getNodeType() == Node.ELEMENT_NODE)
      {
         Element valueEl = (Element) child;
         if ("value".equals(valueEl.getTagName()))
         {            
            valueType = valueEl.getAttribute("class");
            if (valueType.length() == 0)
               valueType = null;
            
            textValue = getElementTextContent(valueEl);
            // Deal with any trim/replace from the outer element
            textValue = trimAndReplace(textValue, getTrim(el), getReplace(el));
         }
         else if ("null".equals(valueEl.getTagName()) == false)
         {
            throw new RuntimeException("Element " + valueEl.getTagName() + " not supported as a child of value-factory/parameter in a -service.xml");
         }
      }
      else
      {
         Node nextChild = child.getNextSibling();
         while (nextChild != null && nextChild.getNodeType() != Node.ELEMENT_NODE)
            nextChild = nextChild.getNextSibling();

         // we have nested element in whitespace
         if (nextChild instanceof Element)
         {
            Element valueEl = (Element) nextChild;
            if ("value".equals(valueEl.getTagName()))
            {
               valueType = valueEl.getAttribute("class");
               if (valueType.length() == 0)
                  valueType = null;

               textValue = getElementTextContent(valueEl);
               // Deal with any trim/replace from the outer element
               textValue = trimAndReplace(textValue, getTrim(el), getReplace(el));
            }
            else if ("null".equals(valueEl.getTagName()) == false)
            {
               throw new RuntimeException("Element " + valueEl.getTagName() + " not supported as a child of value-factory/parameter in a -service.xml");
            }
         }
         else
         {
            textValue = getElementTextContent(el);
         }
      }
      
      return new ServiceValueFactoryParameterMetaData(textValue, parameterType, valueType);
   }

   /**
    * Process a dependency
    * 
    * @param mbeanName the surronding mbean
    * @param attributeName the attribute name
    * @param element the element
    * @param services the list of services
    * @param replace whether to replace properties
    * @return the dependent object name
    * @throws Exception for any error
    */
   private String processDependency(ObjectName mbeanName, String attributeName, Element element, List<ServiceMetaData> services, boolean replace) throws Exception
   {
      String dependsObjectName = null;
      
      NodeList nl = element.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
      {
         Node childNode = nl.item(i);
         if (childNode.getNodeType() == Node.ELEMENT_NODE)
         {
            Element child = (Element) childNode;
            String tagName = child.getTagName();
            if ("mbean".equals(tagName))
            {
               dependsObjectName = internalParse(services, child, replace).getCanonicalName();
               break;
            }
            else
            {
               if (attributeName != null)
                  log.warn("Non mbean child <" + tagName + "/> in depends tag for " + mbeanName + " attribute: " + attributeName);
               else
                  log.warn("Non mbean child <" + tagName + "/> in depends tag for " + mbeanName);
            }
         }
      }

      if (dependsObjectName == null)
         dependsObjectName = getElementTextContent(element, true, replace);

      return dependsObjectName;
   }

   /**
    * Get an element's text content, looking for "trim" and "replace" attributes
    * on the element to determine whether to trim the text and/or perform
    * system property substitution.
    * 
    * @param element the element
    * @return the concatentation of the text nodes
    * @throws Exception for any error
    */
   public static String getElementTextContent(Element element) throws Exception
   {
      boolean replace = getReplace(element);      
      boolean trim = getTrim(element);
      
      String rawText = getRawElementTextContent(element);
      return trimAndReplace(rawText, trim, replace);
   }
   
   public static boolean getTrim(Element element)
   {      
      boolean trim = true;
      String trimAttr = element.getAttribute("trim");
      if (trimAttr.length()  > 0)
         trim = Boolean.valueOf(trimAttr).booleanValue();
      return trim;
   }
   
   public static boolean getReplace(Element element)
   {
      boolean replace = true;
      String replaceAttr = element.getAttribute("replace");
      if (replaceAttr.length() > 0)
         replace = Boolean.valueOf(replaceAttr).booleanValue();
      return replace;
   }

   /**
    * Get an elements text content
    * 
    * @param element the element
    * @param trim whether to trim
    * @param replace whetehr to replace properties
    * @return the concatentation of the text nodes
    * @throws Exception for any error
    */
   public static String getElementTextContent(Element element, boolean trim, boolean replace) throws Exception
   {
      String rawText = getRawElementTextContent(element);
      
      return trimAndReplace(rawText, trim, replace);
   }
   
   public static String getRawElementTextContent(Element element)
   {
      NodeList nl = element.getChildNodes();
      String rawText = "";
      for (int i = 0; i < nl.getLength(); i++)
      {
         Node n = nl.item(i);
         if (n instanceof Text)
         {
            rawText += ((Text) n).getData();
         }
      }
      return rawText;
   }
   
   public static String trimAndReplace(String rawText, boolean trim, boolean replace)
   {
      if (trim)
         rawText = rawText.trim();
      if (replace)
      {
         SecurityManager manager = System.getSecurityManager();
         if (manager == null)
         {
            rawText = StringPropertyReplacer.replaceProperties(rawText);
         }
         else
         {
            final String input = rawText;
            rawText = AccessController.doPrivileged(new PrivilegedAction<String>()
            {
               public String run()
               {
                  return StringPropertyReplacer.replaceProperties(input);
               }
            });
         }
      }
      return rawText;
      
   }   
   
}
