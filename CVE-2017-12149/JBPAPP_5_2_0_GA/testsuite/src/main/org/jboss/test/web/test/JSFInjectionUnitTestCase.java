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
package org.jboss.test.web.test;

import java.net.URL;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.util.web.HttpUtils;

/**
 * JBAS-4399. Test correct injection into a JSF managed beans.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class JSFInjectionUnitTestCase extends JBossTestCase
{

   private String baseURL = HttpUtils.getBaseURL(); 
   
   public JSFInjectionUnitTestCase(String name)
   {
      super(name);
   }

   public void testSimpleJSF() throws Exception
   {
      URL url = new URL(baseURL+"jsfinjection/simple.jsf");
      HttpUtils.accessURL(url);
   }
   
   public void testENCJSF() throws Exception
   {
      URL url = new URL(baseURL+"jsfinjection/enc.jsf");
      HttpUtils.accessURL(url);
   }
   
   public void testAlternateJSF() throws Exception
   {
      URL url = new URL(baseURL+"alternate-jsf-injection/simple.jsf");
      HttpUtils.accessURL(url);
   }
   
   public void testAlternameENCJSF() throws Exception
   {
      URL url = new URL(baseURL+"alternate-jsf-injection/enc.jsf");
      HttpUtils.accessURL(url);
   }
   
   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(JSFInjectionUnitTestCase.class, "simple-mock.beans,jbosstest-jsf-injection.ear");
   }


   
}

