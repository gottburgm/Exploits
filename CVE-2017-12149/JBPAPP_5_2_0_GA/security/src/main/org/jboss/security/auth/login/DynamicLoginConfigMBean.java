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
package org.jboss.security.auth.login;

import javax.management.ObjectName;

import org.jboss.system.ServiceMBean;
import org.jboss.security.config.PolicyConfig;

/** The management interface for the DynamicLoginConfig service.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public interface DynamicLoginConfigMBean
   extends ServiceMBean
{
   /**
    * Get the embedded PolicyConfig
    * @return the PolicyConfig if it was specified, null otherwise.
    */ 
   public PolicyConfig getPolicyConfig();
   /**
    * Set the embedded PolicyConfig
    * @param config - the embedded PolicyConfig object
    */ 
   public void setPolicyConfig(PolicyConfig config);

   /** Get the resource path to the JAAS login configuration file to use.
    */
   public String getAuthConfig();
   /** Set the resource path to the JAAS login configuration file to use.
    * @param authConf - the classpath resource to load.
    */
   public void setAuthConfig(String authConf);

   /** Get the XMLLoginConfig service to use for loading.
    * @return the XMLLoginConfig service name.
    */ 
   public ObjectName getLoginConfigService();
   /** Get the XMLLoginConfig service to use for loading. This service must
    * support a String[] loadConfig(URL) operation to load the configurations. 
    * 
    * @param serviceName - the XMLLoginConfig service name.
    */ 
   public void setLoginConfigService(ObjectName serviceName);

   /** Flush the caches of the security domains that have been registered
    * by this service.
    * @throws Exception
    */ 
   public void flushAuthenticationCaches() throws Exception;

   /** Get the SecurityManagerService used to flush the registered security
    * domains.
    * @return the SecurityManagerService service name.
    */
   public ObjectName getSecurityManagerService();

   /** Set the SecurityManagerService used to flush the registered security
    * domains. This service must support an flushAuthenticationCache(String)
    * operation to flush the case for the argument security domain. Setting
    * this triggers the flush of the authentication caches when the service
    * is stopped.
    * @param serviceName - the SecurityManagerService service name.
    */
   public void setSecurityManagerService(ObjectName serviceName);

}
