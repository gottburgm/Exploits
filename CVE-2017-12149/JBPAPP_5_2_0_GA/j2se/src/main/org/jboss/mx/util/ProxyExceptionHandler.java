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
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $
 */
public interface ProxyExceptionHandler
{
   public Object handleInstanceNotFound(ProxyContext ctx, InstanceNotFoundException e, Method m, Object[] args) throws Exception;
   
   public Object handleAttributeNotFound(ProxyContext ctx, AttributeNotFoundException e, Method m, Object[] args) throws Exception;
   
   public Object handleInvalidAttributeValue(ProxyContext ctx, InvalidAttributeValueException e, Method m, Object[] args) throws Exception;
   
   public Object handleMBeanException(ProxyContext ctx, MBeanException e, Method m, Object[] args) throws Exception;
   
   public Object handleReflectionException(ProxyContext ctx, ReflectionException e, Method m, Object[] args) throws Exception;
   
   public Object handleRuntimeOperationsException(ProxyContext ctx, RuntimeOperationsException e, Method m, Object[] args) throws Exception;
   
   public Object handleRuntimeMBeanException(ProxyContext ctx, RuntimeMBeanException e, Method m, Object[] args) throws Exception;
   
   public Object handleRuntimeError(ProxyContext ctx, RuntimeErrorException e, Method m, Object[] args) throws Exception;
}
      



