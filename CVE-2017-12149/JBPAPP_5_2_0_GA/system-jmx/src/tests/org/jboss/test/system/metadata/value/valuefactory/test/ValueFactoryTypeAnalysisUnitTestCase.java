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

package org.jboss.test.system.metadata.value.valuefactory.test;

import static org.jboss.system.metadata.ServiceValueFactoryValueMetaData.populateParameterTypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.reflect.spi.MethodInfo;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.system.metadata.ServiceValueFactoryParameterMetaData;
import org.jboss.system.metadata.ServiceValueFactoryValueMetaData;

import junit.framework.TestCase;

/**
 * Tests of {@link ServiceValueFactoryValueMetaData#populateParameterTypes(java.util.Set, String, java.util.List)}.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class ValueFactoryTypeAnalysisUnitTestCase extends TestCase
{
   private static final String METHOD = "method";
   private static final String OTHER_METHOD = "other";
   private static final String ONE = "1";
   private static final String TWO = "2";
   private static final String INTEGER = "java.lang.Integer";
   private static final String LONG = "java.lang.Long";
   private static final TypeInfo OBJECT_TYPE = new MockTypeInfo(Object.class);
   private static final TypeInfo STRING_TYPE = new MockTypeInfo(String.class);
   private static final TypeInfo INTEGER_TYPE = new MockTypeInfo(Integer.class);
   private static final TypeInfo LONG_TYPE = new MockTypeInfo(Long.class);
   
   private Set<MethodInfo> allMethods = new HashSet<MethodInfo>();
   /**
    * Create a new ValueFactoryTypeAnalysisUnitTestCase.
    * 
    * @param name
    */
   public ValueFactoryTypeAnalysisUnitTestCase(String name)
   {
      super(name);
   }


   @Override
   protected void tearDown() throws Exception
   {
      allMethods.clear();
      
      super.tearDown();
   }

   @SuppressWarnings("unchecked")
   public void testNoArgsMatch()
   {
      MethodInfo mi = new MockMethodInfo(METHOD, new TypeInfo[0]);
      allMethods.add(mi); 
      
      populateParameterTypes(allMethods, METHOD, Collections.EMPTY_LIST);
   }
   
   public void testFullySpecifiedMatch()
   {
      MethodInfo mi = new MockMethodInfo(METHOD, new TypeInfo[]{ INTEGER_TYPE, LONG_TYPE } );
      allMethods.add(mi); 
      
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, INTEGER);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(TWO, LONG);
      populateParameterTypes(allMethods, METHOD, Arrays.asList(one, two));
      
      assertEquals(INTEGER, one.getParameterTypeName());
      assertEquals(LONG, two.getParameterTypeName());
   }
   
   public void testPartialMatch()
   {
      MethodInfo mi = new MockMethodInfo(METHOD, new TypeInfo[]{ INTEGER_TYPE, LONG_TYPE } );
      allMethods.add(mi); 
      mi = new MockMethodInfo(METHOD, new TypeInfo[]{ INTEGER_TYPE } );
      allMethods.add(mi); 
      
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, null);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(TWO, LONG);
      populateParameterTypes(allMethods, METHOD, Arrays.asList(one, two));
      
      assertEquals(INTEGER, one.getParameterTypeName());
      assertEquals(LONG, two.getParameterTypeName());
   }
   
   public void testUnpecifiedMatch()
   {
      MethodInfo mi = new MockMethodInfo(METHOD, new TypeInfo[]{ INTEGER_TYPE, LONG_TYPE } );
      allMethods.add(mi); 
      mi = new MockMethodInfo(METHOD, new TypeInfo[]{ INTEGER_TYPE } );
      allMethods.add(mi); 
      
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, null);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(TWO, null);
      populateParameterTypes(allMethods, METHOD, Arrays.asList(one, two));
      
      assertEquals(INTEGER, one.getParameterTypeName());
      assertEquals(LONG, two.getParameterTypeName());
   }
   
   public void testFullySpecifiedMismatch()
   {
      MethodInfo mi = new MockMethodInfo(METHOD, new TypeInfo[]{ INTEGER_TYPE, INTEGER_TYPE } );
      allMethods.add(mi); 
      
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, INTEGER);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(TWO, LONG); 
      
      try
      {
         populateParameterTypes(allMethods, METHOD, Arrays.asList(one, two));
         fail("Should have thrown exception due to mismatch");
      }
      catch (IllegalArgumentException good) {}
   }
   
   public void testPartialMismatch()
   {
      MethodInfo mi = new MockMethodInfo(METHOD, new TypeInfo[]{ INTEGER_TYPE, INTEGER_TYPE } );
      allMethods.add(mi); 
      
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, null);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(TWO, LONG); 
      
      try
      {
         populateParameterTypes(allMethods, METHOD, Arrays.asList(one, two));
         fail("Should have thrown exception due to mismatch");
      }
      catch (IllegalArgumentException good) {}
   }

   @SuppressWarnings("unchecked")
   public void testNameMismatch()
   {
      MethodInfo mi = new MockMethodInfo(METHOD, new TypeInfo[0]);
      allMethods.add(mi); 
      
      try
      {
         populateParameterTypes(allMethods, OTHER_METHOD, Collections.EMPTY_LIST);
         fail("Should have thrown exception due to mismatch");
      }
      catch (IllegalArgumentException good) {}
   }

   public void testZeroParamsToOneParam()
   {
      MethodInfo mi = new MockMethodInfo(METHOD, new TypeInfo[0]);
      allMethods.add(mi); 
      
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE);
      
      try
      {
         populateParameterTypes(allMethods, METHOD, Arrays.asList(one));
         fail("Should have thrown exception due to mismatch");
      }
      catch (IllegalArgumentException good) {}
   }

   @SuppressWarnings("unchecked")
   public void testOneParamToZeroParams()
   {
      MethodInfo mi = new MockMethodInfo(METHOD, new TypeInfo[] { OBJECT_TYPE });
      allMethods.add(mi); 
      
      try
      {
         populateParameterTypes(allMethods, METHOD, Collections.EMPTY_LIST);
         fail("Should have thrown exception due to mismatch");
      }
      catch (IllegalArgumentException good) {}
   }

   public void testTwoParamsToOneParam()
   {
      MethodInfo mi = new MockMethodInfo(METHOD, new TypeInfo[]{ INTEGER_TYPE, LONG_TYPE } );
      allMethods.add(mi); 
      
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, INTEGER);
      
      try
      {
         populateParameterTypes(allMethods, METHOD, Arrays.asList(one));
         fail("Should have thrown exception due to mismatch");
      }
      catch (IllegalArgumentException good) {}
   }

   public void testOneParamToTwoParams()
   {
      MethodInfo mi = new MockMethodInfo(METHOD, new TypeInfo[] { INTEGER_TYPE });
      allMethods.add(mi); 
      
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, INTEGER);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(TWO, INTEGER);
      
      try
      {
         populateParameterTypes(allMethods, METHOD, Arrays.asList(one, two));
         fail("Should have thrown exception due to mismatch");
      }
      catch (IllegalArgumentException good) {}
   }
   
   public void testDoubleMatch()
   {
      MethodInfo mi = new MockMethodInfo(METHOD, new TypeInfo[]{ INTEGER_TYPE, LONG_TYPE } );
      allMethods.add(mi); 
      mi = new MockMethodInfo(METHOD, new TypeInfo[]{ INTEGER_TYPE, STRING_TYPE } );
      allMethods.add(mi); 
      
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, null);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(TWO, null); 
      
      try
      {
         populateParameterTypes(allMethods, METHOD, Arrays.asList(one, two));
         fail("Should have thrown exception due to mismatch");
      }
      catch (IllegalArgumentException good) {}
   }

}
