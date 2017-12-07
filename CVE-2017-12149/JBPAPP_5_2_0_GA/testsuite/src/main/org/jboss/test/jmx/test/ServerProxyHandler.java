/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jmx.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

import java.rmi.UnmarshalException;
import java.util.ArrayList;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class ServerProxyHandler implements InvocationHandler
{   
   ObjectName serverName;
   MBeanServerConnection server;
   ServerProxyHandler(MBeanServerConnection server, ObjectName serverName)
   {   
      this.server = server;
      this.serverName = serverName;
   }   

   public Object invoke(Object proxy, Method method, Object[] args)
         throws Throwable
   {   
      String methodName = method.getName();
      Class[] sigTypes = method.getParameterTypes();
      ArrayList<String> sigStrings = new ArrayList<String>();
      for(int s = 0; s < sigTypes.length; s ++) 
         sigStrings.add(sigTypes[s].getName());
      String[] sig = new String[sigTypes.length];
      sigStrings.toArray(sig);
      Object value = null;
      try 
      {   
         value = server.invoke(serverName, methodName, args, sig);
      }
      catch(UnmarshalException ex){
          System.out.println("getUnmarshalException");
          throw ex;
      }  
      return value;
   }   
}   
