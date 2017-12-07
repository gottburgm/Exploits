/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cluster.defaultcfg.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import org.jboss.ha.framework.server.HATarget;
import org.jboss.invocation.InvokerHA;
import org.jboss.invocation.unified.interfaces.UnifiedInvokerHAProxy;
import org.jboss.remoting.InvokerLocator;
import org.jboss.test.cluster.invokerha.AbstractInvokerHa;
import org.jboss.test.cluster.invokerha.InvokerHaInfrastructure;
import org.jboss.test.cluster.invokerha.UnifiedInvokerHaMockUtils;
import org.jboss.test.cluster.invokerha.InvokerHaInfrastructure.InvokerHaFactory;
import org.jboss.test.cluster.invokerha.InvokerHaInfrastructure.TraceRoundRobin;
import org.jboss.test.cluster.invokerha.UnifiedInvokerHaMockUtils.MockInvokerLocator;
import org.jboss.test.cluster.invokerha.UnifiedInvokerHaMockUtils.MockUnifiedInvokerHA;

/**
 * Unit test case for unified invoker ha proxy and invoker at the other side. 
 * Remoting and transactional layer have been mocked as the intention of these 
 * tests is not to test the remoting code nor checking the behaivour of a real
 * transaction manager.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class UnifiedInvokerHaUnitTestCase extends AbstractInvokerHa
{
   @Override
   protected void setUp() throws Exception
   {
      setUp(2, new UnifiedInvokerHaFactory());
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
   }
   
   public void testProxyThreadSafety() throws Throwable
   {
      performConcurrentCalls(50, 50, TraceRoundRobin.class);
   }
   
   public void testProxyThreadSafetyNullPointerException() throws Throwable
   {
      UnifiedInvokerHaMockUtils.SLOW_DOWN_CLIENT_CONNECT = true;
      performConcurrentCalls(5, 5, TraceRoundRobin.class);
      UnifiedInvokerHaMockUtils.SLOW_DOWN_CLIENT_CONNECT = false;
   }   
   
   /** Classes **/
   
   /**
    * Unified invoker ha factory. 
    */
   public static class UnifiedInvokerHaFactory implements InvokerHaFactory
   {
      public String getInvokerTypeName()
      {
         return "UnifiedInvokerHa";
      }
      
      public InvokerHaInfrastructure getInvokerHaInfrastructure(int serverCount)
      {
         return new UnifiedInvokerHaInfrastructure(serverCount, this);
      }
      
      public Map getTxFailoverAuthorizationsMap()
      {
         return UnifiedInvokerHAProxy.txFailoverAuthorizations;
      }
      
      public InvokerHA createInvokerHaServer(String serverName, int serverNumber)
      {
         MockUnifiedInvokerHA invoker = new MockUnifiedInvokerHA(getInvokerTypeName() + "-" + serverName + "-" + serverNumber);
         MockInvokerLocator locator = new MockInvokerLocator("127.0.0.1", serverNumber, invoker);
         invoker.setLocator(locator);
         
         return invoker;          
      }
      
      public String getChosenTargetKey()
      {
         return "TEST_CHOSEN_TARGET";
      }
   }
   
   /**
    * Unified invoker specific infrastructure class.
    */
   public static class UnifiedInvokerHaInfrastructure extends InvokerHaInfrastructure
   {
      private List<InvokerLocator> locators; 
      
      public UnifiedInvokerHaInfrastructure(int serverCount, InvokerHaFactory invokerHaFactory)
      {
         super(serverCount, invokerHaFactory);
      }

      @Override
      protected void deploy(List<? extends InvokerHA> replicants, ObjectName targetName, String replicantName)
            throws Exception
      {
         /* convert replicant list into an InvokerLocator list */
         locators = new ArrayList<InvokerLocator>(replicants.size());
         for (InvokerHA replicant : replicants)
         {
            locators.add(((MockUnifiedInvokerHA)replicant).getLocator());
         }
         
         for(InvokerHA replicant : replicants)
         {
            /* create ha-target in first server with the list of replicants and register 
             * it with the MBeanServer. */
            HATarget target = new MockHATarget(null, replicantName, null, 2);
            target.replicantsChanged(null, locators, 0, false);
            replicant.registerBean(targetName, target);         
         }
      }
      
      public List<InvokerLocator> getLocators()
      {
         return locators;
      }
   }   
}
