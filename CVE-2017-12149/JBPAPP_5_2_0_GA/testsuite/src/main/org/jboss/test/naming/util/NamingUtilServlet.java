 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
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

package org.jboss.test.naming.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * @author pskopek
 *
 */
public class NamingUtilServlet extends HttpServlet {

   public static Logger log = Logger.getLogger(NamingUtilServlet.class);
   
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException {

      String jndiName = req.getParameter("jndiName");
      String dataKey = req.getParameter("dataKey");
      String data = req.getParameter("data");
      boolean useHAJndi = Boolean.parseBoolean(req.getParameter("useHAJndi"));

      try {
         createTestJNDIBinding(jndiName, dataKey, data, useHAJndi, resp.getWriter());
      }
      catch (Exception e) {
         throw new ServletException(e);
      }
      
      resp.getWriter().println("OK");
      
   }
   

   private void createTestJNDIBinding(String jndiName, String dataKey,
         Object data, boolean useHAJNDI, PrintWriter out) throws Exception {

      Context ctx = null;
      if (useHAJNDI) {
         Properties env = new Properties();  
         env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");  
         env.put(Context.URL_PKG_PREFIXES, "jboss.naming:org.jnp.interfaces");
         String partitionName = System.getProperty("jboss.partition.name", "DefaultPartition");
         env.put("jnp.partitionName", partitionName);
         ctx = new InitialContext(env);
         log.debug("HAJNDI initial context created");
      } else {
         ctx = new InitialContext();
         log.debug("JNDI initial context created");
      }

      String[] path = jndiName.split("/");
      String subPath = "";
      for (int i = 0; i < path.length; i++) {

         if (path[i].equals("")) {
            continue;
         }

         subPath = subPath + "/" + path[i];
         log.debug("creating subcontext="+subPath);
         try {
            ctx.createSubcontext(subPath);
            log.debug("subcontext="+subPath+" created.");
         }
         catch (NameAlreadyBoundException e) {
            // ignore
         }
      }
      
      if (data != null) {
         log.debug("bind s="+subPath+", dataKey="+dataKey+", data="+data);
         ctx.bind(subPath + "/" + dataKey, data);
         log.debug(data + " bound.");
      }

      ctx.close();

   }
 
}
