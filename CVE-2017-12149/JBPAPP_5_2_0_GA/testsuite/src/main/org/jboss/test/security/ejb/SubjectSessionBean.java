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

import java.util.Set;
import java.util.Iterator;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.acl.Group;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.ejb.SessionContext;
import javax.ejb.SessionBean;
import javax.ejb.FinderException;
import javax.naming.InitialContext;

import org.jboss.security.SecurityAssociation;
import org.jboss.test.security.interfaces.StatelessSessionHome;
import org.jboss.test.security.interfaces.StatelessSession;
import org.jboss.test.security.interfaces.StatefulSessionHome;
import org.jboss.test.security.interfaces.EntityHome;
import org.jboss.test.security.interfaces.Entity;
import org.jboss.test.security.interfaces.StatefulSession;

/**
 * A session facade that tests that the security context reflected by the 
 * SecurityAssociation.getSubject and PolicyContext. This will not run under
 * the security manager tests as ejbs are not granted access to these security
 * apis.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class SubjectSessionBean implements SessionBean
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

   /**
    * 
    * @param callerName
    * @param callerPrincipals
    * @throws GeneralSecurityException
    */ 
   public void validateCallerContext(String callerName, Set callerPrincipals)
      throws GeneralSecurityException
   {
      Principal caller = context.getCallerPrincipal();
      String name = caller.getName();
      if( name.equals(callerName) == false )
         throw new GeneralSecurityException("CallerPrincipal.name("+name+") != "+callerName);

      validatePolicyContextSubject("enter", callerPrincipals);
      validateSecurityAssociationSubject("enter", callerPrincipals);

      InitialContext ctx = null;
      try
      {
         ctx = new InitialContext();
         StatelessSessionHome home = (StatelessSessionHome)
            ctx.lookup("java:comp/env/ejb/StatelessSession");
         StatelessSession bean = home.create();
         bean.echo("validateCallerContext");
         validatePolicyContextSubject("post stateless", callerPrincipals);
         validateSecurityAssociationSubject("post stateless", callerPrincipals);

         StatefulSessionHome home2 = (StatefulSessionHome) 
            ctx.lookup("java:comp/env/ejb/StatefulSession");
         StatefulSession bean2 = home2.create("validateCallerContext");
         bean2.echo("validateCallerContext");
         validatePolicyContextSubject("post stateful", callerPrincipals);
         validateSecurityAssociationSubject("post stateful", callerPrincipals);

         EntityHome home3 = (EntityHome)
            ctx.lookup("java:comp/env/ejb/Entity");
         Entity bean3 = null;
         try
         {
            bean3 = home3.findByPrimaryKey("validateCallerContext");
         }
         catch(FinderException e)
         {
            bean3 = home3.create("validateCallerContext");            
         }
         bean3.echo("validateCallerContext");
      }
      catch(Exception e)
      {
         GeneralSecurityException ex = new GeneralSecurityException("Unexpected exception");
         ex.initCause(e);
         throw ex;
      }
      validatePolicyContextSubject("exit", callerPrincipals);
      validateSecurityAssociationSubject("exit", callerPrincipals);
   }

   /**
    * Get the active subject as seen by the JACC policy context handler.
    * @throws GeneralSecurityException
    */ 
   protected void validatePolicyContextSubject(String ctx, Set callerPrincipals)
      throws GeneralSecurityException
   {
      try
      {
         Subject caller = caller = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         if( contains(caller, callerPrincipals) == false )
         {
            String msg = ctx+", PolicyContext subject: "+caller
               +" does not contain expected principals: "+callerPrincipals;
            throw new GeneralSecurityException(msg);
         }
      }
      catch(PolicyContextException e)
      {
         
      }
   }
   /**
    * Get the active subject as seen by the jboss SecurityAssociation
    * @throws GeneralSecurityException
    */ 
   protected void validateSecurityAssociationSubject(String ctx, Set callerPrincipals)
      throws GeneralSecurityException
   {
      Subject caller = SecurityAssociation.getSubject();
      if( contains(caller, callerPrincipals) == false )
      {
         String msg = ctx+", SecurityAssociation subject: "+caller
            +" does not contain expected principals: "+callerPrincipals;
         throw new GeneralSecurityException(msg);
      }
   }
   protected boolean contains(Subject s, Set callerPrincipals)
   {
      Set gs = s.getPrincipals(Group.class);
      Iterator iter = gs.iterator();
      while( iter.hasNext() )
      {
         Group g = (Group) iter.next();
         if( g.getName().equals("Roles") )
         {
            Iterator citer = callerPrincipals.iterator();
            while( citer.hasNext() )
            {
               Principal p = (Principal) citer.next();
               if( g.isMember(p) == false )
                  return false;
            }
         }
      }
      return true;
   }
}
