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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.system.metadata.ServiceValueFactoryParameterMetaData;
import org.jboss.system.metadata.ServiceValueMetaData;
import org.jboss.test.system.metadata.value.AbstractValueTest;

/**
 * Tests handling of a value-factory element inside an attribute.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class ValueFactoryParsingUnitTestCase extends AbstractValueTest
{
   private static final String PARAMETER = "parameter";
   private static final String DEFAULT = "default";
   private static final String ONE = "1";
   private static final String TWO = "2";
   private static final String NUMBER = "java.lang.Number";
   private static final String INTEGER = "java.lang.Integer";
   private static final String LONG = "java.lang.Long";
   private static final ServiceValueFactoryParameterMetaData PARAMETER_MD = new ServiceValueFactoryParameterMetaData(PARAMETER, null, null);
   private static final List<ServiceValueFactoryParameterMetaData> SIMPLE_LIST = Arrays.asList(PARAMETER_MD);
   private static final String UNFIXED = " ${valuefactory.test.property:1} ";
   
   public ValueFactoryParsingUnitTestCase(String name)
   {
      super(name);
   }

   public void testBasicValueFactory() throws Exception
   {
      ServiceValueMetaData value = unmarshallSingleValue();
      assertValueFactoryValue(value, SIMPLE_LIST, DEFAULT);
   }

   @SuppressWarnings("unchecked")
   public void testMinimal() throws Exception
   {
      ServiceValueMetaData value = unmarshallSingleValue();
      assertValueFactoryValue(value, Collections.EMPTY_LIST, null);
   }

   public void testNestedBean() throws Exception
   {
      try
      {
         unmarshallSingleValue();
         fail("Should not be able to handle a nested bean element");
      }
      catch (Exception expected) {}
   }

   public void testNoDefault() throws Exception
   {
      ServiceValueMetaData value = unmarshallSingleValue();
      assertValueFactoryValue(value, SIMPLE_LIST, null);
   }

   @SuppressWarnings("unchecked")
   public void testNoParameter() throws Exception
   {
      ServiceValueMetaData value = unmarshallSingleValue();
      assertValueFactoryValue(value, Collections.EMPTY_LIST, DEFAULT);
   }

   public void testNullParameter() throws Exception
   {
      ServiceValueMetaData value = unmarshallSingleValue();
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(null, null, null);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(TWO, null, null);
      assertValueFactoryValue(value, Arrays.asList(one, two), DEFAULT);
   }

   public void testElementParameter() throws Exception
   {
      ServiceValueMetaData value = unmarshallSingleValue();
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData("<element/>", null, null);
      assertValueFactoryValue(value, Arrays.asList(one), DEFAULT);
   }

   public void testState() throws Exception
   {
      ServiceValueMetaData value = unmarshallSingleValue();
      assertValueFactoryValue(value, SIMPLE_LIST, DEFAULT, ControllerState.CREATE);
   }

   public void testTypedParameters() throws Exception
   {
      ServiceValueMetaData value = unmarshallSingleValue();
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, INTEGER, null);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(TWO, LONG, null);
      assertValueFactoryValue(value, Arrays.asList(one, two), DEFAULT);
   }

   public void testTypedParametersWithValue() throws Exception
   {
      ServiceValueMetaData value = unmarshallSingleValue();
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, INTEGER, null);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(TWO, NUMBER, LONG);
      
      assertValueFactoryValue(value, Arrays.asList(one, two), DEFAULT);
   }

   public void testUntypedParameters() throws Exception
   {
      ServiceValueMetaData value = unmarshallSingleValue();
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, null, null);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(TWO, null, null);
      assertValueFactoryValue(value, Arrays.asList(one, two), DEFAULT);
   }

   public void testUntypedParametersWithValue() throws Exception
   {
      ServiceValueMetaData value = unmarshallSingleValue();
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, null, null);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(TWO, null, LONG);
      
      assertValueFactoryValue(value, Arrays.asList(one, two), DEFAULT);
   }
   
   public void testTrimAndReplace() throws Exception
   {
      ServiceValueMetaData value = unmarshallSingleValue();
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, null, null);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(UNFIXED, null, null);
      
      assertValueFactoryValue(value, Arrays.asList(one, two), DEFAULT);
      
   }
   
   public void testTrimAndReplaceWithValue() throws Exception
   {
      ServiceValueMetaData value = unmarshallSingleValue();
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, null, INTEGER);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(UNFIXED, null, null);
      
      assertValueFactoryValue(value, Arrays.asList(one, two), DEFAULT);
      
   }
   
   public void testTrimAndReplaceWithValueAndOverride() throws Exception
   {
      ServiceValueMetaData value = unmarshallSingleValue();
      ServiceValueFactoryParameterMetaData one = new ServiceValueFactoryParameterMetaData(ONE, null, INTEGER);
      ServiceValueFactoryParameterMetaData two = new ServiceValueFactoryParameterMetaData(ONE, null, null);
      
      assertValueFactoryValue(value, Arrays.asList(one, two), DEFAULT);
      
   }
   
   
}
