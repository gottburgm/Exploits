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
package org.jboss.harness;

import java.io.Serializable;

/**
 *  This class defines a serializable object that will be bound 
 *  in the habinding cluster harness test.  There's no significance
 *  to the fields incorporated in this class.
 *   
 * @author <a href="mailto:jgauthier@novell.com">Jerry Gauthier</a>
 * @version $Revision: 81036 $
 */
public class HABindingObject implements Serializable
{
   static final long serialVersionUID = -5987617132918027769L;

   private String m_name;
   private String m_company;
   private String m_location;
   private String m_country;
   private int m_year;
   private boolean m_isManager;
      
   public HABindingObject()
   {
   }   
  
   public void setName(String name)
   {
      m_name = name;
   }
   public String getName()
   {
      return m_name;
   }
   
   public void setCompany(String company)
   {
      m_company = company;
   }
   
   public String getCompany()
   {
      return m_company;
   }
   
   public void setLocation(String location)
   {
      m_location = location;
   }
   
   public String getLocation()
   {
      return m_location;
   }
   
   public void setCountry(String country)
   {
      m_country = country;
   }
   
   public String getCountry()
   {
      return m_country;
   }
   
   public void setYear(int year)
   {
      m_year = year;
   }
   
   public int getYear()
   {
      return m_year;
   }
   
   public void setManager(boolean isManager)
   {
      m_isManager = isManager;
   }
   
   public boolean isManager()
   {
      return m_isManager;
   }
       
   public String toString()
   {
      return("name=" + m_name + ", company=" + m_company + ", location=" + m_location 
               + ", country=" + m_country + ", year=" + m_year);
   }

}
