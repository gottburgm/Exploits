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
package org.jboss.test.isolation.web.c;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

import org.apache.log4j.Logger;

/** 
 * A ContextListener.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class ContextListener implements ServletContextListener
{
   private static final Logger log = Logger.getLogger(ContextListener.class);

   static
   {
      test();
   }
   
   public void contextInitialized(ServletContextEvent e)
   {
      test();
   }

   public void contextDestroyed(ServletContextEvent e)
   {
   }

   protected static void test()
   {
      dump("TCL=", Thread.currentThread().getContextClassLoader());
      dump("MyCL=", ContextListener.class.getClassLoader());
      dump("LoggerCL=", log.getClass().getClassLoader());
      
      if (ContextListener.class.getClassLoader() != log.getClass().getClassLoader())
         throw new RuntimeException("Expected log4j logger to be from same classloader");
   }
   
   protected static void dump(String context, ClassLoader cl)
   {
      System.out.println(context + cl);
   }
}
