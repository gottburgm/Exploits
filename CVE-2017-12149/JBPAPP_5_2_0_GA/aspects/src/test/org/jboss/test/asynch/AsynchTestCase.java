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
package org.jboss.test.asynch;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.aspects.asynch.AsynchProvider;
import org.jboss.aspects.asynch.Future;
import org.jboss.test.JBossTestCase;
import org.jboss.test.aop.test.AOPTestSetup;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 80997 $
 */
public class AsynchTestCase
extends JBossTestCase
{
   org.apache.log4j.Category log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public AsynchTestCase(String name)
   {

      super(name);

   }

   public void testRemote() throws Exception
   {
      try
      {
         POJO pojo = (POJO) getInitialContext().lookup("pojo");

         AsynchProvider asynch = (AsynchProvider) pojo;
         pojo.testMethod(5);

         Future future = asynch.getFuture();
         int rtn = ((Integer) future.get()).intValue();
         assertEquals(rtn, 5);

         pojo.testMethod("hello");

         future = asynch.getFuture();
         String srtn = (String) future.get();
         assertEquals("hello", srtn);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

   }

   public void testLocal() throws Exception
   {
      POJO pojo = (POJO) getInitialContext().lookup("pojo");

      pojo.test();
   }

   public void testCollocated() throws Exception
   {
      POJO pojo = (POJO) getInitialContext().lookup("pojo");

      pojo.testCollocated();
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(AsynchTestCase.class));

      AOPTestSetup setup = new AOPTestSetup(suite, "asynch-test.sar");
      return setup;
   }

}
