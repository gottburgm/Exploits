/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cluster.defaultcfg.web.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.Assert;
import junit.framework.Test;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.jboss.test.JBossClusteredTestCase;

/**
 * A CleanShutdownTestCase.
 * 
 * @author Paul Ferraro
 */
public class CleanShutdownTestCase extends JBossClusteredTestCase
{
   private static final String SERVER_NAME = "jboss.web.deployment:war=/http-sr";
   private static final String SHUTDOWN_METHOD = "stop";
   private static final String URL = "%s/http-sr/sleep.jsp?sleep=%d";
   private static final int MAX_THREADS = 2;
   private static final int REQUEST_DURATION = 10000;
   
   ObjectName name;
   MBeanServerConnection server;
   HttpClient client;
   String baseURL;
   
   private MultiThreadedHttpConnectionManager manager;
   
   public static Test suite() throws Exception
   {
      return JBossClusteredTestCase.getDeploySetup(CleanShutdownTestCase.class, "http-sr.war");
   }
   
   /**
    * Create a new CleanShutdownTestCase.
    * 
    * @param name
    * @throws MalformedObjectNameException 
    */
   public CleanShutdownTestCase(String name) throws MalformedObjectNameException
   {
      super(name);
   }

   /**
    * @see org.jboss.test.JBossClusteredTestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      this.name = ObjectName.getInstance(SERVER_NAME);
      this.server = this.getAdaptors()[0];
      this.baseURL = this.getHttpURLs()[0];

      this.manager = new MultiThreadedHttpConnectionManager();
      
      HttpConnectionManagerParams params = new HttpConnectionManagerParams();
      params.setDefaultMaxConnectionsPerHost(MAX_THREADS);
      params.setMaxTotalConnections(MAX_THREADS);
      
      this.manager.setParams(params);

      this.client = new HttpClient();
   }

   /**
    * @see org.jboss.test.JBossTestCase#tearDown()
    */
   @Override
   protected void tearDown() throws Exception
   {
      this.manager.shutdown();
      
      try
      {
         super.tearDown();
      }
      catch (Exception e)
      {
         // Webapp undeploy failed because server has shutdown
      }
   }

   public void testShutdown() throws Exception
   {
      ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
      
      try
      {
         // Make sure a normal request will succeed
         Assert.assertEquals(200, new RequestTask(0).call().intValue());
         
         // Send a long request - in parallel
         Future<Integer> future = executor.submit(new RequestTask(REQUEST_DURATION));

         // Make sure long request has started
         Thread.sleep(1000);

         // Shutdown server
         this.server.invoke(this.name, SHUTDOWN_METHOD, null, null);

         // Get result of long request
         // This request should succeed since it initiated before server shutdown
         try
         {
            Assert.assertEquals(200, future.get().intValue());
         }
         catch (ExecutionException e)
         {
            e.printStackTrace(System.err);
            
            Assert.fail(e.getCause().getMessage());
         }
         
         // Subsequent request should return 404
         Assert.assertEquals(404, new RequestTask(0).call().intValue());
      }
      finally
      {
         executor.shutdownNow();
      }
   }
   
   private class RequestTask implements Callable<Integer>
   {
      private final int sleep;
      
      RequestTask(int sleep)
      {
         this.sleep = sleep;
      }
      
      public Integer call() throws Exception
      {
         GetMethod method = new GetMethod(String.format(URL, CleanShutdownTestCase.this.baseURL, this.sleep));
         
         try
         {
            return CleanShutdownTestCase.this.client.executeMethod(method);
         }
         finally
         {
            method.releaseConnection();
         }
      }
   }
}
