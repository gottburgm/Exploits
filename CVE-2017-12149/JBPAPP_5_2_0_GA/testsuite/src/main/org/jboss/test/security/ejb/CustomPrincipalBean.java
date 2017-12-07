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
import java.util.Set;
import java.util.Iterator;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import org.jboss.logging.Logger;

/** Test return of a custom principal from getCallerPrincipal.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class CustomPrincipalBean implements SessionBean
{
   private static Logger log = Logger.getLogger(CustomPrincipalBean.class);

   private SessionContext ctx;

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

   public void setSessionContext(SessionContext ctx)
   {
      this.ctx = ctx;
   }

   public boolean validateCallerPrincipal(Class type)
   {
      ClassLoader typeLoader = type.getClassLoader();
      log.info("validateCallerPrincipal, type="+type+", loader="+typeLoader);
      Principal caller = ctx.getCallerPrincipal();
      log.info("caller="+caller+", class="+caller.getClass());
      boolean isType = true;
      if( caller.getClass().isAssignableFrom(type) == false )
      {
         log.error("type of caller is not: "+type);
         isType = false;
      }

      try
      {
         InitialContext ctx = new InitialContext();
         Subject s = (Subject) ctx.lookup("java:comp/env/security/subject");
         Set principals = s.getPrincipals();
         Iterator iter = principals.iterator();
         while( iter.hasNext() )
         {
            Object p = iter.next();
            ClassLoader pLoader = p.getClass().getClassLoader();
            log.info("type="+p.getClass()+", loader="+pLoader);            
         }
         Set customPrincipals = s.getPrincipals(type);
         caller = (Principal) customPrincipals.iterator().next();
         log.info("Subject caller="+caller+", class="+caller.getClass());
         if( caller.getClass().isAssignableFrom(type) == true )
         {
            log.info("type of caller is: "+type);
            isType = true;
         }
      }
      catch(Exception e)
      {
         log.error("Failed to lookup security mgr", e);
      }
      return isType;
   }

}
