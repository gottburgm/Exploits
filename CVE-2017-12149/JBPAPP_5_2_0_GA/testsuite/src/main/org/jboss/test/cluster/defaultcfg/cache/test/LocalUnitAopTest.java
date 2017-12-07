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
package org.jboss.test.cluster.defaultcfg.cache.test;

import java.util.Set;

import junit.framework.Test;

import org.jboss.aop.Advised;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.cache.pojo.interceptors.AbstractInterceptor;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.cluster.cache.bean.TreeCacheAopTester;
import org.jboss.test.cluster.cache.bean.TreeCacheAopTesterHome;
import org.jboss.test.cluster.cache.aop.Person;


/**
 * LocalUnitTestCase.java
 * <p/>
 * <p/>
 * Created: Mon May 05 17:30:11 2003
 *
 * @version $Id: LocalUnitAopTest.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $
 */

//public class LocalUnitAopTest extends TestCase
public class LocalUnitAopTest extends JBossTestCase
{

//   Logger log = getLog();
   Logger log = Logger.getLogger(LocalUnitAopTest.class);
   TreeCacheAopTester tester;


   public LocalUnitAopTest(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      log.info("setUp() ....");
      TreeCacheAopTesterHome home = (TreeCacheAopTesterHome)
            getInitialContext().lookup(TreeCacheAopTesterHome.JNDI_NAME);
      tester = home.create(null, // no need for cluster name
            null, // no need for properties
            1);
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
      tester.remove();
   }

   public void testDummy()
   {
   }

   public void XtestSetup()
   {
      log.info("testSetup() ....");
      try {
         tester.testSetup();
      } catch (Exception ex) {
         ex.printStackTrace();
         fail("testSetup(): fails. " + ex.toString());
      }
   }

   public void XtestSimple() throws Exception
   {
      log.info("testSimple() ....");
      tester.createPerson("/person/test1", "Harald Gliebe", 32);
      assertEquals(tester.getName("/person/test1"), "Harald Gliebe");
      assertTrue(tester.getAge("/person/test1") == 32);
      tester.removePerson("/person/test1");
   }

   public void XtestModification() throws Exception
   {
      tester.createPerson("/person/test2", "Harald", 32);
      tester.setName("/person/test2", "Harald Gliebe");
      assertEquals(tester.getName("/person/test2"), "Harald Gliebe");
      tester.removePerson("/person/test2");
   }

   public void XtestRemove() throws Exception
   {
      tester.createPerson("/person/test3", "Harald", 32);
      tester.removePerson("/person/test3");
      try {
         tester.getName("/person/test3");
         fail("Object wasn't removed");
      } catch (Exception e) {
         // should be thrown
      }
   }

   public void XtestDependent() throws Exception
   {
      tester.createPerson("/person/test4", "Harald Gliebe", 32);
      tester.setCity("/person/test4", "Mannheim");
      assertEquals(tester.getCity("/person/test4"), "Mannheim");
   }


   public void XtestSerialization() throws Throwable
   {

      Person p = (Person) tester.testSerialization();
      if (p instanceof Advised) {
         InstanceAdvisor advisor = ((Advised) p)._getInstanceAdvisor();
         org.jboss.aop.advice.Interceptor[] interceptors = advisor.getInterceptors();
         for (int i = 0; i < interceptors.length; i++) {
            assertTrue("CacheInterceptor shouldn't be serialized",
                  !(interceptors[i] instanceof AbstractInterceptor));
         }
      }
      assertEquals("Harald Gliebe", p.getName());
      assertEquals("Mannheim", p.getAddress().getCity());
   }

   public void XtestDeserialization() throws Throwable
   {

      Person p = new Person();
      p.setName("test6");
      tester.testDeserialization("/person/test6", p);
      String name = tester.getName("/person/test6");
      assertEquals("test6", name);
   }

   public void XtestMap() throws Throwable
   {
      tester.createPerson("/person/test5", "Harald Gliebe", 32);
      tester.setHobby("/person/test5", "music", "guitar");
      Object val = tester.getHobby("/person/test5", "music");
      assertEquals("guitar", val);
      tester.setHobby("/person/test5", "a", "b");
      tester.getHobby("/person/test5", "a");
      tester.printPerson("/person/test5");
   }

   public void XtestList() throws Throwable
   {
      tester.createPerson("/person/test6", "p6", 50);
      tester.addLanguage("/person/test6", "German");
      tester.addLanguage("/person/test6", "English");
      tester.addLanguage("/person/test6", "French");
      int size = tester.getLanguagesSize("/person/test6");
      assertTrue(size == 3);
      tester.printCache();
      tester.addLanguage("/person/test6", "asdf");
      tester.printCache();
      tester.removeLanguage("/person/test6", "asdf");
      tester.printCache();
      size = tester.getLanguagesSize("/person/test6");
      //	assertTrue(size==3);
      for (int i = 0; i < size; i++) {
         log.debug("" + i + " : " + tester.getLanguage("/person/test6", i));
      }
      assertEquals(new Integer(3), new Integer(size));
      String language = (String) tester.getLanguage("/person/test6", 1);
      assertEquals("English", language);
   }

   public void XtestSet() throws Throwable
   {
      tester.createPerson("/person/test7", "p7", 27);
      tester.addSkill("/person/test7", "Java");
      tester.addSkill("/person/test7", "Java");
      tester.addSkill("/person/test7", "Java");
      Set skills = tester.getSkills("/person/test7");
      assertEquals(new Integer(1), new Integer(skills.size()));
      tester.removeSkill("/person/test7", "Java");
      skills = tester.getSkills("/person/test7");
      assertTrue(skills.isEmpty());
      tester.addSkill("/person/test7", "Java");
      tester.addSkill("/person/test7", "J2EE");
      tester.addSkill("/person/test7", "JBoss");
      skills = tester.getSkills("/person/test7");
      assertEquals(new Integer(3), new Integer(skills.size()));
   }

   public void XtestFieldSynchronization() throws Throwable
   {
      String key = "/person/test8";
      tester.createPerson(key, "p8", 8);
      assertEquals(tester.getName(key), tester.getFieldValue(key, "name"));
      assertEquals(new Integer(tester.getAge(key)), tester.getFieldValue(key, "age"));
      tester.setName(key, "p8x");
      assertEquals(tester.getName(key), tester.getFieldValue(key, "name"));
      tester.setAge(key, 18);
      assertEquals(new Integer(tester.getAge(key)), tester.getFieldValue(key, "age"));
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(getDeploySetup(LocalUnitAopTest.class,
            "cachetest.jar"),
            "cachetest.aop");
//        return new TestSuite(LocalUnitAopTest.class);
   }


   public static void main(String[] args) throws Exception
   {
      junit.textui.TestRunner.run(suite());
   }

}

