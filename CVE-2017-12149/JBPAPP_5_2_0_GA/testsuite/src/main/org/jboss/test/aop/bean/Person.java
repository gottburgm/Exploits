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
package org.jboss.test.aop.bean;
import java.util.ArrayList;
/**
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81036 $
 */
public class Person
{
   public Person() {}

   public Person(String name,
                 int age,
                 Address address)
   {
      this.name = name;
      this.age = age;
      this.address = address;
      this.hobbies = new ArrayList();
   }

   private String name;
   private int age;
   private Address address;
   private ArrayList hobbies;

   public void testOptimisticLock()
   {
      name = "Billy";
      requiresNew();
   }

   public void requiresNew()
   {
      name = "William";
   }

   public void testRollback()
   {
      name = "Billy";
      throw new RuntimeException("Roll it back");
   }

   public void setNameTransactional(String newName)
   {
      name = newName;
   }

   public void setName(String newName)
   {
      name = newName;
   }

   public String getName() 
   {
      return name;
   }

   public int getAge() { return age; }
   public void setAge(int newAge) { age = newAge; }

   public void testDifferentFields()
   {
      age = 5;
      requiresNew();
   }

   public void testOptimisticLockWithAddress()
   {
      address.setCity("Billerica");
      requiresNewForAddress();
   }

   public void requiresNewForAddress()
   {
      address.setCity("Rutland");
   }

   
   public void testRollbackForAddress()
   {
      address.setCity("Billerica");
      throw new RuntimeException("Roll it back");
   }

   public void testDifferentFieldsForAddress()
   {
      address.setState("VT");
      requiresNewForAddress();
   }

   public Address getAddress() { return address; }
   public ArrayList getHobbies() { return hobbies; }

   public void testListOptimisticLock()
   {
      hobbies.add("baseball");
      try
      {
         requiresNewForList();
      }
      catch (RuntimeException ex)
      {
         ex.printStackTrace();
         throw ex;
      }
   }

   public void requiresNewForList()
   {
      hobbies.add("football");
   }


   public void testListRollback()
   {
      hobbies.add("tennis");
      throw new RuntimeException("Roll it back");
   }

   public void addHobby(String hobbie)
   {
      hobbies.add(hobbie);
   }
   
}

