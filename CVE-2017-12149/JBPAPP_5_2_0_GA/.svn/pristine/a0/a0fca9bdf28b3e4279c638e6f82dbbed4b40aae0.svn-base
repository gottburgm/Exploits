/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cluster.clusteredentity.classloader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Comment
 * 
 * @author Brian Stansberry
 * @version $Revision: 60233 $
 */
public class AccountHolderPK implements Serializable
{   
   private static final long serialVersionUID = 1L;
   
   private String lastName;
   private String ssn;
   private transient boolean deserialized;
   
   public AccountHolderPK( ) {}
   
   public AccountHolderPK(String lastName, String ssn)
   {
      this.lastName = lastName;
      this.ssn = ssn;
   }
   
   public String getLastName( ) { return this.lastName; }
   public void setLastName(String lastName) { this.lastName = lastName; }
   
   public String getSsn( ) { return ssn; }
   public void setSsn(String ssn) { this.ssn = ssn; }
   
   public boolean equals(Object obj)
   {
      if (obj == this) return true;
      if (!(obj instanceof AccountHolderPK)) return false;
      AccountHolderPK pk = (AccountHolderPK)obj;
      if (!lastName.equals(pk.lastName)) return false;
      if (!ssn.equals(pk.ssn)) return false;
      return true;
   }
   
   public int hashCode( )
   {
      int result = 17;
      result = result * 31 + lastName.hashCode();
      result = result * 31 + ssn.hashCode();
      return result;
   }
   
   public String toString()
   {
      StringBuffer sb = new StringBuffer(getClass().getName());
      sb.append("[lastName=");
      sb.append(lastName);
      sb.append(",ssn=");
      sb.append(ssn);
      sb.append(",deserialized=");
      sb.append(deserialized);
      sb.append("]");
      return sb.toString();
   }
   
   private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
   {
      ois.defaultReadObject();
      deserialized = true;
   }

}
