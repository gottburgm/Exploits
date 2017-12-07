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
import java.util.Calendar;

public class Person implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private Calendar dob;
   private String name;
   private String address;
   private String city;
   private String state;
   private String postal;
   private String employer;
   private boolean notified = false;
    
   private Person() {}
    
   public Person(String name)
   {
      this.name = name;
   }
    
   public void setAddress(String address, String city, String state, String postal)
   {
      this.address = address;
      this.city = city;
      this.state = state;
      this.postal = postal;
   }
    
   public void setDob(Calendar dob)
   {
      this.dob = dob;
   }
    
    public Calendar getDob()
    {
       return dob;
    }
    
    public String getName()
    {
       return name;
    }
    
    public String getAddress()
    {
       return address;
    }
    
    public String getCity()
    {
       return city;
    }
    
    public String getState()
    {
       return state;
    }
    
    public String getPostal()
    {
       return postal;
    }
    
    public void setEmployer(String employer)
    {
       this.employer = employer;
    }
    
    public String getEmployer()
    {
       return employer;
    }
    
    public int getAge()
    {
       Calendar today = Calendar.getInstance();
       int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
       dob.add(Calendar.YEAR, age);        
       if (today.before(dob))
       {
          age--;
       }
       return age;
    }
    
    public void setNotified(boolean notified)
    {
       this.notified = notified;
    }
    
    public boolean getNotified()
    {
       return notified;
    }
    
    public String toString()
    {
       StringBuffer sb = new StringBuffer();
       sb.append("name: " + name);
       if (dob != null)
       {
          sb.append("; dob: " + dob.getTime());
       }
       sb.append("; address: " + address + ", " + city + ", " + state + " " + postal);
       sb.append("; employer: " + employer);
        
       return sb.toString();
    }
}