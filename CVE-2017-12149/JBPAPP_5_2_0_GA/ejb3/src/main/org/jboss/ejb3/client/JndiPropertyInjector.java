/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.client;

import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb3.BeanContext;
import org.jboss.injection.AbstractPropertyInjector;
import org.jboss.injection.PojoInjector;
import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.logging.Logger;

/**
 * Injects a jndi dependency into a bean property.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class JndiPropertyInjector extends AbstractPropertyInjector
   implements PojoInjector
{
   private static final Logger log = Logger.getLogger(JndiPropertyInjector.class);
   
   private String jndiName;
   private Context ctx;

   public JndiPropertyInjector(BeanProperty property, String jndiName,
         Context ctx)
   {
      super(property);
      this.jndiName = jndiName;
      this.ctx = ctx;
   }

   public void inject(BeanContext bctx)
   {
   }
   
   public Class<?> getInjectionClass()
   {
      return property.getType();
   }
   
   public void inject(BeanContext bctx, Object instance)
   {
   }

   public void inject(Object instance)
   {
      Object value = lookup(jndiName);
      log.trace("injecting " + value + " from " + jndiName + " into " + property + " of " + instance);
      try
      {
         property.set(instance, value);
      }
      catch(IllegalArgumentException e)
      {
         Class c1 = value.getClass();
         StringBuffer buffer = new StringBuffer("Jndi value '"+jndiName+"' class info:");
         displayClassInfo(c1, buffer);
         log.debug("Failed to inject jndi property, "+buffer);
         Class c2 = property.getType();
         buffer.setLength(0);
         buffer.append("Field "+property.getName()+" class info:");
         displayClassInfo(c2, buffer);
         log.debug(", "+buffer);
         throw e;
      }
   }

   protected Object lookup(String jndiName)
   {
      Object dependency = null;
      boolean trace = log.isTraceEnabled();
      try
      {
         if(trace)
            log.trace("Looking for enc entry: "+jndiName);
         dependency = ctx.lookup(jndiName);
         if(trace)
            log.trace("Success: "+dependency);
      }
      catch (NamingException e)
      {
         // Try as a global jndi name
         if(trace)
            log.trace("Failed enc lookup: "+e.getExplanation());
         try
         {
            if(trace)
               log.trace("Failed trying as global entry: "+jndiName);
            InitialContext ictx = new InitialContext(ctx.getEnvironment());
            dependency = ictx.lookup(jndiName);
            if(trace)
               log.trace("Success: "+dependency);
         }
         catch(NamingException e2)
         {
            if(trace)
               log.trace("Failed global lookup: "+e2.getExplanation());
         }

         if(dependency == null)
         {
            Throwable cause = e;
            while(cause.getCause() != null)
               cause = cause.getCause();
            throw new RuntimeException("Unable to inject jndi dependency: " + jndiName + " into property " + property + ": " + cause.getMessage(), e);
         }
      }
      return dependency;
   }

   static void displayClassInfo(Class clazz, StringBuffer results)
   {
      // Print out some codebase info for the ProbeHome
      ClassLoader cl = clazz.getClassLoader();
      results.append("\n"+clazz.getName()+".ClassLoader="+cl);
      CodeSource clazzCS = clazz.getProtectionDomain().getCodeSource();
      if( clazzCS != null )
         results.append("\n++++CodeSource: "+clazzCS);
      else
         results.append("\n++++Null CodeSource");

      results.append("\nImplemented Interfaces:");
      Class[] ifaces = clazz.getInterfaces();
      for(int i = 0; i < ifaces.length; i ++)
      {
         results.append("\n++"+ifaces[i]);
         ClassLoader loader = ifaces[i].getClassLoader();
         results.append("\n++++ClassLoader: "+loader);
         ProtectionDomain pd = ifaces[i].getProtectionDomain();
         CodeSource cs = pd.getCodeSource();
         if( cs != null )
            results.append("\n++++CodeSource: "+cs);
         else
            results.append("\n++++Null CodeSource");
      }
   }

}
