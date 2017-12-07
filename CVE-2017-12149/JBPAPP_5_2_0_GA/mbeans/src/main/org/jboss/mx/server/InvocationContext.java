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
package org.jboss.mx.server;

import java.util.ArrayList;
import java.util.List;

import javax.management.Descriptor;
import javax.management.MBeanParameterInfo;

import org.jboss.mx.interceptor.AbstractInterceptor;
import org.jboss.mx.interceptor.Interceptor;
import org.jboss.util.Classes;

/**
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 89133 $
 */
public class InvocationContext
{

   // Constants -----------------------------------------------------
   
   public final static String OP_INVOKE             = "invoke";
   public final static String OP_GETATTRIBUTE       = "getAttribute";
   public final static String OP_SETATTRIBUTE       = "setAttribute";
   public final static String OP_GETMBEANINFO       = "getMBeanInfo";

   public final static String OPERATION_IMPACT   = "operation.impact";
   public final static String ATTRIBUTE_ACCESS   = "attribute.access";

   /* Marker for void */
   private static final Class VOID = Void.class;

   public static final Class[] NOCLASSES = new Class[0];
   
   // Attributes ----------------------------------------------------

   private String attributeType = null;
   private String name = null;
   private String[] signature = null;
   private String returnType = null;
   private String type = null;
   private boolean isWritable = true;
   private boolean isReadable = true;

   List interceptors = null;
   transient Interceptor dispatcher = new NullDispatcher();
   transient Object target = null;
   transient Descriptor descriptor = null;
   transient AbstractMBeanInvoker invoker = null;
   transient Class attributeTypeClass = null;
   transient Class returnTypeClass = null;
   transient Class[] signatureClasses = null;

   // Public --------------------------------------------------------

   public final void copy(final InvocationContext src)
   {
      if (src == null)
         return;

      this.attributeType = src.attributeType;
      this.attributeTypeClass = src.attributeTypeClass;
      this.name = src.name;
      this.signature = src.signature;
      this.signatureClasses = src.signatureClasses;
      this.returnType = src.returnType;
      this.returnTypeClass = src.returnTypeClass;
      this.type = src.type;
      this.isWritable = src.isWritable;
      this.interceptors = src.interceptors;
      this.dispatcher = src.dispatcher;
      this.target = src.target;
      this.descriptor = src.descriptor;
      this.invoker = src.invoker;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String getType()
   {
      return type;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   void setSignature(String[] signature)
   {
      this.signature = signature;
   }

   void setSignature(MBeanParameterInfo[] signature)
   {
      this.signature = new String[signature.length];

      for (int i = 0; i < signature.length; ++i)
         this.signature[i] = signature[i].getType();

   }

   public String[] getSignature()
   {
      return signature;
   }
   
   public Class[] getSignatureClasses() throws ClassNotFoundException
   {
      if (signatureClasses != null)
         return signatureClasses;
      if (signature == null || signature.length == 0)
         return NOCLASSES;
      Class[] signatureClassesTemp = new Class[signature.length];
      for (int i = 0; i < signature.length; ++i)
         signatureClassesTemp[i] = TCLAction.UTIL.getContextClassLoader().loadClass(signature[i]);
      signatureClasses = signatureClassesTemp;
      return signatureClasses;
   }

   public void setAttributeType(String attrType)
   {
      this.attributeType = attrType;
      this.attributeTypeClass = null;
   }

   public String getAttributeType()
   {
      return attributeType;
   }

   public Class getAttributeTypeClass() throws ClassNotFoundException
   {
      if (attributeType == null)
         return null;
      if (attributeTypeClass != null)
         return attributeTypeClass;
      attributeTypeClass = loadClass(attributeType);
      return attributeTypeClass;
   }

   public void setReturnType(String returnType)
   {
      this.returnType = returnType;
      this.returnTypeClass = null;
   }

   public String getReturnType()
   {
      return returnType;
   }

   public Class getReturnTypeClass() throws ClassNotFoundException
   {
      if (returnType == null)
         return null;
      if (returnTypeClass == VOID)
         return null;
      if (returnTypeClass != null)
         return returnTypeClass;
      if (returnType.equals("void"))
      {
         returnTypeClass = VOID;
         return null;
      }
      else
         returnTypeClass = loadClass(returnType);
      return returnTypeClass;
   }

   public boolean isReadable()
   {
      return isReadable;
   }

   public void setReadable(boolean readable)
   {
      isReadable = readable;
   }

   public boolean isWritable()
   {
      return isWritable;
   }

   public void setWritable(boolean writable)
   {
      this.isWritable = writable;
   }

   public void setInterceptors(List interceptors)
   {
      // FIXME: make a copy
      this.interceptors = interceptors;
   }

   public List getInterceptors()
   {
      // FIXME: return a copy
      if (interceptors == null)
      {
         interceptors = new ArrayList();
      }
      return interceptors;
   }

   public void setDispatcher(Interceptor d)
   {
      this.dispatcher = d;
   }

   public Interceptor getDispatcher()
   {
      return dispatcher;
   }

   void setTarget(Object o)
   {
      this.target = o;
   }

   public Object getTarget()
   {
      return target;
   }

   public void setDescriptor(Descriptor d)
   {
      this.descriptor = d;
   }

   public Descriptor getDescriptor()
   {
      return descriptor;
   }

   public void setInvoker(AbstractMBeanInvoker mi)
   {
      this.invoker = mi;
   }

   public MBeanInvoker getInvoker()
   {
      return invoker;
   }

   /**
    * Print what's inside the InvocationContext
    */
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer(256);
      
      sbuf.append("InvocationContext[")
          .append(" name=").append(name)
          .append(", type=").append(type)           
          .append(", attributeType=").append(attributeType)
          .append(", isReadable=").append(isReadable)
          .append(", isWritable=").append(isWritable)
          .append(", returnType=").append(returnType);
      
      if (signature != null)
      {
         sbuf.append(", signature=[");
         for (int i = 0; i < signature.length; i++)
         {
            sbuf.append(" arg[").append(i).append("]=").append(signature[i]);
         }
         sbuf.append(" ] ]");
      }
      else
      {
         sbuf.append(", signature=null ]");
      }
      return sbuf.toString();
   }

   // Private -------------------------------------------------------
   
   private Class loadClass(String clazz) throws ClassNotFoundException
   {
      Class isPrimitive = Classes.getPrimitiveTypeForName(clazz);
      if (isPrimitive != null)
         return Classes.getPrimitiveWrapper(isPrimitive);
      ClassLoader cl = TCLAction.UTIL.getContextClassLoader();
      return cl.loadClass(clazz);
   }
   
   // Inner Class ---------------------------------------------------
   
   class NullDispatcher extends AbstractInterceptor
   {
      public NullDispatcher()
      {
         super("NullDispatcher");
      }
      
      public Object invoke(Invocation invocation) throws Throwable
      {
         return null;
      }
   }
}
