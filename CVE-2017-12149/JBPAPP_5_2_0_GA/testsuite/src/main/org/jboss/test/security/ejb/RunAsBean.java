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
package org.jboss.test.security.ejb;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Set;
import java.util.Iterator;
import javax.ejb.SessionContext;
import javax.ejb.SessionBean;
import javax.ejb.EJBException;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.jboss.test.security.interfaces.CallerInfo;
import org.jboss.security.SimplePrincipal;

/**
 A target session bean that should be deployed with a caller executing with
 a run-as identity.

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class RunAsBean implements SessionBean
{
   /** The JACC PolicyContext key for the current Subject */
   private static final String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container";
   private SessionContext context;

   public void ejbCreate()
   {
   }
   public void ejbActivate()
   {
   }
   public void ejbPassivate()
   {
   }
   public void ejbRemove()
   {
   }
   public void setSessionContext(SessionContext context)
   {
      this.context = context;
   }

   public void unprotectedEjbMethod(CallerInfo info)
   {
      Principal caller = context.getCallerPrincipal();
      if( caller.equals(info.getRunAsIdentity()) == false )
         throw new EJBException("getCallerPrincipal("+caller+") does not contain runAsIdentity: "+info.getRunAsIdentity());

      validateRoles(info);

      try
      {
         Subject subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         String msg = "unprotectedEjbMethod, PolicyContext subject: "+subject
         + ", CallerPrincipal: "+caller;
         System.out.println(msg);
         Set principals = subject.getPrincipals();
         if( principals.contains(info.getRunAsIdentity()) == false )
            throw new EJBException(principals+" does not contain runAsIdentity: "+info.getRunAsIdentity());
         validateRoles(info, subject);
      }
      catch(PolicyContextException e)
      {
      }
   }
   public void runAsMethod(CallerInfo info)
   {
      Principal caller = context.getCallerPrincipal();
      if( caller.equals(info.getRunAsIdentity()) == false )
            throw new EJBException("getCallerPrincipal("+caller+") does not contain runAsIdentity: "+info.getRunAsIdentity());

      validateRoles(info);

      try
      {
         Subject subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         String msg = "runAsMethod, PolicyContext subject: "+subject
         + ", CallerPrincipal: "+caller;
         System.out.println(msg);
         Set principals = subject.getPrincipals();
         if( principals.contains(info.getRunAsIdentity()) == false )
            throw new EJBException(principals+" does not contain runAsIdentity: "+info.getRunAsIdentity());
         validateRoles(info, subject);
      }
      catch(PolicyContextException e)
      {
      }
   }
   public void groupMemberMethod(CallerInfo info)
   {
      Principal caller = context.getCallerPrincipal();
      if( caller.equals(info.getRunAsIdentity()) == false )
            throw new EJBException("getCallerPrincipal("+caller+") does not contain runAsIdentity: "+info.getRunAsIdentity());

      validateRoles(info);

      try
      {
         Subject subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         String msg = "groupMemberMethod, PolicyContext subject: "+subject
         + ", CallerPrincipal: "+caller;
         System.out.println(msg);
         Set principals = subject.getPrincipals();
         if( principals.contains(info.getRunAsIdentity()) == false )
            throw new EJBException(principals+" does not contain runAsIdentity: "+info.getRunAsIdentity());
         validateRoles(info, subject);
      }
      catch(PolicyContextException e)
      {
      }
   }
   public void userMethod(CallerInfo info)
   {
      Principal caller = context.getCallerPrincipal();
      if( caller.equals(info.getRunAsIdentity()) == false )
            throw new EJBException("getCallerPrincipal("+caller+") does not contain runAsIdentity: "+info.getRunAsIdentity());

      validateRoles(info);

      try
      {
         Subject subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         String msg = "userMethod, PolicyContext subject: "+subject
         + ", CallerPrincipal: "+caller;
         System.out.println(msg);
         Set principals = subject.getPrincipals();
         if( principals.contains(info.getRunAsIdentity()) == false )
            throw new EJBException(principals+" does not contain runAsIdentity: "+info.getRunAsIdentity());
         validateRoles(info, subject);
      }
      catch(PolicyContextException e)
      {
      }
   }
   public void allAuthMethod(CallerInfo info)
   {
      Principal caller = context.getCallerPrincipal();
      if( caller.equals(info.getRunAsIdentity()) == false )
            throw new EJBException("getCallerPrincipal("+caller+") does not contain runAsIdentity: "+info.getRunAsIdentity());

      validateRoles(info);

      try
      {
         Subject subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         String msg = "allAuthMethod, PolicyContext subject: "+subject
         + ", CallerPrincipal: "+caller;
         System.out.println(msg);
         Set principals = subject.getPrincipals();
         if( principals.contains(info.getRunAsIdentity()) == false )
            throw new EJBException(principals+" does not contain runAsIdentity: "+info.getRunAsIdentity());
         validateRoles(info, subject);
      }
      catch(PolicyContextException e)
      {
      }
   }
   public void publicMethod(CallerInfo info)
   {
      Principal caller = context.getCallerPrincipal();
      if( caller.equals(info.getRunAsIdentity()) == false )
            throw new EJBException("getCallerPrincipal("+caller+") does not contain runAsIdentity: "+info.getRunAsIdentity());

      validateRoles(info);

      try
      {
         Subject subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         String msg = "publicMethod, PolicyContext subject: "+subject
            + ", CallerPrincipal: "+caller;
         System.out.println(msg);
         validateRoles(info, subject);
      }
      catch(PolicyContextException e)
      {
      }
   }

   private void validateRoles(CallerInfo info)
      throws EJBException
   {
      Iterator iter = info.getExpectedRunAsRoles().iterator();
      StringBuffer buffer = new StringBuffer();
      while( iter.hasNext() )
      {
         String role = (String) iter.next();
         if( context.isCallerInRole(role) == false )
         {
            buffer.append(',');
            buffer.append(role);
         }
      }

      if( buffer.length() > 0 )
      {
         buffer.insert(0, "isCallerInRole failed for: ");
         throw new EJBException(buffer.toString());
      }
   }

   private void validateRoles(CallerInfo info, Subject subject)
      throws EJBException
   {
      Iterator iter = info.getExpectedRunAsRoles().iterator();
      Set groups = subject.getPrincipals(Group.class);
      if( groups == null || groups.size() == 0 )
         throw new EJBException("No groups found in the subject: "+subject);

      Group roles = (Group) groups.iterator().next();
      StringBuffer buffer = new StringBuffer();
      while( iter.hasNext() )
      {
         String role = (String) iter.next();
         SimplePrincipal srole = new SimplePrincipal(role);
         if( roles.isMember(srole) == false )
         {
            buffer.append(',');
            buffer.append(role);
         }
      }

      if( buffer.length() > 0 )
      {
         buffer.insert(0, "Principals failed for: ");
         throw new EJBException(buffer.toString());
      }
   }
}
