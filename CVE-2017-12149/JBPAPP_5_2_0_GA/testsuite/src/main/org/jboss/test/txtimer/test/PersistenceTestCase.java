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
package org.jboss.test.txtimer.test;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.framework.TestSuite;

import org.jboss.ejb.txtimer.DatabasePersistencePolicyMBean;
import org.jboss.ejb.txtimer.EJBTimerService;
import org.jboss.ejb.txtimer.PersistencePolicy;
import org.jboss.ejb.txtimer.TimedObjectId;
import org.jboss.ejb.txtimer.TimerHandleImpl;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.test.JBossTestCase;
import org.jboss.test.txtimer.interfaces.TimerEntity;
import org.jboss.test.txtimer.interfaces.TimerEntityHome;
import org.jboss.test.txtimer.interfaces.TimerSession;
import org.jboss.test.txtimer.interfaces.TimerSessionHome;

/**
 * Test the Tx timer service with an Entity.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 81036 $
 * @since 07-Apr-2004
 */
public class PersistenceTestCase extends JBossTestCase
{
   private PersistencePolicy pp;

   public PersistenceTestCase(String name)
   {
      super(name);
   }

   public static TestSuite suite() throws Exception
   {
      TestSuite ts = new TestSuite();
      ts.addTest(getDeploySetup(PersistenceTestCase.class, "ejb-txtimer.jar"));
      return ts;
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      pp = new PersistencePolicyDelegate(getServer());
   }

   /**
    * Test that ejbTimeout is called
    */
   public void testSingleEventDuration() throws Exception
   {
      // check that there are no timers persisted
      List timerHandles = pp.listTimerHandles();
      assertEquals("unexpected handle count", 0, timerHandles.size());

      InitialContext iniCtx = getInitialContext();
      TimerEntityHome home = (TimerEntityHome)iniCtx.lookup(TimerEntityHome.JNDI_NAME);
      TimerEntity entity = home.create(new Integer(1));
      try
      {
         entity.createTimer(500, 0, null);

         timerHandles = pp.listTimerHandles();
         assertEquals("unexpected handle count", 1, timerHandles.size());

         sleep(1000);
         assertEquals("unexpected call count", 1, entity.getCallCount());

         timerHandles = pp.listTimerHandles();
         assertEquals("unexpected handle count", 0, timerHandles.size());
      }
      finally
      {
         entity.remove();
      }
   }

   /**
    * Insert a timer for an EB and fake rstore after server startup
    */
   public void testRestoreToEntity() throws Exception
   {
      // check that there are no timers persisted
      List timerHandles = pp.listTimerHandles();
      assertEquals("unexpected handle count", 0, timerHandles.size());

      InitialContext iniCtx = getInitialContext();
      TimerEntityHome home = (TimerEntityHome)iniCtx.lookup(TimerEntityHome.JNDI_NAME);
      TimerEntity entity = home.create(new Integer(1));
      try
      {
         // insert a timer into the db
         ObjectName oname = ObjectNameFactory.create("jboss.j2ee:jndiName=test/txtimer/TimerEntity,service=EJB");
         TimedObjectId targetId = new TimedObjectId(oname, new Integer(1));
         pp.insertTimer("pk1", targetId, new Date(), 0, null);
         sleep(500);

         timerHandles = pp.listTimerHandles();
         assertEquals("unexpected handle count", 1, timerHandles.size());

         // fake restore on server startup
         // we cannot test that the PersistencePolicy is notified after startup
         try
         {
            getServer().invoke(
                  EJBTimerService.OBJECT_NAME,
                  "restoreTimers", 
                  new Object[] { oname, null },
                  new String[]{"javax.management.ObjectName", "java.lang.ClassLoader" }
                  );
         }
         catch (Exception e)
         {
            log.warn("Could not restore ejb timers", e);
         }
         sleep(500);
         assertEquals("unexpected call count", 1, entity.getCallCount());

         timerHandles = pp.listTimerHandles();
         assertEquals("unexpected handle count", 0, timerHandles.size());
      }
      finally
      {
         entity.remove();
      }
   }

   /**
    * Insert a timer for a SLSB and fake rstore after server startup
    */
   public void testRestoreToSession() throws Exception
   {
      // check that there are no timers persisted
      List timerHandles = pp.listTimerHandles();
      assertEquals("unexpected handle count", 0, timerHandles.size());

      InitialContext iniCtx = getInitialContext();
      TimerSessionHome home = (TimerSessionHome)iniCtx.lookup(TimerSessionHome.JNDI_NAME);
      TimerSession session = home.create();
      session.resetCallCount();
      try
      {
         // insert a timer into the db
         ObjectName oname = ObjectNameFactory.create("jboss.j2ee:jndiName=test/txtimer/TimerSession,service=EJB");
         TimedObjectId targetId = new TimedObjectId(oname);
         pp.insertTimer("pk1", targetId, new Date(), 0, null);
         sleep(500);

         timerHandles = pp.listTimerHandles();
         assertEquals("unexpected handle count", 1, timerHandles.size());

         // fake restore on server startup
         // we cannot test that the PersistencePolicy is notified after startup
         try
         {
            getServer().invoke(
                  EJBTimerService.OBJECT_NAME,
                  "restoreTimers", 
                  new Object[] { oname, null },
                  new String[]{"javax.management.ObjectName", "java.lang.ClassLoader" }
                  );
         }
         catch (Exception e)
         {
            log.warn("Could not restore ejb timers", e);
         }
         sleep(500);
         assertEquals("unexpected call count", 1, session.getGlobalCallCount());

         timerHandles = pp.listTimerHandles();
         assertEquals("unexpected handle count", 0, timerHandles.size());
      }
      finally
      {
         session.remove();
      }
   }

   /**
    * Test the database roundtrip
    */
   public void testPersistenceEquality() throws Exception
   {
      // check that there are no timers persisted
      List timerHandles = pp.listTimerHandles();
      assertEquals("unexpected handle count", 0, timerHandles.size());

      // insert a timer into the db
      String timerId = "pk1";
      ObjectName oname = ObjectNameFactory.create("jboss.j2ee:jndiName=test/txtimer/TimerEntity,service=EJB");
      TimedObjectId targetId = new TimedObjectId(oname, new Integer(100));
      Date firstEvent = new Date ();
      String info = "info";
      pp.insertTimer(timerId, targetId, firstEvent, 100, info);

      timerHandles = pp.listTimerHandles();
      assertEquals("unexpected handle count", 1, timerHandles.size());

      TimerHandleImpl handle = (TimerHandleImpl)timerHandles.get(0);
      assertEquals("TimerId is not equal", timerId, handle.getTimerId());
      assertEquals("TimedObjectId is not equal", targetId, handle.getTimedObjectId());
      assertEquals("firstEvent is not equal", firstEvent, handle.getFirstTime());
      assertEquals("periode is not equal", 100, handle.getPeriode());
      assertEquals("info is not equal", info, handle.getInfo());

      pp.clearTimers();

      timerHandles = pp.listTimerHandles();
      assertEquals("unexpected handle count", 0, timerHandles.size());
   }

   /**
    * Make the invokations to the MBeanConnection typesafe
    */
   static class PersistencePolicyDelegate implements PersistencePolicy
   {
      DatabasePersistencePolicyMBean proxy;

      public PersistencePolicyDelegate(MBeanServerConnection server)
      {
         proxy = (DatabasePersistencePolicyMBean)
            MBeanServerInvocationHandler.newProxyInstance(server,
            DatabasePersistencePolicyMBean.OBJECT_NAME,
            DatabasePersistencePolicyMBean.class, false);
      }

      public void insertTimer(String timerId, TimedObjectId targetId, Date firstEvent, long periode, Serializable info)
      {
         try
         {
            proxy.insertTimer(timerId, targetId, firstEvent, periode, info);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }

      public void deleteTimer(String timerId, TimedObjectId targetId)
      {
         try
         {
            proxy.deleteTimer(timerId, targetId);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }

      public void clearTimers()
      {
         try
         {
            proxy.clearTimers();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }

      public void restoreTimers()
      {
         try
         {
            proxy.resetAndRestoreTimers();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }

      public List listTimerHandles(ObjectName containerId, ClassLoader loader)
      {
         List list = null;
         try
         {
            list = proxy.listTimerHandles(containerId, loader);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
         return list;
      }
      
      public List listTimerHandles()
      {
         List list = null;
         try
         {
            list = proxy.listTimerHandles();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
         return list;
      }
   }
}
