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
package org.jboss.test.mx.mxbean.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * CompositeDataByteUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class CompositeDataByteUnitTestCase extends CompositeDataTest
{
   public static Test suite()
   {
      return new TestSuite(CompositeDataByteUnitTestCase.class);
   }
   
   public CompositeDataByteUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testByte() throws Exception
   {
      constructReconstructTest(new Byte(Byte.MAX_VALUE));
   }
   
   public void testByteNull() throws Exception
   {
      constructReconstructTest(null, Byte.class);
   }
   
   public void testBytePrimitive() throws Exception
   {
      constructReconstructTest(Byte.MAX_VALUE, Byte.TYPE);
   }
   
   public void testByteNullPrimitive() throws Exception
   {
      assertNullFailure(Byte.TYPE);
   }
}
