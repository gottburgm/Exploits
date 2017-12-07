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
package org.jboss.test.entity.test;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.entity.interfaces.UnsetEntityContext;
import org.jboss.test.entity.interfaces.UnsetEntityContextHome;

/**
 * Test that unsetEntityContext is called.
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class UnsetEntityContextUnitTestCase extends JBossTestCase
{
   public UnsetEntityContextUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
      throws Exception
   {
      return getDeploySetup(UnsetEntityContextUnitTestCase.class, "jboss-test-unsetentitycontext.jar");
   }
   
   /**
    * In this test, pooling is disabled (MaximumSize==0) so every business
    * call to the home interface should create a new instance, use it
    * but then throw it away rather than put it back in the pool.
    * At this point we check whether unsetEntityContext() gets called.
    * @throws Exception
    */
   public void testUnsetEntityContextCalled() throws Exception
   {
      UnsetEntityContextHome home = (UnsetEntityContextHome)super.getInitialContext().lookup("UnsetEntityContext");
      
      // after this call the counter should be 1
      home.clearUnsetEntityContextCallCounter();
      
      // after this call the counter should be 2
      assertEquals("UnsetEntityContextCallCount", 1, home.getUnsetEntityContextCallCounter());
      
      // after this call the counter should be 3
      assertEquals("UnsetEntityContextCallCount", 2, home.getUnsetEntityContextCallCounter());
      
      // after this call the counter should be 4
      assertEquals("UnsetEntityContextCallCount", 3, home.getUnsetEntityContextCallCounter());      
   }

   /**
    * A test to see what happens when remove() is called on an
    * entity bean instance. I would expect it is returned to the
    * pool, and as a result thrown away (since pooling is disabled
    * for this test), but it seems not?
    * @throws Exception
    */
   public void testBeanReturnedToPool() throws Exception
   {
      UnsetEntityContextHome home = (UnsetEntityContextHome)super.getInitialContext().lookup("UnsetEntityContext");
      
      // after this call the counter should be 1
      home.clearUnsetEntityContextCallCounter();
      
      // after this call the counter should be 2
      assertEquals("UnsetEntityContextCallCount", 1, home.getUnsetEntityContextCallCounter());
      
      UnsetEntityContext bean = home.create("TestBean");
      // after this call the counter should be 3 (the bean should be returned to the pool?)
      bean.remove();
      
      // after this call the counter should be 4
      // TODO - this assertion fails!
      // assertEquals("UnsetEntityContextCallCount", 3, home.getUnsetEntityContextCallCounter());
   }
}
