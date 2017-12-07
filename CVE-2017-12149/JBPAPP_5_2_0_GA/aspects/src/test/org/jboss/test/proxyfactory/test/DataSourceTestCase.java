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
package org.jboss.test.proxyfactory.test;

import java.sql.Connection;

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.sql.DataSource;

import junit.framework.Test;

import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.test.proxyfactory.AbstractProxyTest;
import org.jboss.test.proxyfactory.support.ConnectionFactoryInterceptor;
import org.jboss.test.proxyfactory.support.TestConnection;
import org.jboss.test.proxyfactory.support.TestConnectionManager;
import org.jboss.test.proxyfactory.support.TestManagedConnectionFactory;

/**
 * DataSourceTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 80997 $
 */
public class DataSourceTestCase extends AbstractProxyTest
{
   public void testConnectionManager() throws Exception
   {
      ConnectionManager cm = new TestConnectionManager();
      ManagedConnectionFactory mcf = new TestManagedConnectionFactory();
      SimpleMetaData metadata = new SimpleMetaData();
      
      metadata.addMetaData(ConnectionFactoryInterceptor.CONNECTION_FACTORY, ConnectionFactoryInterceptor.CONNECTION_MANAGER, cm);
      metadata.addMetaData(ConnectionFactoryInterceptor.CONNECTION_MANAGER, ConnectionFactoryInterceptor.MANAGED_CONNECTION_FACTORY, mcf);
      DataSource ds = (DataSource) assertCreateHollowProxy(new Class[] { DataSource.class }, metadata, DataSource.class);
      Connection c = ds.getConnection();
      assertNotNull(c);
      assertTrue(c instanceof TestConnection);
      TestConnection tc = (TestConnection) c;
      assertNotNull(tc.getManagedConnectionFactory());
      assertTrue(mcf == tc.getManagedConnectionFactory());
   }
   
   public static Test suite()
   {
      return suite(DataSourceTestCase.class);
   }

   public DataSourceTestCase(String name)
   {
      super(name);
   }
}
