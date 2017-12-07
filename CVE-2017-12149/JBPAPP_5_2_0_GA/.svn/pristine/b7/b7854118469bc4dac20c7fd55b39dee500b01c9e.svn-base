/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2011, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @authors tag. See the copyright.txt in the
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
package org.jboss.test.ejb3.jbpapp6953;

import javax.naming.*;
import java.util.*;

public class HelloUtil {

	public HelloUtil() {

	}

	public Hello getBean(String host) throws Exception {
		String providerJndi = "HelloBean/remote";
		return getBean(host, providerJndi);
	}
	public Hello getBean(String host, String jndi) throws Exception {

		// ------ This code sets up to call JNDI on a different server --------
		Properties properties = new Properties();
    properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
    properties.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
		//properties.put("java.naming.factory.url.pkgs","=org.jboss.naming:org.jnp.interfaces");
    properties.put("jnp.socket.Factory", "org.jnp.interfaces.TimedSocketFactory");
		properties.setProperty("java.naming.provider.url", "jnp://"+ host +":1099");

		// ------- This is all the code required to get the EJB Remote interface from JNDI ------
		// ------- If you want to use the JNDI on localhost, you can just do new InitialContext() --

		Context context = new InitialContext(properties);

		return (Hello) context.lookup(jndi);
	}


}
