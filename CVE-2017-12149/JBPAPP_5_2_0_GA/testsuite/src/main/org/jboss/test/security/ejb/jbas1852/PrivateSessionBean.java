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
package org.jboss.test.security.ejb.jbas1852;

import java.security.Principal;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/** An implmentation of the Session interface that should not
be accessible by external users.

@author Scott.Stark@jboss.org
@version $Revision: 81036 $ 
*/
public class PrivateSessionBean implements SessionBean
{
    private SessionContext sessionContext;

    public void ejbCreate() throws CreateException
    {
        System.out.println("PrivateSessionBean.ejbCreate() called");
    }

    public void ejbActivate() 
    {
        System.out.println("PrivateSessionBean.ejbActivate() called");
    }

    public void ejbPassivate() 
    {
        System.out.println("PrivateSessionBean.ejbPassivate() called");
    }

    public void ejbRemove() 
    {
        System.out.println("PrivateSessionBean.ejbRemove() called");
    }

    public void setSessionContext(SessionContext context) 
    {
        sessionContext = context;
    }

    public String echo(String arg)
    {
        System.out.println("PrivateSessionBean.echo, arg="+arg);
        Principal p = sessionContext.getCallerPrincipal();
        System.out.println("PrivateSessionBean.echo, callerPrincipal="+p);
        System.out.println("PrivateSessionBean.echo, isCallerInRole('InternalUser')="+sessionContext.isCallerInRole("InternalUser"));
        return arg;
    }
    public void noop() 
    {
        System.out.println("PrivateSessionBean.noop");
        Principal p = sessionContext.getCallerPrincipal();
        System.out.println("PrivateSessionBean.noop, callerPrincipal="+p);
    }
    public void restricted() 
    {
        System.out.println("PrivateSessionBean.restricted");
        Principal p = sessionContext.getCallerPrincipal();
        System.out.println("PrivateSessionBean.restricted, callerPrincipal="+p);
    }
}
