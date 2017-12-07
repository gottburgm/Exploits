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
package org.jboss.jmx.connector.invoker;

import org.jboss.invocation.MarshalledInvocation;
import org.jboss.mx.interceptor.AbstractInterceptor;
import org.jboss.mx.server.Invocation;

/**
 * An interceptor that validates the Serializability of responses,
 * using plugable policies. 
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="mailto:fabcipriano@yahoo.com.br">Fabiano C. de Oliveira</a>
 * @version $Revision: 85945 $
 */
public class SerializableInterceptor extends AbstractInterceptor
{   
   /** The plugable policy to use */
   private SerializablePolicy policy = new NoopPolicy();
   
   /**
    * Configure a SerializablePolicy class
    */
   public void setPolicyClass(String policyClass) throws Exception
   {
      try
      {
         // try to load the policy Class
         Class clazz = Thread.currentThread().getContextClassLoader().loadClass(policyClass);
         policy = (SerializablePolicy)clazz.newInstance();
      }
      catch (Exception e) // ClassNotFoundException, IllegalAccessException, InstantiationException
      {
         // policy class not found. Make a second try using
         // the 'org.jboss.jmx.connector.invoker.serializablepolicy.' package prefix
         // for the "standard" reponse policies provided with jboss.
         // If that fails, too, rethrow the original exception.
         try
         {
            policyClass = "org.jboss.jmx.connector.invoker.serializablepolicy." + policyClass;
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(policyClass);
            policy = (SerializablePolicy)clazz.newInstance();
         }
         catch (Exception inner)
         {
            throw e;
         }
      }
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      // Invoke the next in the sequence
      Object result = invocation.nextInterceptor().invoke(invocation);
      
      // If the invocation was an 'invoke(MarshalledInvocation)'
      // filter the result using the plugable policy
      if ("invoke".equals(invocation.getName()))
      {
         Object[] args = invocation.getArgs();
         if ((args.length == 1) && (args[0] instanceof MarshalledInvocation))
         {
            MarshalledInvocation mi = (MarshalledInvocation) args[0];
            result = policy.filter(mi, result);
         }
      }
      return result;
   }
   
   /**
    * A noop serializable policy
    */
   public class NoopPolicy implements SerializablePolicy
   {
      public Object filter(MarshalledInvocation input, Object result) throws Throwable
      {
         return result;
      }
   }
}