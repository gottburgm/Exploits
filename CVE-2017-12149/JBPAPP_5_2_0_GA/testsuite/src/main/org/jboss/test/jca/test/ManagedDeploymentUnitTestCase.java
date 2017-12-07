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
package org.jboss.test.jca.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.resource.deployers.builder.AbstractBuilder;
import org.jboss.resource.deployers.builder.ConnectionFactoryBindingBuilder;
import org.jboss.resource.deployers.builder.ConnectionManagerBuilder;
import org.jboss.resource.deployers.builder.ManagedConnectionPoolBuilder;
import org.jboss.resource.metadata.mcf.LocalDataSourceDeploymentMetaData;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentGroup;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryTransactionSupportMetaData;
import org.jboss.resource.metadata.mcf.NoTxConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.NoTxDataSourceDeploymentMetaData;
import org.jboss.resource.metadata.mcf.TxConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.XADataSourceDeploymentMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.support.ManagedDeploymentSupportHelper;

/**
 * A ManagedDeploymentUnitTestCase.
 *
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 85945 $
 */
public class ManagedDeploymentUnitTestCase extends JBossTestCase
{
   public ManagedDeploymentUnitTestCase(String name)
   {
      super(name);
   }

   public void testDefaultValuesUnmarshal() throws Exception
   {
      ManagedConnectionFactoryDeploymentGroup group = ManagedDeploymentSupportHelper.unmarshalResource("jca/deployment/default-content-ds.xml");
      List<ManagedConnectionFactoryDeploymentMetaData> deployments = group.getDeployments();

      for (ManagedConnectionFactoryDeploymentMetaData md : deployments)
      {
         assertTrue(md.isUseJavaContext());

         if(md instanceof NoTxDataSourceDeploymentMetaData)
         {
            assertFalse(md.getLocalTransactions());
            assertTrue(md.getTrackConnectionByTransaction());
            assertFalse(md.isInterleaving());
            assertTrue(md.getTransactionSupportMetaData().equals(ManagedConnectionFactoryTransactionSupportMetaData.NONE));

         }else if(md instanceof LocalDataSourceDeploymentMetaData)
         {
            assertTrue(md.getLocalTransactions());
            assertTrue(md.getTrackConnectionByTransaction());
            assertFalse(md.isInterleaving());
            assertFalse(md.getNoTxSeparatePools());
            assertTrue(md.getTransactionSupportMetaData().equals(ManagedConnectionFactoryTransactionSupportMetaData.LOCAL));

         }else if(md instanceof XADataSourceDeploymentMetaData)
         {
            assertFalse(md.getLocalTransactions());
            assertTrue(md.getTrackConnectionByTransaction());
            assertFalse(md.isInterleaving());
            assertTrue(md.getTransactionSupportMetaData().equals(ManagedConnectionFactoryTransactionSupportMetaData.XA));

         }
         else if(md instanceof TxConnectionFactoryDeploymentMetaData)
         {
            TxConnectionFactoryDeploymentMetaData txmd = (TxConnectionFactoryDeploymentMetaData)md;

            if(txmd.getJndiName().equalsIgnoreCase("Local"))
            {
               assertTrue(txmd.getLocalTransactions());
               assertFalse(txmd.getXATransaction());
               assertTrue(txmd.getTransactionSupportMetaData().equals(ManagedConnectionFactoryTransactionSupportMetaData.LOCAL));
            }
            else
            {
               assertFalse(txmd.getLocalTransactions());
               assertTrue(txmd.getXATransaction());
               assertTrue(txmd.getTransactionSupportMetaData().equals(ManagedConnectionFactoryTransactionSupportMetaData.XA));
            }
         }
         else if(md instanceof NoTxConnectionFactoryDeploymentMetaData)
         {
            assertFalse(md.getLocalTransactions());
            assertFalse(md.getTrackConnectionByTransaction());
            assertFalse(md.isInterleaving());
            assertTrue(md.getTransactionSupportMetaData().equals(ManagedConnectionFactoryTransactionSupportMetaData.NONE));
         }
      }
   }


   public void testConnectionPoolUnmarshal() throws Exception
   {
      ManagedConnectionFactoryDeploymentGroup group = ManagedDeploymentSupportHelper.unmarshalResource("jca/deployment/pool-content-ds.xml");
      ManagedConnectionFactoryDeploymentMetaData md = group.getDeployments().get(0);
      assertTrue(md.getPrefill());
   }

   public void testBasicMarshal() throws Exception
   {
      ManagedConnectionFactoryDeploymentGroup group = ManagedDeploymentSupportHelper.unmarshalResource("jca/remote-jdbc/remote-ds.xml");
      String result = ManagedDeploymentSupportHelper.marshalResourceAsString(group);
      group = ManagedDeploymentSupportHelper.unmarshalSource(result);

   }

   public void testEmptyContentUnmarshal() throws Exception
   {
      ManagedConnectionFactoryDeploymentGroup group = ManagedDeploymentSupportHelper.unmarshalResource("jca/deployment/empty-content-ds.xml");
      ManagedConnectionFactoryDeploymentMetaData md = group.getDeployments().get(0);
      assertTrue(md.getTrackConnectionByTransaction()); // this is always true for local tx
      assertFalse(md.isInterleaving());
      assertTrue(md.getNoTxSeparatePools());
      assertTrue(md.getUseStrictMin());
      assertEquals(0, md.getAllocationRetry());
      assertEquals(5000, md.getAllocationRetryWaitMillis());
   }

   public void testEmptyContentMarshal() throws Exception
   {
      ManagedConnectionFactoryDeploymentGroup group = ManagedDeploymentSupportHelper.unmarshalResource("jca/deployment/empty-content-ds.xml");
      String result = ManagedDeploymentSupportHelper.marshalResourceAsString(group);
      group = ManagedDeploymentSupportHelper.unmarshalSource(result);
      ManagedConnectionFactoryDeploymentMetaData md = group.getDeployments().get(0);
      assertTrue(md.getTrackConnectionByTransaction()); // this is always true for local tx
      assertFalse(md.isInterleaving());
      assertTrue(md.getNoTxSeparatePools());
      assertTrue(md.getUseStrictMin());
      assertEquals(0, md.getAllocationRetry());
      assertEquals(5000, md.getAllocationRetryWaitMillis());
   }

   public void testXAWithInterleavingMarshal() throws Exception
   {
      ManagedConnectionFactoryDeploymentGroup group = ManagedDeploymentSupportHelper.unmarshalResource("jca/deployment/xa-interleaving-ds.xml");
      String result = ManagedDeploymentSupportHelper.marshalResourceAsString(group);
      group = ManagedDeploymentSupportHelper.unmarshalSource(result);
      ManagedConnectionFactoryDeploymentMetaData md = group.getDeployments().get(0);
      assertFalse(md.getTrackConnectionByTransaction());
      assertTrue(md.isInterleaving());

   }

   public void testBasicUnmarshal() throws Exception
   {
      ManagedConnectionFactoryDeploymentGroup group = ManagedDeploymentSupportHelper.unmarshalResource("jca/remote-jdbc/remote-ds.xml");
      ManagedConnectionFactoryDeploymentMetaData md = group.getDeployments().get(0);

      assertTrue(group.getDeployments().size() == 1);
      assertTrue(md instanceof LocalDataSourceDeploymentMetaData);
      assertTrue(md.getLocalTransactions());

      assertTrue(ManagedDeploymentSupportHelper.isValidDeployment("jca/remote-jdbc/remote-ds.xml", md, "jndi-name",
            "connection-url"));

      assertTrue(ManagedDeploymentSupportHelper.hasAnnotation(md, "jndiName"));

   }

   public void testServiceMetaDataBuilder() throws Exception
   {
      List<AbstractBuilder> builders = new ArrayList<AbstractBuilder>();
      // builders.add(new ManagedConnectionFactoryBuilder());
      builders.add(new ManagedConnectionPoolBuilder());
      builders.add(new ConnectionFactoryBindingBuilder());
      builders.add(new ConnectionManagerBuilder());

      // builders.add(new MetaDataTypeMappingBuilder());

      ManagedConnectionFactoryDeploymentGroup group = ManagedDeploymentSupportHelper.unmarshalResource("jca/remote-jdbc/remote-ds.xml");
      ManagedConnectionFactoryDeploymentMetaData mcmd = group.getDeployments().get(0);

      List<ServiceMetaData> results = new ArrayList<ServiceMetaData>();

      for (AbstractBuilder abstractBuilder : builders)
      {
         ServiceMetaData md = abstractBuilder.build(mcmd);
         results.add(md);
      }
   }

   public void testTrackConnectionByTxUnmarshal() throws Exception
   {
      ManagedConnectionFactoryDeploymentGroup group = ManagedDeploymentSupportHelper.unmarshalResource("jca/deployment/track-connection-by-tx-ds.xml");
      List<ManagedConnectionFactoryDeploymentMetaData> deployments = group.getDeployments();
      assertEquals(4, deployments.size());

      for (ManagedConnectionFactoryDeploymentMetaData md : deployments)
      {
         assertTrue(md instanceof TxConnectionFactoryDeploymentMetaData);
         if(md.getJndiName().equals("XAWithTrackConnectionByTx"))
         {
            assertTrue(md.getTrackConnectionByTransaction());
            assertFalse(md.isInterleaving());
            assertFalse(md.getLocalTransactions());
         }
         else if(md.getJndiName().equals("DefaultXA"))
         {
            assertTrue(md.getTrackConnectionByTransaction());
            assertFalse(md.isInterleaving());
            assertFalse(md.getLocalTransactions());
         }
         else if(md.getJndiName().equals("XAWithInterleaving"))
         {
            assertFalse(md.getTrackConnectionByTransaction());
            assertTrue(md.isInterleaving());
            assertFalse(md.getLocalTransactions());
         }
         else if(md.getJndiName().equals("Local"))
         {
            assertTrue(md.getTrackConnectionByTransaction());
            assertFalse(md.isInterleaving());
            assertTrue(md.getLocalTransactions());
         }
         else
            fail("unexpected connection factory: " + md.getJndiName());
      }
   }

   public void testAllocationRetry() throws Exception
   {
      ManagedConnectionFactoryDeploymentGroup group = ManagedDeploymentSupportHelper.unmarshalResource("jca/deployment/allocation-content-ds.xml");
      List<ManagedConnectionFactoryDeploymentMetaData> deployments = group.getDeployments();
      assertEquals(5, deployments.size());

      for (ManagedConnectionFactoryDeploymentMetaData md : deployments)
      {
         assertEquals(1, md.getAllocationRetry());
         assertEquals(1000, md.getAllocationRetryWaitMillis());
      }
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new ManagedDeploymentUnitTestCase("testBasicUnmarshal"));
      suite.addTest(new ManagedDeploymentUnitTestCase("testServiceMetaDataBuilder"));
      suite.addTest(new ManagedDeploymentUnitTestCase("testBasicMarshal"));
      suite.addTest(new ManagedDeploymentUnitTestCase("testEmptyContentUnmarshal"));
      suite.addTest(new ManagedDeploymentUnitTestCase("testEmptyContentMarshal"));
      suite.addTest(new ManagedDeploymentUnitTestCase("testDefaultValuesUnmarshal"));
      suite.addTest(new ManagedDeploymentUnitTestCase("testConnectionPoolUnmarshal"));
      suite.addTest(new ManagedDeploymentUnitTestCase("testTrackConnectionByTxUnmarshal"));
      suite.addTest(new ManagedDeploymentUnitTestCase("testXAWithInterleavingMarshal"));
      suite.addTest(new ManagedDeploymentUnitTestCase("testAllocationRetry"));

      return suite;
   }
}
