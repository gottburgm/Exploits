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

import org.jboss.logging.Logger;
import org.jboss.util.UnexpectedThrowable;

/**
 * A SystemTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class AbstractSystemTest extends AbstractTestCaseWithSetup
{
   private static Logger staticLog = Logger.getLogger(AbstractSystemTest.class);
   
   // @fixme move to AbstractTestCase
   public static void checkThrowableDeep(Class<? extends Throwable> expected, Throwable throwable) throws Exception
   {
      assertNotNull(expected);
      assertNotNull(throwable);
      
      Throwable original = throwable;
      
      while (throwable.getCause() != null)
         throwable = throwable.getCause();
      
      if (expected.equals(throwable.getClass()) == false)
      {
         if (original instanceof Exception)
            throw (Exception) original;
         else if (original instanceof Error)
            throw (Error) original;
         else
            throw new UnexpectedThrowable("UnexpectedThrowable", original);
      }
      else
      {
         staticLog.debug("Got expected " + expected.getName() + "(" + throwable + ")");
      }
   }
   
   // @fixme move to AbstractTestCase
   public static <T> T assertInstanceOf(Class<T> expected, Object object) throws Exception
   {
      if (object == null)
         return null;
      assertTrue(object.getClass(). getName() + " is not an instance of " + expected.getName(), expected.isInstance(object));
      return expected.cast(object);
   }
   

   /**
    * Create a new ContainerTest.
    * 
    * @param name the test name
    */
   public AbstractSystemTest(String name)
   {
      super(name);
   }
   
   /**
    * Default setup with security manager enabled
    * 
    * @param clazz the class
    * @return the delegate
    * @throws Exception for any error
    */
   public static AbstractTestDelegate getDelegate(Class clazz) throws Exception
   {
      AbstractTestDelegate delegate = new AbstractTestDelegate(clazz);
      delegate.enableSecurity = true;
      return delegate;
   }
}
