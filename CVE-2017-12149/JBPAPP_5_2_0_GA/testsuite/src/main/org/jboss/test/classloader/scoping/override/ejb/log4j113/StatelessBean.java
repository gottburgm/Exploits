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
package org.jboss.test.classloader.scoping.override.ejb.log4j113;

import java.lang.reflect.Method;
import java.net.URL;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.jboss.test.classloader.scoping.override.web.log4j113.Log4jServlet;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class StatelessBean implements SessionBean
{
   private Category log;

   public void ejbCreate() throws CreateException
   {
   }
   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
   }
   public void setSessionContext(SessionContext context)
   {
   }

   public Throwable checkVersion()
   {
      Throwable error = null;
      // Validate the log4j env against the 1.1.3 classes
      try
      {
         Class categoryClass = Category.class;
         System.out.println("Category.CS: "+categoryClass.getProtectionDomain().getCodeSource());
         // Check that the 1.1.3 assert(boolean, String) method exists
         Class[] sig = {boolean.class, String.class};
         Method m = categoryClass.getDeclaredMethod("assert", sig);
         System.out.println("found assert method: "+m);
         // Find the log4j.properties file
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         URL resURL = loader.getResource("log4j.properties");
         System.out.println("found log4j.properties: "+resURL);
         PropertyConfigurator config = new PropertyConfigurator();
         log = Category.getInstance(StatelessBean.class);
         config.configure(resURL);
      }
      catch(Throwable t)
      {
         t.printStackTrace();
         error = t;
      }
      return error;
   }
}
