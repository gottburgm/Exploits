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
package org.jboss.test.xml.mbeanserver;

import java.util.Properties;
import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;

import org.jboss.common.beans.property.BeanUtils;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingInitializer;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.binding.sunday.unmarshalling.TypeBinding;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultElementHandler;
import org.jboss.xb.binding.sunday.unmarshalling.ElementBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ParticleBinding;
import org.jboss.xb.binding.sunday.unmarshalling.impl.runtime.RtElementHandler;
import org.xml.sax.Attributes;

/**
 * JavaBeanSchemaInitializer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 113110 $
 */
public class JavaBeanSchemaInitializer implements SchemaBindingInitializer
{
   /** The namespace */
   private static final String JAVABEAN_NS = "urn:jboss:simplejavabean:1.0";

   /** The javabean binding */
   private static final QName javabeanTypeQName = new QName(JavaBeanSchemaInitializer.JAVABEAN_NS, "javabeanType");


   public SchemaBinding init(SchemaBinding schema)
   {
      // javabean binding
      TypeBinding beanType = schema.getType(JavaBeanSchemaInitializer.javabeanTypeQName);
/*
      beanType.setHandler(new DefaultElementHandler()
      {
         public Object startElement(Object parent, QName name, ElementBinding element)
         {
            return new Holder();
         }

         public void attributes(Object o, QName elementName, ElementBinding element, Attributes attrs, NamespaceContext nsCtx)
         {
            Holder holder = (Holder) o;
            String className = null;
            Properties properties = holder.getProperties();
            for (int i = 0; i < attrs.getLength(); ++i)
            {
               String localName = attrs.getLocalName(i);
               String value = attrs.getValue(i);
               if ("class".equals(localName))
               {
                  className = value;
                  holder.setType(className);
                  continue;
               }
               properties.put(localName, value);
            }

            if (className == null)
               throw new IllegalArgumentException("No class attribute for " + elementName);
         }

         public Object endElement(Object o, QName qName, ElementBinding element)
         {
            Holder holder = (Holder) o;
            Object bean;
            try
            {
               bean = holder.getBean();
               Properties props = holder.getProperties();
               System.out.println("Converting properties: "+props);
               PropertyEditors.mapJavaBeanProperties(bean, props, true);
            }
            catch (Exception e)
            {
               throw new IllegalStateException("Failed to init bean: "+qName+"::"+ e.getMessage());
            }
            return bean;
         }

      });
*/

      beanType.setHandler(new RtElementHandler()
      {
         public Object startParticle(Object parent,
                                     QName elementName,
                                     ParticleBinding particle,
                                     Attributes attrs,
                                     NamespaceContext nsCtx)
         {
            Holder o = new Holder();
            attributes(o, elementName, (ElementBinding)particle.getTerm(), attrs, nsCtx);
            return o;
         }

         public void attributes(Object o, QName elementName, ElementBinding element, Attributes attrs, NamespaceContext nsCtx)
         {
            Holder holder = (Holder) o;
            String className = null;
            Properties properties = holder.getProperties();
            for (int i = 0; i < attrs.getLength(); ++i)
            {
               String localName = attrs.getLocalName(i);
               String value = attrs.getValue(i);
               if ("class".equals(localName))
               {
                  className = value;
                  holder.setType(className);
                  continue;
               }
               properties.put(localName, value);
            }

            if (className == null)
               throw new IllegalArgumentException("No class attribute for " + elementName);
         }

         public Object endParticle(Object o, QName qName, ParticleBinding particle)
         {
            Holder holder = (Holder) o;
            Object bean;
            try
            {
               bean = holder.getBean();
               Properties props = holder.getProperties();
               //System.out.println("Converting properties: "+props);
               BeanUtils.mapJavaBeanProperties(bean, props, true);
            }
            catch (Exception e)
            {
               throw new IllegalStateException("Failed to init bean: "+qName+"::"+ e.getMessage());
            }
            return bean;
         }
      });

      return schema;
   }

   public static class Holder
   {
      private String clazz;
      private Object bean;
      private Properties properties = new Properties();

      public Holder()
      {
      }

      public Object getBean() throws Exception
      {
         if( bean == null )
         {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class c = loader.loadClass(clazz);
            bean = c.newInstance();
         }
         return bean;
      }
      public Properties getProperties()
      {
         return properties;
      }
      public String getType()
      {
         return clazz;
      }
      public void setType(String clazz)
      {
         this.clazz = clazz;
      }
   }

   public static class Property
   {
      private String name;
      private String value;

      public String getName()
      {
         return name;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public String getValue()
      {
         return value;
      }

      public void setValue(String value)
      {
         this.value = value;
      }
   }
}
