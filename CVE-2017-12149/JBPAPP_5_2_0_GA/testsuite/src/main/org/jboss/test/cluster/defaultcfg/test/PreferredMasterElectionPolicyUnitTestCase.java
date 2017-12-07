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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.server.ClusterNodeImpl;
import org.jboss.ha.singleton.PreferredMasterElectionPolicy;
import org.jgroups.stack.IpAddress;

/**
 * Unit tests for the preferred master election policy. The tested policy has 
 * been configured with position 0 which means that the first member of the 
 * candidate list will be chosen if the preferred master is malformed or 
 * missing. So, when we expect the preferred master to be chosen, we assert 
 * against the second candidate. Different test methods will test different 
 * ways to define the preferred master.  
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class PreferredMasterElectionPolicyUnitTestCase extends TestCase
{
   private PreferredMasterElectionPolicy policy;
   private List<ClusterNode> candidates = new ArrayList<ClusterNode>();
   
   @Override
   protected void setUp() throws UnknownHostException
   {
      InetAddress localAddress = InetAddress.getByName("localhost");
      
      this.candidates.add(new ClusterNodeImpl(new IpAddress(localAddress, 10000)));
      this.candidates.add(new ClusterNodeImpl(new IpAddress(localAddress, 10001)));
      this.candidates.add(new ClusterNodeImpl(new IpAddress(localAddress, 10002)));
      
      this.policy = new PreferredMasterElectionPolicy();
   }
   
   /**
    * @{inheritDoc}
    * @see junit.framework.TestCase#tearDown()
    */
   @Override
   protected void tearDown() throws Exception
   {
      this.candidates.clear();
   }


   public void testUsePreferredMasterViaHost()
   {
      this.policy.setPreferredMaster("localhost:10001");
      
      ClusterNode master = this.policy.elect(this.candidates);
      
      Assert.assertSame(this.candidates.get(1), master);
   }
   
   public void testUsePreferredMasterViaAddress()
   {
      this.policy.setPreferredMaster("127.0.0.1:10002");
      
      ClusterNode master = this.policy.elect(this.candidates);
      
      Assert.assertSame(this.candidates.get(2), master);
   }
   
   public void testUseDefaultMaster()
   {
      this.policy.setPreferredMaster("localhost:10003");
      
      ClusterNode master = this.policy.elect(this.candidates);
      
      Assert.assertSame(this.candidates.get(0), master);
      
      this.policy.setPosition(1);
      
      master = this.policy.elect(this.candidates);
      
      Assert.assertSame(this.candidates.get(1), master);
   }
   
   public void testUseDefaultMasterNoPreference()
   {
      ClusterNode master = this.policy.elect(this.candidates);
      
      Assert.assertSame(this.candidates.get(0), master);
      
      this.policy.setPosition(1);
      
      master = this.policy.elect(this.candidates);
      
      Assert.assertSame(this.candidates.get(1), master);
   }
   
   public void testUseDefaultMasterEmptyPreference()
   {
      this.policy.setPreferredMaster("");
      
      ClusterNode master = this.policy.elect(this.candidates);
      
      Assert.assertSame(this.candidates.get(0), master);
      
      this.policy.setPosition(1);
      
      master = this.policy.elect(this.candidates);
      
      Assert.assertSame(this.candidates.get(1), master);
   }
   
   public void testMissingHost() throws Exception
   {
      try
      {
         this.policy.setPreferredMaster(":1199");
         
         Assert.fail("IllegalArgumentException expected");
      }
      catch (IllegalArgumentException e)
      {
         // Expected
      }
   }
   
   public void testInvalidPort() throws Exception
   {
      try
      {
         this.policy.setPreferredMaster("localhost:abcd");
         
         Assert.fail("IllegalArgumentException expected");
      }
      catch (IllegalArgumentException e)
      {
         // Expected
      }
   }

   public void testUnknownHost() throws Exception
   {
      try
      {
         this.policy.setPreferredMaster("onceuponatimeinalandfarfarawaylivedamancalledgalder:1199");
         
         Assert.fail(this.policy.getPreferredMaster());
      }
      catch (IllegalArgumentException e)
      {
         // Expected
      }
   }
   
   public void testGarbage() throws Exception
   {
      try
      {
         this.policy.setPreferredMaster("%^$%&%^&$%$£");
         
         Assert.fail("IllegalArgumentException expected");
      }
      catch (IllegalArgumentException e)
      {
         // Expected
      }
   }
}