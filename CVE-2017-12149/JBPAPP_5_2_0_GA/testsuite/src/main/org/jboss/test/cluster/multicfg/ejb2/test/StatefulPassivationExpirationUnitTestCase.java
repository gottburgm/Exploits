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
package org.jboss.test.cluster.multicfg.ejb2.test;


import java.util.Date;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.cluster.ejb2.basic.interfaces.NodeAnswer;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatefulSession;
import org.jboss.test.testbean.interfaces.StatefulSessionHome;

import org.jboss.test.JBossClusteredTestCase;

/**
 * Test passivation and expiration of clustered SLSBs.
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author Scott.Stark@jboss.org
 * @author Brian Stansberry
 * 
 * @version $Revision: 85945 $
 */
public class StatefulPassivationExpirationUnitTestCase extends JBossClusteredTestCase
{
   static boolean deployed = false;
   public static int test = 0;
   static Date startDate = new Date();
   
   protected final String namingFactory =
   System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
   
   protected final String providerURL =
   System.getProperty(Context.PROVIDER_URL);
   
   public StatefulPassivationExpirationUnitTestCase (String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return JBossClusteredTestCase.getDeploySetup(StatefulPassivationExpirationUnitTestCase.class, 
                                                   "sfsb-passexp.sar, sfsb-passexp.jar");
   }

   /**
    * The test creates 500 stateful session beans, executes some calls to
    * stress state replication, waits for passivation and exipration to kick
    * in, and then updates the sessions to produce the session removal
    * conflict seen in JBAS-1560. This is sensative to timing issues so a
    * failure in activation can show up; we catch any NoSuchObjectExceptio
    * to handle this.
    * 
    * @throws Exception
    */ 
   public void testStatefulPassivationExpiration()
      throws Exception
   {
      log.info("+++ testStatefulPassivationExpiration");

      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[0]);
      InitialContext ctx = new InitialContext(env1);

      int beanCount = 500;
      StatefulSessionHome home =
         (StatefulSessionHome) ctx.lookup("nextgen_ExpiredStatefulSession");
      long start = System.currentTimeMillis();
      log.info("Start bean creation");
      StatefulSession[] beans = new StatefulSession[beanCount];
      long[] accessStamp = new long[beanCount];
      for(int n = 0; n < beans.length; n ++)
      {
         beans[n] = (StatefulSession) home.create("testStatefulPassivationExpiration#"+n);
         accessStamp[n] = System.currentTimeMillis();
      }
      long end = System.currentTimeMillis();
      log.info("End bean creation, elapsed="+(end - start));

      int N = 5000;
      long min = 99999, max = 0, maxInactive = 0;
      for(int n = 0; n < N; n ++)
      {
         int id = n % beans.length;
         StatefulSession bean = beans[id];
         if (bean == null)
            continue;  // bean timed out and removed
         String name = "testStatefulPassiviationExpiration#"+id;
         long callStart = System.currentTimeMillis();
         long inactive = callStart - accessStamp[id];
         try
         {
            bean.setName(name);
            NodeAnswer node = bean.getNodeState();
            long now = System.currentTimeMillis();            
            long elapsed = now - callStart;
            accessStamp[id] = now;
            assertTrue("NodeAnswer == "+name, node.answer.equals(name));
            min = Math.min(min, elapsed);
            max = Math.max(max, elapsed);
            maxInactive = Math.max(maxInactive, inactive);
            log.debug(n+", elapsed="+elapsed+", inactive="+inactive);
         }
         catch (java.rmi.NoSuchObjectException nso)
         {
            log.debug(n+" Caught NoSuchObjectException on bean " + id + " -- inactive time = " + inactive);
            // Remove the bean as it will never succeed again
            beans[id] = null;
         }
      }
      log.info(N+" calls complete, max="+max+", min="+min+", maxInactive="+maxInactive);

      Thread.sleep(15000);
      start = System.currentTimeMillis();
      for(int n = 0; n < beans.length; n ++)
      {
         beans[n] = (StatefulSession) home.create("testStatefulPassivationExpiration#"+n);
         accessStamp[n] = System.currentTimeMillis();
      }
      end = System.currentTimeMillis();
      log.info("End second round bean creation, elapsed="+(end - start));
      for(int n = 0; n < N; n ++)
      {
         int id = n % beans.length;
         StatefulSession bean = beans[id];
         if (bean == null)
            continue;  // bean timed out and removed
         String name = "testStatefulPassiviationExpiration#"+id;
         long callStart = System.currentTimeMillis();
         long inactive = callStart - accessStamp[id];
         try
         {
            bean.setName(name);
            NodeAnswer node = bean.getNodeState();
            long now = System.currentTimeMillis();
            long elapsed = now - callStart;
            accessStamp[id] = now;
            assertTrue("NodeAnswer == "+name, node.answer.equals(name));
            min = Math.min(min, elapsed);
            max = Math.max(max, elapsed);
            maxInactive = Math.max(maxInactive, inactive);
            log.debug(n+", elapsed="+elapsed+", inactive="+inactive);
         }
         catch (java.rmi.NoSuchObjectException nso)
         {
            log.debug(n+" Caught NoSuchObjectException on bean " + id + " -- inactive time = " + (callStart - accessStamp[id]));
            // Remove the bean as it will never succeed again
            beans[id] = null;
         }
      }
      log.info(N+" calls complete, max="+max+", min="+min+", maxInactive="+maxInactive);
      // BES -- max call time check removed, as it can randomly fail if there is a long GC pause
      // assertTrue("max < 3000", max < 3000 );

      for(int n = 0; n < beans.length; n ++)
      {
         try
         {
            if (beans[n] != null)
               beans[n].remove();
         }
         catch (java.rmi.NoSuchObjectException nso)
         {
            log.debug("Caught NoSuchObjectException removing bean " + n);
         }
      }
      
      // Confirm that the bean cache is empty
      String oNameS = "jboss.j2ee:jndiName=ExpiredStatefulSession,plugin=cache,service=EJB";
      ObjectName oName = new ObjectName(oNameS);
      MBeanServerConnection[] adaptors = getAdaptors();      
      Long cacheCount = (Long) adaptors[0].getAttribute(oName, "CacheSize");
      assertEquals("CacheSize is zero", 0, cacheCount.longValue());
      // Checking the passivated count is invalid, as it doesn't get reduced
      // when remove() is called on a bean -- only when the passivation cleanup
      // thread runs
      //cacheCount = (Long) adaptors[0].getAttribute(oName, "PassivatedCount");
      //assertEquals("PassivatedCount is zero", 0, cacheCount.longValue());
   }
}
