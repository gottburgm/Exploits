/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.test.web.jbas8318;

import javax.annotation.Resource;
import javax.jms.Queue;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: jpai
 */
public class SimpleServlet extends BaseServlet
{

   public static final String SUCCESS_MESSAGE = "Success!";
   
   @Resource
   private javax.transaction.UserTransaction userTransaction;

   @Resource (name="simpleString")
   private String simpleEnvResource;

   @Resource(mappedName = "queue/DLQ")
   private Queue dlq;
   
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      if (this.userTransaction == null)
      {
         throw new ServletException("UserTransaction resource not injected in the servlet");
      }

      if (this.dlq == null)
      {
         throw new ServletException("Queue not injected in the servlet");
      }
      if (this.simpleEnvResource == null)
      {
         throw new ServletException("Simple string env resource not injected in the servlet");
      }
      // check base class injections
      if (this.utInServletBase == null)
      {
         throw new ServletException("UserTransaction in base class of servlet not injected");
      }
      if (this.envEntryStringInServletBase == null)
      {
         throw new ServletException("Simple env-entry string in base class of servlet not injected");
      }
      // everything injected
      resp.getOutputStream().print(SUCCESS_MESSAGE);
   }
}
