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
package org.jboss.test.kernel.deployment.jboss.test;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.kernel.deployment.jboss.beans.simplepojo.SimplePOJO;

/**
 * Kernel deployment tests.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class SimplePOJOUnitTestCase extends JBossTestCase
{
   public void testSimplePOJO() throws Exception
   {
      InitialContext ctx = new InitialContext();
      SimplePOJO pojo = (SimplePOJO) ctx.lookup("test/kernel/deployment/simplepojo");
      assertEquals("()", pojo.getConstructorUsed());
      assertEquals("Something", pojo.getSomething());
      assertEquals(true, pojo.created);
      assertEquals(true, pojo.started);
   }
   
   public SimplePOJOUnitTestCase(String test)
   {
      super(test);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(SimplePOJOUnitTestCase.class, "testkernel-simplepojo.beans");
   }
}
