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
 * Test the ejb and resource injection in a annotated-only jsp  
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class JSPAnnotationENCUnitTestCase extends JBossTestCase
{
   private String baseURL = HttpUtils.getBaseURL(); 
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(JSPAnnotationENCUnitTestCase.class, "simple-mock.beans,jbosstest-jsp-injection.ear");
   }
   
   public JSPAnnotationENCUnitTestCase(String name)
   {
      super(name);
   }
   
   /**
    * Access a simple annotated jsp.
    * 
    * @throws Exception
    */
   public void testJSPAnnotations() throws Exception
   {
      URL url = new URL(baseURL+"simple-jsponly/enc.jsp");
      HttpUtils.accessURL(url);
   }
   
   /**
    * Access a simple annotated jsp.
    * 
    * @throws Exception
    */
   public void testSimpleEjbJSPAnnotations() throws Exception
   {
      URL url = new URL(baseURL+"simple-jsponly/simple.jsp");
      HttpUtils.accessURL(url);
   }
   
   /**
    * Access a nested annotated class in a jsp.
    * 
    * @throws Exception
    */
   public void testNestedEjbJSPAnnotations() throws Exception
   {
      URL url = new URL(baseURL+"simple-jsponly/nested.jsp");
      HttpUtils.accessURL(url);
   }
   
}

