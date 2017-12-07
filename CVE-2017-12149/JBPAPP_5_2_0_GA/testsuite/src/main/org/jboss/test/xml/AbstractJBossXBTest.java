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
package org.jboss.test.xml;

import java.net.URL;

import org.jboss.test.AbstractTestCaseWithSetup;
import org.jboss.test.AbstractTestDelegate;
import org.jboss.util.Classes;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;

/**
 * AbstractJBossXBTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class AbstractJBossXBTest extends AbstractTestCaseWithSetup
{
   private static final XmlDiff DIFF = new XmlDiff();

   protected String rootName = getRootName();
   
   /**
    * Create a new AbstractJBossXBTest.
    * 
    * @param name the name of the test
    */
   public AbstractJBossXBTest(String name)
   {
      super(name);
   }

   public void assertXmlEqual(String expected, String was)
   {
      String diff = DIFF.diff(expected, was);
      if(diff != null)
      {
         fail(diff);
      }
   }

   /**
    * Unmarshal some xml
    * 
    * @param name the name
    * @param expected the expected type
    * @return the unmarshalled object
    * @throws Exception for any error
    */
   protected Object unmarshal(String name, Class expected) throws Exception
   {
      Object object = unmarshal(name);
      if (object == null)
         fail("No object from " + name);
      assertTrue("Object '" + object + "' cannot be assigned to " + expected.getName(), expected.isAssignableFrom(object.getClass()));
      return object;
   }
   
   /**
    * Unmarshal some xml
    * 
    * @param name the name
    * @param expected the expected type
    * @param resolver the resolver
    * @return the unmarshalled object
    * @throws Exception for any error
    */
   protected Object unmarshal(String name, Class expected, SchemaBindingResolver resolver) throws Exception
   {
      Object object = unmarshal(name, resolver);
      if (object == null)
         fail("No object from " + name);
      assertTrue("Object '" + object + "' cannot be assigned to " + expected.getName(), expected.isAssignableFrom(object.getClass()));
      return object;
   }
   
   /**
    * Unmarshal some xml
    * 
    * @param name the name
    * @return the unmarshalled object
    * @throws Exception for any error
    */
   protected Object unmarshal(String name) throws Exception
   {
      return unmarshal(name, (SchemaBindingResolver) null);
   }
   
   /**
    * Unmarshal some xml
    * 
    * @param name the name
    * @return the unmarshalled object
    * @throws Exception for any error
    */
   protected Object unmarshal(String name, SchemaBindingResolver resolver) throws Exception
   {
      String url = findXML(name);
      return getJBossXBDelegate().unmarshal(url, resolver);
   }
   
   /**
    * Find the xml
    * 
    * @param name the name
    * @return the url of the xml
    */
   protected String findXML(String name)
   {
      URL url = getResource(name);
      if (url == null)
         fail(name + " not found");
      return url.toString();
   }
   
   /**
    * Get a schema location
    * 
    * @param clazz used to get the package
    * @param schema the schema name
    * @return the location in the "classpath"
    */
   protected static String getSchemaLocation(Class clazz, String schema)
   {
      String packageName = Classes.getPackageName(clazz);
      packageName = packageName.replace('.', '/');
      String name = packageName + '/' + schema;
      return name;
   }

   /**
    * Setup the test delegate
    * 
    * @param clazz the class
    * @return the delegate
    * @throws Exception for any error
    */
   public static AbstractTestDelegate getDelegate(Class clazz) throws Exception
   {
      return new JBossXBTestDelegate(clazz);
   }

   protected JBossXBTestDelegate getJBossXBDelegate()
   {
      return (JBossXBTestDelegate) getDelegate();
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      configureLogging();
   }
   
   /**
    * Get the package root name
    * 
    * @return the root name
    */
   protected String getRootName()
   {
      String longName = getClass().getName();
      int dot = longName.lastIndexOf('.');
      if (dot != -1)
         return longName.substring(dot + 1);
      return longName;
   }
   
   protected void configureLogging()
   {
      //enableTrace("org.jboss.xb");
   }
}
