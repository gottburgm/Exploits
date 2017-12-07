/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.web.test;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.httpclient.HttpMethodBase;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.web.HttpUtils;

/**
 * Test the remote classloading facility of the WebServer listening
 * on post 8083
 * 
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 85945 $
 */
public class RemoteClassloadingServiceUnitTestCase extends JBossTestCase
{
   static final String baseURL = "http://" + System.getProperty("jbosstest.server.host", "localhost") + ":8083/";
   
   public RemoteClassloadingServiceUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * JBAS-4540, don't leak installation directory info
    * through the classloading service.
    */
   public void testHttpRequestRevealInstallationDirectory() throws Exception
   {
      URL url = new URL(baseURL + "org.jboss.web.WebServer.class");
      HttpMethodBase request = HttpUtils.accessURL(url, null, HttpURLConnection.HTTP_NOT_FOUND);
      String statusText = request.getStatusText();
      
      if (statusText.indexOf(".jar") > 0)
         fail("Status text reveals installation directory information: " + statusText);
   }
}