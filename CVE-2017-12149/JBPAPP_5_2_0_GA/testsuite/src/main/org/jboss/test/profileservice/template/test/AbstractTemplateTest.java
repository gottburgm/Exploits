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
package org.jboss.test.profileservice.template.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.beans.info.spi.PropertyInfo;
import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.plugins.factory.DeploymentTemplateInfoFactory;
import org.jboss.managed.plugins.factory.ManagedObjectFactoryBuilder;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.metatype.plugins.values.MetaValueFactoryBuilder;
import org.jboss.test.BaseTestCase;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public abstract class AbstractTemplateTest extends BaseTestCase
{
   
   /** The deployment factory. */
   DeploymentTemplateInfoFactory factory = new DeploymentTemplateInfoFactory();

   /** The managed object factory. */
   ManagedObjectFactory mof = ManagedObjectFactoryBuilder.create();
   
   /** The meta value factory. */
   MetaValueFactory mvf = MetaValueFactoryBuilder.create();
   
   protected boolean debug = false;

   public AbstractTemplateTest(String name)
   {
      super(name);
   }

   public ManagedObjectFactory getMof()
   {
      return mof;
   }
   
   public MetaValueFactory getMvf()
   {
      return mvf;
   }
   
   public DeploymentTemplateInfoFactory getFactory()
   {
      return factory;
   }
   
   public boolean isDebug()
   {
      return debug;
   }
   
   public void setDebug(boolean debug)
   {
      this.debug = debug;
   }

   abstract DeploymentTemplateInfo createDeploymentInfo(String name, Class<?> clazz) throws Exception;
   
   protected Collection<String> getExcludes()
   {
      return Collections.emptySet();
   }
   
   protected DeploymentTemplateInfo assertTemplate(String name, Class<?> clazz) throws Exception
   {
      DeploymentTemplateInfo info = createDeploymentInfo(name, clazz);
      assertNotNull(info);
      
      ManagedObject mo = mof.createManagedObject(clazz);
      assertNotNull(mo);

      log.debug(">> " + info.getName());
      assertTemplateInfo(info, mo);      
      
      return info;
   }

   protected void assertTemplateInfo(DeploymentTemplateInfo info, ManagedObject mo)
   {
      List<String> processed = new ArrayList<String>();
      for(ManagedProperty property : info.getProperties().values())
      {
         //
         String propertyName = property.getName();

         // exclude
         if(getExcludes().contains(propertyName))
            continue;
         //
         ManagedProperty other = mo.getProperty(propertyName);
         assertProperty(propertyName , property, other);
         
         log.debug("property: " + propertyName);
         
      }

      for(ManagedProperty other : info.getProperties().values())
      {
         String otherName = other.getName();
         if(processed.contains(otherName))
            continue;
         
         // exclude
         if(getExcludes().contains(otherName))
            continue;
         
         ManagedProperty reference = mo.getProperty(otherName);
         if(isDebug() && reference == null)
         {
            log.debug("Does not exist in runtime MO: " + otherName);
            continue;
         }
         
         assertNotNull(otherName + " is included in the MO", reference);
         
         ManagementProperty annotation = null;
         if(reference.getAnnotations() != null)
         {
            annotation = (ManagementProperty) reference.getAnnotations().get(ManagementProperty.class.getName());
         }
         else
         {
            PropertyInfo propertyInfo = reference.getField(Fields.PROPERTY_INFO, PropertyInfo.class);
            annotation = (ManagementProperty) propertyInfo.getAnnotation(ManagementProperty.class.getName());
         }
         if(isDebug() && annotation == null)
         {
            log.debug("@ManagedProperty not present: " + otherName);
            continue;
         }
         assertNotNull(otherName + " annotation present", annotation);
         
         if(isDebug() && annotation.includeInTemplate() == false)
         {
            log.error("includeInTemplate == true " + otherName);
            continue;
         }

         assertTrue(otherName + " includeInTemplate", annotation.includeInTemplate());
         assertProperty(otherName, other, reference);
      }      
   }
   
   protected void assertProperty(String name, ManagedProperty property, ManagedProperty other)
   {
      assertNotNull(name, property);
      assertNotNull(name, other);
      
      assertEquals(name, property.getMetaType(), other.getMetaType());
      assertEquals(name, property.isMandatory(), other.isMandatory());
   }
   
   protected void assertProperty(ManagedProperty property, MetaType metaType, Serializable value)
   {
      assertProperty(property, metaType, mvf.create(value));
   }
   
   protected void assertProperty(ManagedProperty property, MetaType metaType, MetaValue metaValue)
   {
      assertNotNull(property);
      assertEquals(property.getMetaType(), metaType);
      assertEquals(property.getValue(), metaValue);
   }

}

