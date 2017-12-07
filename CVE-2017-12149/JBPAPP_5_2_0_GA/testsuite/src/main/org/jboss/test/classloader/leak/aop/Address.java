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
package org.jboss.test.classloader.leak.aop;

import java.io.Serializable;

import org.jboss.cache.pojo.annotation.Replicable;



/**
 * Test class for AOP-prepared classes.  @Replicable annotation will
 * cause the class to be aop-prepared.
 *
 * @version $Revision: 85945 $
 */
@Replicable
public class Address implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 4782007386315375402L;
   
   String street = null;
   String city = null;
   int zip = 0;

   public String getStreet()
   {
      return street;
   }

   public void setStreet(String street)
   {
      this.street = street;
   }

   public String getCity()
   {
      return city;
   }

   public void setCity(String city)
   {
      this.city = city;
   }

   public int getZip()
   {
      return zip;
   }

   public void setZip(int zip)
   {
      this.zip = zip;
   }

   public String toString()
   {
      return "street=" + getStreet() + ", city=" + getCity() + ", zip=" + getZip();
   }

//    public Object writeReplace() {
//	return this;
//    }
}
