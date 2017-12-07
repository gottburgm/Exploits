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
package org.jboss.test;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.jboss.security.Util;
import org.jboss.security.srp.SerialObjectStore;
import org.jboss.security.srp.SRPRemoteServer;

/** An RMI application that creates a SRPRemoteServer instance and
exports it on the standard RMI register 1099 port. It creates a
SerialObjectStore as the SRPVerifierStore for the SRPRemoteServer.

@author Scott.Stark@jboss.org
@version $Revision: 81038 $
*/
public class SRPServerImpl
{
    SerialObjectStore store;

    void run() throws IOException, AlreadyBoundException, RemoteException
    {
        store = new SerialObjectStore();
        SRPRemoteServer server = new SRPRemoteServer(store);
        Registry reg = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        reg.bind("SimpleSRPServer", server);
    }

    public static void main(String[] args) throws Exception
    {
        SRPServerImpl server = new SRPServerImpl();
        server.run();
    }
}
