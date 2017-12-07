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
package org.jboss.ejb.plugins.lock;

import java.io.Serializable;
import java.io.ObjectStreamException;

/**
 * This type safe enumeration s used to mark an invocation as non-entrant.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 81030 $
 */
public final class Entrancy implements Serializable
{
   public static final Entrancy ENTRANT = new Entrancy(true);
   public static final Entrancy NON_ENTRANT = new Entrancy(false);

   private final transient boolean value;
    
   private Entrancy(boolean value)
   {
      this.value = value;
   }

   public String toString()
   {
      if(value)
      {
         return "ENTRANT";
      } else
      {
         return "NON_ENTRANT";
      }
   }

   Object readResolve() throws ObjectStreamException
   {
      if(value)
      {
         return ENTRANT;
      } else
      {
         return NON_ENTRANT;
      }
   }

}

