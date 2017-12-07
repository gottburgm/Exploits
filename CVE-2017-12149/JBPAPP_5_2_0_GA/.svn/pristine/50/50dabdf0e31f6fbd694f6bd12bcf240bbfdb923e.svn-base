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

import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.ha.framework.interfaces.ResponseFilter;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.rspfilter.TruthfulResponseFilter;

/**
 * TruthfulResponseFilterUnitTestCase.
 * 
 * @author Galder Zamarreño
 */
public class TruthfulResponseFilterUnitTestCase extends JBossClusteredTestCase
{
   public TruthfulResponseFilterUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(TruthfulResponseFilterUnitTestCase.class, "rspfilter.sar");
   }
   
   public void testTrueEcho() throws Exception
   {
      MBeanServerConnection[] adaptors = this.getAdaptors();
      ObjectName on = new ObjectName("cluster.rspfilter:service=Echo");
      ResponseFilter filter = new TruthfulResponseFilter();
      Object[] args = new Object[] {true, false, filter};
      String[] signature = new String[] {boolean.class.getName(), boolean.class.getName(),ResponseFilter.class.getName()};
      List resps = (List) adaptors[0].invoke(on, "callEchoOnCluster", args, signature);
      log.debug("Response list: " + resps);
      assertEquals(1, resps.size());
      
      if (resps.get(0) instanceof Exception) 
      {
         throw (Exception)resps.get(0);
      }
      
      assertTrue(((Boolean)resps.get(0)).booleanValue());
   }

//   Commented from the time being since it results of call timing out since there'd be no responses
//   and this will force 1 minute wait. testTrueEcho() is enough to give us an indication of 
//   response filter being in action.
//   
//   public void testFalseEcho() throws Exception
//   {
//      MBeanServerConnection[] adaptors = this.getAdaptors();
//      ObjectName on = new ObjectName("cluster.rspfilter:service=Echo");
//      ResponseFilter filter = new TruthfulResponseFilter();
//      Object[] args = new Object[] {false, false, filter};
//      String[] signature = new String[] {boolean.class.getName(), boolean.class.getName(),ResponseFilter.class.getName()};
//      List resps = (List) adaptors[0].invoke(on, "callEchoOnCluster", args, signature);
//      log.debug("Response list: " + resps);
//      assertEquals(0, resps.size());
//   }
}
