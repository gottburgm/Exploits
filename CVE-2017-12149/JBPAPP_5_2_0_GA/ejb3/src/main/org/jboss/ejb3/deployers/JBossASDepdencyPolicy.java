/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.deployers;

import java.util.HashSet;
import java.util.Set;

import org.jboss.beans.metadata.plugins.AbstractDemandMetaData;
import org.jboss.beans.metadata.plugins.AbstractSupplyMetaData;
import org.jboss.beans.metadata.spi.DemandMetaData;
import org.jboss.beans.metadata.spi.DependencyMetaData;
import org.jboss.beans.metadata.spi.SupplyMetaData;
import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.javaee.JavaEEComponent;
import org.jboss.ejb3.kernel.JNDIKernelRegistryPlugin;

/**
 * The implementation of the DependencyPolicy used by the jbossas ejb3
 * related deployers.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class JBossASDepdencyPolicy extends JBoss5DependencyPolicy
   implements DependencyPolicy
{
   private JavaEEComponent component;
   private Set<DependencyMetaData> dependencies = new HashSet<DependencyMetaData>();
   private Set<DemandMetaData> demands = new HashSet<DemandMetaData>();
   private Set<SupplyMetaData> supplies = new HashSet<SupplyMetaData>();

   public JBossASDepdencyPolicy(JavaEEComponent component)
   {
      super(component);
      this.component = component;
   }

   public void addDependency(String dependency)
   {
      addDependency(new AbstractDemandMetaData(dependency));
   }
   public void addDependency(DemandMetaData dependency)
   {
      demands.add(dependency);      
   }
   public void addDependency(DependencyMetaData dependency)
   {
      dependencies.add(dependency);      
   }

   public void addDependency(Class<?> businessInterface)
   {
      addDependency("Class:" + businessInterface.getName());
   }
   
   /**
    * Add a dependency on an enterprise bean.
    * 
    * Optionally the ejb link is prefixed with the path name to
    * another ejb-jar file separated with a '#' to the enterprise bean's name.
    * 
    * @param ejbLink        the name of the target enterprise bean
    * @param businessInterface
    */
   public void addDependency(String ejbLink, Class<?> businessInterface)
   {
      assert ejbLink != null : "ejbLink is null"; 
      
      // Note that businessInterface is always ignored during resolving.
      
      // FIXME: less hacky
      
      int hashIndex = ejbLink.indexOf('#');
      if (hashIndex != -1)
      {
         String unitName = ejbLink.substring(0, hashIndex);
         String ejbName = ejbLink.substring(hashIndex + 1);
         // Work around ejb2/3 container name mismatches by adding
         String ejb3Name = component.createObjectName(unitName, ejbName);
         String ejb2Name = "TODO...";
         String demand = null;
         AbstractDemandMetaData admd = new AbstractDemandMetaData(demand);
         admd.setTransformer("");
         //addDependency(admd);
      }
      else
      {
         //addDependency(new EjbLinkDemandMetaData(component, ejbLink));
      }
   }
   
   public void addJNDIName(String name)
   {
      assert name != null : "name is null";
      assert name.length() > 0 : "name is empty";
      
      addDependency(JNDIKernelRegistryPlugin.JNDI_DEPENDENCY_PREFIX + name);
   }
   
   public Set<DependencyMetaData> getDependencies()
   {
      return dependencies;
   }
   public Set<DemandMetaData> getDemands()
   {
      return demands;
   }
   
   public void addSupply(Class<?> businessInterface)
   {
      supplies.add(new AbstractSupplyMetaData("Class:" + businessInterface.getName()));
   }
   
   public Set<SupplyMetaData> getSupplies()
   {
      return supplies;
   }
   public void addDatasource(String jndiName)
   {
      addDependency(createDataSourceKernelName(jndiName));
   }
   
   public static String createDataSourceKernelName(String jndiName)
   {
      String ds = jndiName;
      if (ds.startsWith("java:/"))
      {
         ds = ds.substring(6);

      }
      else if (ds.startsWith("java:"))
      {
         ds = ds.substring(5);
      }
      String onStr = "jboss.jca:name=" + ds + ",service=DataSourceBinding";
      return onStr;
   }
}
