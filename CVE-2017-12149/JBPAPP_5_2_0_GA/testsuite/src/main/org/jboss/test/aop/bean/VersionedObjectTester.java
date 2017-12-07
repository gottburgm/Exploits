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

import org.jboss.aspects.versioned.Versioned;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.ArrayList;
/**
 *
 * @see Monitorable
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81036 $
 */
public class VersionedObjectTester
   extends ServiceMBeanSupport
   implements VersionedObjectTesterMBean, MBeanRegistration
{
   // Constants ----------------------------------------------------
   // Attributes ---------------------------------------------------
   static Logger log = Logger.getLogger(VersionedObjectTester.class);
   MBeanServer m_mbeanServer;

   // Static -------------------------------------------------------

   // Constructors -------------------------------------------------
   public VersionedObjectTester()
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

   protected void startService()
      throws Exception
   {
   }

   protected void stopService() {
   }

   public void testPerField()
   {
      try
      {
         log.info("TEST PER FIELD VERSIONING");
         Address address = new Address("Marlborough Street", "Boston", "MA");
         Person person = new Person("Bill", 32, address);
         Versioned.makePerFieldVersioned(person);

         log.info("test optimistic lock");
         boolean exceptionThrown = false;
         try
         {
            person.testOptimisticLock();
         }
         catch (Exception ignored)
         {
            exceptionThrown = true;
            log.info("caught exception correctly: " + ignored.getMessage() + " exception type: " + ignored.getClass().getName());
         }

         if (!exceptionThrown) throw new Exception("Did not catch optimistic lock failure");
         if (!person.getName().equals("William")) throw new Exception("optimistic lock failed, field was changed");

         log.info("test rollback");
         exceptionThrown = false;
         try
         {
            person.testRollback();
         }
         catch (Exception ignored)
         {
            exceptionThrown = true;
            log.info("caught exception correctly: " + ignored.getMessage() + " exception type: " + ignored.getClass().getName());
         }
         if (!exceptionThrown) throw new Exception("No rollback happened");
         if (!person.getName().equals("William")) throw new Exception("rollback lock failed, field was changed");

         log.info("test non transactional set");
         person.setName("Burke");
         log.info("see if name was reset");
         if (!person.getName().equals("Burke")) throw new Exception("Failed to setname");

         log.info("test transactional set");
         person.setNameTransactional("Bill");
         if (!person.getName().equals("Bill")) throw new Exception("Failed to setnametransactional");

         log.info("test 2 transactions, 2 different fields");
         person.testDifferentFields();
         if (person.getAge() != 5) throw new Exception("test 2 transactions, 2 different fields failed");
         if (!person.getName().equals("William")) throw new Exception("test 2 transactions, 2 different fields failed");


         log.info("test optimistic lock with embedded object");
         exceptionThrown = false;
         try
         {
            person.testOptimisticLockWithAddress();
         }
         catch (Exception ignored)
         {
            exceptionThrown = true;
            log.info("caught exception correctly: " + ignored.getMessage() + " exception type: " + ignored.getClass().getName());
         }

         if (!exceptionThrown) throw new Exception("Did not catch optimistic lock failure");
         if (!person.getAddress().getCity().equals("Rutland")) throw new Exception("optimistic lock failed, field was changed");


         log.info("test rollback for embedded object");
         exceptionThrown = false;
         try
         {
            person.testRollbackForAddress();
         }
         catch (Exception ignored)
         {
            exceptionThrown = true;
            log.info("caught exception correctly: " + ignored.getMessage() + " exception type: " + ignored.getClass().getName());
         }

         if (!exceptionThrown) throw new Exception("Did not catch optimistic lock failure");
         if (!person.getAddress().getCity().equals("Rutland")) throw new Exception("optimistic lock failed, field was changed");

         log.info("test 2 fields for embedded object");
         person.testDifferentFieldsForAddress();
         if (!person.getAddress().getCity().equals("Rutland")) throw new Exception("field was not changed");
         if (!person.getAddress().getState().equals("VT")) throw new Exception("field was not changed");


         log.info("test list optimistic lock");
         exceptionThrown = false;
         try
         {
            person.testListOptimisticLock();
         }
         catch (Exception ignored)
         {
            exceptionThrown = true;
            log.info("caught exception correctly: " + ignored.getMessage() + " exception type: " + ignored.getClass().getName());
         }

         if (!exceptionThrown) throw new Exception("Did not catch optimistic lock failure");
         ArrayList hobbies = person.getHobbies();
         if (hobbies.size() != 1) throw new Exception("optimistic lock failed, unexpected list size:" + hobbies.size());
         if (!hobbies.get(0).toString().equals("football")) throw new Exception("optimistic lock failed.  unexpected value in list");

         log.info("test list rollback");
         exceptionThrown = false;
         try
         {
            person.testListRollback();
         }
         catch (Exception ignored)
         {
            exceptionThrown = true;
            log.info("caught exception correctly: " + ignored.getMessage() + " exception type: " + ignored.getClass().getName());
         }

         if (!exceptionThrown) throw new Exception("Did not catch optimistic lock failure");
         hobbies = person.getHobbies();
         if (hobbies.size() != 1) throw new Exception("optimistic lock failed, unexpected list size:" + hobbies.size());
         if (!hobbies.get(0).toString().equals("football")) throw new Exception("optimistic lock failed.  unexpected value in list");

         person.addHobby("basketball");
         if (hobbies.size() != 2) throw new Exception("failed to add hobby");

      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex.getMessage());
      }
   }

   // Inner classes -------------------------------------------------
}

