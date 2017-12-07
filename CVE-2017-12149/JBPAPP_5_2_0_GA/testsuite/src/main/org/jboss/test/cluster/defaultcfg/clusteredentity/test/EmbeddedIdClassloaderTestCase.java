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
package org.jboss.test.cluster.defaultcfg.clusteredentity.test;

import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.clusteredentity.embeddedid.EmbeddedIdTest;
import org.jboss.test.cluster.clusteredentity.embeddedid.MusicianPK;
import org.jboss.test.cluster.testutil.DBSetup;

/**
 * Simple test of replication of entities and related queries with @EmbeddedId 
 * fields involved.
 *
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1.1 $
 */
public class EmbeddedIdClassloaderTestCase
extends JBossClusteredTestCase
{
   private org.jboss.logging.Logger log = getLog();

   protected static final long SLEEP_TIME = 300L;

   private EmbeddedIdTest sfsb0;
   private EmbeddedIdTest sfsb1;
   
   public EmbeddedIdClassloaderTestCase(String name)
   {
      super(name);
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      
      sfsb0 = getUserTest(System.getProperty("jbosstest.cluster.node0"));
      sfsb1 = getUserTest(System.getProperty("jbosstest.cluster.node1"));  
      sfsb0.cleanup();
      sfsb1.cleanup();
   }
    
   protected void tearDown() throws Exception
   {
      if (sfsb0 != null)
      {
         try
         {
            sfsb0.remove();
         }
         catch (Exception e) {}
      }
      if (sfsb1 != null)
      {
         try
         {
            sfsb1.remove();
         }
         catch (Exception e) {}
      }
      
      sfsb0 = sfsb1 = null;
   }
   
   protected EmbeddedIdTest getUserTest(String nodeJNDIAddress) throws Exception
   {
      Properties prop1 = new Properties();
      prop1.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
      prop1.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      prop1.put("java.naming.provider.url", "jnp://" + nodeJNDIAddress + ":1099");
   
      log.info("===== Naming properties for " + nodeJNDIAddress + ": ");
      log.info(prop1);
      log.info("Create InitialContext for " + nodeJNDIAddress);
      InitialContext ctx1 = new InitialContext(prop1);
   
      log.info("Lookup sfsb from " + nodeJNDIAddress);
      return (EmbeddedIdTest) ctx1.lookup("EmbeddedIdTestBean/remote");
      
   }
   
   protected String getEarName()
   {
      return "clusteredentity-embeddedid-test";
   }
   
   public void testQuery() throws Exception
   {
      try
      {
         sfsb0.createMusician(EmbeddedIdTest.DEFAULT_PK, "zither");
         
         queryByInstrument(sfsb0, "zither", false);
         queryByInstrument(sfsb0, "zither", true);
         
         // pause to let queries replicate async
         sleep(SLEEP_TIME);
         
         queryByInstrument(sfsb1, "zither", false);
         queryByInstrument(sfsb1, "zither", true);
      }
      finally
      {
         // cleanup the db so we can run this test multiple times w/o restarting the cluster
         sfsb0.cleanup();
      }
   }
   
   private void queryByInstrument(EmbeddedIdTest sfsb, String instrument, boolean useNamedRegion)
   {
      List<MusicianPK> pks = sfsb.getMusiciansForInstrument(instrument, useNamedRegion);
      assertNotNull("Got pks", pks);
      assertEquals("Got one pk", 1, pks.size());
      assertEquals("Got correct pks", EmbeddedIdTest.DEFAULT_PK, pks.get(0));
   }
   
   protected void sleep(long millis)
   {
      try
      {
         Thread.sleep(millis);
      }
      catch (InterruptedException e)
      {
         log.warn("Interrupted while sleeping", e);
      }
   }

   public static Test suite() throws Exception
   {
      return DBSetup.getDeploySetup(EmbeddedIdClassloaderTestCase.class, 
                               "clusteredentity-embeddedid-test.ear");
   }
}
