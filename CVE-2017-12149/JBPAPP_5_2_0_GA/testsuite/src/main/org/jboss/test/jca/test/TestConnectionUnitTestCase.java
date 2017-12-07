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
package org.jboss.test.jca.test;

import java.util.ArrayList;

import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * Unit test for the TestConnection method
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class TestConnectionUnitTestCase extends JBossTestCase
{
   private Logger log = Logger.getLogger(getClass());

   /**
    * Creates a new <code>TestConnectionUnitTestCase</code> instance.
    * @param name test name
    */
   public TestConnectionUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testSuccessfulCall() throws Exception
   {
      try
      {
         ObjectName on = new ObjectName("jboss.jca:name=DefaultDS,service=ManagedConnectionPool");

         Object result = getServer().invoke(on, "testConnection", null, null);

         assertNotNull(result);
         assertTrue(result instanceof Boolean);

         Boolean b = (Boolean)result;

         assertTrue(b.booleanValue());
      }
      finally
      {
      }
   }
}
