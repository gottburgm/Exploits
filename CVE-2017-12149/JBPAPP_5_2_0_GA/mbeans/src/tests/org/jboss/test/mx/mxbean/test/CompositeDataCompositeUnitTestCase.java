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

import javax.management.openmbean.CompositeData;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.mx.mxbean.support.NullSimpleObject;
import org.jboss.test.mx.mxbean.support.SimpleInterface;
import org.jboss.test.mx.mxbean.support.SimpleObject;

/**
 * CompositeDataCompositeUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class CompositeDataCompositeUnitTestCase extends CompositeDataTest
{
   public static Test suite()
   {
      return new TestSuite(CompositeDataCompositeUnitTestCase.class);
   }
   
   public CompositeDataCompositeUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testComposite() throws Exception
   {
      SimpleObject simple = new SimpleObject();
      CompositeData openData = createCompositeData(simple.getClass().getName(), SimpleInterface.KEYS, SimpleInterface.VALUES);
      constructReconstructTest(simple, openData);
   }
   
   public void testCompositeNull() throws Exception
   {
      constructReconstructTest(null, SimpleObject.class);
   }
   
   public void testCompositeContainsNull() throws Exception
   {
      NullSimpleObject simple = new NullSimpleObject();
      CompositeData openData = createCompositeData(simple.getClass().getName(), SimpleInterface.KEYS, SimpleInterface.TYPES, SimpleInterface.LEGAL_NULL_VALUES);
      constructReconstructTest(simple, openData);
   }

   protected void checkFinalEquals(Object expected, Object actual)
   {
      checkCompositeDataHandlerEquals(expected, actual);
   }
}
