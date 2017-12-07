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
package org.jboss.mx.metadata;

import java.beans.IntrospectionException;
import java.beans.PropertyEditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.Descriptor;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.jboss.common.beans.property.BeanUtils;
import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.logging.Logger;
import org.jboss.mx.modelmbean.XMBeanConstants;
import org.jboss.mx.util.JBossNotCompliantMBeanException;
import org.jboss.util.Classes;
import org.jboss.util.StringPropertyReplacer;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** 
 * The JBoss 1.0 model mbean descriptor parser class.
 * 
 * @author Matt Munz
 * @author Scott.Stark@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 113110 $
 */
public class JBossXMBean10 extends AbstractBuilder
   implements XMBeanConstants
{
   private static Logger log = Logger.getLogger(JBossXMBean10.class);

   // Attributes ----------------------------------------------------
   
   private Element element;

   /**
    * The class name of the Model MBean implementation class.
    */
   private String mmbClassName      = null;
   
   /**
    * The class name of the resource object represented by this Model MBean.
    */
   private String resourceClassName = null; 

  
   // Constructors --------------------------------------------------

   public JBossXMBean10(String mmbClassName, String resourceClassName, Element element, Map properties) 
   {
      super();
      this.mmbClassName = mmbClassName;
      this.resourceClassName = resourceClassName;
      this.element = element;
      setProperties(properties);
   }

   // MetaDataBuilder implementation --------------------------------

   public MBeanInfo build() throws NotCompliantMBeanException
   {
      try
      {
         if (element == null)
         {
            throw new JBossNotCompliantMBeanException("No xml configuration supplied!");
         }
         String description = element.elementTextTrim("description");

         if (resourceClassName == null) 
         {
            resourceClassName = element.elementTextTrim("class");
         }
         
         List constructors = element.elements("constructor");
         List operations = element.elements("operation");
         List attributes = element.elements("attribute");
         List notifications = element.elements("notification");

         Descriptor descr = getDescriptor(element, mmbClassName, MBEAN_DESCRIPTOR);

         ModelMBeanInfo info = buildMBeanMetaData(
            description, constructors, operations,
            attributes, notifications, descr
         );

         return (MBeanInfo) info;
      }
      catch (Throwable t)
      {
         throw new JBossNotCompliantMBeanException("Error parsing the XML file: ", t);
      }
   }

   
   // Protected -----------------------------------------------------
   
   protected Descriptor getDescriptor(final Element parent, final String infoName, final String type)
           throws NotCompliantMBeanException
   {
      Descriptor descr = new DescriptorSupport();
      descr.setField(NAME, infoName);
      descr.setField(DISPLAY_NAME, infoName);
      descr.setField(DESCRIPTOR_TYPE, type);

      Element descriptors = parent.element("descriptors");
      if (descriptors == null) 
      {
         return descr;
      }

      for (Iterator i = descriptors.elementIterator(); i.hasNext();)
      {
         Element descriptor = (Element)i.next();
         String name = descriptor.getName();
         if (name.equals("persistence")) 
         {
            String persistPolicy = descriptor.attributeValue(PERSIST_POLICY);
            String persistPeriod = descriptor.attributeValue(PERSIST_PERIOD);
            String persistLocation = descriptor.attributeValue(PERSIST_LOCATION);
            String persistName = descriptor.attributeValue(PERSIST_NAME);
            if (persistPolicy != null)
            {
               validate(persistPolicy, PERSIST_POLICIES);
               descr.setField(PERSIST_POLICY, persistPolicy);
            }
            if (persistPeriod != null)
            {
               descr.setField(PERSIST_PERIOD, persistPeriod);
            }
            if (persistLocation != null)
            {
               descr.setField(PERSIST_LOCATION, persistLocation);
            }
            if (persistName != null)
            {
               descr.setField(PERSIST_NAME, persistName);
            }
         }
         else if (name.equals(CURRENCY_TIME_LIMIT))
         {
            descr.setField(CURRENCY_TIME_LIMIT, descriptor.attributeValue("value"));
         }
         else if (name.equals(DEFAULT))
         {
            String value = descriptor.attributeValue("value");
            descr.setField(DEFAULT, value);
         }
         else if (name.equals("display-name"))//DISPLAY_NAME is displayname
         {
            String value = descriptor.attributeValue("value");
            descr.setField(DISPLAY_NAME, value);
         }
         else if (name.equals(CACHED_VALUE))
         {
            String value = descriptor.attributeValue("value");
            descr.setField(CACHED_VALUE, value);
         }
         else if (name.equals(PERSISTENCE_MANAGER))
         {
            descr.setField(PERSISTENCE_MANAGER, descriptor.attributeValue("value"));
         }
         else if (name.equals(DESCRIPTOR))
         {
            descr.setField(descriptor.attributeValue("name"), descriptor.attributeValue("value"));
         }
         else if (name.equals("injection"))
         {
            descr.setField(descriptor.attributeValue("id"), descriptor.attributeValue("setMethod"));
         }
         else if(name.equals(INTERCEPTORS))
         {
            Descriptor[] interceptorDescriptors = buildInterceptors(descriptor);
            descr.setField(INTERCEPTORS, interceptorDescriptors);
         }         
      } // end of for ()

      return descr;
   }

   private void validate(String value, String[] valid) throws NotCompliantMBeanException
   {
      for (int i = 0; i< valid.length; i++)
      {
         if (valid[i].equalsIgnoreCase(value)) 
         {
            return;
         } // end of if ()
      } // end of for ()
      throw new JBossNotCompliantMBeanException("Unknown descriptor value: " + value);      
   }


   // builder methods

   protected ModelMBeanInfo buildMBeanMetaData(String description,
                                               List constructors, List operations, List attributes,
                                               List notifications, Descriptor descr)
      throws NotCompliantMBeanException
   {

      ModelMBeanOperationInfo[] operInfo =
         buildOperationInfo(operations);
      ModelMBeanAttributeInfo[] attrInfo =
         buildAttributeInfo(attributes);
      ModelMBeanConstructorInfo[] constrInfo =
         buildConstructorInfo(constructors);
      ModelMBeanNotificationInfo[] notifInfo =
         buildNotificationInfo(notifications);

      ModelMBeanInfo info = new ModelMBeanInfoSupport(
         mmbClassName, description, attrInfo, constrInfo,
         operInfo, notifInfo, descr
      );

      return info;
   }


   protected ModelMBeanConstructorInfo[] buildConstructorInfo(List constructors)
      throws NotCompliantMBeanException
   {

      List infos = new ArrayList();

      for (Iterator it = constructors.iterator(); it.hasNext();)
      {
         Element constr = (Element) it.next();
         String name = constr.elementTextTrim("name");
         String description = constr.elementTextTrim("description");
         List params = constr.elements("parameter");

         MBeanParameterInfo[] paramInfo =
            buildParameterInfo(params);

         Descriptor descr = getDescriptor(constr, name, OPERATION_DESCRIPTOR);
         descr.setField(ROLE,  ROLE_CONSTRUCTOR);

         ModelMBeanConstructorInfo info =
            new ModelMBeanConstructorInfo(name, description, paramInfo, descr);

         infos.add(info);
      }

      return (ModelMBeanConstructorInfo[]) infos.toArray(
         new ModelMBeanConstructorInfo[0]);
   }

   protected ModelMBeanOperationInfo[] buildOperationInfo(List operations)
      throws NotCompliantMBeanException
   {
      List infos = new ArrayList();

      for (Iterator it = operations.iterator(); it.hasNext(); )
      {
         Element oper = (Element) it.next();
         String name = oper.elementTextTrim("name");
         String description = oper.elementTextTrim("description");
         String type = oper.elementTextTrim("return-type");
         String impact = oper.attributeValue("impact");
         List params = oper.elements("parameter");

         MBeanParameterInfo[] paramInfo =
            buildParameterInfo(params);

         Descriptor descr = getDescriptor(oper, name, OPERATION_DESCRIPTOR);
         descr.setField(ROLE, ROLE_OPERATION);

         // defaults to ACTION_INFO
         int operImpact = MBeanOperationInfo.ACTION_INFO;

         if (impact != null)
         {
            if (impact.equals(INFO))
               operImpact = MBeanOperationInfo.INFO;
            else if (impact.equals(ACTION))
               operImpact = MBeanOperationInfo.ACTION;
            else if (impact.equals(ACTION_INFO))
               operImpact = MBeanOperationInfo.ACTION_INFO;
         }

         // default return-type is void
         if (type == null)
            type = "void";

         ModelMBeanOperationInfo info = new ModelMBeanOperationInfo(
            name, description, paramInfo, type, operImpact, descr);

         infos.add(info);
      }

      return (ModelMBeanOperationInfo[]) infos.toArray(
         new ModelMBeanOperationInfo[0]);
   }


   protected ModelMBeanNotificationInfo[] buildNotificationInfo(List notifications)
      throws NotCompliantMBeanException
   {

      List infos = new ArrayList();

      for (Iterator it = notifications.iterator(); it.hasNext();)
      {
         Element notif = (Element) it.next();
         String name = notif.elementTextTrim("name");
         String description = notif.elementTextTrim("description");
         List notifTypes = notif.elements("notification-type");
         Descriptor descr = getDescriptor(notif, name, NOTIFICATION_DESCRIPTOR);

         List types = new ArrayList();

         for (Iterator iterator = notifTypes.iterator(); iterator.hasNext();)
         {
            Element type = (Element) iterator.next();
            types.add(type.getTextTrim());
         }

         ModelMBeanNotificationInfo info = new ModelMBeanNotificationInfo(
            (String[]) types.toArray(new String[types.size()]), name, description, descr);

         infos.add(info);
      }

      return (ModelMBeanNotificationInfo[]) infos.toArray(
         new ModelMBeanNotificationInfo[infos.size()]
      );
   }

   protected ModelMBeanAttributeInfo[] buildAttributeInfo(List attributes)
      throws NotCompliantMBeanException
   {

      List infos = new ArrayList();

      for (Iterator it = attributes.iterator(); it.hasNext();)
      {
         Element attr = (Element) it.next();
         String name = attr.elementTextTrim("name");
         String description = attr.elementTextTrim("description");
         String type = attr.elementTextTrim("type");
         String access = attr.attributeValue("access");
         String getMethod = attr.attributeValue("getMethod");
         String setMethod = attr.attributeValue("setMethod");
         Descriptor descr = getDescriptor(attr, name, ATTRIBUTE_DESCRIPTOR);
         //Convert types here from string to specified type
         String unconvertedValue = (String)descr.getFieldValue(CACHED_VALUE);
         if (unconvertedValue != null && !"java.lang.String".equals(type))
         {
            descr.setField(CACHED_VALUE, convertValue(unconvertedValue, type));
         }
         else
         {
            // if <value value="xxx"/> is absent
            // try new syntax for VALUE initialization
            // e.g <value><nested-element/></value>
            Object value = getAttributeValue(attr, type, CACHED_VALUE);
            if (value != null)
               descr.setField(CACHED_VALUE, value);
         }         
         String unconvertedDefault = (String)descr.getFieldValue(DEFAULT);
         if (unconvertedDefault != null && !"java.lang.String".equals(type))
         {
            descr.setField(DEFAULT, convertValue(unconvertedDefault, type));
         }
         else
         {
            // if <defaul value="xxx"/> is absent
            // try new syntax for DEFAULT initialization
            // e.g <default><nested-element/></default>
            Object value = getAttributeValue(attr, type, DEFAULT);
            if (value != null)
               descr.setField(DEFAULT, value);
         }             
         if (getMethod != null) 
         {
            descr.setField(GET_METHOD, getMethod);
         } // end of if ()
         
         if (setMethod != null) 
         {
            descr.setField(SET_METHOD, setMethod);
         } // end of if ()
         

         // defaults read-write
         boolean isReadable = true;
         boolean isWritable = true;

         if (access.equalsIgnoreCase("read-only"))
            isWritable = false;

         else if (access.equalsIgnoreCase("write-only"))
            isReadable = false;


         ModelMBeanAttributeInfo info = new ModelMBeanAttributeInfo(
            name, type, description, isReadable, isWritable, false, descr
         );


         infos.add(info);
      }

      return (ModelMBeanAttributeInfo[]) infos.toArray(
         new ModelMBeanAttributeInfo[0]
      );
   }

   /**
    * Get the value for the attribute descriptor "value" or "default"
    * the same way we would do for mbean attribute overrides
    */
   protected Object getAttributeValue(Element attribute, String typeName, String which)
      throws NotCompliantMBeanException
   {
      Object value = null;
      
      Element descriptors = attribute.element("descriptors");
      if (descriptors != null) 
      {
         for (Iterator i = descriptors.elementIterator(); i.hasNext();)
         {
            // looking for 'which', i.e. "value" or "default"
            Element descriptor = (Element)i.next();
            String name = descriptor.getName();
            if (name.equals(which) && descriptor.hasContent()) 
            {
               // at this point "value" attribute does not exist
               // plus the descriptor has content so we know the
               // new syntax is used.
               // 
               // Convert to org.w3c.dom.Element so that the code
               // from ServiceConfigurator can be applied, plus
               // if attribute type is org.w3c.dom.Element we need
               // to make the conversion anyway.
               
               // descriptor(org.dom4j.Element) -> element (org.w3c.dom.Element)
               try
               {
                  org.w3c.dom.Element element = toW3CElement(descriptor);
                  
                  boolean replace = true;
                  boolean trim = true;
                  
                  String replaceAttr = element.getAttribute("replace");
                  if( replaceAttr.length() > 0 )
                     replace = Boolean.valueOf(replaceAttr).booleanValue();
                  String trimAttr = element.getAttribute("trim");
                  if( trimAttr.length() > 0 )
                     trim = Boolean.valueOf(trimAttr).booleanValue();
                  
                  // Get the classloader for loading attribute classes.
                  ClassLoader cl = Thread.currentThread().getContextClassLoader();
                  
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
                        throw new JBossNotCompliantMBeanException
                           ("Class not found '" + typeName + "'", e);
                     }
                  }

                  /* Attributes of type Element are passed as is after optionally
                  performing system property replacement
                  */
                  if (typeClass.equals(org.w3c.dom.Element.class))
                  {
                     // Use the first child Element of this element as the value
                     NodeList nl = element.getChildNodes();
                     for (int j=0; j < nl.getLength(); j++)
                     {
                        Node n = nl.item(j);
                        if (n.getNodeType() == Node.ELEMENT_NODE)
                        {
                           value = n;
                           break;
                        }
                     }
                     // Replace any ${x} references in the element text
                     if( replace )
                     {
                        PropertyEditor editor = PropertyEditorFinder.getInstance().find(typeClass);
                        if( editor == null )
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
                        throw new JBossNotCompliantMBeanException
                           ("No property editor for type '" + typeName + "'");
                     }
                     // Get the attribute value
                     String attributeText = getElementContent(element, trim, replace);
                     editor.setAsText(attributeText);
                     value = editor.getValue();
                  }
               }
               catch (org.dom4j.DocumentException e)
               {
                  throw new JBossNotCompliantMBeanException(
                        "cannot convert '" + which + "' descriptor to org.w3c.dom.Element", e);
               }

               // stop processing
               break;
            }
         }
      }
      return value;
   }

   /**
    * Convert org.dom4j.Element->org.w3c.dom.Element
    */
   private org.w3c.dom.Element toW3CElement(org.dom4j.Element d4element)
      throws org.dom4j.DocumentException
   {
      // prepare
      org.dom4j.Document d4doc = org.dom4j.DocumentFactory.getInstance().createDocument();
      org.dom4j.io.DOMWriter d4Writer = new org.dom4j.io.DOMWriter();
      // copy
      d4doc.setRootElement(d4element.createCopy());
      // convert
      org.w3c.dom.Document doc = d4Writer.write(d4doc);
      // return root Element - should I copy again?
      return doc.getDocumentElement();
   }
   
   /**
    * Copied from ServiceConfigurator
    */
   private String getElementContent(org.w3c.dom.Element element, boolean trim, boolean replace)
   {
      NodeList nl = element.getChildNodes();
      String attributeText = "";
      for (int i = 0; i < nl.getLength(); i++)
      {
         Node n = nl.item(i);
         if( n instanceof org.w3c.dom.Text )
         {
            attributeText += ((org.w3c.dom.Text)n).getData();
         }
      } // end of for ()
      if( trim )
         attributeText = attributeText.trim();
      if (replace)
         attributeText = StringPropertyReplacer.replaceProperties(attributeText);
      return attributeText;
   }
   
   /**
    * Describe <code>convertType</code> method here.
    * Copied from ServiceConfigurator, without Element support.
    *
    * @param unconverted a <code>String</code> value
    * @param typeName a <code>String</code> value
    * @return an <code>Object</code> value
    * @exception NotCompliantMBeanException if an error occurs
    */
   protected Object convertValue(String unconverted, String typeName)
      throws NotCompliantMBeanException
   {
      Object value = null;
      try
      {
         value = BeanUtils.convertValue(unconverted, typeName);
      }
      catch (ClassNotFoundException e)
      {
         log.debug("Failed to load type class", e);
         throw new NotCompliantMBeanException
               ("Class not found for type: " + typeName);
      }
      catch(IntrospectionException e)
      {
         throw new NotCompliantMBeanException
               ("No property editor for type=" + typeName);
      }
      return value;
   }

   protected MBeanParameterInfo[] buildParameterInfo(List parameters)
   {
      Iterator it = parameters.iterator();
      List infos = new ArrayList();

      while (it.hasNext())
      {
         Element param = (Element) it.next();
         String name = param.elementTextTrim("name");
         String type = param.elementTextTrim("type");
         String descr = param.elementTextTrim("description");

         MBeanParameterInfo info = new MBeanParameterInfo(name, type, descr);

         infos.add(info);
      }
      
      return (MBeanParameterInfo[]) infos.toArray(new MBeanParameterInfo[0]);
   }

   protected Descriptor[] buildInterceptors(Element descriptor)
   {
      List interceptors = descriptor.elements("interceptor");
      ArrayList tmp = new ArrayList();
      for(int i = 0; i < interceptors.size(); i ++)
      {
         Element interceptor = (Element) interceptors.get(i);
         String code = interceptor.attributeValue("code");
         DescriptorSupport interceptorDescr = new DescriptorSupport();
         interceptorDescr.setField("code", code);
         List attributes = interceptor.attributes();
         for(int a = 0; a < attributes.size(); a ++)
         {
            Attribute attr = (Attribute) attributes.get(a);
            String name = attr.getName();
            String value = attr.getValue();
            value = StringPropertyReplacer.replaceProperties(value);
            interceptorDescr.setField(name, value);
         }
         tmp.add(interceptorDescr);
      }
      Descriptor[] descriptors = new Descriptor[tmp.size()];
      tmp.toArray(descriptors);
      return descriptors;
   }

}
