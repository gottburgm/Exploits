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
 * Data object for bank/Transaction.
 */
public class TransactionData extends java.lang.Object implements java.io.Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 8601981534267975114L;

   private java.lang.String id;

   private java.lang.String acountId;

   private int type;

   private float amount;

   private java.util.Date date;

   private java.lang.String description;

   public TransactionData()
   {
   }

   public TransactionData(java.lang.String id, java.lang.String acountId, int type, float amount, java.util.Date date,
         java.lang.String description)
   {
      setId(id);
      setAcountId(acountId);
      setType(type);
      setAmount(amount);
      setDate(date);
      setDescription(description);
   }

   public TransactionData(TransactionData otherData)
   {
      setId(otherData.getId());
      setAcountId(otherData.getAcountId());
      setType(otherData.getType());
      setAmount(otherData.getAmount());
      setDate(otherData.getDate());
      setDescription(otherData.getDescription());

   }

   public org.jboss.test.banknew.interfaces.TransactionPK getPrimaryKey()
   {
      org.jboss.test.banknew.interfaces.TransactionPK pk = new org.jboss.test.banknew.interfaces.TransactionPK(this
            .getId());
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

   public java.lang.String getAcountId()
   {
      return this.acountId;
   }

   public void setAcountId(java.lang.String acountId)
   {
      this.acountId = acountId;
   }

   public int getType()
   {
      return this.type;
   }

   public void setType(int type)
   {
      this.type = type;
   }

   public float getAmount()
   {
      return this.amount;
   }

   public void setAmount(float amount)
   {
      this.amount = amount;
   }

   public java.util.Date getDate()
   {
      return this.date;
   }

   public void setDate(java.util.Date date)
   {
      this.date = date;
   }

   public java.lang.String getDescription()
   {
      return this.description;
   }

   public void setDescription(java.lang.String description)
   {
      this.description = description;
   }

   public String toString()
   {
      StringBuffer str = new StringBuffer("{");

      str.append("id=" + getId() + " " + "acountId=" + getAcountId() + " " + "type=" + getType() + " " + "amount="
            + getAmount() + " " + "date=" + getDate() + " " + "description=" + getDescription());
      str.append('}');

      return (str.toString());
   }

   public boolean equals(Object pOther)
   {
      if (pOther instanceof TransactionData)
      {
         TransactionData lTest = (TransactionData) pOther;
         boolean lEquals = true;

         if (this.id == null)
         {
            lEquals = lEquals && (lTest.id == null);
         }
         else
         {
            lEquals = lEquals && this.id.equals(lTest.id);
         }
         if (this.acountId == null)
         {
            lEquals = lEquals && (lTest.acountId == null);
         }
         else
         {
            lEquals = lEquals && this.acountId.equals(lTest.acountId);
         }
         lEquals = lEquals && this.type == lTest.type;
         lEquals = lEquals && this.amount == lTest.amount;
         if (this.date == null)
         {
            lEquals = lEquals && (lTest.date == null);
         }
         else
         {
            lEquals = lEquals && this.date.equals(lTest.date);
         }
         if (this.description == null)
         {
            lEquals = lEquals && (lTest.description == null);
         }
         else
         {
            lEquals = lEquals && this.description.equals(lTest.description);
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

      result = 37 * result + ((this.acountId != null) ? this.acountId.hashCode() : 0);

      result = 37 * result + type;

      result = 37 * result + Float.floatToIntBits(amount);

      result = 37 * result + ((this.date != null) ? this.date.hashCode() : 0);

      result = 37 * result + ((this.description != null) ? this.description.hashCode() : 0);

      return result;
   }

}
