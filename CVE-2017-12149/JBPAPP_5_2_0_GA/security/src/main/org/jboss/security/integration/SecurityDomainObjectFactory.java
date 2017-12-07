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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.OperationNotSupportedException;
import javax.naming.spi.ObjectFactory;

import org.jboss.security.SecurityConstants;
import org.jboss.security.plugins.SecurityDomainContext;

//$Id: SecurityDomainObjectFactory.java 106477 2010-07-06 21:17:57Z mmoyses $

/**
 *  A JNDI Object Factory for the legacy integration
 *  to return an instance of SubjectSecurityManager
 *  @author Anil.Saldhana@redhat.com
 *  @since  Sep 10, 2007 
 *  @version $Revision: 106477 $
 */
public class SecurityDomainObjectFactory implements InvocationHandler, ObjectFactory
{
   private JNDIBasedSecurityManagement securityManagement = SecurityConstantsBridge.getSecurityManagement();
   
   public void setSecurityManagement(JNDIBasedSecurityManagement sm)
   {
      this.securityManagement = sm;
   }

   /** Object factory implementation. This method returns a Context proxy
   that is only able to handle a lookup operation for an atomic name of
   a security domain.
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx,
         Hashtable<?,?> environment)
   throws Exception
   {
      ClassLoader loader = SecurityActions.getContextClassLoader();
      Class<?>[] interfaces = {Context.class};
      Context ctx = (Context) Proxy.newProxyInstance(loader, interfaces, this);
      return ctx;
   }


   /** This is the InvocationHandler callback for the Context interface that
   was created by out getObjectInstance() method. We handle the java:/jaas/domain
   level operations here.
    */
   public Object invoke(Object obj, Method method, Object[] args) throws Throwable
   {
      Context ctx = new InitialContext();
      NameParser parser = ctx.getNameParser("");
      String securityDomain = null;
      Name name = null;
      
      
      String methodName = method.getName();
      if( methodName.equals("toString") == true )
         return SecurityConstants.JAAS_CONTEXT_ROOT + " Context proxy";

      if( methodName.equals("list") == true )
         return new DomainEnumeration(JNDIBasedSecurityManagement.securityMgrMap.keys(),
               JNDIBasedSecurityManagement.securityMgrMap); 
      
      if(methodName.equals("bind") || methodName.equals("rebind"))
      {
         if( args[0] instanceof String )
            name = parser.parse((String) args[0]);
         else
            name = (Name)args[0];
         securityDomain = name.get(0);
         SecurityDomainContext val = (SecurityDomainContext)args[1];
         JNDIBasedSecurityManagement.securityMgrMap.put(securityDomain, val); 
         return obj;
      }
      if( methodName.equals("lookup") == false )
         throw new OperationNotSupportedException("Only lookup is supported, op="+method);
      if( args[0] instanceof String )
         name = parser.parse((String) args[0]);
      else
         name = (Name)args[0];
      securityDomain = name.get(0);
      SecurityDomainContext securityDomainCtx = lookupSecurityDomain(securityDomain);
      //TODO: Legacy expectation was subjectsecuritymgr
      Object binding = securityDomainCtx.getSecurityManager(); 
      // Look for requests against the security domain context
      if( name.size() == 2 )
      {
         String request = name.get(1);
         binding = securityDomainCtx.lookup(request);
      }
      return binding; 
   }
   
   private SecurityDomainContext lookupSecurityDomain(String securityDomain)
   throws Exception
   {
      SecurityDomainContext sdc = (SecurityDomainContext) JNDIBasedSecurityManagement.securityMgrMap.get(securityDomain);
      if( sdc == null )
      {
         sdc = securityManagement.createSecurityDomainContext(securityDomain);
         JNDIBasedSecurityManagement.securityMgrMap.put(securityDomain, sdc); 
      }
      return sdc;
   }
   
   class DomainEnumeration implements NamingEnumeration<NameClassPair>
   {
      Enumeration<String> domains;
      Map<String,SecurityDomainContext> ctxMap;
      DomainEnumeration(Enumeration<String> domains, Map<String,SecurityDomainContext> ctxMap)
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
      public NameClassPair next()
      {
         String name = (String) domains.nextElement();
         Object value = ctxMap.get(name);
         String className = value.getClass().getName();
         NameClassPair pair = new NameClassPair(name, className);
         return pair;
      }
      public NameClassPair nextElement()
      {
         return next();
         //return domains.nextElement();
      }
   }  
}
