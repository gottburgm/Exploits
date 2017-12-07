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
package org.jboss.test.system.controller.support;

import java.util.Iterator;

/**
 * JavaBean.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class JavaBean
{
   private String property1;
   private Integer property2;
   private Iterator property3;

   public String getProperty1()
   {
      return property1;
   }

   public void setProperty1(String property1)
   {
      this.property1 = property1;
   }

   public Integer getProperty2()
   {
      return property2;
   }

   public void setProperty2(Integer property2)
   {
      this.property2 = property2;
   }

   public Iterator getProperty3()
   {
      return property3;
   }

   public void setProperty3(Iterator property3)
   {
      this.property3 = property3;
   }
}
