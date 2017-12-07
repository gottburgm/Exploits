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

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.jboss.cache.Fqn;
import org.jboss.cache.pojo.PojoCache;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.web.tomcat.service.session.JBossCacheManager;

/**
 * Tests that a stopped session manager reacts correctly to Manager API calls.
 * Used to validate that races during undeploy will not lead to errors.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class StoppedManagerUnitTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(StoppedManagerUnitTestCase.class);
   
   private static long testCount = System.currentTimeMillis();
   
   private Set<PojoCache> caches = new HashSet<PojoCache>();
   
   /**
    * Create a new SessionCountUnitTestCase.
    * 
    * @param name
    */
   public StoppedManagerUnitTestCase(String name)
   {
      super(name);
   } 

   @Override
   protected void tearDown() throws Exception
   {
      try
      {
         super.tearDown();
      }
      finally
      {      
         SessionTestUtil.clearDistributedCacheManagerFactory();
         
         for (PojoCache cache : caches)
         { 
            // Try to clean up so we avoid loading sessions 
            // from storage in later tests
            try
            {
               log.info("Removing /JSESSION from " + cache.getCache().getLocalAddress());
               cache.getCache().removeNode(Fqn.fromString("/JSESSION"));
            }
            catch (Exception e)
            {
               log.error("Cache " + cache.getCache().getLocalAddress() + ": " + e.getMessage(), e);
            }
            
            try
            {
               cache.stop();
               cache.destroy();
            }
            catch (Exception e)
            {
               log.error("Cache " + cache.getCache().getLocalAddress() + ": " + e.getMessage(), e);
            }
            
         }
         
         caches.clear();
      }
   }

   public void testStoppedManager() throws Exception
   {
      log.info("Enter testStoppedManager");
      
      ++testCount;
      
      JBossCacheManager jbcm = SessionTestUtil.createManager("test" + testCount, 30, true, null, false, false, null, caches);
       
      JBossWebMetaData webMetaData = SessionTestUtil.createWebMetaData(100);
      jbcm.init("test.war", webMetaData);
      
      jbcm.start();
      
      // Set up a session
      Session sess1 = createAndUseSession(jbcm, "1", true, true);      
      Session sess2 = createAndUseSession(jbcm, "2", true, true);
      
      // Sanity check
      Session[] sessions = jbcm.findSessions();
      assertNotNull(sessions);
      assertEquals(2, sessions.length);
      
      jbcm.stop();
      
      assertNull(jbcm.findSession("1"));
      assertNull(jbcm.findSession("2"));
      assertNull(jbcm.findSessions());
      assertNull(jbcm.createEmptySession());
      assertNull(jbcm.createSession());
      assertNull(jbcm.createSession("3"));
      
      assertFalse(sess1.isValid());
      assertFalse(sess2.isValid());
      jbcm.add(sess1); // shouldn't blow up
      assertFalse(sess1.isValid());
      
      jbcm.remove(sess2);
   }
   
   private Session createAndUseSession(JBossCacheManager jbcm, String id, 
                           boolean canCreate, boolean access)
         throws Exception
   {
      //    Shift to Manager interface when we simulate Tomcat
      Manager mgr = jbcm;
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
         
         jbcm.storeSession(sess);
         
         sess.endAccess();
      }
      
      return sess;
   }
}
