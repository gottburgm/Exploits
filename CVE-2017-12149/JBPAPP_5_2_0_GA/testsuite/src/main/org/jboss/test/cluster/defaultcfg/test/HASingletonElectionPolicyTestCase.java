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
package org.jboss.test.cluster.defaultcfg.test;

import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Test;

import org.jboss.ha.singleton.examples.HASingletonPojoExample;
import org.jboss.test.JBossClusteredTestCase;

/**
 * Unit tests for HASingletonElectionPolicy.
 * The testing deployment is under resources/ha/electionpolicy. Tests both
 * MBean and Pojo based HA singletons. 
 *
 * @author <a href="mailto:Alex.Fu@novell.com">Alex Fu</a>
 * @author Brian Stansberry
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 85945 $
 *
 */
public class HASingletonElectionPolicyTestCase extends JBossClusteredTestCase 
{
   private final Properties env = new Properties();
   
   public HASingletonElectionPolicyTestCase(String name)
   {
      super(name);
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
   }
   
   public static Test suite() throws Exception
   {
      // Refer to jboss-service.xml under resources/ha/electionpolicy
      return getDeploySetup(HASingletonElectionPolicyTestCase.class, "ha-electionpolicy-jboss-beans.xml");
   }
   
   public void testMBeanElectionPolicy() throws Exception
   {
      // Get MBeanServerConnections
      MBeanServerConnection[] adaptors = this.getAdaptors();
      int size = adaptors.length;
      assertTrue(size == 2);   // cluster size must be 2 for 3rd policy test
      
      // First policy is to elect the oldest node (position = 0)
      {
         ObjectName mbean = new ObjectName("jboss.examples:service=HASingletonMBeanExample_1");
         
         Boolean n1 = (Boolean)adaptors[0].getAttribute(mbean, "MasterNode");
         Boolean n2 = (Boolean)adaptors[size - 1].getAttribute(mbean, "MasterNode");
         
         assertEquals(Boolean.TRUE, n1);
         assertEquals(Boolean.FALSE, n2);
      }
      
      // Second policy is the youngest (position = -1)
      {
         ObjectName mbean = new ObjectName("jboss.examples:service=HASingletonMBeanExample_2");
         
         Boolean n1 = (Boolean)adaptors[0].getAttribute(mbean, "MasterNode");
         Boolean n2 = (Boolean)adaptors[size - 1].getAttribute(mbean, "MasterNode");
         
         assertEquals(Boolean.FALSE, n1);
         assertEquals(Boolean.TRUE, n2);
      }
      
      // 3rd policy is the 2nd oldest (position = 1)
      {
         ObjectName mbean = new ObjectName("jboss.examples:service=HASingletonMBeanExample_3");
         
         Boolean n1 = (Boolean)adaptors[0].getAttribute(mbean, "MasterNode");
         Boolean n2 = (Boolean)adaptors[1].getAttribute(mbean, "MasterNode");
         
         assertEquals(Boolean.FALSE, n1);
         assertEquals(Boolean.TRUE, n2);
      }
      
      // 4th policy is not set, default is oldest
      {
         ObjectName mbean = new ObjectName("jboss.examples:service=HASingletonMBeanExample_4");
         
         Boolean n1 = (Boolean)adaptors[0].getAttribute(mbean, "MasterNode");
         Boolean n2 = (Boolean)adaptors[size - 1].getAttribute(mbean, "MasterNode");
         
         assertEquals(Boolean.TRUE, n1);
         assertEquals(Boolean.FALSE, n2);
      }
      
      // 5th policy is the oldest (position = 0), preferredMaster is set to 127.0.0.1:1099
      // 6th policy is the youngest (position = -1), preferredMaster is set to 127.0.0.1:1099
      // So the master node of example 5 and 6 should be the same - the preferred master
      {
          ObjectName mbean5 = new ObjectName("jboss.examples:service=HASingletonMBeanExample_5");
          ObjectName mbean6 = new ObjectName("jboss.examples:service=HASingletonMBeanExample_6");

          Boolean n51 = (Boolean)adaptors[0].getAttribute(mbean5, "MasterNode");
          Boolean n61 = (Boolean)adaptors[0].getAttribute(mbean6, "MasterNode");
          Boolean n52 = (Boolean)adaptors[size - 1].getAttribute(mbean5, "MasterNode");
          Boolean n62 = (Boolean)adaptors[size - 1].getAttribute(mbean6, "MasterNode");
          
          assertEquals(n51, n61);
          assertEquals(n52, n62);
       }
       
      return;
   }
   
   public void testPojoElectionPolicy() throws Exception
   {
      String[] namingUrls = getNamingURLs();
      int size = namingUrls.length;
      assertTrue(size == 2);   // cluster size must be 2 for 3rd policy test
      
      // First policy is to elect the oldest node (position = 0)
      int exampleNumber = 1;
      shouldHaveHaSingletonDeployed(namingUrls[0], exampleNumber);
      shouldNotHaveHaSingletonDeployed(namingUrls[size - 1], exampleNumber);
      
      // Second policy is the youngest (position = -1)
      exampleNumber = 2;
      shouldNotHaveHaSingletonDeployed(namingUrls[0], exampleNumber);
      shouldHaveHaSingletonDeployed(namingUrls[size - 1], exampleNumber);
      
      // 3rd policy is the 2nd oldest (position = 1)
      exampleNumber = 3;
      shouldNotHaveHaSingletonDeployed(namingUrls[0], exampleNumber);
      shouldHaveHaSingletonDeployed(namingUrls[1], exampleNumber);

      // 4th policy is not set, default is oldest
      exampleNumber = 4;
      shouldHaveHaSingletonDeployed(namingUrls[0], exampleNumber);
      shouldNotHaveHaSingletonDeployed(namingUrls[size - 1], exampleNumber);
      
      // 5th policy is the oldest (position = 0)
      // however, preferred master is set to 127.0.0.1:1099 (which is the 1st node)
      exampleNumber = 5;
      shouldHaveHaSingletonDeployed(namingUrls[0], exampleNumber);
      shouldNotHaveHaSingletonDeployed(namingUrls[size - 1], exampleNumber);      
      
      // 6th policy is the youngest (position = -1)
      // however, preferred master is set to 127.0.0.1:1099 (which is the 1st node)
      exampleNumber = 6;
      shouldHaveHaSingletonDeployed(namingUrls[0], exampleNumber);
      shouldNotHaveHaSingletonDeployed(namingUrls[size - 1], exampleNumber);
   }
   
   private void shouldHaveHaSingletonDeployed(String namingUrl, int exampleNumber) throws Exception
   {
      env.setProperty(Context.PROVIDER_URL, namingUrl);
      Context ctx = new InitialContext(env);
      HASingletonPojoExample pojo = (HASingletonPojoExample)ctx.lookup("test/cluster/hasingleton/simplepojo/" + exampleNumber);      
      assertTrue("Pojo in " + namingUrl + " should be deployed as HA singleton", pojo.isMasterNode());      
   }
   
   private void shouldNotHaveHaSingletonDeployed(String namingUrl, int exampleNumber) throws Exception
   {
      env.setProperty(Context.PROVIDER_URL, namingUrl);
      Context ctx = new InitialContext(env);
      try
      {
         HASingletonPojoExample pojo = (HASingletonPojoExample)ctx.lookup("test/cluster/hasingleton/simplepojo/" + exampleNumber);
         fail("Should have thrown a NamingException indicating 'test not bound'");
      }
      catch(NamingException ne)
      {
      }      
   }
}
