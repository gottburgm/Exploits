package org.jboss.test.invokers.test;

import java.util.ArrayList;
import java.util.List;

import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.RoundRobin;
import org.jboss.invocation.InvokerInterceptor;
import org.jboss.invocation.unified.interfaces.UnifiedInvokerHAProxy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.Invoker;
import org.jboss.remoting.InvokerLocator;
import org.jboss.system.Registry;
import org.jboss.test.JBossTestCase;


public class JBPAPP6428TestCase extends JBossTestCase
{
   private static String CHECK_PARTITION_PROPERTY = "org.jboss.invocation.use.partition.name";

   private static String PARTITION_NAME = "jboss.partition.name";

   public JBPAPP6428TestCase(String name)
   {
      super(name);
   }

   private Invoker dummyInvoker = new Invoker()
   {
      @Override
      public String getServerHostName() throws Exception
      {
         return null;
      }
      
      @Override
      public Object invoke(Invocation invocation) throws Exception
      {
         return null;
      }
   };

   private InvokerInterceptor getDummyInvoker()
   {
      InvokerInterceptor invokerInterceptor = new InvokerInterceptor();

      // this needs to be set to get back the first if
      invokerInterceptor.setLocal(dummyInvoker);

      return invokerInterceptor;
   }

   public Invocation getDummyInvocation(String destinationCluster)
   {
      Invocation invocation = new Invocation();
      try
      {
         // Setup context to have name in org.jboss.invocation.InvokerProxyHA.getFamilyClusterInfo().getFamilyName()
         InvokerLocator locator = new InvokerLocator("http://localhost:8080/dummyInvokerPath");
         List targets = new ArrayList();
         LoadBalancePolicy loadBalancePolicy = new RoundRobin();
         String clusterFamilyName = destinationCluster;
         UnifiedInvokerHAProxy invokerProxy = new UnifiedInvokerHAProxy(locator, false, targets, loadBalancePolicy, clusterFamilyName, 0L);
                  
         InvocationContext invocationContext = new InvocationContext();
         invocationContext.setInvoker(invokerProxy);      
         invocation.setInvocationContext(invocationContext);
         
         invocation.setObjectName("JBPAPP6428Test-dummy-"+destinationCluster);
         
         Registry.bind(invocation.getObjectName(), invocation);
      } 
      catch ( Exception e)
      {
         e.printStackTrace();
      }
      return invocation;
   }

   public void testCheckPartitionTrue()
   {
      System.setProperty(CHECK_PARTITION_PROPERTY, "true");

      // This is the cluster that the server is set to -g cluster1
      System.setProperty(PARTITION_NAME, "cluster1");

      // Test when invocation should go to cluster1 - isLocal should return true
      String destinationCluster = "cluster1/";
      InvokerInterceptor invokerInterceptor = getDummyInvoker();
      Invocation invocation = getDummyInvocation(destinationCluster);
      assertTrue("Test destination == server cluster", invokerInterceptor.isLocal(invocation));
      Registry.unbind(invocation.getObjectName());
         
      // Test when invocation should go to cluster2 - isLocal should return false      
      destinationCluster = "cluster2/";
      invokerInterceptor = getDummyInvoker();
      invocation = getDummyInvocation(destinationCluster);            
      assertFalse("Test destination != server cluster", invokerInterceptor.isLocal(invocation));
      Registry.unbind(invocation.getObjectName());
   }
}
