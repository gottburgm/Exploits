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
package org.jboss.test.web.classloader;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.jboss.mx.util.MBeanServerLocator;

/**
 * TestServlet.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class TestServlet extends HttpServlet
{
   private static final long serialVersionUID = -1L;

   private Class<?> reference;
   
   private String test;
   
   public TestServlet(Class<?> reference, String test)
   {
      this.reference = reference;
      this.test = test;
   }

   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      try
      {
         Class<?> testClass = Thread.currentThread().getContextClassLoader().loadClass(Test.class.getName());
         
         ClassLoader testCl = testClass.getClassLoader();
         ClassLoader refCl = reference.getClassLoader();
         Object outcome = true;
         if (testCl.equals(refCl) == false)
            outcome = "Expected " + refCl + " got " + testCl;
         
         MBeanServer server = MBeanServerLocator.locateJBoss();
         ObjectName testFromDD = new ObjectName("jboss.test:service=LegacyWebClassLoader");
         System.out.println(getClass().getName() + " " + test + " ==> " + outcome);
         server.setAttribute(testFromDD, new Attribute(test, outcome));
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
