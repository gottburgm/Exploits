/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
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
package org.jboss.as.integration.hornetq.management.template;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hornetq.jms.server.config.ConnectionFactoryConfiguration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.TopicConfiguration;
import org.jboss.deployers.spi.management.DeploymentTemplate;
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
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VFS;

/**
 * A HornetQDestinationTemplate.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class HornetQDestinationTemplate implements DeploymentTemplate
{
   /** The file suffix. */
   private static final String FILE_SUFFIX = "hornetq-jms.xml";
   
   /** The role attributes. */
   protected static final String[] attributes = new String[] { "read", "write", "create"};
   
   private static final MetaValueFactory mvf = MetaValueFactoryBuilder.create();
   
   private DeploymentTemplateInfo info;

   public VirtualFile applyTemplate(DeploymentTemplateInfo info) throws Exception
   {
      // Create a temp file
      File xml = File.createTempFile(getClass().getSimpleName(), FILE_SUFFIX);
      // Write template
      writeTemplate(xml, info);
      // Return virtual file
      //todo check this
      //return VFS.getChild(xml.toURI());
       return VFS.getRoot(xml.toURI());
   }

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
   
   private void writeTemplate(File xml, DeploymentTemplateInfo info) throws Exception
   {
      if (info == null)
         throw new IllegalArgumentException("Null template info.");
      if (info.getProperties() == null)
         throw new IllegalArgumentException("Null template info.");

      JAXBJMSConfiguration config = new JAXBJMSConfiguration();

      // Look for the destination type using the destinationType ManagedProperty
      ManagedProperty destTypeMP = info.getProperties().get("destinationType");
      String destinationType = null;
      if(destTypeMP == null)
      {
         // Try casting this to a DsDataSourceTemplateInfo
         destinationType = ((HornetQDestinationTemplateInfo)info).getDestinationType();
      }
      else
      {
         SimpleValue dsTypeSV = (SimpleValue) destTypeMP.getValue();
         destinationType = dsTypeSV.getValue().toString();
      }

      String destinationName = (String) getProperty(info, "name");
      if(destinationName == null)
         throw new IllegalStateException("Destination name has not been specified!");

      String[] bindings = (String[]) getProperty(info, "bindings");
      if(bindings == null)
         throw new IllegalStateException("bindings have not been specified!");

      if("QueueTemplate".equals(destinationType))
      {
         JAXBJMSQueueConfiguration queue = new JAXBJMSQueueConfiguration();
         config.setQueueConfigurations(Arrays.asList(new JMSQueueConfiguration[]{queue}));
         queue.setName(destinationName);
         queue.setBindings(bindings);
      }
      else if("TopicTemplate".equals(destinationType))
      {
         JAXBJMSTopicConfiguration topic = new JAXBJMSTopicConfiguration();
         config.setTopicConfigurations(Arrays.asList(new TopicConfiguration[]{topic}));
         topic.setName(destinationName);
         topic.setBindings(bindings);
      }
      else
         throw new IllegalStateException("Unsupported destination type: " + destinationType);

      JAXBContext context = JAXBContext.newInstance(JAXBJMSConfiguration.class);
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);

      JAXBElement<JAXBJMSConfiguration> root = new JAXBElement<JAXBJMSConfiguration>(
            new javax.xml.namespace.QName("urn:hornetq", "configuration"), JAXBJMSConfiguration.class, null, config);

      Writer fw = null;
      try
      {
         fw = new FileWriter(xml);
         marshaller.marshal(root, fw);
      }
      finally
      {
         if (fw != null)
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
}
