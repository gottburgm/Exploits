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
package org.jboss.test.hibernate.model.v1;

import java.io.Serializable;

/**
 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class Person implements Serializable
{
   private static final long serialVersionUID = 1;
   private String name;
   private String address;
   private int iq;
   private java.util.Date bday;
   private Float number;
   private Long id;

   public Person(int iq)
   {
      this.iq = iq;
   }

   public Person()
   {
   }

   public Long getId()
   {
      return id;
   }

   public void setId(Long id)
   {
      this.id = id;
   }

   /**
    Gets the name
    @return Returns a String
    */
   public String getName()
   {
      return name;
   }

   /**
    Sets the name
    @param name The name to set
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    Gets the address
    @return Returns a String
    */
   public String getAddress()
   {
      return address;
   }

   /**
    Sets the address
    @param address The address to set
    */
   public void setAddress(String address)
   {
      this.address = address;
   }

   /**
    Gets the IQ
    @return Returns a int
    */
   public int getIQ()
   {
      return iq;
   }

   /**
    Sets the IQ
    @param iq The count to set
    */
   public void setIQ(int iq)
   {
      this.iq = iq;
   }

   /**
    Gets the date
    @return Returns a java.util.Date
    */
   public java.util.Date getBDay()
   {
      return bday;
   }

   /**
    Sets the date
    @param bday The date to set
    */
   public void setBDay(java.util.Date bday)
   {
      this.bday = bday;
   }

   /**
    Gets the pay number
    @return Returns a Float
    */
   public Float getPay()
   {
      return number;
   }

   /**
    Sets the pay number
    @param number The Pay to set
    */
   public void setPay(Float number)
   {
      this.number = number;
   }

}
