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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * A factory for producing MBean proxies.
 *
 * <p>Created proxies will also implement {@link org.jboss.mx.util.MBeanProxyInstance}
 * allowing access to the proxies configuration.
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>.
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>.
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>.
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * @version <tt>$Revision: 81019 $</tt>
 */
public class MBeanProxyExt
   implements InvocationHandler, MBeanProxyInstance, Externalizable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2942844863242742655L;

   /**
    * The remote MBeanServerConnection
    */
   public static MBeanServerConnection remote;
   
   /**
    * The server to proxy invoke calls to.
    */
   private MBeanServerConnection server;

   /**
    * The name of the object to invoke.
    */
   private ObjectName name;

   /**
    * The MBean's attributes
    */
   private transient final HashMap attributeMap = new HashMap();
   /**
    * Have the attributes been retrieved
    */
   private transient boolean inited = false;

   /**
    * For externalizable
    */
   public MBeanProxyExt()
   {
   }
   
   /**
    * Construct an MBeanProxy.
    */
   MBeanProxyExt(final ObjectName name, final MBeanServer server, boolean lazyInit)
   {
      this.name = name;
      this.server = server;
      if (lazyInit == false)
         init();
   }

   /**
    * Used when args is null.
    */
   private static final Object EMPTY_ARGS[] = {};

   /**
    * Invoke the configured MBean via the target MBeanServer and decode any
    * resulting JMX exceptions that are thrown.
    */
   public Object invoke(final Object proxy,
      final Method method,
      final Object[] args)
      throws Throwable
   {
      // if the method belongs to ProxyInstance, then invoke locally
      Class type = method.getDeclaringClass();
      if (type == MBeanProxyInstance.class || type == Object.class)
      {
         return method.invoke(this, args);
      }

      String methodName = method.getName();

      // Get attribute
      if (methodName.startsWith("get") && args == null)
      {
         if (inited == false)
            init();

         String attrName = methodName.substring(3);
         MBeanAttributeInfo info = (MBeanAttributeInfo) attributeMap.get(attrName);
         if (info != null)
         {
            String retType = method.getReturnType().getName();
            if (retType.equals(info.getType()))
            {
               try
               {
                  return server.getAttribute(name, attrName);
               }
               catch (Exception e)
               {
                  throw JMXExceptionDecoder.decode(e);
               }
            }
         }
      }

      // Is attribute
      else if (methodName.startsWith("is") && args == null)
      {
         if (inited == false)
            init();

         String attrName = methodName.substring(2);
         MBeanAttributeInfo info = (MBeanAttributeInfo) attributeMap.get(attrName);
         if (info != null && info.isIs())
         {
            Class retType = method.getReturnType();
            if (retType.equals(Boolean.class) || retType.equals(Boolean.TYPE))
            {
               try
               {
                  return server.getAttribute(name, attrName);
               }
               catch (Exception e)
               {
                  throw JMXExceptionDecoder.decode(e);
               }
            }
         }
      }

      // Set attribute
      else if (methodName.startsWith("set") && args != null && args.length == 1)
      {
         if (inited == false)
            init();

         String attrName = methodName.substring(3);
         MBeanAttributeInfo info = (MBeanAttributeInfo) attributeMap.get(attrName);
         if (info != null && method.getReturnType() == Void.TYPE)
         {
            try
            {
               server.setAttribute(name, new Attribute(attrName, args[0]));
               return null;
            }
            catch (Exception e)
            {
               throw JMXExceptionDecoder.decode(e);
            }
         }
      }

      // Operation

      // convert the parameter types to strings for JMX
      Class[] types = method.getParameterTypes();
      String[] sig = new String[types.length];
      for (int i = 0; i < types.length; i++)
      {
         sig[i] = types[i].getName();
      }

      // invoke the server and decode JMX exceptions
      try
      {
         return server.invoke(name, methodName, args == null ? EMPTY_ARGS : args, sig);
      }
      catch (Exception e)
      {
         throw JMXExceptionDecoder.decode(e);
      }
   }


   ///////////////////////////////////////////////////////////////////////////
   //                          MBeanProxyInstance                           //
   ///////////////////////////////////////////////////////////////////////////

   public final ObjectName getMBeanProxyObjectName()
   {
      return name;
   }

   public final MBeanServer getMBeanProxyMBeanServer()
   {
      if (server instanceof MBeanServer == false)
         throw new IllegalStateException("This operation is not available for an MBeanServerConnection");
      return (MBeanServer) server;
   }

   public final MBeanServerConnection getMBeanProxyMBeanServerConnection()
   {
      return server;
   }

   ///////////////////////////////////////////////////////////////////////////
   //                          Object Overrides                             //
   ///////////////////////////////////////////////////////////////////////////
   
   /**
    * We need to override this because by default equals returns false when
    * called on the proxy object and then relayed here.
    */
   public boolean equals(Object that)
   {
      if (that == null) return false;
      if (that == this) return true;
      
      // check if 'that' is an MBeanProxyExt or a Proxy instance
      // that implements the MBeanProxyInstance interface
      if (that instanceof MBeanProxyInstance)
      {
         MBeanProxyInstance proxy = (MBeanProxyInstance) that;
         
         // assume equality if both the MBeanServer and ObjectName match
         if (name.equals(proxy.getMBeanProxyObjectName()) &&
            server.equals(proxy.getMBeanProxyMBeanServer()))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * As with equals, use the MBeanServer + ObjectName to calculate the
    * hashCode
    */
   public int hashCode()
   {
      return name.hashCode() * 31 + server.hashCode();
   }

   /**
    * avoid the default printout, e.g. org.jboss.mx.util.MBeanProxyExt@120540c
    */
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer(128);

      sbuf.append("MBeanProxyExt[").append(name.toString()).append(']');

      return sbuf.toString();
   }

   ///////////////////////////////////////////////////////////////////////////
   //                            Factory Methods                            //
   ///////////////////////////////////////////////////////////////////////////

   /**
    * Create an MBean proxy.
    * @param intf The interface which the proxy will implement.
    * @param name A string used to construct the ObjectName of the MBean to
    * proxy to.
    * @return A MBean proxy.
    * @throws javax.management.MalformedObjectNameException Invalid object
    * name.
    */
   public static Object create(final Class intf, final String name)
      throws MalformedObjectNameException
   {
      return create(intf, new ObjectName(name));
   }

   /**
    * Create an MBean proxy.
    * @param intf The interface which the proxy will implement.
    * @param name A string used to construct the ObjectName of the MBean to
    * proxy to.
    * @param server The MBeanServer that contains the MBean to proxy to.
    * @return A MBean proxy.
    * @throws javax.management.MalformedObjectNameException Invalid object
    * name.
    */
   public static Object create(final Class intf,
      final String name,
      final MBeanServer server)
      throws MalformedObjectNameException
   {
      return create(intf, new ObjectName(name), server);
   }

   /**
    * Create an MBean proxy.
    * @param intf The interface which the proxy will implement.
    * @param name The name of the MBean to proxy invocations to.
    * @return A MBean proxy.
    */
   public static Object create(final Class intf, final ObjectName name)
   {
      return create(intf, name, MBeanServerLocator.locateJBoss());
   }

   /**
    * Create an MBean proxy.
    * @param intf The interface which the proxy will implement.
    * @param name The name of the MBean to proxy invocations to.
    * @param server The MBeanServer that contains the MBean to proxy to.
    * @return A MBean proxy.
    */
   public static Object create(final Class intf,
      final ObjectName name,
      final MBeanServer server)
   {
      return create(intf, name, server, false);
   }

   /**
    * Create an MBean proxy.
    * @param intf The interface which the proxy will implement.
    * @param name The name of the MBean to proxy invocations to.
    * @param server The MBeanServer that contains the MBean to proxy to.
    * @param lazyInit - a flag indicating if the mbean attribute info should
    *    be retrieved when the proxy is created.
    * @return A MBean proxy.
    */
   public static Object create(final Class intf, final ObjectName name,
      final MBeanServer server, boolean lazyInit)
   {
      // CL which delegates to MBeanProxyInstance's cl for it's class resolution
      PrivilegedAction action = new PrivilegedAction()
      {
         public Object run()
         {
            ClassLoader cl = new ClassLoader(intf.getClassLoader())
            {
               public Class loadClass(final String className) throws ClassNotFoundException
               {
                  try
                  {
                     return super.loadClass(className);
                  }
                  catch (ClassNotFoundException e)
                  {
                     // only allow loading of MBeanProxyInstance from this loader
                     if (className.equals(MBeanProxyInstance.class.getName()))
                     {
                        return MBeanProxyInstance.class.getClassLoader().loadClass(className);
                     }
                     // was some other classname, throw the CNFE
                     throw e;
                  }
               }
            };
            return cl;
         }
      };
      ClassLoader cl = (ClassLoader) AccessController.doPrivileged(action);
      Class[] ifaces = {MBeanProxyInstance.class, intf};
      InvocationHandler handler = new MBeanProxyExt(name, server, lazyInit);
      return Proxy.newProxyInstance(cl, ifaces, handler);
   }

   /**
    * Retrieve the mbean MBeanAttributeInfo
    */
   private synchronized void init()
   {
      // The MBean's attributes
      inited = true;
      try
      {
         MBeanInfo info = server.getMBeanInfo(name);
         MBeanAttributeInfo[] attributes = info.getAttributes();

         for (int i = 0; i < attributes.length; ++i)
            attributeMap.put(attributes[i].getName(), attributes[i]);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error creating MBeanProxy: " + name, e);
      }
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      name = (ObjectName) in.readObject();
      server = (MBeanServerConnection) in.readObject();
   }


   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeObject(name);
      if (remote != null)
         out.writeObject(remote);
      else
         out.writeObject(server); // This will fail for a normal MBeanServer
   }
}
