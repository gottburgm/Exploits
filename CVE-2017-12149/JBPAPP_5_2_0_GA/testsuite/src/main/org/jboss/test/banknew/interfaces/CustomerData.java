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
package org.jboss.test.banknew.interfaces;

/**
 * Data object for bank/Customer.
 */
public class CustomerData extends java.lang.Object implements java.io.Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 713727152767197901L;

   private java.lang.String id;

   private java.lang.String bankId;

   private java.lang.String name;

   public CustomerData()
   {
   }

   public CustomerData(java.lang.String id, java.lang.String bankId, java.lang.String name)
   {
      setId(id);
      setBankId(bankId);
      setName(name);
   }

   public CustomerData(CustomerData otherData)
   {
      setId(otherData.getId());
      setBankId(otherData.getBankId());
      setName(otherData.getName());

   }

   public org.jboss.test.banknew.interfaces.CustomerPK getPrimaryKey()
   {
      org.jboss.test.banknew.interfaces.CustomerPK pk = new org.jboss.test.banknew.interfaces.CustomerPK(this.getId());
      return pk;
   }

   public java.lang.String getId()
   {
      return this.id;
   }

   public void setId(java.lang.String id)
   {
      this.id = id;
   }

   public java.lang.String getBankId()
   {
      return this.bankId;
   }

   public void setBankId(java.lang.String bankId)
   {
      this.bankId = bankId;
   }

   public java.lang.String getName()
   {
      return this.name;
   }

   public void setName(java.lang.String name)
   {
      this.name = name;
   }

   public String toString()
   {
      StringBuffer str = new StringBuffer("{");

      str.append("id=" + getId() + " " + "bankId=" + getBankId() + " " + "name=" + getName());
      str.append('}');

      return (str.toString());
   }

   public boolean equals(Object pOther)
   {
      if (pOther instanceof CustomerData)
      {
         CustomerData lTest = (CustomerData) pOther;
         boolean lEquals = true;

         if (this.id == null)
         {
            lEquals = lEquals && (lTest.id == null);
         }
         else
         {
            lEquals = lEquals && this.id.equals(lTest.id);
         }
         if (this.bankId == null)
         {
            lEquals = lEquals && (lTest.bankId == null);
         }
         else
         {
            lEquals = lEquals && this.bankId.equals(lTest.bankId);
         }
         if (this.name == null)
         {
            lEquals = lEquals && (lTest.name == null);
         }
         else
         {
            lEquals = lEquals && this.name.equals(lTest.name);
         }

         return lEquals;
      }
      else
      {
         return false;
      }
   }

   public int hashCode()
   {
      int result = 17;

      result = 37 * result + ((this.id != null) ? this.id.hashCode() : 0);

      result = 37 * result + ((this.bankId != null) ? this.bankId.hashCode() : 0);

      result = 37 * result + ((this.name != null) ? this.name.hashCode() : 0);

      return result;
   }

}
