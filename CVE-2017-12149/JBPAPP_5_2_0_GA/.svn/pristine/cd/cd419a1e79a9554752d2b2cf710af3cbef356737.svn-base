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
package org.jboss.test.spring.test;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.spring.ejb.SpringBeansManager;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class SpringEJBTestCase extends JBossTestCase
{
   public SpringEJBTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(SpringEJBTestCase.class, "spring-ear.ear");
   }

   public void testEar() throws Exception
   {
      InitialContext ctx = new InitialContext();
      String ejbName = "spring-ear/" + SpringBeansManager.class.getSimpleName() + "Impl/remote";
      log.info("Excuting lookup: " + ejbName);
      Object result = ctx.lookup(ejbName);
      assertNotNull(result);
      SpringBeansManager manager = (SpringBeansManager)PortableRemoteObject.narrow(result, SpringBeansManager.class);
      log.info("SpringBeansManager: " + manager);
      assertEquals(3, manager.add(1, 2));
      assertEquals(10, manager.multipy(2, 5));
      manager.log("Some log");
   }
}
