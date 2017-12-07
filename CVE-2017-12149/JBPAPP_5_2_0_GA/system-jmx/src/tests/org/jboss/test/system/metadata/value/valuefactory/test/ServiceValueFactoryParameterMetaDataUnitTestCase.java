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

import static org.jboss.system.metadata.ServiceValueFactoryParameterMetaData.getValue;

import java.beans.PropertyEditor;

import org.jboss.common.beans.property.ElementEditor;
import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.deployers.spi.DeploymentException;
import org.w3c.dom.Element;

import junit.framework.TestCase;

/**
 * A ServiceValueFactoryParameterMetaDataUnitTestCase.
 * 
 * @author Brian Stansberry
 * @version $Revision: 113110 $
 */
public class ServiceValueFactoryParameterMetaDataUnitTestCase extends TestCase
{
   private static final String ONE = "1";
   private static final String TRUE = "TRUE";
   private static final String BOOL = "boolean";
   private static final String BOOLEAN = "java.lang.Boolean";
   private static final String INT = "int";
   private static final String INTEGER = "java.lang.Integer";
   private static final String STRING = "java.lang.String";
   private static final String HASH_MAP = "java.util.HashMap";
   private static final String ATTR = "Attr";
   private static final String ELEMENT = Element.class.getName();
   private static final String ELEMENT_ATTR = "<element/>";
   
   private PropertyEditor existingElementEditor;

   /**
    * Create a new ServiceValueFactoryParameterMetaDataUnitTestCase.
    * 
    * @param name
    */
   public ServiceValueFactoryParameterMetaDataUnitTestCase(String name)
   {
      super(name);
   }
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      existingElementEditor = PropertyEditorFinder.getInstance().find(Element.class);
      if (existingElementEditor == null)
          PropertyEditorFinder.getInstance().register(Element.class, ElementEditor.class);
   }

   @Override
   protected void tearDown() throws Exception
   {
      if (existingElementEditor == null)
          PropertyEditorFinder.getInstance().register(Element.class, null);
      
      super.tearDown();
   }



   public void testGetValueNull() throws Exception
   {
      assertNull(getValue(Thread.currentThread().getContextClassLoader(), null, STRING, ATTR));
   }
   
   public void testGetValueInt() throws Exception
   {
      assertEquals(1, getValue(Thread.currentThread().getContextClassLoader(), ONE, INT, ATTR));
   }
   
   public void testGetValueInteger() throws Exception
   {
      assertEquals(new Integer(1), getValue(Thread.currentThread().getContextClassLoader(), ONE, INTEGER, ATTR));
   }
   
   public void testGetValueBool() throws Exception
   {
      assertEquals(true, getValue(Thread.currentThread().getContextClassLoader(), TRUE, BOOL, ATTR));
   }
   
   public void testGetValueBoolean() throws Exception
   {
      assertEquals(Boolean.TRUE, getValue(Thread.currentThread().getContextClassLoader(), TRUE, BOOLEAN, ATTR));
   }
   
   public void testGetValueString() throws Exception
   {
      assertEquals(ONE, getValue(Thread.currentThread().getContextClassLoader(), ONE, STRING, ATTR));
   }
   
   public void testGetValueElement() throws Exception
   {  
      Element result = (Element) getValue(Thread.currentThread().getContextClassLoader(), ELEMENT_ATTR, ELEMENT, ATTR);
      assertEquals("element", result.getNodeName());
   }
   
   public void testGetValueNoPropertyEditor()
   {
      try
      {
         getValue(Thread.currentThread().getContextClassLoader(), ONE, HASH_MAP, ATTR);
         fail("Should not have a property editor for HashMap");
      }
      catch (DeploymentException expected) {}
   }
   
   public void testGetValueUnknownType()
   {
      try
      {
         getValue(Thread.currentThread().getContextClassLoader(), ONE, "com.foo.Bar", ATTR);
         fail("Should not succeed with bogus type");
      }
      catch (DeploymentException expected) {}
   }

}
