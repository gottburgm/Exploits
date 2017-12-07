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
package org.jboss.test.cluster.hapartition.rpc;

import java.io.Serializable;

/*
 * trivial custom class to be used as parameter
 */
public class PersonQuery implements Serializable
{  
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   
   private String city = null;
   private String state = null;
   private String employer = null;
   
   public PersonQuery() {}

   public void setCity(String city)
   {
      this.city = city;
   }
   
   public String getCity()
   {
      return city;
   }
   
   public void setState(String state)
   {
      this.state = state;
   }
   
   public String getState()
   {
      return state;
   }
   
   public void setEmployer(String employer)
   {
      this.employer = employer;
   }
   
   public String getEmployer()
   {
      return employer;
   }
   
   public boolean isMatch(Person person)
   {
      if (city != null && !city.equalsIgnoreCase(person.getCity()))
         return false;
      if (state != null && !state.equalsIgnoreCase(person.getState()))
         return false;
      if (employer != null && !employer.equalsIgnoreCase(person.getEmployer()))
         return false;
      
      return true;
   }
   
}
