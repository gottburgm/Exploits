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
package org.jboss.test.cmp2.perf.test;

import javax.naming.InitialContext;

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.cmp2.perf.interfaces.CheckBookMgrHome;
import org.jboss.test.cmp2.perf.interfaces.CheckBookMgr;

/**
 *
 * @author Scott.Stark@jboss.org
 * @version 1.0
 */
public class PerfUnitTestCase
   extends JBossTestCase
{

   // Constructor -----------------------------------
   public PerfUnitTestCase(String name)
   {
      super(name);
   }

   // TestCase overrides ----------------------------
   public static Test suite() throws Exception
   {
      return getDeploySetup(PerfUnitTestCase.class, "cmp2-perf.jar");
   }

   // Tests -----------------------------------------
   public void testCheckBookBalance() throws Exception
   {
      InitialContext ctx = getInitialContext();
      CheckBookMgrHome home = (CheckBookMgrHome) ctx.lookup("cmp2/perf/CheckBookMgrHome");
      CheckBookMgr mgr = home.create("Acct123456789USD", 10000);
      long start = System.currentTimeMillis();
      int entryCount = mgr.getEntryCount();
      double balance = mgr.getBalance();
      long end = System.currentTimeMillis();
      double expectedBalance = 10000 - entryCount;
      assertTrue(expectedBalance+" == "+balance, balance == expectedBalance);
      mgr.remove();
      getLog().info("getBalance() time: "+(end - start));
   }

}
