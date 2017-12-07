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
package org.jboss.test.cluster.multicfg.web.test;

import junit.framework.Test;

import org.jboss.test.cluster.web.JBossClusteredWebTestCase;

/**
 * Clustering test case of get/set under scoped class loader.
 * Replication granularity is attribute-based.
 *
 * @author Ben Wang
 * @version $Revision: 1.0
 */
public class ScopedAttrBasedTestCase
      extends ScopedTestCase
{

   public ScopedAttrBasedTestCase(String name)
   {
      super(name);
      warName_ = "/http-scoped-attr/";

      concatenate();
   }
   
   protected String getWarName()
   {
      return "http-scoped-attr";
   }

   public static Test suite() throws Exception
   {
      return JBossClusteredWebTestCase.getDeploySetup(ScopedAttrBasedTestCase.class,
                                                      "http-scoped-attr.war");
   }

}
