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
package org.jboss.test.security.test;

import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.Configuration;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.security.interfaces.StatelessSession;
import org.jboss.test.security.interfaces.StatelessSessionHome;

/**
 * Test for assignment of default security domain for an EJB with no security domain provided in jboss.xml.
 * 
 * @author <a href="mmoyses@redhat.com">Marcus Moyses</a>
 * @version $Revision: 85945 $
 */
public class EJBNullSecurityDomainUnitTestCase extends JBossTestCase
{

   public EJBNullSecurityDomainUnitTestCase(String name)
   {
      super(name);
   }

   public void testEJBNullSecurityDomain() throws Exception
   {
      log.debug("+++ testEJBNullSecurityDomain");
      Object obj = getInitialContext().lookup("null.StatelessSession");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found null.StatelessSession Home");
      StatelessSession bean = null;
      try
      {
         bean = home.create();
         fail("Invoking create() should fail");
      }
      catch (Exception e)
      {
         Throwable t = e.getCause();
         if (t instanceof SecurityException)
         {
            log.debug("Invoking create() was correctly denied by a SecurityException:", e);
         }
         else
         {
            log.debug("Invoking create() failed by an unexpected reason:", e);
            fail("Unexpected exception");
         }
      }
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("scott", "echoman");
      client.login();
      try
      {
         bean = home.create();
         bean.echo("hi");
      }
      catch (Exception e)
      {
         fail(e.getLocalizedMessage());
      }
   }

   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(EJBNullSecurityDomainUnitTestCase.class));

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         @Override
         protected void setUp() throws Exception
         {
            super.setUp();
            Configuration.setConfiguration(XMLLoginConfigImpl.getInstance());
            redeploy("ejb-null-security-domain.jar");
            flushAuthCache();
         }

         @Override
         protected void tearDown() throws Exception
         {
            undeploy("ejb-null-security-domain.jar");
            super.tearDown();
         }
      };
      return wrapper;
   }
}
