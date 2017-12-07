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
package org.jboss.security.integration.password;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

/**
 * Utility to handle Java Keystore
 * @author Anil.Saldhana@redhat.com
 * @since Jan 12, 2009
 */
public class KeyStoreUtil
{ 
   /**
    * Create a Keystore
    * @param storePass
    * @throws Exception
    */
   public static void createKeyStore(String path, char[] storePass) throws Exception
   {
      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
      ks.load(null, storePass); //creates an empty keystore 
      
      ks.store(new FileOutputStream(new File(path)), storePass);
      System.out.println("Keystore created");
   }
   
   /**
    * Get the Keystore given the url to the keystore file as a string
    * @param fileURL
    * @param storePass 
    * @return
    * @throws GeneralSecurityException
    * @throws IOException
    */
   public static KeyStore getKeyStore(String fileURL, char[] storePass) throws GeneralSecurityException, IOException
   {
      if(fileURL == null)
         throw new IllegalArgumentException("fileURL is null");
      
      File file = new File(fileURL);
      FileInputStream fis = new FileInputStream(file);
      return getKeyStore(fis,storePass);
   }
   
   /**
    * Get the Keystore given the URL to the keystore
    * @param url
    * @param storePass
    * @return
    * @throws GeneralSecurityException
    * @throws IOException
    */
   public static KeyStore getKeyStore(URL url, char[] storePass) throws GeneralSecurityException, IOException
   {
      if(url == null)
         throw new IllegalArgumentException("url is null");
      
      return getKeyStore(url.openStream(), storePass);
   }
   
   /**
    * Get the Key Store
    * <b>Note:</b> This method wants the InputStream to be not null. 
    * @param ksStream
    * @param storePass
    * @return
    * @throws GeneralSecurityException
    * @throws IOException
    * @throws IllegalArgumentException if ksStream is null
    */
   public static KeyStore getKeyStore(InputStream ksStream, char[] storePass) throws GeneralSecurityException, IOException
   {
      if(ksStream == null)
         throw new IllegalArgumentException("InputStream for the KeyStore is null");
      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
      ks.load(ksStream, storePass);
      return ks;
   }
   
   /**
    * Generate a Key Pair
    * @param algo (RSA, DSA etc)
    * @return
    * @throws Exception
    */
   public static KeyPair generateKeyPair(String algo) throws Exception
   {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(algo);
      return kpg.genKeyPair();
   }
   
   /**
    * Get the Public Key from the keystore
    * @param ks
    * @param alias
    * @param password
    * @return
    * @throws Exception
    */
   public static PublicKey getPublicKey(KeyStore ks, String alias, char[] password) throws Exception
   {
      PublicKey publicKey = null;
      
      // Get private key
      Key key = ks.getKey(alias, password);
      if (key instanceof PrivateKey) 
      {
         // Get certificate of public key
         Certificate cert = ks.getCertificate(alias);

         // Get public key
         publicKey = cert.getPublicKey();
      }
      
      return publicKey;      
   }
   
   public static void storeKeyPair(KeyStore ks, String alias,
         PublicKey publicKey, PrivateKey privateKey, char[] pass)
   throws Exception
   {
      ks.setKeyEntry(alias, privateKey, pass, null);  
   }
}