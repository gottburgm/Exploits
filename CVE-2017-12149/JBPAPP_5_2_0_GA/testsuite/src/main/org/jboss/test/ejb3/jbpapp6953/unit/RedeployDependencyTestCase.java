/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2011, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @authors tag. See the copyright.txt in the
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
package org.jboss.test.ejb3.jbpapp6953.unit;

import junit.framework.Test;
import org.jboss.test.ejb3.common.EJB3TestCase;
import org.jboss.test.ejb3.jbpapp6953.Hello;

import javax.naming.NameNotFoundException;

/**
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class RedeployDependencyTestCase extends EJB3TestCase
{
   public RedeployDependencyTestCase(String name)
   {
      super(name);
   }

   public void testRedeploy() throws Exception
   {
      // sanity check
      lookup("jbpapp6953/HelloBean/remote", Hello.class);

      redeploy("jbpapp6953-ejb2.jar");

      // if the lookup works, we're good to go
      try
      {
         lookup("jbpapp6953/HelloBean/remote", Hello.class);
      }
      catch (NameNotFoundException e)
      {
         fail("Failed to find jbpapp6953/HelloBean/remote after redeploy");
      }
   }


   public static Test suite() throws Exception
   {
      return getDeploySetup(RedeployDependencyTestCase.class, "jbpapp6953-ejb2.jar,jbpapp6953.ear");
   }
}
