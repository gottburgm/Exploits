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
package org.jboss.test.jacc.test;

import javax.management.ObjectName; 
 
import junit.framework.Test; 
import junit.framework.TestSuite; 
 
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/** Basic tests for web apps run under a JACC authorization manager
 *
 *  @author Scott.Stark@jboss.org
 *  @author Anil.Saldhana@jboss.org
 *  @version $Revision: 105785 $
 */
public class WebIntegrationUnitTestCase
      extends org.jboss.test.web.test.WebIntegrationUnitTestCase
{
   public WebIntegrationUnitTestCase(String name)
   {
      super(name);
   }

    /** 
    * Overriden because a check is made for isUserInRole("InternalUser") 
    * and there is no security-role-ref for this role defined in web.xml 
    * @see org.jboss.test.web.test.WebIntegrationUnitTestCase#testUnsecureRunAsServletWithPrincipalName() 
    */  
   public void testUnsecureRunAsServletWithPrincipalName() throws Exception 
   {   
   } 
 
   /** 
    * Overriden because a check is made for isUserInRole("InternalUser") 
    * and there is no security-role-ref for this role defined in web.xml 
    * @see org.jboss.test.web.test.WebIntegrationUnitTestCase#testUnsecureRunAsServletWithPrincipalNameAndRoles() 
    */  
   public void testUnsecureRunAsServletWithPrincipalNameAndRoles() throws Exception 
   {  
   }  
    
   /** 
    * Setup the test suite. 
    */ 
   public static Test suite() throws Exception 
   { 
      TestSuite suite = new TestSuite(); 
      suite.addTest(new TestSuite(WebIntegrationUnitTestCase.class)); 
 
      // Create an initializer for the test suite 
      Test wrapper = new JBossTestSetup(suite) 
      { 
         protected void setUp() throws Exception 
         { 
            super.setUp(); 
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            JMSDestinationsUtil.setupBasicDestinations();
            redeploy("jbosstest-web.ear"); 
            flushAuthCache("jbosstest-web"); 
         } 
         protected void tearDown() throws Exception 
         { 
            undeploy("jbosstest-web.ear");
            JMSDestinationsUtil.destroyDestinations();
            super.tearDown();
         } 
      }; 
      return wrapper; 
   } 
}
