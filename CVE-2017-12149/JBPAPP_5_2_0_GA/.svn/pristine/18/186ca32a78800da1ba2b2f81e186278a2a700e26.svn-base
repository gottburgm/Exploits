/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ejb3.jbpapp2473.unit;

import junit.framework.Test;

import org.jboss.test.ejb3.common.EJB3TestCase;
import org.jboss.test.ejb3.jbpapp2473.WhoAmI;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class RunAsUnitTestCase extends EJB3TestCase
{
   public RunAsUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testRole1() throws Exception
   {
      WhoAmI bean = lookup("RunAsManagerBean/remote", WhoAmI.class);
      
      assertTrue("RunAsManagerBean specifies a RunAs 'manager' role", bean.isCallerInRole("manager"));
   }
   
   public void testRole2() throws Exception
   {
      WhoAmI bean = lookup("RunAsPrincipalBean/remote", WhoAmI.class);
      
      assertTrue("RunAsPrincipalBean specifies a RunAs 'manager' role", bean.isCallerInRole("manager"));
   }
   
   public void testRunAsManager() throws Exception
   {
      WhoAmI bean = lookup("RunAsManagerBean/remote", WhoAmI.class);
      
      String caller = bean.getCallerPrincipal();
      // TODO: note that there is a bug in RunAsIdentity which hard-codes anonymous
      assertEquals("RunAsManagerBean doesn't override the principal", "anonymous", caller);
   }
   
   public void testRunAsPrincipal() throws Exception
   {
      WhoAmI bean = lookup("RunAsPrincipalBean/remote", WhoAmI.class);
      
      String caller = bean.getCallerPrincipal();
      assertEquals("RunAsPrincipalBean overrides the principal", "jbpapp2473", caller);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(RunAsUnitTestCase.class, "jbpapp2473.jar");
   }
}
