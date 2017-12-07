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
package org.jboss.ha.framework.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import org.jboss.ha.framework.interfaces.ClusteringTargetsRepository;
import org.jboss.ha.framework.interfaces.FamilyClusterInfo;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.PayloadKey;
import org.jboss.invocation.ServiceUnavailableException;
import org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxyHA;

/**
 * Used for testing clustering: mimics an exhausted set of targets.
 * This interceptor should be placed between a RetryInterceptor and
 * the InvokerInterceptor.
 *
 * @author  brian.stansberry@jboss.com.
 * @version $Id: ServiceUnavailableClientInterceptor.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $
 */

public class ServiceUnavailableClientInterceptor extends org.jboss.proxy.Interceptor
{

   // Constants -----------------------------------------------------
   
   /** The serialVersionUID */
   private static final long serialVersionUID = 8830272856328720750L;
   
   // Attributes ----------------------------------------------------
   
   private String proxyFamilyName;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   public ServiceUnavailableClientInterceptor ()
   {
   }
   
   // Public --------------------------------------------------------
   
   // Z implementation ----------------------------------------------
   
   // Interceptor overrides ---------------------------------------------------
   
   public Object invoke (Invocation mi) throws Throwable
   {
      Object data = mi.getValue ("DO_FAIL_DURING_NEXT_CALL");
      
      if (data != null &&
            data instanceof java.lang.Boolean &&
            data.equals (java.lang.Boolean.TRUE))
      {
         
         // Clear the instruction
         mi.setValue ("DO_FAIL_DURING_NEXT_CALL", Boolean.FALSE, PayloadKey.AS_IS);
         
         if (proxyFamilyName == null)
         {
            proxyFamilyName = getProxyFamilyName(mi);
         }
         
         // Clear the targets to simulate exhausting them all
         FamilyClusterInfo info = ClusteringTargetsRepository.getFamilyClusterInfo(proxyFamilyName);
         List targets = info.getTargets();
         for (Iterator it = targets.iterator(); it.hasNext(); )
            info.removeDeadTarget(it.next());
         
         throw new ServiceUnavailableException("Service unavailable", 
                                               new Exception("Test"));
      } 

      return getNext().invoke(mi);
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
   static String getProxyFamilyName(Invocation invocation) throws Exception
   {
      InvocationContext ctx = invocation.invocationContext;
      Invoker invoker = ctx.getInvoker();
      
      // HACK!  Get the proxy family name via reflection.  
      // Works for the known InvokerProxyHA impls
      Method m = invoker.getClass().getDeclaredMethod("getProxyFamilyName", new Class[]{});
      String proxyFamilyName = (String) m.invoke(invoker, new Object[] {});
      
      return proxyFamilyName;
   }
}
