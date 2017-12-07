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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Metadata about a {@link ServiceBinding} that management tools can use. Does
 * not represent the runtime binding information, but rather the metadata
 * used to create the binding. 
 * 
 * @author Brian Stansberry
 */
public class ServiceBindingMetadata implements Comparable<ServiceBindingMetadata>
{
   /**
    * Checks if <code>serviceName</code> can be converted into an
    * ObjectName; if it can, converts it and returns its canonical form.
    * 
    * @param serviceName the service name
    * @return the canonicalized form, or <code>serviceName</code> if it
    *         cannot be converted into an ObjectName.
    */
   public static String canonicalizeServiceName(String serviceName)
   {      
      // If the serviceName looks like an object name, canonicalize it
      try
      {
         ObjectName oname = new ObjectName(serviceName);
         return oname.getCanonicalName();
      }
      catch (MalformedObjectNameException e)
      {
         return serviceName;
      }      
   }
   
   // ----------------------------------------------------------------- Fields
   
   /**
    * The name of the service to which the binding applies.
    */
   private String serviceName;
   
   /** 
    * The name of the binding. A null or empty name implies the default
    * binding for a service.
    */
   private String bindingName;
   
   /** The virtual host name. This is the interface name used to
    construct the bindAddress value. A null value implies bind on any
    interface.
    */
   private String hostName;
   
   /** The port the service should listen on. A 0 value implies an
    anonymous port.
    */
   private int port;
   
   /** The ServiceBindingValueSource implementation class
    */
   private String serviceBindingValueSourceClassName;
   
   /** The ServiceBindingValueSource 
    */
   private ServiceBindingValueSource serviceBindingValueSource;
   
   /** An aribtrary object used to configure the behavior of
    the ServiceBindingValueSource. An example would be an XML Element.
    */
   private Object serviceBindingValueSourceConfig;
   
   /**
    * Whether runtime @{link ServiceBinding}s created from this metadata
    * can alter the port value based on the server on which the binding
    * is running. 
    */
   private boolean fixedPort;
   
   /**
    * Whether runtime @{link ServiceBinding}s created from this metadata
    * can alter the hostName value based on the server on which the binding
    * is running. 
    */
   private Boolean fixedHostName;
   
   /** Description of the binding that can be displayed by management tools */ 
   private String description;
   
   // ------------------------------------------------------------ Constructors
   
   /**
    * Create a new ServiceBindingMetadata.
    */
   public ServiceBindingMetadata() {}
   
   /**
    * Create a new ServiceBindingMetadata with given service name.
    * 
    * @param serviceName the name of the service to which this binding applies.
    *                    Cannot be <code>null</code>
    * 
    * @throws IllegalArgumentException if {@code serviceName} is <code>null</code>
    */
   public ServiceBindingMetadata(String serviceName) 
   {
      this(serviceName, null, null, 0, false, false);
   }
   
   /**
    * Create a new ServiceBindingMetadata with given service and binding names.
    * 
    * @param serviceName the name of the service to which this binding applies.
    *                    Cannot be <code>null</code>
    * @param bindingName qualifier identifying which particular binding within 
    *                    the service this is. May be <code>null</code>
    * 
    * @throws IllegalArgumentException if {@code serviceName} is <code>null</code>
    */
   public ServiceBindingMetadata(String serviceName, String bindingName) 
   {
      this(serviceName, bindingName, null, 0, false, false);
   }
   
   /**
    * Create a new ServiceBindingMetadata with given property values and a
    * non-fixed {@link #isFixedPort() port}. The 
    * {@link #isFixedHostName() hostname is fixed} if the provided {@code hostName}
    * is not <code>null</code>.
    * 
    * @param serviceName the name of the service to which this binding applies.
    *                    Cannot be <code>null</code>
    * @param bindingName qualifier identifying which particular binding within 
    *                    the service this is. May be <code>null</code>
    * @param hostName  the host name or string notation IP address of the 
    *                  interface to bind to
    * @param port      the port to bind to 
    * 
    * @throws IllegalArgumentException if {@code serviceName} is <code>null</code>
    */
   public ServiceBindingMetadata(String serviceName, String bindingName, 
         String hostName, int port) 
   {
      this(serviceName, bindingName, hostName, port, false, hostName != null);
   }
   
   /**
    * Create a new ServiceBindingMetadata with given property values.
    * 
    * @param serviceName the name of the service to which this binding applies.
    *                    Cannot be <code>null</code>
    * @param bindingName qualifier identifying which particular binding within 
    *                    the service this is. May be <code>null</code>
    * @param hostName  the host name or string notation IP address of the 
    *                  interface to bind to
    * @param port      the port to bind to
    * @param fixedPort whether runtime @{link ServiceBinding}s created from this 
    *                  metadata can alter the port value based on the server 
    *                  on which the binding is running.
    * @param fixedHostName whether runtime @{link ServiceBinding}s created from 
    *                      this metadata can alter the hostName value based on 
    *                      the server on which the binding is running. 
    * 
    * @throws IllegalArgumentException if {@code serviceName} is <code>null</code>
    */
   public ServiceBindingMetadata(String serviceName, String bindingName, 
         String hostName, int port, boolean fixedPort, boolean fixedHostName) 
   {
      setServiceName(serviceName);
      setBindingName(bindingName);
      setHostName(hostName);
      setPort(port);
      setFixedPort(fixedPort);
      setFixedHostName(fixedHostName);
   }
   
   /**
    * Create a new ServiceBindingMetadata from a runtime ServiceBinding. The
    * resulting object has a fixed port and host name.
    * 
    * @param binding the binding. Cannot be <code>null</code>
    */
   public ServiceBindingMetadata(ServiceBinding binding)
   {
      this(binding.getServiceName(), binding.getBindingName(), 
            binding.getHostName(), binding.getPort(), true, true);
      setServiceBindingValueSource(binding.getServiceBindingValueSource());
      if (this.serviceBindingValueSourceClassName == null)
      {
         setServiceBindingValueSourceClassName(binding.getServiceBindingValueSourceClassName());
      }
      setServiceBindingValueSourceConfig(binding.getServiceBindingValueSourceConfig());
   }
   
   /**
    * Copy constructor.
    * 
    * @param binding the metadata to copy. Cannot be <code>null</code>
    */
   public ServiceBindingMetadata(ServiceBindingMetadata binding)
   {
      this(binding.getServiceName(), binding.getBindingName(), 
            binding.getHostName(), binding.getPort(), binding.isFixedHostName(), binding.isFixedPort());
      setServiceBindingValueSource(binding.getServiceBindingValueSource());
      if (this.serviceBindingValueSourceClassName == null)
      {
         setServiceBindingValueSourceClassName(binding.getServiceBindingValueSourceClassName());
      }
      setServiceBindingValueSourceConfig(binding.getServiceBindingValueSourceConfig());
   }
   
   // ------------------------------------------------------------  Properties
   
   /**
    * Gets the name of the service to which this binding applies.
    * 
    * @return the name.
    */
   public String getServiceName()
   {
      return serviceName;
   }

   /**
    * Sets the name of the service to which this binding applies.
    * 
    * @param serviceName the name. Cannot be <code>null</code>.
    * 
    * @throws IllegalArgumentException if {@code serviceName} is <code>null</code>
    */
   public void setServiceName(String serviceName)
   {
      if (serviceName == null)
      {
         throw new IllegalArgumentException("serviceName is null");
      }
      this.serviceName = canonicalizeServiceName(serviceName);
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
    * Sets a qualifier identifying which particular binding within 
    * {@link #getServiceName() the service} this is.
    *
    * @param bindingName the name, or <code>null</code> if this is an unnamed 
    *                    default binding for the service.
    */
   public void setBindingName(String bindingName)
   {
      this.bindingName = bindingName;
   }

   /**
    * Gets the fully qualified binding name.
    * 
    * @return the {@link #getServiceName() serviceName}:{@link #getBindingName() bindingName} or
    *         just the service name if the binding name is <code>null</code>.
    *         
    * @throws IllegalStateException if {@link #getServiceName() serviceName} is <code>null</code>
    */
   public String getFullyQualifiedName()
   {      
      if (this.serviceName == null)
      {
         throw new IllegalStateException("Must set serviceName");
      }
      else if (this.bindingName == null)
      {
         return this.serviceName;
      }
      else
      {
         StringBuilder sb = new StringBuilder(this.serviceName);
         if (this.bindingName != null)
         {
            sb.append(':');
            sb.append(this.bindingName);
         }
         
         return sb.toString();
      }
   }

   /**
    * Gets the host name or string notation IP address to use for the binding.
    *
    * @return the hostname or address. May be <code>null</code>
    */
   public String getHostName()
   {
      return hostName;
   }

   /**
    * Sets the host name or string notation IP address to use for the binding.
    *
    * @param hostName the hostname or address. May be <code>null</code>
    */
   public void setHostName(String hostName)
   {
      this.hostName = hostName;
      // Assume that setting a host name means it's meant to be fixed
      if (this.fixedHostName == null)
      {
         setFixedHostName(hostName != null);
      }
   }

   public int getPort()
   {
      return port;
   }

   /**
    * Sets the port to use for the binding.
    *
    * @param port the port
    */
   public void setPort(int port)
   {
      this.port = port;
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
    * Sets the object that can return this ServiceBinding's values in formats
    * usable by consumers.
    * 
    * @param serviceBindingValueSource the ServiceBindingValueSource; may be <code>null</code>
    */
   public void setServiceBindingValueSource(ServiceBindingValueSource serviceBindingValueSource)
   {
      this.serviceBindingValueSource = serviceBindingValueSource;
      if (serviceBindingValueSource != null)
      {
         setServiceBindingValueSourceClassName(serviceBindingValueSource.getClass().getName());
      }
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
    * Sets the fully qualified class name of the {@link #getServiceBindingValueSource() serviceBindingValueSource}.
    * 
    * @param serviceBindingValueSourceClassName the binding value source class, or <code>null</code>
    */
   public void setServiceBindingValueSourceClassName(String serviceBindingValueSourceClassName)
   {
      this.serviceBindingValueSourceClassName = serviceBindingValueSourceClassName;
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
   
   /** 
    * Sets the configuration object the {@link #getServiceBindingValueSource() serviceBindingValueSource}
    * should use.
    * 
    * @param serviceBindingValueSourceConfig the configuration object, or <code>null</code>
    */
   public void setServiceBindingValueSourceConfig(Object serviceBindingValueSourceConfig)
   {
      this.serviceBindingValueSourceConfig = serviceBindingValueSourceConfig;
   }

   /**
    * Gets whether runtime @{link ServiceBinding}s created from this metadata
    * can alter the port value based on the server on which the binding
    * is running.
    * 
    * @return <code>true</code> if the {@link #getPort()} value from this
    *         object must be respected; <code>false</code> if it can be
    *         altered.
    */
   public boolean isFixedPort()
   {
      return fixedPort;
   }

   /**
    * Sets whether runtime @{link ServiceBinding}s created from this metadata
    * can alter the port value based on the server on which the binding
    * is running.
    * 
    * param fixedPort <code>true</code> if the {@link #getPort()} value from this
    *                 object must be respected; <code>false</code> if it can be
    *                 altered.
    */
   public void setFixedPort(boolean fixedPort)
   {
      this.fixedPort = fixedPort;
   }

   /**
    * Gets whether runtime @{link ServiceBinding}s created from this metadata
    * can alter the hostName value based on the server on which the binding
    * is running. 
    * 
    * @return <code>true</code> if the {@link #getHostName()} value from this
    *         object must be respected; <code>false</code> if it can be
    *         altered.
    */
   public boolean isFixedHostName()
   {
      return (this.fixedHostName == null ? this.hostName != null : this.fixedHostName.booleanValue());
   }

   /**
    * Sets whether runtime @{link ServiceBinding}s created from this metadata
    * can alter the hostName value based on the server on which the binding
    * is running. 
    * 
    * param fixedHostName <code>true</code> if the {@link #getHostName()} value 
    *                     from this object must be respected; <code>false</code> 
    *                     if it can be altered.
    */
   public void setFixedHostName(boolean fixedHostName)
   {
      this.fixedHostName = Boolean.valueOf(fixedHostName);
   }

   // -------------------------------------------------------------  Comparable

   public int compareTo(ServiceBindingMetadata other)
   {      
      return getFullyQualifiedName().compareTo(other.getFullyQualifiedName());
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
      
      if (obj instanceof ServiceBindingMetadata)
      {
         ServiceBindingMetadata other = (ServiceBindingMetadata) obj;
         return (this.serviceName != null && this.serviceName.equals(other.serviceName)
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
      StringBuffer sBuf = new StringBuffer("ServiceBindingMetadata [serviceName=");
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
