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
package org.jboss.test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Override the TestSuite to support the setting of a Properties collection
 * associated with the TestSuite on each test.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class JBossTestSuite extends TestSuite
{
   protected Properties props;

   public JBossTestSuite(Properties props)
   {
      this.props = props;
   }

   public JBossTestSuite(Class theClass, String name, Properties props)
   {
      if (name == null)
         name = theClass.getName();
      super.setName(name);
      this.props = props;

      Class superClass = theClass;
      while (Test.class.isAssignableFrom(superClass))
      {
         Method[] methods = superClass.getDeclaredMethods();
         Method setProps = null;
         try
         {
            Class[] sig = {Properties.class};
            setProps = superClass.getMethod("setProps", sig);
         }
         catch (Throwable ignore)
         {
         }
         for (int i = 0; i < methods.length; i++)
         {
            Method m = methods[i];
            if( isPublicTestMethod(m) == false )
               continue;
            String testName = m.getName();
            Test test = createTest(theClass, testName);
            if (setProps != null)
            {
               Object[] args = {props};
               try
               {
                  setProps.invoke(test, args);
               }
               catch (Throwable t)
               {
                  test = failure(t);
               }
            }
            super.addTest(test);
         }
         superClass = superClass.getSuperclass();
      }
   }

   public JBossTestSuite(Class theClass, Properties props)
   {
      this(theClass, null, props);
   }

   public JBossTestSuite(String name, Properties props)
   {
      super(name);
      this.props = props;
   }

   public void addTestSuite(Class testClass)
   {
      super.addTest(new JBossTestSuite(testClass, props));
   }

   public static boolean isPublicTestMethod(Method m)
   {
      return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
   }

   public static boolean isTestMethod(Method m)
   {
      String name = m.getName();
      Class[] parameters = m.getParameterTypes();
      Class returnType = m.getReturnType();
      return parameters.length == 0 && name.startsWith("test")
         && returnType.equals(Void.TYPE);
   }

   /**
    * Returns a test which will fail and log a warning message.
    */
   private static Test failure(final Throwable t)
   {
      return new TestCase("failure")
      {
         protected void runTest()
         {
            fail(t.getMessage());
         }
      };
   }
}
