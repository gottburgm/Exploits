/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.test.jpa.test;

import java.net.HttpURLConnection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.JBossTestCase;

/**
 * AbstractWebJPATest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class AbstractWebJPATest extends JBossTestCase
{
   private String baseURL = "http://" + System.getProperty("jbosstest.server.host", "localhost") + ":" + Integer.getInteger("web.port", 8080) + "/jpa-test/Test?mode=";

   public AbstractWebJPATest(String name)
   {
      super(name);
   }
   
   public void testWebJPA() throws Exception
   {
      accessURL("Write");
      assertEquals("World", accessURL("Read"));
   }

   public String accessURL(String url) throws Exception
   {
      HttpClient httpConn = new HttpClient();
      HttpMethodBase request = new GetMethod(baseURL + url);
      log.debug("RequestURI: " + request.getURI());
      int responseCode = httpConn.executeMethod(request);
      String response = request.getStatusText();
      log.debug("responseCode="+responseCode+", response="+response);
      String content = request.getResponseBodyAsString();
      log.debug(content);
      assertEquals(HttpURLConnection.HTTP_OK, responseCode);
      return content;
   }
}
