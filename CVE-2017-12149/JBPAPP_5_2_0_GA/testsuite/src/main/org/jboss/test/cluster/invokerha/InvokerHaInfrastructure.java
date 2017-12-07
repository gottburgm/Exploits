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

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.transaction.Transaction;

import org.jboss.ha.framework.interfaces.FamilyClusterInfo;
import org.jboss.ha.framework.interfaces.FirstAvailable;
import org.jboss.ha.framework.interfaces.FirstAvailableIdenticalAllProxies;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.HARMIClient;
import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.RandomRobin;
import org.jboss.ha.framework.interfaces.RoundRobin;
import org.jboss.ha.framework.server.HATarget;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerHA;
import org.jboss.logging.Logger;
import org.jboss.system.Registry;

/**
 * Infrastructure class that encapsulates the simulated deployment of 
 * servers or invoker endpoints and mbeans that receive the invocations.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class InvokerHaInfrastructure
{
   private static final Logger log = Logger.getLogger(InvokerHaInfrastructure.class);
   
   /**
    * MBeanServer instance. This is not gonna run in Java 1.4.x, so we can 
    * safely assume that we can use Java's MBeanServer.
    */
   private static final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
   
   private InvokerHaFactory invokerHaFactory;
   
   private final ObjectName dateTimeTellerON;
   
   private final ObjectName systemTimeTellerON;
   
   private final ObjectName clientUserTransactionServiceON;
   
   private final Integer dateTimeTellerONHashCode;
   
   private final Integer systemTimeTellerONHashCode;
   
   private final Integer clientUserTransactionServiceONHashCode;
   
   private final int serverCount;
   
   private List<? extends InvokerHA> replicants;
   
   private List<ObjectName> invokerONs;
   
   /**
    * Create a new InvokerHaInfrastructure.
    * 
    * @param serverCount number of invoker endpoints to create
    * @param invokerHaFactory invoker ha factory class implementation 
    */
   public InvokerHaInfrastructure(int serverCount, InvokerHaFactory invokerHaFactory)
   {
      this.serverCount = serverCount;
      this.invokerHaFactory = invokerHaFactory;

      try 
      {
         /* initialise ObjectNames and hashcodes */
         dateTimeTellerON = new ObjectName("com.acme.mbeans:type=DateTimeTeller");
         systemTimeTellerON = new ObjectName("com.acme.mbeans:type=SystemTimeTeller");
         clientUserTransactionServiceON = new ObjectName("com.acme.mbeans:type=ClientUserTransactionService");
         dateTimeTellerONHashCode = new Integer(dateTimeTellerON.hashCode());
         systemTimeTellerONHashCode = new Integer(systemTimeTellerON.hashCode());
         clientUserTransactionServiceONHashCode = new Integer(clientUserTransactionServiceON.hashCode());
   
         /* create a list of object names for the invoker endpoints */
         invokerONs = new ArrayList<ObjectName>(this.serverCount);
         for (int i = 0; i < serverCount; i++)
         {
            invokerONs.add(new ObjectName("com.acme.invokers:type=" + this.invokerHaFactory.getInvokerTypeName() + "-Server" + (i + 1)));
         }
      }
      catch (MalformedObjectNameException mone)
      {
         throw new IllegalArgumentException("invalid object name", mone);
      }      
   }

   /**
    * Binds ObjectName hash code with the ObjectName which is used by the 
    * invoker. We just do it once cos both servers are in the same VM.
    */
   public void registerManagedObjects()
   {
      Registry.bind(dateTimeTellerONHashCode, dateTimeTellerON);
      Registry.bind(systemTimeTellerONHashCode, systemTimeTellerON);
      Registry.bind(clientUserTransactionServiceONHashCode, clientUserTransactionServiceON);      
   }

   /** 
    * Create a list of invoker ha instances, called the replicants, and 
    * register the list with the mbean server.
    */
   public void deployServers() throws Exception
   {
      List<InvokerHA> replicantServers = new ArrayList<InvokerHA>(serverCount);
      
      /* create n invoker instances that emulate n AS servers */
      for (int i =0; i < serverCount; i++)
      {
         InvokerHA server = invokerHaFactory.createInvokerHaServer("Server", i + 1);
         /* add invoker as replicant */
         replicantServers.add(server);
         
         mbs.registerMBean(server, invokerONs.get(i));
      }

      replicants = replicantServers;
   }

   /** 
    * Create new instance of DateTimeTeller and register it with the 
    * MBeanServer. Note that a single instance is created and both server 
    * invokers point to this instance behind the scenes. This simplifies 
    * testing.
    */   
   public DateTimeTeller createDateTimeTeller() throws Exception
   {
      DateTimeTeller dateTimeTellerMBean = new DateTimeTeller();
      mbs.registerMBean(dateTimeTellerMBean, dateTimeTellerON);
      return dateTimeTellerMBean;
   }

   /**
    * Create new instance of SystemTimeTeller and register it with the 
    * MBeanServer. Note that a single instance is created and both server 
    * invokers point to this instance behind the scenes. This simplifies 
    * testing.
    */
   public SystemTimeTeller createSystemTimeTeller() throws Exception
   {
      SystemTimeTeller systemTimeTellerMBean = new SystemTimeTeller();
      mbs.registerMBean(systemTimeTellerMBean, systemTimeTellerON);
      return systemTimeTellerMBean;
   }
   
   public ClientUserTransactionService createClientUserTransactionService() throws Exception
   {
      ClientUserTransactionService clientUserTransactionServiceMBean = new ClientUserTransactionService();
      mbs.registerMBean(clientUserTransactionServiceMBean, clientUserTransactionServiceON);
      return clientUserTransactionServiceMBean;
   }   
   
   /**
    * Deploy date time teller mbean in each server, or invoker endpoint, 
    * creating a mock HATarget with the list of replicants and associating the 
    * mbean's object name with the HATarget instance.   
    */
   public void deployDateTimeTeller() throws Exception
   {
      deploy(replicants, dateTimeTellerON, "DateTimeTellerReplicant");
   }

   /**
    * Deploy system time teller mbean in each server, or invoker endpoint, 
    * creating a mock HATarget with the list of replicants and associating the 
    * mbean's object name with the HATarget instance.   
    */
   public void deploySystemTimeTeller() throws Exception
   {
      deploy(replicants, systemTimeTellerON, "SystemTimeTellerReplicant");
   }
   
   public void deployClientUserTransactionService() throws Exception
   {
      deploy(replicants, clientUserTransactionServiceON, "ClientUserTransactionServiceReplicant");
   }   
   
   /**
    * Create a proxy to date time teller bean.
    * 
    * @param serverIndex invoker endpoint from which to return the proxy
    * @param policyClass load balance policy to use in the proxy
    * @return 
    * @throws Exception
    */
   public Invoker createDateTimeTellerProxy(int serverIndex, Class<? extends LoadBalancePolicy> policyClass) throws Exception
   {
      InvokerHA server = replicants.get(serverIndex);
      log.debug("replicants: " + replicants);
      return server.createProxy(dateTimeTellerON, policyClass.newInstance(), "UnitTestPartition/DateTimeTellerMBean");
   }
   
   /**
    * Create a proxy to system time teller bean.
    * 
    * @param serverIndex invoker endpoint from which to return the proxy
    * @param policyClass load balance policy to use in the proxy
    * @return 
    * @throws Exception
    */
   public Invoker createSystemTimeTellerProxy(int serverIndex, Class<? extends LoadBalancePolicy> policyClass) throws Exception
   {
      InvokerHA server = replicants.get(serverIndex);
      return server.createProxy(systemTimeTellerON, policyClass.newInstance(), "UnitTestPartition/SystemTimeTellerMBean");
   }

   public Invoker createClientUserTransactionProxy(int serverIndex, Class<? extends LoadBalancePolicy> policyClass) throws Exception
   {
      InvokerHA server = replicants.get(serverIndex);
      return server.createProxy(clientUserTransactionServiceON, policyClass.newInstance(), "UnitTestPartition/ClientUserTransactionServiceMBean");
   }   
   
   /**
    * Create a new invocation for date time teller mbean 
    * 
    * @param tx instance of Transaction. If tx is null, transaction is not 
    * added to invocation, which is useful to replicate transactions starting 
    * in non managed environments.
    * @param failureType type of failure to inject. If null, no failure is 
    * injected.
    * @return
    */
   public Invocation createDateTimeTellerInvocation(Transaction tx, InvokerHaFailureType failureType, Invoker invoker)
   {
      return createInvocation(tx, dateTimeTellerONHashCode, failureType, invoker);
   }
   
   /**
    * Create a new invocation for system time teller mbean 
    * 
    * @param tx instance of Transaction. If tx is null, transaction is not 
    * added to invocation, which is useful to replicate transactions starting 
    * in non managed environments.
    * @return
    */
   public Invocation createSystemTimeTellerInvocation(Transaction tx, InvokerHaFailureType failureType, Invoker invoker)
   {
      return createInvocation(tx, systemTimeTellerONHashCode, failureType, invoker);
   }  
   
   public Invocation createClientUserTransactionInvocation(Transaction tx, InvokerHaFailureType failureType, Invoker invoker)
   {
      return createInvocation(tx, clientUserTransactionServiceONHashCode, failureType, invoker);
   } 
   
   /**
    * Unbind mbean object name hashcodes from JMX registry.
    */
   public void unregisterManagedObjects()
   {
      /* Unregister from the JMX registry */
      Registry.unbind(dateTimeTellerONHashCode);
      Registry.unbind(systemTimeTellerONHashCode);      
      Registry.unbind(clientUserTransactionServiceONHashCode);
   }
   
   /**
    * Unregister date time teller mbean object name from each invoker endpoint 
    * and from the mbean server. 
    */
   public void undeployDateTimeTeller() throws Exception
   {
      undeploy(replicants, dateTimeTellerON);
   }

   /**
    * Unregister system time teller mbean object name from each invoker endpoint 
    * and from the mbean server. 
    */
   public void undeploySystemTimeTeller() throws Exception
   {
      undeploy(replicants, systemTimeTellerON);
   }
   

   public void undeployClientUserTransactionService() throws Exception
   {
      undeploy(replicants, clientUserTransactionServiceON);
   }
   
   /**
    * Unregister invoker endpoints from mbean server.
    */
   public void undeployServers() throws Exception
   {
      for (int i = 0; i < serverCount; i++)
      {
         mbs.unregisterMBean(invokerONs.get(i));
      }
   }
   
   public List<? extends InvokerHA> getReplicants()
   {
      return replicants;
   }
   
   /**
    * Simulate the deployment of an mbean in a list of invoker endpoints.
    * 
    * @param replicants list of invoker endpoints
    * @param targetName object name of the mbean to deploy
    * @param replicantName replicant name
    * @throws Exception
    */
   protected void deploy(List<? extends InvokerHA> replicants, ObjectName targetName, String replicantName) throws Exception
   {
      for(InvokerHA replicant : replicants)
      {
         /* create ha-target in the server with the list of replicants and register 
          * it with the MBeanServer. */
         HATarget target = new MockHATarget(null, replicantName, null, 2);
         target.replicantsChanged(null, replicants, 0, false);
         replicant.registerBean(targetName, target);         
      }
   }

   /**
    * Simulate the undeployment of an mbean from a list of invoker endpoints.
    * 
    * @param replicants list of invoker endpoints
    * @param targetName object name of the mbean to deploy
    * @throws Exception
    */
   protected void undeploy(List<? extends InvokerHA> replicants, ObjectName targetName) throws Exception
   {
      for(InvokerHA replicant : replicants)
      {
         replicant.unregisterBean(targetName);
      }
      
      mbs.unregisterMBean(targetName);      
   }
   
   /**
    * Create invocation with optional transaction instance for the target mbean 
    * with the given hashcode.
    * 
    * @param tx instance of transaction
    * @param hashCode hashcode of object name of the mbean
    * @return
    */
   private Invocation createInvocation(Transaction tx, Integer hashCode, InvokerHaFailureType failureType, Invoker invoker)
   {
      Invocation inv = new Invocation();
      inv.setObjectName(hashCode);
      if (tx != null)
      {
         inv.setTransaction(tx);
      }
      if (failureType != null)
      {
         inv.setValue("FAILURE_TYPE", failureType);
      }

      InvocationContext ctx = new InvocationContext();
      ctx.setInvoker(invoker);
      inv.setInvocationContext(ctx);
      
      return inv;
   }
      
   /** Interfaces **/
   
   /**
    * Factory interface to be implemented by different type of invokers 
    * available, i.e. jrmp, unified, pooled...etc.
    */
   public interface InvokerHaFactory
   {
      /**
       * Returns the invoker type name 
       * 
       * @return String representing the invoker type
       */
      String getInvokerTypeName();
      
      /**
       * Return invoker ha instructure instance associated with this invoker. 
       *
       * @param serverCount number of invocation endpoints, or simulated cluster nodes
       * @return InvokerHaInfrastructure instance of infrastructure
       */      
      InvokerHaInfrastructure getInvokerHaInfrastructure(int serverCount);
      
      /**
       * Returns the invoker ha tx failover authorisations map.
       * 
       * @return WeakHashMap containing the tx failover authorisations.
       */
      Map getTxFailoverAuthorizationsMap();
      
      /**
       * Creates an instance of invoker endpoint.
       * 
       * @param serverName name of invoker endpoint
       * @param serverNumber invoker endpoint number
       * @return
       */
      InvokerHA createInvokerHaServer(String serverName, int serverNumber);
      
      /**
       * Returns transient payload key to retrieve chosen target.
       * 
       * @return String representation of the key
       */
      String getChosenTargetKey();
   }
   
   /**
    * Date time teller mbean interface
    */
   public interface DateTimeTellerMBean 
   {
      Object invoke(Invocation invocation) throws Exception;
   }
   
   /**
    * Systemr time teller mbean interface 
    */
   public interface SystemTimeTellerMBean
   {
      Object invoke(Invocation invocation) throws Exception;
   }
   
   public interface ClientUserTransactionServiceMBean
   {
      Object invoke(Invocation invocation) throws Exception;      
   }
   
   /** Classes **/
   
   public class DateTimeTeller implements DateTimeTellerMBean
   {
      public Object invoke(Invocation invocation) throws Exception
      {
         /* returns a Date representation of the current time */
         return new Date(System.currentTimeMillis());
      }
   }
   
   public class SystemTimeTeller implements SystemTimeTellerMBean
   {
      public Object invoke(Invocation invocation) throws Exception
      {
         /* returns a long (milliseconds) representation of the current time */
         return System.currentTimeMillis();
      }      
   }
   
   public class ClientUserTransactionService implements ClientUserTransactionServiceMBean
   {
      public Object invoke(Invocation invocation) throws Exception
      {
         // Return new tpc -> only begin() calls really tested as they set the sticky target
         return new UID();
      }
   }
   
   /**
    * MockHATarget class that avoids using DistributedReplicantManager in any 
    * way. The unit test will force changes in the composition of replicants, 
    * that avoids the need of replicant manager or listener. 
    */
   public class MockHATarget extends HATarget
   {
      public MockHATarget(HAPartition partition, String replicantName, Serializable target, int allowInvocations) throws Exception
      {
         super(partition, replicantName, target, allowInvocations);
      }

      /**
       * No-op to avoid DistributedReplicantManager being set up at within the 
       * super's constructor.
       */
      @Override
      public void updateHAPartition(HAPartition partition) throws Exception
      {
      }
   }
   
   /**
    * Load balance policy based on a delegate pattern that tracks down the 
    * chosen target for the invocation and puts it in the transient payload. 
    * This allows for UTs to inspect the content and potentially check whether 
    * load balance policies are working correctly.
    * 
    * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
    */
   public static class TraceLoadBalancePolicy implements LoadBalancePolicy
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = 3089456214843995414L;
      
      /** Load balance policy to delegate to */
      private LoadBalancePolicy delegateTo;

      public TraceLoadBalancePolicy(LoadBalancePolicy delegateTo)
      {
         this.delegateTo = delegateTo;
      }

      public Object chooseTarget(FamilyClusterInfo clusterFamily)
      {
         return delegateTo.chooseTarget(clusterFamily);
      }

      public Object chooseTarget(FamilyClusterInfo clusterFamily, Invocation routingDecision)
      {
         Object chosenTarget = delegateTo.chooseTarget(clusterFamily, routingDecision);
         
         /* put chosen target in the transient payload */
         routingDecision.getTransientPayload().put("TEST_CHOSEN_TARGET", chosenTarget);
         log.debug("chosen target: " + chosenTarget);
         return chosenTarget;
      }

      public void init(HARMIClient father)
      {
         delegateTo.init(father);
      }      
   }
   
   /**
    * Trace load balance policy specific for round robin. This allows for non 
    * argument construction of such load balance policy, which makes it testing 
    * cleaner. We just pass the load balance policy class and we use reflection 
    * to create a new instance.  
    * 
    * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
    */
   public static class TraceRoundRobin extends TraceLoadBalancePolicy
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = -8583420254744619692L;
      
      public TraceRoundRobin()
      {
         super(new RoundRobin());
      }
   }

   /**
    * Trace load balance policy specific for first available. This allows for non 
    * argument construction of such load balance policy, which makes it testing 
    * cleaner. We just pass the load balance policy class and we use reflection 
    * to create a new instance.  
    * 
    * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
    */
   public static class TraceFirstAvailable extends TraceLoadBalancePolicy
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = -1626190092127048933L;
    
      public TraceFirstAvailable()
      {
         super(new FirstAvailable());
      }      
   }

   /**
    * Trace load balance policy specific for first available indentical all 
    * proxies. This allows for non argument construction of such load balance 
    * policy, which makes it testing cleaner. We just pass the load balance 
    * policy class and we use reflection to create a new instance.  
    * 
    * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
    */
   public static class TraceFirstAvailableIdenticalAllProxies extends TraceLoadBalancePolicy
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = -8656749681577922508L;

      public TraceFirstAvailableIdenticalAllProxies()
      {
         super(new FirstAvailableIdenticalAllProxies());
      }      
   }
   
   /**
    * Trace load balance policy specific for random robin. This allows for non 
    * argument construction of such load balance policy, which makes it testing 
    * cleaner. We just pass the load balance policy class and we use reflection 
    * to create a new instance.  
    * 
    * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
    */
   public static class TraceRandomRobin extends TraceLoadBalancePolicy
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = -1626190092127048933L;
    
      public TraceRandomRobin()
      {
         super(new RandomRobin());
      }      
   }
}
