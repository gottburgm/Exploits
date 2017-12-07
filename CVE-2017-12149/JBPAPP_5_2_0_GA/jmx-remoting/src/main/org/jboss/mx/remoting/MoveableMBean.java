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
package org.jboss.mx.remoting;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import org.jboss.remoting.loading.ClassUtil;

/**
 * MoveableMBean is a Dynamic Proxy to an MBean that exists on a JMX Network. This object
 * can be created an cast to the appropriate set of interfaces that the MBean implements and can
 * be serialized and past across the network and serialization and remote invocation will be
 * handled as if the Object was local to the JVM.  <P>
 * <p/>
 * Example usage:
 * <p/>
 * <CODE><PRE>
 * <p/>
 * TestMBean mybean=(TestMBean)MoveableMBean.create(mbeanLocator,TestMBean.class);
 * <p/>
 * // transport method
 * mybean.myMethod();
 * <p/>
 * // transport a method against a remote JMX server and pass the TestMBean object
 * // it will be serialized and passed to the remote server .. on the other side, the
 * // JVM will de-serialize, create a local DynamicProxy and then invocations against the
 * // parameter, will be invoked remotely back to the mbeanLocator above
 * remoteserver.transport(new ObjectName(":test=MyObject"),"myMethod",new Object[]{mybean},new String[]{TestMBean.class.getName()});
 * <p/>
 * </PRE></CODE>
 * <p/>
 * You can also cache attribute values in the local proxy, in the case where the values are fixed
 * and you don't want to remote overhead associated with sending the invocation remotely.  In this case,
 * you can pass in a Map of attribute/value pairs which will be always returned in any getter method invocation
 * against the attribute.
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class MoveableMBean implements InvocationHandler, Serializable
{
   static final long serialVersionUID = -7506487379354274551L;
   
   private transient static Map methodArgs = new WeakHashMap();

   // preloaded Method objects for the methods in java.lang.Object
   private static transient Method hashCodeMethod;
   private static transient Method equalsMethod;
   private static transient Method toStringMethod;
   private static transient Method notifyAllMethod;
   private static transient Method notifyMethod;
   private static transient Method wait0Method;
   private static transient Method wait1Method;
   private static transient Method wait2Method;

   protected MBeanLocator locator;
   protected Integer hashCode;
   protected Map staticAttributes;

   static
   {
      try
      {
         // SETUP Method objects from the Object class
         hashCodeMethod = Object.class.getMethod("hashCode", null);
         equalsMethod = Object.class.getMethod("equals", new Class[]{Object.class});
         toStringMethod = Object.class.getMethod("toString", null);
         notifyMethod = Object.class.getMethod("notify", null);
         notifyAllMethod = Object.class.getMethod("notifyAll", null);
         notifyMethod = Object.class.getMethod("notify", null);
         wait0Method = Object.class.getMethod("wait", null);
         wait1Method = Object.class.getMethod("wait", new Class[]{Long.TYPE});
         wait2Method = Object.class.getMethod("wait", new Class[]{Long.TYPE, Integer.TYPE});
      }
      catch(NoSuchMethodException e)
      {
         throw new InternalError(e.getMessage());
      }
   }


   protected MoveableMBean(MBeanLocator locator, Map staticAttributes)
   {
      this.locator = locator;
      this.staticAttributes = staticAttributes;
      this.hashCode = new Integer(System.identityHashCode(locator));
   }

   /**
    * return the locator that the mbean references
    *
    * @return
    */
   public final MBeanLocator getMBeanLocator()
   {
      return locator;
   }

   public static Object create(MBeanLocator locator, ClassLoader loader, Class interfaceClass)
   {
      Class classes[] = ClassUtil.getInterfacesFor(interfaceClass);
      return create(locator, loader, classes, null);
   }

   public static Object create(MBeanLocator locator, ClassLoader loader, Class interfaceClass, Map staticAttributes)
   {
      Class classes[] = ClassUtil.getInterfacesFor(interfaceClass);
      return create(locator, loader, classes, staticAttributes);
   }

   public static Object create(MBeanLocator locater, Class interfaceClass)
   {
      Class classes[] = ClassUtil.getInterfacesFor(interfaceClass);
      return create(locater, classes);
   }

   public static Object create(MBeanLocator locator, Class interfaces[])
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if(loader == null)
      {
         loader = interfaces[0].getClassLoader();
      }
      return create(locator, loader, interfaces, null);
   }

   public static Object create(MBeanLocator locator, ClassLoader loader, Class interfaces[], Map staticAttributes)
   {
      Class _inf[] = interfaces;
      boolean found = false;
      for(int c = 0; c < interfaces.length; c++)
      {
         if(interfaces[c] == LocationAware.class)
         {
            found = true;
            break;
         }
      }
      if(found == false)
      {
         _inf = new Class[interfaces.length + 1];
         System.arraycopy(interfaces, 0, _inf, 0, interfaces.length);
         // always add location aware interface so you can cast this mbean to that interface
         _inf[interfaces.length] = LocationAware.class;
      }
      return Proxy.newProxyInstance(loader, _inf, new MoveableMBean(locator, staticAttributes));
   }

   /**
    * add a map of static attributes, with each key being the attributeName and the value being the
    * value to return for every invocation to this attribute getter
    *
    * @param values
    */
   public void addStaticAttributes(Map values)
   {
      if(staticAttributes == null)
      {
         staticAttributes = values;
      }
      else
      {
         staticAttributes.putAll(values);
      }
   }

   /**
    * add a static attribute
    *
    * @param name
    * @param value
    */
   public void addStaticAttribute(String name, Object value)
   {
      if(staticAttributes == null)
      {
         staticAttributes = new HashMap(1);
      }
      staticAttributes.put(name, value);
   }

   protected Object handleLocationMethods(Object proxy, Method method, Object[] args)
         throws Throwable
   {
      return locator;
   }

   /**
    * Processes a method invocation on a proxy instance and returns
    * the result.  This method will be invoked on an invocation handler
    * when a method is invoked on a proxy instance that it is
    * associated with.
    *
    * @param	proxy the proxy instance that the method was invoked on
    * @param	method the <code>Method</code> instance corresponding to
    * the interface method invoked on the proxy instance.  The declaring
    * class of the <code>Method</code> object will be the interface that
    * the method was declared in, which may be a superinterface of the
    * proxy interface that the proxy class inherits the method through.
    * @param	args an array of objects containing the values of the
    * arguments passed in the method invocation on the proxy instance,
    * or <code>null</code> if interface method takes no arguments.
    * Arguments of primitive types are wrapped in instances of the
    * appropriate primitive wrapper class, such as
    * <code>java.lang.Integer</code> or <code>java.lang.Boolean</code>.
    * @return	the value to return from the method invocation on the
    * proxy instance.  If the declared return type of the interface
    * method is a primitive type, then the value returned by
    * this method must be an instance of the corresponding primitive
    * wrapper class; otherwise, it must be a type assignable to the
    * declared return type.  If the value returned by this method is
    * <code>null</code> and the interface method's return type is
    * primitive, then a <code>NullPointerException</code> will be
    * thrown by the method invocation on the proxy instance.  If the
    * value returned by this method is otherwise not compatible with
    * the interface method's declared return type as described above,
    * a <code>ClassCastException</code> will be thrown by the method
    * invocation on the proxy instance.
    * @throws	java.lang.Throwable the exception to throw from the method
    * invocation on the proxy instance.  The exception's type must be
    * assignable either to any of the exception types declared in the
    * <code>throws</code> clause of the interface method or to the
    * unchecked exception types <code>java.lang.RuntimeException</code>
    * or <code>java.lang.Error</code>.  If a checked exception is
    * thrown by this method that is not assignable to any of the
    * exception types declared in the <code>throws</code> clause of
    * the interface method, then an
    * {@link java.lang.reflect.UndeclaredThrowableException} containing the
    * exception that was thrown by this method will be thrown by the
    * method invocation on the proxy instance.
    * @see	java.lang.reflect.UndeclaredThrowableException
    */
   public Object invoke(Object proxy, Method method, Object[] args)
         throws Throwable
   {
      if(method.getDeclaringClass() == Object.class)
      {
         return handleObjectMethods(proxy, method, args);
      }
      if(method.getDeclaringClass() == LocationAware.class)
      {
         return handleLocationMethods(proxy, method, args);
      }
      try
      {
         String name = method.getName();
         if(name.startsWith("get") && name.length() > 3 && (args == null || args.length <= 0))
         {
            // getter
            // example: getListeningPoint   "ListeningPoint"
            String attributeName = method.getName().substring(3);
            if(staticAttributes != null)
            {
               // check to see if we want a static attribute value to
               // come from the local cache version, rather than from
               // remote
               if(staticAttributes.containsKey(attributeName))
               {
                  return staticAttributes.get(attributeName);
               }
            }
            return locator.getMBeanServer().getAttribute(locator.getObjectName(), attributeName);
         }
         else if(name.startsWith("set") && name.length() > 3)
         {
            // setter
            String attributeName = method.getName().substring(3);

            // first transport remotely, to make sure that this doesn't fail ..
            locator.getMBeanServer().setAttribute(locator.getObjectName(), new Attribute(attributeName, (args == null ? null : args[0])));

            // if the transport succeeded and we need to update our local cache copy ..
            if(staticAttributes != null)
            {
               // check to see if we want a static attribute value to
               // come from the local cache version, rather than from
               // remote, and if so, store it local as well as transport remotely
               if(staticAttributes.containsKey(attributeName))
               {
                  return staticAttributes.put(attributeName, (args == null ? null : args[0]));
               }
            }
            return null;
         }
         else
         {
            return locator.getMBeanServer().invoke(locator.getObjectName(), method.getName(), args, makeArgSignature(method, args));
         }
      }
      catch(InstanceNotFoundException inf)
      {
         throw inf;
      }
      catch(MBeanException mbe)
      {
         throw mbe.getTargetException();
      }
      catch(ReflectionException re)
      {
         throw re.getTargetException();
      }
   }

   protected Boolean proxyEquals(Object proxy, Object other)
   {
      return (proxy == other ? Boolean.TRUE : Boolean.FALSE);
   }

   protected String proxyToString(Object proxy)
   {
      return (locator == null ? ("MoveableMBean [Proxy" + "@" + proxy.hashCode() + "]") : locator.toString());
   }

   protected Integer proxyHashCode(Object proxy)
   {
      return hashCode;
   }

   protected Object handleObjectMethods(Object proxy, Method method, Object args[])
         throws Exception
   {
      if(method.equals(hashCodeMethod))
      {
         return proxyHashCode(proxy);
      }
      else if(method.equals(equalsMethod))
      {
         return proxyEquals(proxy, args[0]);
      }
      else if(method.equals(toStringMethod))
      {
         return proxyToString(proxy);
      }
      else if(method.equals(notifyAllMethod))
      {
         notifyAll();
         return null;
      }
      else if(method.equals(notifyMethod))
      {
         notify();
         return null;
      }
      else if(method.equals(wait0Method))
      {
         wait();
         return null;
      }
      else if(method.equals(wait1Method))
      {
         wait(((Long) args[0]).longValue());
         return null;
      }
      else if(method.equals(wait2Method))
      {
         wait(((Long) args[0]).longValue(), ((Integer) args[1]).intValue());
         return null;
      }

      else
      {
         throw new InternalError("unexpected Object method dispatched: " + method);
      }
   }

   /**
    * convert the method to a String array for the signature of the MBean invocation
    *
    * @param method
    * @param args
    * @return
    */
   protected synchronized String[] makeArgSignature(Method method, Object args[])
   {
      if(methodArgs.containsKey(method))
      {
         return (String[]) methodArgs.get(method);
      }
      Class pt[] = method.getParameterTypes();
      if(pt == null || pt.length <= 0)
      {
         return null;
      }
      String sig[] = new String[args.length];
      for(int c = 0; c < pt.length; c++)
      {
         sig[c] = pt[c].getName();
      }
      methodArgs.put(method, sig);
      return sig;
   }

   /**
    * resolve the proxy interfaces
    *
    * @param interfaces
    * @return
    * @throws IOException
    * @throws ClassNotFoundException
    */
   protected Class resolveProxyClass(String[] interfaces)
         throws IOException, ClassNotFoundException
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      if(cl == null)
      {
         cl = MoveableMBean.class.getClassLoader();
      }
      Class inf[] = new Class[interfaces.length];
      for(int c = 0; c < interfaces.length; c++)
      {
         inf[c] = cl.loadClass(interfaces[c]);
      }
      return java.lang.reflect.Proxy.getProxyClass(cl, inf);
   }

}
