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

import org.jboss.aspects.asynch.AsynchProvider;
import org.jboss.aspects.asynch.AsynchRemoting;
import org.jboss.aspects.asynch.Future;
import org.jboss.remoting.InvokerLocator;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 80997 $
 */
public class POJO
{
   /**
    * @@org.jboss.aspects.asynch.Asynchronous
    */
   public int testMethod(int echo)
   {
      System.out.println("echo: " + echo);
      return echo;
   }

   /**
    * @@org.jboss.aspects.asynch.Asynchronous
    */
   public String testMethod(String echo)
   {
      System.out.println("ECHO: echo");
      return echo;
   }

   public void test() throws Exception
   {
      AsynchProvider asynch = (AsynchProvider) this;
      testMethod(5);

      Future future = asynch.getFuture();
      int rtn = ((Integer) future.get()).intValue();
      if (rtn != 5) throw new RuntimeException("integer return value invalid");

      testMethod("hello");

      future = asynch.getFuture();
      String srtn = (String) future.get();
      if (!"hello".equals(srtn)) throw new RuntimeException("string return value failed");


   }

   public void testCollocated() throws Exception
   {

      POJO pojo = (POJO) AsynchRemoting.createRemoteProxy("pojo", POJO.class, new InvokerLocator("socket://localhost:5150"));

      AsynchProvider asynch = (AsynchProvider) pojo;
      pojo.testMethod(5);

      Future future = asynch.getFuture();
      int rtn = ((Integer) future.get()).intValue();
      if (rtn != 5) throw new RuntimeException("integer return value invalid");

      pojo.testMethod("hello");

      future = asynch.getFuture();
      String srtn = (String) future.get();
      if (!"hello".equals(srtn)) throw new RuntimeException("string return value failed");

   }

}
