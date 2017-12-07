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
package org.jboss.test.cts.test;


import java.util.Properties;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.cts.interfaces.CtsCmp;
import org.jboss.test.cts.interfaces.CtsCmpHome;
import org.jboss.test.cts.keys.AccountPK;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/** Basic conformance tests for stateless sessions
 *
 *  @author kimptoc
 *  @author d_jencks converted to JBossTestCase and logging.
 *  @author Scott.Stark@jboss.org
 *  @version $Revision: 105321 $
 */
public class CmpUnitTestCase
      extends JBossTestCase
{
   private CtsCmpHome home;

   public CmpUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      InitialContext ctx = new InitialContext();
      Object ref = ctx.lookup("ejbcts/CMPBean");
      home = (CtsCmpHome) ref;
   }

   /**
    * Method testBasicStatelessSession
    * @throws Exception
    */
   public void testBasicCmp()
         throws Exception
   {
      getLog().debug("+++ testBasicCmp()");
      AccountPK pk = new AccountPK("testBasicCmp");
      CtsCmp bean = home.create(pk, "testBasicCmp unitTest");
      String result = bean.getPersonsName();
      // Test response
      assertTrue(result.equals("testBasicCmp unitTest"));
      bean.remove();
   }

   /** Test of accessing the home interface from the remote interface in an env
    * new InitialContext() will not work.
    * @throws Exception
    */
   public void testHomeFromRemoteNoDefaultJNDI()
         throws Exception
   {
      getLog().debug("+++ testHomeFromRemoteNoDefaultJNDI()");

      // Override the JNDI variables in the System properties
      Properties sysProps = System.getProperties();
      Properties newProps = new Properties(sysProps);
      newProps.setProperty("java.naming.factory.initial", "badFactory");
      newProps.setProperty("java.naming.provider.url", "jnp://badhost:12345");
      System.setProperties(newProps);

      // Do a lookup of the home and create a remote using a custom env
      Properties env = new Properties();
      env.setProperty("java.naming.factory.initial", super.getJndiInitFactory());
      env.setProperty("java.naming.provider.url", super.getJndiURL());
      try
      {
         InitialContext ctx = new InitialContext(env);
         Object ref = ctx.lookup("ejbcts/CMPBean");
         CtsCmpHome home = (CtsCmpHome)
               PortableRemoteObject.narrow(ref, CtsCmpHome.class);
         AccountPK pk1 = new AccountPK("bean1");
         CtsCmp bean1 = home.create(pk1, "testHomeFromRemoteNoDefaultJNDI");
         CtsCmpHome home2 = (CtsCmpHome) bean1.getEJBHome();
         AccountPK pk2 = new AccountPK("bean2");
         CtsCmp bean2 = home2.create(pk2, "testHomeFromRemoteNoDefaultJNDI");
         bean2.remove();
      }
      finally
      {
         System.setProperties(sysProps);
      }
   }

   public static Test suite() throws Exception
   {
      return new JBossTestSetup(new TestSuite(CmpUnitTestCase.class))
      {
         public void setUp() throws Exception
         {
            super.setUp();
            JMSDestinationsUtil.setupBasicDestinations();
            deploy("cts.jar");
            
         }
         
         public void tearDown() throws Exception
         {
            undeploy("cts.jar");
            JMSDestinationsUtil.destroyDestinations();
         }
      };
      
   }

}
