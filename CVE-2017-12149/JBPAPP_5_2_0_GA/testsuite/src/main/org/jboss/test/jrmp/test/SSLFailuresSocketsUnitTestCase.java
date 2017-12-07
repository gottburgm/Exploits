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
package org.jboss.test.jrmp.test;

import java.io.IOException;
import java.rmi.RemoteException;
import java.net.URL;
import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;

import org.jboss.test.jrmp.interfaces.StatelessSession;
import org.jboss.test.jrmp.interfaces.StatelessSessionHome;

/**
 * Test of using custom RMI socket factories with the JRMP ejb container
 * invoker.
 *
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class SSLFailuresSocketsUnitTestCase extends JBossTestCase
{
   /**
    * Constructor for the CustomSocketsUnitTestCase object
    *
    * @param name  Description of Parameter
    */
   public SSLFailuresSocketsUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Test basic ejb access over the ssl socket requiring a client cert
    *
    * @exception Exception  Description of Exception
    */
   public void testClientCertSSLAccessFailure() throws Exception
   {
      log.info("+++ testClientCertSSLAccessFailure");
      // Install the truststore to use for the server cert
      String res = super.getResourceURL("test-configs/tomcat-ssl/conf/client.keystore");
      log.info("client.keystore: "+res);
      URL clientURL = new URL(res);
      System.setProperty("javax.net.ssl.trustStore", clientURL.getFile());
      System.setProperty("javax.net.ssl.keyStore", clientURL.getFile());
      System.setProperty("javax.net.ssl.trustStorePassword", "unit-tests-client");
      System.setProperty("javax.net.ssl.keyStorePassword", "unit-tests-client");
      InitialContext jndiContext = new InitialContext();
      log.debug("Lookup StatelessSessionWithSSL");
      Object obj = jndiContext.lookup("StatelessSessionWithSSL");
      StatelessSessionHome home = (StatelessSessionHome)obj;
      log.debug("Found StatelessSessionWithSSL Home");
      StatelessSession bean = home.create();
      log.debug("Created StatelessSessionWithSSL");
      // Test that the Entity bean sees username as its principal
      String echo = bean.echo("jrmp");
      log.debug("bean.echo(jrmp) = " + echo);
      bean.remove();
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(SSLFailuresSocketsUnitTestCase.class, "jrmp-comp.jar");
   }

}
