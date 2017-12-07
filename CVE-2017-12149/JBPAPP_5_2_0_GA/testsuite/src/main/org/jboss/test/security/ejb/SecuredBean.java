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

import java.rmi.RemoteException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Iterator;
import java.util.Set;
import javax.ejb.SessionContext;
import javax.ejb.SessionBean;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.jboss.test.security.interfaces.RunAsServiceRemote;
import org.jboss.test.security.interfaces.RunAsServiceRemoteHome;
import org.jboss.test.security.interfaces.CallerInfo;
import org.jboss.security.SimplePrincipal;

/**
 * A session facade that tests that the security context reflected by the 
 * SecurityAssociation.getSubject and PolicyContext. This will not run under
 * the security manager tests as ejbs are not granted access to these security
 * apis.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class SecuredBean implements SessionBean
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
      throws RemoteException
   {
      Principal caller = context.getCallerPrincipal();
      if( caller.equals(info.getCallerIdentity()) == false )
         throw new EJBException("getCallerPrincipal("+caller+") does not equal CallerIdentity: "+info.getCallerIdentity());

      validateRoles(info);
      try
      {
         Subject subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         validateRoles(info, subject);
      }
      catch (PolicyContextException e)
      {
         throw new EJBException(e);
      }

      RunAsServiceRemote bean = getBean();
      bean.unprotectedEjbMethod(info);
   }
   public void runAsMethod(CallerInfo info)
      throws RemoteException
   {
      Principal caller = context.getCallerPrincipal();
      if( caller.equals(info.getCallerIdentity()) == false )
         throw new EJBException("getCallerPrincipal("+caller+") does not equal CallerIdentity: "+info.getCallerIdentity());

      validateRoles(info);
      try
      {
         Subject subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         validateRoles(info, subject);
      }
      catch (PolicyContextException e)
      {
         throw new EJBException(e);
      }

      RunAsServiceRemote bean = getBean();
      bean.runAsMethod(info);
   }
   public void groupMemberMethod(CallerInfo info)
      throws RemoteException
   {
      Principal caller = context.getCallerPrincipal();
      if( caller.equals(info.getCallerIdentity()) == false )
         throw new EJBException("getCallerPrincipal("+caller+") does not equal CallerIdentity: "+info.getCallerIdentity());

      validateRoles(info);
      try
      {
         Subject subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         validateRoles(info, subject);
      }
      catch (PolicyContextException e)
      {
         throw new EJBException(e);
      }

      RunAsServiceRemote bean = getBean();
      bean.groupMemberMethod(info);
   }
   public void userMethod(CallerInfo info)
      throws RemoteException
   {
      Principal caller = context.getCallerPrincipal();
      if( caller.equals(info.getCallerIdentity()) == false )
         throw new EJBException("getCallerPrincipal("+caller+") does not equal CallerIdentity: "+info.getCallerIdentity());

      validateRoles(info);
      try
      {
         Subject subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         validateRoles(info, subject);
      }
      catch (PolicyContextException e)
      {
         throw new EJBException(e);
      }

      RunAsServiceRemote bean = getBean();
      bean.userMethod(info);
   }
   public void allAuthMethod(CallerInfo info)
      throws RemoteException
   {
      Principal caller = context.getCallerPrincipal();
      if( caller.equals(info.getCallerIdentity()) == false )
         throw new EJBException("getCallerPrincipal("+caller+") does not equal CallerIdentity: "+info.getCallerIdentity());

      validateRoles(info);
      try
      {
         Subject subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         validateRoles(info, subject);
      }
      catch (PolicyContextException e)
      {
         throw new EJBException(e);
      }

      RunAsServiceRemote bean = getBean();
      bean.allAuthMethod(info);
   }
   public void publicMethod(CallerInfo info)
      throws RemoteException
   {
      Principal caller = context.getCallerPrincipal();
      if( caller.equals(info.getCallerIdentity()) == false )
         throw new EJBException("getCallerPrincipal("+caller+") does not equal CallerIdentity: "+info.getCallerIdentity());

      validateRoles(info);
      try
      {
         Subject subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         validateRoles(info, subject);
      }
      catch (PolicyContextException e)
      {
         throw new EJBException(e);
      }

      RunAsServiceRemote bean = getBean();
      bean.publicMethod(info);
   }

   private RunAsServiceRemote getBean()
   {
      RunAsServiceRemote bean = null;
      try
      {
         InitialContext ctx = new InitialContext();
         RunAsServiceRemoteHome home = (RunAsServiceRemoteHome) ctx.lookup("jacc/RunAs");
         bean = home.create();
      }
      catch(Exception e)
      {
         throw new EJBException("Failed to create RunAsServiceRemote", e);
      }
      return bean;
   }

   private void validateRoles(CallerInfo info)
      throws EJBException
   {
      Iterator iter = info.getExpectedCallerRoles().iterator();
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
      // If there are no expected roles succeed
      if( info.getExpectedCallerRoles().size() == 0 )
         return;

      Iterator iter = info.getExpectedCallerRoles().iterator();
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
