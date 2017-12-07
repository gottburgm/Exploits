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

import javax.management.Attribute;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.interfaces.CachedConnectionSession;
import org.jboss.test.jca.interfaces.CachedConnectionSessionHome;

/**
 * CachedConnectionSessionUnitTestCase.java
 * Tests connection disconnect-reconnect mechanism.
 *
 * Created: Fri Mar 15 22:48:41 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class CachedConnectionSessionUnitTestCase extends JBossTestCase
{

   private CachedConnectionSessionHome sh;
   private CachedConnectionSession s;

   public CachedConnectionSessionUnitTestCase (String name)
   {
      super(name);
   }

   protected void setSpecCompliant(Boolean value)
      throws Exception
   {
      ObjectName CCM = new ObjectName("jboss.jca:service=CachedConnectionManager");
      getServer().setAttribute(CCM, new Attribute("SpecCompliant", value));
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      setSpecCompliant(Boolean.TRUE);
      sh = (CachedConnectionSessionHome)getInitialContext().lookup("CachedConnectionSession");
      s = sh.create();
      s.createTable();
   }

   protected void tearDown() throws Exception
   {
      if (s != null)
      {
         s.dropTable();
      } // end of if ()

      setSpecCompliant(Boolean.FALSE);
      super.tearDown();
   }

   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(CachedConnectionSessionUnitTestCase.class, "jcatest.jar");
      Test t2 = getDeploySetup(t1, "testadapter-ds.xml");
      return getDeploySetup(t2, "jbosstestadapter.rar");
   }

   public void testCachedConnectionSession() throws Exception
   {
      s.insert(1L, "testing");
      assertTrue("did not get expected value back", "testing".equals(s.fetch(1L)));
   }

   public void testTLDB() throws Exception
   {
      setSpecCompliant(Boolean.FALSE);
      s.firstTLTest();
   }

}// CachedConnectionSessionUnitTestCase
