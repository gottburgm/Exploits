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
package org.jboss.test.exception;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;

/**
 * A test of entity beans exceptions.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public interface EntityExceptionTesterLocal
   extends EJBLocalObject
{
    public String getKey();

    public void applicationExceptionInTx()
       throws ApplicationException;

    public void applicationExceptionInTxMarkRollback()
       throws ApplicationException;

    public void applicationErrorInTx();

    public void ejbExceptionInTx();

    public void runtimeExceptionInTx();

    public void applicationExceptionNewTx()
       throws ApplicationException;

    public void applicationExceptionNewTxMarkRollback()
       throws ApplicationException;

    public void applicationErrorNewTx();

    public void ejbExceptionNewTx();

    public void runtimeExceptionNewTx();

    public void applicationExceptionNoTx()
       throws ApplicationException;

    public void applicationErrorNoTx();

    public void ejbExceptionNoTx();

    public void runtimeExceptionNoTx();
} 