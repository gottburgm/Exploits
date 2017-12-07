/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 * HASingletonControllerBeanUnitTestCase.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class HASingletonControllerBeanUnitTestCase extends JBossClusteredTestCase
{
   public static final String SINGLETON_CONTROLLER_ONAME = "jboss.ha:service=TestHASingletonControllerBean";
   
   public HASingletonControllerBeanUnitTestCase(String name)
   {
      super(name);      
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(HASingletonControllerBeanUnitTestCase.class, "ha-singleton-jboss-beans.xml");
   }

   public void testBeanDeployment() throws Exception
   {
      // JBAS-6279. Figure out who the master really is
      MBeanServerConnection[] adaptors = getAdaptors();
      boolean node0Master = isMaster(adaptors[0]);
      assertFalse(node0Master == isMaster(adaptors[1]));
      int masterIndex = node0Master ? 0 : 1;
      int nonMasterIndex = node0Master ? 1 : 0;
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");

      String[] namingUrls = getNamingURLs();
      
      String namingUrl = namingUrls[masterIndex];
      env.setProperty(Context.PROVIDER_URL, namingUrl);
      Context ctx = new InitialContext(env);
      HASingletonPojoExample pojo = (HASingletonPojoExample)ctx.lookup("test/cluster/hasingleton/simplepojo");
      
      assertTrue("Pojo in " + namingUrl + " should be deployed as HA singleton", pojo.isMasterNode());
      
      namingUrl = namingUrls[nonMasterIndex];
      env.setProperty(Context.PROVIDER_URL, namingUrl);
      ctx = new InitialContext(env);
      try
      {
         pojo = (HASingletonPojoExample)ctx.lookup("test/cluster/hasingleton/simplepojo");
         fail("Should have thrown a NamingException indicating 'test not bound'");
      }
      catch(NamingException ne)
      {
      }      
   }
   
   private boolean isMaster(MBeanServerConnection server)
   {
      try
      {
         ObjectName oname = new ObjectName(SINGLETON_CONTROLLER_ONAME);
         return ((Boolean) server.invoke(oname, "isMasterNode", new Object[]{}, new String[]{})).booleanValue();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
}
