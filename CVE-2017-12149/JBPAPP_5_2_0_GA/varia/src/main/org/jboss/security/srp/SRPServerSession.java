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
package org.jboss.security.srp;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.jboss.logging.Logger;
import org.jboss.crypto.CryptoUtil;

/** The server side logic to the SRP protocol. The class is the server side
 equivalent of the SRPClientSession object. An implementation of
 SRPServerInterface creates an SRPServerSession on the start of a login
 session.
 
 The client side algorithm using these classes consists of:
 
 1. Get server, SRPServerInterface server = (SRPServerInterface) Naming.lookup(...);
 2. Get SRP parameters, SRPParameters params = server.getSRPParameters(username);
 3. Create a client session, SRPClientSession client = new SRPClientSession(username, password, params);
 4. Exchange public keys, byte[] A = client.exponential();
 byte[] B = server.init(username, A);
 5. Exchange challenges, byte[] M1 = client.response(B);
 byte[] M2 = server.verify(username, M1);
 6. Verify the server response, if( client.verify(M2) == false )
 throw new SecurityException("Failed to validate server reply");
 7. Validation complete
 
 Note that these steps are stateful. They must be performed in order and a
 step cannot be repeated to update the session state.
 
 This product uses the 'Secure Remote Password' cryptographic
 authentication system developed by Tom Wu (tjw@CS.Stanford.EDU).
 
 @author Scott.Stark@jboss.org
 @version $Revision: 81038 $
 */
public class SRPServerSession implements Serializable
{
   /** The serial version ID
    @since 1.6
    */
   static final long serialVersionUID = -2448005747721323704L;
   private static int B_LEN = 64; // 64 bits for 'b'
   private static Logger log = Logger.getLogger(SRPServerSession.class);

   private SRPParameters params;
   private BigInteger N;
   private BigInteger g;
   private BigInteger v;
   private BigInteger b;
   private BigInteger B;
   private byte[] K;
   /** The M1 = H(H(N) xor H(g) | H(U) | s | A | B | K) hash */
   private transient MessageDigest clientHash;
   private byte[] M1;
   /** The M2 = H(A | M | K) hash */
   private transient MessageDigest serverHash;
   private byte[] M2;
   
   /** Creates a new SRP server session object from the username, password
    verifier, and session parameters.
    @param username, the user ID
    @param vb, the password verifier byte sequence
    @param params, the SRP parameters for the session
    */
   public SRPServerSession(String username, byte[] vb, SRPParameters params)
   {
      this.params = params;
      this.v = new BigInteger(1, vb);
      this.g = new BigInteger(1, params.g);
      this.N = new BigInteger(1, params.N);
      if( log.isTraceEnabled() )
         log.trace("g: "+CryptoUtil.tob64(params.g));
      if( log.isTraceEnabled() )
         log.trace("v: "+CryptoUtil.tob64(vb));
      serverHash = CryptoUtil.newDigest();
      clientHash = CryptoUtil.newDigest();
      // H(N)
      byte[] hn = CryptoUtil.newDigest().digest(params.N);
      if( log.isTraceEnabled() )
         log.trace("H(N): "+CryptoUtil.tob64(hn));
      // H(g)
      byte[] hg = CryptoUtil.newDigest().digest(params.g);
      if( log.isTraceEnabled() )
         log.trace("H(g): "+CryptoUtil.tob64(hg));
      // clientHash = H(N) xor H(g)
      byte[] hxg = CryptoUtil.xor(hn, hg, 20);
      if( log.isTraceEnabled() )
         log.trace("H(N) xor H(g): "+CryptoUtil.tob64(hxg));
      clientHash.update(hxg);
      if( log.isTraceEnabled() )
      {
         MessageDigest tmp = CryptoUtil.copy(clientHash);
         log.trace("H[H(N) xor H(g)]: "+CryptoUtil.tob64(tmp.digest()));
      }
      // clientHash = H(N) xor H(g) | H(U)
      clientHash.update(CryptoUtil.newDigest().digest(username.getBytes()));
      if( log.isTraceEnabled() )
      {
         MessageDigest tmp = CryptoUtil.copy(clientHash);
         log.trace("H[H(N) xor H(g) | H(U)]: "+CryptoUtil.tob64(tmp.digest()));
      }
      // clientHash = H(N) xor H(g) | H(U) | s
      clientHash.update(params.s);
      if( log.isTraceEnabled() )
      {
         MessageDigest tmp = CryptoUtil.copy(clientHash);
         log.trace("H[H(N) xor H(g) | H(U) | s]: "+CryptoUtil.tob64(tmp.digest()));
      }
      K = null;
   }
   
   /**
    * @returns The user's password salt
    */
   public SRPParameters getParameters()
   {
      return params;
   }
   
   /**
    * @returns The exponential residue (parameter B) to be sent to the
    *          client.
    */
   public byte[] exponential()
   {
      if(B == null)
      {
         BigInteger one = BigInteger.valueOf(1);
         do
         {
            b = new BigInteger(B_LEN, CryptoUtil.getPRNG());
         } while(b.compareTo(one) <= 0);
         B = v.add(g.modPow(b, N));
         if(B.compareTo(N) >= 0)
            B = B.subtract(N);
      }
      return CryptoUtil.trim(B.toByteArray());
   }
   
   /**
   @param ab The client's exponential (parameter A).
   @returns The secret shared session K between client and server
    @exception NoSuchAlgorithmException thrown if the session key
    MessageDigest algorithm cannot be found.
    */
   public void buildSessionKey(byte[] ab) throws NoSuchAlgorithmException
   {
      if( log.isTraceEnabled() )
         log.trace("A: "+CryptoUtil.tob64(ab));
      byte[] nb = CryptoUtil.trim(B.toByteArray());
      // clientHash = H(N) xor H(g) | H(U) | s | A
      clientHash.update(ab);
      if( log.isTraceEnabled() )
      {
         MessageDigest tmp = CryptoUtil.copy(clientHash);
         log.trace("H[H(N) xor H(g) | H(U) | s | A]: "+CryptoUtil.tob64(tmp.digest()));
      }
      // clientHash = H(N) xor H(g) | H(U) | A | B
      clientHash.update(nb);
      if( log.isTraceEnabled() )
      {
         MessageDigest tmp = CryptoUtil.copy(clientHash);
         log.trace("H[H(N) xor H(g) | H(U) | s | A | B]: "+CryptoUtil.tob64(tmp.digest()));
      }
      // serverHash = A
      serverHash.update(ab);
      // Calculate u as the first 32 bits of H(B)
      byte[] hB = CryptoUtil.newDigest().digest(nb);
      byte[] ub =
      {hB[0], hB[1], hB[2], hB[3]};
      // Calculate S = (A * v^u) ^ b % N
      BigInteger A = new BigInteger(1, ab);
      if( log.isTraceEnabled() )
         log.trace("A: "+CryptoUtil.tob64(A.toByteArray()));
      if( log.isTraceEnabled() )
         log.trace("B: "+CryptoUtil.tob64(B.toByteArray()));
      if( log.isTraceEnabled() )
         log.trace("v: "+CryptoUtil.tob64(v.toByteArray()));
      BigInteger u = new BigInteger(1, ub);
      if( log.isTraceEnabled() )
         log.trace("u: "+CryptoUtil.tob64(u.toByteArray()));
      BigInteger A_v2u = A.multiply(v.modPow(u, N)).mod(N);
      if( log.isTraceEnabled() )
         log.trace("A * v^u: "+CryptoUtil.tob64(A_v2u.toByteArray()));
      BigInteger S = A_v2u.modPow(b, N);
      if( log.isTraceEnabled() )
         log.trace("S: "+CryptoUtil.tob64(S.toByteArray()));
      // K = SessionHash(S)
      MessageDigest sessionDigest = MessageDigest.getInstance(params.hashAlgorithm);
      K = sessionDigest.digest(S.toByteArray());
      if( log.isTraceEnabled() )
         log.trace("K: "+CryptoUtil.tob64(K));
      // clientHash = H(N) xor H(g) | H(U) | A | B | K
      clientHash.update(K);
      if( log.isTraceEnabled() )
      {
         MessageDigest tmp = CryptoUtil.copy(clientHash);
         log.trace("H[H(N) xor H(g) | H(U) | s | A | B | K]: "+CryptoUtil.tob64(tmp.digest()));
      }
   }
   
   /** Returns the negotiated session K, K = SessionHash(S)
    @return the private session K byte[]
    @throws SecurityException - if the current thread does not have an
    getSessionKey SRPPermission.
    */
   public byte[] getSessionKey() throws SecurityException
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
      {
         SRPPermission p = new SRPPermission("getSessionKey");
         sm.checkPermission(p);
      }
      return K;
   }

   /**
    @returns M2 = H(A | M | K)
    */
   public byte[] getServerResponse()
   {
      if( M2 == null )
         M2 = serverHash.digest();
      return M2;
   }
   public byte[] getClientResponse()
   {
      return M1;
   }
   
   /**
    * @param resp The client's response to the server's challenge
    * @returns True if and only if the client's response was correct.
    */
   public boolean verify(byte[] clientM1)
   {
      boolean valid = false;
      // M1 = H(H(N) xor H(g) | H(U) | A | B | K)
      M1 = clientHash.digest();
      if( log.isTraceEnabled() )
      {
         log.trace("verify M1: "+CryptoUtil.tob64(M1));
         log.trace("verify clientM1: "+CryptoUtil.tob64(clientM1));
      }
      if( Arrays.equals(clientM1, M1) )
      {
         // serverHash = A | M
         serverHash.update(M1);
         // serverHash = A | M | K
         serverHash.update(K);
         if( log.isTraceEnabled() )
         {
            MessageDigest tmp = CryptoUtil.copy(serverHash);
            log.trace("H(A | M1 | K)"+CryptoUtil.tob64(tmp.digest()));
         }
         valid = true;
      }
      return valid;
   }
}
