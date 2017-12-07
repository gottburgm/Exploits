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

import java.io.Serializable;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;

import org.jboss.security.Util;
import org.jboss.security.srp.SRPConf;
import org.jboss.security.srp.SRPParameters;
import org.jboss.security.srp.SRPServerInterface;
import org.jboss.security.srp.SRPServerSession;

/** A simple hard coded implementation of SRPServerInterface that validates
 any given username to the password and salt provided to its constructor.
 
 @author Scott.Stark@jboss.org
 @version $Revision: 81038 $
 */
public class SimpleSRPServer implements SRPServerInterface
{
   SRPParameters params;
   SRPServerSession session;
   char[] password;

   public Object[] getSRPParameters(String username, boolean mutipleSessions)
         throws KeyException, RemoteException
   {
      return new Object[0];
   }

   public byte[] init(String username, byte[] A, int sessionID) throws SecurityException,
         NoSuchAlgorithmException, RemoteException
   {
      return new byte[0];
   }

   public byte[] verify(String username, byte[] M1, int sessionID)
         throws SecurityException, RemoteException
   {
      return new byte[0];
   }

   public byte[] verify(String username, byte[] M1, Object auxChallenge)
         throws SecurityException, RemoteException
   {
      return new byte[0];
   }

   public byte[] verify(String username, byte[] M1, Object auxChallenge, int sessionID)
         throws SecurityException, RemoteException
   {
      return new byte[0];
   }

   public void close(String username, int sessionID) throws SecurityException, RemoteException
   {
   }

   SimpleSRPServer(char[] password, String salt)
   {
      byte[] N = SRPConf.getDefaultParams().Nbytes();
      byte[] g = SRPConf.getDefaultParams().gbytes();
      byte[] s = Util.fromb64(salt);
      params = new SRPParameters(N, g, s);
      this.password = password;
   }
   
   public SRPParameters getSRPParameters(String username) throws KeyException, RemoteException
   {
      return params;
   }
   
   public byte[] init(String username,byte[] A) throws SecurityException,
      NoSuchAlgorithmException, RemoteException
   {
      // Calculate the password verfier v
      byte[] v = Util.calculateVerifier(username, password, params.s, params.N, params.g);
      // Create an SRP session
      session = new SRPServerSession(username, v, params);
      byte[] B = session.exponential();
      session.buildSessionKey(A);
      
      return B;
   }
   
   public byte[] verify(String username, byte[] M1) throws SecurityException, RemoteException
   {
      if( session.verify(M1) == false )
         throw new SecurityException("Failed to verify M1");
      return session.getServerResponse();
   }
  
   /** Close the SRP session for the given username.
    */
   public void close(String username) throws SecurityException, RemoteException
   {
   }

}
