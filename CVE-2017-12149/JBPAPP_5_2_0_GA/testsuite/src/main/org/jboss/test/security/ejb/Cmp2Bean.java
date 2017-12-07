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
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * A CMP2 entity bean used to test Principal propagation using the echo method.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public abstract class Cmp2Bean implements EntityBean
{
   static Logger log = Logger.getLogger(Cmp2Bean.class);
   private EntityContext context;

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
   }

   public void ejbLoad()
   {
   }

   public void ejbStore()
   {
   }

   public void setEntityContext(EntityContext context)
   {
      this.context = context;
   }

   public void unsetEntityContext()
   {
      this.context = null;
   }

   public String ejbCreate(String key)
      throws CreateException
   {
      setKey(key);
      return null;
   }
   public void ejbPostCreate(String key)
   {
   }

   public abstract String getKey();
   public abstract void setKey(String key);

   public String echo(String arg)
   {
      Principal p = context.getCallerPrincipal();
      log.debug("EntityBean.echo, callerPrincipal=" + p);
      // Check the java:comp/env/security/security-domain
      try
      {
         InitialContext ctx = new InitialContext();
         Object securityMgr = ctx.lookup("java:comp/env/security/security-domain");
         log.debug("Checking java:comp/env/security/security-domain");
         if (securityMgr == null)
            throw new EJBException("Failed to find security mgr under: java:comp/env/security/security-domain");
         log.debug("Found SecurityManager: " + securityMgr);
         Subject activeSubject = (Subject) ctx.lookup("java:comp/env/security/subject");
         log.debug("ActiveSubject: " + activeSubject);
      }
      catch (NamingException e)
      {
         log.debug("failed", e);
         throw new EJBException("Naming exception: " + e.toString(true));
      }
      return p.getName();
   }

}
