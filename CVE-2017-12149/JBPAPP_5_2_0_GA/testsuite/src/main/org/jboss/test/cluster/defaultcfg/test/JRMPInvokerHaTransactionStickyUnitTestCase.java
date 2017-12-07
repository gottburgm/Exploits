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

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.invocation.InvokerHA;
import org.jboss.test.cluster.defaultcfg.test.JRMPInvokerHaUnitTestCase.JRMPInvokerHaFactory;
import org.jboss.test.cluster.invokerha.AbstractInvokerHaTransactionSticky;

/**
 * JRMPInvokerHaTransactionStickyUnitTestCase.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class JRMPInvokerHaTransactionStickyUnitTestCase extends AbstractInvokerHaTransactionSticky
{
   @Override
   protected void setUp() throws Exception
   {
      setUp(2, new JRMPInvokerHaTransactionStickyFactory());
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
   }   
   
   @Override
   protected List undeployChosenTargetNode(Object chosenTarget)
   {
      List<? extends InvokerHA> replicants = infrastructure.getReplicants();
      replicants.remove(chosenTarget);
      return replicants;
   }
   
   public static class JRMPInvokerHaTransactionStickyFactory extends JRMPInvokerHaFactory
   {
      @Override
      public String getChosenTargetKey()
      {
         return "TX_STICKY_TARGET";
      }
   }
   
   public static Test suite() 
   {
      /* No transactions to be run for JRMP invoker version of transaction 
       * sticky functionality because this old legacy invoker is no longer 
       * supported for transaction sticky functionality. */
      return new TestSuite();
   }
}
