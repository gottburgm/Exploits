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

import javax.xml.namespace.QName;

import org.jboss.xb.binding.GenericValueContainer;

/**
 * ModuleOption declares a constructor that takes name as a parameter while the
 * value should be set with the setter. This use-case is not supported
 * out-of-the-box by jbxb, so, we use this container.
 * 
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class ModuleOptionContainer
   implements GenericValueContainer
{
   private String name;
   private Object value;


   /**
    @return - the option value
    */
   public Object getValue()
   {
      return value;
   }

   /**
    Setter used when the module option is passed as the text body of the
    module-option element.

    @param value - text value
    */
   public void setValue(Object value)
   {
      this.value = value;
   }

   /**
    Add attributes or nested element content.

    @param name - the attribute or element name  
    @param value - the attribute or element value
    */
   public void addChild(QName name, Object value)
   {
      if("name".equals(name.getLocalPart()))
      {
         this.name = (String)value;
      }
      else
      {
         this.value = value;
      }
   }

   public Object instantiate()
   {
      ModuleOption option = new ModuleOption(name);
      option.setValue(value);
      return option;
   }

   public Class getTargetClass()
   {
      return ModuleOption.class;
   }
}
