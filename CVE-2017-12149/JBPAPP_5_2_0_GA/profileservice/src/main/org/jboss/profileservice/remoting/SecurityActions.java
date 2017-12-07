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
package org.jboss.profileservice.remoting;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.jboss.security.ISecurityManagement;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;
 
/**
 *  Privileged Blocks
 *  @author Anil.Saldhana@redhat.com
 *  @author Scott.Stark@jboss.org 
 *  @version $Revision: 85526 $
 */
class SecurityActions
{
   static SecurityContext createAndSetSecurityContext(final String domain) throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<SecurityContext>()
      { 
         public SecurityContext run() throws Exception
         {
            SecurityContext sc =  SecurityContextFactory.createSecurityContext(domain); 
            setSecurityContext(sc);
            return sc;
         }}
      );
   }

   static void setSecurityContext(final SecurityContext sc)
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>()
      { 
         public Object run()
         {
            SecurityContextAssociation.setSecurityContext(sc);
            return null;
         }}
      );
   }
   static void setSecurityManagement(final SecurityContext sc, final ISecurityManagement sm)
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>()
      { 
         public Object run()
         {
            sc.setSecurityManagement(sm);
            return null;
         }}
      );
   }
   static String trace(final SecurityContext sc)
   {
      return AccessController.doPrivileged(new PrivilegedAction<String>()
      { 
         public String run()
         {
            StringBuilder sb = new StringBuilder();
            sb.append(" Principal = " + sc.getUtil().getUserPrincipal());
            sb.append(" Subject:"+sc.getUtil().getSubject());
            sb.append(" Incoming run as:"+sc.getIncomingRunAs());
            sb.append(" Outgoing run as:"+sc.getOutgoingRunAs());
            return sb.toString();
         }
      }
      );
   }
}
