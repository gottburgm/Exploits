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
package org.jboss.mx.util;

import java.lang.reflect.Method;

import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeErrorException;

/**
 * Default exception handler for MBean proxy.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $
 *   
 */
public class DefaultExceptionHandler
      implements ProxyExceptionHandler
{

   // InstanceNotFound, AttributeNotFound and InvalidAttributeValue
   // are not exceptions declared in the mgmt interface and therefore
   // must be rethrown as runtime exceptions to avoid UndeclaredThrowable
   // exceptions on the client

   public Object handleInstanceNotFound(ProxyContext ctx,
                                        InstanceNotFoundException e,
                                        Method m, Object[] args)
                                        throws Exception
   {
      throw new RuntimeProxyException("Instance not found: " + e.toString());
   }
   
   public Object handleAttributeNotFound(ProxyContext ctx,
                                         AttributeNotFoundException e,
                                         Method m, Object[] args)
                                         throws Exception
   {
      throw new RuntimeProxyException("Attribute not found: " + e.toString());
   }
   
   public Object handleInvalidAttributeValue(ProxyContext ctx,
                                             InvalidAttributeValueException e,
                                             Method m, Object[] args)
                                             throws Exception
   {
      throw new RuntimeProxyException("Invalid attribute value: " + e.toString());
   }
   
   public Object handleMBeanException(ProxyContext ctx, MBeanException e,
                                      Method m, Object[] args)
                                      throws Exception
   {
      // assuming MBeanException only wraps mgmt interface "application" 
      // exceptions therefore we can safely rethrow the target exception
      // as its declared in the mgmt interface
      throw e.getTargetException();
   }
   
   public Object handleReflectionException(ProxyContext ctx,
                                           ReflectionException e,
                                           Method m, Object[] args)
                                           throws Exception
   {
      // use of reflection exception is inconsistent in the API so the 
      // safest bet is to rethrow a runtime exception
      
      Exception target = e.getTargetException();
      if (target instanceof RuntimeException)
         throw target;
      else
         throw new RuntimeProxyException(target.toString());
   }
   
   public Object handleRuntimeOperationsException(ProxyContext ctx,
                                                  RuntimeOperationsException e,
                                                  Method m, Object[] args)
                                                  throws Exception
   {
      // target is always a runtime exception, so its ok to throw it from here
      throw e.getTargetException();
   }
   
   public Object handleRuntimeMBeanException(ProxyContext ctx,
                                             RuntimeMBeanException e,
                                             Method m, Object[] args)
                                             throws Exception
   {
      // target is always a runtime exception, so its ok to throw it from here
      throw e.getTargetException();
   }
   
   public Object handleRuntimeError(ProxyContext ctx, RuntimeErrorException e,
                                    Method m, Object[] args)
                                    throws Exception
   {
      // just unwrap and throw the actual error
      throw e.getTargetError();
   }
   
}
      



