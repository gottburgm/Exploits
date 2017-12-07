/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.profileservice.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.annotation.ViewUse;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision:$
 */
public interface ManagedMBeanDeploymentFactory
{
   public static class MBeanDeployment
   {
      String name;
      Collection <MBeanComponent> components;
      public MBeanDeployment(String name)
      {
         this(name, new ArrayList<MBeanComponent>());
      }
      public MBeanDeployment(String name, Collection <MBeanComponent> components)
      {
         this.name = name;
         this.components = components;
      }
      public String getName()
      {
         return name;
      }
      public Collection<MBeanComponent> getComponents()
      {
         return components;
      }
      public void setComponents(Collection<MBeanComponent> components)
      {
         this.components = components;
      }
      public synchronized void addComponent(MBeanComponent comp)
      {
         if(components == null)
            components = new ArrayList<MBeanComponent>();
         components.add(comp);
      }
      public String toString()
      {
         return "MBeanDeployment("+name+"), "+components;
      }
   }
   public static class MBeanComponent
   {
      String type;
      String subtype;
      ObjectName name;
      public MBeanComponent(ObjectName name, String type, String subtype)
      {
         super();
         this.type = type;
         this.subtype = subtype;
         this.name = name;
      }
      public ObjectName getName()
      {
         return name;
      }
      public String getType()
      {
         return type;
      }
      public String getSubtype()
      {
         return subtype;
      }
      public String toString()
      {
         return "MBeanComponent("+name+","+type+","+subtype+")";
      }
   }
   public String getFactoryName();
   public Collection<MBeanDeployment> getDeployments(MBeanServer mbeanServer);
   public String getDefaultViewUse();
   public Map<String, String> getPropertyMetaMappings();
}
