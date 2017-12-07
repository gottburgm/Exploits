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
package javax.management.remote.rmi;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import javax.security.auth.Subject;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class RMIJRMPServerImpl extends RMIServerImpl
{
   private final int port;
   private final RMIClientSocketFactory clientSocketFactory;
   private final RMIServerSocketFactory serverSocketFactory;
   private final Map environment;

   public RMIJRMPServerImpl(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf, Map env)
         throws IOException
   {
      super(env);
      this.port = port;
      this.clientSocketFactory = csf;
      this.serverSocketFactory = ssf;
      this.environment = env;
   }

   protected void export() throws IOException
   {
      UnicastRemoteObject.exportObject(this, port, clientSocketFactory, serverSocketFactory);
   }

   protected String getProtocol()
   {
      return "rmi";
   }

   public Remote toStub() throws IOException
   {
      return RemoteObject.toStub(this);
   }

   protected RMIConnection makeClient(String connectionId, Subject subject) throws IOException
   {
      RMIConnection client = new RMIConnectionImpl(this, connectionId, getDefaultClassLoader(), subject, environment);
      UnicastRemoteObject.exportObject(client, port, clientSocketFactory, serverSocketFactory);
      return client;
   }

   protected void closeClient(RMIConnection client) throws IOException
   {
      UnicastRemoteObject.unexportObject(client, true);
   }

   protected void closeServer() throws IOException
   {
      UnicastRemoteObject.unexportObject(this, true);
   }


}