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

import javax.management.MBeanServerConnection;

import junit.framework.Test;

import org.apache.commons.httpclient.HttpClient;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.testutil.WebTestBase;
import org.jboss.test.cluster.web.JBossClusteredWebTestCase;

/**
 * Tests that a clustered session still functions properly on the second
 * node after the webapp is undeployed from the first node.
 * <p/>
 * This version tests a SessionBasedClusteredSession.
 * 
 * @author Brian Stansberry
 * @version $Id: UndeployTestCase.java 81084 2008-11-14 17:30:43Z dimitris@jboss.org $
 */
public class UndeployTestCase extends WebTestBase
{
   protected String setUrl_;
   protected String getUrl_;
   protected String setUrlBase_;
   protected String getUrlBase_;
   
   protected boolean deployed0_ = true;
   protected boolean deployed1_ = true;
   
   public UndeployTestCase(String name)
   {
      super(name);
      setUrlBase_ = "setSession.jsp";
      getUrlBase_ = "getAttribute.jsp";

      concatenate();
   }
   
   protected String getContextPath()
   {
      return "/http-scoped/";
   }
   
   protected String getWarName()
   {
      return "http-scoped.war";
   }

   public static Test suite() throws Exception
   {
      return JBossClusteredWebTestCase.getDeploySetup(UndeployTestCase.class,
                                                     "http-scoped.war");
   }

   /**
    * Main method that deals with the Http Session Replication Test
    *
    * @throws Exception
    */
   public void testRedeploy()
         throws Exception
   {
      String attr = "";
      getLog().info("Enter testRedeploy");

      getLog().debug(setUrl_ + ":::::::" + getUrl_);

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl_);

      // Create a method instance.
      // Get the Attribute set
      attr = makeGetWithState(client, baseURL0_ +getUrl_);
      
      reconfigureCluster();

      sleepThread(DEFAULT_SLEEP);

      // Make connection to server 1 and get
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);
      String attr2 = makeGetWithState(client, baseURL1_ + getUrl_);

      assertEquals("Get attribute should be but is ", attr, attr2);
      getLog().debug("Exit testRedeploy");
   }

   protected void concatenate()
   {
      setUrl_ = getContextPath() +setUrlBase_;
      getUrl_ = getContextPath() +getUrlBase_;
   }   
   
   protected void setUp() throws Exception
   {
      super.setUp();
      configureCluster();
   }

   protected void configureCluster() throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      String warName = getWarName();
      if (!deployed0_)
      {
         deploy(adaptors[0], warName);
         getLog().debug("Deployed " + warName + " on server0");
         deployed0_ = true;
      }
      if (!deployed1_)
      {
         deploy(adaptors[1], warName);
         getLog().debug("Deployed " + warName + " on server1");
         deployed1_ = true;
      }
   
      sleep(2000);
   }
   
   protected void reconfigureCluster() throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      deploy(adaptors[1], getWarName());
      deployed1_ = true;
      
      sleep(2000);
      
      undeploy(adaptors[0], getWarName());
      deployed0_ = false;
      
      sleep(2000);
   }

}
