/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.cluster.defaultcfg.simpleweb.test;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.jboss.logging.Logger;
import org.jboss.test.cluster.testutil.DBSetupDelegate;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.web.persistent.PersistentSessionTestUtil;
import org.jboss.test.cluster.web.persistent.SimplePersistentStoreTestSetup;
import org.jboss.web.tomcat.service.session.persistent.DataSourcePersistentManager;

/**
 * Unit tests of session count management with a persistent manager.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class PersistentStoreSessionCountUnitTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(PersistentStoreSessionCountUnitTestCase.class);
   
   private static long testCount = System.currentTimeMillis();
   
   /**
    * Create a new SessionCountUnitTestCase.
    * 
    * @param name
    */
   public PersistentStoreSessionCountUnitTestCase(String name)
   {
      super(name);
   } 

   public static Test suite() throws Exception
   {
      String dbAddress = System.getProperty(DBSetupDelegate.DBADDRESS_PROPERTY, DBSetupDelegate.DEFAULT_ADDRESS);
      return SimplePersistentStoreTestSetup.getDeploySetup(PersistentStoreSessionCountUnitTestCase.class, dbAddress, DBSetupDelegate.DEFAULT_PORT);
   }

   public void testStandaloneMaxSessions() throws Exception
   {
      log.info("Enter testStandaloneMaxSessions");
      
      ++testCount;
      
      DataSourcePersistentManager mgr = PersistentSessionTestUtil.createManager("test" + testCount, 5, null);       
      PersistentSessionTestUtil.configureManager(mgr, 2);
      
      mgr.start();
      
      assertFalse("Passivation is disabled", mgr.isPassivationEnabled());
      assertEquals("Correct max active count", 2, mgr.getMaxActiveAllowed());
      
      // Set up a session
      Session sess1 = createAndUseSession(mgr, "1", true, true);
      
      assertEquals("Session count correct", 1, mgr.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr.getLocalActiveSessionCount());
      
      createAndUseSession(mgr, "2", true, true);
      
      assertEquals("Session count correct", 2, mgr.getActiveSessionCount());
      assertEquals("Local session count correct", 2, mgr.getLocalActiveSessionCount());
      
      // Should fail to create a 3rd
      createAndUseSession(mgr, "3", false, false);
      
      // Confirm a session timeout clears space
      sess1.setMaxInactiveInterval(1);       
      SessionTestUtil.sleepThread(1100);      
      
      createAndUseSession(mgr, "3", true, true);      
      
      assertEquals("Session count correct", 2, mgr.getActiveSessionCount());
      assertEquals("Local session count correct", 2, mgr.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 3, mgr.getCreatedSessionCount());
      assertEquals("Expired session count correct", 1, mgr.getExpiredSessionCount());
   }
   
   public void testStandaloneMaxSessionsWithMaxIdle()
         throws Exception
   {
      log.info("Enter testStandaloneMaxSessionsWithMaxIdle");
      
      ++testCount;
      DataSourcePersistentManager mgr = PersistentSessionTestUtil.createManager("test" + testCount, 5, null);
       
      PersistentSessionTestUtil.configureManager(mgr, 1, true, 1, -1);
      
      mgr.start();
      
      assertTrue("Passivation is enabled", mgr.isPassivationEnabled());
      assertEquals("Correct max active count", 1, mgr.getMaxActiveAllowed());
      assertEquals("Correct max idle time", 1, mgr.getPassivationMaxIdleTime());
      assertEquals("Correct min idle time", -1, mgr.getPassivationMinIdleTime());

      // Set up a session
      Session sess1 = createAndUseSession(mgr, "1", true, true);
      
      assertEquals("Session count correct", 1, mgr.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr.getLocalActiveSessionCount());
      
      // Should fail to create a 2nd
      createAndUseSession(mgr, "2", false, false);
      
      // Confirm a session timeout clears space
      sess1.setMaxInactiveInterval(1);       
      SessionTestUtil.sleepThread(1100);      
      
      createAndUseSession(mgr, "2", true, true);      
      
      assertEquals("Session count correct", 1, mgr.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 2, mgr.getCreatedSessionCount());
      assertEquals("Expired session count correct", 1, mgr.getExpiredSessionCount());
      assertEquals("Passivated session count correct", 0, mgr.getPassivatedSessionCount());

      //    Sleep past maxIdleTime
      SessionTestUtil.sleepThread(1100);        
      
      assertEquals("Passivated session count correct", 0, mgr.getPassivatedSessionCount());
      
      createAndUseSession(mgr, "3", true, true);      
      
      assertEquals("Session count correct", 1, mgr.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 3, mgr.getCreatedSessionCount());
      assertEquals("Expired session count correct", 1, mgr.getExpiredSessionCount());
      assertEquals("Passivated session count correct", 1, mgr.getPassivatedSessionCount());
      
   }
   
   public void testStandaloneMaxSessionsWithMinIdle() throws Exception
   {
      log.info("Enter testStandaloneMaxSessionsWithMinIdle");
      
      ++testCount;
      DataSourcePersistentManager mgr = PersistentSessionTestUtil.createManager("test" + testCount, 5, null);
      
      PersistentSessionTestUtil.configureManager(mgr, 1, true, 3, 1);
      
      mgr.start();
      
      assertTrue("Passivation is enabled", mgr.isPassivationEnabled());
      assertEquals("Correct max active count", 1, mgr.getMaxActiveAllowed());
      assertEquals("Correct max idle time", 3, mgr.getPassivationMaxIdleTime());
      assertEquals("Correct min idle time", 1, mgr.getPassivationMinIdleTime());
      
      // Set up a session
      Session sess1 = createAndUseSession(mgr, "1", true, true);
      
      assertEquals("Session count correct", 1, mgr.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr.getLocalActiveSessionCount());
      
      // Should fail to create a 2nd
      createAndUseSession(mgr, "2", false, false);
      
      // Confirm a session timeout clears space
      sess1.setMaxInactiveInterval(1);       
      SessionTestUtil.sleepThread(1100);      
      
      createAndUseSession(mgr, "2", true, false);      
      
      assertEquals("Session count correct", 1, mgr.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr.getLocalActiveSessionCount());     
      assertEquals("Created session count correct", 2, mgr.getCreatedSessionCount());
      assertEquals("Expired session count correct", 1, mgr.getExpiredSessionCount());

      //    Sleep past minIdleTime
      SessionTestUtil.sleepThread(1100);        
      
//      assertTrue("Session 2 still valid", sess2.isValid());
      assertEquals("Passivated session count correct", 0, mgr.getPassivatedSessionCount());
      
      createAndUseSession(mgr, "3", true, true);      
      
      assertEquals("Session count correct", 1, mgr.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 3, mgr.getCreatedSessionCount());
      assertEquals("Expired session count correct", 1, mgr.getExpiredSessionCount());
      assertEquals("Passivated session count correct", 1, mgr.getPassivatedSessionCount());
   }
   
   public void testReplicatedMaxSessions() throws Exception
   {
      log.info("Enter testReplicatedMaxSessions");
      
      ++testCount;
      DataSourcePersistentManager mgr0 = PersistentSessionTestUtil.createManager("test" + testCount, 1, null);      
      PersistentSessionTestUtil.configureManager(mgr0, 1);
      
      mgr0.start();
      
      assertFalse("Passivation is disabled", mgr0.isPassivationEnabled());
      assertEquals("Correct max active count", 1, mgr0.getMaxActiveAllowed());
      assertEquals("Correct max inactive interval", 1, mgr0.getMaxInactiveInterval());
      
      DataSourcePersistentManager mgr1 = PersistentSessionTestUtil.createManager("test" + testCount, 1, null);
      PersistentSessionTestUtil.configureManager(mgr1, 1);
      
      mgr1.start();
      
      assertFalse("Passivation is disabled", mgr1.isPassivationEnabled());
      assertEquals("Correct max active count", 1, mgr1.getMaxActiveAllowed());
      assertEquals("Correct max inactive interval", 1, mgr1.getMaxInactiveInterval());
      
      // Set up a session
      createAndUseSession(mgr0, "1", true, true);
      
      assertEquals("Session count correct", 1, mgr0.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr0.getLocalActiveSessionCount());      
      assertEquals("Session count correct", 0, mgr1.getActiveSessionCount());
      assertEquals("Local session count correct", 0, mgr1.getLocalActiveSessionCount());
      
      // Get it in-memory on mgr1 as well 
      Session sess1 = useSession(mgr1, "1");
      
      // A 2nd session should fail
      createAndUseSession(mgr0, "2", false, false);
      createAndUseSession(mgr1, "2", false, false);
      
      // Confirm a session timeout clears space
      sess1.setMaxInactiveInterval(1);      
      useSession(mgr1, "1");   
      useSession(mgr0, "1"); 
      SessionTestUtil.sleepThread(mgr0.getMaxInactiveInterval() * 1000 + 100);      
      
      createAndUseSession(mgr0, "2", true, true); 
      createAndUseSession(mgr1, "3", true, true);      
      
      assertEquals("Session count correct", 1, mgr0.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr0.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 2, mgr0.getCreatedSessionCount());
      assertEquals("Expired session count correct", 1, mgr0.getExpiredSessionCount());      
      
      assertEquals("Session count correct", 1, mgr1.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr1.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 1, mgr1.getCreatedSessionCount());
      assertEquals("Expired session count correct", 1, mgr1.getExpiredSessionCount());
   }
   
   public void testReplicatedMaxSessionsWithMaxIdle() throws Exception
   {
      log.info("Enter testReplicatedMaxSessionsWithMaxIdle");
      
      ++testCount;
      DataSourcePersistentManager mgr0 = PersistentSessionTestUtil.createManager("test" + testCount, 5, null);      
      PersistentSessionTestUtil.configureManager(mgr0, 1, true, 1, -1);
      
      mgr0.start();
      
      assertTrue("Passivation is enabled", mgr0.isPassivationEnabled());
      assertEquals("Correct max active count", 1, mgr0.getMaxActiveAllowed());
      assertEquals("Correct max idle time", 1, mgr0.getPassivationMaxIdleTime());
      assertEquals("Correct min idle time", -1, mgr0.getPassivationMinIdleTime());
      
      DataSourcePersistentManager mgr1 = PersistentSessionTestUtil.createManager("test" + testCount, 5, null);
      PersistentSessionTestUtil.configureManager(mgr1, 1, true, 1, -1);
      
      mgr1.start();
      
      assertTrue("Passivation is enabled", mgr1.isPassivationEnabled());
      assertEquals("Correct max active count", 1, mgr1.getMaxActiveAllowed());
      assertEquals("Correct max idle time", 1, mgr1.getPassivationMaxIdleTime());
      assertEquals("Correct min idle time", -1, mgr1.getPassivationMinIdleTime());
      
      // Set up a session
      createAndUseSession(mgr0, "1", true, true);
      
      assertEquals("Session count correct", 1, mgr0.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr0.getLocalActiveSessionCount());
      assertEquals("Passivated session count correct", 0, mgr0.getPassivatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr0.getExpiredSessionCount());       
      assertEquals("Session count correct", 0, mgr1.getActiveSessionCount());
      assertEquals("Local session count correct", 0, mgr1.getLocalActiveSessionCount());
      assertEquals("Passivated session count correct", 0, mgr1.getPassivatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr1.getExpiredSessionCount()); 
      
      // Get it in-memory on mgr1 as well 
      useSession(mgr1, "1");
      
      // A 2nd session should fail
      createAndUseSession(mgr0, "2", false, false);
      createAndUseSession(mgr1, "2", false, false);
      
      //    Sleep past maxIdleTime      
      SessionTestUtil.sleepThread(1100);        
      
      assertEquals("Passivated session count correct", 0, mgr1.getPassivatedSessionCount());
       
      createAndUseSession(mgr1, "2", true, true); 
      createAndUseSession(mgr0, "3", true, true); 
       
      assertEquals("Session count correct", 1, mgr0.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr0.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 2, mgr0.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr0.getExpiredSessionCount());  
      assertEquals("Passivated session count correct", 1, mgr0.getPassivatedSessionCount());    
       
      assertEquals("Session count correct", 1, mgr1.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr1.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 1, mgr1.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr1.getExpiredSessionCount()); 
      assertEquals("Passivated session count correct", 1, mgr1.getPassivatedSessionCount());     
   }
   
   public void testReplicatedMaxSessionsWithMinIdle() throws Exception
   {
      log.info("Enter testReplicatedMaxSessionsWithMinIdle");
      
      ++testCount;
      DataSourcePersistentManager mgr0 = PersistentSessionTestUtil.createManager("test" + testCount, 5, null);
      PersistentSessionTestUtil.configureManager(mgr0, 1, true, 3, 1);
      
      mgr0.start();
      
      assertTrue("Passivation is enabled", mgr0.isPassivationEnabled());
      assertEquals("Correct max active count", 1, mgr0.getMaxActiveAllowed());
      assertEquals("Correct max idle time", 3, mgr0.getPassivationMaxIdleTime());
      assertEquals("Correct min idle time", 1, mgr0.getPassivationMinIdleTime());
      
      DataSourcePersistentManager mgr1 = PersistentSessionTestUtil.createManager("test" + testCount, 5, null);
      PersistentSessionTestUtil.configureManager(mgr1, 1, true, 3, 1);
      
      mgr1.start();
      
      assertTrue("Passivation is enabled", mgr1.isPassivationEnabled());
      assertEquals("Correct max active count", 1, mgr1.getMaxActiveAllowed());
      assertEquals("Correct max idle time", 3, mgr1.getPassivationMaxIdleTime());
      assertEquals("Correct min idle time", 1, mgr1.getPassivationMinIdleTime());
      
      // Set up a session
      createAndUseSession(mgr0, "1", true, true);
      
      assertEquals("Session count correct", 1, mgr0.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr0.getLocalActiveSessionCount());
      assertEquals("Passivated session count correct", 0, mgr0.getPassivatedSessionCount());      
      assertEquals("Session count correct", 0, mgr1.getActiveSessionCount());
      assertEquals("Local session count correct", 0, mgr1.getLocalActiveSessionCount());
      assertEquals("Passivated session count correct", 0, mgr0.getPassivatedSessionCount());
      
      // Get it in-memory on mgr1 as well 
      useSession(mgr1, "1");
      
      // A 2nd session should fail
      createAndUseSession(mgr0, "2", false, false);
      createAndUseSession(mgr1, "2", false, false);
      
      // Sleep past minIdleTime      
      SessionTestUtil.sleepThread(1100);        
      
      assertEquals("Passivated session count correct", 0, mgr1.getPassivatedSessionCount());
       
      createAndUseSession(mgr1, "2", true, true);   
      createAndUseSession(mgr0, "3", true, true);    
       
      assertEquals("Session count correct", 1, mgr0.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr0.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 2, mgr0.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr0.getExpiredSessionCount());  
      assertEquals("Passivated session count correct", 1, mgr0.getPassivatedSessionCount());    
       
      assertEquals("Session count correct", 1, mgr1.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr1.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 1, mgr1.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr1.getExpiredSessionCount()); 
      assertEquals("Passivated session count correct", 1, mgr1.getPassivatedSessionCount());     
      
   }
   
   public void testStandaloneRedeploy() throws Exception
   {
      log.info("Enter testStandaloneRedeploy");
      ++testCount;
      DataSourcePersistentManager mgr = PersistentSessionTestUtil.createManager("test" + testCount, 300, null);      
      PersistentSessionTestUtil.configureManager(mgr, 2, true, 3, 1);
      
      mgr.start();
      
      assertTrue("Passivation is enabled", mgr.isPassivationEnabled());
      assertEquals("Correct max active count", 2, mgr.getMaxActiveAllowed());
      assertEquals("Correct max idle time", 3, mgr.getPassivationMaxIdleTime());
      assertEquals("Correct min idle time", 1, mgr.getPassivationMinIdleTime());
      
      // Set up a session
      createAndUseSession(mgr, "1", true, true);
      
      assertEquals("Session count correct", 1, mgr.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr.getLocalActiveSessionCount());
      
      // And a 2nd
      createAndUseSession(mgr, "2", true, true);     
      
      assertEquals("Session count correct", 2, mgr.getActiveSessionCount());
      assertEquals("Local session count correct", 2, mgr.getLocalActiveSessionCount());     
      assertEquals("Created session count correct", 2, mgr.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr.getExpiredSessionCount());

      //    Sleep past minIdleTime
      SessionTestUtil.sleepThread(1100);
      
      assertEquals("Passivated session count correct", 0, mgr.getPassivatedSessionCount());
      
      createAndUseSession(mgr, "3", true, true);      
      
      assertEquals("Session count correct", 2, mgr.getActiveSessionCount());
      assertEquals("Local session count correct", 2, mgr.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 3, mgr.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr.getExpiredSessionCount());
      assertEquals("Passivated session count correct", 1, mgr.getPassivatedSessionCount());
      
      mgr.stop();
      
      mgr = PersistentSessionTestUtil.createManager("test" + testCount, 5, null);      
      PersistentSessionTestUtil.configureManager(mgr, 2, true, 3, 1);
      
      mgr.start();
      
      assertTrue("Passivation is enabled", mgr.isPassivationEnabled());
      assertEquals("Correct max active count", 2, mgr.getMaxActiveAllowed());
      assertEquals("Correct max idle time", 3, mgr.getPassivationMaxIdleTime());
      assertEquals("Correct min idle time", 1, mgr.getPassivationMinIdleTime());     
      
      assertEquals("Session count correct", 0, mgr.getActiveSessionCount());
      assertEquals("Local session count correct", 0, mgr.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 0, mgr.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr.getExpiredSessionCount());
      assertEquals("Passivated session count correct", 0, mgr.getPassivatedSessionCount());
      
      // Sleep past minIdleTime
      SessionTestUtil.sleepThread(1100);
      
      createAndUseSession(mgr, "4", true, true); 
   }
   
   public void testReplicatedRedeploy() throws Exception
   {
      log.info("Enter testReplicatedRedeploy");
      
      replicatedWarRedeployTest(false);
   }
   
   public void testReplicatedRestart() throws Exception
   {
      log.info("Enter testReplicatedRestart");
      
      replicatedWarRedeployTest(true);
      
   }
   
   private void replicatedWarRedeployTest(boolean fullRestart)
         throws Exception
   {
      ++testCount;
      DataSourcePersistentManager mgr0 = PersistentSessionTestUtil.createManager("test" + testCount, 300, null);
      PersistentSessionTestUtil.configureManager(mgr0, 1, true, 30, 1);
      
      mgr0.start();
      
      assertTrue("Passivation is enabled", mgr0.isPassivationEnabled());
      assertEquals("Correct max active count", 1, mgr0.getMaxActiveAllowed());
      assertEquals("Correct max idle time", 30, mgr0.getPassivationMaxIdleTime());
      assertEquals("Correct min idle time", 1, mgr0.getPassivationMinIdleTime());
      
      DataSourcePersistentManager mgr1 = PersistentSessionTestUtil.createManager("test" + testCount, 300, null);
      PersistentSessionTestUtil.configureManager(mgr1, 1, true, 30, 1);
      
      mgr1.start();
      
      assertTrue("Passivation is enabled", mgr1.isPassivationEnabled());
      assertEquals("Correct max active count", 1, mgr1.getMaxActiveAllowed());
      assertEquals("Correct max idle time", 30, mgr1.getPassivationMaxIdleTime());
      assertEquals("Correct min idle time", 1, mgr1.getPassivationMinIdleTime());
      
      // Set up a session
      createAndUseSession(mgr0, "1", true, true);
      
      assertEquals("Session count correct", 1, mgr0.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr0.getLocalActiveSessionCount());    
      assertEquals("Session count correct", 0, mgr1.getActiveSessionCount());
      assertEquals("Local session count correct", 0, mgr1.getLocalActiveSessionCount());
      
      // Create a 2nd
      createAndUseSession(mgr1, "2", true, true);     
      
      assertEquals("Session count correct", 1, mgr0.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr0.getLocalActiveSessionCount());     
      assertEquals("Created session count correct", 1, mgr0.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr0.getExpiredSessionCount());
      assertEquals("Session count correct", 1, mgr1.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr1.getLocalActiveSessionCount());     
      assertEquals("Created session count correct", 1, mgr1.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr1.getExpiredSessionCount());

      //    Sleep past minIdleTime
      SessionTestUtil.sleepThread(1100);
      
      assertEquals("Passivated session count correct", 0, mgr1.getPassivatedSessionCount());
      
      createAndUseSession(mgr1, "3", true, true);      
      
      assertEquals("Session count correct", 1, mgr0.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr0.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 1, mgr0.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr0.getExpiredSessionCount());
      assertEquals("Passivated session count correct", 0, mgr0.getPassivatedSessionCount());
      // mgr1 only has 1 active since it passivated one when it created #3 
      assertEquals("Session count correct", 1, mgr1.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr1.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 2, mgr1.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr1.getExpiredSessionCount());
      assertEquals("Passivated session count correct", 1, mgr1.getPassivatedSessionCount());
      
      if (fullRestart)
      {
        mgr1.stop();
      }
      
      mgr0.stop();
      
      mgr0 = PersistentSessionTestUtil.createManager("test" + testCount, 300, null);
      PersistentSessionTestUtil.configureManager(mgr0, 1, true, 30, 1);
      
      mgr0.start();
      
      assertTrue("Passivation is enabled", mgr0.isPassivationEnabled());
      assertEquals("Correct max active count", 1, mgr0.getMaxActiveAllowed());
      assertEquals("Correct max idle time", 30, mgr0.getPassivationMaxIdleTime());
      assertEquals("Correct min idle time", 1, mgr0.getPassivationMinIdleTime());     
      
//      int expected = (totalReplication && marshalling && fullRestart) ? 0 : 2;
      int expected = 2;
      assertEquals("Session count correct", 0, mgr0.getActiveSessionCount());
      assertEquals("Local session count correct", 0, mgr0.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 0, mgr0.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr0.getExpiredSessionCount());
//      expected = (totalReplication && !(marshalling && fullRestart)) ? 1 : 0;
      expected = 1;
      assertEquals("Passivated session count correct", 0, mgr0.getPassivatedSessionCount());
      
      if (!fullRestart)
      {
         assertEquals("Session count correct", 1, mgr1.getActiveSessionCount());
         assertEquals("Local session count correct", 1, mgr1.getLocalActiveSessionCount());
         assertEquals("Created session count correct", 2, mgr1.getCreatedSessionCount());
         assertEquals("Expired session count correct", 0, mgr1.getExpiredSessionCount());
         assertEquals("Passivated session count correct", 1, mgr1.getPassivatedSessionCount());
      }
      // Sleep past minIdleTime
      SessionTestUtil.sleepThread(1100);
      
      createAndUseSession(mgr0, "4", true, true); 
   }
   
   public void testNoPassivationRedeploy() throws Exception
   {
      log.info("Enter testoPassivationRedeploy");
      
      noPassivationRedeployTest(false);
   }
   
   public void testoPassivationRestart() throws Exception
   {
      log.info("Enter testoPassivationRestart");
      
      noPassivationRedeployTest(true);      
   }
   
   private void noPassivationRedeployTest(boolean fullRestart)
         throws Exception
   {
      ++testCount;
      DataSourcePersistentManager mgr0 = PersistentSessionTestUtil.createManager("test" + testCount, 300, null);
      PersistentSessionTestUtil.configureManager(mgr0, 1);
      
      mgr0.start();
      
      assertFalse("Passivation is disabled", mgr0.isPassivationEnabled());
      assertEquals("Correct max active count", 1, mgr0.getMaxActiveAllowed());
      assertEquals("Correct max idle time", -1, mgr0.getPassivationMaxIdleTime());
      assertEquals("Correct min idle time", -1, mgr0.getPassivationMinIdleTime());
      
      DataSourcePersistentManager mgr1 = PersistentSessionTestUtil.createManager("test" + testCount, 300, null);
      PersistentSessionTestUtil.configureManager(mgr1, 1);
      
      mgr1.start();
      
      assertFalse("Passivation is disabled", mgr1.isPassivationEnabled());
      assertEquals("Correct max active count", 1, mgr1.getMaxActiveAllowed());
      assertEquals("Correct max idle time", -1, mgr1.getPassivationMaxIdleTime());
      assertEquals("Correct min idle time", -1, mgr1.getPassivationMinIdleTime());
      
      // Set up a session
      createAndUseSession(mgr0, "1", true, true);
      
      assertEquals("Session count correct", 1, mgr0.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr0.getLocalActiveSessionCount());    
      assertEquals("Session count correct", 0, mgr1.getActiveSessionCount());
      assertEquals("Local session count correct", 0, mgr1.getLocalActiveSessionCount());
      
      // Create a 2nd
      createAndUseSession(mgr1, "2", true, true);     
      
      assertEquals("Session count correct", 1, mgr0.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr0.getLocalActiveSessionCount());     
      assertEquals("Created session count correct", 1, mgr0.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr0.getExpiredSessionCount());
      assertEquals("Session count correct", 1, mgr1.getActiveSessionCount());
      assertEquals("Local session count correct", 1, mgr1.getLocalActiveSessionCount());     
      assertEquals("Created session count correct", 1, mgr1.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr1.getExpiredSessionCount());
      
      if (fullRestart)
      {
        mgr1.stop();
      }
      
      mgr0.stop();
      
      mgr0 = PersistentSessionTestUtil.createManager("test" + testCount, 300, null);
      PersistentSessionTestUtil.configureManager(mgr0, 1);
      
      mgr0.start();
      
      assertFalse("Passivation is disabled", mgr0.isPassivationEnabled());
      assertEquals("Correct max active count", 1, mgr0.getMaxActiveAllowed());
      assertEquals("Correct max idle time", -1, mgr0.getPassivationMaxIdleTime());
      assertEquals("Correct min idle time", -1, mgr0.getPassivationMinIdleTime());   
      
//      int expected = (totalReplication && marshalling && fullRestart) ? 0 : 2;
      int expected = 2;
      assertEquals("Session count correct", 0, mgr0.getActiveSessionCount());
      assertEquals("Local session count correct", 0, mgr0.getLocalActiveSessionCount());
      assertEquals("Created session count correct", 0, mgr0.getCreatedSessionCount());
      assertEquals("Expired session count correct", 0, mgr0.getExpiredSessionCount());
//      expected = (totalReplication && !(marshalling && fullRestart)) ? 1 : 0;
      expected = 1;
      assertEquals("Passivated session count correct", 0, mgr0.getPassivatedSessionCount());
      
      if (!fullRestart)
      {
         assertEquals("Session count correct", 1, mgr1.getActiveSessionCount());
         assertEquals("Local session count correct", 1, mgr1.getLocalActiveSessionCount());
         assertEquals("Created session count correct", 1, mgr1.getCreatedSessionCount());
         assertEquals("Expired session count correct", 0, mgr1.getExpiredSessionCount());
         assertEquals("Passivated session count correct", 0, mgr1.getPassivatedSessionCount());
      }
      // Sleep past minIdleTime
      SessionTestUtil.sleepThread(1100);
      
      createAndUseSession(mgr0, "4", true, true); 
   }
   
   private Session createAndUseSession(DataSourcePersistentManager dspm, String id, 
                           boolean canCreate, boolean access)
         throws Exception
   {
      //    Shift to Manager interface when we simulate Tomcat
      Manager mgr = dspm;
      Session sess = mgr.findSession(id);
      assertNull("session does not exist", sess);
      try
      {
         sess = mgr.createSession(id);
         if (!canCreate)
            fail("Could not create session" + id);
      }
      catch (IllegalStateException ise)
      {
         if (canCreate)
         {
            log.error("Failed to create session " + id, ise);
            fail("Could create session " + id);
         }
      }
      
      if (access)
      {
         sess.access();
         sess.getSession().setAttribute("test", "test");
         
         dspm.storeSession(sess);
         
         sess.endAccess();
      }
      
      return sess;
   }
   
   private Session useSession(DataSourcePersistentManager dspm, String id)
         throws Exception
   {
      //    Shift to Manager interface when we simulate Tomcat
      Manager mgr = dspm;
      Session sess = mgr.findSession(id);
      assertNotNull("session exists", sess);
      
      sess.access();
      sess.getSession().setAttribute("test", "test");
      
      dspm.storeSession(sess);
      
      sess.endAccess();
      
      return sess;
   }
}
