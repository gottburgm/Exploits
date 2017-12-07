/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.system.metadata.annotation;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.management.ObjectName;

import org.jboss.annotation.factory.AnnotationCreator;
import org.jboss.system.metadata.ServiceAnnotationMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.test.system.metadata.test.AbstractMetaDataTest;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class AnnotationUnitTestCase extends AbstractMetaDataTest
{
   public AnnotationUnitTestCase(String name)
   {
      super(name);
   }

   public void testCompTypeAnno() throws Exception
   {
      compTypeAnno();
   }

   protected void compTypeAnno() throws Exception
   {
      ServiceMetaData metaData = unmarshalSingleMBean();
      String exp = "@org.jboss.test.system.metadata.annotation.CompType(type=\"DS\",subtype=\"LocalTX\")";
      assertAnnotation(metaData, exp, CompType.class);
      assertOthers(metaData);
   }

   protected void assertAnnotation(ServiceMetaData metaData, String exp, Class c)
      throws Exception
   {
      List<ServiceAnnotationMetaData> annotations = metaData.getAnnotations();
      assertEquals("annotations.size", 1, annotations.size());
      ServiceAnnotationMetaData amd = annotations.get(0);
      assertNotNull(amd);
      String amdExp = amd.getAnnotation();
      assertEquals(exp, amdExp);
      Object expAnn = AnnotationCreator.createAnnotation(exp, c);
      Annotation ann = amd.getAnnotationInstance();
      assertEquals(expAnn, ann);
   }
   protected void assertOthers(ServiceMetaData metaData) throws Exception
   {
      assertEquals(testBasicMBeanName, metaData.getObjectName());
      assertEquals(testBasicMBeanCode, metaData.getCode());
      assertNull(metaData.getInterfaceName());
      assertDefaultConstructor(metaData);
      assertNoAttributes(metaData);
      assertNoXMBean(metaData);
   }

}
