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
package org.jboss.test.isolation.mbean;

import java.net.URL;

import org.jboss.logging.Logger;

/**
 * JavaClassIsolation.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class JavaClassIsolation implements JavaClassIsolationMBean
{
   private static final Logger log = Logger.getLogger(JavaClassIsolation.class);
   
   public void test() throws Exception
   {
      // Use our scoped classloader
      ClassLoader cl = getClass().getClassLoader();

      // Should be able to find the class as a resource
      URL resource = cl.getResource("java/org/jboss/test/Test.class");
      if (resource == null)
         throw new RuntimeException("Unable to find resource java/org/jboss/test/Test.class");

      // Should not be able to load the resource
      try
      {
         cl.loadClass("java.org.jboss.test.Test");
         throw new RuntimeException("Should not be able to load class");
      }
      catch (ClassNotFoundException expected)
      {
         // Should not be able to load it
         log.info("Got expected exception", expected);
      }

      // Should be able to load the resgistry class even though it is in our deployment
      cl.loadClass("java.rmi.registry.Registry");
   }
}
