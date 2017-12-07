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
package org.jboss.web.tomcat.security;

import java.security.acl.Group;

import javax.security.auth.login.LoginException;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;

import org.jboss.security.auth.spi.UsernamePasswordLoginModule;

/**
 * An abstract subclass of UsernamePasswordLoginModule that makes the
 * HttpServletRequest from the client attempting to login available to the Login
 * Module.
 * 
 * You could invoke the getHttpServletRequest() inside your getUsersPassword()
 * method implementation, allowing you to access information from the
 * HttpServletRequest from the client, to perform things like denying access to
 * certain IP addresses, or to disallow a maximun number of login retries per IP
 * address, inserting attempts into a database.
 * 
 * @see #getHttpServletRequest
 * 
 * @author Ricardo Arguello (ricardoarguello@users.sourceforge.net)
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81037 $
 */
public abstract class HttpServletRequestLoginModule extends
      UsernamePasswordLoginModule
{
   /** Client's HttpServletRequest. */
   protected HttpServletRequest request;

   /**
    * Obtains the HttpServletRequest of the user attempting to authenticate
    * using the JACC HttpServletRequest policy context handler.
    *
    * You could use this information to deny access when a number of login
    * retries per IP address has been attempted.
    * 
    * @return the IP address of the user attempting to authenticate.
    */
   protected HttpServletRequest getHttpServletRequest()
      throws PolicyContextException
   {
      request = (HttpServletRequest) PolicyContext.getContext("javax.servlet.http.HttpServletRequest");
      return request;
   }

   /**
    * Get the expected password for the current username available via the
    * getUsername() method. This is called from within the login() method after
    * the CallbackHandler has returned the username and candidate password.
    * <p>
    * You could use getHttpServletRequest() inside this method.
    * 
    * @see org.jboss.security.auth.spi.UsernamePasswordLoginModule#getUsersPassword()
    * 
    * @return the valid password String
    */
   protected abstract String getUsersPassword() throws LoginException;

   /**
    * @see org.jboss.security.auth.spi.AbstractServerLoginModule#getRoleSets()
    */
   protected abstract Group[] getRoleSets() throws LoginException;
}
