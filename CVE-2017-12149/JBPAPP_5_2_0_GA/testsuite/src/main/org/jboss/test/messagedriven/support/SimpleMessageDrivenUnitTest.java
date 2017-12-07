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
package org.jboss.test.messagedriven.support;

import java.util.Properties;

import javax.management.ObjectName;

/**
 * A simple test 
 *
 * @author <a href="mailto:adrian@jboss.com>Adrian Brock</a>
 * @version <tt>$Revision: 1.4</tt>
 */
public abstract class SimpleMessageDrivenUnitTest extends BasicMessageDrivenUnitTest
{
   public SimpleMessageDrivenUnitTest(String name, ObjectName jmxDestination, Properties defaultProps)
   {
      super(name, jmxDestination, defaultProps);
   }
   
   public void testSimpleRequired() throws Exception
   {
      runTest(getOperations(), defaultProps);
   }
   
   public void testSimpleNotSupported() throws Exception
   {
      Properties props = (Properties) defaultProps.clone();
      props.put("transactionAttribute", "NotSupported");
      runTest(getOperations(), props);
   }
   
   public void testSimpleBMT() throws Exception
   {
      Properties props = (Properties) defaultProps.clone();
      props.put("transactionType", "Bean");
      runTest(getOperations(), props);
   }

   public Operation[] getOperations() throws Exception
   {
      return new Operation[]
      {
         new SendMessageOperation(this, "1"),
         new CheckMessageSizeOperation(this, 1, 0),
         new CheckJMSDestinationOperation(this, 0),
         new CheckMessageIDOperation(this, 0, "1"),
      };
   }

}
