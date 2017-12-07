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
package org.jboss.test.jmx.mbean;

import org.jboss.system.Service;
import org.jboss.system.ServiceMBeanSupport;

/**
 * TestMBClassLoader.java
 *
 *
 * Created: Sun Feb 17 20:08:31 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 * @jmx:mbean name="jboss.test:service=BrokenDeployer"
 * @jmx:interface extends="org.jboss.system.Service"
 */

public class TestMBClassLoader extends ServiceMBeanSupport implements TestMBClassLoaderMBean
{
   public TestMBClassLoader ()
   {
      
   }

   /**
    * Describe <code>getClassLoader</code> method here.
    *
    * @return a <code>String</code> value
    * @jmx:managed-operation
    */
   public String getClassLoader()
   {
      return this.getClass().getClassLoader().toString();
   }
   
}// TestMBClassLoader
