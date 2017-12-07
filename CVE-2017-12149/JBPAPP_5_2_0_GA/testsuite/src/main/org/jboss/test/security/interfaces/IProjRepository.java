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

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.rmi.RemoteException;

/** The business interface for the ProjRepository bean.

@see ProjRepository

@author Scott.Stark@jboss.org
@version $Revision: 81036 $
 * @stereotype business-interface
*/
public interface IProjRepository
{
    public void createFolder(Name folderPath) throws NamingException, RemoteException;
    public void deleteFolder(Name folderPath, boolean recursive) throws NamingException, RemoteException;
    public void createItem(Name itemPath, Attributes attributes) throws NamingException, RemoteException;
    public void updateItem(Name itemPath, Attributes attributes) throws NamingException, RemoteException;
    public void deleteItem(Name itemPath) throws NamingException, RemoteException;
    public Attributes getItem(Name itemPath) throws NamingException, RemoteException;
}
