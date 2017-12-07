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
package org.jboss.test.aop.scopedextender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.aop.advice.Interceptor;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 85945 $
 */
public class TestUtil
{
   StringBuffer errors;
   
   public void invoked(Class interceptor)
   {
      boolean intercepted = false;
      if (interceptor == BasePerClassInterceptor.class)
      {
         intercepted = BasePerClassInterceptor.invoked;
      }
      else if (interceptor == BasePerInstanceInterceptor.class)
      {
         intercepted = BasePerInstanceInterceptor.invoked;
      }
      else if (interceptor == BasePerJoinPointInterceptor.class)
      {
         intercepted = BasePerJoinPointInterceptor.invoked;
      }
      else if (interceptor == BasePerClassJoinPointInterceptor.class)
      {
         intercepted = BasePerClassJoinPointInterceptor.invoked;         
      }
      
      if (!intercepted)
      {
         throw new RuntimeException(interceptor.getClass().getName() + " did not intercept");
      }
   }
   public void compare(int expected, int actual)
   {
      if (expected != actual) throw new RuntimeException("Expected " + expected + " but was " + actual);
   }
   
   public void compare(String method, String aspect, String[] expect, ArrayList actual)
   {
      List expected = Arrays.asList(expect);
      if (expected.size() != actual.size())
      {
         addError("Wrong number interceptions on " + aspect + " for " + method + " Expected=" + expected + "(size=" + expected.size() + ")" + " actual=" + actual + "(size=" + actual.size() + ")" );
         return;
      }
      
      for (int i = 0 ; i < expected.size() ; i++)
      {
         if (!expected.get(i).equals(actual.get(i)))
         {
            addError("Wrong interceptions on " + aspect + " for " + method + " Expected=" + expected  + "(size=" + expected.size() + ")" + " actual=" + actual + "(size=" + actual.size() + ")" );
            return;
         }
      }
   }
   
   private void addError(String s)
   {
      if (errors == null)
      {
         errors = new StringBuffer();
      }
      errors.append(s + "\n");
   }
   
   public String getErrors()
   {
      if (errors == null)
      {
         return null;
      }
      return errors.toString();
   }
}
