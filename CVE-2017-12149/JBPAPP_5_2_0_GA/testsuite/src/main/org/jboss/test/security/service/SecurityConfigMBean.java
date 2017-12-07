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
package org.jboss.test.security.service;

import org.jboss.system.ServiceMBean;

/** An mbean interface for a config service that pushes an xml based
 javax.security.auth.login.Configuration onto the config stack managed by
 the mbean whose name is given by the SecurityConfigName attribute.

 @see org.jboss.security.plugins.SecurityConfigMBean

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public interface SecurityConfigMBean extends ServiceMBean
{
   /** Get the classpath resource name of the security configuration file */
   public String getAuthConfig();
   /** Set the classpath resource name of the security configuration file */
   public void setAuthConfig(String configURL);
   /** Get the name of the SecurityConfig mbean whose pushLoginConfig and
    popLoginConfig ops will be used to install and remove the xml login config*/
   public String getSecurityConfigName();
   /** Set the name of the SecurityConfig mbean whose pushLoginConfig and
    popLoginConfig ops will be used to install and remove the xml login config*/
   public void setSecurityConfigName(String objectName);
}
