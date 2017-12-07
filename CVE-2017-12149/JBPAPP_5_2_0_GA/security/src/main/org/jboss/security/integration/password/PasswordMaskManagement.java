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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.jboss.security.plugins.FilePassword;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Manages masking the password for xml configuration files
 * @author Anil.Saldhana@redhat.com
 * @since Mar 26, 2009
 */
public class PasswordMaskManagement
{
   private Logger log = Logger.getLogger(PasswordMaskManagement.class);
   
   private Map<String,char[]> passwordMap = new HashMap<String,char[]>();
   private KeyStore keystore;
   
   private String alias = "jboss";
   
   private String passwordEncryptedFileName = "password/jboss_password_enc.dat";
   
   static String keystorePassEncFileName = "password/jboss_keystore_pass.dat";
   
   private String keystoreLocation = "password/password.keystore";
   
   KeyPair kp = null;
   private char[] storePass;
   
   
   public PasswordMaskManagement()
   {    
   }
   
   //Public Methods
   public void setKeyStoreDetails(String location, String alias) throws Exception
   {
      if(location == null)
         throw new IllegalArgumentException("location is null");
      this.keystoreLocation = location;
      this.alias = alias;
      this.ensureKeyStore();
   }
   public void setKeyStoreDetails(String location, char[] storePass, String alias) throws Exception
   {
      if(location == null)
         throw new IllegalArgumentException("location is null");
      this.keystore = KeyStoreUtil.getKeyStore(location, storePass);
      this.storePass = storePass;
      this.alias = alias;
      load();
   }  
   
   //Public property setters
   public void setKeyStoreLocation(String location)
   {
      if(location == null)
         throw new IllegalArgumentException("location is null");
      this.keystoreLocation = location;       
   }
   
   public void setKeyStoreAlias(String alias)
   {
      if(alias == null)
         throw new IllegalArgumentException("alias is null");
      this.alias = alias;
   }
   
   /**
    * Customize the location where the encrypted
    * password file needs to be stored
    * @param pefn
    */
   public void setPasswordEncryptedFileName(String pefn)
   {
      this.passwordEncryptedFileName = pefn;
   }
 
   /**
    * Customize the location where the encrypted
    * keystore password file is stored
    * @param kpe
    */
   public void setKeyStorePasswordEncryptedFileName(String kpe)
   {
      keystorePassEncFileName = kpe;
   } 
   
   //Package protected Methods
 
   /**
    * Whether a security domain exists
    * in the password map
    * @param securityDomain
    * @return
    */
   boolean exists(String securityDomain)
   {
      return this.passwordMap.containsKey(securityDomain);
   }
   
   /**
    * Check whether the keystore exists
    * @return
    */
   boolean keyStoreExists()
   {
      return this.keystore != null;
   }
   
   /**
    * Get the password
    * @param securityDomain
    * @return
    * @throws Exception
    */
   char[] getPassword(String securityDomain) throws Exception
   {
      if(keystore == null)
      {           
         if(this.storePass == null)
            this.ensureKeyStore();
         if(passwordMap.size() == 0)
            load(); 
      }
      
      return passwordMap.get(securityDomain);
   }
    
   void storePassword(String securityDomain, char[] pass)
   {
      this.passwordMap.put(securityDomain, pass);
   }

   void removePassword(String domainToRemove)
   {
      this.passwordMap.remove(domainToRemove);
   }
   
   void load() throws Exception
   {
      Document doc = loadPasswordEncryptedDocument(); 
      if(doc == null)
      {
         log.trace(this.passwordEncryptedFileName + " does not exist");
         return;
      }
      if(keystore == null)
      {
         System.out.println("Keystore is null. Please specify keystore below:");
         return;
      } 
      
      PrivateKey privateKey = (PrivateKey) keystore.getKey(this.alias, this.storePass);
      
      if(privateKey == null)
         throw new IllegalStateException("private key not found");
      
      Document decryptedDoc = XMLEncryptionUtil.decrypt(doc, privateKey);
      
      NodeList nl = decryptedDoc.getDocumentElement().getElementsByTagName("entry");
      int len = nl != null ? nl.getLength() : 0;
      
      System.out.println("Loading domains [");
      for(int i = 0; i < len; i++)
      {
         Element n = (Element) nl.item(i);
         String name = n.getAttribute("name");
         System.out.println(name + ",");
         this.passwordMap.put(name, n.getAttribute("pass").toCharArray());
      }

      System.out.println("]"); 
   }
   
   void store() throws Exception
   {
      if(this.keystore == null)
      {
         System.out.println("Keystore is null. Cannot store.");
         return; 
      }      
      StringBuilder builder = new StringBuilder();
      
      Document doc = DocumentUtil.createDocument();
      Element el = doc.createElementNS(null, "pass-map");
      doc.appendChild(el);
      
      System.out.println("Storing domains [");
      Set<Entry<String,char[]>> entries = this.passwordMap.entrySet();
      for(Entry<String,char[]> e: entries)
      {
         Element entry = doc.createElementNS(null, "entry"); 
         
         System.out.println(e.getKey()+",");
         entry.setAttributeNS(null, "name", e.getKey());
         entry.setAttributeNS(null, "pass", new String(e.getValue())); 
        
         el.appendChild(entry);
      }
      builder.append("</pass-map>");
      
      System.out.println("]");
       
      SecretKey skey = this.getSecretKey("AES", 128);
      
      PublicKey pk = KeyStoreUtil.getPublicKey(keystore, alias, storePass); 
      if(pk == null)
         throw new RuntimeException("public key is null");
      XMLEncryptionUtil.encrypt(doc, skey, pk, 128); 
      
      storePasswordEncryptedDocument(doc); 
   }
   
   void ensurePasswordFile() throws Exception
   {
      try
      {
         this.loadPasswordEncryptedDocument();  
      }
      catch(FileNotFoundException e)
      {
         //Just create the file
         File file = new File(passwordEncryptedFileName);
         if(file.exists() == false)
            file.createNewFile(); 
      }
   }
   
   void ensureKeyStore() throws Exception
   {
      if(keystore == null)
      {
         if(keystoreLocation == null)
            throw new IllegalStateException("KeyStore Location is null");

         ClassLoader tcl = SecurityActions.getContextClassLoader();
         
         /**
          * Look for the encrypted keystore pass file
          * via the direct File approach or via the context classloader
          */
         URL keyEncFileURL = null;
         File keyfile = new File(keystorePassEncFileName);
         if(keyfile.exists() == false)
         {
            keyEncFileURL = tcl.getResource(keystorePassEncFileName);
         }
         else
            keyEncFileURL = keyfile.toURL();
         
         //Get the keystore passwd
         FilePassword fp = null;
         try
         { 
            fp = new FilePassword(keyEncFileURL.toString());  
            this.storePass = fp.toCharArray();
         } 
         catch(IOException eof)
         {
            throw new IllegalStateException("The Keystore Encrypted file not located:",eof); 
         }
         
         if(this.storePass == null)
            throw new IllegalStateException("Keystore password is null");
         
         /**
          * We look for the keystore in either direct File access or
          * via the context class loader
          */
         URL ksFileURL = null;
         File ksFile = new File(keystoreLocation);
         if(ksFile.exists() == false)
            ksFileURL = tcl.getResource(keystoreLocation);
         else
            ksFileURL = ksFile.toURL();
         this.keystore = KeyStoreUtil.getKeyStore(ksFileURL, storePass); 
      }
   }
   
   /**
    * Generate a secret key useful for encryption/decryption
    * @param encAlgo
    * @param keySize Length of the key  (if 0, defaults to 128 bits)
    * @return
    * @throws Exception
    */
   private  SecretKey getSecretKey(String encAlgo, int keySize) throws Exception
   { 
      KeyGenerator keyGenerator = KeyGenerator.getInstance(encAlgo);
      if(keySize == 0)
         keySize = 128;
      keyGenerator.init(keySize);
      return keyGenerator.generateKey();
   }
   
   private Document loadPasswordEncryptedDocument() throws Exception
   {
      Document doc = null;
      File docFile = new File(this.passwordEncryptedFileName);
      
      if(docFile == null || docFile.exists() == false)
      {
         //Try the TCL
         ClassLoader tcl = SecurityActions.getContextClassLoader();
         InputStream is = tcl.getResourceAsStream(passwordEncryptedFileName);
         if(is == null)
            throw new FileNotFoundException("Encrypted password file not located");
         doc = DocumentUtil.getDocument(is);
      }
      else
      {
         doc = DocumentUtil.getDocument(docFile);
      }
      return doc; 
   }
   
   private void storePasswordEncryptedDocument(Document doc) throws Exception
   {
      byte[] data = DocumentUtil.getDocumentAsString(doc).getBytes();
      FileOutputStream faos = null;
      
      //Try the url route
      try
      {
         URL url = new URL(this.passwordEncryptedFileName);
         File file = new File(url.toString());
         faos = new FileOutputStream(file);
         faos.write(data);
         faos.flush();
         faos.close(); 
      }
      catch(Exception e)
      {
         if(faos == null)
            faos = new FileOutputStream(new File(passwordEncryptedFileName));
      } 
      finally
      {
         if(faos == null)
            throw new RuntimeException("File Output Stream is null"); 
         faos.write(data);
         faos.flush();
         faos.close();
      }  
   }
}