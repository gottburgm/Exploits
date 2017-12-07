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
package org.jboss.test.web.servlets;

import java.io.File;
import java.io.IOException;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;

/**
 *  ServletContextListener class that creates a plain text file 
 *  in the jboss tmp directory
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since  Jan 10, 2006
 *  @version $Revision: 81036 $
 */
public class TextFileServletContextListener implements ServletContextListener
{
   private static final Logger log = Logger.getLogger(TextFileServletContextListener.class);
   
   private File location = null;
   
   public void contextInitialized(ServletContextEvent event)
   {
      log.debug("ContextInitialized");
      ServletContext ctx = event.getServletContext();
      try
      {
         location = this.getTmpLocation(); 
      }catch(JMException e)
      {
         log.error(e);
         throw new RuntimeException("Error locating tmp file loc");
      }
      
      String name = ctx.getServletContextName();
      //Delete any old files that exist
      File file = new File(location ,name + ".txt");
      if(file.exists())
         file.delete();
   }

   public void contextDestroyed(ServletContextEvent event)
   {
      log.debug("ContextDestroyed"); 
      ServletContext ctx = event.getServletContext(); 
      String name = ctx.getServletContextName();

      // Test for classloading in destroy context
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      log.info("contextDestroyed cl=" + cl);
      try
      {
         cl.loadClass("org.jboss.test.web.servlets.ContextDestroyed");
      }
      catch (ClassNotFoundException e)
      {
         log.error("Cannot load class", e);
         throw new RuntimeException(e.toString());
      }

      //Create a text file
      String fileName = name + ".txt";
      File file = new File(location, fileName);
      if(file.exists())
         throw new RuntimeException(fileName + " should not exist");
      try
      {
         file.createNewFile();
      }
      catch (IOException e)
      {
         log.error(e);
         throw new RuntimeException(fileName + " creation failed");
      }
   } 
   
   private File getTmpLocation() throws JMException
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      ObjectName oname = new ObjectName("jboss.system:type=ServerConfig");
      return (File)server.getAttribute(oname,"ServerTempDir"); 
   }
}
