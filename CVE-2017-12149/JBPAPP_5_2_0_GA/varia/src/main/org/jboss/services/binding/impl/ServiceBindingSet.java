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

package org.jboss.services.binding.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.services.binding.ServiceBindingMetadata;

/**
 * Encapsulates information used to configure a unique set of bindings.
 * 
 * @author Brian Stansberry
 * @version $Revision: 88905 $
 */
public class ServiceBindingSet
{   
   /** The serialVersionUID */
   private static final long serialVersionUID = 765380451233486038L;

   private final String bindingSetName;
   private String defaultHostName;
   private int portOffset;
   private final Set<ServiceBindingMetadata> overrides = new HashSet<ServiceBindingMetadata>(0);
   
   // ------------------------------------------------------------ Constructors
   
   /**
    * Same as ServiceBindingSet(name, null, 0, null)
    */
   public ServiceBindingSet(String name)
   {
      this(name, null, 0, null);
   }
   
   /**
    * Same as ServiceBindingSet(name, null, 0, overrides)
    */
   public ServiceBindingSet(String name, Set<ServiceBindingMetadata> overrides)
   {
      this(name, null, 0, overrides);
   }

   /**
    * Same as ServiceBindingSet(name, null, offset, null)
    */
   public ServiceBindingSet(String name, int offset)
   {
      this(name, null, offset, null);
   }

   /**
    * Same as ServiceBindingSet(name, defaultHostName, offset, null)
    */
   public ServiceBindingSet(String name, String defaultHostName, int offset)
   {
      this(name, defaultHostName, offset, null);
   }
   
   /**
    *  Create a new ServiceBindingSet.
    * 
    * @param name the name of the binding set. Cannot be <code>null</code>
    * @param defaultHostName default host name to use for bindings associated
    *                        with this set. May be <code>null</code>
    * @param offset offset to apply to bindings associdated with this set
    * @param overrides set of bindings whose values should override any matching
    *                  default bindings found in the service binding store
    *                  with which this set is associated
    */
   public ServiceBindingSet(String name, String defaultHostName, 
                            int offset, 
                            Set<ServiceBindingMetadata> overrides)
   {
      if (name == null)
      {
         throw new IllegalArgumentException("name is null");
      }
      
      this.bindingSetName = name;
      this.defaultHostName = defaultHostName;
      this.portOffset = offset;
      
      if (overrides != null)
      {
         for (ServiceBindingMetadata sbm : overrides)
         {
            this.overrides.add(sbm);
         }
      }
   }

   // -------------------------------------------------------------- Properties
   
   public String getName()
   {
      return bindingSetName;
   }

   public String getDefaultHostName()
   {
      return defaultHostName;
   }

   public void setDefaultHostName(String defaultHostName)
   {
      this.defaultHostName = defaultHostName;
   }

   public int getPortOffset()
   {
      return portOffset;
   }

   public void setPortOffset(int portOffset)
   {
      this.portOffset = portOffset;
   }
   
   public Set<ServiceBindingMetadata> getOverrideBindings()
   {
      @SuppressWarnings("unchecked")      
      Set<ServiceBindingMetadata> result = overrides == null ? Collections.EMPTY_SET : overrides;
      return result;
   }
   
}
