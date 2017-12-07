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
package org.jboss.test.jbossmx.performance.dynamic.support;

import javax.management.*;

/**
 * Dynamic MBean with a single void management operation.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81036 $
 *   
 */
public class Dyn
         implements DynamicMBean
{

   private int counter = 0;

   public Object getAttribute(String attribute)
   throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      return null;
   }

   public void setAttribute(Attribute attribute)
   throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
      {}

   public AttributeList getAttributes(String[] attributes)
   {
      return null;
   }

   public AttributeList setAttributes(AttributeList attributes)
   {
      return null;
   }

   public Object invoke(String actionName, Object[] params, String[] signature)
   throws MBeanException, ReflectionException
   {
      if (actionName.equals("methodInvocation"))
      {
         methodInvocation();
         return null;
      }

      else if (actionName.equals("counter"))
      {
         countInvocation();
         return null;
      }

      else if (actionName.equals("mixedArguments"))
      {
         myMethod((Integer)params[0], ((Integer)params[1]).intValue(),
                  (Object[][][])params[2], (Attribute)params[3]);
      
         return null;
      }
      
      return null;
   }

   public MBeanInfo getMBeanInfo()
   {

      return new MBeanInfo(
                "test.performance.dynamic.support.Dynamic", "",
                null,
                null,
                new MBeanOperationInfo[] { 
                     new MBeanOperationInfo(
                           "methodInvocation", "",
                           null, void.class.getName(), 0)
                     ,
                     new MBeanOperationInfo(
                           "counter", "",
                           null, void.class.getName(), 0)
                     },      
                null
             );
   }

   private void methodInvocation()
   {}

   private void countInvocation()
   {
      ++counter;
   }

   public void myMethod(Integer int1, int int2, Object[][][] space, Attribute attr)
   {
      
   }
   
   public int getCount()
   {
      return counter;
   }
}




