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
package org.jboss.test.cluster.web.aop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.cache.pojo.annotation.Replicable;


/**
 * Test class for PojoCache.
 * Person is a POJO that will be instrumented with CacheInterceptor
 *
 * @version $Revision: 81036 $
 */
@Replicable
public class Person
{
   String name = null;
   int age = 0;
   Map hobbies = null;
   Address address = null;
   Set skills;
   List languages;
   // Test for transient field non-replication
   transient String currentStatus = "Active";
   // Test swapping out the Collection ref with proxy one
   // medication will be different when age limit is exceeded.
   List medication = null;
   Color[] favoriteColors = new Color[2];
   static final int AGE1 = 50;
   static final int AGE2 = 60;

   public Person() {
      addFavoriteColor("Blue");
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void setCurrentStatus(String status) {
      currentStatus = status;
   }

   public String getCurrentStatus() {
      return currentStatus;
   }

   public void setName(Object obj)
   {
      this.name = (String)obj;
   }

   public int getAge()
   {
      return age;
   }

   public void setAge(int age)
   {

      this.age = age;

      // This will swap out the reference dynamically
      if(age < AGE1) {
         if(medication != null) {
            medication.clear();
            medication=null;
         }
      }
      else {
         if( age >= AGE1 ) {
            addMedication("Lipitor");
         }

         if (age >= AGE2) {
            addMedication("Vioxx");
         }
      }


   }

   void addMedication(String name) {
      if( medication == null )
         medication = new ArrayList();
      if(!medication.contains(name))
         medication.add(name);
   }

   public Map getHobbies()
   {
      return hobbies;
   }

   public void setHobbies(Map hobbies)
   {
      this.hobbies = hobbies;
   }

   public Address getAddress()
   {
      return address;
   }

   public void setAddress(Address address)
   {
      this.address = address;
   }

   public Set getSkills()
   {
      return skills;
   }

   public void setSkills(Set skills)
   {
      this.skills = skills;
   }

   public List getMedication()
   {
      return medication;
   }

   public void setMedication(List medication)
   {
      this.medication = medication;
   }

   public List getLanguages()
   {
      return languages;
   }

   public void setLanguages(List languages)
   {
      this.languages = languages;
   }
   
   public String getFavoriteColors()
   {
      StringBuilder sb = new StringBuilder();
      boolean wrote = false;
      for (Color color : favoriteColors)
      {
         if (color != null)
         {
            if (wrote)
               sb.append(',');
            sb.append(color);
            wrote = true;
         }
         else
         {
            break;
         }
      }
      
      return sb.toString();
   }
   
   public void addFavoriteColor(String color)
   {
      Color newColor = new Color(color);
      Color oldFavorite = favoriteColors[0];
      if (oldFavorite == null)
      {
         favoriteColors[0] = newColor;
      }
      else if (newColor.equals(oldFavorite) == false)
      {
         favoriteColors[1] = oldFavorite;
         favoriteColors[0] = newColor;
      }
   }

   public String toString()
   {
      StringBuffer sb=new StringBuffer();
      sb.append("name=").append(getName()).append(", age=").append(getAge()).append(", hobbies=")
            .append(print(getHobbies())).append(", address=").append(getAddress()).append(", skills=")
            .append(skills).append(", languages=").append(languages)
            .append(", favoriteColors=").append(getFavoriteColors()).toString();
      if(medication != null)
         sb.append(", medication=" + medication);
      return sb.toString();
   }

   public String print(Map m)
   {
      StringBuffer sb = new StringBuffer();
      Map.Entry entry;
      if (m != null) {
         for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
            entry = (Map.Entry) it.next();
            sb.append(entry.getKey()).append(": ").append(entry.getValue());
            sb.append("\n");
         }
      }
      return sb.toString();
   }
}
