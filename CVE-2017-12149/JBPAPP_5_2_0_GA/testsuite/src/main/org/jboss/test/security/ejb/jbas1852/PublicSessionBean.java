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
import javax.naming.InitialContext;

/** An implmentation of the Session interface that delegates its
echo method call to the PrivateSession bean to test run-as.

@author Scott.Stark@jboss.org
@version $Revision: 81036 $ 
*/
public class PublicSessionBean implements SessionBean
{
    private SessionContext sessionContext;

    public void ejbCreate() throws CreateException
    {
        System.out.println("PublicSessionBean.ejbCreate() called");
    }

    public void ejbActivate()
    {
        System.out.println("PublicSessionBean.ejbActivate() called");
    }

    public void ejbPassivate()
    {
        System.out.println("PublicSessionBean.ejbPassivate() called");
    }

    public void ejbRemove()
    {
        System.out.println("PublicSessionBean.ejbRemove() called");
    }

    public void setSessionContext(SessionContext context)
    {
        sessionContext = context;
    }

    public String echo(String arg)
    {
        System.out.println("PublicSessionBean.echo, arg="+arg);
        Principal p = sessionContext.getCallerPrincipal();
        System.out.println("PublicSessionBean.echo, callerPrincipal="+p);
        System.out.println("PublicSessionBean.echo, isCallerInRole('EchoUser')="+sessionContext.isCallerInRole("EchoUser"));
        try
        {
            InitialContext ctx = new InitialContext();
			SessionHome home = (SessionHome) ctx.lookup("java:comp/env/ejb/PrivateSession");
            Session bean = home.create();
            System.out.println("PublicSessionBean.echo, created PrivateSession");
            arg = bean.echo(arg);
        }
        catch(Exception e)
        {
        }
        return arg;
    }
    public void noop()
    {
        System.out.println("PublicSessionBean.noop");
        Principal p = sessionContext.getCallerPrincipal();
        System.out.println("PublicSessionBean.noop, callerPrincipal="+p);
    }
    public void restricted() 
    {
        System.out.println("PublicSessionBean.restricted");
        Principal p = sessionContext.getCallerPrincipal();
        System.out.println("PublicSessionBean.restricted, callerPrincipal="+p);
    }
}
