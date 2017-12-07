/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.commons_logging.jbpapp6523.test;

import java.io.IOException;

/**
 * This test case's only method tests that the jbpapp6523 servlet does not
 * compile by checking for an HTTP response code of 500.  It uses the
 * jbpapp6523-use_tccl-true server configuration which sets
 * org.apache.commons.logging.use_tccl to true.
 *
 * @author jiwils
 */
public class UseTCCLTrueTestCase extends CommonsLoggingBaseTestCase
{
   public UseTCCLTrueTestCase(String name)
   {
      super(name);
   }

   public void testUseTCCLTrue()
   throws IOException
   {
      assertEquals(500, getHTTPResponseCode());
   }
}