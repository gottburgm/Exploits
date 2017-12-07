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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import javax.ejb.EJBLocalObject;
import javax.naming.InitialContext;

/** Abstract superclass of local interface proxies.

 @author  <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 @version $Revision: 81030 $
 */
public abstract class LocalProxy implements Serializable
{
   // Constants -----------------------------------------------------
   static final long serialVersionUID = 8387750757101826407L;
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   /** An empty method parameter list. */
   protected static final Object[] EMPTY_ARGS = {};

   /** {@link Object#toString} method reference. */
   protected static final Method TO_STRING;
   
   /** {@link Object#hashCode} method reference. */
   protected static final Method HASH_CODE;
   
   /** {@link Object#equals} method reference. */
   protected static final Method EQUALS;
   
   /** {@link EJBLocalObject#getPrimaryKey} method reference. */
   protected static final Method GET_PRIMARY_KEY;
   
   /** {@link EJBLocalObject#getEJBLocalHome} method reference. */
   protected static final Method GET_EJB_HOME;
   
   /** {@link EJBLocalObject#isIdentical} method reference. */
   protected static final Method IS_IDENTICAL;

   /** {@link EJBLocalObject#remove} method reference. */
   protected static final Method REMOVE;

   protected String jndiName;
   protected transient BaseLocalProxyFactory factory;

   /**
    * Initialize {@link EJBLocalObject} method references.
    */
   static
   {
      try
      {
         final Class[] empty = {};
         final Class type = EJBLocalObject.class;
         
         GET_PRIMARY_KEY = type.getMethod("getPrimaryKey", empty);
         GET_EJB_HOME = type.getMethod("getEJBLocalHome", empty);
         IS_IDENTICAL = type.getMethod("isIdentical", new Class[] { type });
         REMOVE = type.getMethod("remove", empty);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
   
   /**
    * Initialize {@link Object} method references.
    */
   static
   {
      try
      {
         final Class[] empty = {};
         final Class type = Object.class;
         
         TO_STRING = type.getMethod("toString", empty);
         HASH_CODE = type.getMethod("hashCode", empty);
         EQUALS = type.getMethod("equals", new Class[] { type });
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
   
   protected String getJndiName()
   {
      return jndiName;
   }
   protected abstract Object getId();
   
   public LocalProxy(String jndiName, BaseLocalProxyFactory factory)
   {
      this.jndiName = jndiName;
      this.factory = factory;
   }

   /**
    * Test the identitiy of an <tt>EJBObject</tt>.
    *
    * @param a    <tt>EJBObject</tt>.
    * @param b    Object to test identity with.
    * @return     True if objects are identical.
    *
    * @throws ClassCastException   Not an EJBObject instance.
    */
   Boolean isIdentical(final Object a, final Object b)
   {
      final EJBLocalObject ejb = (EJBLocalObject)a;
      Boolean isIdentical = Boolean.FALSE;
      if( ejb != null )
      {
         isIdentical = new Boolean(ejb.toString().equals(b));
      }
      return isIdentical;
   }

   /**
    * Implementation of toString for EJBLocalObject.
    * @return String representation of EJBLocalObject.
    */
   String toStringImpl()
   {
      return jndiName + ":" + getId();
   }

   public Object invoke(final Object proxy, final Method m, Object[] args)
      throws Throwable
   {
      Object id = getId();
      Object retValue = null;

      // Implement local methods
      if (m.equals(TO_STRING))
      {
         retValue = toStringImpl();
      }
      else if (m.equals(EQUALS))
      {
         retValue = invoke(proxy, IS_IDENTICAL, args );
      }
      else if (m.equals(HASH_CODE))
      {
         retValue = new Integer(id.hashCode());
      }
      
      // Implement local EJB calls
      else if (m.equals(GET_PRIMARY_KEY))
      {
         retValue = id;
      }
      else if (m.equals(GET_EJB_HOME))
      {
         InitialContext ctx = new InitialContext();
         return ctx.lookup(jndiName);
      }
      else if (m.equals(IS_IDENTICAL))
      {
         retValue = isIdentical(args[0], toStringImpl());
      }
      return retValue;
   }

   /** Restore the jndiName using default serialization and then lookup
    the BaseLocalProxyFactory using the jndiName
    */
   private void readObject(ObjectInputStream in)
     throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      factory = (BaseLocalProxyFactory) BaseLocalProxyFactory.invokerMap.get(jndiName);
   }

   private void writeObject(ObjectOutputStream out)
      throws IOException
   {
      out.defaultWriteObject();
   }

}
