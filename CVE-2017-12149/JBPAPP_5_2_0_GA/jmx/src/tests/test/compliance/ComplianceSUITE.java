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
package test.compliance;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Everything under test.compliance is a set of unit tests
 * which should pass as much as possible against the JMX RI
 *
 * Additions to this package are welcome/encouraged - adding a
 * test that fails is a great way to communicate a bug ;-)
 *
 * Anyone contributing to the JBoss JMX impl should seriously
 * consider providing a testcase prior to making code changes
 * in the impl itself - ala XP.
 *
 * The only restriction is that if the tests don't succeed against
 * the RI, the test error message should indicate that the test
 * will fail on the RI (preferred way) or at least comment the testcase
 * stating expected failures.  Either way, you should comment the code
 * justifying why the test is valid despite failing against the RI.
 *
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 */

public class ComplianceSUITE extends TestSuite
{
   public static void main(String[] args)
   {
      try
      {
         // Support for RI tracing, use -Dcom.sun.jmx.trace.level=x where x is one of 0, 1 or 2
         Class trace = Thread.currentThread().getContextClassLoader().loadClass("com.sun.jmx.trace.TraceImplementation");
         java.lang.reflect.Method init = trace.getMethod("init", new Class[] { Integer.TYPE });
         init.invoke(null, new Object[] { new Integer(System.getProperty("com.sun.jmx.trace.level")) });
      }
      catch (Exception ignored)
      {
      }

      junit.textui.TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("All Compliance Tests");

      suite.addTest(org.jboss.test.jmx.compliance.ComplianceSUITE.suite());
      return suite;
   }
}
