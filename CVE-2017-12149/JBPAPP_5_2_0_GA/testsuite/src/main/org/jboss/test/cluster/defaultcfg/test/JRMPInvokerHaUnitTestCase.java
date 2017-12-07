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

import java.util.Map;

import org.jboss.invocation.InvokerHA;
import org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxyHA;
import org.jboss.test.cluster.invokerha.AbstractInvokerHa;
import org.jboss.test.cluster.invokerha.InvokerHaInfrastructure;
import org.jboss.test.cluster.invokerha.InvokerHaInfrastructure.InvokerHaFactory;
import org.jboss.test.cluster.invokerha.JRMPInvokerHaMockUtils.MockJRMPInvokerHA;

/**
 * Unit test case for jrmp invoker ha proxy and invoker at the other side.
 *  
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class JRMPInvokerHaUnitTestCase extends AbstractInvokerHa
{
   @Override
   protected void setUp() throws Exception
   {
      setUp(2, new JRMPInvokerHaFactory());
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
   }
   
   /** Classes **/
   
   public static class JRMPInvokerHaFactory implements InvokerHaFactory
   {
      public String getInvokerTypeName()
      {
         return "JRMPInvokerHa";
      }

      public InvokerHaInfrastructure getInvokerHaInfrastructure(int serverCount)
      {
         return new InvokerHaInfrastructure(serverCount, this);
      }
      
      public Map getTxFailoverAuthorizationsMap()
      {
         return JRMPInvokerProxyHA.txFailoverAuthorizations;
      }

      public InvokerHA createInvokerHaServer(String serverName, int serverNumber)
      {
         return new MockJRMPInvokerHA(getInvokerTypeName() + "-" + serverName + "-" + serverNumber);
      }

      public String getChosenTargetKey()
      {
         return "TEST_CHOSEN_TARGET";
      }
   }
}
