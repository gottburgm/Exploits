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
package org.jboss.test.cluster.cache.bean;

import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.cache.pojo.PojoCache;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.test.cluster.cache.aop.Address;
import org.jboss.test.cluster.cache.aop.Person;
import org.jboss.util.NestedRuntimeException;

/**
 * Proxy to the TreeCacheAop MBean.
 * The AOP framework requires that classes are loaded by special classloaders (e.g UCL).
 * This bean is used to execute tests within the server.
 *
 * @author Ben Wang
 * @version $Revision: 81036 $
 * @ejb.bean type="Stateful"
 * name="test/TreeCacheAopMBeanTester"
 * jndi-name="ejb/test/TreeCacheAopMBeanTester"
 * view-type="remote"
 * @ejb.transaction type="Supports"
 */

public class TreeCacheAopMBeanTesterBean implements SessionBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1285651899817677886L;
   
   static final String OBJECT_NAME = "jboss.cache:service=testTreeCacheAop";
   MBeanServer server;
   ObjectName cacheService;

   SessionContext ctx;
   PojoCache cache;

   Logger logger_ = Logger.getLogger(TreeCacheAopMBeanTesterBean.class);

   public void ejbActivate() throws EJBException, RemoteException
   {
   }

   public void ejbPassivate() throws EJBException, RemoteException
   {
   }

   public void ejbRemove() throws EJBException, RemoteException
   {
   }

   public void setSessionContext(SessionContext ctx) throws EJBException
   {
      this.ctx = ctx;
   }

   /**
    * @ejb.create-method
    */
   public void ejbCreate() throws CreateException
   {
      init();
   }

   private void init() throws CreateException
   {
      init(OBJECT_NAME);
   }

   private void init(String name) throws CreateException
   {
      try {
         cacheService = new ObjectName(name);
         server = MBeanServerLocator.locate();
      } catch (Exception ex) {
         throw new CreateException(ex.toString());
      }
   }

   /**
    * @ejb.interface-method
    */
   public void createPerson(String key, String name, int age) throws Exception
   {
      Person p = new Person();
      p.setName(name);
      p.setAge(age);
      p.setAddress(new Address());
      server.invoke(cacheService, "putObject",
            new Object[]{key, p},
            new String[]{String.class.getName(),
                         Object.class.getName()});
   }

   /**
    * @ejb.interface-method
    */
   public void removePerson(String key) throws Exception
   {
      server.invoke(cacheService, "removeObject",
            new Object[]{key},
            new String[]{String.class.getName()});
   }


   Object getPerson(String key) throws Exception
   {
      return server.invoke(cacheService, "getObject",
            new Object[]{key},
            new String[]{String.class.getName()});
   }

   /**
    * @ejb.interface-method
    */
   public void setName(String key, String name) throws Exception
   {
      ((Person) getPerson(key)).setName(name);
   }

   /**
    * @ejb.interface-method
    */
   public String getName(String key) throws Exception
   {
      return ((Person) getPerson(key)).getName();
   }

   /**
    * @ejb.interface-method
    */
   public void setAge(String key, int age) throws Exception
   {
      ((Person) getPerson(key)).setAge(age);
   }

   /**
    * @ejb.interface-method
    */
   public int getAge(String key) throws Exception
   {
      return ((Person) getPerson(key)).getAge();
   }

   /**
    * @ejb.interface-method
    */
   public void setStreet(String key, String street)  throws Exception
   {
      ((Person) getPerson(key)).getAddress().setStreet(street);
   }

   /**
    * @ejb.interface-method
    */
   public String getStreet(String key) throws Exception
   {
      return ((Person) getPerson(key)).getAddress().getStreet();
   }

   /**
    * @ejb.interface-method
    */
   public void setCity(String key, String city) throws Exception
   {
      ((Person) getPerson(key)).getAddress().setCity(city);
   }

   /**
    * @ejb.interface-method
    */
   public String getCity(String key)  throws Exception
   {
      return ((Person) getPerson(key)).getAddress().getCity();
   }

   /**
    * @ejb.interface-method
    */
   public void setZip(String key, int zip) throws Exception
   {
      ((Person) getPerson(key)).getAddress().setZip(zip);
   }

   /**
    * @ejb.interface-method
    */
   public int getZip(String key) throws Exception
   {
      return ((Person) getPerson(key)).getAddress().getZip();
   }

   // Map operations

   /**
    * @ejb.interface-method
    */
   public Object getHobby(String key, Object hobbyKey) throws Exception
   {
      Map hobbies = ((Person) getPerson(key)).getHobbies();
      return hobbies == null ? null : hobbies.get(hobbyKey);
   }

   /**
    * @ejb.interface-method
    */
   public void setHobby(String key, Object hobbyKey, Object value) throws Exception
   {
      Person person = ((Person) getPerson(key));
      Map hobbies = person.getHobbies();
      if (hobbies == null) {
         hobbies = new HashMap();
         person.setHobbies(hobbies);
         // NB: it is neccessary to get hobbies again to get advised version
         hobbies = person.getHobbies();
      }
      hobbies.put(hobbyKey, value);
   }

   // List operations

   /**
    * @ejb.interface-method
    */
   public Object getLanguage(String key, int index) throws Exception
   {
      List languages = ((Person) getPerson(key)).getLanguages();
      return languages == null ? null : languages.get(index);
   }

   /**
    * @ejb.interface-method
    */
   public void addLanguage(String key, Object language) throws Exception
   {
      Person person = ((Person) getPerson(key));
      List languages = person.getLanguages();
      if (languages == null) {
         person.setLanguages(new ArrayList());
         languages = person.getLanguages();
      }
      languages.add(language);
   }

   /**
    * @ejb.interface-method
    */
   public void removeLanguage(String key, Object language) throws Exception
   {
      List languages = ((Person) getPerson(key)).getLanguages();
      if (languages == null) return;
      languages.remove(language);
   }

   /**
    * @ejb.interface-method
    */
   public int getLanguagesSize(String key) throws Exception
   {
      List languages = ((Person) getPerson(key)).getLanguages();
      return languages == null ? 0 : languages.size();
   }

   /**
    * @ejb.interface-method
    */
   public Set getSkills(String key) throws Exception
   {
      return new HashSet(((Person) getPerson(key)).getSkills());
   }

   /**
    * @ejb.interface-method
    */
   public void addSkill(String key, String skill) throws Exception
   {
      Person person = ((Person) getPerson(key));
      Set skills = person.getSkills();
      if (skills == null) {
         person.setSkills(new HashSet());
         skills = person.getSkills();
      }
      skills.add(skill);
   }

   /**
    * @ejb.interface-method
    */
   public void removeSkill(String key, String skill) throws Exception
   {
      Person person = ((Person) getPerson(key));
      Set skills = person.getSkills();
      if (skills != null) {
         skills.remove(skill);
      }
   }


   /**
    * @ejb.interface-method
    */
   public void printPerson(String key) throws Exception
   {
      System.out.println(getPerson(key));
   }

   /**
    * @ejb.interface-method
    */
   public void printCache()
   {
      System.out.println(cache);
   }

   /**
    * @ejb.interface-method
    */
   public Object getFieldValue(String key, String name)
   {
      try {
         Object object = cache.find(key);
         Field f = object.getClass().getDeclaredField(name);
         f.setAccessible(true);
         return f.get(object);
      } catch (Exception e) {
         throw new NestedRuntimeException(e);
      }
   }

}

