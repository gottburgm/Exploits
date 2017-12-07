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
package org.jboss.invocation.pooled.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;
import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/**
 * An ObjectInputStream subclass used by the MarshalledValue class to
 * ensure the classes and proxies are loaded using the thread context
 * class loader.
 *
 * @author Scott.Stark@jboss.org
 * @author Clebert.Suconic@jboss.org
 * @version $Revision: 81030 $
 */
public class OptimizedObjectInputStream
        extends ObjectInputStream
{
   /** A class wide cache of proxy classes populated by resolveProxyClass */
   private static Map classCache;
   private static ConcurrentReaderHashMap objectStreamClassCache;
   private static Method lookupStreamClass = null;

   static
   {
      useClassCache(true);
      try
      {
         lookupStreamClass = ObjectStreamClass.class.getDeclaredMethod("lookup", new Class[]{Class.class, boolean.class});
         lookupStreamClass.setAccessible(true);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   /** Enable local caching of resolved proxy classes. This can only be used
    * if there is a single ULR and no redeployment of the proxy classes.
    *
    * @param flag true to enable caching, false to disable it
    */
   public static void useClassCache(boolean flag)
   {
      if (flag == true)
      {
         classCache = Collections.synchronizedMap(new WeakHashMap());
         objectStreamClassCache = new ConcurrentReaderHashMap();
      }
      else
      {
         classCache = null;
         objectStreamClassCache = null;
      }
   }

   /** Clear the current proxy cache.
    *
    */
   public static void flushClassCache()
   {
      classCache.clear();
      objectStreamClassCache.clear();
   }

   private static Class forName(String className) throws ClassNotFoundException
   {
      Class clazz = null;

      if (classCache != null)
      {

    	 ConcurrentHashMap subCache = (ConcurrentHashMap )classCache.get(Thread.currentThread().getContextClassLoader());
    	 if (subCache==null)
    	 {
    		 classCache.put(Thread.currentThread().getContextClassLoader(), new ConcurrentHashMap());
    		 subCache = (ConcurrentHashMap )classCache.get(Thread.currentThread().getContextClassLoader());
    	 }

         WeakReference ref = (WeakReference) subCache.get(className);
         if (ref != null)
         {
            clazz = (Class) ref.get();
         }
         if (clazz == null)
         {
            if (ref != null) subCache.remove(className);
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try
            {
               clazz = loader.loadClass(className);
            }
            catch (ClassNotFoundException e)
            {
               /* Use the Class.forName call which will resolve array classes. We
               do not use this by default as this can result in caching of stale
               values across redeployments.
               */
               clazz = Class.forName(className, false, loader);
            }
            subCache.put(className, new WeakReference(clazz));
         }
      }
      else
      {
         clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
      }
      return clazz;
   }

   /**
    * Creates a new instance of MarshalledValueOutputStream
    */
   public OptimizedObjectInputStream(InputStream is) throws IOException
   {
      super(is);
   }

   protected static ObjectStreamClass lookup(Class clazz)
   {
      Object[] args = {clazz, Boolean.TRUE};
      try
      {
         return (ObjectStreamClass) lookupStreamClass.invoke(null, args);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
      return null;
   }

   protected ObjectStreamClass readClassDescriptor()
           throws IOException, ClassNotFoundException
   {
	  if (CompatibilityVersion.pooledInvokerLegacy)
	  {
	      String className = readUTF();
	      ObjectStreamClass osc = null;
	      if (objectStreamClassCache != null)
	      {
	         osc = (ObjectStreamClass) objectStreamClassCache.get(className);
	      }
	      if (osc == null)
	      {
	         Class clazz = forName(className);
	         osc = ObjectStreamClass.lookup(clazz);
	         if (osc == null) osc = lookup(clazz);
	         if (osc == null) throw new IOException("Unable to readClassDescriptor for class " + className);
	         if (objectStreamClassCache != null) objectStreamClassCache.put(className, osc);
	      }
	      return osc;
	  }
	  else
	  {
		  return super.readClassDescriptor();
	  }
   }

   /**
    * Use the thread context class loader to resolve the class
    *
    * @throws IOException   Any exception thrown by the underlying OutputStream.
    */
   protected Class resolveClass(ObjectStreamClass v)
           throws IOException, ClassNotFoundException
   {
      String className = v.getName();
      return forName(className);
   }

   protected Class resolveProxyClass(String[] interfaces)
           throws IOException, ClassNotFoundException
   {
      // Load the interfaces from the cache or thread context class loader
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class[] ifaceClasses = new Class[interfaces.length];
      for (int i = 0; i < interfaces.length; i++)
      {
         String className = interfaces[i];
         Class iface = forName(className);
         ifaceClasses[i] = iface;
      }

      return Proxy.getProxyClass(loader, ifaceClasses);
   }
}
