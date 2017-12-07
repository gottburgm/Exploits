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

import junit.framework.TestCase;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.ResponseFilter;
import org.jboss.ha.framework.server.ClusterNodeImpl;
import org.jboss.ha.framework.server.ClusterPartition;
import org.jboss.ha.framework.server.RspFilterAdapter;
import org.jboss.ha.jndi.LookupSucceededFilter;
import org.jgroups.Address;
import org.jgroups.blocks.RspFilter;
import org.jgroups.stack.IpAddress;

/**
 * LookupSucceededFilterUnitTestCase.
 * 
 * @author Galder Zamarreño
 */
public class LookupSucceededFilterUnitTestCase extends TestCase
{
   public void testFilterLogic()
   {
      ResponseFilter filter = new LookupSucceededFilter();
      ClusterNode sender = new ClusterNodeImpl(new IpAddress(12345));
      exerciseFilterLogic(filter, sender);
   }

   public void testFilterLogicViaAdapter()
   {
      ResponseFilter filter = new LookupSucceededFilter();
      Address sender = new IpAddress(12345);
      RspFilterAdapter adapter = new RspFilterAdapter(filter);
      exerciseFilterLogic(adapter, sender);
  }
   
   /**
    * JBAS-7945. Test that receiving a non-acceptable response before
    * the needMoreResponses() call from an earlier acceptable response
    * doesn't result in an incorrect answer.
    */
   public void testConcurrentResponses()
   {
      ResponseFilter filter = new LookupSucceededFilter();
      ClusterNode sender1 = new ClusterNodeImpl(new IpAddress(12345));
      ClusterNode sender2 = new ClusterNodeImpl(new IpAddress(67890));
      
      filter.isAcceptable("A", sender1);
      filter.isAcceptable(null, sender2);
      assertFalse("Concurrency problem (JBAS-7945)",filter.needMoreResponses());
   }
   
   private void exerciseFilterLogic(ResponseFilter filter, ClusterNode sender)
   {
      filter.isAcceptable(null, sender);
      assertTrue(filter.needMoreResponses());
      
      filter.isAcceptable(new Exception(), sender);
      assertTrue(filter.needMoreResponses());
      
      filter.isAcceptable(new ClusterPartition.NoHandlerForRPC(), sender);
      assertTrue(filter.needMoreResponses());

      filter.isAcceptable(new String(), sender);
      assertFalse(filter.needMoreResponses());
   }
   
   private void exerciseFilterLogic(RspFilter filter, Address sender)
   {
      filter.isAcceptable(null, sender);
      assertTrue(filter.needMoreResponses());
      
      filter.isAcceptable(new Exception(), sender);
      assertTrue(filter.needMoreResponses());
      
      filter.isAcceptable(new ClusterPartition.NoHandlerForRPC(), sender);
      assertTrue(filter.needMoreResponses());

      filter.isAcceptable(new String(), sender);
      assertFalse(filter.needMoreResponses());
   }
}
