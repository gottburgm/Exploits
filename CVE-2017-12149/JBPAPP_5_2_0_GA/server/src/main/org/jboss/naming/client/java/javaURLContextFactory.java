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
package org.jboss.naming.client.java;

import java.util.Hashtable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.naming.OperationNotSupportedException;
import javax.naming.NameParser;
import javax.naming.spi.ObjectFactory;

import org.jboss.corba.ORBFactory;
import org.jboss.logging.Logger;

/** The external client java URL context factory. This is used in conjunction
 * with j2ee application clients to implement the java:comp/env
 * enterprise naming context (ENC).
 *     
 * @see javax.naming.spi.ObjectFactory
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public class javaURLContextFactory
   implements ObjectFactory
{
   private static final Logger log = Logger.getLogger(javaURLContextFactory.class);
   
   public static final String J2EE_CLIENT_NAME_PROP = "j2ee.clientName";

   // ObjectFactory implementation ----------------------------------
   public Object getObjectInstance(Object obj, Name name, Context nameCtx,
      Hashtable env)
      throws Exception
   {
      // Get the j2ee.clientName value
      String clientName = (String) env.get(J2EE_CLIENT_NAME_PROP);
      if (clientName == null)
      {
         // Look for the name as a system property
         clientName = (String) AccessController.doPrivileged(
            new PrivilegedAction()
            {
               public Object run()
               {
                  try
                  {
                     return System.getProperty(J2EE_CLIENT_NAME_PROP);
                  }
                  catch (SecurityException e)
                  {
                     return null;
                  }
               }
            }
         );
         if (clientName == null)
            throw new NamingException("Failed to find j2ee.clientName in jndi env");
      }

      Object result = null;

      if (nameCtx == null)
         nameCtx = new InitialContext(env);
      if (obj == null)
      {
         // Create a context for resolving the java: url
         InvocationHandler handler = new EncContextProxy(nameCtx, clientName);
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         Class[] ifaces = {Context.class};
         result = Proxy.newProxyInstance(loader, ifaces, handler);
      }
      return result;
   }

   private static class EncContextProxy implements InvocationHandler
   {
      Context lookupCtx;
      String clientName;

      EncContextProxy(Context lookupCtx, String clientName)
      {
         this.lookupCtx = lookupCtx;
         this.clientName = clientName;
      }

      /**
       */
      public Object invoke(Object proxy, Method method, Object[] args)
         throws Throwable
      {
         String methodName = method.getName();
         if (methodName.equals("toString") == true)
            return "Client ENC(" + clientName + ")";

         if (methodName.equals("lookup") == false)
            throw new OperationNotSupportedException("Only lookup is supported, op=" + method);
         NameParser parser = lookupCtx.getNameParser("");
         Name name = null;
         if (args[0] instanceof String)
            name = parser.parse((String) args[0]);
         else
            name = (Name) args[0];

         // Check for special objects not in the env
         if (name.size() < 2 || "java:comp".equals(name.get(0)) == false || "env".equals(name.get(1)) == false)
            return getSpecialObject(name);
         // Lookup the client application context from the server
         Context clientCtx = (Context) lookupCtx.lookup(clientName);
         
         // JBAS-3967: EJB3 Client container hack
         try
         {
            clientCtx = (Context) clientCtx.lookup("env");
         }
         catch(NamingException e)
         {
            // ignore
            log.trace("No env sub context found", e);
         }
         
         // Strip the comp/env prefix
         Name bindingName = name.getSuffix(2);
         Object binding = clientCtx.lookup(bindingName);
         
         return binding;
      }
      
      public Object getSpecialObject(Name name) throws NamingException
      {
         if (name.size() > 0 && "java:comp".equals(name.get(0)))
         {
            if (name.size() == 2 && "ORB".equals(name.get(1)))
               return ORBFactory.getORB();
            else if (name.size() == 2 && "HandleDelegate".equals(name.get(1)))
               return HandleDelegateFactory.getHandleDelegateSingleton();
         }
         throw new NamingException("Name not found " + name);
      }
   }
}
