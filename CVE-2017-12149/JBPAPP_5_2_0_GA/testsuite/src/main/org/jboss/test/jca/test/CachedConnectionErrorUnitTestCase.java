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

import java.rmi.RemoteException;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.resource.ResourceException;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.interfaces.UserTxSession;
import org.jboss.test.jca.interfaces.UserTxSessionHome;

/**
 * Abstract concurrent stress test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class CachedConnectionErrorUnitTestCase extends JBossTestCase
{
   public CachedConnectionErrorUnitTestCase (String name)
   {
      super(name);
   }

   protected void setError(Boolean value) throws Exception
   {
      ObjectName CCM = new ObjectName("jboss.jca:service=CachedConnectionManager");
      getServer().setAttribute(CCM, new Attribute("Error", value));
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      setError(Boolean.TRUE);
   }

   protected void tearDown() throws Exception
   {
      setError(Boolean.FALSE);
      super.tearDown();
   }

   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(CachedConnectionErrorUnitTestCase.class, "jcatest.jar");
      Test t2 = getDeploySetup(t1, "testadapter-ds.xml");
      return getDeploySetup(t2, "jbosstestadapter.rar");
   }

   public void testCachedConnectionError() throws Exception
   {
      UserTxSessionHome sh = (UserTxSessionHome) getInitialContext().lookup("UserTxSession");
      UserTxSession s = sh.create();
      try
      {
         s.testUnclosedError();
         fail("Should not be here");
      }
      catch (RemoteException expected)
      {
         log.debug("Got the expected exception RemoteException:", expected);
         Throwable linked = expected.getCause();
         assertTrue("Not the expected exception: " + linked, linked != null && linked instanceof ResourceException);
      }
   }
}
