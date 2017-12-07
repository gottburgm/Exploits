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

import org.jboss.aop.Advised;
import org.jboss.cache.config.Configuration;
import org.jboss.cache.pojo.PojoCache;
import org.jboss.cache.pojo.PojoCacheFactory;
import org.jboss.logging.Logger;
import org.jboss.test.cluster.cache.aop.Address;
import org.jboss.test.cluster.cache.aop.Person;
import org.jboss.util.NestedRuntimeException;

/**
 * Proxy to the TreeCacheAop MBean.
 * The AOP framework requires that classes are loaded by special classloaders (e.g UCL).
 * This bean is used to execute tests within the server.
 *
 * @author <a href="mailto:harald@gliebe.de">Harald Gliebe</a>
 * @version $Revision: 81036 $
 * @ejb.bean type="Stateful"
 * name="test/TreeCacheAopTester"
 * jndi-name="test/TreeCacheAopTester"
 * view-type="remote"
 * @ejb.transaction type="Supports"
 */

public class TreeCacheAopTesterBean implements SessionBean
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -4783977385735822976L;
   SessionContext ctx;
   PojoCache cache;
   PojoCache cache2;

   Logger logger_ = Logger.getLogger(TreeCacheAopTesterBean.class);

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
   public void ejbCreate(String cluster_name, String props, int caching_mode) throws CreateException
   {
      try {
         Configuration config = new Configuration();
         config.setClusterName(cluster_name);
         config.setClusterConfig(props);
         config.setCacheMode(Configuration.legacyModeToCacheMode(caching_mode));
         cache = PojoCacheFactory.createCache(config, false);
         cache.start();
         cache2 = PojoCacheFactory.createCache(config, false);
         cache2.start();
      } catch (Exception e) {
         throw new CreateException(e.toString());
      }
   }


   /**
    * @ejb.interface-method
    */
   public void testSetup()
   {
      Person p = new Person();
      if (!(p instanceof Advised)) {
         logger_.error("testSetup(): p is not an instance of Advised");
         throw new RuntimeException("Person must be advised!");
      }
      Address a = new Address();
      if (!(a instanceof Advised)) {
         logger_.error("testSetup(): a is not an instance of Advised");
         throw new RuntimeException("Address must be advised!");
      }
   }

   /**
    * @ejb.interface-method
    */
   public void createPerson(String key, String name, int age)
   {
      Person p = new Person();
      p.setName(name);
      p.setAge(age);
      p.setAddress(new Address());
      try {
         cache.attach(key, p);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * @ejb.interface-method
    */
   public void removePerson(String key)
   {
      try {
         cache.detach(key);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }


   Object getPerson(String key)
   {
      try {
         return (Person) cache.find(key);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * @ejb.interface-method
    */
   public void setName(String key, String name)
   {
      ((Person) getPerson(key)).setName(name);
   }

   /**
    * @ejb.interface-method
    */
   public String getName(String key)
   {
      return ((Person) getPerson(key)).getName();
   }

   /**
    * @ejb.interface-method
    */
   public void setAge(String key, int age)
   {
      ((Person) getPerson(key)).setAge(age);
   }

   /**
    * @ejb.interface-method
    */
   public int getAge(String key)
   {
      return ((Person) getPerson(key)).getAge();
   }

   /**
    * @ejb.interface-method
    */
   public void setStreet(String key, String street)
   {
      ((Person) getPerson(key)).getAddress().setStreet(street);
   }

   /**
    * @ejb.interface-method
    */
   public String getStreet(String key)
   {
      return ((Person) getPerson(key)).getAddress().getStreet();
   }

   /**
    * @ejb.interface-method
    */
   public void setCity(String key, String city)
   {
      ((Person) getPerson(key)).getAddress().setCity(city);
   }

   /**
    * @ejb.interface-method
    */
   public String getCity(String key)
   {
      return ((Person) getPerson(key)).getAddress().getCity();
   }

   /**
    * @ejb.interface-method
    */
   public void setZip(String key, int zip)
   {
      ((Person) getPerson(key)).getAddress().setZip(zip);
   }

   /**
    * @ejb.interface-method
    */
   public int getZip(String key)
   {
      return ((Person) getPerson(key)).getAddress().getZip();
   }

   // Map operations

   /**
    * @ejb.interface-method
    */
   public Object getHobby(String key, Object hobbyKey)
   {
      Map hobbies = ((Person) getPerson(key)).getHobbies();
      return hobbies == null ? null : hobbies.get(hobbyKey);
   }

   /**
    * @ejb.interface-method
    */
   public void setHobby(String key, Object hobbyKey, Object value)
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
   public Object getLanguage(String key, int index)
   {
      List languages = ((Person) getPerson(key)).getLanguages();
      return languages == null ? null : languages.get(index);
   }

   /**
    * @ejb.interface-method
    */
   public void addLanguage(String key, Object language)
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
   public void removeLanguage(String key, Object language)
   {
      List languages = ((Person) getPerson(key)).getLanguages();
      if (languages == null) return;
      languages.remove(language);
   }

   /**
    * @ejb.interface-method
    */
   public int getLanguagesSize(String key)
   {
      List languages = ((Person) getPerson(key)).getLanguages();
      return languages == null ? 0 : languages.size();
   }

   /**
    * @ejb.interface-method
    */
   public Set getSkills(String key)
   {
      return new HashSet(((Person) getPerson(key)).getSkills());
   }

   /**
    * @ejb.interface-method
    */
   public void addSkill(String key, String skill)
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
   public void removeSkill(String key, String skill)
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
   public Object testSerialization()
   {
      try {
         Person p = new Person();
         /*
         if (!(p instanceof Externalizable)) {
        throw new RuntimeException("p not Externalizable");
         }
         */
         p.setName("Harald Gliebe");
         Address address = new Address();
         address.setCity("Mannheim");
         p.setAddress(address);
         cache.attach("/person/harald", p);
         return (Person) cache.find("/person/harald");
      } catch (Throwable t) {
         throw new RuntimeException(t);
      }
   }

   /**
    * @ejb.interface-method
    */
   public void testDeserialization(String key, Object value)
   {
      try {
         cache.attach(key, value);
      } catch (Throwable t) {
         throw new RuntimeException(t);
      }
   }

   /**
    * @ejb.interface-method
    */
   public void printPerson(String key)
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

