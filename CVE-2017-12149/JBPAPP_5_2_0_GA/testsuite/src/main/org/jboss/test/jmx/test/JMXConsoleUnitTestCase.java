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
package org.jboss.test.jmx.test;

import java.net.URL;
import java.net.HttpURLConnection;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.HttpClient;

import org.jboss.jmx.adaptor.html.CSRFUtil;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.web.HttpUtils;

/** Basic access tests of the http jmx-console interface
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 113239 $
 */
public class JMXConsoleUnitTestCase
   extends JBossTestCase
{
   private String baseURLNoAuth = HttpUtils.getBaseURLNoAuth(); 

   public JMXConsoleUnitTestCase(String name)
   {
      super(name);
   }

   /** Test access of the jmx-console/index.jsp page
    * @throws Exception
    */ 
   public void testIndexPage()
      throws Exception
   {
      URL url = new URL(baseURLNoAuth+"jmx-console/index.jsp");
      HttpUtils.accessURL(url);
   }

   /** Test an mbean inspection via a get to the HtmlAdaptor
    * @throws Exception
    */ 
   public void testMBeanInspection()
      throws Exception
   {
      // The jboss:service=Naming mbean view 
      URL url = new URL(baseURLNoAuth+"jmx-console/HtmlAdaptor?action=inspectMBean&name=jboss%3Aservice%3DNaming");
      HttpUtils.accessURL(url);
   }

   /** Test an mbean invocation via a post to the HtmlAdaptor
    * @throws Exception
    */ 
   public void testMBeanOperation()
      throws Exception
   {
      //NOTE: HttpUtils does not return... HttpClient, only answer, hence session is not maintained.
      // The jboss.system:type=Server mbean view 
      final String url = baseURLNoAuth+"jmx-console/HtmlAdaptor";
      HttpClient testClient = new HttpClient();
      GetMethod getForm = new GetMethod(url+"?action=inspectMBean&name=jboss.system%3Atype%3DServer");
      int responseCode = testClient.executeMethod(getForm.getHostConfiguration(),getForm);
      assertEquals(new String(getForm.getResponseBody()),HttpURLConnection.HTTP_OK, responseCode);
      //need response body to extract CSRF token and put into url, this is done by JSP...
      //http://viewvc.jboss.org/cgi-bin/viewvc.cgi/jbossas?view=revision&revision=113042
      final String responseBody = new String(getForm.getResponseBody());
      int index = responseBody.indexOf(CSRFUtil.CSRF_TOKEN);
      index = responseBody.indexOf("value",index+CSRFUtil.CSRF_TOKEN.length()+1);
      index= responseBody.indexOf("=",index)+1;
      char separatorChar = responseBody.charAt(index);
      index++;
      int endIndex= responseBody.indexOf(separatorChar,index);
      final String csrfToken = responseBody.substring(index,endIndex);

      // Submit the op invocation form for op=runGarbageCollector
      PostMethod formPost = new PostMethod(url);
      formPost.addRequestHeader("Referer", url);
      formPost.addParameter("action", "invokeOpByName");
      formPost.addParameter("name", "jboss.system:type=Server");
      formPost.addParameter("methodName", "runGarbageCollector");
      formPost.addParameter(CSRFUtil.CSRF_TOKEN,csrfToken);
      
      responseCode = testClient.executeMethod(formPost.getHostConfiguration(),
         formPost);

      assertEquals(new String(formPost.getResponseBody()),HttpURLConnection.HTTP_OK, responseCode);
   }
}
