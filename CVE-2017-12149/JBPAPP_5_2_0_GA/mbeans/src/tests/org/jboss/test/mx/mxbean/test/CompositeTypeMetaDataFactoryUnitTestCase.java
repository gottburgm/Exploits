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

import javax.management.openmbean.CompositeType;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.mx.mxbean.CompositeTypeMetaData;
import org.jboss.mx.mxbean.CompositeTypeMetaDataFactory;
import org.jboss.test.mx.mxbean.support.CollectionsInterface;
import org.jboss.test.mx.mxbean.support.CompositeInterface;
import org.jboss.test.mx.mxbean.support.SimpleInterface;
import org.jboss.test.mx.mxbean.support.SimpleObject;

/**
 * CompositeTypeMetaDataFactoryUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class CompositeTypeMetaDataFactoryUnitTestCase extends AbstractMXBeanTest
{
   private static final CompositeType OBJECT_TYPE = CompositeTypeMetaData.generateObject();
   private static final CompositeType CLASS_TYPE = CompositeTypeMetaData.generateClass();
   private static final CompositeType CLASSLOADER_TYPE = CompositeTypeMetaData.generateClassLoader();
   
   public static Test suite()
   {
      return new TestSuite(CompositeTypeMetaDataFactoryUnitTestCase.class);
   }
   
   public CompositeTypeMetaDataFactoryUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testGetCompositeDataObject() throws Exception
   {
      CompositeType type = CompositeTypeMetaDataFactory.getCompositeType(Object.class);
      assertNotNull(type);
      assertEquals(OBJECT_TYPE, type);
   }
   
   public void testGetCompositeDataClass() throws Exception
   {
      CompositeType type = CompositeTypeMetaDataFactory.getCompositeType(Class.class);
      assertNotNull(type);
      assertEquals(CLASS_TYPE, type);
   }
   
   public void testGetCompositeDataClassLoader() throws Exception
   {
      CompositeType type = CompositeTypeMetaDataFactory.getCompositeType(ClassLoader.class);
      assertNotNull(type);
      assertEquals(CLASSLOADER_TYPE, type);
   }
   
   public void testSimpleInterfaceCompositeType() throws Exception
   {
      CompositeType type = CompositeTypeMetaDataFactory.getCompositeType(SimpleInterface.class);
      assertNotNull(type);
      assertEquals(createSimpleCompositeType(SimpleInterface.class), type);
   }
   
   public void testSimpleObjectCompositeType() throws Exception
   {
      CompositeType type = CompositeTypeMetaDataFactory.getCompositeType(SimpleObject.class);
      assertNotNull(type);
      assertEquals(createSimpleCompositeType(SimpleObject.class), type);
   }
   
   public void testCompositeInterfaceType() throws Exception
   {
      CompositeType type = CompositeTypeMetaDataFactory.getCompositeType(CompositeInterface.class);
      assertNotNull(type);
      CompositeType expected = createCompositeType(CompositeInterface.class.getName(), CompositeInterface.KEYS, CompositeInterface.TYPES);
      assertEquals(expected, type);
   }
   
   public void testCollectionsInterfaceType() throws Exception
   {
      CompositeType type = CompositeTypeMetaDataFactory.getCompositeType(CollectionsInterface.class);
      assertNotNull(type);
      CompositeType expected = createCompositeType(CollectionsInterface.class.getName(), CollectionsInterface.KEYS, CollectionsInterface.TYPES);
      assertEquals(expected, type);
   }
   
   protected CompositeType createSimpleCompositeType(Class clazz) throws Exception
   {
      return createCompositeType(clazz.getName(), SimpleInterface.KEYS, SimpleInterface.TYPES);
   }
}
