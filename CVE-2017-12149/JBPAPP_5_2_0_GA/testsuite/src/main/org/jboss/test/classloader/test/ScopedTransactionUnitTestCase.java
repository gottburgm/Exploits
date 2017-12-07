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
package org.jboss.test.classloader.test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.classloader.scoping.transaction.interfaces.TestSession;
import org.jboss.test.classloader.scoping.transaction.interfaces.TestSessionHome;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for class and transaction passing between scopes
 *
 * @author adrian@jboss.org
 * @version $Revision: 81036 $
 */
public class ScopedTransactionUnitTestCase extends JBossTestCase
{
   public ScopedTransactionUnitTestCase(String name)
   {
      super(name);
   }

   public void testScopedTransaction() throws Exception
   {
      getLog().debug("+++ testScopedTransaction start");

      TestSessionHome home = (TestSessionHome) getInitialContext().lookup("ScopedTxTestSession");
      TestSession session = home.create();
      session.runTest();

      getLog().debug("+++ testScopedTransaction end");
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ScopedTransactionUnitTestCase.class, "scopedtx.jar");
   }
}
