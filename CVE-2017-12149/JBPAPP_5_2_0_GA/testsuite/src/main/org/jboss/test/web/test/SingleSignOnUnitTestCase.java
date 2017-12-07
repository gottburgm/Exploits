/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.test.web.test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Tests of web app single sign-on
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 76164 $
 */
public class SingleSignOnUnitTestCase extends JBossTestCase
{
   private String baseURLNoAuth;

   public SingleSignOnUnitTestCase(String name)
   {
      super(name);
   }   

   protected void setUp() throws Exception
   {
      super.setUp();
      baseURLNoAuth = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080); 
   }

   /** Test single sign-on across two web apps using form based auth
    * 
    * @throws Exception
    */ 
   public void testFormAuthSingleSignOn() throws Exception
   {
      log.info("+++ testFormAuthSingleSignOn");
      
      SSOBaseCase.executeFormAuthSingleSignOnTest(baseURLNoAuth, baseURLNoAuth, getLog());
      
   }
   
   /** Test single sign-on across two web apps using form based auth
    * 
    * @throws Exception
    */ 
   public void testNoAuthSingleSignOn() throws Exception
   {
      log.info("+++ testNoAuthSingleSignOn");

      SSOBaseCase.executeNoAuthSingleSignOnTest(baseURLNoAuth, baseURLNoAuth, getLog());
   }

   /** One time setup for all SingleSignOnUnitTestCase unit tests
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(SingleSignOnUnitTestCase.class));

      // Create an initializer for the test suite
      Test wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            // Deploy the test ear
            deploy("web-sso.ear");
            // Make sure the security cache is clear
            flushAuthCache();
         }
         protected void tearDown() throws Exception
         {
            undeploy("web-sso.ear");
            super.tearDown();
         }
      };
      return wrapper;
   }
}
