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
package org.jboss.crypto;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.crypto.SecretKey;

/**
 *
 * @author  Scott.Stark@jboss.org
 */
public class CipherServerSocket extends ServerSocket
{
   String algorithm;
   SecretKey key;

   /** Creates a new instance of CipherServerSocket */
   public CipherServerSocket(int port, int backlog,
      InetAddress bindAddr, String algorithm, SecretKey key) throws IOException
   {
      super(port, backlog, bindAddr);
      this.algorithm = algorithm;
      this.key = key;
   }

   public Socket accept() throws IOException
   {
      Socket s = super.accept();
      return new CipherSocket(s, algorithm, key);
   }
}
