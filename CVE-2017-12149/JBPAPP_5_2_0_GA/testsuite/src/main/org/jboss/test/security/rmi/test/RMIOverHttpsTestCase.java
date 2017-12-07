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
package org.jboss.test.security.rmi.test;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.security.rmi.ejb3.HelloWorld;

/**
 * Tests whether server can handle RMI over HTTPS calls to EJB3. 
 * 
 * How to the keys, certificates and keystores were generated:
 * <code>
 * keytool -genkey -alias ejb-ssl -keystore ssl.keystore -storepass secsec -keypass secsec -dname "CN=localhost,OU=QE,O=redhat.com,L=Brno,C=CZ"
 * keytool -export -alias ejb-ssl -file ssl.cer -keystore ssl.keystore -storepass secsec
 * keytool -import -alias ejb-ssl -file ssl.cer -keystore ssl.truststore -storepass 123456
 * </code>
 * 
 * @author <a href="mailto:pskopekf@redhat.com">Peter Skopek</a>
 * @version $Revision: $
 */
public class RMIOverHttpsTestCase extends JBossTestCase {

	public RMIOverHttpsTestCase(String name) {
		super(name);
	}

	private static final int HTTP_PORT = Integer.getInteger("rmi.over.web.port", 8080).intValue();
	private static final int HTTPS_PORT = Integer.getInteger("rmi.over.secureweb.port", 8443).intValue();

	   public static Test suite() throws Exception
	   {
	      TestSuite suite = new TestSuite();
	      
	      suite.addTest(new JBossTestSetup(new TestSuite(RMIOverHttpsTestCase.class))
	      {
	          protected void setUp() throws Exception
	          {
	             super.setUp();
	             deploy ("rmi-over-https-test.jar");
	          }
	          protected void tearDown() throws Exception
	          {
	             undeploy ("rmi-over-https-test.jar");
	          }
	      });
	      
	      return suite;
	   }
	
	/**
	 * Test objective:  To test calls to EJB3 over HTTPS transport protocol.
	 * Expected resutl: Test has to pass without any exception. Which means all calls return expected values.
	 * 
	 * @throws Exception
	 */
	public void testCallingEJB3OverHttps() throws Exception {

		Properties env = new Properties();
		env.put("java.naming.factory.initial", "org.jboss.naming.HttpNamingContextFactory");
		env.put("java.naming.provider.url", "https://" + getServerHost() + ":" + HTTPS_PORT + "/invoker/JNDIFactory");
		env.put("java.naming.factory.url.pkgs", "org.jboss.naming");
		
		log.debug("java.naming.provider.url="+env.get("java.naming.provider.url"));
		
		Context ctx = new InitialContext(env);
		HelloWorld hwb = (HelloWorld) ctx.lookup("HelloWorldBean/remote-https");
		
		assertTrue("Call to sayHello should return true", hwb.sayHello());
		assertEquals("echo_value", hwb.echo("echo_value"));
		
	}
	   
	/**
	 * Test objective:  To test calls to EJB3 over HTTP transport protocol.
	 * Expected resutl: Test has to pass without any exception. Which means all calls return expected values.
	 * 
	 * @throws Exception
	 */
	public void testCallingEJB3OverHttp() throws Exception {

		Properties env = new Properties();
		env.put("java.naming.factory.initial", "org.jboss.naming.HttpNamingContextFactory");
		env.put("java.naming.provider.url", "http://" + getServerHost() + ":" + HTTP_PORT + "/invoker/JNDIFactory");
		env.put("java.naming.factory.url.pkgs", "org.jboss.naming");
		log.debug("java.naming.provider.url="+env.get("java.naming.provider.url"));
		Context ctx = new InitialContext(env);
		HelloWorld hwb = (HelloWorld) ctx.lookup("HelloWorldBean/remote-http");
		
		assertTrue("Call to sayHello should return true", hwb.sayHello());
		assertEquals("echo_value", hwb.echo("echo_value"));
		
	}
	   
}
