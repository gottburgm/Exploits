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
package org.jboss.test.xml.mbeanserver.interceptors;

import org.jboss.mx.interceptor.AbstractInterceptor;

import java.net.InetAddress;
import java.net.URL;
import java.util.Properties;

/**
 * A test mbean interceptor whose fields are initialized by the element
 * attributes and property editor string to object conversion.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81084 $
 */
public class SomeBeanInterceptor extends AbstractInterceptor
{
   private boolean flag;
   private int anInt;
   private Long aLong;
   private String aString;
   private String[] someStrings;
   private InetAddress address;
   private URL homePage;
   private Class aClass;
   private Properties someProperties;

   public boolean isFlag()
   {
      return flag;
   }

   public void setFlag(boolean flag)
   {
      this.flag = flag;
   }

   public int getAnInt()
   {
      return anInt;
   }

   public void setAnInt(int anInt)
   {
      this.anInt = anInt;
   }

   public Long getaLong()
   {
      return aLong;
   }

   public void setaLong(Long aLong)
   {
      this.aLong = aLong;
   }

   public String getaString()
   {
      return aString;
   }

   public void setaString(String aString)
   {
      this.aString = aString;
   }

   public String[] getSomeStrings()
   {
      return someStrings;
   }

   public void setSomeStrings(String[] someStrings)
   {
      this.someStrings = someStrings;
   }

   public InetAddress getAddress()
   {
      return address;
   }

   public void setAddress(InetAddress address)
   {
      this.address = address;
   }

   public URL getHomePage()
   {
      return homePage;
   }

   public void setHomePage(URL homePage)
   {
      this.homePage = homePage;
   }

   public Class getaClass()
   {
      return aClass;
   }

   public void setaClass(Class aClass)
   {
      this.aClass = aClass;
   }

   public Properties getSomeProperties()
   {
      return someProperties;
   }

   public void setSomeProperties(Properties someProperties)
   {
      this.someProperties = someProperties;
   }
}
