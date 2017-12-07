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
package org.jboss.test.aop.test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.test.JBossTestCase;
import org.jboss.test.aop.bean.NonadvisedPOJO;
import org.jboss.test.aop.bean.POJO;
import org.jboss.aop.proxy.ClassProxy;

/**
* Sample client for the jboss container.
*
* @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
* @version $Id: RemotingUnitTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
*/

public class RemotingUnitTestCase
   extends JBossTestCase
{
   Logger log = getLog();

   private ObjectName testerName = ObjectNameFactory.create("jboss.aop:name=RemotingTester");

   private Object[] noparams = {};
   private String[] nosig = {};

   public RemotingUnitTestCase(String name)
   {
      super(name);
   }

   public void testRemoting() throws Exception
   {
      MBeanServerConnection server = getServer();
      POJO pojo = (POJO)server.invoke(testerName, "testRemoting", noparams, nosig);
      try
      {
         ClassProxy proxy = (ClassProxy)pojo;
         String rtn = pojo.remoteTest(); // invokes remotely
         if (!rtn.equals("hello"))
            throw new Exception("DID NOT GET EXPECTED REMOTE VALUE");
      }
      finally
      {
         unregisterWithOid(server, "myobj");
      }
   }

   public void testNonadvisedRemoting() throws Exception
   {
      MBeanServerConnection server = getServer();
      NonadvisedPOJO pojo = (NonadvisedPOJO)server.invoke(testerName, "testNonadvisedRemoting", noparams, nosig);
      try
      {
         String rtn = pojo.remoteTest(); // invokes remotely
         if (!rtn.equals("hello"))
            throw new Exception("DID NOT GET EXPECTED REMOTE VALUE");
      }
      finally
      {
         unregisterWithOid(server, "myobj");
      }
   }

   public void testClusteredRemoting() throws Exception
   {
      MBeanServerConnection server = getServer();
      POJO pojo = (POJO)server.invoke(testerName, "testClusteredRemoting", noparams, nosig);
      try
      {
         String rtn = pojo.remoteTest(); // invokes remotely
         if (!rtn.equals("hello"))
            throw new Exception("DID NOT GET EXPECTED REMOTE VALUE");
         pojo.remoteTest();//just make sure we can invoke twice...
      }
      finally
      {
         unregisterTarget(server, pojo);
      }
   }

   public void testClusteredNonadvisedRemoting() throws Exception
   {
      MBeanServerConnection server = getServer();
      NonadvisedPOJO pojo = (NonadvisedPOJO)server.invoke(testerName, "testClusteredNonadvisedRemoting", noparams, nosig);
      try
      {
         String rtn = pojo.remoteTest(); // invokes remotely
         if (!rtn.equals("hello"))
            throw new Exception("DID NOT GET EXPECTED REMOTE VALUE");
      }
      finally
      {
         unregisterTarget(server, pojo);
      }
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(RemotingUnitTestCase.class));

      AOPTestSetup setup = new AOPTestSetup(suite, "aoptest.sar");
      return setup; 
   }

   protected void unregisterWithOid(MBeanServerConnection server, String oid) throws Exception
   {
      Object[] params = {oid};
      String[] sig = {"java.lang.String"};
      server.invoke(testerName, "unregisterNonClusteredObject", params, sig);
   }
   
   protected void unregisterTarget(MBeanServerConnection server, Object proxy)
      throws Exception
   {
      Object[] params = { proxy };
      String[] sig = { "java.lang.Object" };
      server.invoke(testerName, "unregisterTarget", params, sig);
   }

}
