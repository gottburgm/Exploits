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
package org.jboss.mx.modelmbean;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.management.Descriptor;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcaster;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import javax.management.StandardMBean;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.mx.interceptor.StandardMBeanInfoInterceptor;
import org.jboss.mx.metadata.MBeanInfoConversion;
import org.jboss.mx.metadata.MetaDataBuilder;
import org.jboss.mx.metadata.StandardMetaData;
import org.jboss.mx.metadata.XMLMetaData;

/**
 * XMBean implementation.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author Matt Munz
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81026 $
 */
public class XMBean
   extends ModelMBeanInvoker
   implements XMBeanConstants, NotificationListener
{

   // Constructors --------------------------------------------------

   /**
    * Default constructor for the XMBean Model MBean implementation. This
    * creates an uninitialized Model MBean template.
    */
   public XMBean() throws MBeanException
   {
      try
      {
         setManagedResource(new Object(), OBJECT_REF);
         setModelMBeanInfo(new ModelMBeanInfoSupport("XMBean", "Uninitialized XMBean", new ModelMBeanAttributeInfo[0],
                 new ModelMBeanConstructorInfo[0], new ModelMBeanOperationInfo[0], new ModelMBeanNotificationInfo[0]));
      }
      catch (RuntimeException e)
      {
         throw new RuntimeOperationsException(e);
      }
      catch (Exception e)
      {
         throw new MBeanException(e);
      }
   }

   /**
    * Creates an XMBean Model MBean implementation with a predefined JMX
    * metadata.
    *
    * @param   info  Model MBean metadata describing this MBean template
    */
   public XMBean(ModelMBeanInfo info) throws MBeanException
   {
      super(info);
   }

   /**
    * Creates a XMBean instance with a given resource object and resource type. <p>
    *
    * This Model MBean implementation supports the following resource types:    <br><pre>
    *
    *   - {@link ModelMBeanConstants#OBJECT_REF OBJECT_REF}
    *   - {@link XMBeanConstants#STANDARD_INTERFACE STANDARD_INTERFACE}
    *   - {@link XMBeanConstants#DESCRIPTOR DESCRIPTOR}
    *   - Any valid URL string to a *.xml file.
    *
    * </pre>
    *
    * <tt><b>OBJECT_REF:</b></tt> resource object can be any Java object. The
    * management interface must be set separately via
    * {@link javax.management.modelmbean.ModelMBean#setModelMBeanInfo setModelMBeanInfo}
    * method.  <p>
    *
    * <tt><b>STANDARD_INTERFACE:</b></tt> the resource object is assumed to
    * follow the Standard MBean naming conventions to expose its management
    * interface, including implementing a <tt>xxxMBean</tt> interface. A
    * corresponding Model MBean metadata is generated for the Model MBean
    * representing this resource type.  <p>
    *
    * <tt><b>DESCRIPTOR:</b></tt> the resource object is wrapped as a part of
    * the {@link javax.management.Descriptor Descriptor} object passed to this
    * Model MBean instance. The descriptor object must contain the mandatory
    * fields {@link XMBeanConstants#RESOURCE_REFERENCE RESOURCE_REFERENCE} and
    * {@link XMBeanConstants#RESOURCE_TYPE RESOURCE_TYPE} that identify the
    * correct resource reference and type used for this Model MBean instance.
    * The descriptor object may also contain additional fields, such as
    * {@link XMBeanConstants#SAX_PARSER SAX_PARSER} and
    * {@link XMBeanConstants#XML_VALIDATION XML_VALIDATION} that are passed as
    * configuration properties for the metadata builder instances. Any
    * additional descriptor fields that match the
    * {@link XMBeanConstants#METADATA_DESCRIPTOR_PREFIX METADATA_DESCRIPTOR_PREFIX}
    * naming pattern will be passed to the builder implementation via its
    * {@link org.jboss.mx.metadata.MetaDataBuilder#setProperty setProperty}
    * method.    <p>
    *
    * <tt><b>URL String:</b></tt> if a resource type string contains an URL
    * that ends with a *.xml file name the resource object is exposed via the
    * XML management interface definition read from this URL. The XML parser
    * implementation is picked based on the schema definition in the XML
    * document.
    *
    * @param   resource     resource object or descriptor
    * @param   resourceType resource type string or URL to *.xml file
    */
   public XMBean(Object resource, String resourceType) throws MBeanException, NotCompliantMBeanException
   {
      // TODO: document STANDARD_MBEAN

      ModelMBeanInfo minfo = null;
      try
      {
         HashMap properties = new HashMap();

         if (resourceType.equals(DESCRIPTOR))
         {
            Descriptor d = (Descriptor)resource;

            // get the actual resource type from the descriptor
            resourceType = (String)d.getFieldValue(RESOURCE_TYPE);

            // and the resource reference
            resource = d.getFieldValue(RESOURCE_REFERENCE);

            // extract builder configuration fields
            String[] fields = d.getFieldNames();

            for (int i = 0; i < fields.length; ++i)
            {
               // extract all the fields starting with the METADATA_DESCRIPTOR_PREFIX
               // prefix to a property map that is passed to the builder implementations
               if (fields[i].startsWith(METADATA_DESCRIPTOR_PREFIX))
                  properties.put(fields[i], d.getFieldValue(fields[i]));
            }
         }

         if (resourceType.equals(STANDARD_MBEAN) && resource instanceof StandardMBean)
            setManagedResource(((StandardMBean)resource).getImplementation(), resourceType);
         else
            setManagedResource(resource, resourceType);

         // automatically create management operations that the attributes
         // can map to.
         final boolean CREATE_ATTRIBUTE_OPERATION_MAPPING = true;

         // the resource extends StandardMBean
         if (resourceType.equals(STANDARD_MBEAN) &&
             resource instanceof StandardMBean)
         {
            StandardMBean standardMBean = (StandardMBean) resource;
            minfo = MBeanInfoConversion.toModelMBeanInfo(standardMBean.getMBeanInfo(),
               CREATE_ATTRIBUTE_OPERATION_MAPPING);
         }

         // the resource implements a Standard MBean interface
         else if ((resourceType.equals(STANDARD_INTERFACE)) ||
             (resourceType.equals(STANDARD_MBEAN)))
         {
            dynamicResource = false;
            
            // create and configure the builder
            MetaDataBuilder builder = new StandardMetaData(resource);

            // pass the config keys to the builder instance
            for (Iterator it = properties.keySet().iterator(); it.hasNext();)
            {
               String key = (String)it.next();
               builder.setProperty(key, properties.get(key));
            }

            // build the metadata
            MBeanInfo standardInfo = builder.build();

            // StandardMetaData is used by the MBean server to introspect
            // standard MBeans. We need to now turn that Standard metadata into
            // ModelMBean metadata (including operation mapping for attributes)
            minfo = MBeanInfoConversion.toModelMBeanInfo(standardInfo, CREATE_ATTRIBUTE_OPERATION_MAPPING);
         }

         // If the resource type string ends with an '.xml' extension attempt
         // to create the metadata with the aggregated XML builder.
         else if (resourceType.endsWith(".xml"))
         {
            // Create and configure the builder. XMLMetaData builder is an
            // aggregate builder that picks the correct schema specific builder
            // based on schema declaration at the beginning of the XML file.

            MetaDataBuilder builder = new XMLMetaData(
                  this.getClass().getName(),     // MMBean implementation name
                  resource.getClass().getName(), // resource class name
                  resourceType
            );

            // pass the config keys to the builder instance
            for (Iterator it = properties.keySet().iterator(); it.hasNext();)
            {
               String key = (String)it.next();
               builder.setProperty(key, properties.get(key));
            }

            minfo = (ModelMBeanInfo) builder.build();
         }
         // Sotre the ModelMBeanInfo
         this.setModelMBeanInfo(minfo);

         // we must try to load this MBean (as the superclass does), even if only NullPersistence
         // is used - MMM
         load();
      }
      catch (InstanceNotFoundException e)
      {
         throw new MBeanException(e);
      }
      catch (InvalidTargetObjectTypeException e)
      {
         if (resourceType.endsWith(".xml"))
            throw new MBeanException(e, "Malformed URL: " + resourceType);

         throw new MBeanException(e, "Unsupported resource type: " + resourceType);
      }
      catch (MalformedURLException e)
      {
         throw new MBeanException(e, "Malformed URL: " + resourceType);
      }
   }


   public XMBean(Object resource, URL interfaceURL) throws MBeanException, NotCompliantMBeanException
   {
      this(resource, interfaceURL.toString());
   }


   public XMBean(Descriptor descriptor) throws MBeanException, NotCompliantMBeanException
   {
      this(descriptor, DESCRIPTOR);
   }

   public XMBean(Object resource, org.w3c.dom.Element element, String version) throws MBeanException, NotCompliantMBeanException
   {
      try
      {
         DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         org.w3c.dom.Document doc = builder.newDocument();
         doc.appendChild(doc.importNode(element, true));

         org.dom4j.io.DOMReader domReader = new org.dom4j.io.DOMReader();
         org.dom4j.Document dom4jDoc = domReader.read(doc);
         org.dom4j.Element dom4jElem = dom4jDoc.getRootElement();
         dom4jElem.detach();
         createXMBean(resource, dom4jElem, version);
      }
      catch (ParserConfigurationException e)
      {
         throw new MBeanException(e, "Could not convert w3c Element to dom4j Element.");
      }

   }

   public XMBean(Object resource, org.dom4j.Element element, String version) throws MBeanException, NotCompliantMBeanException
   {
      //      this(resource, OBJECT_REF);
      createXMBean(resource, element, version);

   }

   private void createXMBean(Object resource, org.dom4j.Element element, String version)
         throws MBeanException, NotCompliantMBeanException
   {
      try
      {
         setManagedResource(resource, OBJECT_REF);
         MetaDataBuilder builder = new XMLMetaData(
            this.getClass().getName(),     // MMBean implementation name
            resource.getClass().getName(), // resource class name
            element,
            version
            );

         ModelMBeanInfo minfo = (ModelMBeanInfo) builder.build();
         this.setModelMBeanInfo(minfo);
      }
      catch (InstanceNotFoundException e)
      {
         throw new MBeanException(e);
      }
      catch (InvalidTargetObjectTypeException e)
      {
         throw new MBeanException(e, "Unsupported resource type: " + resourceType);
      }

   }


   // Public --------------------------------------------------------

   public boolean isSupportedResourceType(Object resource, String resourceType)
   {
      if (resourceType == null)
         return false;

      if (resourceType.equalsIgnoreCase(OBJECT_REF))
         return true;
      if (resourceType.equalsIgnoreCase(STANDARD_INTERFACE))
         return true;
      if (resourceType.equalsIgnoreCase(STANDARD_MBEAN))
         return true;
      if (resourceType.equalsIgnoreCase(DESCRIPTOR))
      {
         if (resource == null || !(resource instanceof Descriptor))
            return false;

         Descriptor d = (Descriptor)resource;

         if (d.getFieldValue(RESOURCE_REFERENCE) == null)
            return false;

         if (d.getFieldValue(RESOURCE_TYPE) == null)
            return false;

         return true;
      }
      if (resourceType.endsWith(".xml"))
      {
         try
         {
            new URL(resourceType);
            return true;
         }
         catch (MalformedURLException e)
         {
            return false;
         }
      }

      return false;
   }


   // ModelMBeanInvoker overrides -----------------------------------

   protected void configureInterceptorStack(ModelMBeanInfo info,
      MBeanServer server, ObjectName name)
     throws Exception
   {
      // FIXME: do not require super calls

      super.configureInterceptorStack(info, server, name);

      if (resourceType.equals(STANDARD_MBEAN))
      {
         List interceptors = getMBeanInfoCtx.getInterceptors();
         interceptors.add(0, new StandardMBeanInfoInterceptor());
         getMBeanInfoCtx.setInterceptors(interceptors);
      }
   }

   // NotificationBroadcaster overrides -----------------------------

   // TODO: intercept these...  (?)  rather than do this overriding

   public void addNotificationListener(NotificationListener listener,
                                       NotificationFilter filter, Object handback)
   {
      // a standard mbean handles broadcasting itself (if a broadcaster)
      if (resourceType.equals(STANDARD_MBEAN))
      {
         addNotificationListenerToResource(listener, filter, handback);
      }
      else
      {
         // for all other types register a listener for AVCs
         // (including XMBeans wrapping POJOs or standard mbean impls)
         super.addNotificationListener(listener, filter, handback);
         
         // in addition if the resource is a broadcaster update its subscription list
         if (getResource() instanceof NotificationBroadcaster)
            addNotificationListenerToResource(listener, filter, handback);
      }
   }

   public void removeNotificationListener(NotificationListener listener)
         throws ListenerNotFoundException
   {
      // a standard mbean handles broadcasting itself (if a broadcaster)
      if (resourceType.equals(STANDARD_MBEAN))
      {
         removeNotificationListenerFromResource(listener);
      }
      else
      {
         // for all other types remove the listener for AVCs
         // (including XMBeans wrapping POJOs or standard mbean impls)
         super.removeNotificationListener(listener);
         
         // in addition if the resource is a broadcaster update its subscription list
         if (getResource() instanceof NotificationBroadcaster)
            removeNotificationListenerFromResource(listener);
      }
   }

   public void removeNotificationListener(NotificationListener listener,
                                          NotificationFilter filter,
                                          Object handback)
         throws ListenerNotFoundException
   {
      // a standard mbean handles broadcasting itself (if a broadcaster)
      if (resourceType.equals(STANDARD_MBEAN))
      {
         removeNotificationListenerFromResource(listener, filter, handback);
      }
      else
      {
         // for all other types remove the listener for AVCs
         // (including XMBeans wrapping POJOs or standard mbean impls)
         super.removeNotificationListener(listener, filter, handback);
         
         // in addition if the resource is a broadcaster update its subscription list
         if (getResource() instanceof NotificationBroadcaster)
            removeNotificationListenerFromResource(listener, filter, handback);
      }
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      if (resourceType.equals(STANDARD_MBEAN))
         return getNotificationInfoFromResource();
      else
         return super.getNotificationInfo();
   }

   // NotificationListener overrides --------------------------------
   
   /**
    * Implements NotificationListener interface by simply forwarding
    * any received Notification to the wrapped resource, if it
    * implements the NotificationListener interface, too.
    * 
    * This is needed to allow the wrapped resource to register for
    * Notifications using the XMBean ObjectName, rather than its own
    * "this" reference - dimitris
    */
   public void handleNotification(Notification notification, Object handback)
   {
      Object resource = getResource();
      
      if (resource instanceof NotificationListener)
         ((NotificationListener)resource).handleNotification(notification, handback);
   }

}
