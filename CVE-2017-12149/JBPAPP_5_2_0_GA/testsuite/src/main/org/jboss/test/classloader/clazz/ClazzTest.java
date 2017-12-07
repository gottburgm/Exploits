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
package org.jboss.test.classloader.clazz;

import org.jboss.system.ServiceMBeanSupport;

/**
 * A simple service to test class loading.
 * 
 * @author claudio.vesco@previnet.it
 */
public class ClazzTest
   extends ServiceMBeanSupport
   implements ClazzTestMBean
{
   /* (non-Javadoc)
    * @see org.jboss.test.classloader.clazz.ClazzTestMBean#loadClass(java.lang.String)
    */
   public void loadClass(String clazz) throws Exception {
      ClassLoader cl = getClass().getClassLoader();
      
      cl.loadClass(clazz);
   }

   /* (non-Javadoc)
    * @see org.jboss.test.classloader.clazz.ClazzTestMBean#loadClassFromTCL(java.lang.String)
    */
   public void loadClassFromTCL(String clazz) throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      
      tcl.loadClass(clazz);
   }
}
