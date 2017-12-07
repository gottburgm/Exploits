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
package org.jboss.test.cluster.clusteredentity.embeddedid;

import java.io.Serializable;

import javax.persistence.Embeddable;

/**
 * Primary key for the {@link Musician} entity.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1.1 $
 */
@Embeddable
public class MusicianPK implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   private String firstName;
   private String lastName;
   private String ssn;
   
   /**
    * Default constructor
    */
   public MusicianPK() {}
   
   public MusicianPK(String firstName, String lastName, String ssn)
   {
      this.firstName = firstName;
      this.lastName = lastName;
      this.ssn = ssn;
   }
   
   public String getFirstName()
   {
      return firstName;
   }
   
   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }
   
   public String getLastName()
   {
      return lastName;
   }
   
   public void setLastName(String lastName)
   {
      this.lastName = lastName;
   }
   
   public String getSsn()
   {
      return ssn;
   }
   
   public void setSsn(String ssn)
   {
      this.ssn = ssn;
   }

   @Override
   public boolean equals(Object obj)
   {
      boolean equal = (this == obj);
      
      if (!equal && obj instanceof MusicianPK)
      {
         MusicianPK other = (MusicianPK) obj;
         
         equal = firstName.equals(other.firstName)
                  && lastName.equals(other.lastName)
                  && ssn.equals(other.ssn);
      }
      return equal;
   }

   @Override
   public int hashCode()
   {
      int result = 19;
      result = result * 29 + firstName.hashCode();
      result = result * 29 + lastName.hashCode();
      result = result * 29 + ssn.hashCode();
      return result;
   }

   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer(getClass().getName());
      sb.append("[firstName=");
      sb.append(firstName);
      sb.append(",lastName=");
      sb.append(lastName);
      sb.append(",ssn=");
      sb.append(ssn);
      sb.append("]");
      return sb.toString();
   }
   
   
   
}
