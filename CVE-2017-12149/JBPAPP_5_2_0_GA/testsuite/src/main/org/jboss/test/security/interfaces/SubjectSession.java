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
package org.jboss.test.security.interfaces;

import java.util.Set;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 * A session facade interface for validating the security context 
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public interface SubjectSession extends EJBObject
{
   /**
    * Call a method on the ejb/StatelessSession, ejb/StatefulSession, ejb/Entity
    * ejb-refs and validate that the security context seen after each call
    * matches the expected context seen at the start of the call.
    * 
    * @param callerName - the Principal.getName expected for the getCallerPrincipal
    * @param callerPrincipals - Set<Principal> for the current Subject.getPrincipals set
    * @throws java.security.GeneralSecurityException
    */ 
   public void validateCallerContext(String callerName, Set callerPrincipals)
      throws GeneralSecurityException, RemoteException;
}
