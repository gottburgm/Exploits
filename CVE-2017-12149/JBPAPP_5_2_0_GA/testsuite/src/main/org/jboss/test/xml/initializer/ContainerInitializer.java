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
package org.jboss.test.xml.initializer;

import javax.xml.namespace.QName;

import org.jboss.xb.binding.metadata.ClassMetaData;
import org.jboss.xb.binding.metadata.PropertyMetaData;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultWildcardHandler;
import org.jboss.xb.binding.sunday.unmarshalling.ElementBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingInitializer;

/**
 * ContainerInitializer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class ContainerInitializer implements SchemaBindingInitializer
{
   public static final String NS = "dummy://www.jboss.org/container";

   private static final QName containerStrictQName = new QName(NS, "containerStrict");
   private static final QName containerLaxQName = new QName(NS, "containerLax");
   private static final QName containerSkipQName = new QName(NS, "containerSkip");

   private static final ContainerWildcardHandler wildcardHandler = new ContainerWildcardHandler();
   
   public SchemaBinding init(SchemaBinding schema)
   {
      ClassMetaData classMetaData = new ClassMetaData();
      classMetaData.setImpl(Container.class.getName());

      PropertyMetaData property = new PropertyMetaData();
      property.setName("value");
      
      ElementBinding containerStrict = schema.getElement(containerStrictQName);
      containerStrict.setClassMetaData(classMetaData);
      containerStrict.getType().getWildcard().setWildcardHandler(wildcardHandler);
      ElementBinding containerLax = schema.getElement(containerLaxQName);
      containerLax.setClassMetaData(classMetaData);
      containerStrict.getType().getWildcard().setWildcardHandler(wildcardHandler);
      ElementBinding containerSkip = schema.getElement(containerSkipQName);
      containerSkip.setClassMetaData(classMetaData);
      containerStrict.getType().getWildcard().setWildcardHandler(wildcardHandler);

      return schema;
   }
   
   private static class ContainerWildcardHandler extends DefaultWildcardHandler
   {
      public void setParent(Object parent, Object o, QName elementName, ElementBinding element, ElementBinding parentElement)
      {
         Container container = (Container) parent;
         container.setValue(o);
      }
   }
}
