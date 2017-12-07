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
package org.jboss.test.jca.inflow;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.mx.util.ObjectNameFactory;

/**
 * A TestResourceAdapter.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class TestResourceAdapter implements ResourceAdapter, TestResourceAdapterMBean
{
   public static final ObjectName mbean = ObjectNameFactory.create("jboss.test:test=TestResourceAdapter");
   
   BootstrapContext ctx;

   ConcurrentHashMap endpoints = new ConcurrentHashMap();
   
   public TestResourceAdapterInflowResults testInflow() throws Exception
   {
      TestResourceAdapterInflow test = new TestResourceAdapterInflow(this);
      return test.run();
   }
   
   public TestResourceAdapterWorkManagerResults testWorkManager() throws Exception
   {
      TestResourceAdapterWorkManager test = new TestResourceAdapterWorkManager(this);
      return test.run();
   }
   
   public TestResourceAdapterTimerResults testTimer() throws Exception
   {
      TestResourceAdapterTimer test = new TestResourceAdapterTimer(this);
      return test.run();
   }
   
   public TestResourceAdapterTxInflowResults testTxInflow() throws Exception
   {
      TestResourceAdapterTxInflow test = new TestResourceAdapterTxInflow(this);
      return test.run();
   }
   
   public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException
   {
      MessageEndpoint endpoint = endpointFactory.createEndpoint(null);
      endpoints.put(spec, endpoint);
   }

   public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec)
   {
      MessageEndpoint endpoint = (MessageEndpoint) endpoints.remove(spec);
      if (endpoint != null)
         endpoint.release();
   }
   
   public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException
   {
      // TODO getXAResources
      return null;
   }
   
   public void start(BootstrapContext ctx) throws ResourceAdapterInternalException
   {
      this.ctx = ctx;

      try
      {
         MBeanServer server = MBeanServerLocator.locateJBoss();
         server.registerMBean(this, mbean);
      }
      catch (Exception e)
      {
         throw new ResourceAdapterInternalException(e);
      }
   }

   public void stop()
   {
      try
      {
         MBeanServer server = MBeanServerLocator.locateJBoss();
         server.unregisterMBean(mbean);
         
         for (Iterator i = endpoints.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry) i.next();
            MessageEndpoint endpoint = (MessageEndpoint) entry.getValue();
            if (endpoint != null)
            {
               endpoint.release();
               i.remove();
            }
         }
      }
      catch (Exception ignored)
      {
      }
   }
   
   MessageEndpoint getEndpoint(String name) throws Exception
   {
      for (Iterator i = endpoints.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry) i.next();
         TestActivationSpec spec = (TestActivationSpec) entry.getKey();
         if (name.equals(spec.getName()))
            return (MessageEndpoint) entry.getValue();
      }
      throw new Exception("MessageEndpoint not found for name: " + name);      
   }
}
