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
package org.jboss.managed.plugins.advice;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 * WrapperSet.
 * 
 * @param <T> the interface type
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85526 $
 */
class WrapperSet<T> extends AbstractSet<T> implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -5588975054846538928L;

   /** The delegate */
   private Set<T> delegate;

   /** The interface class */
   private Class<T> interfaceClass;
   
   /**
    * Create a new WrapperSet.
    * 
    * @param delegate the delegate
    * @param interfaceClass the interface class
    */
   public WrapperSet(Set<T> delegate, Class<T> interfaceClass)
   {
      if (delegate == null)
         throw new IllegalArgumentException("Null delegate");
      if (interfaceClass == null)
         throw new IllegalArgumentException("Null interface class");

      this.delegate = delegate;
      this.interfaceClass = interfaceClass;
   }

   public Iterator<T> iterator()
   {
      return new WrapperIterator<T>(delegate.iterator(), interfaceClass);
   }

   public int size()
   {
      return delegate.size();
   }
}
