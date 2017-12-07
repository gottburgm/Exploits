/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.profileservice.management.matchers;

import java.io.Serializable;

import org.jboss.deployers.spi.management.NameMatcher;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.ArrayValue;
import org.jboss.metatype.api.values.SimpleValue;

/**
 * A NameMatcher that matches against a component alias property values in
 * addition to the component name.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85526 $
 */
public class AliasMatcher implements NameMatcher<ManagedComponent>
   , Serializable
{
   private static final long serialVersionUID = 1;
   private String propertyName;

   public AliasMatcher()
   {
      this("alias");
   }
   public AliasMatcher(String propertyName)
   {
      this.propertyName = propertyName;
   }

   public String getPropertyName()
   {
      return propertyName;
   }
   public void setPropertyName(String propertyName)
   {
      this.propertyName = propertyName;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.spi.management.NameMatcher#matches(java.lang.Object, java.lang.String)
    */
   public boolean matches(ManagedComponent comp, String name)
   {
      boolean matches = comp.getName().equals(name);
      if(matches == false)
      {
         // Look for an alias property
         ManagedProperty prop = comp.getProperty(propertyName);
         if(prop != null)
         {
            MetaType type = prop.getMetaType();
            if(type.isSimple())
            {
               SimpleValue value = (SimpleValue) prop.getValue();
               String n = value.getValue().toString();
               matches = name.equals(n);
            }
            else if(type.isArray())
            {
               ArrayValue value = (ArrayValue) prop.getValue();
               for(Object n : value)
               {
                  if(name.equals(n.toString()))
                  {
                     matches = true;
                     break;
                  }
               }
            }
         }
      }
      return matches;
   }

}
