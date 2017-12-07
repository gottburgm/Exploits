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
package org.jboss.test.jmsra.bean;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBException;

import org.jboss.test.util.ejb.EntitySupport;

/**
 * 3rdparty bean to help test JMS RA transactions.
 *
 * <p>Created: Tue Apr 24 22:32:41 2001
 *
 * @author  <a href="mailto:peter.antman@tim.se">Peter Antman</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 81036 $
 */
public class PublisherCMPBean
    extends EntitySupport
{
    public Integer nr;
    
    public PublisherCMPBean() {
        // empty
    }
    
    public Integer getNr() {
        return nr;
    }

    public void setNr(Integer nr) {
        this.nr = nr;
    }

    public void ok(int nr) {
        // Do nothing
    }

    public void error(int nr) {
        // Roll back throug an exception
        throw new EJBException("Roll back!");
    }
    
    // EntityBean implementation -------------------------------------
    
    public Integer ejbCreate(Integer nr)
        throws CreateException
    {
        this.nr = nr;
        return null;
    }

    public void ejbPostCreate(Integer nr)
        throws CreateException
    {
    }

    public void ejbLoad()
    {
    }
    
} // PublisherCMPBean
