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

import java.security.Principal;
import java.util.Properties;

import javax.ejb.TimerHandle;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.framework.TestSuite;

import org.jboss.ejb.txtimer.TimerHandleImpl;
import org.jboss.test.JBossTestCase;
import org.jboss.test.txtimer.interfaces.TimerEntity;
import org.jboss.test.txtimer.interfaces.TimerEntityHome;
import org.jboss.test.txtimer.interfaces.TimerFacade;
import org.jboss.test.txtimer.interfaces.TimerFacadeHome;

/**
 * Test the Tx timer service with an Entity.
 * 
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 81036 $
 * @since 07-Apr-2004
 */
public class EntityBeanTestCase extends JBossTestCase
{
   public EntityBeanTestCase(String name)
   {
      super(name);
   }

   public static TestSuite suite() throws Exception
   {
      TestSuite ts = new TestSuite();
      ts.addTest(getDeploySetup(EntityBeanTestCase.class, "ejb-txtimer.jar"));
      return ts;
   }

   /**
    * Test that ejbTimeout is called
    */
   public void testSingleEventDuration() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerEntityHome home = (TimerEntityHome) iniCtx.lookup(TimerEntityHome.JNDI_NAME);
      TimerEntity entity = home.create(new Integer(1));
      try
      {
         entity.createTimer(500, 0, null);
         sleep(1000);
         assertEquals("unexpected call count", 1, entity.getCallCount());
      }
      finally
      {
         entity.remove();
      }
   }

   /**
    * Test that ejbTimeout is called once on each instance
    */
   public void testInstanceAsscociation() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerEntityHome home = (TimerEntityHome) iniCtx.lookup(TimerEntityHome.JNDI_NAME);
      TimerEntity entity1 = home.create(new Integer(1));
      TimerEntity entity2 = home.create(new Integer(2));

      try
      {
         entity1.createTimer(500, 1000, null);
         entity2.createTimer(500, 1000, null);
         sleep(1000);
         assertEquals("unexpected call count", 1, entity1.getCallCount());
         assertEquals("unexpected call count", 1, entity2.getCallCount());
      }
      finally
      {
         entity1.remove();
         entity2.remove();
      }
   }

   /**
    * Test that ejbTimeout see the container's default principal
    */
   public void testEjbTimeoutCallerPrincipal() throws Exception
   {
      MBeanServerConnection server = getServer();
      String defaultPrincipal = (String) server.getAttribute(new ObjectName("jboss.security:service=JaasSecurityManager"), "DefaultUnauthenticatedPrincipal");

      InitialContext iniCtx = getInitialContext();
      TimerEntityHome home = (TimerEntityHome) iniCtx.lookup(TimerEntityHome.JNDI_NAME);
      TimerEntity entity = home.create(new Integer(1));
      try
      {
         entity.createTimer(500, 0, null);
         sleep(1000);
         assertEquals("unexpected call count", 1, entity.getCallCount());

         Principal callerPrincipal = entity.getEjbTimeoutCaller();
         assertEquals("unexpected principal", defaultPrincipal, callerPrincipal.getName());
      }
      finally
      {
         entity.remove();
      }
   }

   /**
    * The TimerHandle sould not pass through the remote interface
    */
   public void testReturnTimerHandle() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerEntityHome home = (TimerEntityHome) iniCtx.lookup(TimerEntityHome.JNDI_NAME);
      TimerEntity entity = home.create(new Integer(1));
      try
      {

         try
         {
            TimerHandle handle = (TimerHandle) entity.createTimerReturnHandle(500);
            fail("TimerHandle should not pass through the remote interface: " + handle);
         }
         catch (Exception e)
         {
            assertTrue("Timer list should be empty", entity.getTimers().size() == 0);
            sleep(1000);
            assertEquals("unexpected call count", 0, entity.getCallCount());
         }

         try
         {
            TimerHandle handle = TimerHandleImpl.parse("[[id=jboss.j2ee:jndiName=test/txtimer/TimerEntity,service=EJB,pk=1],created=10-Apr-2004 20:16:11.000,first=10-Apr-2004 20:16:11.000,periode=0]");
            String retStr = entity.passTimerHandle(handle);
            fail("TimerHandle should not pass through the remote interface: " + retStr);
         }
         catch (Exception e)
         {
            assertTrue("Timer list should be empty", entity.getTimers().size() == 0);
            sleep(1000);
            assertEquals("unexpected call count", 0, entity.getCallCount());
         }
      }
      finally
      {
         entity.remove();
      }
   }

   /**
    * Create the timer and rollback the transaction, the timer create should be rolled back as well
    */
   public void testRollbackAfterCreate() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerEntityHome entityHome = (TimerEntityHome) iniCtx.lookup(TimerEntityHome.JNDI_NAME);
      TimerEntity entity = entityHome.create(new Integer(1));

      TimerFacadeHome facadeHome = (TimerFacadeHome) iniCtx.lookup(TimerFacadeHome.JNDI_NAME);
      TimerFacade facade = facadeHome.create();
      try
      {
         facade.rollbackAfterCreateEntity(500);
         assertTrue("Timer list should be empty", entity.getTimers().size() == 0);
         sleep(1000);
         assertEquals("unexpected call count", 0, entity.getCallCount());
      }
      finally
      {
         entity.remove();
      }
   }

   /**
    * Cancel the timer and rollback the transaction, the timer cancel should be rolled back as well
    */
   public void testRollbackAfterCancel() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerEntityHome entityHome = (TimerEntityHome) iniCtx.lookup(TimerEntityHome.JNDI_NAME);
      TimerEntity entity = entityHome.create(new Integer(1));

      TimerFacadeHome facadeHome = (TimerFacadeHome) iniCtx.lookup(TimerFacadeHome.JNDI_NAME);
      TimerFacade facade = facadeHome.create();
      try
      {
         entity.createTimer(500, 0, null);
         facade.rollbackAfterCancelEntity();
         sleep(1000);
         assertEquals("unexpected call count", 1, entity.getCallCount());
      }
      finally
      {
         entity.remove();
      }
   }

   /**
    * Throw a RuntimeException in ejbTimeout, the timer should retry the invocation at least once
    */
   public void testRetryAfterRollback() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerEntityHome home = (TimerEntityHome) iniCtx.lookup(TimerEntityHome.JNDI_NAME);
      TimerEntity entity = home.create(new Integer(1));

      try
      {
         Properties props = new Properties();
         props.setProperty("rollback", "true");

         entity.createTimer(500, 0, props);
         sleep(1000);

         // The timer is expected to retry the invocation to ejbTimeout at least once
         // Note, due to bean sepuku the instance for retry should be another one as for the first attempt
         assertEquals("unexpected call count", 1, entity.getCallCount());
      }
      finally
      {
         entity.remove();
      }
   }
}
