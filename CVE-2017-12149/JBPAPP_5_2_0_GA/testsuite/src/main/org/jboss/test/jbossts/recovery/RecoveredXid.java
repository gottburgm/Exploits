/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jbossts.recovery;

import java.io.Serializable;
import java.util.Arrays;

public class RecoveredXid implements Serializable
{
   private byte[] branchQualifier;
   private int formatId;
   private byte[] globalTransactionId;
   
   public RecoveredXid()
   {
   }

   public byte[] getBranchQualifier()
   {
      return branchQualifier;
   }

   public void setBranchQualifier(byte[] branchQualifier)
   {
      this.branchQualifier = branchQualifier;
   }

   public int getFormatId()
   {
      return formatId;
   }

   public void setFormatId(int formatId)
   {
      this.formatId = formatId;
   }

   public byte[] getGlobalTransactionId()
   {
      return globalTransactionId;
   }

   public void setGlobalTransactionId(byte[] globalTransactionId)
   {
      this.globalTransactionId = globalTransactionId;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(branchQualifier);
      result = prime * result + formatId;
      result = prime * result + Arrays.hashCode(globalTransactionId);
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      RecoveredXid other = (RecoveredXid) obj;
      if (!Arrays.equals(branchQualifier, other.branchQualifier))
         return false;
      if (formatId != other.formatId)
         return false;
      if (!Arrays.equals(globalTransactionId, other.globalTransactionId))
         return false;
      return true;
   }

   @Override
   /**
    * Only for debugging purposes.
    */
   public String toString()
   {
      return "RecoveredXid [branchQualifier=" + Arrays.toString(branchQualifier) + ", formatId=" + formatId
            + ", globalTransactionId=" + Arrays.toString(globalTransactionId) + "]";
   }
   
}
