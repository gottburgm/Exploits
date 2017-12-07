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
package org.jboss.test.jmx.xmbean;

import javax.management.Attribute;

import org.jboss.system.ServiceMBeanSupport;

/**
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 85945 $
 */
public class ServiceUsingCustomAttribute extends ServiceMBeanSupport
{
   private CustomType attr;
   
   public CustomType getAttr()
   {
      return attr;
   }
   
   public void setAttr(CustomType attr)
   {
      this.attr = attr;
   }

   public void selfTest() throws Exception
   {
      CustomType ct = new CustomType(666, 999);
      getServer().setAttribute(getServiceName(), new Attribute("Attr", ct));
   }
   
   public String show()
   {
      if (attr == null)
         return "null";
      else
         return attr.toString();
   }
}
