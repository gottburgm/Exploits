/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jpa.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.jboss.test.jpa.support.TestEntity;

/**
 * TestServlet.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class TestServlet extends HttpServlet
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -5539196377086639503L;

   private Logger log = Logger.getLogger(TestServlet.class);

   @Resource
   private UserTransaction ut;
   
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      try
      {
         ut.begin();
         try
         {
            InitialContext ctx = new InitialContext();
            EntityManager em = (EntityManager) ctx.lookup("java:comp/env/persistence/em");
            
            String mode = req.getParameter("mode");
            if ("Write".equals(mode))
            {
               TestEntity test = new TestEntity("Hello", "World");
               em.persist(test);
            }
            else
            {
               TestEntity test = em.find(TestEntity.class, "Hello");
               resp.setContentType("text/plain");
               PrintWriter out = resp.getWriter();
               out.print(test.getDescription());
               out.close();
            }
         }
         catch (Exception e)
         {
            log.error("Error in servlet", e);
            ut.setRollbackOnly();
            throw e;
         }
         finally
         {
            ut.commit();
         }
      }
      catch (Exception e)
      {
         throw new ServletException("Error", e);
      }
   }
}
