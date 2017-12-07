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
package org.jboss.profileservice.management.templates;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.deployers.spi.management.DeploymentTemplate;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.plugins.values.MetaValueFactoryBuilder;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A template for creating jms destinations
 * 
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version <tt>$Revision: 87482 $</tt>
 */
public class JmsDestinationTemplate
   implements DeploymentTemplate
{
   
   /** The file suffix. */
   private static final String FILE_SUFFIX = "-service.xml";
   
   /** The role attributes. */
   protected static final String[] attributes = new String[] { "read", "write", "create"};
   
   private static final MetaValueFactory mvf = MetaValueFactoryBuilder.create();
   
   private DeploymentTemplateInfo info;
   
   public String getDeploymentName(String deploymentBaseName)
   {
      if(deploymentBaseName == null)
         throw new IllegalArgumentException("Null base name.");
      
      if(deploymentBaseName.endsWith(FILE_SUFFIX) == false)
         deploymentBaseName = deploymentBaseName + FILE_SUFFIX;
      
      return deploymentBaseName;
   }

   public DeploymentTemplateInfo getInfo()
   {
      return info;
   }

   public void setInfo(DeploymentTemplateInfo info)
   {
      this.info = info;
   }

   public VirtualFile applyTemplate(VirtualFile root, String deploymentBaseName,
         DeploymentTemplateInfo values)
      throws Exception
   {
      return applyTemplate(values);
   }

   public VirtualFile applyTemplate(DeploymentTemplateInfo values) throws Exception
   {
      // Create a temp file
      File dsXml = File.createTempFile(getClass().getSimpleName(), FILE_SUFFIX);
      // Write template
      writeTemplate(dsXml, values);
      // Return virtual file
      return VFS.getRoot(dsXml.toURI());
   }

   public void updateTemplateDeployment(VFSDeployment ctx, DeploymentTemplateInfo values) throws Exception
   {
   }

   private void writeTemplate(File dsXml, DeploymentTemplateInfo info)
      throws Exception
   {
      if(info == null)
         throw new IllegalArgumentException("Null template info.");
      if(info.getProperties() == null)
         throw new IllegalArgumentException("Null template info.");
      
      // Look for the destination type using the destinationType ManagedProperty
      ManagedProperty destTypeMP = info.getProperties().get("destinationType");
      String destinationType = null;
      if(destTypeMP == null)
      {
         // Try casting this to a DsDataSourceTemplateInfo
         destinationType = ((JmsDestinationTemplateInfo)info).getDestinationType();
      }
      else
      {
         SimpleValue dsTypeSV = (SimpleValue) destTypeMP.getValue();
         destinationType = dsTypeSV.getValue().toString();
      }

      JmsDestinationMetaData destination = new JmsDestinationMetaData();
      
      String destinationName = (String) getProperty(info, "name");
      String jndiName = (String) getProperty(info, "JNDIName");
      
      if(jndiName == null)
         throw new IllegalStateException("Null jndi name.");
      if(destinationName == null)
         destinationName = jndiName;
      
      if("QueueTemplate".equals(destinationType))
      {
         destination.className = "org.jboss.jms.server.destination.QueueService";
//         destination.annotation =
//            "@org.jboss.system.deployers.managed.ManagementObjectClass(code=org.jboss.jms.server.destination.QueueServiceMO)";
         destination.xmbeanDd = "xmdesc/Queue-xmbean.xml";
         destination.jmxName = "jboss.messaging.destination:service=Queue,name=" + destinationName;
      }
      else if("TopicTemplate".equals(destinationType))
      {
         destination.className = "org.jboss.jms.server.destination.TopicService";
//         destination.annotation =
//            "@org.jboss.system.deployers.managed.ManagementObjectClass(code=org.jboss.jms.server.destination.TopicServiceMO)";
         destination.xmbeanDd = "xmdesc/Topic-xmbean.xml";
         destination.jmxName = "jboss.messaging.destination:service=Topic,name=" + destinationName;
      }
      else
      {
         throw new IllegalStateException("Unsupported destination type: " + destinationType);
      }
      List<JmsAttribute> attributes = new ArrayList<JmsAttribute>();
      destination.attribute = attributes;
      
      Map<String, ManagedProperty> properties = info.getProperties();
      for(ManagedProperty p : properties.values())
      {
         // Get the value
         MetaValue v = p.getValue();
         String name = p.getName();
         
         // Check if we need to skip a property
         boolean skip = p.hasViewUse(ViewUse.CONFIGURATION) == false
            || p.isReadOnly()
            || p.isRemoved();
          
         // Don't skip clustered, which is a read
         if(skip)
         {
            if(name.equals("clustered"))
               skip = false;
         }

         if(skip)
            continue;
         
         // Skip null values
         if(v == null)
            continue;
         
         // Skip the destinationType
         if(p == destTypeMP)
            continue;

         char c = name.charAt(0);
         if(Character.isLowerCase(c))
            name = Character.toUpperCase(c) + name.substring(1);
         
         JmsAttribute attribute = null;
         if(v.getMetaType().isSimple())
         {
            attribute = new JmsAttributeMetaData(name, "" + ((SimpleValue)v).getValue());
         }
         else
         {
            if(name.equals("DLQ"))
            {
               String n = (String) mvf.unwrap(v);
               attribute = new JmsAttributeMetaData("DLQ", n);
            }
            else if(name.equals("ExpiryQueue"))
            {
               String n = (String) mvf.unwrap(v);
               attribute = new JmsAttributeMetaData("ExpiryQueue", n);
            }
            else if(name.equals("SecurityConfig"))
            {
               Element e = unwrapMetaValue((MapCompositeValueSupport) v);
               if(e != null)
                  attribute = new JmsElementMetaData("SecurityConfig", e);
            }
         }
         if(attribute != null)
            attributes.add(attribute);
      }
      
      // Add dependencies
      List<JmsDependencyMetaData> depends = new ArrayList<JmsDependencyMetaData>();
      destination.depends = depends;
      // Set server peer
      String serverPeer = (String) getProperty(info, "serverPeer");
      if(serverPeer != null)
      {
         depends.add(new JmsDependencyMetaData("ServerPeer", serverPeer));
      }
      else
      {
         depends.add(new JmsDependencyMetaData("ServerPeer", "jboss.messaging:service=ServerPeer"));
      }
      // <depends>
      depends.add(new JmsDependencyMetaData("jboss.messaging:service=PostOffice"));

      JmsDestinationDeployment deployment = new JmsDestinationDeployment();
      deployment.destination = destination;

      Class<?>[] classes = {JmsDestinationDeployment.class};
      JAXBContext context = JAXBContext.newInstance(classes);
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);

      JAXBElement<JmsDestinationDeployment> root =
         new JAXBElement<JmsDestinationDeployment>(
            new javax.xml.namespace.QName("server"),
            JmsDestinationDeployment.class,
            null, deployment
         );

      FileWriter fw = null;
      try
      {
         fw = new FileWriter(dsXml);
         marshaller.marshal(root, fw);
      }
      finally
      {
         if(fw != null)
         {
            fw.close();
         }
      }
   }

   /**
    * Extract the value from the property MetaValue
    * @param info - template info
    * @param propName - the name of the property to return a value for
    * @return the unwrapped property value
    */
   private Object getProperty(DeploymentTemplateInfo info, String propName)
   {
      Map<String, ManagedProperty> propsInfo = info.getProperties();
      ManagedProperty prop = propsInfo.get(propName);
      if(prop == null)
      {
         return null;
      }
      Object value = prop.getValue();
      if(value instanceof MetaValue)
      {
         return mvf.unwrap((MetaValue) value);
      }
      return value;
   }

   /**
    * Unwrap the value.
    * TODO This should actually be done by the SecurityConfigMapper and 
    * the metaValueFacotyr
    * 
    * @param metaValue the meta value
    * @return the element
    */
   protected Element unwrapMetaValue(MapCompositeValueSupport metaValue)
   {
      if(metaValue == null)
         return null;
      
      MapCompositeValueSupport value = (MapCompositeValueSupport) metaValue;
      CompositeMetaType metaType = value.getMetaType();
      // Don't create a empty set
      if(metaType.itemSet().isEmpty())
         return null;
      
      // Create the dom document
      Document d = createDocument();
      // Security
      Element security = d.createElement("security");
      // Get the roles
      for(String name : metaType.itemSet())
      {
         // Role
         CompositeValue row = (CompositeValue) value.get(name);
         if(row == null)
            continue;
         Element role = d.createElement("role");
         role.setAttribute("name", name);
         
         // For each attribute: read, write, create
         for(String attribute : attributes)
         {
            SimpleValue v = (SimpleValue) row.get(attribute);
            if(v != null && v.getValue() != null)
            {
               role.setAttribute(attribute, ((Boolean)v.getValue()).toString());
            }
         }
         security.appendChild(role);
      }
      return security;
   }
   
   protected static Document createDocument()
   {
      try
      {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         return db.newDocument();
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   // the classes below should go away and ServiceDeployment and MetaData
   // API should be used instead once their bound with JAXB

   @XmlRootElement(name = "server")
   public static class JmsDestinationDeployment
   {
      @XmlElement(name = "mbean")
      public JmsDestinationMetaData destination;
   }

   public static class JmsDestinationMetaData
   {
      @XmlAttribute(name = "code")
      String className;

      @XmlAttribute(name = "name")
      String jmxName;

      @XmlAttribute(name = "xmbean-dd")
      String xmbeanDd;

      @XmlElement
      String annotation;

      @XmlElements(value = {
         @XmlElement(type = JmsAttributeMetaData.class),
         @XmlElement(type = JmsElementMetaData.class) })
      public List<JmsAttribute> attribute;
      public List<JmsDependencyMetaData> depends;
   }

   public static class JmsAttributeMetaData implements JmsAttribute
   {
      @XmlAttribute
      String name;
      @XmlValue
      String value;
      public JmsAttributeMetaData()
      {
      }
      public JmsAttributeMetaData(String name, String value)
      {
         this.name = name;
         this.value = value;
      }
   }
   public static class JmsElementMetaData implements JmsAttribute
   {
      @XmlAttribute
      String name;
      @XmlAnyElement
      Element value;
      public JmsElementMetaData()
      {
      }
      public JmsElementMetaData(String name, Element value)
      {
         this.name = name;
         this.value = value;
      }
   }
   public static class JmsDependencyMetaData
   {
      @XmlAttribute(name = "optional-attribute-name")
      String attribute;
      @XmlValue
      String value;
      public JmsDependencyMetaData()
      {
      }

      public JmsDependencyMetaData(String value)
      {
         this.value = value;
      }

      public JmsDependencyMetaData(String attribute, String value)
      {
         this.attribute = attribute;
         this.value = value;
      }
   }
   public static interface JmsAttribute
   {
      // workaround jaxb
   }
}
