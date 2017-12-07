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
package org.jboss.deployment.plugin;

import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.DefaultJndiBindingPolicy;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.EjbDeploymentSummary;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.KnownInterfaces;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.KnownInterfaces.KnownInterfaceType;

/**
 * DefaultJndiBindingPolicy that uses the old ejb3 binding name conventions
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class LegacyEjb3JndiPolicy
   implements DefaultJndiBindingPolicy
{
   private static Logger log = Logger.getLogger(LegacyEjb3JndiPolicy.class);
   private String baseJndiName;

   public LegacyEjb3JndiPolicy()
   {
   }
   public LegacyEjb3JndiPolicy(String baseJndiName)
   {
      this.baseJndiName = baseJndiName;
   }
   
   public String getBaseJndiName()
   {
      return baseJndiName;
   }
   public void setBaseJndiName(String baseJndiName)
   {
      this.baseJndiName = baseJndiName;
   }

   public String getDefaultLocalHomeJndiName(EjbDeploymentSummary summary)
   {
      return baseJndiName + "/" + KnownInterfaces.LOCAL_HOME;
   }

   public String getDefaultLocalJndiName(EjbDeploymentSummary summary)
   {
      return baseJndiName + "/" + KnownInterfaces.LOCAL;
   }

   public String getDefaultRemoteHomeJndiName(EjbDeploymentSummary summary)
   {
      return baseJndiName + "/" + KnownInterfaces.HOME;
   }

   public String getDefaultRemoteJndiName(EjbDeploymentSummary summary)
   {
      return baseJndiName + "/" + KnownInterfaces.REMOTE;
   }

   public String getJndiName(EjbDeploymentSummary summary)
   {
      return baseJndiName;
   }

   public String getJndiName(EjbDeploymentSummary summary, String iface,
         KnownInterfaceType ifaceType)
   {
      JBossEnterpriseBeanMetaData beanMD = summary.getBeanMD();
      baseJndiName = beanMD.getMappedName();
      if(baseJndiName == null)
         baseJndiName = beanMD.determineJndiName();
      String jndiName = null;
      String localJndiName = beanMD.getLocalJndiName();
      boolean is3x = beanMD.getJBossMetaData().isEJB3x();
      boolean hasJndiName = false;
      if(beanMD.isSession())
      {
         JBossSessionBeanMetaData sbeanMD = (JBossSessionBeanMetaData) beanMD;
         String givenJndiName = sbeanMD.getJndiName();
         if(givenJndiName != null && givenJndiName.trim().length() > 0)
            hasJndiName = true;
      }
      String appName = summary.getDeploymentScopeBaseName();
      if(appName == null)
         appName = "";
      else
         appName += "/";

      String ejbName = beanMD.getEjbName();
      switch(ifaceType)
      {
         case BUSINESS_LOCAL:
            if(localJndiName == null)
               jndiName = appName + ejbName + "/local";
            else
               jndiName = localJndiName;
            break;
         case BUSINESS_REMOTE:
            if(hasJndiName == false)
               jndiName = appName + ejbName + "/remote";
            else
               jndiName = baseJndiName;
            break;
         case LOCAL_HOME:
            if(is3x)
            {
               // TODO: Not really, hopefully no one uses this
               log.warn("Requested ejb3 local home for bean: "+beanMD.getEjbName());
               jndiName = ejbName + "/localHome";
            }
            else
               jndiName = beanMD.determineLocalJndiName();
            break;
         case REMOTE_HOME:
            if(is3x)
            {
               if (!hasJndiName)
               {
                  jndiName = ejbName + "/home";
               }
               else
               {
                  jndiName = baseJndiName;
                  // ejb3-core 0.1.0 behavior
                  //jndiName = baseJndiName + "Home";
               }
            }
            else
            {
               jndiName = baseJndiName;
            }
            break;
         case UNKNOWN:
            if(iface != null)
               log.warn("UKNOWN iface seen: "+iface+", for bean: "+beanMD.getEjbName());
            jndiName = baseJndiName;
            break;
      }
      return jndiName;
   }

}
