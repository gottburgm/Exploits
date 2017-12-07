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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.jacc.PolicyContext;

import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityDomain;
import org.jboss.security.SecurityProxyFactory;
import org.jboss.security.auth.callback.CallbackHandlerPolicyContextHandler;
import org.jboss.security.auth.callback.JBossCallbackHandler;
import org.jboss.security.config.SecurityConfiguration;
import org.jboss.security.integration.JNDIBasedSecurityManagement;
import org.jboss.security.integration.SecurityConstantsBridge;
import org.jboss.security.integration.SecurityDomainObjectFactory;
import org.jboss.security.jacc.SubjectPolicyContextHandler;
import org.jboss.security.propertyeditor.PrincipalEditor;
import org.jboss.security.propertyeditor.SecurityDomainEditor;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.CachePolicy;
import org.jboss.util.TimedCachePolicy;

/**
 * This is a JMX service which manages JAAS based SecurityManagers.
 * JAAS SecurityManagers are responsible for validating credentials
 * associated with principals. The service defaults to the
 * org.jboss.security.plugins.JaasSecurityManager implementation but
 * this can be changed via the securityManagerClass property.
 *
 * @see JaasSecurityManager
 * @see org.jboss.security.SubjectSecurityManager
 * 
 * @author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
 * @author <a href="rickard@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 * @version $Revision: 113110 $
 */
public class JaasSecurityManagerService
   extends ServiceMBeanSupport
   implements JaasSecurityManagerServiceMBean
{
   private static final String SECURITY_MGR_PATH = "java:/jaas";
   private static final String DEFAULT_CACHE_POLICY_PATH = "java:/timedCacheFactory";
   /** The log4j interface */
   private static Logger log;
   /** The class that provides the security manager implementation */
   private static String securityMgrClassName = "org.jboss.security.plugins.JaasSecurityManager";
   /** The loaded securityMgrClassName */
   private static Class securityMgrClass = JaasSecurityManager.class;
   /** The JAAS CallbackHandler interface implementation to use */
   private static String callbackHandlerClassName = "org.jboss.security.auth.callback.JBossCallbackHandler";
   private static Class callbackHandlerClass = org.jboss.security.auth.callback.JBossCallbackHandler.class;

   /** The location of the security credential cache policy. This is first treated
    as a ObjectFactory location that is capable of returning CachePolicy instances
    on a per security domain basis by appending a '/security-domain-name' string
    to this name when looking up the CachePolicy for a domain. If this fails then
    the location is treated as a single CachePolicy for all security domains.
    */
   private static String cacheJndiName = DEFAULT_CACHE_POLICY_PATH;
   private static int defaultCacheTimeout = 30*60;
   private static int defaultCacheResolution = 60;
   /** The class that provides the SecurityProxyFactory implementation */
   private static String securityProxyFactoryClassName = "org.jboss.security.SubjectSecurityProxyFactory";
   private static Class securityProxyFactoryClass = org.jboss.security.SubjectSecurityProxyFactory.class;
   /** A mapping from security domain name to a SecurityDomainContext object */
   private static ConcurrentHashMap securityDomainCtxMap = new ConcurrentHashMap();
   private static NameParser parser;
   /** A flag indicating if the SecurityAssociation.setServer should be called */
   private boolean serverMode = true;
   /** A flag indicating if the Deep Copy of Subject Sets should be enabled in the security managers */
   private static boolean deepCopySubjectMode = false;

   /** The default unauthenticated principal */
   private static String defaultUnauthenticatedPrincipal = "Unauthenticated Principal";
   
   /** Frequency of the thread cleaning the authentication cache of expired entries */
   private static int defaultCacheFlushPeriod = 60*60;

   private static JNDIBasedSecurityManagement securityManagement = SecurityConstantsBridge.getSecurityManagement();

   static
   {
      // Get a log interface, required for some statics below
      // can not use instance field inherited from ServiceMBeanSupport
      log = Logger.getLogger(JaasSecurityManagerService.class);

   }

   /** The constructor does nothing as the security manager is created
    on each lookup into java:/jaas/xxx. This is also why all variables
    in this class are static.
    */
   public JaasSecurityManagerService()
   {
   }

   public boolean getServerMode()
   {
      return serverMode;
   }
   public void setServerMode(boolean mode)
   {
      this.serverMode = mode;
   }

   public String getSecurityManagerClassName()
   {
      return securityMgrClassName;
   }
   public void setSecurityManagerClassName(String className)
      throws ClassNotFoundException, ClassCastException
   {
      securityMgrClassName = className;
      ClassLoader loader = getContextClassLoader();
      securityMgrClass = loader.loadClass(securityMgrClassName);
      if( AuthenticationManager.class.isAssignableFrom(securityMgrClass) == false )
         throw new ClassCastException(securityMgrClass+" does not implement "+AuthenticationManager.class);
   }
   public String getSecurityProxyFactoryClassName()
   {
      return securityProxyFactoryClassName;
   }
   public void setSecurityProxyFactoryClassName(String className)
      throws ClassNotFoundException
   {
      securityProxyFactoryClassName = className;
      ClassLoader loader = getContextClassLoader();
      securityProxyFactoryClass = loader.loadClass(securityProxyFactoryClassName);
   } 

   /** Get the default CallbackHandler implementation class name
    *
    * @return The fully qualified classname of the
    */
   public String getCallbackHandlerClassName()
   {
      return JaasSecurityManagerService.callbackHandlerClassName;
   }
   /** Set the default CallbackHandler implementation class name
    * @see javax.security.auth.callback.CallbackHandler
    */
   public void setCallbackHandlerClassName(String className)
      throws ClassNotFoundException
   {
      callbackHandlerClassName = className;
      ClassLoader loader = getContextClassLoader();
      callbackHandlerClass = loader.loadClass(callbackHandlerClassName);
   }

   /** Get the jndi name under which the authentication cache policy is found
    */
   public String getAuthenticationCacheJndiName()
   {
      return cacheJndiName;
   }
   /** Set the jndi name under which the authentication cache policy is found
    */
   public void setAuthenticationCacheJndiName(String jndiName)
   {
      cacheJndiName = jndiName;
   }
   /** Get the default timed cache policy timeout.
    @return the default cache timeout in seconds.
    */
   public int getDefaultCacheTimeout()
   {
      return defaultCacheTimeout;
   }
   /** Set the default timed cache policy timeout. This has no affect if the
    AuthenticationCacheJndiName has been changed from the default value.
    @param timeoutInSecs - the cache timeout in seconds.
    */
   public void setDefaultCacheTimeout(int timeoutInSecs)
   {
      defaultCacheTimeout = timeoutInSecs;
      SecurityConstantsBridge.defaultCacheTimeout = timeoutInSecs;
   }
   /** Get the default timed cache policy resolution.
    */
   public int getDefaultCacheResolution()
   {
      return defaultCacheResolution;
   }
   /** Set the default timed cache policy resolution. This has no affect if the
    AuthenticationCacheJndiName has been changed from the default value.
    @param resInSecs - resolution of timeouts in seconds.
    */
   public void setDefaultCacheResolution(int resInSecs)
   {
      defaultCacheResolution = resInSecs;
      SecurityConstantsBridge.defaultCacheResolution = resInSecs;
   }

   /**
    * @see JaasSecurityManagerServiceMBean#getDeepCopySubjectMode()
    */
   public boolean getDeepCopySubjectMode()
   { 
      return deepCopySubjectMode;
   }

   /**
    * @see JaasSecurityManagerServiceMBean#getDeepCopySubjectMode() 
    */
   public void setDeepCopySubjectMode(boolean flag)
   {  
      log.debug("setDeepCopySubjectMode="+flag);
      deepCopySubjectMode = flag;
      //Update the security managers if already present
      if(securityDomainCtxMap.isEmpty() == false)
      {
         Iterator iter = securityDomainCtxMap.keySet().iterator();
         while(iter.hasNext())
         {
            String securityDomainName = (String)iter.next();
            SecurityDomainContext sdc = (SecurityDomainContext)securityDomainCtxMap.get(securityDomainName);
            setDeepCopySubjectOption(sdc.securityMgr, flag);
         }
      }
      SecurityConfiguration.setDeepCopySubjectMode(flag);
   }

   /** Set the indicated security domain cache timeout. This only has an
    effect if the security domain is using the default jboss TimedCachePolicy
    implementation.

    @param securityDomain the name of the security domain cache
    @param timeoutInSecs - the cache timeout in seconds.
    @param resInSecs - resolution of timeouts in seconds.
    */
   public void setCacheTimeout(String securityDomain, int timeoutInSecs, int resInSecs)
   {
      CachePolicy cache = getCachePolicy(securityDomain);
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

      //Set the CacheTimeOut on JNDIBasedSecurityManagement
      JNDIBasedSecurityManagement.setCacheTimeout(securityDomain, timeoutInSecs, resInSecs);      
   } 
   
   /**
    * Get the authentication cache flush period
    * @return period in seconds
    */
   public int getDefaultCacheFlushPeriod()
   {
      return defaultCacheFlushPeriod;
   }
   
   /**
    * Set the authentication cache flush period
    * 
    * @param flushPeriodInSecs
    */
   public void setDefaultCacheFlushPeriod(int flushPeriodInSecs)
   {
      this.defaultCacheFlushPeriod = flushPeriodInSecs;
      JNDIBasedSecurityManagement.setDefaultCacheFlushPeriod(flushPeriodInSecs);
   }

   /** flush the cache policy for the indicated security domain if one exists.
    * @param securityDomain the name of the security domain cache
    */
   public void flushAuthenticationCache(String securityDomain)
   {
      CachePolicy cache = getCachePolicy(securityDomain);
      if( cache != null )
      {
         cache.flush();
      }
      else
      {
         log.warn("Failed to find cache policy for securityDomain='"
            + securityDomain + "'");
      }
   }

   /** Flush a principal's authentication cache entry associated with the
    * given securityDomain.
    *
    * @param securityDomain the name of the security domain cache
    * @param user the principal of the user to flush
    */
   public void flushAuthenticationCache(String securityDomain, Principal user)
   {
      CachePolicy cache = getCachePolicy(securityDomain);
      if( cache != null )
      {
         cache.remove(user);
      }
      else
      {
         log.warn("Failed to find cache policy for securityDomain='"
            + securityDomain + "'");
      }
   }

   /** Return the active principals in the indicated security domain auth cache.
    * @param securityDomain the name of the security to lookup the cache for
    * @return List<Principal> of active keys found in the auth cache if
    *    the cache exists and is accessible, null otherwise.
    */
   public List getAuthenticationCachePrincipals(String securityDomain)
   {
      CachePolicy cache = getCachePolicy(securityDomain);
      List validPrincipals = null;
      if( cache instanceof TimedCachePolicy )
      {
         TimedCachePolicy tcache = (TimedCachePolicy) cache;
         validPrincipals = tcache.getValidKeys();
      }
      return validPrincipals;
   }

// Begin SecurityManagerMBean interface methods
   public boolean isValid(String securityDomain, Principal principal, Object credential)
   {
      boolean isValid = false;
      try
      {
         SecurityDomainContext sdc = lookupSecurityDomain(securityDomain);
         isValid = sdc.getSecurityManager().isValid(principal, credential, null);
      }
      catch(NamingException e)
      {
         log.debug("isValid("+securityDomain+") failed", e);
      }
      return isValid;
   }

   public Principal getPrincipal(String securityDomain, Principal principal)
   {
      Principal realmPrincipal = null;
      try
      {
         SecurityDomainContext sdc = lookupSecurityDomain(securityDomain);
         realmPrincipal = sdc.getRealmMapping().getPrincipal(principal);
      }
      catch(NamingException e)
      {
         log.debug("getPrincipal("+securityDomain+") failed", e);
      }
      return realmPrincipal;
   }

    public boolean doesUserHaveRole(String securityDomain, Principal principal,
       Object credential, Set roles)
    {
       boolean doesUserHaveRole = false;
       try
       {
          SecurityDomainContext sdc = lookupSecurityDomain(securityDomain);
          // Must first validate the user
          Subject subject = new Subject();
          boolean isValid = sdc.getSecurityManager().isValid(principal, credential, subject);
          if( isValid )
          {
             // Now can query if the authenticated Subject has the role
             SubjectActions.pushSubjectContext(principal, credential, subject,
                   sdc.getSecurityManager().getSecurityDomain());
             doesUserHaveRole = sdc.getRealmMapping().doesUserHaveRole(principal, roles);
             SubjectActions.popSubjectContext();
          }
       }
       catch(NamingException e)
       {
          log.debug("doesUserHaveRole("+securityDomain+") failed", e);
       }
       return doesUserHaveRole;
    }

    public Set getUserRoles(String securityDomain, Principal principal, Object credential)
    {
       Set userRoles = null;
       try
       {
          SecurityDomainContext sdc = lookupSecurityDomain(securityDomain);
          // Must first validate the user
          Subject subject = new Subject();
          boolean isValid = sdc.getSecurityManager().isValid(principal, credential, subject);
          // Now can query if the authenticated Subject has the role
          if( isValid )
          {
            SubjectActions.pushSubjectContext(principal, credential, subject,
                  sdc.getSecurityManager().getSecurityDomain() );
            userRoles = sdc.getRealmMapping().getUserRoles(principal);
             SubjectActions.popSubjectContext();
          }
       }
       catch(NamingException e)
       {
          log.debug("getUserRoles("+securityDomain+") failed", e);
       }
       return userRoles;
    }
// End SecurityManagerMBean interface methods

   protected void startService() throws Exception
   {
      // use thread-local principal and credential propagation
      if (serverMode)
         SecurityAssociation.setServer();

      // Register the default active Subject PolicyContextHandler
      SubjectPolicyContextHandler handler = new SubjectPolicyContextHandler();
      PolicyContext.registerHandler(SecurityConstants.SUBJECT_CONTEXT_KEY,
         handler, true);
      // Register the JAAS CallbackHandler JACC PolicyContextHandlers
      CallbackHandlerPolicyContextHandler chandler = new CallbackHandlerPolicyContextHandler();
      PolicyContext.registerHandler(CallbackHandlerPolicyContextHandler.CALLBACK_HANDLER_KEY,
         chandler, true);

      Context ctx = new InitialContext();
      parser = ctx.getNameParser("");

      RefAddr refAddr = new StringRefAddr("nns", "JSMCachePolicy");
      String factoryName = DefaultCacheObjectFactory.class.getName();
      Reference ref = new Reference("javax.naming.Context", refAddr, factoryName, null);
      ctx.rebind(DEFAULT_CACHE_POLICY_PATH, ref);
      log.debug("cachePolicyCtxPath="+cacheJndiName);
      
      // JBAPAPP-5459: binding java:/jaas to JNDI before services in the deploy start
      /* Create a mapping from the java:/jaas context to a SecurityDomainObjectFactory
      so that any lookup against java:/jaas/domain returns an instance of our
      security manager class.
      */
      refAddr = new StringRefAddr("nns", "JSM");
      factoryName = SecurityDomainObjectFactory.class.getName();
      ref = new Reference("javax.naming.Context", refAddr, factoryName, null);
      ctx.rebind(SecurityConstants.JAAS_CONTEXT_ROOT, ref);

      // Bind the default SecurityProxyFactory instance under java:/SecurityProxyFactory
      SecurityProxyFactory proxyFactory = (SecurityProxyFactory) securityProxyFactoryClass.newInstance();
      ctx.bind("java:/SecurityProxyFactory", proxyFactory);
      log.debug("SecurityProxyFactory="+proxyFactory);
      
      //Handler custom callbackhandler
      if(callbackHandlerClass != JBossCallbackHandler.class)
      {
         AccessController.doPrivileged(new PrivilegedAction<Object>()
         {
            public Object run()
            {
               System.setProperty(JNDIBasedSecurityManagement.CBH, callbackHandlerClassName);
               return null;
            }
         });
         CallbackHandler callbackHandler = null;
         callbackHandler = (CallbackHandler) callbackHandlerClass.newInstance();
         if (callbackHandler != null)
            securityManagement.setCallBackHandler(callbackHandler);
      }
      
      // Set AuthenticationManager class
      securityManagement.setAuthenticationMgrClass(securityMgrClassName);

      // Register the Principal property editor
      PropertyEditorFinder.getInstance().register(Principal.class, PrincipalEditor.class);
      PropertyEditorFinder.getInstance().register(SecurityDomain.class, SecurityDomainEditor.class);
      log.debug("Registered PrincipalEditor, SecurityDomainEditor");
      
      log.debug("ServerMode="+this.serverMode);
      log.debug("SecurityMgrClass="+JaasSecurityManagerService.securityMgrClass);
      log.debug("CallbackHandlerClass="+JaasSecurityManagerService.callbackHandlerClass);
   }

   protected void stopService() throws Exception
   {
      InitialContext ic = new InitialContext();

      try
      {
         ic.unbind(SECURITY_MGR_PATH);
      }
      catch(CommunicationException e)
      {
         // Do nothing, the naming services is already stopped
      }
      finally
      {
         ic.close();
      }
   }

   /** Register a SecurityDomain implmentation. This is synchronized to ensure
    * that the binding of the security domain and cache population is atomic.
    * @param securityDomain the name of the security domain
    * @param instance the SecurityDomain instance to bind
    */
   public synchronized void registerSecurityDomain(String securityDomain, SecurityDomain instance)
   {
      log.debug("Added "+securityDomain+", "+instance+" to map");
      CachePolicy authCache = lookupCachePolicy(securityDomain);
      
      SecurityDomainContext sdc = new SecurityDomainContext(instance, authCache);
      securityDomainCtxMap.put(securityDomain, sdc);
      // See if the security mgr supports an externalized cache policy
      setSecurityDomainCache(instance, authCache);
   }

   /** Access the CachePolicy for the securityDomain.
    * @param securityDomain the name of the security domain
    * @return The CachePolicy if found, null otherwise.
    */
   private static CachePolicy getCachePolicy(String securityDomain)
   {
      if( securityDomain.startsWith(SECURITY_MGR_PATH) )
         securityDomain = securityDomain.substring(SECURITY_MGR_PATH.length()+1);
      CachePolicy cache = null;
      try
      {
         SecurityDomainContext sdc = lookupSecurityDomain(securityDomain);
         if( sdc != null )
            cache = sdc.getAuthenticationCache();
      }
      catch(NamingException e)
      {
         log.debug("getCachePolicy("+securityDomain+") failure", e);
      }
      return cache;
   }

   /** Lookup the authentication CachePolicy object for a security domain. This
    method first treats the cacheJndiName as a ObjectFactory location that is
    capable of returning CachePolicy instances on a per security domain basis
    by appending a '/security-domain-name' string to the cacheJndiName when
    looking up the CachePolicy for a domain. If this fails then the cacheJndiName
    location is treated as a single CachePolicy for all security domains.
    */
   static CachePolicy lookupCachePolicy(String securityDomain)
   {
      CachePolicy authCache = null;
      String domainCachePath = cacheJndiName + '/' + securityDomain;
      try
      {
         InitialContext iniCtx = new InitialContext();
         authCache = (CachePolicy) iniCtx.lookup(domainCachePath);
      }
      catch(Exception e)
      {
         // Failed, treat the cacheJndiName name as a global CachePolicy binding
         try
         {
            InitialContext iniCtx = new InitialContext();
            authCache = (CachePolicy) iniCtx.lookup(cacheJndiName);
         }
         catch(Exception e2)
         {
            log.warn("Failed to locate auth CachePolicy at: "+cacheJndiName
               + " for securityDomain="+securityDomain);
         }
      }
      return authCache;
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
         Class[] setCachePolicyTypes = {CachePolicy.class};
         Method m = securityMgrClass.getMethod("setCachePolicy", setCachePolicyTypes);
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

   /** Use reflection to attempt to set the DeepCopySubject on the
    * securityMgr argument.  
    * @param securityMgr the security manager
    * @param flag deep copy subject option
    */
   private static void setDeepCopySubjectOption(AuthenticationManager securityMgr,
      boolean flag)
   { 
      Boolean bValue = flag ? Boolean.TRUE : Boolean.FALSE;
      try
      {
         Class[] setDeepCopySubjTypes = {Boolean.class};
         Method m = securityMgrClass.getMethod("setDeepCopySubjectOption", setDeepCopySubjTypes);
         Object[] setDeepCopySubjectOptionArgs = {bValue};
         m.invoke(securityMgr, setDeepCopySubjectOptionArgs);
         log.debug("setDeepCopySubjectOption, c="+setDeepCopySubjectOptionArgs[0]);
      }
      catch(Exception e2)
      {   // No setDeepCopySubjectOption support, this is ok
         log.debug("setDeepCopySubjectOption failed", e2);
      }
   }

   /** Lookup or create the SecurityDomainContext for securityDomain.
    * @param securityDomain
    * @return the SecurityDomainContext for securityDomain
    * @throws NamingException
    */
   private synchronized static SecurityDomainContext lookupSecurityDomain(String securityDomain)
         throws NamingException
   {
      SecurityDomainContext securityDomainCtx = (SecurityDomainContext) securityDomainCtxMap.get(securityDomain);
      if( securityDomainCtx == null )
      {
         securityDomainCtx = (SecurityDomainContext) new InitialContext().lookup(
                                 SecurityConstants.JAAS_CONTEXT_ROOT + "/" + securityDomain + "/domainContext");
         securityDomainCtxMap.put(securityDomain, securityDomainCtx);
         log.debug("Added "+securityDomain+", "+securityDomainCtx+" to map");
      }
      return securityDomainCtx;
   }
   
   /**
    * Get the default unauthenticated principal.
    * @return The principal name
    */
   public String getDefaultUnauthenticatedPrincipal()
   {
      return defaultUnauthenticatedPrincipal;
   }

   /**
    * Set the default unauthenticated principal.
    * @param principal The principal name
    */
   public void setDefaultUnauthenticatedPrincipal(String principal)
   {
      defaultUnauthenticatedPrincipal = principal;
   }
   
   /**
    * @see JaasSecurityManagerServiceMBean#getJCAInformation()
    */
   public String displayJCAInformation()
   {
      String[] sarr = new String[]{"Cipher","Signature","KeyFactory",
                             "SecretKeyFactory","AlgorithmParameters",
                             "MessageDigest","Mac"}; 
      StringBuilder sb = new StringBuilder();
      JCASecurityInfo jsi = new JCASecurityInfo();
      sb.append("JCA Providers=").append(jsi.getJCAProviderInfo());
      sb.append("JCA Service/Algorithms=");
      for(String serviceName:sarr)
      {
         sb.append(jsi.getJCAAlgorithms(serviceName));
      }
      return sb.toString();  
   }

    
   static class DomainEnumeration implements NamingEnumeration
   {
      Enumeration domains;
      Map ctxMap;
      DomainEnumeration(Enumeration domains, Map ctxMap)
      {
         this.domains = domains;
         this.ctxMap = ctxMap;
      }

      public void close()
      {
      }
      public boolean hasMoreElements()
      {
         return domains.hasMoreElements();
      }
      public boolean hasMore()
      {
         return domains.hasMoreElements();
      }
      public Object next()
      {
         String name = (String) domains.nextElement();
         Object value = ctxMap.get(name);
         String className = value.getClass().getName();
         NameClassPair pair = new NameClassPair(name, className);
         return pair;
      }
      public Object nextElement()
      {
         return domains.nextElement();
      }
   }

   /** java:/timedCacheFactory ObjectFactory implementation
    */
   public static class DefaultCacheObjectFactory implements InvocationHandler, ObjectFactory
   {
      /** Object factory implementation. This method returns a Context proxy
       that is only able to handle a lookup operation for an atomic name of
       a security domain.
      */
      public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment)
         throws Exception
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         Class[] interfaces = {Context.class};
         Context ctx = (Context) Proxy.newProxyInstance(loader, interfaces, this);
         return ctx;
      }
      /** This is the InvocationHandler callback for the Context interface that
       was created by out getObjectInstance() method. All this does is create
       a new TimedCache instance.
       */
      public Object invoke(Object obj, Method method, Object[] args) throws Throwable
      {
         TimedCachePolicy cachePolicy = new TimedCachePolicy(defaultCacheTimeout,
            true, defaultCacheResolution);
         cachePolicy.create();
         cachePolicy.start();
         return cachePolicy;
      }
   }

   static ClassLoader getContextClassLoader()
   {
      return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
      {
         public ClassLoader run()
         {
            return Thread.currentThread().getContextClassLoader();
         }
      });
   }
}
