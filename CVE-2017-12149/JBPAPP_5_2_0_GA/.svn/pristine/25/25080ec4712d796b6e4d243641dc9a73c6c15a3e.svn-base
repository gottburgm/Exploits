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
package org.jboss.test.cluster.defaultcfg.web.test;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import org.jboss.test.cluster.testutil.DBSetupDelegate;
import org.jboss.test.cluster.testutil.DelegatingClusteredTestCase;
import org.jboss.test.cluster.testutil.TestSetupDelegate;
import org.jboss.test.cluster.web.persistent.PersistentStoreSetupDelegate;

public class PersistentManagerCrossContextCallsTestCase extends CrossContextCallsTestCase
{

   public PersistentManagerCrossContextCallsTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      String dbAddress = System.getProperty(DBSetupDelegate.DBADDRESS_PROPERTY, DBSetupDelegate.DEFAULT_ADDRESS);
      TestSetupDelegate dbDelegate = new DBSetupDelegate(dbAddress, DBSetupDelegate.DEFAULT_PORT);
      TestSetupDelegate storeDelegate = new PersistentStoreSetupDelegate(dbAddress, DBSetupDelegate.DEFAULT_PORT);
      List<TestSetupDelegate> list = Arrays.asList(new TestSetupDelegate[]{dbDelegate, storeDelegate});
      return DelegatingClusteredTestCase.getDeploySetup(PersistentManagerCrossContextCallsTestCase.class,
                                                      "httpsession-ds.xml, disable-manager-override.beans, " +
                                                      "http-cross-ctx-persistent.ear", list);
   }
}
