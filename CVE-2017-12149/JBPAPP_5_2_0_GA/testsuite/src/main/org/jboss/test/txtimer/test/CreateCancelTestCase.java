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

import java.util.List;

import javax.management.MBeanServerInvocationHandler;
import javax.naming.InitialContext;

import junit.framework.TestSuite;

import org.jboss.ejb.txtimer.DatabasePersistencePolicyMBean;
import org.jboss.test.JBossTestCase;
import org.jboss.test.txtimer.interfaces.TimerTest;
import org.jboss.test.txtimer.interfaces.TimerTestHome;

/**
 * Test the Tx timer creation/cancelation for every tx setting
 * 
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 81036 $
 */
public class CreateCancelTestCase extends JBossTestCase
{
   DatabasePersistencePolicyMBean pp;

   public CreateCancelTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      pp = (DatabasePersistencePolicyMBean) MBeanServerInvocationHandler.newProxyInstance(
            getServer(), DatabasePersistencePolicyMBean.OBJECT_NAME, DatabasePersistencePolicyMBean.class, false);
   }
   
   protected int getTimerCount()
   {
      List timerHandles = pp.listTimerHandles();
      return timerHandles.size();
   }
   
   public static TestSuite suite() throws Exception
   {
      TestSuite ts = new TestSuite();
      ts.addTest(getDeploySetup(CreateCancelTestCase.class, "ejb-txtimer.jar"));
      return ts;
   }
   
   public void testCreateRequiredCancelRequired() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxRequired();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxRequired();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }

   public void testCreateRequiredCancelRequiresNew() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxRequired();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxRequiresNew();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }
   
   public void testCreateRequiredCancelNotSupported() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxRequired();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxNotSupported();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }
   
   public void testCreateRequiredCancelNever() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxRequired();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxNever();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }

   public void testCreateRequiresNewCancelRequired() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxRequiresNew();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxRequired();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }

   public void testCreateRequiresNewCancelRequiresNew() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxRequiresNew();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxRequiresNew();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }
   
   public void testCreateRequiresNewCancelNotSupported() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxRequiresNew();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxNotSupported();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }
   
   public void testCreateRequiresNewCancelNever() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxRequiresNew();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxNever();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }
   
   public void testCreateNotSupportedCancelRequired() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxNotSupported();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxRequired();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }

   public void testCreateNotSupportedCancelRequiresNew() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxNotSupported();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxRequiresNew();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }
   
   public void testCreateNotSupportedCancelNotSupported() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxNotSupported();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxNotSupported();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }
   
   public void testCreateNotSupportedCancelNever() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxNotSupported();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxNever();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }
   
   public void testCreateNeverCancelRequired() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxNever();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxRequired();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }

   public void testCreateNeverCancelRequiresNew() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxNever();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxRequiresNew();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }
   
   public void testCreateNeverCancelNotSupported() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxNever();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxNotSupported();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }
   
   public void testCreateNeverCancelNever() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      TimerTestHome home = (TimerTestHome) iniCtx.lookup(TimerTestHome.JNDI_NAME);
      TimerTest session = home.create();
      
      try
      {
         int initialTimerCount;
         int createdTimerCount;
         int canceledTimerCount;
         
         initialTimerCount = getTimerCount();
         session.startTimerInTxNever();
         createdTimerCount = getTimerCount();
         assertEquals("Timer not created", initialTimerCount + 1, createdTimerCount);
         session.cancelTimerInTxNever();
         canceledTimerCount = getTimerCount();
         assertEquals("Timer not canceled", createdTimerCount, canceledTimerCount + 1);
      }
      finally
      {
         session.remove();
      }
   }
   
}
