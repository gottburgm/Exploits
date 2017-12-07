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
package org.jboss.test.jmx.compliance.metadata;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test cases for JMX metadata classes.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $ 
 */
public class MetaDataSUITE
   extends TestSuite
{
   
  public static void main(String[] args)
   {
      junit.textui.TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("JBossMX Meta Data Tests");

      suite.addTest(new TestSuite(MBeanFeatureInfoTEST.class));
      suite.addTest(new TestSuite(MBeanParameterInfoTEST.class));
      suite.addTest(new TestSuite(MBeanOperationInfoTEST.class));
      suite.addTest(new TestSuite(MBeanAttributeInfoTEST.class));
      suite.addTest(new TestSuite(MBeanNotificationInfoTEST.class));
      suite.addTest(new TestSuite(MBeanConstructorInfoTEST.class));
      suite.addTest(new TestSuite(MBeanInfoTEST.class));
      
      return suite;
   }
   
}
      



