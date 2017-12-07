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

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.inflow.TestResourceAdapter;
import org.jboss.test.jca.inflow.TestResourceAdapterInflowResults;

/**
 * Inflow Unit Tests
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class InflowUnitTestCase extends JBossTestCase
{

   public InflowUnitTestCase (String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(InflowUnitTestCase.class, "jcainflowmdb.jar");
      Test t2 = getDeploySetup(t1, "jcainflow.rar");
      return t2;
   }

   public void testInflow() throws Throwable
   {
      TestResourceAdapterInflowResults results = (TestResourceAdapterInflowResults) getServer().invoke
      (
          TestResourceAdapter.mbean,
          "testInflow",
          new Object[0],
          new String[0]
      );
      
      results.check(); 
   }
}
