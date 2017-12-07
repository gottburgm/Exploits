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
package org.jboss.test.securitymgr.interfaces;

import java.io.IOException;
import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */ 
public interface IOSession extends EJBObject
{
   public String read(String path) throws IOException, RemoteException;
   public void write(String path) throws IOException, RemoteException;
   public void listen(int port) throws IOException, RemoteException;
   public void connect(String host, int port) throws IOException, RemoteException;
   public void createClassLoader() throws RemoteException;
   public void getContextClassLoader() throws RemoteException;
   public void setContextClassLoader() throws RemoteException;
   public void renameThread() throws RemoteException;
   public void createThread() throws RemoteException;
   public void useReflection() throws RemoteException;
   public void loadLibrary() throws RemoteException;
   public void createSecurityMgr() throws RemoteException;
   public void changeSystemOut() throws RemoteException;
   public void changeSystemErr() throws RemoteException;
   public void systemExit(int status) throws RemoteException;
}
