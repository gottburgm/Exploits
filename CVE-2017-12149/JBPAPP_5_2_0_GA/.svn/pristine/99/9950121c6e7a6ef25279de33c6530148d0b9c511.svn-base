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
 * Data object for bank/Account.
 */
public class AccountData extends java.lang.Object implements java.io.Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -4437159401867282063L;

   private java.lang.String id;

   private java.lang.String customerId;

   private int type;

   private float balance;

   public AccountData()
   {
   }

   public AccountData(java.lang.String id, java.lang.String customerId, int type, float balance)
   {
      setId(id);
      setCustomerId(customerId);
      setType(type);
      setBalance(balance);
   }

   public AccountData(AccountData otherData)
   {
      setId(otherData.getId());
      setCustomerId(otherData.getCustomerId());
      setType(otherData.getType());
      setBalance(otherData.getBalance());

   }

   public org.jboss.test.banknew.interfaces.AccountPK getPrimaryKey()
   {
      org.jboss.test.banknew.interfaces.AccountPK pk = new org.jboss.test.banknew.interfaces.AccountPK(this.getId());
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

   public java.lang.String getCustomerId()
   {
      return this.customerId;
   }

   public void setCustomerId(java.lang.String customerId)
   {
      this.customerId = customerId;
   }

   public int getType()
   {
      return this.type;
   }

   public void setType(int type)
   {
      this.type = type;
   }

   public float getBalance()
   {
      return this.balance;
   }

   public void setBalance(float balance)
   {
      this.balance = balance;
   }

   public String toString()
   {
      StringBuffer str = new StringBuffer("{");

      str.append("id=" + getId() + " " + "customerId=" + getCustomerId() + " " + "type=" + getType() + " " + "balance="
            + getBalance());
      str.append('}');

      return (str.toString());
   }

   public boolean equals(Object pOther)
   {
      if (pOther instanceof AccountData)
      {
         AccountData lTest = (AccountData) pOther;
         boolean lEquals = true;

         if (this.id == null)
         {
            lEquals = lEquals && (lTest.id == null);
         }
         else
         {
            lEquals = lEquals && this.id.equals(lTest.id);
         }
         if (this.customerId == null)
         {
            lEquals = lEquals && (lTest.customerId == null);
         }
         else
         {
            lEquals = lEquals && this.customerId.equals(lTest.customerId);
         }
         lEquals = lEquals && this.type == lTest.type;
         lEquals = lEquals && this.balance == lTest.balance;

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

      result = 37 * result + ((this.customerId != null) ? this.customerId.hashCode() : 0);

      result = 37 * result + type;

      result = 37 * result + Float.floatToIntBits(balance);

      return result;
   }

}
