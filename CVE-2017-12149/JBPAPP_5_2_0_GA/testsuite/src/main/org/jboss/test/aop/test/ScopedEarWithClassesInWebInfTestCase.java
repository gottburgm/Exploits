/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.web.HttpUtils;

/**
 * This test must be run within the test-aop-scoped target since it requires the classloader hooks
 * for interception of classes within the .war file.
 * 
 * THE EAR IS NOT REALLY SCOPED!!!!
 * The test is named to fit in with the naming convention for tests requiring a special hook
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 85945 $
 */
public class ScopedEarWithClassesInWebInfTestCase extends JBossTestCase
{
   Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;
   static AOPClassLoaderHookTestSetup setup;

   public ScopedEarWithClassesInWebInfTestCase(String name)
   {
      super(name);
   }

   public void testEar1() throws Exception
   {
      URL url = new URL(HttpUtils.getBaseURL() + "aop-classesinwebinf/srv");
      HttpUtils.accessURL(url);      
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(ScopedEarWithClassesInWebInfTestCase.class));

      setup = new AOPClassLoaderHookTestSetup(suite, "aop-classesinwebinf.ear");
      return setup;
   }
}
