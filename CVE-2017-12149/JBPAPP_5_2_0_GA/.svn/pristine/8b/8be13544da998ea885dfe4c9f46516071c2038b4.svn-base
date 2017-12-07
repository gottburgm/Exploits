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
package org.jboss.test.bench.servlet;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Random;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.test.bench.interfaces.*;

public class SimpleServlet extends HttpServlet {
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
	PrintWriter out;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, java.io.IOException {

		String dest = req.getParameter("dest");

		resp.setContentType("text/html");
		out = resp.getWriter();
		
		out.println("<html>");
		out.println("<head>");
		
		out.println("<title>HelloEJB</title>");
		out.println("</head>");
			
		out.println("<body>");
		
		out.println("<h1>Servlet calling EJB</h1>");
					
		if ("SL".equals(dest)) callStateless();
		else if ("Entity".equals(dest)) callEntity();
		
		out.println("</body>");
		out.println("</html>");
	}

	Context getContext() throws Exception {
		System.setProperty("java.naming.factory.initial","org.jnp.interfaces.NamingContextFactory");
		System.setProperty("java.naming.provider.url","localhost");
		System.setProperty("java.naming.factory.url.pkgs","org.jboss.naming;");
		
		return new InitialContext();
		
	}
	
	void callStateless() {
		try {
			Context ctx = getContext();
			MySessionHome home = (MySessionHome)ctx.lookup("StatelessSession");
			MySession bean = home.create();

			out.println("called stateless session and it said: " + bean.getInt());
			
		} catch (Exception e) {
			out.println("<pre>");
			e.printStackTrace(out);
			out.println("</pre>");
		}
	}
	
	void callEntity() {
		try {
			Context ctx = getContext();
			SimpleEntityHome home = (SimpleEntityHome)ctx.lookup("SimpleEntity");
			SimpleEntity bean = home.create(50000+ new Random().nextInt());

			out.println("called entity and it said: " + bean.getField());
			
		} catch (Exception e) {
			out.println("<pre>");
			e.printStackTrace(out);
			out.println("</pre>");
		}
	}
	
}


