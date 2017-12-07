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
package org.jboss.test.bootstrapdependencies.jbas5349.ejb3;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A simple EJB3 entity.
 * 
 * @author <a href="istudens@redhat.com">Ivo Studensky</a>
 * @version $Revision: 85945 $
 */
@Entity
public class SimpleEntity implements java.io.Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   
   private int pk;

   /** empty constructor */
   public SimpleEntity() {}   
   
   @Id
   public int getPk()
   {
      return pk;
   }
   public void setPk(int pk)
   {
      this.pk = pk;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + pk;
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
      SimpleEntity other = (SimpleEntity) obj;
      if (pk != other.pk)
         return false;
      return true;
   }
   
}

