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
package org.jboss.test.jbossmx.implementation.server.support;

import java.io.IOException;
import java.io.*;
import java.rmi.MarshalledObject;

import org.jboss.logging.Logger;

/** The ContextCL standard MBean implementation.
 *
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class ContextCL implements ContextCLMBean
{
   private static Logger log = Logger.getLogger(ContextCL.class);
   private TestData data0;

   /** The TestData.class ClassLoader will be the ClassLoader of the ContextCL
    *mbean because we have a static reference to the TestData class. This
    *causes the VM to call the ClassLoader.loadClassInternal method.
    */
   public ContextCL() throws IOException
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      log.info("ContextCL ClassLoader: "+getClass().getClassLoader());
      log.info("ctor Context ClassLoader: "+cl);
      data0 = new TestData();
      log.info("TestData.class ProtectionDomain: "+TestData.class.getProtectionDomain());
   }

   /** An operation that load the TestData class using the current thread
    *context class loader (TCL) and the Class.forName(String, boolean, ClassLoader)
    *operation to validate that the class loader used to load TestData in
    *the ctor is compatible with the operation TCL.
    */
   public void useTestData() throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      log.info("useTestData ClassLoader: "+cl);
      Class c0 = data0.getClass();
      log.info("TestData #0 ProtectionDomain: "+c0.getProtectionDomain());
      Class c1 = Class.forName("org.jboss.test.jbossmx.implementation.server.support.TestData",
         false, cl);
      log.info("TestData #1 ProtectionDomain: "+c1.getProtectionDomain());
      if( c1.isInstance(data0) == false )
      {
         log.error("Assertion failed: data0 is NOT compatible with c1");
         throw new IllegalStateException("data0 is NOT compatible with c1");
      }
   }
}
