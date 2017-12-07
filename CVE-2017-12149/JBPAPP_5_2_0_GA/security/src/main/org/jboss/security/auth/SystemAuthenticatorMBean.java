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
package org.jboss.security.auth;

import org.jboss.system.ServiceMBean;

/** An MBean that requires a JAAS login in order for it to startup. This 
 * cam be used to require a login to startup the JBoss server.
 *
 * @version $Revision: 85945 $
 * @author Scott.Stark@jboss.org
 */
public interface SystemAuthenticatorMBean extends ServiceMBean
{
   /** Get the name of the security domain used for authentication
    */
   public String getSecurityDomain();
   /** Set the name of the security domain used for authentication
    */
   public void setSecurityDomain(String name);

   /** Get the CallbackHandler to use to obtain the authentication
    information.
    @see javax.security.auth.callback.CallbackHandler
    */
   public Class getCallbackHandler();
   /** Specify the CallbackHandler to use to obtain the authentication
    information.
    @see javax.security.auth.callback.CallbackHandler
    */
   public void setCallbackHandler(Class callbackHandlerClass)
      throws InstantiationException, IllegalAccessException;
}
