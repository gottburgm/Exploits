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
package org.jboss.test.jmx.compliance.server.support;

/**
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 81019 $
 */
public class InvocationHandlerTest
   implements InvocationHandlerTestMBean
{
   String attribute = "Attribute";

   public boolean invokeNoArgsNoReturnInvoked = false;
   public boolean invokeNoReturnInvoked = false;

   public boolean primitive;
   public Boolean type;

   public String getAttribute()
   {
      return attribute;
   }

   public void setAttribute(String attribute)
   {
      this.attribute = attribute;
   }

   public boolean isIsPrimitive()
   {
      return primitive;
   }

   public void setIsPrimitive(boolean bool)
   {
      this.primitive = bool;
   }

   public Boolean getType()
   {
      return type;
   }

   public void setType(Boolean bool)
   {
      this.type = bool;
   }

   public void invokeNoArgsNoReturn()
   {
      invokeNoArgsNoReturnInvoked = true;
   }

   public String invokeNoArgs()
   {
      return("invokeNoArgs");
   }

   public void invokeNoReturn(String parameter)
   {
      invokeNoReturnInvoked = true;
   }

   public String invoke(String parameter)
   {
      return parameter;
   }

   public Object invokeMixedParameters(String parameter1, int parameter2, Object parameter3)
   {
      return parameter3;
   }
}