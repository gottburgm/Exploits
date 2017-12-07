/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.invocation;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.proxy.ClientContainer;
import org.jboss.remoting.serialization.IMarshalledValue;
import org.jboss.remoting.serialization.impl.jboss.LocalMarshalledValue;
import org.jboss.serial.objectmetamodel.safecloning.SafeClone;
import org.jboss.serial.objectmetamodel.safecloning.SafeCloningRepository;


/** 
 * This MarshallingInvokerInterceptor uses JbossSerialization DataContainer's doing a faster serialization over call-by-values than For Marshalling local call-by-values using JBossSerialization
 * @author <mailto="clebert.suconic@jboss.com">Clebert Suconic</a> 
 * */
public class DataContainerMarshallingInvokerInterceptor extends MarshallingInvokerInterceptor
{
	private static final long serialVersionUID = -1889397492156790576L;

	
	  /** These objects are safe to reuse in callByValue operations */
	   static final SafeClone safeToReuse = new SafeClone(){
	                 public boolean isSafeToReuse(Object obj) {
	                     if (obj==null)
	                     {
	                         return false;
	                     }

	                     if (obj instanceof ClientContainer ||
	                         obj instanceof String ||
	                         obj instanceof Number ||
	                         obj instanceof BigDecimal ||
	                         obj instanceof BigInteger ||
	                         obj instanceof Byte ||
	                         obj instanceof Double ||
	                         obj instanceof Float ||
	                         obj instanceof Integer ||
	                         obj instanceof Long ||
	                         obj instanceof Short)
	                     {
	                         return true;
	                     }
	                     else
	                     {
	                         return false;
	                     }
	                 }
	             };


	   protected IMarshalledValue createMarshalledValueForCallByValue(Object value) throws IOException
	   {
		   return new LocalMarshalledValue(value,new SafeCloningRepository(safeToReuse));

	   }
	
}
