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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.crypto.CryptoUtil;
import org.jboss.security.srp.SRPConf;
import org.jboss.security.srp.SRPVerifierStore;
import org.jboss.security.srp.SRPVerifierStore.VerifierInfo;

/** A simple implementation of the SRPVerifierStore that uses a
file store made up of VerifierInfo serialized objects. Users and
be added or removed using the addUser and delUser methods. User passwords
are never stored in plaintext either in memory or in the serialized file.
Note that usernames and passwords are logged when a user is added
via the addUser operation. This is a development class and its use in
a production environment is not advised.

@see #addUser(String, String)
@see #delUser(String)

@author Scott.Stark@jboss.org
@version $Revision: 81038 $
*/
public class SerialObjectStore implements SRPVerifierStore
{
    private static Logger log = Logger.getLogger(SerialObjectStore.class);
    private Map infoMap;
    private BigInteger g;
    private BigInteger N;

   /** Create an in memory store and load any VerifierInfo found in
        ./SerialObjectStore.ser if it exists.
    */
    public SerialObjectStore() throws IOException
    {
        this(null);
    }
    /** Create an in memory store and load any VerifierInfo found in
        the storeFile archive if it exists.
    */
    public SerialObjectStore(File storeFile) throws IOException
    {
        if( storeFile == null )
            storeFile = new File("SerialObjectStore.ser");
        if( storeFile.exists() == true )
        {
            FileInputStream fis = new FileInputStream(storeFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            try
            {
                infoMap = (Map) ois.readObject();
            }
            catch(ClassNotFoundException e)
            {
            }
            ois.close();
            fis.close();
        }
        else
        {
            infoMap = Collections.synchronizedMap(new HashMap());
        }

        try
        {
           CryptoUtil.init();
        }
        catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            throw new IOException("Failed to initialzed security utils: "+e.getMessage());
        }
        N = SRPConf.getDefaultParams().N();
        g = SRPConf.getDefaultParams().g();
        log.trace("N: "+CryptoUtil.tob64(N.toByteArray()));
        log.trace("g: "+CryptoUtil.tob64(g.toByteArray()));
        byte[] hn = CryptoUtil.newDigest().digest(N.toByteArray());
        log.trace("H(N): "+CryptoUtil.tob64(hn));
        byte[] hg = CryptoUtil.newDigest().digest(g.toByteArray());
        log.trace("H(g): "+CryptoUtil.tob64(hg));
    }

// --- Begin SRPVerifierStore interface methods
    public VerifierInfo getUserVerifier(String username) throws KeyException, IOException
    {
        VerifierInfo info = null;
        if( infoMap != null )
            info = (VerifierInfo) infoMap.get(username);
        if( info == null )
            throw new KeyException("username: "+username+" not found");
        return info;
    }
    public void setUserVerifier(String username, VerifierInfo info)
    {
        infoMap.put(username, info);
    }

   public void verifyUserChallenge(String username, Object auxChallenge)
         throws SecurityException
   {
      throw new SecurityException("verifyUserChallenge not supported");
   }
// --- End SRPVerifierStore interface methods

    /** Save the current in memory map of VerifierInfo to the indicated
        storeFile by simply serializing the map to the file.
    */
    public void save(File storeFile) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(storeFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        synchronized( infoMap )
        {
            oos.writeObject(infoMap);
        }
        oos.close();
        fos.close();
    }

    public void addUser(String username, String password)
    {
        log.trace("addUser, username='"+username+"', password='"+password+"'");
        VerifierInfo info = new VerifierInfo();
        info.username = username;
        /*
        long r = Util.nextLong();
        String rs = Long.toHexString(r);
         */
        String rs = "123456";
        info.salt = rs.getBytes();
        try
        {
           char[] pass = password.toCharArray();
           info.verifier = CryptoUtil.calculateVerifier(username, pass,
               info.salt, N, g);
           info.g = g.toByteArray();
           info.N = N.toByteArray();
           if( log.isTraceEnabled() )
           {
               log.trace("N: "+CryptoUtil.tob64(info.N));
               log.trace("g: "+CryptoUtil.tob64(info.g));
               log.trace("s: "+CryptoUtil.tob64(info.salt));
               byte[] xb = CryptoUtil.calculatePasswordHash(username, pass, info.salt);
               log.trace("x: "+CryptoUtil.tob64(xb));
               log.trace("v: "+CryptoUtil.tob64(info.verifier));
               byte[] hn = CryptoUtil.newDigest().digest(info.N);
               log.trace("H(N): "+CryptoUtil.tob64(hn));
               byte[] hg = CryptoUtil.newDigest().digest(info.g);
               log.trace("H(g): "+CryptoUtil.tob64(hg));
           }
        }
        catch(Throwable t)
        {
           log.error("Failed to calculate verifier", t);
           return;
        }

        setUserVerifier(username, info);
    }
    public void delUser(String username)
    {
        infoMap.remove(username);
    }

    public static void main(String[] args) throws IOException
    {
        File storeFile = new File("SerialObjectStore.ser");
        SerialObjectStore store = new SerialObjectStore();

        for(int a = 0; a < args.length; a ++)
        {
            if( args[a].startsWith("-a") )
            {
                store.addUser(args[a+1], args[a+2]);
            }
            else if( args[a].startsWith("-d") )
            {
                store.delUser(args[a+1]);
            }
        }
        store.save(storeFile);
    }
}
