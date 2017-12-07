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
import javax.naming.NamingException;

import org.jboss.test.security.rmi.ejb3.HelloWorld;

/**
 * @author <a href="mailto:pskopekf@redhat.com">Peter Skopek</a>
 * @version $Revision: $
 */
public class TestClient {

	public static TestClient client;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		client = new TestClient();
		client.run();

	}

	public void run() {

		System.out.println("Running test client");

		
		Properties env = new Properties();
		env.put("java.naming.factory.initial", "org.jboss.naming.HttpNamingContextFactory");
		env.put("java.naming.provider.url", "https://localhost:8443/invoker/JNDIFactory");
		env.put("java.naming.factory.url.pkgs", "org.jboss.naming");
        
		/*
		Properties env = new Properties();
		env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		env.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming.client");
		env.setProperty(Context.PROVIDER_URL, "jnp://localhost:1099");
		env.setProperty("j2ee.clientName", TestClient.class.getName());
        */
		try {
			Context ctx = new InitialContext(env);
			
			System.out.println("context:"+ctx.getClass().getName());
			
			HelloWorld hwb = (HelloWorld) ctx.lookup("HelloWorldBean/remote-https");
			
			System.out.println("Echo: " + hwb.echo("opakuj"));
			
			
		} catch (NamingException e) {
			System.out.println(e);
		}

	}

}
