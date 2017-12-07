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
package org.jboss.test.cluster.invokerha;

import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.transaction.Transaction;

import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.logging.Logger;
import org.jboss.test.cluster.invokerha.InvokerHaInfrastructure.InvokerHaFactory;
import org.jboss.test.cluster.invokerha.InvokerHaInfrastructure.TraceFirstAvailable;
import org.jboss.test.cluster.invokerha.InvokerHaInfrastructure.TraceFirstAvailableIdenticalAllProxies;
import org.jboss.test.cluster.invokerha.InvokerHaInfrastructure.TraceRandomRobin;
import org.jboss.test.cluster.invokerha.InvokerHaInfrastructure.TraceRoundRobin;
import org.jboss.test.cluster.invokerha.InvokerHaTransactionalMockUtils.MockTransaction;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Base class for invoker related tests that do not run within AS.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public abstract class AbstractInvokerHa extends TestCase
{
   private static final Logger log = Logger.getLogger(AbstractInvokerHa.class);
   
   protected InvokerHaInfrastructure infrastructure;
   
   InvokerHaTransactionalMockUtils transactionalMockUtils;
   
   InvokerHaFactory invokerHaFactory;
   
   Invoker timeTellerProxy;
   
   Invoker systemTimeProxy;
   
   Invoker clientUserTransactionProxy;
   
   Object prevChosenTargetDateTimeTeller;
   
   Object prevChosenTargetSystemTimeTeller;

   protected void setUp(int serverCount, InvokerHaFactory factory) throws Exception
   {
      super.setUp();
      
      log.info(getName());
      
      invokerHaFactory = factory; 
      
      transactionalMockUtils = new InvokerHaTransactionalMockUtils();
      
      infrastructure = invokerHaFactory.getInvokerHaInfrastructure(2);
      
      infrastructure.registerManagedObjects();
      
      infrastructure.deployServers();
      
      infrastructure.createDateTimeTeller();
      infrastructure.createSystemTimeTeller();
      infrastructure.createClientUserTransactionService();

      infrastructure.deployDateTimeTeller();
      infrastructure.deploySystemTimeTeller();
      infrastructure.deployClientUserTransactionService();
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      infrastructure.unregisterManagedObjects();
      
      infrastructure.undeployDateTimeTeller();
      infrastructure.undeploySystemTimeTeller();
      infrastructure.undeployClientUserTransactionService();
      
      infrastructure.undeployServers();
   }
   
   public void testTransactionalSuccessfulCallsRoundRobin() throws Exception
   {
      /* same proxies used in simulated transactions */
      transactionalSuccessfulCalls(TraceRoundRobin.class, false);
      /* different proxies in simulated transactions */
      transactionalSuccessfulCalls(TraceRoundRobin.class, true);
   }
   
   public void testTransactionalSuccessfulCallsFirstAvailable() throws Exception
   {
      /* same proxies used in simulated transactions */
      transactionalSuccessfulCalls(TraceFirstAvailable.class, false);
      /* same proxies used in simulated transactions */
      transactionalSuccessfulCalls(TraceFirstAvailable.class, true);            
   }
   
   public void testTransactionalSuccessfulCallsFirstAvailableIndenticalAllProxies() throws Exception
   {
      /* same proxies used in simulated transactions */
      transactionalSuccessfulCalls(TraceFirstAvailableIdenticalAllProxies.class, false);
      /* same proxies used in simulated transactions */
      transactionalSuccessfulCalls(TraceFirstAvailableIdenticalAllProxies.class, true);                  
   }
   
   public void testTransactionalSuccessfulCallsRandomRobin() throws Exception
   {
      /* same proxies used in simulated transactions */
      transactionalSuccessfulCalls(TraceRandomRobin.class, false);
      /* same proxies used in simulated transactions */
      transactionalSuccessfulCalls(TraceRandomRobin.class, true);                  
   }
   
   protected void transactionalSuccessfulCalls(Class<? extends LoadBalancePolicy> policyClass, boolean newProxiesInBetweenTransactions)
   {
      log.debug("transactional successfull calls [policy=" + policyClass + ",newProxiesInBetweenTransactions=" + newProxiesInBetweenTransactions + "]");
      
      try
      {
         UID uid;
         
         createNewProxies(0, policyClass, true);

         /* Simulate client user transaction */
         uid = new UID();         
         transactionalMockUtils.getTpcf().setUid(uid);
         performCalls(3, null, policyClass);
         
         if (newProxiesInBetweenTransactions)
         {
            createNewProxies(0, policyClass, false);
         }
         
         /* Simulate transaction interceptor */
         uid = new UID();
         Transaction tx = new MockTransaction();
         transactionalMockUtils.getTpcf().setUid(uid);
         transactionalMockUtils.getTpci().setTransaction(tx);
         performCalls(3, tx, policyClass);                  
      }
      catch(Exception e)
      {
         /* catching to log the error properly (JUnit in eclipse does not show 
          * correctly exceptions from invokers) and fail */
         log.error("error", e);
         fail();
      }
   }

   protected void performCalls(int numberPairCalls, 
         Transaction tx, Class<? extends LoadBalancePolicy> policyClass) throws Exception
   {
      Invocation inv;
      
      for (int i = 0; i < numberPairCalls; i++)
      {
         /* create invocation to date time teller */
         inv = infrastructure.createDateTimeTellerInvocation(tx, null, timeTellerProxy);
         /* invoke on proxy passing the invocation */
         log.debug(timeTellerProxy.invoke(inv));
         /* assert post conditions after invocation */
         prevChosenTargetDateTimeTeller = assertSuccessfulPostConditions(inv, prevChosenTargetDateTimeTeller, tx, policyClass);
         
         /* create invocation to system time teller */
         inv = infrastructure.createSystemTimeTellerInvocation(tx, null, systemTimeProxy);
         /* invoke on proxy passing the invocation */
         log.debug(systemTimeProxy.invoke(inv));
         /* assert post conditions after invocation */
         prevChosenTargetSystemTimeTeller = assertSuccessfulPostConditions(inv, prevChosenTargetSystemTimeTeller, tx, policyClass);
      }
   }
   
   protected void performConcurrentCalls(int numberThreads, 
         int numberCallsPerThread, Class<? extends LoadBalancePolicy> policyClass) throws Exception
   {
      ExecutorService executor = Executors.newCachedThreadPool();
      CyclicBarrier barrier = new CyclicBarrier(numberThreads + 1);
      List<Future<Void>> futures = new ArrayList<Future<Void>>();
      
      log.debug("create proxies");
      createNewProxies(0, policyClass, true);
      for (int i = 0; i < numberThreads; i++)
      {
         log.debug("schedule execution");
         Future<Void> future = executor.submit(new InvokerProxyBatch(barrier, numberCallsPerThread, policyClass));
         futures.add(future);
      }
      barrier.await(); // wait for all threads to be ready
      barrier.await(); // wait for all threads to finish
      
      log.debug("all threads finished, let's shutdown the executor and check whether any exceptions were reported");
      
      for (Future<Void> future : futures)
      {
         future.get();
      }
      
      executor.shutdown();
      
      log.debug("no exceptions reported, good :)");
   }
     
   protected Object assertSuccessfulPostConditions(Invocation inv, Object prevChosenTarget, Transaction tx, Class<? extends LoadBalancePolicy> policyClass)
   {
      assertEquals(0, inv.getAsIsValue("FAILOVER_COUNTER"));
      Object chosenTarget = inv.getTransientValue(invokerHaFactory.getChosenTargetKey());
      assertNotNull(chosenTarget);
      /* if tx was null, invocation's tx should be null after invocation. */
      assertEquals(tx, inv.getTransaction());
      if (transactionalMockUtils.getTpcf().getUid() != null)
      {
         /* check tx failover authorisations */
         assertTrue("transaction should have reached the server", invokerHaFactory.getTxFailoverAuthorizationsMap().containsKey(transactionalMockUtils.getTpcf().getUid()));         
      }
      /* check chosen target with previously chosen target, if there's any */
      return assertChosenTarget(policyClass, chosenTarget, prevChosenTarget);
   }
   
   protected void createNewProxies(int serverIndex, Class<? extends LoadBalancePolicy> policyClass, boolean isVery1st) throws Exception
   {
      /* Create a proxy instances retrieved from the first server */
      timeTellerProxy = infrastructure.createDateTimeTellerProxy(serverIndex, policyClass);
      systemTimeProxy = infrastructure.createSystemTimeTellerProxy(serverIndex, policyClass);
      clientUserTransactionProxy = infrastructure.createClientUserTransactionProxy(serverIndex, policyClass);
      
      /* Initialise previous chosen targets. If not new proxies elected 
       * between transactions, this allows to carry on checking chosen 
       * targets in between transactions. */ 
      if (!isVery1st && policyClass.equals(TraceFirstAvailableIdenticalAllProxies.class))
      {
         /* In the particular case of first availble indentical proxies, if we're 
         * not creating the proxies for the first time, do not initialise the 
         * proxies because we need them to check them with next chosen ones. */
      }
      else
      {
         prevChosenTargetDateTimeTeller = null;
         prevChosenTargetSystemTimeTeller = null;                  
      }
   }
   
   protected Object assertChosenTarget(Class<? extends LoadBalancePolicy> policyClass, Object chosenTarget, Object prevChosenTarget)
   {
      if (policyClass.equals(TraceRoundRobin.class))
      {
         prevChosenTarget = checkRoundRobin(chosenTarget, prevChosenTarget);
      }
      else if (policyClass.equals(TraceFirstAvailable.class))
      {
         prevChosenTarget = checkFirstAvailable(chosenTarget, prevChosenTarget);
      }
      else if (policyClass.equals(TraceFirstAvailableIdenticalAllProxies.class))
      {
         prevChosenTarget = checkFirstAvailableIndenticalAllProxies(chosenTarget, prevChosenTarget);
      }
      
      return prevChosenTarget;
   }
   
   protected Object checkRoundRobin(Object chosenTarget, Object prevChosenTarget)
   {
      if (prevChosenTarget != null)
      {
         /* In round robin, previous chosen target must be different to the 
          * current one, unless there's only one node in the cluster, but we're 
          * not testing that here. */
         assertNotSame(prevChosenTarget, chosenTarget);
      }      
      
      return chosenTarget;
   }
   
   protected Object checkFirstAvailable(Object chosenTarget, Object prevChosenTarget)
   {
      if (prevChosenTarget != null)
      {
         /* In first available robin, previous chosen target must be the same to the 
          * current one, unless there's only one node in the cluster, but we're 
          * not testing that here. */
         assertEquals(prevChosenTarget, chosenTarget);
      }
      
      return chosenTarget;      
   }
   
   protected Object checkFirstAvailableIndenticalAllProxies(Object chosenTarget, Object prevChosenTarget)
   {
      return checkFirstAvailable(chosenTarget, prevChosenTarget);
   }   

   /** Classes **/
   
   public class InvokerProxyBatch implements Callable<Void>
   {
      private final CyclicBarrier barrier;
      
      private final int numberCallsPerThread;
      
      private final Class<? extends LoadBalancePolicy> policyClass;
      
      public InvokerProxyBatch(CyclicBarrier ciclycBarrier, int numberCalls, Class<? extends LoadBalancePolicy> policy)
      {
         barrier = ciclycBarrier;
         numberCallsPerThread = numberCalls;
         policyClass = policy;
      }

      public Void call() throws Exception
      {
         try
         {
            log.debug("wait for all executions paths to be ready to perform calls");
            barrier.await();
            
            log.debug("perform invoker proxy calls");
            performThreadSafeCalls(numberCallsPerThread, policyClass);
         }
         catch(AssertionFailedError afe)
         {
            logAndThrow("Assertion failed in thread: " + Thread.currentThread().getName(), afe);
         }
         finally
         {
            log.debug("wait for all execution paths to finish");
            barrier.await();
         }
         
         return null;
      }
      
      protected void performThreadSafeCalls(int numberPairCalls, Class<? extends LoadBalancePolicy> policyClass) throws Exception
      {
         Invocation inv;
         
         for (int i = 0; i < numberPairCalls; i++)
         {
            /* create invocation to date time teller */
            inv = infrastructure.createDateTimeTellerInvocation(null, null, timeTellerProxy);
            /* invoke on proxy passing the invocation */
            log.debug(timeTellerProxy.invoke(inv));
            /* assert post conditions after invocation */
            assertThreadSafePostConditions(inv, timeTellerProxy);
            
            /* create invocation to system time teller */
            inv = infrastructure.createSystemTimeTellerInvocation(null, null, systemTimeProxy);
            /* invoke on proxy passing the invocation */
            log.debug(systemTimeProxy.invoke(inv));
            /* assert post conditions after invocation */
            assertThreadSafePostConditions(inv, systemTimeProxy);
         }
      }

      protected void assertThreadSafePostConditions(Invocation inv, Invoker invoker)
      {
         assertEquals(0, inv.getAsIsValue("FAILOVER_COUNTER"));
         Object chosenTarget = inv.getTransientValue(invokerHaFactory.getChosenTargetKey());
         assertNotNull(chosenTarget);
         
         /* NOTE: currently, the following assertion is only valid for unified 
          * invokers, do not call from thread safety tests related to other 
          * invokers */
         
         /* assert that target chosen by load balance policy and target to which 
          * invocation was directed is the same */      
         assertEquals(chosenTarget, inv.getTransientValue("TEST_USED_TARGET"));
      } 
      
      private void logAndThrow(String message, Throwable t) throws Exception
      {
         log.error(message, t);
         throw new Exception(message, t);
      }
   }
}
