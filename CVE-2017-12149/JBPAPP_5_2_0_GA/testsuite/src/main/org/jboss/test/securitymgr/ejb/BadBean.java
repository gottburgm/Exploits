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
package org.jboss.test.securitymgr.ejb;

import java.security.Principal;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.RunAsIdentity;

/** A session bean that attempts things that should not be allowed
when running JBoss with a security manager.
 
@author Scott.Stark@jboss.org
@version $Revision: 81036 $
 */
public class BadBean implements SessionBean
{
   static final Logger log = Logger.getLogger(BadBean.class);

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
   }

   /** Creates a new instance of BadBean */
   public BadBean()
   {
   }
   
   public void accessSystemProperties()
   {
      System.getProperty("java.home");
      System.setProperty("java.home","tjo");
   }
   
   public Principal getPrincipal()
   {
      return SecurityAssociation.getPrincipal();
   }
   public Object getCredential()
   {
      return SecurityAssociation.getCredential();
   }
   public void setPrincipal(Principal user)
   {
      SecurityAssociation.setPrincipal(user);
   }
   public void setCredential(char[] password)
   {
      SecurityAssociation.setCredential(password);
   }
   public void getSubject()
   {
      // This should be allowed
      Subject s = SecurityAssociation.getSubject();
   }
   public void getSubjectCredentials()
   {
      // This should be allowed
      Subject s = SecurityAssociation.getSubject();
      // This should fail
      s.getPrivateCredentials();
   }
   public void setSubject()
   {
      Subject s = new Subject();
      SecurityAssociation.pushSubjectContext(s, null, null);
   }
   public void popRunAsRole()
   {
      SecurityAssociation.popRunAsIdentity();
   }
   public void pushRunAsRole()
   {
      RunAsIdentity runAs = new RunAsIdentity("SuperUser", "admin");
      SecurityAssociation.pushRunAsIdentity(runAs);
   }

}
