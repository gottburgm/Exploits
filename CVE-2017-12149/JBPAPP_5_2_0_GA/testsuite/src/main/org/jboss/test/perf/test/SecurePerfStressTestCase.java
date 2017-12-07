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
package org.jboss.test.perf.test;

import javax.security.auth.login.Configuration;

import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.test.JBossTestSetup;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Test of EJB call invocation overhead in the presence of EJB security.
 * No more copied code!!
 *
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 *
 * @version $Revision: 81036 $
 */
public class SecurePerfStressTestCase extends PerfStressTestCase //JBossTestCase
{
   {  // Override the PerfStressTestCase names
      CLIENT_SESSION = "secure/perf/ClientSession";
      CLIENT_ENTITY = "local/perfClientEntity";
      PROBE = "secure/perf/Probe";
      PROBE_CMT = "secure/perf/ProbeCMT";
      TX_SESSION = "secure/perf/TxSession";
      ENTITY = "secure/perf/Entity";
      ENTITY2 = "secure/perf/Entity2";
   }

   public SecurePerfStressTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(SecurePerfStressTestCase.class));

      // Create an initializer for the test suite 
      Setup wrapper = new Setup(suite, "secure-perf.jar", true);
      return wrapper; 
   }
}
