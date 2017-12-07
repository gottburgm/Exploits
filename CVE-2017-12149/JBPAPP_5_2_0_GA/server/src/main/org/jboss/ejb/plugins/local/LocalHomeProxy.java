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
package org.jboss.ejb.plugins.local;

import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.RemoveException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * The proxy for an EJBLocalHome object.
 *
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @version $Revision: 81030 $
 */
public class LocalHomeProxy
   extends LocalProxy
   implements InvocationHandler
{
   static final long serialVersionUID = 1762319499924478521L;

   /** {@link javax.ejb.EJBHome#remove(Object)} method reference. */
   protected static final Method REMOVE_BY_PRIMARY_KEY;
   
   /** {@link javax.ejb.EJBObject#remove} method reference. */
   protected static final Method REMOVE_OBJECT;
   
   /**
    * Initialize {@link javax.ejb.EJBHome} and {@link javax.ejb.EJBObject} method references.
    */
   static
   {
      try
      {
         final Class empty[] = {};
         final Class type = EJBLocalHome.class;
         
         REMOVE_BY_PRIMARY_KEY = type.getMethod("remove", new Class[] {Object.class});
         
         // Get the "remove" method from the EJBObject
         REMOVE_OBJECT = EJBLocalObject.class.getMethod("remove", empty);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
   
   public LocalHomeProxy(String jndiName, BaseLocalProxyFactory factory)
   {
      super(jndiName, factory);
   }

   protected Object getId()
   {
      return jndiName;
   }

   /**
    * InvocationHandler implementation.
    *
    * @param proxy   The proxy object.
    * @param m       The method being invoked.
    * @param args    The arguments for the method.
    *
    * @throws Throwable    Any exception or error thrown while processing.
    */
   public Object invoke(final Object proxy, final Method m,
      Object[] args)
      throws Throwable
   {
      Object retValue = null;

      if (args == null)
         args = EMPTY_ARGS;

      // Implement local methods
      if (m.equals(TO_STRING))
      {
         retValue = jndiName + "Home";
      }
      else if (m.equals(EQUALS))
      {
         // equality of the proxy home is based on names...
         Object temp = invoke(proxy, TO_STRING, args);
         retValue = new Boolean(temp.equals(jndiName + "Home"));
      }
      else if (m.equals(HASH_CODE))
      {
         retValue = new Integer(this.hashCode());
      }
      else if (m.equals(REMOVE_BY_PRIMARY_KEY))
      {
         try
         {
            // The trick is simple we trick the container in believe it
            // is a remove() on the instance
            Object id = args[0];
            retValue = factory.invoke(id, REMOVE_OBJECT, EMPTY_ARGS);
         }
         catch (Exception e)
         {
            RemoveException re = new RemoveException(e.getMessage());
            re.initCause(e);
            throw re;
         }
      }
      // If not taken care of, go on and call the container
      else
      {
         retValue = factory.invokeHome(m, args);
      }

      return retValue;
   }
}
