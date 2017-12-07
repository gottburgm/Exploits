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
package org.jboss.test.hibernate.test;

import java.util.Date;
import java.util.List;
import java.util.Iterator;
import javax.naming.InitialContext;

import org.jboss.test.JBossTestCase;

import junit.framework.Test;

/**
 Test of the ejb timers table mapping

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class ScopedEarsUnitTestCase extends JBossTestCase
{
   public ScopedEarsUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   /**
    Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(ScopedEarsUnitTestCase.class, "hib-ear1.ear,hib-ear2.ear");
   }

   public void testEAR1Session() throws Throwable
   {
      InitialContext ctx = getInitialContext();
      org.jboss.test.hibernate.model.v1.IPersonHome home =
         (org.jboss.test.hibernate.model.v1.IPersonHome) ctx.lookup("hibernate/ear1/PersonBean");
      org.jboss.test.hibernate.model.v1.IPerson bean = null;

      try
      {
         bean = home.create();
         bean.init();
         bean.sessionInit();

         int initialCount = bean.listPeople().size();
         assertTrue("People initialCount == 0 ", initialCount == 0 );

         org.jboss.test.hibernate.model.v1.Person jimmy = new org.jboss.test.hibernate.model.v1.Person(197);
         Date bday = new Date(2003, 6, 13);
         jimmy.setBDay(bday);
         jimmy.setName("Jimmy Neutron");
         jimmy.setAddress("Channel 302, Nicktoons USA");
         jimmy.setPay(new Float(123456.789));
         bean.storeUser(jimmy);

         List persons = bean.listPeople();
         assertNotNull(persons);
         assertEquals("Incorrect result size", initialCount + 1, persons.size());

         org.jboss.test.hibernate.model.v1.Person found = null;
         Iterator itr = persons.iterator();
         while (itr.hasNext())
         {
            org.jboss.test.hibernate.model.v1.Person p =
               (org.jboss.test.hibernate.model.v1.Person) itr.next();
            if (p.getName().equals(jimmy.getName()))
            {
               found = p;
            }
         }
         assertNotNull("Found Jimmy in list", found);
      }
      finally
      {
         if (bean != null)
         {
            try
            {
               bean.remove();
            }
            catch (Throwable t)
            {
               // ignore
            }
         }
      }
   }

   public void testEAR2Session() throws Throwable
   {
      InitialContext ctx = getInitialContext();
      org.jboss.test.hibernate.model.v2.IPersonHome home =
         (org.jboss.test.hibernate.model.v2.IPersonHome) ctx.lookup("hibernate/ear2/PersonBean");
      org.jboss.test.hibernate.model.v2.IPerson bean = null;

      try
      {
         bean = home.create();
         bean.init();
         bean.sessionInit();

         int initialCount = bean.listPeople().size();
         assertTrue("People initialCount == 0 ", initialCount == 0 );

         org.jboss.test.hibernate.model.v2.Person jimmy = new org.jboss.test.hibernate.model.v2.Person(197);
         Date bday = new Date(2003, 6, 13);
         jimmy.setBDay(bday);
         jimmy.setName("Jimmy Neutron");
         jimmy.setAddress("Channel 302, Nicktoons USA");
         jimmy.setPay(new Float(123456.789));
         bean.storeUser(jimmy);

         List persons = bean.listPeople();
         assertNotNull(persons);
         assertEquals("Incorrect result size", initialCount + 1, persons.size());

         org.jboss.test.hibernate.model.v2.Person found = null;
         Iterator itr = persons.iterator();
         while (itr.hasNext())
         {
            org.jboss.test.hibernate.model.v2.Person p =
               (org.jboss.test.hibernate.model.v2.Person) itr.next();
            if (p.getName().equals(jimmy.getName()))
            {
               found = p;
            }
         }
         assertNotNull("Found Jimmy in list", found);
      }
      finally
      {
         if (bean != null)
         {
            try
            {
               bean.remove();
            }
            catch (Throwable t)
            {
               // ignore
            }
         }
      }
   }

}
