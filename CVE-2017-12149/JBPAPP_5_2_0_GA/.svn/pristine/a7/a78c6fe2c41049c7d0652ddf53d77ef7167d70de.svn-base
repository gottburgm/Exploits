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
package org.jboss.test.jca.xads;

import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jboss.system.ServiceMBeanSupport;

public class Test extends ServiceMBeanSupport implements TestMBean
{
   public Test()
   {
      System.setProperty("org.jboss.test.jca.xads.SomeProperty", "${org.jboss.test.jca.xads.SomeProperty2}");
      System.setProperty("org.jboss.test.jca.xads.SomeProperty2", "DOUBLE REPLACEMENT");
      System.setProperty("org.jboss.test.jca.xads.BackSlash", "\\");
   }
   
   public void test() throws Exception
   {
      InitialContext ctx = new InitialContext();
      DataSource ds = (DataSource) ctx.lookup("java:/TestXADS");
      try
      {
         ds.getConnection();
      }
      catch (SQLException expected)
      {
         Throwable t = expected;
         while (t.getCause() != null)
            t = t.getCause();
         
         if (t instanceof SQLException == false)
            throw new RuntimeException("Wrong exception: ", t);
      }
   }
}
