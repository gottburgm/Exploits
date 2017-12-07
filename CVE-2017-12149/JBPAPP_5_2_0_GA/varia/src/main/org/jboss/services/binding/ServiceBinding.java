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
package org.jboss.services.binding;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

/** 
 * A ServiceBinding is a {name,virtualHost,port,interfaceAddress}
 * quad specifying a named binding for a service.
 *
 * @author <a href="mailto:bitpushr@rochester.rr.com">Mike Finn</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 88012 $
 */
public class ServiceBinding implements Comparable<ServiceBinding>
{
   
   // ----------------------------------------------------------------- Fields
   
   /**
    * The name of the service to which the binding applies.
    */
   private final String serviceName;
   
   /** 
    * The name of the binding. A null or empty name implies the default
    * binding for a service.
    */
   private final String bindingName;
   
   /** The bindingName, if any, concatenated to serviceName */
   private final String fullyQualifiedName;
   
   /** The virtual host name. This is the interface name used to
    construct the bindAddress value. A null value implies bind on any
    interface.
    */
   private final String hostName;
   
   /** The port the service should listen on. A 0 value implies an
    anonymous port.
    */
   private final int port;
   
   /** The interface on which the service should bind its listening port. A
    null address implies bind on any interface.
    */
   private final InetAddress bindAddress;
   
   /** Description of the binding that can be displayed by management tools */ 
   private String description;
   
   /** The ServiceBindingValueSource implementation class
    */
   private final String serviceBindingValueSourceClassName;
   
   /** The ServiceBindingValueSource 
    */
   private final ServiceBindingValueSource serviceBindingValueSource;
   
   /** An aribtrary object used to configure the behavior of
    the ServiceBindingValueSource. An example would be an XML Element.
    */
   private final Object serviceBindingValueSourceConfig;

   // -----------------------------------------------------------  Constructors
   
   /**
    * Build a ServiceBinding from metadata.
    * 
    * @param metadata the binding metadata. Cannot be <code>null</code>
    * @param defaultHostName host name to use if the metadata's hostname is not
    *                    {@link ServiceBindingMetadata#isFixedHostName() fixed}
    * @param portOffset offset to apply to the metadata port value if it is not 
    *                    {@link ServiceBindingMetadata#isFixedPort() fixed}
    *                                           
    * @throws UnknownHostException  if no IP address for the <code>hostName</code> could be found
    * 
    * @throws IllegalArgumentException if {@code metadata} is <code>null</code>ll
    * @throws IllegalStateException if metadata's {@code serviceName} is <code>null</code>
    * @throws RuntimeException if a {@code serviceBindingValueSourceClassName} 
    *                          is provided but there is a problem instantiating
    *                          an instance of it via {@link Class#newInstance()}
    */
   public ServiceBinding(ServiceBindingMetadata metadata, String defaulHostName, int portOffset) 
         throws UnknownHostException
   {
      if (metadata == null)
      {
         throw new IllegalArgumentException("metadata is null");
      }      
      
      if (metadata.getServiceName() == null)
      {
         throw new IllegalStateException("metadata's serviceName is null");
      }
      
      this.serviceName = metadata.getServiceName();
      this.bindingName = metadata.getBindingName();
      this.fullyQualifiedName = (bindingName == null) ? serviceName : serviceName + ":" + bindingName;
      this.port = metadata.isFixedPort() ? metadata.getPort() : metadata.getPort() + portOffset;
      this.hostName = metadata.isFixedHostName() ? metadata.getHostName() : defaulHostName;
      this.bindAddress = InetAddress.getByName(this.hostName);
      this.description = metadata.getDescription();
      
      this.serviceBindingValueSourceConfig = metadata.getServiceBindingValueSourceConfig();

      ServiceBindingValueSource valueSource = metadata.getServiceBindingValueSource();
      if (valueSource == null)
      {
         this.serviceBindingValueSourceClassName = metadata.getServiceBindingValueSourceClassName();
         if (this.serviceBindingValueSourceClassName != null)
         {
            // Try and instantiate the value source
            try
            {
               this.serviceBindingValueSource = AccessController.doPrivileged(
                     new PrivilegedExceptionAction<ServiceBindingValueSource>() {
      
                  public ServiceBindingValueSource run() throws Exception
                  {
                     ClassLoader loader = Thread.currentThread().getContextClassLoader();
                     Class<?> delegateClass = loader.loadClass(serviceBindingValueSourceClassName);
                     return (ServiceBindingValueSource) delegateClass.newInstance();
                  }               
               });
            }
            catch (RuntimeException e)
            {
               throw e;
            }
            catch (Exception e)
            {
               throw new RuntimeException("Failed creating ServiceBindingValueSource of type " + 
                     serviceBindingValueSourceClassName, e);
            }
         }
         else
         {
            // The standard case; just not configured at all
            this.serviceBindingValueSource = null;
         }
      }
      else
      {
         this.serviceBindingValueSource = valueSource;
         this.serviceBindingValueSourceClassName = valueSource.getClass().getName();
      }

   }

   // -------------------------------------------------------------  Properties
   
   /**
    * Gets the name of the service to which this binding applies.
    * 
    * @return the name. Will not be <code>null</code>.
    */
   public String getServiceName()
   {
      return serviceName;
   }

   /**
    * Gets a qualifier identifying which particular binding within 
    * {@link #getServiceName() the service} this is.
    *
    * @return the name, or <code>null</code> if this is an unnamed default binding
    *         for the service.
    */
   public String getBindingName()
   {
      return this.bindingName;
   }
   
   /**
    * Gets the fully qualified binding name.
    * 
    * @return the {@link #getServiceName() serviceName}:{@link #getBindingName() bindingName} or
    *         just the service name if the binding name is <code>null</code>.
    */
   public String getFullyQualifiedName()
   {
      return fullyQualifiedName;
   }

   /**
    * Gets the host name or string notation IP address to use for the binding.
    *
    * @return the hostname or address
    */
   public String getHostName()
   {
      return this.hostName;
   }

   /**
    * Gets the port to use for the binding.
    *
    * @return The port
    */
   public int getPort()
   {
      return this.port;
   }

   /**
    * Gets the InetAddress of the interface to use for the binding.
    *
    * @return  The binding address
    */
   public InetAddress getBindAddress()
   {
      return this.bindAddress;
   }   
   
   /**
    * Gets a description of the binding suitable for display by management tools.
    * 
    * @return the description, or <code>null</code> if there isn't one
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Sets a description of the binding suitable for display by management tools.
    * 
    * @param description the description; may be <code>null</code>
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * Gets the object that can return this ServiceBinding's values in formats
    * usable by consumers. If unset (the norm), {@link ServiceBindingManager} will use
    * reasonable defaults based on the format requested by the consumer.
    * 
    * @return the ServiceBindingValueSource; may be <code>null</code>
    */
   public synchronized ServiceBindingValueSource getServiceBindingValueSource()
   {
      return this.serviceBindingValueSource;
   }

   /** 
    * Gets the fully qualified class name of the {@link #getServiceBindingValueSource() serviceBindingValueSource}.
    * 
    * @return the binding value source class, or <code>null</code>
    */
   public String getServiceBindingValueSourceClassName()
   {
      return serviceBindingValueSourceClassName;
   }

   /** 
    * Gets the configuration object the {@link #getServiceBindingValueSource() serviceBindingValueSource}
    * should use.
    * 
    * @return the configuration object, or <code>null</code>
    */
   public Object getServiceBindingValueSourceConfig()
   {
      return serviceBindingValueSourceConfig;
   }

   // -------------------------------------------------------------  Comparable

   public int compareTo(ServiceBinding o)
   {      
      return getFullyQualifiedName().compareTo(o.getFullyQualifiedName());
   }

   // --------------------------------------------------------------  Overrides
   
   /**
    * Equality is based on our serviceName and our bindingName.
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      
      if (obj instanceof ServiceBinding)
      {
         ServiceBinding other = (ServiceBinding) obj;
         return (this.serviceName.equals(other.serviceName)
                 && safeEquals(this.bindingName, other.bindingName));
      }
      
      return false;
   }

   /**
    * Hashcode is based on our serviceName and our bindingName.
    */
   @Override
   public int hashCode()
   {
      int result = 19;
      result += 29 * this.serviceName.hashCode();
      result += 29 * (this.bindingName == null ? 0 : this.bindingName.hashCode());
      return result;
   }

   /**
    * Create string representation of the service descriptor
    *
    * @return  String containing service descriptor properties
    */
   public String toString()
   {
      StringBuffer sBuf = new StringBuffer("ServiceBinding [serviceName=");
      sBuf.append(this.serviceName);
      sBuf.append(";bindingName=");

      sBuf.append(this.getBindingName());
      sBuf.append(";hostName=");
      String host = getHostName();

      if (hostName == null)
      {
         host = "<ANY>";
      }
      sBuf.append(host);
      sBuf.append(";bindAddress=");
      sBuf.append(this.getBindAddress().toString());
      sBuf.append(";port=");
      sBuf.append(this.getPort());
      sBuf.append("]");
      return sBuf.toString();
   }

   // ----------------------------------------------------------------  Private
   
   private boolean safeEquals(Object a, Object b)
   {
      return (a == b || (a != null && a.equals(b)));
   }
}
