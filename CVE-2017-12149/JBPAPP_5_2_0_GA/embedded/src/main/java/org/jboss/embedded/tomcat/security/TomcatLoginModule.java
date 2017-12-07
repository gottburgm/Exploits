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
package org.jboss.embedded.tomcat.security;

import java.security.Principal;
import java.security.acl.Group;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import org.jboss.security.auth.spi.AbstractServerLoginModule;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class TomcatLoginModule extends AbstractServerLoginModule
{
   static ThreadLocal<HttpServletRequest> request = new ThreadLocal<HttpServletRequest>();

   @Override
   public boolean login() throws LoginException
   {
      return super.login();    //To change body of overridden methods use File | Settings | File Templates.
   }

   protected Principal getIdentity()
   {
      return request.get().getUserPrincipal();
   }

   protected Group[] getRoleSets() throws LoginException
   {
      return new Group[0];
   }
}
