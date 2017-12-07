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
package org.jboss.profileservice.management;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;

import org.jboss.managed.api.ComponentType;
import org.jboss.profileservice.spi.MBeanDeploymentNameBuilder;

/**
 * Encapsulation of a collection of mbeans that should be exposed as a
 * ManagedDeployment with ManagedComponents.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 89854 $
 */
public class ProxyDeploymentInfo
{
   /** The root component type */
   private String compType;
   /** The root component subtype */
   private String compSubtype;
   /** The name pattern used to locate the root mbeans of the deployment */
   private ObjectName pattern;
   private MBeanDeploymentNameBuilder nameBuilder;

   /** A map of attribute name to ComponentType:ComponentSubType for child components of the root component */
   private Map<String, String> componentInfo;

   /** An attribute of the mdb used to build the subtype */
   private String subtypeAttribute;

   /** Excluded ObjectName keys */
   private Set<String> excludedKeys = Collections.emptySet();

   public String getCompType()
   {
      return compType;
   }
   public void setCompType(String compType)
   {
      this.compType = compType;
   }
   public String getCompSubtype()
   {
      return compSubtype;
   }
   public void setCompSubtype(String compSubtype)
   {
      this.compSubtype = compSubtype;
   }

   public String getSubtypeAttribute()
   {
      return this.subtypeAttribute;
   }

   public void setSubtypeAttribute(String subtypeAttribute)
   {
      this.subtypeAttribute = subtypeAttribute;
   }

   public Set<String> getExcludedKeys()
   {
      return this.excludedKeys;
   }

   public void setExcludedKeys(Set<String> excludedKeys)
   {
      this.excludedKeys = excludedKeys;
   }
   public ComponentType getType()
   {
      return new ComponentType(compType, compSubtype);
   }
   public ObjectName getPattern()
   {
      return pattern;
   }
   public void setPattern(ObjectName pattern)
   {
      this.pattern = pattern;
   }

   public MBeanDeploymentNameBuilder getNameBuilder()
   {
      return nameBuilder;
   }
   public void setNameBuilder(MBeanDeploymentNameBuilder nameBuilder)
   {
      this.nameBuilder = nameBuilder;
   }
   public Map<String, String> getComponentInfo()
   {
      return componentInfo;
   }
   public void setComponentInfo(Map<String, String> componentInfo)
   {
      this.componentInfo = componentInfo;
   }
}
