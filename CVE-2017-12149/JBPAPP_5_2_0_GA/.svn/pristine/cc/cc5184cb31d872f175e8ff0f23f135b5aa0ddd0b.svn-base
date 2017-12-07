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
package org.jboss.test.jca.test;

import java.net.URL;

import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.resource.adapter.jdbc.local.LocalManagedConnectionFactory;
import org.jboss.resource.connectionmanager.ConnectionListener;
import org.jboss.resource.connectionmanager.InternalManagedConnectionPool;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool;
import org.jboss.resource.connectionmanager.ManagedConnectionPool;
import org.jboss.resource.connectionmanager.NoTxConnectionManager;
import org.jboss.resource.connectionmanager.PreFillPoolSupport;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.support.PoolHelper;

public class BackgroundValidationUnitTestCase extends JBossTestCase
{
   Logger log = Logger.getLogger(PreFillPoolingUnitTestCase.class);

   private static ObjectName INVALID_BACKGROUND_POOL = ObjectNameFactory.create("jboss.jca:name=TestFailedBackgroundDS,service=ManagedConnectionPool");
   private static ObjectName VALID_BACKGROUND_POOL = ObjectNameFactory.create("jboss.jca:name=TestSuccessBackgroundDS,service=ManagedConnectionPool");
   private static ObjectName INVALID_MATCH_POOL = ObjectNameFactory.create("jboss.jca:name=TestNonMatchDS,service=ManagedConnectionPool");
   private static ObjectName VALID_MATCH_POOL = ObjectNameFactory.create("jboss.jca:name=TestValidationMatchDS,service=ManagedConnectionPool");

   
   public BackgroundValidationUnitTestCase(String name) throws Exception
   {
      super(name);
      
   }
   
   /**
    * Test for connection background validation
    * 
    * Pool: PoolByCri
    * Deployed *-ds.xml: test-background-failed-validation-ds.xml
    * 
    * Background validation is enabled, and a connection is acquired. The FailedValidation checker
    * is being used. Background validation destroys the connections in the pool and the destroyed
    * count is incremented. 
    * 
    * Next, we set background validation to false, flush the pool and acquire the connection. 
    * Since background validation is disabled, the pool should fill to the minimum allowance.
    * 
    * @throws Exception
    */
   public void testDeployedBackgroundValidationFailure() throws Exception
   {
      
      InitialContext ctx = super.getInitialContext();
      DataSource ds = (DataSource) ctx.lookup("TestFailedBackgroundDS");
      ds.getConnection("sa", "").close();
      
      Integer minCount = PoolHelper.getMinSize(getServer(), INVALID_BACKGROUND_POOL);
         
      Long backMillis = PoolHelper.getBackgroundValMillis(getServer(), INVALID_BACKGROUND_POOL);
      
      PoolHelper.sleepForValidation(backMillis.intValue());
    
      Integer destroyedCount = PoolHelper.getDestroyed(getServer(), INVALID_BACKGROUND_POOL); 
         
      assertTrue("Background validation ran. Destroyed count should exceed zero.", destroyedCount.intValue() > 0);
      
      Integer connCount = PoolHelper.getConnectionCount(getServer(), INVALID_BACKGROUND_POOL); 

      //assertTrue(connCount.intValue() == minCount.intValue());
      
      PoolHelper.setPoolAttributeAndFlush(getServer(), INVALID_BACKGROUND_POOL, PoolHelper.POOL_ATT_BACKGROUND_VAL_MILLIS, new Long(0));
            
      //Reprime the pool
      ds.getConnection().close();
      
      PoolHelper.sleepForValidation(backMillis.intValue());

      destroyedCount = PoolHelper.getDestroyed(getServer(), INVALID_BACKGROUND_POOL); 
      connCount = PoolHelper.getConnectionCount(getServer(), INVALID_BACKGROUND_POOL); 
      
      assertTrue("Background validation is disabled. Destroyed count should be zero", destroyedCount.intValue() == 0);
      assertTrue("Background validation is disabled. Pool should be filled to min", connCount.intValue() == minCount.intValue());
      
   
   }
  
   public void testDeployedBackgroundValidationSuccess() throws Exception
   {
      InitialContext ctx = super.getInitialContext();
      DataSource ds = (DataSource) ctx.lookup("TestSuccessBackgroundDS");
      ds.getConnection("sa", "").close();

      Long backMillis = PoolHelper.getBackgroundValMillis(getServer(), VALID_BACKGROUND_POOL);
            
      PoolHelper.sleepForValidation(backMillis.intValue());
    
      Integer destroyedCount = PoolHelper.getDestroyed(getServer(), VALID_BACKGROUND_POOL);
      

      assertTrue("Background validation ran on valid pool. Destroyed count should not exceed zero.", destroyedCount.intValue() == 0);
      
      
   }
   
   
   /**
    * Pool: PoolByCri
    * Deployed *-ds.xml: test-non-validation-match-ds.xml
    * 
    * @throws Exception
    */
   public void testDeployedNonValidateOnMatch() throws Exception
   {

      InitialContext ctx = super.getInitialContext();
      DataSource ds = (DataSource) ctx.lookup("TestNonMatchDS");
      ds.getConnection("sa", "").close();

      Integer destroyed = PoolHelper.getDestroyed(getServer(), INVALID_MATCH_POOL);

      assertTrue("Validation should not have occured at this point.", destroyed.intValue() == 0);

      //No new-connection-sql provided, though connections are invalid, first one will
      //succeed because a matchManagedConnections is not called...
      ds.getConnection("sa", "").close();

      destroyed = PoolHelper.getDestroyed(getServer(), INVALID_MATCH_POOL);

      assertTrue(
            "Validation on match is set to true for invalid connections. Destroyed count should be greater than zero.",
            destroyed.intValue() > 0);
       
      
   }
   /**
    * Pool: PoolByCri
    * Deployed *-ds.xml: test-non-validation-match-ds.xml
    * 
    * @throws Exception
    */
   public void testDeployedValidateOnMatch() throws Exception
   {

      InitialContext ctx = super.getInitialContext();
      DataSource ds = (DataSource) ctx.lookup("TestValidationMatchDS");
      ds.getConnection("sa", "").close();

      Integer destroyed = PoolHelper.getDestroyed(getServer(), VALID_MATCH_POOL);

      assertTrue("Validation should not have occured at this point.", destroyed.intValue() == 0);

      ds.getConnection("sa", "").close();

      destroyed = PoolHelper.getDestroyed(getServer(), VALID_MATCH_POOL);

      assertTrue("Validation on match is set to true for connections. Destroyed count should be zero.", destroyed
            .intValue() == 0);

         
         
   
   }
   public void testValidateOnMatchSuccess() throws Exception
   {
      LocalManagedConnectionFactory mcf = new LocalManagedConnectionFactory();
      
      mcf.setDriverClass("org.jboss.test.jca.jdbc.TestDriver");
      mcf.setConnectionURL("jdbc:jboss-test-adapter");
      mcf.setValidConnectionCheckerClassName("org.jboss.test.jca.support.MockSuccessValidationConnectionChecker");
      mcf.setValidateOnMatch(true);
      
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 1;
      pp.maxSize = 2;
      pp.blockingTimeout = 10000;
      pp.idleTimeout = 0;
      pp.prefill = true;
      pp.backgroundInterval = 1 * 1000 * 30;
      ManagedConnectionPool mcp = new JBossManagedConnectionPool.OnePool(mcf, pp, false, log);
      NoTxConnectionManager noTxn = new NoTxConnectionManager(null, mcp);
      mcp.setConnectionListenerFactory(noTxn);

      ((PreFillPoolSupport)mcp).prefill();
      
      //Let prefiller run
      Thread.sleep(5000);
      
      ConnectionListener cl = noTxn.getManagedConnection(null, null);
      noTxn.returnManagedConnection(cl, false);
      
      assertTrue(mcp.getConnectionDestroyedCount() == 0);

      
   }
   public void testBasicValidateOnMatchFailure() throws Exception
   {
      LocalManagedConnectionFactory mcf = new LocalManagedConnectionFactory();
      
      mcf.setDriverClass("org.jboss.test.jca.jdbc.TestDriver");
      mcf.setConnectionURL("jdbc:jboss-test-adapter");
      mcf.setValidConnectionCheckerClassName("org.jboss.test.jca.support.MockFailedValidationConnectionChecker");
      mcf.setValidateOnMatch(true);

      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 1;
      pp.maxSize = 2;
      pp.blockingTimeout = 10000;
      pp.idleTimeout = 0;
      pp.prefill = true;
      pp.backgroundInterval = 1 * 1000 * 30;
      ManagedConnectionPool mcp = new JBossManagedConnectionPool.OnePool(mcf, pp, false, log);
      NoTxConnectionManager noTxn = new NoTxConnectionManager(null, mcp);
      mcp.setConnectionListenerFactory(noTxn);

      ((PreFillPoolSupport)mcp).prefill();
      
      //Let prefiller run
      Thread.sleep(5000);
      
      ConnectionListener cl = noTxn.getManagedConnection(null, null);
      noTxn.returnManagedConnection(cl, false);
      
      assertTrue(mcp.getConnectionDestroyedCount() > 0);
      
   }
   
   
   public void testBasicBackgroundValidationSuccess() throws Exception
   {
      
      LocalManagedConnectionFactory mcf = new LocalManagedConnectionFactory();
      
      mcf.setDriverClass("org.jboss.test.jca.jdbc.TestDriver");
      mcf.setConnectionURL("jdbc:jboss-test-adapter");
      mcf.setValidConnectionCheckerClassName("org.jboss.test.jca.support.MockSuccessValidationConnectionChecker");
      
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 1;
      pp.maxSize = 2;
      pp.blockingTimeout = 10000;
      pp.idleTimeout = 0;
      pp.prefill = false;
      pp.backgroundInterval = 2000;
      ManagedConnectionPool mcp = new JBossManagedConnectionPool.OnePool(mcf, pp, false, log);
      NoTxConnectionManager noTxn = new NoTxConnectionManager(null, mcp);
      mcp.setConnectionListenerFactory(noTxn);
      
      ConnectionListener cl = noTxn.getManagedConnection(null, null);
      noTxn.returnManagedConnection(cl, false);
      
      Thread.sleep(pp.backgroundInterval);
      assertTrue(mcp.getConnectionDestroyedCount() == 0);
      
   }
   public void testBasicBackgroundValidationDestroy() throws Exception
   {
      
      LocalManagedConnectionFactory mcf = new LocalManagedConnectionFactory();
      
      mcf.setDriverClass("org.jboss.test.jca.jdbc.TestDriver");
      mcf.setConnectionURL("jdbc:jboss-test-adapter");
      mcf.setValidConnectionCheckerClassName("org.jboss.test.jca.support.MockFailedValidationConnectionChecker");
      
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 1;
      pp.maxSize = 2;
      pp.blockingTimeout = 10000;
      pp.idleTimeout = 0;
      pp.prefill = false;
      pp.backgroundInterval = 2000;
      ManagedConnectionPool mcp = new JBossManagedConnectionPool.OnePool(mcf, pp, false, log);
      NoTxConnectionManager noTxn = new NoTxConnectionManager(null, mcp);
      mcp.setConnectionListenerFactory(noTxn);
      
      ConnectionListener cl = noTxn.getManagedConnection(null, null);
      noTxn.returnManagedConnection(cl, false);
      
      Thread.sleep(pp.backgroundInterval);
      assertTrue(mcp.getConnectionDestroyedCount() > 0);
      
   }
   
   public static Test suite() throws Exception{
      
      Test test1 = getDeploySetup(BackgroundValidationUnitTestCase.class, "jca-support.sar");
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL resURL = loader.getResource("jca/validation/test-background-failed-validation-ds.xml");
      Test test2 = getDeploySetup(test1, resURL.toString());
      resURL = loader.getResource("jca/validation/test-background-success-validation-ds.xml"); 
      Test test3 = getDeploySetup(test2, resURL.toString());
      resURL = loader.getResource("jca/validation/test-non-validation-match-ds.xml");
      Test test4 = getDeploySetup(test3, resURL.toString());
      resURL = loader.getResource("jca/validation/test-validation-match-ds.xml");
      return getDeploySetup(test4, resURL.toString());
      
           
   }
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
   }

}
