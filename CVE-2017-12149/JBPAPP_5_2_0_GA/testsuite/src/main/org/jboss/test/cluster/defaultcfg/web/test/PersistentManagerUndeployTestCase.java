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
package org.jboss.test.cluster.defaultcfg.web.test;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import org.apache.commons.httpclient.HttpClient;
import org.jboss.test.cluster.testutil.DBSetupDelegate;
import org.jboss.test.cluster.testutil.DelegatingClusteredTestCase;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.testutil.TestSetupDelegate;
import org.jboss.test.cluster.testutil.WebTestBase;
import org.jboss.test.cluster.web.persistent.PersistentStoreSetupDelegate;

/**
 * Tests that a clustered session still functions properly on the second
 * node after the webapp is undeployed from the first node.
 * <p/>
 * This version tests a SessionBasedClusteredSession.
 * 
 * @author Brian Stansberry
 * @version $Id: UndeployTestCase.java 81084 2008-11-14 17:30:43Z dimitris@jboss.org $
 */
public class PersistentManagerUndeployTestCase extends WebTestBase
{
   protected String setUrl_;
   protected String getUrl_;
   protected String setUrlBase_;
   protected String getUrlBase_;
   
   private static boolean deployed0_ = true;
   private static boolean deployed1_ = true;
   
   public PersistentManagerUndeployTestCase(String name)
   {
      super(name);
      setUrlBase_ = "setSession.jsp";
      getUrlBase_ = "getAttribute.jsp";

      concatenate();
   }
   
   protected String getContextPath()
   {
      return "/http-scoped-persistent/";
   }
   
   protected String getWarName()
   {
      return "http-scoped-persistent.war";
   }

   public static Test suite() throws Exception
   {
      String dbAddress = System.getProperty(DBSetupDelegate.DBADDRESS_PROPERTY, DBSetupDelegate.DEFAULT_ADDRESS);
      TestSetupDelegate dbDelegate = new DBSetupDelegate(dbAddress, DBSetupDelegate.DEFAULT_PORT);
      TestSetupDelegate storeDelegate = new PersistentStoreSetupDelegate(dbAddress, DBSetupDelegate.DEFAULT_PORT);
      List<TestSetupDelegate> list = Arrays.asList(new TestSetupDelegate[]{dbDelegate, storeDelegate});
      return DelegatingClusteredTestCase.getDeploySetup(PersistentManagerUndeployTestCase.class,
                                                      "httpsession-ds.xml, disable-manager-override.beans, " +
                                                      "http-scoped-persistent.war", list);
   }

   /**
    * Tests creating session on a node when the 2nd node is already running,
    * then undeploying war, then successfully reading session from 2nd node
    *
    * @throws Exception
    */
   public void testFailoverAfterFirstNodeStop()
         throws Exception
   {
      String attr = "";
      getLog().info("Enter testSecondNodeStartAfterFirstNodeStop");

      getLog().debug(setUrl_ + ":::::::" + getUrl_);

      deploy(true);
      deploy(false);
      
      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl_);

      // Create a method instance.
      // Get the Attribute set
      attr = makeGetWithState(client, baseURL0_ +getUrl_);
      
      undeploy(true);

      sleepThread(DEFAULT_SLEEP);

      // Make connection to server 1 and get
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);
      String attr2 = makeGetWithState(client, baseURL1_ + getUrl_);

      assertEquals("Get attribute should be but is ", attr, attr2);
      getLog().debug("Exit testSecondNodeStartAfterFirstNodeStop");
   }

   /**
    * Tests creating session on a node then undeploying war, then deploying
    * on 2nd node, then successfully reading session
    *
    * @throws Exception
    */
   public void testSecondNodeStartAfterFirstNodeStop()
         throws Exception
   {
      String attr = "";
      getLog().info("Enter testSecondNodeStartAfterFirstNodeStop");

      getLog().debug(setUrl_ + ":::::::" + getUrl_);

      deploy(true);
      undeploy(false);
      
      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl_);

      // Create a method instance.
      // Get the Attribute set
      attr = makeGetWithState(client, baseURL0_ +getUrl_);
      
      undeploy(true);
      deploy(false);

      sleepThread(DEFAULT_SLEEP);

      // Make connection to server 1 and get
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);
      String attr2 = makeGetWithState(client, baseURL1_ + getUrl_);

      assertEquals("Get attribute should be but is ", attr, attr2);
      getLog().debug("Exit testSecondNodeStartAfterFirstNodeStop");
   }

   /**
    * Tests creating session on a node with the 2nd node running,
    * then undeploying war from both nodes, then deploying
    * on 2nd node, then successfully reading session
    *
    * @throws Exception
    */
   public void testFailoverAfterFullStop()
         throws Exception
   {
      String attr = "";
      getLog().info("Enter testSecondNodeStartAfterFirstNodeStop");

      getLog().debug(setUrl_ + ":::::::" + getUrl_);

      deploy(true);
      deploy(false);
      
      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl_);

      // Create a method instance.
      // Get the Attribute set
      attr = makeGetWithState(client, baseURL0_ +getUrl_);
      
      undeploy(true);
      undeploy(false);
      deploy(false);

      sleepThread(DEFAULT_SLEEP);

      // Make connection to server 1 and get
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);
      String attr2 = makeGetWithState(client, baseURL1_ + getUrl_);

      assertEquals("Get attribute should be but is ", attr, attr2);
      getLog().debug("Exit testSecondNodeStartAfterFirstNodeStop");
   }

   protected void concatenate()
   {
      setUrl_ = getContextPath() +setUrlBase_;
      getUrl_ = getContextPath() +getUrlBase_;
   }
   
   protected void deploy(boolean first) throws Exception
   {
      if (first && !deployed0_)
      {
         deploy(getAdaptors()[0], getWarName());
         getLog().debug("Deployed " + getWarName() + " on server0");
         deployed0_ = true;
      }
      else if (!first && !deployed1_)
      {
         deploy(getAdaptors()[1], getWarName());
         getLog().debug("Deployed " + getWarName() + " on server1");
         deployed1_ = true;         
      }
   }
   
   protected void undeploy(boolean first) throws Exception
   {
      if (first && deployed0_)
      {
         undeploy(getAdaptors()[0], getWarName());
         getLog().debug("Undeployed " + getWarName() + " from server0");
         deployed0_ = false;
      }
      else if (!first && deployed1_)
      {
         undeploy(getAdaptors()[1], getWarName());
         getLog().debug("Undeployed " + getWarName() + " from server1");
         deployed1_ = false;         
      }
   }

}
