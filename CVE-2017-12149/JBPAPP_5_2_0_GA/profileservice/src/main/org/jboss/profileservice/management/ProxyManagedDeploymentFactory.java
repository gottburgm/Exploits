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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jboss.profileservice.spi.ManagedMBeanDeploymentFactory;

/**
 * A ManagedDeploymentFactory that acts as a facade on top of mbean deployments
 * given by an ObjectName pattern.
 *
 * @author Scott.Stark@jboss.org
 * @author Jason T. Greene
 * @version $Revision: 89854 $
 */
public class ProxyManagedDeploymentFactory implements ManagedMBeanDeploymentFactory
{
   /** Name under which the factory will be registered */
   private String factoryName;

   /** The map of patterns to locate root mbeans that go into the deployment ManagedObject set */
   private Set<ProxyDeploymentInfo> rootMOPatterns;

   /** The default view to expose fields as */
   private String defaultViewUse;

   /** The global property to meta-mapper configuration */
   private Map<String, String> propertyMetaMappings;

   public String getFactoryName()
   {
      return factoryName;
   }
   public void setFactoryName(String factoryName)
   {
      this.factoryName = factoryName;
   }

   public Set<ProxyDeploymentInfo> getRootMOPatterns()
   {
      return rootMOPatterns;
   }
   public void setRootMOPatterns(Set<ProxyDeploymentInfo> rootMOPatterns)
   {
      this.rootMOPatterns = rootMOPatterns;
   }

   public String getDefaultViewUse()
   {
      return this.defaultViewUse;
   }

   public void setDefaultViewUse(String defaultViewUse)
   {
      this.defaultViewUse = defaultViewUse;
   }

   public Map<String, String> getPropertyMetaMappings()
   {
      return propertyMetaMappings;
   }

   public void setPropertyMetaMappings(Map<String, String> propertyMappings)
   {
      this.propertyMetaMappings = propertyMappings;
   }



   public Collection<MBeanDeployment> getDeployments(MBeanServer mbeanServer)
   {
      Map<String, MBeanDeployment> tmp = new HashMap<String, MBeanDeployment>();
      if(this.rootMOPatterns == null)
         return tmp.values();

      for(ProxyDeploymentInfo info : rootMOPatterns)
      {
      Set<ObjectName> names = mbeanServer.queryNames(info.getPattern(), null);
      if(names != null)
      {
         for(ObjectName name : names)
         {
            if (hasExcludedNameKey(info, name))
               continue;

            String dname = info.getNameBuilder().getName(name, mbeanServer);
            MBeanDeployment deployment = tmp.get(dname);
            if(deployment == null)
            {
               deployment = new MBeanDeployment(dname);
               tmp.put(dname, deployment);
            }
            String compType = info.getCompType();
            String compSubtype = null;

            if (info.getSubtypeAttribute() != null)
            {
               try
               {
                  compSubtype = (String) mbeanServer.getAttribute(name, info.getSubtypeAttribute());
               }
               catch (Exception e)
               {
                  // EAT
               }
            }

            if (compSubtype == null)
               compSubtype = info.getCompSubtype();


            MBeanComponent rootComp = new MBeanComponent(name, compType, compSubtype);
            deployment.addComponent(rootComp);
            Map<String, String> componentInfo = info.getComponentInfo();
            if(componentInfo != null)
            {
               for(Map.Entry<String, String> comp : componentInfo.entrySet())
               {
                  String compPropertyName = comp.getKey();
                  String compTypeInfo = comp.getValue();
                  try
                  {
                     // Query for the attribute containing names of child components
                     Object attribute = mbeanServer.getAttribute(name, compPropertyName);
                     // Type:Subtype is the format
                     String[] compTypeParts = compTypeInfo.split(":");
                     processComponents(attribute, compTypeParts[0], compTypeParts[1], deployment);
                  }
                  catch(Exception e)
                  {
                     // EAT
                  }
               }
            }
         }
      }
      }
      return tmp.values();
   }

   private boolean hasExcludedNameKey(ProxyDeploymentInfo info, ObjectName name)
   {
      Set<String> excludedKeys = info.getExcludedKeys();
      for (Object key : name.getKeyPropertyList().keySet())
      {
         if (excludedKeys.contains(key))
            return true;
      }

      return false;
   }
   /**
    * Generate MBeanComponents for each name given by the attribute value. This
    * processes the attribute value as an array or collection of strings or
    * ObjectNames referencing other mbeans that are child type of components.
    * @param attribute - the value containing the names of child components
    * @param type - the child component type
    * @param subtype - the child component subtype
    * @param deployment - the deployment to add the component to
    * @throws Exception - thrown on failure to parse a component name
    */
   private void processComponents(Object attribute, String type, String subtype, MBeanDeployment deployment)
      throws Exception
   {
      if(attribute instanceof String[])
      {
         String[] names = (String[]) attribute;
         for(String name : names)
         {
            ObjectName compName = new ObjectName(name);
            MBeanComponent comp = new MBeanComponent(compName, type , subtype);
            deployment.addComponent(comp);
         }
      }
      else if(attribute instanceof ObjectName[])
      {
         ObjectName[] names = (ObjectName[]) attribute;
         for(ObjectName compName : names)
         {
            MBeanComponent comp = new MBeanComponent(compName, type , subtype);
            deployment.addComponent(comp);
         }
      }
      else if(attribute instanceof Collection)
      {
         Collection names = (Collection) attribute;
         for(Object name : names)
         {
            MBeanComponent comp = null;
            ObjectName compName = null;
            if(name instanceof ObjectName)
            {
               compName = (ObjectName) name;
            }
            else
            {
               compName = new ObjectName(name.toString());

            }
            comp = new MBeanComponent(compName, type , subtype);
            deployment.addComponent(comp);
         }
      }
   }


}
