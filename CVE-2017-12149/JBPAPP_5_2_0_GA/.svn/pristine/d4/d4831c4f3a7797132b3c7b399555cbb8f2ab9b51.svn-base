/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.ejb3.jbas7883.unit;

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.ejb3.jbas7883.TestRemote;

/**
 * 
 * @author Darran Lofthouse darran.lofthouse@jboss.com
 * @see https://issues.jboss.org/browse/JBAS-7883
 */
public class MailQueryUnitTestCase extends JBossTestCase
{

   public MailQueryUnitTestCase(String name)
   {
      super(name);
   }

   public void testResourceOne() throws Exception
   {
      TestRemote bean = (TestRemote) getInitialContext().lookup("TestBean/remote");
      String host = bean.testResourceOne();

      assertEquals("Expected host name", "1.1.1.1", host);
   }

   public void testResourceTwo() throws Exception
   {
      TestRemote bean = (TestRemote) getInitialContext().lookup("TestBean/remote");
      String host = bean.testResourceTwo();
      
      assertEquals("Expected host name", "2.2.2.2", host);
   }

   public void testLookupOne() throws Exception
   {
      TestRemote bean = (TestRemote) getInitialContext().lookup("TestBean/remote");
      String host = bean.testCall(null, "mail/Mail1");

      assertEquals("Expected host name", "1.1.1.1", host);
   }
   
   public void testLookupOne_Env() throws Exception
   {
      TestRemote bean = (TestRemote) getInitialContext().lookup("TestBean/remote");
      String host = bean.testCall(null, "java:/comp/env/mail1");

      assertEquals("Expected host name", "1.1.1.1", host);
   }   

   public void testLookupTwo() throws Exception
   {
      TestRemote bean = (TestRemote) getInitialContext().lookup("TestBean/remote");
      String host = bean.testCall(null, "mail/Mail2");

      assertEquals("Expected host name", "2.2.2.2", host);
   }

   public void testLookupTwo_Env() throws Exception
   {
      TestRemote bean = (TestRemote) getInitialContext().lookup("TestBean/remote");
      String host = bean.testCall(null, "java:/comp/env/mail2");

      assertEquals("Expected host name", "2.2.2.2", host);
   }
   
   public void testLookupOne_Context() throws Exception
   {
      TestRemote bean = (TestRemote) getInitialContext().lookup("TestBean/remote");
      String host = bean.testCall("mail", "Mail1");

      assertEquals("Expected host name", "1.1.1.1", host);
   }

   public void testLookupTwo_Context() throws Exception
   {
      TestRemote bean = (TestRemote) getInitialContext().lookup("TestBean/remote");
      String host = bean.testCall("mail", "Mail2");

      assertEquals("Expected host name", "2.2.2.2", host);
   }
   
   public void testLookupThree_Java() throws Exception
   {
      TestRemote bean = (TestRemote) getInitialContext().lookup("TestBean/remote");
      String host = bean.testCall(null, "java:/Mail3");

      assertEquals("Expected host name", "3.3.3.3", host);
   }

   public void testLookupThree_JavaContext() throws Exception
   {
      TestRemote bean = (TestRemote) getInitialContext().lookup("TestBean/remote");
      String host = bean.testCall("java:/", "Mail3");

      assertEquals("Expected host name", "3.3.3.3", host);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(MailQueryUnitTestCase.class, "jbas7883-service.xml, jbas7883.jar");
   }

}
