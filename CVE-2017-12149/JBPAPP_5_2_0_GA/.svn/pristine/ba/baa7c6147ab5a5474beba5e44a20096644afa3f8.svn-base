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
package test.implementation;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suites under <tt>test.implementation</tt> are used
 * to test internal JBossMX implementation and additional
 * functionality not covered in the JMX spec.
 *
 * This suite should be run with the compliance test suite
 * (see <tt>test.compliance</tt> package) whenever new
 * features are being added.
 *
 * @see test.compliance.ComplianceSUITE
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 */

public class ImplementationSUITE extends TestSuite
{
   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(suite());
      
      if (System.getProperty("force.jvm.exit") != null &&
          System.getProperty("force.jvm.exit").equalsIgnoreCase("true"))
         System.exit(0);
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("All JBossMX Implementation Tests");

      suite.addTest(test.implementation.util.UtilSUITE.suite());
      suite.addTest(test.implementation.persistence.PersistenceSUITE.suite());
      suite.addTest(test.implementation.loading.LoadingSUITE.suite());
      suite.addTest(test.implementation.server.ServerSUITE.suite());
      suite.addTest(test.implementation.registry.RegistrySUITE.suite());
      suite.addTest(test.implementation.modelmbean.ModelMBeanSUITE.suite());
      suite.addTest(test.implementation.interceptor.InterceptorSUITE.suite());
      suite.addTest(test.implementation.notification.NotificationSUITE.suite());
      
      return suite;
   }
}
