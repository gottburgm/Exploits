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
package org.jboss.test.ws.jaxws.samples.webservice;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test the JSR-181 annotation: javax.jws.WebService
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 * @since 29-Apr-2005
 */
public class WebServiceEJB3TestCase extends WebServiceBase
{
   public static Test suite()
   {
      return new JBossWSTestSetup(WebServiceEJB3TestCase.class, "");
   }

   public void testWebServiceTest() throws Exception
   {
      deploy("jaxws-samples-webservice01-ejb3.jar");
      try
      {
         webServiceTest("jaxws-samples-webservice01-ejb3");
      }
      finally
      {
         undeploy("jaxws-samples-webservice01-ejb3.jar");
      }
   }

   public void testWebServiceWsdlLocationTest() throws Exception
   {
      deploy("jaxws-samples-webservice02-ejb3.jar");
      try
      {
         webServiceWsdlLocationTest("jaxws-samples-webservice02-ejb3");
      }
      finally
      {
         undeploy("jaxws-samples-webservice02-ejb3.jar");
      }
   }

   public void testWebServiceEndpointInterfaceTest() throws Exception
   {
      deploy("jaxws-samples-webservice03-ejb3.jar");
      try
      {
         webServiceEndpointInterfaceTest("jaxws-samples-webservice03-ejb3");
      }
      finally
      {
         undeploy("jaxws-samples-webservice03-ejb3.jar");
      }
   }
}
