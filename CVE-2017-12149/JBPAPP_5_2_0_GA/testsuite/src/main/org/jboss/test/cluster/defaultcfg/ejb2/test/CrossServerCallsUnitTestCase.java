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
package org.jboss.test.cluster.defaultcfg.ejb2.test;

import java.util.Properties;
import java.rmi.dgc.VMID;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.ejb2.crossserver.CalledHome;
import org.jboss.test.cluster.ejb2.crossserver.CalledRemote;

/**
 * Tests of inter-server ejb calls
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class CrossServerCallsUnitTestCase extends JBossClusteredTestCase
{
   public CrossServerCallsUnitTestCase (String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      Test t1 = JBossClusteredTestCase.getDeploySetup(CrossServerCallsUnitTestCase.class, "cross-server.jar");
      return t1;
   }

   public void testEjb2EjbCall() 
      throws Exception
   {       
      log.info("+++ testEjb2EjbCall");

      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[0]);
      InitialContext ctx = new InitialContext(env1);
      
      CalledHome home = (CalledHome) ctx.lookup("cluster.ejb.CalledHome");
      CalledRemote bean = home.create();
      VMID[] ids = bean.invokeCall(urls[1], "cluster.ejb.CalleeHome");
      log.info("VMID[0] = "+ids[0]);
      log.info("VMID[1] = "+ids[1]);
      assertTrue("VMID[0] != VMID[1]", ids[0].equals(ids[1]) == false);
      bean.remove();
      log.info("done");
   }

}
