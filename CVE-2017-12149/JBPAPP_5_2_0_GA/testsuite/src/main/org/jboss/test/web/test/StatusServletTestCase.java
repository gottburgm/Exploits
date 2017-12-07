/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import javax.management.MalformedObjectNameException;

import junit.framework.Test;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.JBossTestCase;

/**
 * Invokes on the ROOT.war's StatusServlet and checks for 200 responses. This
 * was written as a test for the https://jira.jboss.org/browse/JBPAPP-3674 
 * condition.
 * 
 * @author Brian Stansberry
 */
public class StatusServletTestCase extends JBossTestCase
{   
   
   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(StatusServletTestCase.class, "http-sr.war");
   }
   
   /**
    * Create a new StatusServletTestCase.
    * 
    * @param name
    * @throws MalformedObjectNameException 
    */
   public StatusServletTestCase(String name) throws MalformedObjectNameException
   {
      super(name);
   }

   public void testStatusServlet() throws Exception
   {
      HttpClient httpConn = new HttpClient();
      String url = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + "/status?full=true"; 
      GetMethod getMethod = new GetMethod(url);
      int responseCode = httpConn.executeMethod(getMethod);
      assertEquals("GET " + url + " OK", HttpURLConnection.HTTP_OK, responseCode);
   }
}
