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

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.security.Principal;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;

/** A BMP entity bean that creates beans on the fly with
a key equal to that passed to findByPrimaryKey. Obviously
not a real entity bean. It is used to test Principal propagation
using the echo method. 

@author Scott.Stark@jboss.org
@version $Revision: 81036 $
*/
public class EntityBeanImpl implements EntityBean
{
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
   
    private String key;
    private EntityContext context;

    public void ejbActivate()
    {
        log.debug("EntityBean.ejbActivate() called");
    }

    public void ejbPassivate()
    {
        log.debug("EntityBean.ejbPassivate() called");
    }

    public void ejbRemove()
    {
        log.debug("EntityBean.ejbRemove() called");
    }
    public void ejbLoad()
    {
        log.debug("EntityBean.ejbLoad() called");
        key = (String) context.getPrimaryKey();
    }
    public void ejbStore()
    {
        log.debug("EntityBean.ejbStore() called");
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
      this.key = key;
      return key;
   }
   public void ejbPostCreate(String key)
   {
   }

    public String echo(String arg)
    {
        log.debug("EntityBean.echo, arg="+arg);
        Principal p = context.getCallerPrincipal();
        boolean isInternalRole = context.isCallerInRole("InternalRole");
        log.debug("EntityBean.echo, callerPrincipal="+p);
        log.debug("EntityBean.echo, isCallerInRole('InternalRole')="+isInternalRole);
        // Check the java:comp/env/security/security-domain
        try
        {
           InitialContext ctx = new InitialContext();
           Object securityMgr = ctx.lookup("java:comp/env/security/security-domain");
           log.debug("Checking java:comp/env/security/security-domain");
           if( securityMgr == null )
              throw new EJBException("Failed to find security mgr under: java:comp/env/security/security-domain");
           log.debug("Found SecurityManager: "+securityMgr);
           Subject activeSubject = (Subject) ctx.lookup("java:comp/env/security/subject");
           log.debug("ActiveSubject: "+activeSubject);
        }
        catch(NamingException e)
        {
           log.debug("failed", e);
           throw new EJBException("Naming exception: "+e.toString(true));
        }
        return p.getName();
    }

    public String ejbFindByPrimaryKey(String key)
    {
        log.debug("EntityBean.ejbFindByPrimaryKey, key="+key);
        return key;
    }
}
