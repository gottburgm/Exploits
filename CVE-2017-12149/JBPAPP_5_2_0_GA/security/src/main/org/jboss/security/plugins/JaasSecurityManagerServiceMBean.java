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
package org.jboss.security.plugins;

import java.security.Principal;
import java.util.List;
import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.security.SecurityDomain;
import org.jboss.system.ServiceMBean;

/**
 * The management interface for the JaasSecurityManagerService mbean.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 96172 $
 */
public interface JaasSecurityManagerServiceMBean
   extends ServiceMBean, SecurityManagerMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.security:service=JaasSecurityManager");

   /** A flag indicating if the Deep Copy of Subject Sets should be enabled in the security managers */
   public boolean getDeepCopySubjectMode();
   
   /**
    * A flag indicating if the Deep Copy of Subject Sets should be enabled in the security managers
    * 
    * @param flag
    */
   public void setDeepCopySubjectMode(boolean flag);
   
   /**
    * A flag indicating if the SecurityAssociation.setServer should be called
    * on service startup.
    * @return the SecurityAssociation.setServer flag.
    */
   public boolean getServerMode();

   /**
    * The SecurityAssociation server mode flag.
    * 
    * @param flag -  A flag indicating if the SecurityAssociation.setServer
    * should be called on service startup.
    */
   public void setServerMode(boolean flag);

   /**
    * Get the name of the class that provides the security manager implementation.
    */
   String getSecurityManagerClassName();
   
   /**
    * Set the name of the class that provides the security manager implementation.
    * 
    * @exception ClassNotFoundException thrown if the className cannot be found
    * using the thread context class loader.
    * @exception ClassCastException thrown if the className does not implement the
    * org.jboss.security.AuthenticationManager interface.
    */
   void setSecurityManagerClassName(String className)
      throws ClassNotFoundException, ClassCastException;
   
   /**
    * Get the name of the class that provides the SecurityProxyFactory implementation.
    */
   String getSecurityProxyFactoryClassName();
   
   /**
    * Set the name of the class that provides the SecurityProxyFactory implementation.
    */
   void setSecurityProxyFactoryClassName(String className)
      throws ClassNotFoundException;
   
   /** Get the default CallbackHandler implementation class name
    *
    * @return The fully qualified classname of the
    */
   public String getCallbackHandlerClassName();
   /** Set the default CallbackHandler implementation class name
    * @see javax.security.auth.callback.CallbackHandler
    */
   public void setCallbackHandlerClassName(String className)
      throws ClassNotFoundException;

   /**
    * Get the jndi name under which the authentication CachePolicy implenentation
    * is found
    */
   String getAuthenticationCacheJndiName();
   
   /**
    * Set the location of the security credential cache policy. This is first treated
    * as a ObjectFactory location that is capable of returning CachePolicy instances
    * on a per security domain basis by appending a '/security-domain-name' string
    * to this name when looking up the CachePolicy for a domain. If this fails then
    * the location is treated as a single CachePolicy for all security domains.
    * 
    * @param jndiName the name to the ObjectFactory or CachePolicy binding.
    */
   void setAuthenticationCacheJndiName(String jndiName);

   /**
    * Get the default timed cache policy timeout.
    * @return the default cache timeout in seconds.
    */
   int getDefaultCacheTimeout();
   
   /**
    * Set the default timed cache policy timeout. This has no affect if the
    * AuthenticationCacheJndiName has been changed from the default value.
    * @param timeoutInSecs the cache timeout in seconds.
    */
   void setDefaultCacheTimeout(int timeoutInSecs);
   
   /**
    * Get the default timed cache policy resolution.
    */
   int getDefaultCacheResolution();
   
   /**
    * Set the default timed cache policy resolution. This has no affect if the
    * AuthenticationCacheJndiName has been changed from the default value.
    * 
    * @param resInSecs resolution of timeouts in seconds.
    */
   void setDefaultCacheResolution(int resInSecs);

   /** Set the indicated security domain cache timeout. This only has an
    * effect if the security domain is using the default jboss TimedCachePolicy
    * implementation.
     
    @param securityDomain the name of the security domain cache
    @param timeoutInSecs - the cache timeout in seconds.
    @param resInSecs - resolution of timeouts in seconds.
    */ 
   public void setCacheTimeout(String securityDomain, int timeoutInSecs, int resInSecs);

   /** Flush the authentication cache associated with the given securityDomain.
    *
    * @param securityDomain the name of the security domain cache
    */
   void flushAuthenticationCache(String securityDomain);

   /** Flush a principal's authentication cache entry associated with the
    * given securityDomain.
    *
    * @param securityDomain the name of the security domain cache
    * @param user the principal of the user to flush
    */
   void flushAuthenticationCache(String securityDomain, Principal user);

   /** The the list of active Principls for the given security domain
    * @param securityDomain
    * @return List<Princpals> of active users, may be null.
    */
   List getAuthenticationCachePrincipals(String securityDomain);

   /**
    * Register a SecurityDomain implmentation
    */
   void registerSecurityDomain(String securityDomain, SecurityDomain instance);

   /**
    * Get the default unauthenticated principal.
    * @return The principal name
    */
   String getDefaultUnauthenticatedPrincipal();

   /**
    * Set the default unauthenticated principal.
    * @param principal The principal name
    */
   void setDefaultUnauthenticatedPrincipal(String principal);
 
   /**
    * Get information about the JCA Providers
    * @return
    */
   String displayJCAInformation();
   
   /**
    * Get the authentication cache flush period
    * @return period in seconds
    */
   int getDefaultCacheFlushPeriod();
   
   /**
    * Set the authentication cache flush period
    * @param flushPeriodInSecs
    */
   void setDefaultCacheFlushPeriod(int flushPeriodInSecs);
}
