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
package org.jboss.test.ws.jaxws.samples.soapbinding;

import javax.jws.WebService;

import org.jboss.logging.Logger;

/**
 * Test the JSR-181 annotation: javax.jws.SOAPBinding
 *
 * @author Thomas.Diesler@jboss.org
 * @since 16-Oct-2005
 */

@WebService(serviceName="ExampleService", endpointInterface="org.jboss.test.ws.jaxws.samples.soapbinding.ExampleSEI")
public class ExampleServiceImpl
{
   // Provide logging
   private static Logger log = Logger.getLogger(ExampleServiceImpl.class);

   public String concat(String first, String second, String third)
   {
      String retStr = first + "|" + second + "|" + third;
      log.info("concat: " + retStr);
      return retStr;
   }
}
