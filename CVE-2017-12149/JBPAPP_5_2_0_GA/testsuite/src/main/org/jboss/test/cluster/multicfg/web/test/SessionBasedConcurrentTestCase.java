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
package org.jboss.test.cluster.multicfg.web.test;

import java.util.Random;

import junit.framework.Test;

import org.apache.commons.httpclient.HttpClient;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.testutil.WebTestBase;
import org.jboss.test.cluster.web.JBossClusteredWebTestCase;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;

/**
 * Simple clustering test case of get/set. It is session based granularity with concurrent access.
 *
 * @author Ben Wang
 * @version $Revision: 1.0
 */
public class SessionBasedConcurrentTestCase
      extends WebTestBase
{
   Throwable ex_ = null;
   final int PERMITS = 100;
   Semaphore sem_ = new Semaphore(PERMITS);
   String setURLName_;
   String getURLName_;

   public SessionBasedConcurrentTestCase(String name)
   {
      super(name);
      setURLName_ = "/http-sr/testsessionreplication.jsp";
      getURLName_ = "/http-sr/getattribute.jsp";
   }

   protected void setUp() throws Exception {
      super.setUp();
      ex_ = null;
   }

   public static Test suite() throws Exception
   {
      return JBossClusteredWebTestCase.getDeploySetup(SessionBasedConcurrentTestCase.class,
                                                      "http-sr.war");
   }

   /**
    * Test different set in different servers.
    * @throws Exception
    */
   public void testConcurrentPut()
         throws Exception
   {
      int TIMES = 10;
      for(int i=0; i < 10; i++)
      {
         String threadName = "startWithServer_1_ " +i;
         Thread t1 = runThread(threadName, baseURL0_, baseURL1_, servers_[1], TIMES, i);
         threadName = "startWithServer_2_ " +i;
         Thread t2 = runThread(threadName, baseURL1_, baseURL0_, servers_[0], TIMES, i);
         t1.start();
         t2.start();
      }

      sleepThread(1000);
      while(true) {
         if(sem_.permits() != PERMITS)
         {
            sleepThread(1000);
            continue;
         } else
         {
            break;
         }
      }

      if(ex_ != null)
      {
         fail("Test fail " +ex_);
      }
   }

   /**
    * Thread to execute the http request.
    * @param threadName
    * @return
    */
   protected Thread runThread(final String threadName, final String baseURL0, final String baseURL1,
                              final String server2, final int TIMES, final int SEED) {
      return new Thread(threadName) {
         Random rand = new Random(SEED);
         public void run() {
            try {
               sem_.acquire();
            } catch (InterruptedException e) {
               e.printStackTrace();
               ex_ = e;
               return;
            }

            try {
               for(int i=0; i < TIMES; i++)
               {
                  work();
                  // Random numbder between [0, 200].
                  long msecs = rand.nextInt(200);
                  sleepThread(msecs);
               }
            } finally {
               sem_.release();
            }
         }

         protected void work() {
            String attr = "";
            getLog().debug("Enter runThread");

            String setURLName = setURLName_;
            String getURLName = getURLName_;

            getLog().debug(setURLName + ":::::::" + getURLName);

            // Create an instance of HttpClient.
            HttpClient client = new HttpClient();

            try {
               // Set the session attribute first
               makeGet(client, baseURL0 +setURLName);
   
               // Get the Attribute set by testsessionreplication.jsp
               attr = makeGetWithState(client, baseURL0 +getURLName);
   
               // Let's switch to server 2 to retrieve the session attribute.
               getLog().debug("Switching to server " +server2);
               SessionTestUtil.setCookieDomainToThisServer(client, server2);
               String attr2 = makeGet(client, baseURL1 +getURLName);
   
               // Check the result
              assertEquals("Http session replication attribtues retrieved from both servers ", attr, attr2);
            } catch (Throwable ex) {
               ex_ = ex;
            }

            getLog().debug("Http Session Replication has happened");
            getLog().debug("Exit runThread");
         }
      };
  }
}
