/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cluster.classloader.leak.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.test.classloader.leak.web.Address;
import org.jboss.test.classloader.leak.web.Person;

public class SimpleServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
   {
      org.jboss.test.classloader.leak.clstore.ClassLoaderStore.getInstance().storeClassLoader("SERVLET", Thread.currentThread().getContextClassLoader());
      org.jboss.test.classloader.leak.clstore.ClassLoaderStore.getInstance().storeClassLoader("SERVLET_TCCL", Thread.currentThread().getContextClassLoader());
      
      HttpSession session = req.getSession();
      Person ben = (Person) session.getAttribute("TEST_PERSON");
      if (ben == null)
      {
         ben=new Person();
         Address addr = new Address();
         addr.setZip(95123);
         addr.setCity("San Jose");
         ben.setAge(100);
         ben.setName("Ben");
         ben.setAddress(addr);
         List languages = new ArrayList();
         languages.add("Farsi");
         ben.setLanguages(languages);
         session.setAttribute("TEST_PERSON", ben);
      }
      else
      {
         ben.setAge(ben.getAge() + 1);
         ben.getLanguages().add("Java");
      }
      
      res.setContentType("text/text");
      PrintWriter writer = res.getWriter();
      writer.println("WEBAPP");
      writer.flush();
   }
   
}
