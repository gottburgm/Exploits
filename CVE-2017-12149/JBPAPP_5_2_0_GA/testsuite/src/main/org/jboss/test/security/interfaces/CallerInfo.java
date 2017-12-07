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
package org.jboss.test.security.interfaces;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

/**
 A representation of the expected principal identity and roles.

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class CallerInfo implements Serializable
{
   private static final long serialVersionUID = 1;

   /** The expected caller for non-run-as contexts */
   private Principal callerIdentity;
   /** The expected caller for run-as contexts */
   private Principal runAsIdentity;
   /** HashSet<String> expected role names for isCallerInRole for non-run-as contexts */
   private HashSet expectedCallerRoles = new HashSet();
   /** HashSet<String> expected role names for isCallerInRole for non-run-as contexts */
   private HashSet expectedRunAsRoles = new HashSet();

   public CallerInfo()
   {
      
   }
   public CallerInfo(Principal callerIdentity, Principal runAsIdentity,
      Set expectedCallerRoles, Set expectedRunAsRoles)
   {
      this.callerIdentity = callerIdentity;
      this.runAsIdentity = runAsIdentity;
      this.expectedCallerRoles.addAll(expectedCallerRoles);
      this.expectedRunAsRoles.addAll(expectedRunAsRoles);
   }

   public Principal getCallerIdentity()
   {
      return callerIdentity;
   }

   public void setCallerIdentity(Principal callerIdentity)
   {
      this.callerIdentity = callerIdentity;
   }

   public Principal getRunAsIdentity()
   {
      return runAsIdentity;
   }

   public void setRunAsIdentity(Principal runAsIdentity)
   {
      this.runAsIdentity = runAsIdentity;
   }

   public Set getExpectedCallerRoles()
   {
      return expectedCallerRoles;
   }

   public void setExpectedCallerRoles(Set expectedCallerRoles)
   {
      this.expectedCallerRoles.clear();
      this.expectedCallerRoles.addAll(expectedCallerRoles);
   }

   public Set getExpectedRunAsRoles()
   {
      return expectedRunAsRoles;
   }

   public void setExpectedRunAsRoles(Set expectedRunAsRoles)
   {
      this.expectedRunAsRoles.clear();
      this.expectedRunAsRoles.addAll(expectedRunAsRoles);
   }
}
