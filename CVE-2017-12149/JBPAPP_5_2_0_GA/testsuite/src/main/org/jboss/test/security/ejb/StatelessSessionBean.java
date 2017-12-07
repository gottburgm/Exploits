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
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/** A simple session bean for testing declarative security.

@author Scott.Stark@jboss.org
@version $Revision: 81036 $
*/
public class StatelessSessionBean implements SessionBean
{
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
   
    private SessionContext sessionContext;

    public void ejbCreate() throws CreateException
    {
        log.debug("ejbCreate() called");
    }

    public void ejbActivate()
    {
        log.debug("ejbActivate() called");
    }

    public void ejbPassivate()
    {
        log.debug("ejbPassivate() called");
    }

    public void ejbRemove()
    {
        log.debug("ejbRemove() called");
    }

    public void setSessionContext(SessionContext context)
    {
        sessionContext = context;
    }

    public String echo(String arg)
    {
        log.debug("echo, arg="+arg);
        Principal p = sessionContext.getCallerPrincipal();
        log.debug("echo, callerPrincipal="+p);
        boolean isCaller = sessionContext.isCallerInRole("EchoCaller");
        log.debug("echo, isCallerInRole('EchoCaller')="+isCaller);
        if( isCaller == false )
            throw new SecurityException("Caller does not have EchoCaller role");
        return arg;
    }
    public String forward(String echoArg)
    {
        log.debug("forward, echoArg="+echoArg);
        return echo(echoArg);
    }

    public void noop()
    {
        log.debug("noop");
    }
    public void npeError()
    {
        log.debug("npeError");
        Object obj = null;
        obj.toString();
    }

    public void unchecked()
    {
        Principal p = sessionContext.getCallerPrincipal();
        log.debug("unchecked, callerPrincipal="+p);
    }

    public void excluded()
    {
        throw new EJBException("excluded, no access should be allowed");
    }

}
