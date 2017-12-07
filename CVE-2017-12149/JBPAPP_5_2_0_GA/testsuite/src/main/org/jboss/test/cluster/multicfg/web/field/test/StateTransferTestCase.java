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
package org.jboss.test.cluster.multicfg.web.field.test;

import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.apache.commons.httpclient.HttpClient;
import org.jboss.cache.Fqn;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.testutil.WebTestBase;
import org.jboss.test.cluster.web.JBossClusteredWebTestCase;

/**
 * Tests the use of the TreeCache.activateRegion()/inactivateRegion().
 * 
 * NOTE: This test doesn't directly use FIELD but is in the field
 * test package because it's a test of region activation/deactivation.
 *
 * TODO add a concurrency test.
 * 
 * @author <a href="mailto://brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class StateTransferTestCase extends WebTestBase
{

   private static boolean deployed0 = true;
   private static boolean deployed1 = true;
   
   protected String setUrl_;
   protected String getUrl_;
   protected String setUrlBase_;
   protected String getUrlBase_;
   
   private ObjectName warObjectName;
   private MBeanServerConnection adaptor0_;
   private MBeanServerConnection adaptor1_;
   private String warFqn_;
   
   /**
    * Create a new StateTransferTestCase.
    * 
    * @param name
    */
   public StateTransferTestCase(String name)
   {
      super(name);
      setUrlBase_ = "setSession.jsp";
      getUrlBase_ = "getAttribute.jsp";
      concatenate();
   }

   protected void concatenate()
   {
      String contextPath = "/" + getWarName() + "/";
      setUrl_ = contextPath +setUrlBase_;
      getUrl_ = contextPath +getUrlBase_;
   }

   public static Test suite() throws Exception
   {
      return JBossClusteredWebTestCase.getDeploySetup(StateTransferTestCase.class,
                                                      "http-scoped.war");
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      
      MBeanServerConnection[] adaptors = getAdaptors();
      // Tell the CacheHelper that this isn't a PojoCache-based test
      for (MBeanServerConnection adaptor : adaptors)
      {
         SessionTestUtil.setCacheConfigName(adaptor, getCacheConfigName(), false);
      }
      
      if (warObjectName == null)
      {
         String oname = "jboss.web:J2EEApplication=none,J2EEServer=none," +
                        "j2eeType=WebModule,name=//localhost/" + getWarName();
         warObjectName = new ObjectName(oname);
         
         
         adaptor0_ = adaptors[0];
         adaptor1_ = adaptors[1];
         
         Object[] names = {"JSESSION", SessionTestUtil.getContextHostPath("localhost", getWarName()) };
         Fqn fqn = Fqn.fromElements(names);
         warFqn_ = fqn.toString();
      }
      
      if (!deployed0)
      {
         deploy(adaptor0_, getWarName() + ".war");
         deployed0 = true;
      }
      
      if (!deployed1)
      {
         deploy(adaptor1_, getWarName() + ".war");
         deployed1 = true;
      }
   }
   
   protected String getWarName()
   {
      return "http-scoped";
   }
   
   public void testActivationInactivation() throws Exception
   {
      getLog().debug("Enter testActivationInactivation");

      getLog().debug(setUrl_ + ":::::::" + getUrl_);
      
      Set sessions = SessionTestUtil.getSessionIds(adaptor0_, warFqn_);
      assertEquals("server0 has no cached sessions", 0, sessions.size());
      
      // Stop the war on server1
      undeploy(adaptor1_, getWarName() + ".war");

      // Confirm the war isn't available on server1
      HttpClient client0 = new HttpClient();
      makeGetFailed(client0, baseURL1_ +setUrl_);
      
      // Create 3 sessions on server0
      HttpClient[] clients = new HttpClient[3];
      String[] attrs = new String[clients.length];
      for (int i = 0; i < clients.length; i++)
      {
         clients[i] = new HttpClient();
         makeGet(clients[i], baseURL0_ +setUrl_);
         attrs[i] = makeGet(clients[i], baseURL0_ + getUrl_);
         // Set cookie domain to server1
         SessionTestUtil.setCookieDomainToThisServer(clients[i], servers_[1]);
      }
      
      getLog().debug("Sessions created");
      
      // Confirm there are no sessions in the server1 cache
      sessions = SessionTestUtil.getSessionIds(adaptor1_, warFqn_);
      
      assertEquals("server1 has no cached sessions", 0, sessions.size());
      
      getLog().debug("Server1 has no cached sessions");
      
      // Start the war on server1
      deploy(adaptor1_, getWarName() + ".war");
      
      getLog().debug("Server1 started");
      
      // Confirm the sessions are in the server1 cache
      sessions = SessionTestUtil.getSessionIds(adaptor1_, warFqn_);

      assertEquals("server1 has cached sessions", clients.length, sessions.size());
      
      getLog().debug("Server1 has cached sessions");
      
      for (int i = 0; i < clients.length; i++)
      {
         String attr = makeGet(clients[i], baseURL1_ + getUrl_);
         assertEquals("attribute matches for client " + i, attrs[i], attr);
      }
      
      getLog().debug("Attributes match");
      
      // Sleep a bit in case the above get triggers replication that takes
      // a while -- don't want a repl to arrive after the cache is cleared
      sleep(500);
      
      // Stop the war on server0
      undeploy(adaptor0_, getWarName() + ".war");
      
      boolean buddyRepl = SessionTestUtil.isBuddyReplication();
      if (buddyRepl)
      {
         sessions = SessionTestUtil.getSessionIds(adaptor0_, warFqn_);
         
         assertEquals("server0 has three cached sessions", 3, sessions.size());
         sessions = SessionTestUtil.getSessionIds(adaptor0_, warFqn_, false);
         
         assertEquals("server0 has no cached sessions outside buddy backup", 0, sessions.size());
      }
      else
      {
         // Confirm there are no sessions in the server0 cache
         sessions = SessionTestUtil.getSessionIds(adaptor0_, warFqn_);
         
         assertEquals("server0 has no cached sessions", 0, sessions.size());
         
         getLog().debug("Server0 has no cached sessions");
      }
   }
}
