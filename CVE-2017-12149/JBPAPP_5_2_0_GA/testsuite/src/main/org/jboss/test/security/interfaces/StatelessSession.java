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

import javax.ejb.*;
import java.rmi.*;

public interface StatelessSession extends EJBObject
{
    /** A method that returns its arg */
    public String echo(String arg) throws RemoteException;
    /** A method that does nothing but is not assiged a
     method-permission in the ejb-jar.xml descriptor
    */
    public void noop() throws RemoteException;
    /** A method that looks up the StatelessSession bean located at
     java:comp/env/ejb/Session and invokes echo(echoArg) on the
     bean and returns the result.
    */
    public String forward(String echoArg) throws RemoteException;
    /** A method that throws a NullPointerException */
    public void npeError() throws RemoteException;
    /** A method that is assigned the method-permission/unchecked tag
     to allow any authenticated user call the method.
     */
    public void unchecked() throws RemoteException;

    /** A method that is assigned to the exclude-list tag
     to indicate that no users should be allowed to call it.
     */
    public void excluded() throws RemoteException;
}
