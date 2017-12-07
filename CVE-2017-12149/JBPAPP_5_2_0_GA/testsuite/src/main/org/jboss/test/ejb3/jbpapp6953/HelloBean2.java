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

import javax.ejb.Stateless;
import java.util.logging.Logger;

/**
 * 
 * @author bmaxwell
 * Requires >= JBoss 4.2 for annotations
 */

@Stateless(name="HelloBean2",description="hello world 2 example")
public class HelloBean2 implements Hello, HelloLocal {
	
	public static final String NAME = HelloBean2.class.getSimpleName();

	public static final String RemoteJNDIName =  NAME + "/remote";
	public static final String LocalJNDIName =  NAME + "/local";
	
	private Logger log = Logger.getLogger(this.getClass().toString());
	
	public String hello(String name) {
		log.info("hello("+name+") = Hello " + name);
		return "Hello " + name;
	}

	public String hello(String host, String name) {
		try { 
			Hello bean = new HelloUtil().getBean(host);
			String response = bean.hello(name);
			log.info("hello("+host+","+name+") = " + response);
			return response;
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getMessage() != null) return e.getMessage();
			return "Error - check log";
		}
	}

	public String echo(String name)
	{
		return "echo " + name;
	}
	public String hello(Param name)
	{
		return "Hello " + name.getName();
	}

}
