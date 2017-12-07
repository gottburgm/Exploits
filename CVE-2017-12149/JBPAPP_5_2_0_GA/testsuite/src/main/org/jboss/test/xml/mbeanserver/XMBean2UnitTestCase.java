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
package org.jboss.test.xml.mbeanserver;

import java.net.URL;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Properties;

import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultSchemaResolver;
import org.jboss.test.xml.AbstractJBossXBTest;
import org.jboss.test.xml.mbeanserver.interceptors.SomeBeanInterceptor;
import org.jboss.mx.metadata.xb.ModelMBeanInfoSupportWrapper;
import org.jboss.mx.interceptor.Interceptor;
import org.jboss.mx.interceptor.PersistenceInterceptor2;
import org.jboss.mx.interceptor.ModelMBeanInterceptor;
import org.jboss.mx.interceptor.ObjectReferenceInterceptor;

import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanInfo;

/**
 * Test unmarshalling xml documents conforming to jboss_xmbean_2_0.xsd
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 113110 $
 */
public class XMBean2UnitTestCase
   extends AbstractJBossXBTest
{
   public XMBean2UnitTestCase(String name)
   {
      super(name);
   }

   public void testJavaBeanSchemaInitializerInterceptor() throws Exception
   {
      DefaultSchemaResolver resolver = new DefaultSchemaResolver();
      JavaBeanSchemaInitializer si = new JavaBeanSchemaInitializer();
      resolver.addSchemaInitializer("urn:jboss:simplejavabean:1.0", si);
      resolver.addSchemaLocation("urn:jboss-test:xmbean:2.0", "xml/mbeanserver/jboss_xmbean_2_0.xsd");
      resolver.addSchemaLocation("urn:jboss:simplejavabean:1.0", "xml/mbeanserver/simplejavabean_1_0.xsd");

      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      String xmlPath = getResourcePath("xml/mbeanserver/testXMBean2.xml");
      Object root = unmarshaller.unmarshal(xmlPath, resolver);

      assertInstanceOf( root,ModelMBeanInfoSupportWrapper.class);
      ModelMBeanInfoSupportWrapper mbean = (ModelMBeanInfoSupportWrapper) root;
      Descriptor descriptor = mbean.getDescriptors();
      Object i = descriptor.getFieldValue("interceptors");
      Interceptor[] interceptors = (Interceptor[]) i;
      SomeBeanInterceptor sbi = (SomeBeanInterceptor) interceptors[0];
      assertTrue(sbi.isFlag());
      assertEquals("Bean interceptor aclass", Integer.class,sbi.getaClass());

      URL homePage = new URL("http://www.jboss.org/");
      assertEquals("homePage",homePage, sbi.getHomePage());
      Long l = new Long(123456789);
      assertEquals("aLong", l,sbi.getaLong());
      assertEquals("aString", "string1",sbi.getaString());
      assertEquals("anInt",1234, sbi.getAnInt());
      InetAddress localhost = InetAddress.getByName("127.0.0.1");
      assertEquals("address", localhost,sbi.getAddress());
      String[] strings = {"string1", "string2", "string3"};
      assertTrue("someStrings == {string1, string2, string3}",
         Arrays.equals(strings, sbi.getSomeStrings()));
      Properties someProperties = new Properties();
      someProperties.setProperty("prop1", "value1");
      someProperties.setProperty("prop2", "value2");
      someProperties.setProperty("prop3", "value3");
      assertEquals("someProperties", someProperties,sbi.getSomeProperties());

      Object i1 = interceptors[1];
      assertInstanceOf(i1, PersistenceInterceptor2.class);
      Object i2 = interceptors[2];
      assertInstanceOf(i2,ModelMBeanInterceptor.class);
      Object i3 = interceptors[3];
      assertInstanceOf(i3 , ObjectReferenceInterceptor.class);

      String clazz = mbean.getClassName();
      assertEquals("Classname",org.jboss.naming.JNDIBindingService.class.getName(),clazz);


      ModelMBeanInfo info = mbean.getMBeanInfo();
      MBeanAttributeInfo[] attrs = info.getAttributes();
      assertTrue("There are 2 attributes", attrs.length == 2);
      MBeanAttributeInfo rn = info.getAttribute("RootName");
      assertNotNull(rn);
      assertEquals("RootName.name", rn.getName(), "RootName");
      assertEquals("RootName.type", rn.getType(), "java.lang.String");
      MBeanAttributeInfo bindings = info.getAttribute("Bindings");
      assertNotNull(bindings);
      assertEquals("Bindings.name", bindings.getName(), "Bindings");
      assertEquals("Bindings.type", bindings.getType(), "org.jboss.naming.JNDIBindings");

      MBeanConstructorInfo[] ctors = info.getConstructors();
      assertEquals("ctors length", ctors.length, 1);
      assertEquals("description",
         "An xmbean description with custom interceptors that are handled by the JavaBeanSchemaInitializer",
         info.getDescription().trim());
      MBeanNotificationInfo[] notices = info.getNotifications();
      assertEquals("notices length", notices.length, 1);
      assertEquals("notices[0].name", notices[0].getName(), "bindEvent");
      assertEquals("notices[0].description",
         notices[0].getDescription(), "The bind event notification");
      String[] types = {"org.jboss.naming.JNDIBindingService.bindEvent"};
      assertEquals("notices[0].types",
         notices[0].getNotifTypes(), types);
      MBeanOperationInfo[] ops = info.getOperations();
      assertEquals("ops length", ops.length, 2);
      assertEquals("ops[0].name", ops[0].getName(), "start");
      assertEquals("ops[1].name", ops[1].getName(), "stop");
   }

   // Private

   private String getResourcePath(String path)
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(path);
      if(url == null)
      {
         fail("URL not found: " + path);
      }
      return url.toString();
   }
}
