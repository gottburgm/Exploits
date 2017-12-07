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
package org.jboss.test.system.controller.configure.value.text.test;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.test.system.controller.AbstractControllerTest;
import org.jboss.test.system.controller.support.BrokenDynamicMBeanAttributeInfoTypeNotFound;
import org.jboss.test.system.controller.support.BrokenDynamicMBeanNoAttributeInfoType;
import org.jboss.test.system.controller.support.Simple;
import org.jboss.test.system.controller.support.SimpleMBean;
import org.jboss.util.NestedRuntimeException;

/**
 * TextValueTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class TextValueTest extends AbstractControllerTest
{
   private static final DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
   
   private static final String stringValue =  new String("StringValue");
   private static final Byte byteValue = new Byte("12");
   private static final Boolean booleanValue = Boolean.TRUE;
   // TODO character
   // private static final Character characterValue = new Character('a'); 
   private static final Short shortValue = new Short("123");
   private static final Integer integerValue = new Integer("1234");
   private static final Long longValue = new Long("12345");
   private static final Float floatValue = new Float("3.14");
   private static final Double doubleValue = new Double("3.14e12");
   private static final Date dateValue = createDate("Mon Jan 01 00:00:00 CET 2001");
   private static final BigDecimal bigDecimalValue = new BigDecimal("12e4");
   //private static final BigInteger bigIntegerValue = new BigInteger("123456");

   public TextValueTest(String name)
   {
      super(name);
   }
   
   public void testPropertyEditors() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      
      assertInstall(name);
      try
      {
         Simple test = getSimple();

         assertEquals(stringValue, test.getAString());
         assertEquals(byteValue, test.getAByte());
         assertEquals(booleanValue, test.getABoolean());
         // TODO character 
         // assertEquals(characterValue, test.getACharacter());
         assertEquals(shortValue, test.getAShort());
         assertEquals(integerValue, test.getAnInt());
         assertEquals(longValue, test.getALong());
         assertEquals(floatValue, test.getAFloat());
         assertEquals(doubleValue, test.getADouble());
         assertEquals(dateValue, test.getADate());
         assertEquals(bigDecimalValue, test.getABigDecimal());
         // TODO BigInteger
         //assertEquals(bigIntegerValue, test.getABigInteger());
         assertEquals(byteValue.byteValue(), test.getAbyte());
         assertEquals(booleanValue.booleanValue(), test.isAboolean());
         // TODO character
         // assertEquals(characterValue.charValue(), test.getAchar()); 
         assertEquals(shortValue.shortValue(), test.getAshort());
         assertEquals(integerValue.intValue(), test.getAnint());
         assertEquals(longValue.longValue(), test.getAlong());
         assertEquals(floatValue.floatValue(), test.getAfloat());
         assertEquals(doubleValue.doubleValue(), test.getAdouble());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testNoPropertyEditor() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, DeploymentException.class);
   }
   
   public void testNoAttributeInfoType() throws Exception
   {
      assertDeployFailure(BrokenDynamicMBeanNoAttributeInfoType.OBJECT_NAME, DeploymentException.class);
   }
   
   public void testAttributeInfoTypeNotFound() throws Exception
   {
      assertDeployFailure(BrokenDynamicMBeanAttributeInfoTypeNotFound.OBJECT_NAME, ClassNotFoundException.class);
   }

   private static Date createDate(String date)
   {
      try
      {
         return dateFormat.parse(date);
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }
}
