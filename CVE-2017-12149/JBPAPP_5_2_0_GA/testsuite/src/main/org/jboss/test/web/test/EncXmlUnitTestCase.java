/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.web.test;

import java.net.URL;

import junit.framework.Test;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.web.HttpUtils;

/** Tests of a web app java:comp/env jndi settings specified via the
 * web.xml/jboss-web.xml descriptors.
 
 @author Scott.Stark@jboss.org
 @version $Revision: 85945 $
 */
public class EncXmlUnitTestCase extends JBossTestCase
{
   private String baseURL = HttpUtils.getBaseURL();
   Logger log = getLog();
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(EncXmlUnitTestCase.class, "simple-mock.beans,simple-xmlonly.war");
   }

   public EncXmlUnitTestCase(String name)
   {
      super(name);
   }

   /** Access the http://{host}/simple-xmlonly/ENCServlet
    */
   public void testAltRequestInfoServlet()
      throws Exception
   {
      URL url = new URL(baseURL+"simple-xmlonly/ENCServlet");
      HttpMethodBase request = HttpUtils.accessURL(url);
      Header errors = request.getResponseHeader("X-Exception");
      log.info("X-Exception: "+errors);
      assertTrue("X-Exception("+errors+") is null", errors == null);      
   }

}
