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
 * Primary key for bank/Transaction.
 */
public class TransactionPK extends java.lang.Object implements java.io.Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -1474116205950583565L;

   private int _hashCode = 0;

   private StringBuffer _toStringValue = null;

   public java.lang.String id;

   public TransactionPK()
   {
   }

   public TransactionPK(java.lang.String id)
   {
      this.id = id;
   }

   public java.lang.String getId()
   {
      return id;
   }

   public void setId(java.lang.String id)
   {
      this.id = id;
      _hashCode = 0;
   }

   public int hashCode()
   {
      if (_hashCode == 0)
      {
         if (this.id != null)
            _hashCode += this.id.hashCode();
      }

      return _hashCode;
   }

   public boolean equals(Object obj)
   {
      if (!(obj instanceof org.jboss.test.banknew.interfaces.TransactionPK))
         return false;

      org.jboss.test.banknew.interfaces.TransactionPK pk = (org.jboss.test.banknew.interfaces.TransactionPK) obj;
      boolean eq = true;

      if (obj == null)
      {
         eq = false;
      }
      else
      {
         if (this.id == null && ((org.jboss.test.banknew.interfaces.TransactionPK) obj).getId() == null)
         {
            eq = true;
         }
         else
         {
            if (this.id == null || ((org.jboss.test.banknew.interfaces.TransactionPK) obj).getId() == null)
            {
               eq = false;
            }
            else
            {
               eq = eq && this.id.equals(pk.id);
            }
         }
      }

      return eq;
   }

   /** @return String representation of this pk in the form of [.field1.field2.field3]. */
   public String toString()
   {
      if (_toStringValue == null)
      {
         _toStringValue = new StringBuffer("[.");
         _toStringValue.append(this.id).append('.');
         _toStringValue.append(']');
      }

      return _toStringValue.toString();
   }

}
