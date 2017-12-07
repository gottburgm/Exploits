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
package org.jboss.security.plugins;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;

import org.jboss.system.ServiceMBean;

/** A security configuration MBean. This establishes the JAAS and Java2
 security properties and related configuration.

 @see DefaultLoginConfig
 @see javax.security.auth.login.Configuration

@author Scott.Stark@jboss.org
@version $Revision: 85945 $
*/
public interface SecurityConfigMBean extends ServiceMBean
{
   /** Get the name of the mbean that provides the default JAAS login configuration */
   public String getLoginConfig();
   /** Set the name of the mbean that provides the default JAAS login configuration */
   public void setLoginConfig(String objectName) throws MalformedObjectNameException;
   /** Push an mbean onto the login configuration stack and install its
    Configuration as the current instance.
    @see javax.security.auth.login.Configuration
    */
   public void pushLoginConfig(String objectName) throws JMException, MalformedObjectNameException;
   /** Pop the current mbean from the login configuration stack and install
    the previous Configuration as the current instance.
    @see javax.security.auth.login.Configuration
    */
   public void popLoginConfig() throws JMException;

}
