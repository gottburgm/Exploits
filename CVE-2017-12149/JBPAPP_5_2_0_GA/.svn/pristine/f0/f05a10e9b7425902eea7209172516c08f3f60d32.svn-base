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

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Dispatcher extends HttpServlet {

       static org.jboss.logging.Logger log =
       org.jboss.logging.Logger.getLogger(Dispatcher.class);

	public static String[] params = {"hw", "os", "ram", "cpu", "jdk", "ejb", "web", "servlet" };

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, java.io.IOException {
		try {

		resp.setHeader("Location", req.getContextPath() + "/");

		if (req.getParameter("gototest") != null)
			// save config and go to tests
			saveInfo(req, resp);
		
		else if (req.getParameter("goejb") != null)
			// test ejb
			testEjb(req, resp);

		else if (req.getParameter("goall") != null)
			// test the whole stack
			testAll(req, resp);
		
		else 
			// should not get there, go back to the main page
			req.getRequestDispatcher("/index.jsp").include(req, resp);
		} catch (Throwable t) { 
			log.debug("failed", t);
		}

	}

    /** 
	 * Saves the info from the request in the session object		
	 */
	void saveInfo(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, java.io.IOException {
		
		HttpSession session = req.getSession();
		ConfigData conf = (ConfigData)session.getAttribute("conf");

		for (int i=0; i<conf.size(); i++) {
			conf.setInfo(conf.getName(i), req.getParameter(conf.getName(i)));
		}
		
		req.getRequestDispatcher("/tests.jsp").include(req, resp);
		
	}		
	
    void testEjb(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, java.io.IOException {

		EJBTester ejbTester = new EJBTester(req);
		
		// do the test
	    ejbTester.test();
		
		req.setAttribute("ejbTester", ejbTester);

		// finally include to the correct jsp page

		req.getRequestDispatcher("/ejbResult.jsp").include(req, resp);
		
	}

	void testAll(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, java.io.IOException {

		FullTester fullTester = new FullTester(req);
		
		// do the test
	    fullTester.test();
		
		req.setAttribute("fullTester", fullTester);

		// finally include to the correct jsp page
		req.getRequestDispatcher("/allResult.jsp").include(req, resp);

	}

}


