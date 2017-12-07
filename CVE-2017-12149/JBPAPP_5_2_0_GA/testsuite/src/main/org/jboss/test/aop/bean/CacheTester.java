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

import org.jboss.aspects.versioned.DistributedTxCache;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.id.GUID;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @see Monitorable
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81036 $
 */
public class CacheTester
   extends ServiceMBeanSupport
   implements CacheTesterMBean, MBeanRegistration
{
   // Constants ----------------------------------------------------
   // Attributes ---------------------------------------------------
   static Logger log = Logger.getLogger(CacheTester.class);
   MBeanServer m_mbeanServer;
   DistributedTxCache cache;
   GUID vmid = new GUID();
   // Static -------------------------------------------------------
   
   // Constructors -------------------------------------------------
   public CacheTester()
   {}
   
   // Public -------------------------------------------------------
   
   // MBeanRegistration implementation -----------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
   throws Exception
   {
      m_mbeanServer = server;
      return name;
   }
   
   public void postRegister(Boolean registrationDone)
   {}
   public void preDeregister() throws Exception
   {}
   public void postDeregister()
   {}

   protected void createService()
      throws Exception
   {
      cache = new DistributedTxCache(10, 5000, "Test");
      cache.create();
   }

   protected void startService()
      throws Exception
   {
      cache.start();
      Thread.sleep(5000);
      Person person = (Person)cache.get("Bill");
      if (person != null)
      {
         log.info("Bill found in cache, no need to create");
         log.info(person.getName() + " is " + person.getAge() + " years old");
         log.info("lives at : " + person.getAddress().getStreet());
         log.info(person.getAddress().getCity() + ", " + person.getAddress().getState());
         log.info("hobbies: ");
         Iterator it = person.getHobbies().iterator();
         while (it.hasNext())
         {
            log.info(it.next());
         }
      }
      else
      {
         log.info("inserting stuff");
         Address address = new Address("Marlborough Street", "Boston", "MA");
         person = new Person("Bill", 32, address);
         person.addHobby("Football");
         person.addHobby("Basketball");
         cache.insert("Bill", person);
      }
   }

   protected void stopService() { }

   public String getVMID()
   {
      return vmid.toString();
   }

   public int getAge(String key)
   {
      Person person = (Person)cache.get(key);
      return person.getAge();
   }

   public void setAge(String key, int value)
   {
      Person person = (Person)cache.get(key);
      person.setAge(value);
   }

   public List getHobbies(String key)
   {
      Person person = (Person)cache.get(key);
      return new ArrayList(person.getHobbies());
   }

   public void addHobby(String key, String hobby)
   {
      Person person = (Person)cache.get(key);
      person.addHobby(hobby);
   }

   public String getCity(String key)
   {
      Person person = (Person)cache.get(key);
      return person.getAddress().getCity();
   }

   public void setCity(String key, String city)
   {
      Person person = (Person)cache.get(key);
      person.getAddress().setCity(city);
   }


   // Inner classes -------------------------------------------------
}

