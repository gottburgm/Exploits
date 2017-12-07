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
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.web.HttpUtils;

/** Tests of using MC aware servlet context

 @author Ales.Justin@jboss.org
 */
public class MCAwareUnitTestCase extends JBossTestCase
{
   private String baseURL = HttpUtils.getBaseURL();

   public static Test suite() throws Exception
   {
      return getDeploySetup(MCAwareUnitTestCase.class, "mcaware.war");
   }

   public MCAwareUnitTestCase(String name)
   {
      super(name);
   }

   public void testMCAwareServlet() throws Exception
   {
      URL url = new URL(baseURL+"mcaware/MCAwareServlet");
      HttpMethodBase request = HttpUtils.accessURL(url);
   }
}