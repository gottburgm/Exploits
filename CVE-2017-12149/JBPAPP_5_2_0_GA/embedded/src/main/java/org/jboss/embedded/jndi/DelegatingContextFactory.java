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
package org.jboss.embedded.jndi;

import org.jnp.interfaces.NamingContextFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;

/**
 * abstract class that delegates to a different initial context factory
 * Allows you to reuse other JBoss context factories like the security one.
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public abstract class DelegatingContextFactory implements InitialContextFactory
{
   public static final String INITIAL_CONTEXT_FACTORY_DELEGATE = "jboss.embedded.initial.context.factory.delegate";
   public static final String INITIAL_CONTEXT_FACTORY_DEFAULT = "org.jnp.interfaces.NamingContextFactory";
   private NamingContextFactory delegate;

   protected synchronized NamingContextFactory getDelegate(Hashtable env) throws NamingException
   {
      if (delegate != null) return delegate;
      String factoryName = (String) env.get(INITIAL_CONTEXT_FACTORY_DELEGATE);
      if (factoryName != null)
      {
         try
         {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(factoryName);
            delegate = (NamingContextFactory)clazz.newInstance();
         }
         catch (Exception e)
         {
            throw new RuntimeException("Unable to initialize InitialContextFactory", e);
         }
      }
      else
      {
         delegate = new NamingContextFactory();
      }
      return delegate;
   }

   public Context getInitialContext(Hashtable env) throws NamingException
   {
      return getDelegate(env).getInitialContext(env);
   }
}
