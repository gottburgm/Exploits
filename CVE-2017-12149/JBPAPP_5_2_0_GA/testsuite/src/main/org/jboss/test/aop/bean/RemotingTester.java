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

import org.jboss.aop.Dispatcher;
import org.jboss.aspects.remoting.ClusteredRemoting;
import org.jboss.aspects.remoting.NotRegisteredException;
import org.jboss.aspects.remoting.Remoting;
import org.jboss.ha.framework.interfaces.RoundRobin;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;
import org.jboss.system.ServiceMBeanSupport;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81036 $
 * @see Monitorable
 */
public class RemotingTester
extends ServiceMBeanSupport
implements RemotingTesterMBean, MBeanRegistration
{
   // Constants ----------------------------------------------------
   // Attributes ---------------------------------------------------
   static Logger log = Logger.getLogger(RemotingTester.class);
   MBeanServer m_mbeanServer;

   // Static -------------------------------------------------------

   // Constructors -------------------------------------------------
   public RemotingTester()
   {
   }

   // Public -------------------------------------------------------

   // MBeanRegistration implementation -----------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
   throws Exception
   {
      m_mbeanServer = server;
      return name;
   }

   public void postRegister(Boolean registrationDone)
   {
   }

   public void preDeregister() throws Exception
   {
   }

   public void postDeregister()
   {
   }

   protected void startService()
   throws Exception
   {
   }

   protected void stopService()
   {
   }

   public POJO testRemoting()
   {
      try
      {
         log.info("Testing REMOTING");
         POJO remote = new POJO("hello");
         Dispatcher.singleton.registerTarget("myobj", remote);

         return (POJO) Remoting.createRemoteProxy("myobj", remote.getClass(), "socket://localhost:5150");
      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex.getMessage());
      }
   }

   public NonadvisedPOJO testNonadvisedRemoting()
   {
      try
      {
         log.info("Testing NONADVISED REMOTING");
         NonadvisedPOJO remote = new NonadvisedPOJO("hello");
         Dispatcher.singleton.registerTarget("myobj", remote);

         return (NonadvisedPOJO) Remoting.createRemoteProxy("myobj", remote.getClass(), "socket://localhost:5150");
      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex.getMessage());
      }
   }

   public POJO testClusteredRemoting()
   {
      try
      {
         log.info("Testing CLUSTERED REMOTING");
         POJO remote = new POJO("hello");
         return (POJO) ClusteredRemoting.clusterObject("clusteredobj", remote,
         "DefaultPartition", new RoundRobin(),
         new InvokerLocator("socket://localhost:5150"));
      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex.getMessage());
      }
   }

   public NonadvisedPOJO testClusteredNonadvisedRemoting()
   {
      try
      {
         log.info("Testing CLUSTERED NONADVISED REMOTING");
         NonadvisedPOJO remote = new NonadvisedPOJO("hello");
         return (NonadvisedPOJO) ClusteredRemoting.clusterObject("nonadvisedclusteredobj", remote,
         "DefaultPartition", new RoundRobin(),
         new InvokerLocator("socket://localhost:5150"));
      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex.getMessage());
      }
   }


   public void unregisterNonClusteredObject(String oid)
   {
      Object obj = Dispatcher.singleton.getRegistered(oid);
      if (obj == null)
      {
         throw new NotRegisteredException(oid.toString() + " is not registered");         
      }
      Dispatcher.singleton.unregisterTarget(oid);
   }
   
   public void unregisterTarget(Object object)
   {
      ClusteredRemoting.unregisterClusteredObject(object);
   }

   // Inner classes -------------------------------------------------
}

