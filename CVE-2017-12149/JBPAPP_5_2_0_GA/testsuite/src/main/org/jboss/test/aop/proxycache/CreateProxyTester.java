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
package org.jboss.test.aop.proxycache;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jboss.aop.proxy.ClassProxyFactory;
import org.jboss.aop.proxy.ProxyMixin;
import org.jboss.aop.proxy.container.AOPProxyFactoryParameters;
import org.jboss.aop.proxy.container.AspectManaged;
import org.jboss.aop.proxy.container.GeneratedAOPProxyFactory;
import org.jboss.logging.Logger;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 85945 $
 */
public class CreateProxyTester implements CreateProxyTesterMBean
{
   Logger log = Logger.getLogger(CreateProxyTester.class);
   
   public int createContainerProxy(String className, String[] interfaceNames) throws Exception
   {
      try
      {
         log.info("==== createContainerProxy()");
         log.info("TCL " + Thread.currentThread().getContextClassLoader());

         Class<?> clazz = this.getClass().getClassLoader().loadClass(className);

         log.info("class loader " + clazz.getName() + " " + clazz.getClassLoader());

         Object object = clazz.newInstance();      
         Class<?>[] interfaces = new Class[interfaceNames.length];
         for (int i = 0 ; i < interfaceNames.length ; i++)
         {
            interfaces[i] = this.getClass().getClassLoader().loadClass(interfaceNames[i]);
            log.info("interface " + interfaces[i] + " " + interfaces[i].getClassLoader());
         }
         
         AOPProxyFactoryParameters params = new AOPProxyFactoryParameters();
         params.setTarget(object);
         params.setInterfaces(interfaces);

         GeneratedAOPProxyFactory factory = new GeneratedAOPProxyFactory();
         Object proxy = factory.createAdvisedProxy(params);
         
         if (proxy instanceof AspectManaged == false)
         {
            throw new RuntimeException("Proxy is not a proxy " + proxy.getClass().getName());
         }
         
         log.info("proxy loader " + proxy.getClass() + " " + proxy.getClass().getClassLoader());

         if (proxy.getClass().getSuperclass() != clazz)
         {
            throw new RuntimeException("Wrong superclass " + proxy.getClass().getSuperclass() + " expected " + clazz);
         }
         
         if (interfaces.length > 0)
         {
            HashSet<Class<?>> ifs = new HashSet<Class<?>>(); 
            Collections.addAll(ifs, interfaces);
            for (Class<?> iface : ifs)
            {
               if (!ifs.contains(iface))
               {
                  throw new RuntimeException("Could not find the interface " + iface);
               }
            }
         }
         
         return System.identityHashCode(proxy.getClass());
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw e;
      }
   }
   
   public int createClassProxy(String className, Map<String, List<String>> mixins) throws Exception
   {
      try
      {
         log.info("==== createClassProxy()");
         log.info("TCL " + Thread.currentThread().getContextClassLoader());

         Class<?> clazz = this.getClass().getClassLoader().loadClass(className);
         log.info("Class.getCLassLoader() " + clazz.getName() + " " + clazz.getClassLoader());

         ProxyMixin[] proxyMixins = createMixins(mixins);

         Object proxy = ClassProxyFactory.newInstance(clazz, proxyMixins);
         
         log.info("proxy loader " + proxy.getClass() + " " + proxy.getClass().getClassLoader());

         return System.identityHashCode(proxy.getClass());
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw e;
      }
   }
   
   private ProxyMixin[] createMixins(Map<String, List<String>> mixins) throws Exception
   {
      if (mixins == null)
      {
         return null;
      }
      ProxyMixin[] proxyMixins = new ProxyMixin[mixins.size()];
      
      int i = 0;
      for (String mixinName : mixins.keySet())
      {
         Class<?> mixinClass = this.getClass().getClassLoader().loadClass(mixinName);
         log.info("mixin class loader " + mixinClass.getClass() + " " + mixinClass.getClass().getClassLoader());
         Object mixin = mixinClass.newInstance();
         
         List<String> mixinInterfaces = mixins.get(mixinName);
         Class<?>[] interfaces = new Class<?>[mixinInterfaces.size()];
         int j = 0;
         for (String interfaceName : mixinInterfaces)
         {
            Class<?> iface = this.getClass().getClassLoader().loadClass(interfaceName); 
            log.info("iface class loader " + iface.getClass() + " " + iface.getClass().getClassLoader());
            interfaces[j++] = iface;
         }
            
         proxyMixins[i++] = new ProxyMixin(mixin, interfaces);
      }
      
      return proxyMixins;
   }
}
