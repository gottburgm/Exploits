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
package org.jboss.test.jmx.interceptors;

import java.security.Principal;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import javax.naming.InitialContext;
import javax.security.auth.Subject;

import org.jboss.mx.interceptor.AbstractInterceptor;
import org.jboss.mx.server.Invocation;
import org.jboss.mx.server.MBeanInvoker;
import org.jboss.logging.Logger;
import org.jboss.security.RealmMapping;
import org.jboss.security.SubjectSecurityManager;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.SecurityAssociation;
import org.jboss.invocation.MarshalledInvocation;

/** A role based security interceptor that requries the caller of
 * any write operations to have a JNDIWriter role and the caller of any
 * read operations to have a JNDIReader role.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public final class JNDISecurity
   extends AbstractInterceptor
{
   private static Logger log = Logger.getLogger(JNDISecurity.class);
   private static final Principal READER_ROLE = new SimplePrincipal("JNDIReader");
   private static final Principal WRITER_ROLE = new SimplePrincipal("JNDIWriter");

   private String securityDomain;
   private SubjectSecurityManager authMgr;
   private RealmMapping roleMgr;
   private Map methodMap;

   public String getSecurityDomain()
   {
      return securityDomain;
   }
   public void setSecurityDomain(String securityDomain) throws Exception
   {
      log.info("setSecurityDomain: "+securityDomain);
      this.securityDomain = securityDomain;
      InitialContext ctx = new InitialContext();
      this.authMgr = (SubjectSecurityManager) ctx.lookup(securityDomain);
      this.roleMgr = (RealmMapping) ctx.lookup(securityDomain);
   }

   // Interceptor overrides -----------------------------------------
   public Object invoke(Invocation invocation) throws Throwable
   {
      String opName = invocation.getName();
      log.info("invoke, opName="+opName);

      // If this is not the invoke(Invocation) op just pass it along
      if( opName == null || opName.equals("invoke") == false )
         return invocation.nextInterceptor().invoke(invocation);

      Object[] args = invocation.getArgs();
      org.jboss.invocation.Invocation invokeInfo =
         (org.jboss.invocation.Invocation) args[0];
      // There must be a valid security manager
      if( authMgr == null || roleMgr == null )
      {
         String msg = "No security mgr configured, check securityDomain: "+securityDomain;
         throw new SecurityException(msg);
      }

      // Get the security context passed from the client
      Principal principal = invokeInfo.getPrincipal();
      Object credential = invokeInfo.getCredential();
      Subject subject = new Subject();
      if( authMgr.isValid(principal, credential, subject) == false )
      {
         String msg = "Failed to authenticate principal: "+principal;
         throw new SecurityException(msg);
      }
      SecurityAssociation.pushSubjectContext(subject, principal, credential);

      try
      {
         // See what operation is being attempted
         if( methodMap == null )
            initMethodMap(invocation);
         HashSet methodRoles = new HashSet();
         if( invokeInfo instanceof MarshalledInvocation )
         {
            MarshalledInvocation mi = (MarshalledInvocation) invokeInfo;
            mi.setMethodMap(methodMap);
         }
         Method method = invokeInfo.getMethod();
         boolean isRead = isReadMethod(method);
         if( isRead == true )
            methodRoles.add(READER_ROLE);
         else
            methodRoles.add(WRITER_ROLE);
         if( roleMgr.doesUserHaveRole(principal, methodRoles) == false )
         {
            String msg = "Failed to authorize subject: "+authMgr.getActiveSubject()
               + " principal: " + principal
               + " for access roles:" + methodRoles;
            throw new SecurityException(msg);
         }
   
         // Let the invocation go
         return invocation.nextInterceptor().invoke(invocation);
      }
      finally
      {
         SecurityAssociation.popSubjectContext();         
      }
   }

   private boolean isReadMethod(Method method)
   {
      boolean isRead = true;
      String name = method.getName();
      isRead = name.equals("lookup") || name.equals("list")
         || name.equals("listBindings");
      return isRead;
   }

   /**
    *
    */
   private void initMethodMap(Invocation invocation) throws Throwable
   {
      MBeanInvoker invoker = invocation.getInvoker();
      methodMap = (Map) invoker.getAttribute("MethodMap");
   }
}
