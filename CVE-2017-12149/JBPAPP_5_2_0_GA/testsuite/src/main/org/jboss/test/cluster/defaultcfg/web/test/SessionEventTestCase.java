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
import org.jboss.test.cluster.testutil.WebTestBase;
import org.jboss.test.cluster.web.JBossClusteredWebTestCase;

/**
 * Test case for clustered session event.
 *
 * @author Ben Wang
 * @version $Revision: 1.0
 */
public class SessionEventTestCase
      extends WebTestBase
{

   public SessionEventTestCase(String name)
   {
      super(name);

   }

   public static Test suite() throws Exception
   {
      return JBossClusteredWebTestCase.getDeploySetup(SessionEventTestCase.class,
                                                      "http-sr.war");
   }

   public void testSessionBindingEvent()
      throws Exception
   {
      String attr = "";
      getLog().debug("Enter testSessionBindingEvent");

      String setURLName = "/http-sr/bindSession.jsp" + "?Binding=true";
      String getURLName = "/http-sr/bindSession.jsp" + "?Binding=false";

      getLog().debug(setURLName + ":::::::" + getURLName);

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      attr = makeGet(client, baseURL0_ +setURLName);
      log.info("*** Response is " +attr);

      assertNotNull("Http session get", attr);
      boolean isOK = false;
      if( attr.indexOf("OK") >= 0 ) isOK = true;
      assertTrue("Response for session bound event should be", isOK);
      attr = makeGetWithState(client, baseURL0_ +getURLName);
      if( attr.indexOf("OK") >= 0 ) isOK = true;
      assertNotNull("Http session get", attr);
      assertTrue("Response for session unbound event should be", isOK);
   }

}
