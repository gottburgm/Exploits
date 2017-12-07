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

import junit.framework.Test;
import org.apache.commons.httpclient.HttpClient;
import org.jboss.test.cluster.multicfg.web.test.ScopedTestCase;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.web.JBossClusteredWebTestCase;

/**
 * Clustering test case of get/set under scoped class loader.
 * Trigger type is set only, i.e., get is not dirty.
 * And replication granularity is attribute based.
 *
 * @author Ben Wang
 * @version $Revision: 1.0
 */
public class ScopedSetAttributeTestCase
      extends ScopedTestCase
{

   public ScopedSetAttributeTestCase(String name)
   {
      super(name);
      warName_ = "/http-scoped-set-attr/";
      
      concatenate();
   }
   
   protected String getWarName()
   {
      return "http-scoped-set-attr";
   }

   public static Test suite() throws Exception
   {
      return JBossClusteredWebTestCase.getDeploySetup(ScopedSetAttributeTestCase.class,
                                                      "http-scoped-set-attr.war");
   }


   /**
    * Test session modify with non-primitive get/modify.
    *
    * @throws Exception
    */
   public void testNonPrimitiveModify()
         throws Exception
   {
      getLog().debug("Enter testNonPrimitiveModify");

      getLog().debug(setUrl + ":::::::" + getUrl);

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl);

      // Get the Attribute set
      String attrOld = makeGet(client, baseURL0_ +getUrl);

      // Get the Attribute set
      makeGet(client, baseURL0_ +modifyNoSetUrl);

      // Get the Attribute set
      makeGet(client, baseURL0_ +getUrl);

      sleepThread(DEFAULT_SLEEP);

      // Make connection to server 1 and get
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);
      String attr2 = makeGet(client, baseURL1_ +getUrl);

      // Check the result
      assertEquals("Attributes should be the same", attrOld, attr2);
      getLog().debug("Exit testNonPrimitiveModify");
   }


}
