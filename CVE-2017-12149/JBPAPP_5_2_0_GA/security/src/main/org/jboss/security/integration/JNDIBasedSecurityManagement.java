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
package org.jboss.security.integration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.callback.CallbackHandler;

import org.jboss.logging.Logger;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementParameter;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.AuthorizationManager;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.SecurityConstants;
import org.jboss.security.audit.AuditManager;
import org.jboss.security.auth.AuthenticationCacheFlushThread;
import org.jboss.security.auth.AuthenticationTimedCachePolicy;
import org.jboss.security.auth.callback.JBossCallbackHandler;
import org.jboss.security.config.SecurityConfiguration;
import org.jboss.security.identitytrust.IdentityTrustManager;
import org.jboss.security.mapping.MappingManager;
import org.jboss.security.plugins.JaasSecurityDomain;
import org.jboss.security.plugins.SecurityDomainContext;
import org.jboss.util.CachePolicy;
import org.jboss.util.TimedCachePolicy;
 
/**
 *  JNDI Based Security Management
 *  @author Anil.Saldhana@redhat.com
 *  @since  Sep 9, 2007 
 *  @version $Revision: 106788 $
 */ 
@ManagementObject(name="JNDIBasedSecurityManagement", componentType = @ManagementComponent(type = "MCBean", subtype = "Security"),
                  properties = ManagementProperties.EXPLICIT)
public class JNDIBasedSecurityManagement implements ISecurityManagement
{ 
   private static final long serialVersionUID = 1L;

   public static final String CBH = "org.jboss.security.callbackhandler";
   
   protected static Logger log = Logger.getLogger(JNDIBasedSecurityManagement.class);
   
   static transient ConcurrentHashMap<String,SecurityDomainContext> securityMgrMap = new ConcurrentHashMap<String,SecurityDomainContext>();
    
   protected String BASE_CTX = SecurityConstants.JAAS_CONTEXT_ROOT; 
   
   protected String authenticationMgrClass = "org.jboss.security.plugins.JaasSecurityManager";
   
   protected String authorizationMgrClass = "org.jboss.security.plugins.JBossAuthorizationManager";
   
   protected String auditMgrClass = "org.jboss.security.plugins.audit.JBossAuditManager";
   
   protected String identityTrustMgrClass = "org.jboss.security.plugins.identitytrust.JBossIdentityTrustManager";
   
   protected String mappingMgrClass = "org.jboss.security.plugins.mapping.JBossMappingManager";
   
   protected static transient CallbackHandler callBackHandler = new JBossCallbackHandler(); 
   
   /** Enable the IdentityTrust feature */
   protected boolean enableIdentity = false;
   
   /** Enable the Audit feature */
   protected boolean enableAudit = true; 
   
   private CachePolicy cachePolicy = null;
   
   private transient ConcurrentHashMap<String,AuthenticationManager> authMgrMap = null;
   private transient ConcurrentHashMap<String,AuthorizationManager> authzMgrMap = null;
   private transient ConcurrentHashMap<String,MappingManager> mappingMgrMap = null;
   private transient ConcurrentHashMap<String,AuditManager> auditMgrMap = null;
   private transient ConcurrentHashMap<String,IdentityTrustManager> idmMgrMap = null;
   
   /** Thread to cleanup the authentication cache */
   private static AuthenticationCacheFlushThread authCacheFlushThread;
   
   public JNDIBasedSecurityManagement()
   {   
      initialize();
      initializeCallbackHandler();
   } 
   
   @ManagementOperation(description = "Get the audit manager for the specified security domain",
         params = {@ManagementParameter(name = "securityDomain", description = "The security domain name")})
   public AuditManager getAuditManager(String securityDomain)
   { 
      initialize();
      AuditManager auditManager = null;
      try
      { 
         if(this.enableAudit)
         {
            auditManager = this.auditMgrMap.get(securityDomain);
            if(auditManager == null)
            {
               auditManager = (AuditManager) lookUpJNDI(securityDomain + "/auditMgr");
               this.auditMgrMap.put(securityDomain, auditManager); 
            } 
         }  
      }
      catch(Exception e)
      {
         log.trace("Exception in getting audit mgr", e); 
      }
      return auditManager;
   }

   @ManagementOperation(description = "Get the authentication manager for the specified security domain",
         params = {@ManagementParameter(name = "securityDomain", description = "The security domain name")})
   public AuthenticationManager getAuthenticationManager(String securityDomain)
   {
      initialize();
      AuthenticationManager am = null;
      try
      {
         am = this.authMgrMap.get(securityDomain);
         if(am == null)
         {
            am = (AuthenticationManager) lookUpJNDI(securityDomain + "/authenticationMgr");
            this.authMgrMap.put(securityDomain, am); 
         }
      }
      catch(Exception e)
      {
         log.trace("Exception in getting authentication mgr "
               + " for domain="+securityDomain , e );
      }
      return am;
   }

   @ManagementOperation(description = "Get the authorization manager for the specified security domain",
         params = {@ManagementParameter(name = "securityDomain", description = "The security domain name")})
   public AuthorizationManager getAuthorizationManager(String securityDomain)
   {
      initialize();
      AuthorizationManager am = null;
      try
      {
         am = this.authzMgrMap.get(securityDomain);
         if(am == null)
         {
            am = (AuthorizationManager) lookUpJNDI(securityDomain + "/authorizationMgr");
            this.authzMgrMap.put(securityDomain, am);
         }
      }
      catch(Exception e)
      {
         log.trace("Exception in getting authorization mgr", e);
      }
      return am;
   }

   @ManagementOperation(description = "Get the identity trust manager for the specified security domain",
         params = {@ManagementParameter(name = "securityDomain", description = "The security domain name")})
   public IdentityTrustManager getIdentityTrustManager(String securityDomain)
   {
      initialize();
      IdentityTrustManager am = null;
      try
      {
         if(this.enableIdentity)
         {
            am = this.idmMgrMap.get(securityDomain);
            if(am == null)
            {
               am = (IdentityTrustManager) lookUpJNDI(securityDomain + "/identityTrustMgr");
               this.idmMgrMap.put(securityDomain, am); 
            } 
         }
      }
      catch(Exception e)
      {
         log.trace("Exception in getting IdentityTrustManager", e);
      }
      return am;
   }

   @ManagementOperation(description = "Get the mapping manager for the specified security domain",
         params = {@ManagementParameter(name = "securityDomain", description = "The security domain name")})
   public MappingManager getMappingManager(String securityDomain)
   {
      initialize();
      MappingManager am = null;
      try
      {
         am = this.mappingMgrMap.get(securityDomain);
         if(am == null)
         {
            am = (MappingManager) lookUpJNDI(securityDomain + "/mappingMgr");
            if(am == null)
              am = createMappingManager(securityDomain);
            this.mappingMgrMap.put(securityDomain, am); 
         }
      }
      catch(Exception e)
      {
         log.trace("Exception in getting MappingManager", e);
      }
      return am;
   }
       
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, 
         description = "The class that implements the AuthenticationManager interface")
   public void setAuthenticationMgrClass(String authenticationMgrClass)
   {
      this.authenticationMgrClass = authenticationMgrClass;
      securityMgrMap.clear();
   }

   @ManagementProperty(use = {ViewUse.CONFIGURATION}, 
         description = "The class that implements the AuthorizationManager interface")
   public void setAuthorizationMgrClass(String authorizationMgrClass)
   {
      this.authorizationMgrClass = authorizationMgrClass;
   }

   @ManagementProperty(use = {ViewUse.CONFIGURATION}, 
         description = "The class that implements the AuditManager interface")
   public void setAuditMgrClass(String auditMgrClass)
   {
      this.auditMgrClass = auditMgrClass;
   } 

   @ManagementProperty(use = {ViewUse.CONFIGURATION}, 
         description = "The class that implements the IdentityTrustManager interface")
   public void setIdentityTrustMgrClass(String identityTrustMgrClass)
   {
      this.identityTrustMgrClass = identityTrustMgrClass;
   }

   @ManagementProperty(use = {ViewUse.CONFIGURATION}, 
         description = "The class that implements the MappingManager interface")
   public void setMappingMgrClass(String mappingMgrClass)
   {
      this.mappingMgrClass = mappingMgrClass;
   }

   public void setCallBackHandler(CallbackHandler cbh)
   {
      callBackHandler = cbh;
      securityMgrMap.clear();
   }

   public void setEnableAudit(boolean enableAudit)
   {
      this.enableAudit = enableAudit;
   }
   
   public void setEnableIdentity(boolean enableIdentity)
   {
      this.enableIdentity = enableIdentity;
   }

   public void setCachePolicy(CachePolicy cp)
   {
      this.cachePolicy = cp;
   } 
   
   public void setBaseContext(String ctx)
   {
      if(ctx == null)
         throw new IllegalArgumentException("ctx is null");
      this.BASE_CTX = ctx;
   }
     
   /** Set the indicated security domain cache timeout. This only has an
   effect if the security domain is using the default jboss TimedCachePolicy
   implementation.

   @param securityDomain the name of the security domain cache
   @param timeoutInSecs - the cache timeout in seconds.
   @param resInSecs - resolution of timeouts in seconds.
   */
  public static void setCacheTimeout(String securityDomain, int timeoutInSecs, int resInSecs)
  {
     SecurityDomainContext securityDomainCtx = (SecurityDomainContext) securityMgrMap.get(securityDomain);
     if(securityDomainCtx == null)
     {
      try
      {
         String lookupStr = SecurityConstants.JAAS_CONTEXT_ROOT + "/" + securityDomain;
         securityDomainCtx = (SecurityDomainContext) new InitialContext().lookup(lookupStr);
         securityMgrMap.put(securityDomain, securityDomainCtx);
      }
      catch (NamingException e)
      {
         log.trace("SetCacheTimeOut:Failed to look up SecurityDomainCtx:"+securityDomain);
      }  
     }
     if(securityDomainCtx != null)
     {
        CachePolicy cache = securityDomainCtx.getAuthenticationCache(); 
        if( cache != null && cache instanceof TimedCachePolicy )
        {
           TimedCachePolicy tcp = (TimedCachePolicy) cache;
           synchronized( tcp )
           {
              tcp.setDefaultLifetime(timeoutInSecs);
              tcp.setResolution(resInSecs);
           }
        }
        else
        {
           log.warn("Failed to find cache policy for securityDomain='"
              + securityDomain + "'");
        } 
     }
  } 
   
   public static void setDefaultCacheTimeout(int defaultCacheTimeout)
   {
      SecurityConstantsBridge.defaultCacheTimeout = defaultCacheTimeout;
   }

   public static void setDefaultCacheResolution(int defaultCacheResolution)
   {
      SecurityConstantsBridge.defaultCacheResolution = defaultCacheResolution;
   }
   
   public static void setDefaultCacheFlushPeriod(int flushPeriodInSecs)
   {
      SecurityConstantsBridge.defaultCacheFlushPeriod = flushPeriodInSecs;
      if (SecurityConstantsBridge.defaultCacheFlushPeriod == 0 && authCacheFlushThread != null)
      {
         authCacheFlushThread.interrupt();
         authCacheFlushThread = null;
      }
      if (SecurityConstantsBridge.defaultCacheFlushPeriod > 0 && authCacheFlushThread == null)
      {
         authCacheFlushThread = new AuthenticationCacheFlushThread(securityMgrMap);
         authCacheFlushThread.start();
      }
   }

   @ManagementOperation(description = "Create the context for the specified security domain",
         params = {@ManagementParameter(name = "securityDomain", description = "The security domain name")})
   public SecurityDomainContext createSecurityDomainContext(String securityDomain) throws Exception
   {   
      log.debug("Creating SDC for domain="+securityDomain);
      AuthenticationManager am = createAuthenticationManager(securityDomain);
      CachePolicy cache = createDefaultCachePolicy();
      //Set security cache if the auth manager implementation supports it
      setSecurityDomainCache(am, cache);
      //Set DeepCopySubject option if supported
      if(SecurityConfiguration.isDeepCopySubjectMode())
      {
        setDeepCopySubjectMode(am);  
      }
      
      SecurityDomainContext securityDomainContext = new SecurityDomainContext(am, cache); 
      
      securityDomainContext.setAuthorizationManager(createAuthorizationManager(securityDomain));
      securityDomainContext.setAuditMgr(createAuditManager(securityDomain));
      securityDomainContext.setIdentityTrustMgr(createIdentityTrustManager(securityDomain));
      securityDomainContext.setMappingMgr(createMappingManager(securityDomain));
      return securityDomainContext;
   }
   
   /**
    * Legacy registration of JaasSecurityDomain instance with the JNDI
    * Object Factory internal hashmap
    * @param domain
    * @param jsd
    * @throws Exception
    */
   @ManagementOperation(description = "Register the specified security domain",
         params = {@ManagementParameter(name = "domain", description = "The security domain being registered")})
   public void registerJaasSecurityDomainInstance(JaasSecurityDomain domain) throws Exception
   {
      String domainName = domain.getSecurityDomain();
      SecurityDomainContext sdc = (SecurityDomainContext) securityMgrMap.get(domainName);
      if(sdc != null)
      {
         sdc.setAuthenticationManager(domain);
      }
      else
      {
         sdc = createSecurityDomainContext(domainName);
         sdc.setAuthenticationManager(domain);
      }
      securityMgrMap.put(domainName, sdc);
   }  
   
   /**
    * Legacy deregistration of JaasSecurityDomain instance with the JNDI
    * Object Factory internal hashmap
    * @param securityDomain
    * @param jsd
    * @throws Exception
    */
   @ManagementOperation(description = "Deregister the specified security domain",
         params = {@ManagementParameter(name = "securityDomain", description = "The name of the security domain being deregistered")})
   public void deregisterJaasSecurityDomainInstance(String securityDomain)
   {
      securityMgrMap.remove(securityDomain); 
   }  
   
   /**
    * Clear all the maps
    */
   public static void clear()
   {
      RuntimePermission rtp = new RuntimePermission(JNDIBasedSecurityManagement.class.getName());
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(rtp);
      
      securityMgrMap.clear();
   }
   
   // Private Methods
 
   private Object lookUpJNDI(String ctxName) 
   {
      Object result = null;
      try
      { 
         Context ctx = new InitialContext();
         if(ctxName.startsWith(BASE_CTX))
            result = ctx.lookup(ctxName);
         else
            result = ctx.lookup(BASE_CTX + "/" + ctxName);  
      }
      catch(Exception e)
      {
         log.trace("Look up of JNDI for " + ctxName + " failed with "+ e.getLocalizedMessage());
         return null;
      }
      return result;
   }
   
   private AuthenticationManager createAuthenticationManager(String securityDomain) throws Exception
   {
      Class<?> clazz = SecurityActions.getContextClassLoader().loadClass(authenticationMgrClass);
      Constructor<?> ctr = clazz.getConstructor(new Class[] { String.class, CallbackHandler.class});
      return (AuthenticationManager) ctr.newInstance(new Object[]{ securityDomain, callBackHandler});
   }
   
   private AuthorizationManager createAuthorizationManager(String securityDomain) throws Exception
   {
      Class<?> clazz = SecurityActions.getContextClassLoader().loadClass(authorizationMgrClass);
      Constructor<?> ctr = clazz.getConstructor(new Class[] { String.class});
      return (AuthorizationManager) ctr.newInstance(new Object[]{ securityDomain});
   }
   
   private AuditManager createAuditManager(String securityDomain) throws Exception
   {
      Class<?> clazz = SecurityActions.getContextClassLoader().loadClass(auditMgrClass);
      Constructor<?> ctr = clazz.getConstructor(new Class[] { String.class});
      return (AuditManager) ctr.newInstance(new Object[]{ securityDomain});
   }
   
   private MappingManager createMappingManager(String securityDomain) throws Exception
   {
      Class<?> clazz = SecurityActions.getContextClassLoader().loadClass(mappingMgrClass);
      Constructor<?> ctr = clazz.getConstructor(new Class[] { String.class});
      return (MappingManager) ctr.newInstance(new Object[]{ securityDomain});
   }
   
   private IdentityTrustManager createIdentityTrustManager(String securityDomain) throws Exception
   {
      Class<?> clazz = SecurityActions.getContextClassLoader().loadClass(identityTrustMgrClass);
      Constructor<?> ctr = clazz.getConstructor(new Class[] { String.class});
      return (IdentityTrustManager) ctr.newInstance(new Object[]{ securityDomain});
   }
   
   /** Use reflection to attempt to set the authentication cache on the
    * securityMgr argument.
    * @param securityMgr the security manager
    * @param cachePolicy the cache policy implementation
    */
   private static void setSecurityDomainCache(AuthenticationManager securityMgr,
      CachePolicy cachePolicy)
   {
      try
      {
         Class<?>[] setCachePolicyTypes = {CachePolicy.class};
         Method m = securityMgr.getClass().getMethod("setCachePolicy", setCachePolicyTypes);
         Object[] setCachePolicyArgs = {cachePolicy};
         m.invoke(securityMgr, setCachePolicyArgs);
         log.debug("setCachePolicy, c="+setCachePolicyArgs[0]);
      }
      catch(Exception e2)
      {    
         if(log.isTraceEnabled())
            log.trace("Optional setCachePolicy failed" + e2.getLocalizedMessage());
      }
   }
   
   /** Use reflection to attempt to set the authentication cache on the
    * securityMgr argument.
    * @param securityMgr the security manager
    * @param cachePolicy the cache policy implementation
    */
   private static void setDeepCopySubjectMode(AuthenticationManager securityMgr)
   {
      try
      {
         Class<?>[] argsType = {Boolean.class};
         Method m = securityMgr.getClass().getMethod("setDeepCopySubjectOption", argsType);
         Object[] deepCopyArgs = {Boolean.TRUE};
         m.invoke(securityMgr, deepCopyArgs);
         log.trace("setDeepCopySubjectOption, option="+deepCopyArgs[0]);
      }
      catch(Exception e2)
      {    
         if(log.isTraceEnabled())
            log.trace("Optional setDeepCopySubjectMode failed" + e2.getLocalizedMessage());
      }
   }
   
   /**
    * Create a Default Cache Policy
    * @return
    */
   private CachePolicy createDefaultCachePolicy()
   {
      TimedCachePolicy cachePolicy = 
          new AuthenticationTimedCachePolicy(SecurityConstantsBridge.defaultCacheTimeout,
                               true, 
                               SecurityConstantsBridge.defaultCacheResolution);
      cachePolicy.create();
      cachePolicy.start();
      return cachePolicy; 
   } 
   
   /**
    * Since the maps are transient, initialize them
    */
   private void initialize()
   {
      if(authMgrMap == null)
         authMgrMap = new ConcurrentHashMap<String,AuthenticationManager>();
      if(authzMgrMap == null)
         authzMgrMap = new ConcurrentHashMap<String,AuthorizationManager>();
      if(mappingMgrMap == null)
         mappingMgrMap = new ConcurrentHashMap<String,MappingManager>();
      if(auditMgrMap == null)
         auditMgrMap = new ConcurrentHashMap<String,AuditManager>();
      if(idmMgrMap == null)
         idmMgrMap = new ConcurrentHashMap<String,IdentityTrustManager>();
   }
   
   private void initializeCallbackHandler()
   {
	   //Look for a system property for a VM wide Callback Handler
	   String cbh = SecurityActions.getSystemProperty(CBH, null);
	   if(cbh != null)
	   {
		   try
		   { 
			   ClassLoader tcl = SecurityActions.getContextClassLoader();
			   Class<?> clazz = tcl.loadClass(cbh);
			   callBackHandler = (CallbackHandler) clazz.newInstance();
		   }
		   catch(Exception e)
		   {
			   throw new RuntimeException("Error initializing JNDIBasedSecurityManagement:",e);
		   }
	   }
	   if(callBackHandler == null)
		   callBackHandler = new JBossCallbackHandler(); 
   }
   
   public void start()
   {
      // start the authentication cache flush thread
      if (SecurityConstantsBridge.defaultCacheFlushPeriod > 0 && authCacheFlushThread == null)
      {
         authCacheFlushThread = new AuthenticationCacheFlushThread(securityMgrMap);
         authCacheFlushThread.start();
      }
   }
   
   public void stop()
   {
      if (authCacheFlushThread != null)
      {
         authCacheFlushThread.interrupt();
         authCacheFlushThread = null;
      }
   }
}
