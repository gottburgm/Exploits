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
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Set;

import org.jboss.services.binding.impl.SimpleServiceBindingValueSourceImpl;
import org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceImpl;
import org.jboss.services.binding.impl.Util;
import org.jboss.services.binding.impl.XSLTServiceBindingValueSourceConfig;
import org.jboss.services.binding.impl.XSLTServiceBindingValueSourceImpl;
import org.w3c.dom.Element;

/** 
 * The services configuration binding manager implementation.
 *
 * <p>The ServiceBindingManager enables the centralized management
 * of ports, by service. The port configuration store is abstracted out
 * using the ServiceBindingStore interface.
 *
 * @version $Revision: 88905 $
 * @author  <a href="mailto:bitpushr@rochester.rr.com">Mike Finn</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author Brian Stansberry
 *
 * @jmx:mbean
 */
public class ServiceBindingManager
   implements ServiceBindingManagerMBean
{  
   // -----------------------------------------------------------------  Static
   
   /** Enumeration of types of binding requests */
   public enum BindingType { INT, INETADDRESS, STRING, ELEMENT, URL, RESOURCE, GENERIC };
   
   /**
    * Algorithm for obtaining a {@link ServiceBindingValueSource} given a particular
    * binding and binding type.
    * 
    * @param binding the binding
    * @param bindingType the binding type
    * @return the appropriate {@link ServiceBindingValueSource}. Will not return <code>null</code>.
    * 
    * @throws ClassNotFoundException if any {@link ServiceBinding#getServiceBindingValueSourceClassName()} cannot be found
    * @throws InstantiationException if any {@link ServiceBinding#getServiceBindingValueSourceClassName()} cannot be instantiated
    * @throws IllegalAccessException if any {@link ServiceBinding#getServiceBindingValueSourceClassName()} is not public
    * @throws IllegalStateException if no appropriate ServiceBindingValueSource can be identified
    */
   public static ServiceBindingValueSource getServiceBindingValueSource(ServiceBinding binding, BindingType bindingType)
   {
      ServiceBindingValueSource source = binding.getServiceBindingValueSource();
      if (source == null)
      {
         switch (bindingType)
         {
            case INT:
            case INETADDRESS:
               source = new SimpleServiceBindingValueSourceImpl();
               break;
            case STRING:
               source = new StringReplacementServiceBindingValueSourceImpl();
               break;
            case ELEMENT:
            case URL:
            case RESOURCE:
               Object config = binding.getServiceBindingValueSourceConfig();
               if (config instanceof XSLTServiceBindingValueSourceConfig)
               {
                  source = new XSLTServiceBindingValueSourceImpl();
               }
               else
               {
                  source = new StringReplacementServiceBindingValueSourceImpl();
               }
               break;
            default:
               throw new IllegalStateException("No ServiceBindingValueSource configured for " + 
                                               binding + " and no default source available for binding of type " +
                                               bindingType);
         }         
      }
      return source;
   }

   // ----------------------------------------------------------------  Fields
   
   /** 
    * The name of the config set this manager is associated with. This is a
    * logical name used to lookup ServiceBindings from the ServiceBindingStore.
    */
   private String serverName;
   
   /** The ServiceBindingStore instance  */
   private final ServiceBindingStore store;

   // -----------------------------------------------------------  Constructors
   
   public ServiceBindingManager(String serverName, ServiceBindingStore store)
   {
      if (serverName == null)
         throw new IllegalArgumentException("serverName is null");
      if (store == null)
         throw new IllegalArgumentException("store is null");
      
      this.serverName = serverName;
      this.store = store;
   }

   // -------------------------------------------------------------  Properties
   
   /**
    * Gets the value of the <code>serverName</code> param this instance should pass 
    * to <code>ServiceBindingStore</code> when 
    * {@link ServiceBindingStore#getServiceBinding(String, String, String) requesting bindings}.
    * 
    * @return name of the set of bindings this server uses
    * 
    * @jmx:attribute
    */
   public String getServerName()
   {
      return this.serverName;
   }
   
   /**
    * Sets the value of the <code>serverName</code> param this instance should pass 
    * to <code>ServiceBindingStore</code> when 
    * {@link ServiceBindingStore#getServiceBinding(String, String, String) requesting bindings}.
    * 
    * @return name of the set of bindings this server uses. Cannot be <code>null</code>
    * 
    * @throws IllegalArgumentException if <code>serverName</code> is <code>null</code>
    */
   public void setServerName(String serverName)
   {
      if (serverName == null)
      {
         throw new IllegalArgumentException("serverName is null");
      }
      this.serverName = serverName;
   }

   public Set<ServiceBinding> getServiceBindings()
   {
      return this.store.getServiceBindings(this.serverName);
   }

   // ----------------------------------------------------------------- Public
   
   /**
    * Gets the <code>int</code> binding value for the
    * <code>ServiceBinding</code> with the given <code>serviceName</code> 
    * and no binding name qualifier.
    * <p>
    * This is typically the {@link ServiceBinding#getPort() port}.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    *                    
    * @return the binding value as an <code>int</code>
    *  
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    * 
    * @see IntServiceBindingValueSource
    */
   public int getIntBinding(String serviceName) throws NoSuchBindingException
   {
      return getIntBinding(serviceName, null);
   }
   
   /**
    * Gets the <code>int</code> binding value for the
    * <code>ServiceBinding</code> with the given <code>serviceName</code> 
    * and <code>bindingName</code> qualifier.
    * <p>
    * This is typically the {@link ServiceBinding#getPort() port}.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    *                    
    * @return the binding value as an <code>int</code>
    * 
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    *                    
    * @see IntServiceBindingValueSource
    */
   public int getIntBinding(String serviceName, String bindingName) throws NoSuchBindingException
   {
      ServiceBinding binding = store.getServiceBinding(serverName, serviceName, bindingName);
      ServiceBindingValueSource source = getServiceBindingValueSource(binding, BindingType.INT);
      if (source instanceof IntServiceBindingValueSource)
      {
         return ((IntServiceBindingValueSource) source).getIntServiceBindingValue(binding);
      }
      else
      {
         return Util.getBindingValue(source, binding, Number.class).intValue();             
      }
   }
   
   /**
    * Same as {@link #getIntBinding(String, String)} but, if no matching
    * service binding is found, creates a new one using the given
    * <code>hostName</code> and <code>basePort</code>.
    *  
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param hostName    Host name to use for new service binding if one is
    *                    created.
    * @param basePort    base port to use for the binding; ServiceBindingStore
    *                    may adjust this.
    *                  
    * @return the binding value as an <code>int</code>
    * 
    * @throws DuplicateServiceException in unlikely event of concurrent attempts
    *                                   to create same binding with different
    *                                   binding values                                   
    * @throws UnknownHostException if no IP address for the <code>hostName</code> could be found 
    */
   public int getIntBinding(String serviceName, String bindingName, 
         String hostName, int basePort) throws UnknownHostException, DuplicateServiceException
   {
      return getIntBinding(serviceName, bindingName, hostName, basePort, false, hostName != null);
   }
   
   /**
    * Same as {@link #getIntBinding(String, String)} but, if no matching
    * service binding is found, creates a new one using the given
    * <code>hostName</code> and <code>basePort</code>.
    *  
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param hostName    Host name to use for new service binding if one is
    *                    created.
    * @param basePort    base port to use for the binding; ServiceBindingStore
    *                    may adjust this.
    * @param fixedPort whether runtime @{link ServiceBinding}s created from this 
    *                  metadata can alter the port value based on the server 
    *                  on which the binding is running.
    * @param fixedHostName whether runtime @{link ServiceBinding}s created from 
    *                      this metadata can alter the hostName value based on 
    *                      the server on which the binding is running. 
    *                  
    * @return the binding value as an <code>int</code>
    * 
    * @throws DuplicateServiceException in unlikely event of concurrent attempts
    *                                   to create same binding with different
    *                                   binding values                                   
    * @throws UnknownHostException if no IP address for the <code>hostName</code> could be found 
    */
   public int getIntBinding(String serviceName, String bindingName, 
         String hostName, int basePort, boolean fixedPort, boolean fixedHostName) throws UnknownHostException, DuplicateServiceException
   {
      try
      {
         return getIntBinding(serviceName, bindingName);
      }
      catch (NoSuchBindingException e)
      {
         createBindingFromDefaults(serviceName, bindingName, hostName, basePort, fixedPort, fixedHostName);
         
         try
         {
            return getIntBinding(serviceName, bindingName);
         }
         catch (NoSuchBindingException e1)
         {
            // Shouldn't be possible
            throw new IllegalStateException("Newly created binding not found", e1);
         }
      }
   }
   
   /**
    * Gets the <code>InetAddress</code> binding value for the
    * <code>ServiceBinding</code> with the given <code>serviceName</code> 
    * and no binding name qualifier.
    * <p>
    * This is typically the {@link ServiceBinding#getBindAddress() bind address}.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    *   
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    *                   
    * @see InetAddressServiceBindingValueSource
    */
   public InetAddress getInetAddressBinding(String serviceName) throws NoSuchBindingException
   {
      return getInetAddressBinding(serviceName, null);
   }
   
   /**
    * Gets the <code>InetAddress</code> binding value for the
    * <code>ServiceBinding</code> with the given <code>serviceName</code>
    * and <code>bindingName</code> qualifier.
    * <p>
    * This is typically the {@link ServiceBinding#getBindAddress() bind address}.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    *      
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    *                
    * @see InetAddressServiceBindingValueSource
    */
   public InetAddress getInetAddressBinding(String serviceName, String bindingName) throws NoSuchBindingException
   {
      ServiceBinding binding = store.getServiceBinding(serverName, serviceName, bindingName);
      ServiceBindingValueSource source = getServiceBindingValueSource(binding, BindingType.INETADDRESS);
      if (source instanceof InetAddressServiceBindingValueSource)
      {
         return ((InetAddressServiceBindingValueSource) source).getInetAddressServiceBindingValue(binding);
      }
      else
      {
         return Util.getBindingValue(source, binding, InetAddress.class);
      }
   }
   
   /**
    * Same as {@link #getInetAddressBinding(String, String)} but, if no matching
    * service binding is found, creates a new one using the given
    * <code>hostName</code> and <code>basePort</code>.
    *  
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param hostName    Host name to use for new service binding if one is
    *                    created.
    * @param basePort    base port to use for the binding; ServiceBindingStore
    *                    may adjust this.
    *                  
    * @return the binding value as an <code>InetAddress</code>
    * 
    * @throws DuplicateServiceException in unlikely event of concurrent attempts
    *                                   to create same binding with different
    *                                   binding values
    *                                   
    * @throws UnknownHostException if no IP address for the <code>hostName</code> could be found
    */
   public InetAddress getInetAddressBinding(String serviceName, String bindingName, 
         String hostName, int basePort) throws UnknownHostException, DuplicateServiceException
   {
      return getInetAddressBinding(serviceName, bindingName, hostName, basePort, false, hostName != null);
   }
   
   /**
    * Same as {@link #getInetAddressBinding(String, String)} but, if no matching
    * service binding is found, creates a new one using the given
    * <code>hostName</code> and <code>basePort</code>.
    *  
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param hostName    Host name to use for new service binding if one is
    *                    created.
    * @param basePort    base port to use for the binding; ServiceBindingStore
    *                    may adjust this.
    * @param fixedPort whether runtime @{link ServiceBinding}s created from this 
    *                  metadata can alter the port value based on the server 
    *                  on which the binding is running.
    * @param fixedHostName whether runtime @{link ServiceBinding}s created from 
    *                      this metadata can alter the hostName value based on 
    *                      the server on which the binding is running. 
    *                  
    * @return the binding value as an <code>InetAddress</code>
    * 
    * @throws DuplicateServiceException in unlikely event of concurrent attempts
    *                                   to create same binding with different
    *                                   binding values
    *                                   
    * @throws UnknownHostException if no IP address for the <code>hostName</code> could be found
    */
   public InetAddress getInetAddressBinding(String serviceName, String bindingName, 
         String hostName, int basePort, boolean fixedPort, boolean fixedHostName) throws UnknownHostException, DuplicateServiceException
   {
      try
      {
         return getInetAddressBinding(serviceName, bindingName);
      }
      catch (NoSuchBindingException e)
      {
         createBindingFromDefaults(serviceName, bindingName, hostName, basePort, fixedPort, fixedHostName);
         
         try
         {
            return getInetAddressBinding(serviceName, bindingName);
         }
         catch (NoSuchBindingException e1)
         {
            // Shouldn't be possible
            throw new IllegalStateException("Newly created binding not found", e1);
         }
      }
   }
   
   /**
    * Gets the <code>String</code> binding value for the
    * <code>ServiceBinding</code> with the given <code>serviceName</code> 
    * and no binding name qualifier.
    * <p>
    * This is typically the {@link ServiceBinding#getHostName() host name}.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    *              
    * @return the raw binding value.
    *      
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    *                
    * @see StringServiceBindingValueSource
    */
   public String getStringBinding(String serviceName) throws NoSuchBindingException
   {
      return getStringBinding(serviceName, null, null);
   }
   
   /**
    * Gets the <code>String</code> binding value for the
    * <code>ServiceBinding</code> with the given <code>serviceName</code> 
    * and no binding name qualifier.
    * <p>
    * This is typically the {@link ServiceBinding#getHostName() host name}.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param input string that should be used as a source for transformations 
    *              (e.g. string replacement), or <code>null</code> if no
    *              transformation is needed
    *              
    * @return the raw binding value, or a transformed string based on the raw
    *         binding value and <code>input</code>.
    *      
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    *                
    * @see StringServiceBindingValueSource
    */
   public String getStringBinding(String serviceName, String input) throws NoSuchBindingException
   {
      return getStringBinding(serviceName, null, input);
   }
   
   /**
    * Gets the <code>String</code> binding value for the
    * <code>ServiceBinding</code> with the given <code>serviceName</code>
    * and <code>bindingName</code> qualifier.
    * <p>
    * This is typically the {@link ServiceBinding#getHostName() host name}.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param input string that should be used as a source for transformations 
    *              (e.g. string replacement), or <code>null</code> if no
    *              transformation is needed
    *              
    * @return the raw binding value, or a transformed string based on the raw
    *         binding value and <code>input</code>.
    *      
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    *                
    * @see StringServiceBindingValueSource
    */
   public String getStringBinding(String serviceName, String bindingName, String input) throws NoSuchBindingException
   {
      ServiceBinding binding = store.getServiceBinding(serverName, serviceName, bindingName);
      ServiceBindingValueSource source = getServiceBindingValueSource(binding, BindingType.STRING);
      if (source instanceof StringServiceBindingValueSource)
      {
         return ((StringServiceBindingValueSource) source).getStringServiceBindingValue(binding, input);
      }
      else
      {
         return Util.getBindingValueWithInput(source, binding, input, String.class);             
      }
   }
   
   /**
    * Same as {@link #getStringBinding(String, String, String)} but, if no matching
    * service binding is found, creates a new one using the given
    * <code>hostName</code> and <code>basePort</code>.
    *  
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param input string that should be used as a source for transformations 
    *              (e.g. string replacement), or <code>null</code> if no
    *              transformation is needed
    * @param hostName    Host name to use for new service binding if one is
    *                    created.
    * @param basePort    base port to use for the binding; ServiceBindingStore
    *                    may adjust this.
    *              
    * @return the raw binding value, or a transformed string based on the raw
    *         binding value and <code>input</code>.
    * 
    * @throws DuplicateServiceException in unlikely event of concurrent attempts
    *                                   to create same binding with different
    *                                   binding values
    *                                   
    * @throws UnknownHostException if no IP address for the <code>hostName</code> could be found 
    */
   public String getStringBinding(String serviceName, String bindingName, String input, 
         String hostName, int basePort) throws UnknownHostException, DuplicateServiceException
   {
      return getStringBinding(serviceName, bindingName, input, hostName, basePort, false, hostName != null);
   }
   
   /**
    * Same as {@link #getStringBinding(String, String, String)} but, if no matching
    * service binding is found, creates a new one using the given
    * <code>hostName</code> and <code>basePort</code>.
    *  
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param input string that should be used as a source for transformations 
    *              (e.g. string replacement), or <code>null</code> if no
    *              transformation is needed
    * @param hostName    Host name to use for new service binding if one is
    *                    created.
    * @param basePort    base port to use for the binding; ServiceBindingStore
    *                    may adjust this.
    * @param fixedPort whether runtime @{link ServiceBinding}s created from this 
    *                  metadata can alter the port value based on the server 
    *                  on which the binding is running.
    * @param fixedHostName whether runtime @{link ServiceBinding}s created from 
    *                      this metadata can alter the hostName value based on 
    *                      the server on which the binding is running. 
    *              
    * @return the raw binding value, or a transformed string based on the raw
    *         binding value and <code>input</code>.
    * 
    * @throws DuplicateServiceException in unlikely event of concurrent attempts
    *                                   to create same binding with different
    *                                   binding values
    *                                   
    * @throws UnknownHostException if no IP address for the <code>hostName</code> could be found 
    */
   public String getStringBinding(String serviceName, String bindingName, String input, 
         String hostName, int basePort, boolean fixedPort, boolean fixedHostName) throws UnknownHostException, DuplicateServiceException
   {
      try
      {
         return getStringBinding(serviceName, bindingName, input);
      }
      catch (NoSuchBindingException e)
      {
         createBindingFromDefaults(serviceName, bindingName, hostName, basePort, fixedPort, fixedHostName);
         
         try
         {
            return getStringBinding(serviceName, bindingName, input);
         }
         catch (NoSuchBindingException e1)
         {
            // Shouldn't be possible
            throw new IllegalStateException("Newly created binding not found", e1);
         }
      }
   }
   
   /**
    * Gets an <code>Element</code> containing the binding values for the
    * <code>ServiceBinding</code> with the given <code>serviceName</code> 
    * and no binding name qualifier.
    * <p>
    * Used to perform transformations on values embedded in DOM elements.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param input element that should be used as a source for transformations 
    *              
    * @return transformed element based on the raw binding value(s) and <code>input</code>.
    *      
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    *                
    * @see ElementServiceBindingValueSource
    */
   public Element getElementBinding(String serviceName, Element input) throws NoSuchBindingException
   {
      return getElementBinding(serviceName, null, input);
   }
   
   /**
    * Gets an <code>Element</code> containing the binding values for the
    * <code>ServiceBinding</code> with the given <code>serviceName</code> 
    * and <code>bindingName</code> qualifier.
    * <p>
    * Used to perform transformations on values embedded in DOM elements.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param input element that should be used as a source for transformations 
    *              
    * @return transformed element based on the raw binding value(s) and <code>input</code>.
    *      
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    *                
    * @see ElementServiceBindingValueSource
    */
   public Element getElementBinding(String serviceName, String bindingName, Element input) throws NoSuchBindingException
   {
      ServiceBinding binding = store.getServiceBinding(serverName, serviceName, bindingName);
      ServiceBindingValueSource source = getServiceBindingValueSource(binding, BindingType.ELEMENT);
      if (source instanceof ElementServiceBindingValueSource)
      {
         return ((ElementServiceBindingValueSource) source).getElementServiceBindingValue(binding, input);
      }
      else
      {
         return Util.getBindingValueWithInput(source, binding, input, Element.class);              
      }
   }
   
   /**
    * Same as {@link #getElementBinding(String, String, Element)} but, if no matching
    * service binding is found, creates a new one using the given
    * <code>hostName</code> and <code>basePort</code>.
    *  
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param input string that should be used as a source for transformations 
    *              (e.g. string replacement), or <code>null</code> if no
    *              transformation is needed
    * @param hostName    Host name to use for new service binding if one is
    *                    created.
    * @param basePort    base port to use for the binding; ServiceBindingStore
    *                    may adjust this.
    *              
    * @return transformed element based on the raw binding value(s) and <code>input</code>.
    * 
    * @throws DuplicateServiceException in unlikely event of concurrent attempts
    *                                   to create same binding with different
    *                                   binding values
    *                                   
    * @throws UnknownHostException if no IP address for the <code>hostName</code> could be found 
    */
   public Element getElementBinding(String serviceName, String bindingName, Element input, 
         String hostName, int basePort) throws UnknownHostException, DuplicateServiceException
   {
      return getElementBinding(serviceName, bindingName, input, hostName, basePort, false, hostName != null);
   }
   
   /**
    * Same as {@link #getElementBinding(String, String, Element)} but, if no matching
    * service binding is found, creates a new one using the given
    * <code>hostName</code> and <code>basePort</code>.
    *  
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param input string that should be used as a source for transformations 
    *              (e.g. string replacement), or <code>null</code> if no
    *              transformation is needed
    * @param hostName    Host name to use for new service binding if one is
    *                    created.
    * @param basePort    base port to use for the binding; ServiceBindingStore
    *                    may adjust this.
    * @param fixedPort whether runtime @{link ServiceBinding}s created from this 
    *                  metadata can alter the port value based on the server 
    *                  on which the binding is running.
    * @param fixedHostName whether runtime @{link ServiceBinding}s created from 
    *                      this metadata can alter the hostName value based on 
    *                      the server on which the binding is running.
    *              
    * @return transformed element based on the raw binding value(s) and <code>input</code>.
    * 
    * @throws DuplicateServiceException in unlikely event of concurrent attempts
    *                                   to create same binding with different
    *                                   binding values
    *                                   
    * @throws UnknownHostException if no IP address for the <code>hostName</code> could be found 
    */
   public Element getElementBinding(String serviceName, String bindingName, Element input, 
         String hostName, int basePort, boolean fixedPort, boolean fixedHostName) throws UnknownHostException, DuplicateServiceException
   {
      try
      {
         return getElementBinding(serviceName, bindingName, input);
      }
      catch (NoSuchBindingException e)
      {
         createBindingFromDefaults(serviceName, bindingName, hostName, basePort, fixedPort, fixedHostName);
         
         try
         {
            return getElementBinding(serviceName, bindingName, input);
         }
         catch (NoSuchBindingException e1)
         {
            // Shouldn't be possible
            throw new IllegalStateException("Newly created binding not found", e1);
         }
      }
   }
   
   /**
    * Gets a <code>URL</code> pointing to content that contains the binding values
    * for the <code>ServiceBinding</code> with the given <code>serviceName</code>
    * and no binding name qualifier.
    * <p>
    * Typical usage is in file transformation operations, where the content
    * of the given <code>input</code> URL is read, transformed, written to a 
    * temp file, and the URL of the temp file returned.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param input URL of content that should be used as a source for transformations 
    *              
    * @return URL pointing to the output of the transformation.
    *      
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    *                
    * @see URLServiceBindingValueSource
    */
   public URL getURLBinding(String serviceName, URL input) throws NoSuchBindingException
   {
      return getURLBinding(serviceName, null, input);
   }
   
   /**
    * Gets a <code>URL</code> pointing to content that contains the binding values
    * for the <code>ServiceBinding</code> with the given <code>serviceName</code>
    * and <code>bindingName</code> qualifier.
    * <p>
    * Typical usage is in file transformation operations, where the content
    * of the given <code>input</code> URL is read, transformed, written to a 
    * temp file, and the URL of the temp file returned.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param input URL of content that should be used as a source for transformations 
    *              
    * @return URL pointing to the output of the transformation.
    *      
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    *                
    * @see URLServiceBindingValueSource
    */
   public URL getURLBinding(String serviceName, String bindingName, URL input) throws NoSuchBindingException
   {
      ServiceBinding binding = store.getServiceBinding(serverName, serviceName, bindingName);
      ServiceBindingValueSource source = getServiceBindingValueSource(binding, BindingType.URL);
      if (source instanceof URLServiceBindingValueSource)
      {
         return ((URLServiceBindingValueSource) source).getURLServiceBindingValue(binding, input);
      }
      else
      {
         return Util.getBindingValueWithInput(source, binding, input, URL.class);                 
      }
   }
   
   /**
    * Same as {@link #getURLBinding(String, String, URL)} but, if no matching
    * service binding is found, creates a new one using the given
    * <code>hostName</code> and <code>basePort</code>.
    *  
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param input string that should be used as a source for transformations 
    *              (e.g. string replacement), or <code>null</code> if no
    *              transformation is needed
    * @param hostName    Host name to use for new service binding if one is
    *                    created.
    * @param basePort    base port to use for the binding; ServiceBindingStore
    *                    may adjust this.
    *              
    * @return URL pointing to the output of the transformation.
    * 
    * @throws DuplicateServiceException in unlikely event of concurrent attempts
    *                                   to create same binding with different
    *                                   binding values
    *                                   
    * @throws UnknownHostException if no IP address for the <code>hostName</code> could be found 
    */
   public URL getURLBinding(String serviceName, String bindingName, URL input, 
         String hostName, int basePort) throws UnknownHostException, DuplicateServiceException
   {
      return getURLBinding(serviceName, bindingName, input, hostName, basePort, false, hostName != null);
   }
   
   /**
    * Same as {@link #getURLBinding(String, String, URL)} but, if no matching
    * service binding is found, creates a new one using the given
    * <code>hostName</code> and <code>basePort</code>.
    *  
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param input string that should be used as a source for transformations 
    *              (e.g. string replacement), or <code>null</code> if no
    *              transformation is needed
    * @param hostName    Host name to use for new service binding if one is
    *                    created.
    * @param basePort    base port to use for the binding; ServiceBindingStore
    *                    may adjust this.
    * @param fixedPort whether runtime @{link ServiceBinding}s created from this 
    *                  metadata can alter the port value based on the server 
    *                  on which the binding is running.
    * @param fixedHostName whether runtime @{link ServiceBinding}s created from 
    *                      this metadata can alter the hostName value based on 
    *                      the server on which the binding is running.
    *              
    * @return URL pointing to the output of the transformation.
    * 
    * @throws DuplicateServiceException in unlikely event of concurrent attempts
    *                                   to create same binding with different
    *                                   binding values
    *                                   
    * @throws UnknownHostException if no IP address for the <code>hostName</code> could be found 
    */
   public URL getURLBinding(String serviceName, String bindingName, URL input, 
         String hostName, int basePort, boolean fixedPort, boolean fixedHostName) throws UnknownHostException, DuplicateServiceException
   {
      try
      {
         return getURLBinding(serviceName, bindingName, input);
      }
      catch (NoSuchBindingException e)
      {
         createBindingFromDefaults(serviceName, bindingName, hostName, basePort, fixedPort, fixedHostName);
         
         try
         {
            return getURLBinding(serviceName, bindingName, input);
         }
         catch (NoSuchBindingException e1)
         {
            // Shouldn't be possible
            throw new IllegalStateException("Newly created binding not found", e1);
         }
      }
   }
   
   /**
    * Gets a filesystem path pointing to content that contains the binding values
    * for the <code>ServiceBinding</code> with the given <code>serviceName</code>
    * and no binding name qualifier.
    * <p>
    * Typical usage is in file transformation operations, where the content
    * of the given <code>input</code> classpath resource is read, transformed, written to a 
    * temp file, and the filesystem path of the temp file returned.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param input location of content that should be used as a source for transformations;
    *              either a String representation of a URL or a value that
    *              can be passed to {@link ClassLoader#getResourceAsStream(String)}. 
    *              Cannot be <code>null</code>.
    *              
    * @return a filesystem path pointing to the output of the transformation. 
    *         May return <code>null</code>.
    *      
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    *                
    * @see URLServiceBindingValueSource
    */
   public String getResourceBinding(String serviceName, String input) throws NoSuchBindingException
   {
      return getResourceBinding(serviceName, null, input);
   }
   
   /**
    * Gets a filesystem path pointing to content that contains the binding values
    * for the <code>ServiceBinding</code> with the given <code>serviceName</code>
    * and <code>bindingName</code> qualifier.
    * <p>
    * Typical usage is in file transformation operations, where the content
    * of the given <code>input</code> classpath resource is read, transformed, written to a 
    * temp file, and the filesystem path of the temp file returned.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param input location of content that should be used as a source for transformations;
    *              either a String representation of a URL or a value that
    *              can be passed to {@link ClassLoader#getResourceAsStream(String)}. 
    *              Cannot be <code>null</code>.
    *              
    * @return a filesystem path pointing to the output of the transformation. 
    *         May return <code>null</code>.
    *      
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    *                
    * @see URLServiceBindingValueSource
    */
   public String getResourceBinding(String serviceName, String bindingName, String input) throws NoSuchBindingException
   {
      ServiceBinding binding = store.getServiceBinding(serverName, serviceName, bindingName);
      ServiceBindingValueSource source = getServiceBindingValueSource(binding, BindingType.RESOURCE);
      if (source instanceof URLServiceBindingValueSource)
      {
         return ((URLServiceBindingValueSource) source).getResourceServiceBindingValue(binding, input);
      }
      else
      {
         return Util.getBindingValueWithInput(source, binding, input, String.class);             
      }
   }
   
   /**
    * Same as {@link #getResourceBinding(String, String, String)} but, if no matching
    * service binding is found, creates a new one using the given
    * <code>hostName</code> and <code>basePort</code>.
    *  
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param input string that should be used as a source for transformations 
    *              (e.g. string replacement), or <code>null</code> if no
    *              transformation is needed
    * @param hostName    Host name to use for new service binding if one is
    *                    created.
    * @param basePort    base port to use for the binding; ServiceBindingStore
    *                    may adjust this.
    *              
    * @return a filesystem path pointing to the output of the transformation. 
    *         May return <code>null</code>.
    * 
    * @throws DuplicateServiceException in unlikely event of concurrent attempts
    *                                   to create same binding with different
    *                                   binding values
    *                                   
    * @throws UnknownHostException if no IP address for the <code>hostName</code> could be found 
    */
   public String getResourceBinding(String serviceName, String bindingName, String input, 
         String hostName, int basePort) throws UnknownHostException, DuplicateServiceException
   {
      return getResourceBinding(serviceName, bindingName, input, hostName, basePort, false, hostName != null);
   }
   
   /**
    * Same as {@link #getResourceBinding(String, String, String)} but, if no matching
    * service binding is found, creates a new one using the given
    * <code>hostName</code> and <code>basePort</code>.
    *  
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param input string that should be used as a source for transformations 
    *              (e.g. string replacement), or <code>null</code> if no
    *              transformation is needed
    * @param hostName    Host name to use for new service binding if one is
    *                    created.
    * @param basePort    base port to use for the binding; ServiceBindingStore
    *                    may adjust this.
    * @param fixedPort whether runtime @{link ServiceBinding}s created from this 
    *                  metadata can alter the port value based on the server 
    *                  on which the binding is running.
    * @param fixedHostName whether runtime @{link ServiceBinding}s created from 
    *                      this metadata can alter the hostName value based on 
    *                      the server on which the binding is running. 
    *              
    * @return a filesystem path pointing to the output of the transformation. 
    *         May return <code>null</code>.
    * 
    * @throws DuplicateServiceException in unlikely event of concurrent attempts
    *                                   to create same binding with different
    *                                   binding values
    *                                   
    * @throws UnknownHostException if no IP address for the <code>hostName</code> could be found 
    */
   public String getResourceBinding(String serviceName, String bindingName, String input, 
         String hostName, int basePort, boolean fixedPort, boolean fixedHostName) throws UnknownHostException, DuplicateServiceException
   {
      try
      {
         return getResourceBinding(serviceName, bindingName, input);
      }
      catch (NoSuchBindingException e)
      {
         createBindingFromDefaults(serviceName, bindingName, hostName, basePort, fixedPort, fixedHostName);
         
         try
         {
            return getResourceBinding(serviceName, bindingName, input);
         }
         catch (NoSuchBindingException e1)
         {
            // Shouldn't be possible
            throw new IllegalStateException("Newly created binding not found", e1);
         }
      }
   }
   
   /**
    * Gets the detyped binding value for the <code>ServiceBinding</code> with 
    * the given <code>serviceName</code> and <code>bindingName</code> qualifier.
    * <p>
    * This method is an extension point to allow integration of custom
    * {@link ServiceBindingValueSource} implementations.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>.
    * @param params arbitrary parameters understood by the @{link {@link ServiceBindingValueSource}
    *               associated with the binding.
    *      
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    *                
    * @see ServiceBinding#getServiceBindingValueSource()
    */
   public Object getGenericBinding(String serviceName, Object ... params) throws NoSuchBindingException
   {
      return getGenericBinding(serviceName, null, params);
   }
   
   /**
    * Gets the detyped binding value for the <code>ServiceBinding</code> with 
    * the given <code>serviceName</code> and <code>bindingName</code> qualifier.
    * <p>
    * This method is an extension point to allow integration of custom
    * {@link ServiceBindingValueSource} implementations.
    * </p>
    * 
    * @param serviceName value to match to {@link ServiceBinding#getServiceName()}
    *                    to identify the appropriate binding. Cannot be <code>null</code>. 
    * @param bindingName value to match to {@link ServiceBinding#getBindingName()}
    *                    to identify the appropriate binding. May be <code>null</code>.
    * @param params arbitrary parameters understood by the @{link {@link ServiceBindingValueSource}
    *               associated with the binding.
    *      
    * @throws NoSuchBindingException if a matching ServiceBinding could not be found
    *                
    * @see ServiceBinding#getServiceBindingValueSource()
    */
   public Object getGenericBinding(String serviceName, String bindingName, Object ... params) throws NoSuchBindingException
   {
      ServiceBinding binding = store.getServiceBinding(serverName, serviceName, bindingName);      
      ServiceBindingValueSource source = getServiceBindingValueSource(binding, BindingType.GENERIC);
      return source.getServiceBindingValue(binding, params); 
   }   

   // ----------------------------------------------------------------- Private
   
   private void createBindingFromDefaults(String serviceName, String bindingName, 
         String hostName, int basePort, boolean fixedPort, boolean fixedHostName) throws UnknownHostException, DuplicateServiceException
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata(serviceName, bindingName, hostName, basePort, fixedPort, fixedHostName);
      store.addServiceBinding(getServerName(), md);
   }
   
}
