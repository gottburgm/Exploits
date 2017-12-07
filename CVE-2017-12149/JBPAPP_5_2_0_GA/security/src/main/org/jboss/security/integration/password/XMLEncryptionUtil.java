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

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML Encryption Util
 * <b>Note: </b> This utility is currently using Apache XML Security
 * library API. JSR-106 is not yet final. Until that happens,we
 * rely on the non-standard API.
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Feb 4, 2009
 */
public class XMLEncryptionUtil
{ 
   public static final String CIPHER_DATA_LOCALNAME = "CipherData";
   public static final String ENCRYPTED_DATA_LOCALNAME = "EncryptedData";
   public static final String ENCRYPTED_KEY_LOCALNAME = "EncryptedKey";
   public static final String DS_KEY_INFO = "ds:KeyInfo";
   
   public static final String XMLNS = "http://www.w3.org/2000/xmlns/"; 
   public static String XMLSIG_NS = "http://www.w3.org/2000/09/xmldsig#";
   public static String XMLENC_NS = "http://www.w3.org/2001/04/xmlenc#";
   
   private static Map<String,EncryptionAlgorithm> algorithms;
   
   private static class EncryptionAlgorithm
   {
      EncryptionAlgorithm(String jceName, String xmlSecName, int size)
      {
         this.jceName = jceName;
         this.xmlSecName = xmlSecName;
         this.size = size;
      }

      public String jceName;
      public String xmlSecName;
      public int size;
   }
   
   static
   {
      algorithms = new HashMap<String, EncryptionAlgorithm>(4);
      algorithms.put("aes-128", new EncryptionAlgorithm("AES", XMLCipher.AES_128, 128));
      algorithms.put("aes-192", new EncryptionAlgorithm("AES", XMLCipher.AES_192, 192));
      algorithms.put("aes-256", new EncryptionAlgorithm("AES", XMLCipher.AES_256, 256));
      algorithms.put("tripledes", new EncryptionAlgorithm("TripleDes", XMLCipher.TRIPLEDES, 168));
    
      //Initialize the Apache XML Security Library
      org.apache.xml.security.Init.init();
   }
   
   /**
    * <p>
    * Encrypt the Key to be transported
    * </p>
    * <p>
    * Data is encrypted with a SecretKey. Then the key needs to be
    * transported to the other end where it is needed for decryption.
    * For the Key transport, the SecretKey is encrypted with the
    * recipient's public key. At the receiving end, the receiver
    * can decrypt the Secret Key using his private key.s
    * </p>
    * @param document
    * @param keyToBeEncrypted Symmetric Key (SecretKey)
    * @param keyUsedToEncryptSecretKey Asymmetric Key (Public Key)
    * @param keySize Length of the key
    * @return
    * @throws Exception
    */
   public static EncryptedKey encryptKey(Document document,
         SecretKey keyToBeEncrypted, PublicKey keyUsedToEncryptSecretKey,
         int keySize) throws Exception
   {
      if(keyToBeEncrypted == null)
         throw new IllegalArgumentException("secret key is null");
      
      XMLCipher keyCipher = null;
      String pubKeyAlg = keyUsedToEncryptSecretKey.getAlgorithm();
      
      String keyWrapAlgo = getXMLEncryptionURLForKeyUnwrap(pubKeyAlg, keySize);
      keyCipher = XMLCipher.getInstance(keyWrapAlgo);
         
      keyCipher.init(XMLCipher.WRAP_MODE, keyUsedToEncryptSecretKey);
      return keyCipher.encryptKey(document, keyToBeEncrypted); 
   }
    
   /**
    * Encrypt a document at the root (Use aes-128)
    * @param document
    * @param secretKey
    * @param publicKey
    * @param keySize
    * @return
    * @throws Exception
    */
   public static Document encrypt(Document document, SecretKey secretKey, PublicKey publicKey, int keySize)
   throws Exception
   {
      //Encrypt
      XMLCipher cipher = XMLCipher.getInstance(algorithms.get("aes-128").xmlSecName);
      cipher.init(XMLCipher.ENCRYPT_MODE, secretKey);

      //Encrypted Key
      EncryptedKey ekey = XMLEncryptionUtil.encryptKey(document, secretKey, publicKey, keySize);
      //Encrypted Data
      String encryptionAlgorithm = XMLEncryptionUtil.getXMLEncryptionURL(secretKey.getAlgorithm(), keySize);
      //Encrypt the Document 
      cipher = XMLCipher.getInstance(encryptionAlgorithm);
      cipher.init(XMLCipher.ENCRYPT_MODE, secretKey);

      Document encryptedDoc =  cipher.doFinal(document, document.getDocumentElement()); 
      Element encryptedDocRootElement = encryptedDoc.getDocumentElement(); 
      // The EncryptedKey element is added
      Element encryptedKeyElement =  cipher.martial(document, ekey); 

      // Outer ds:KeyInfo Element to hold the EncryptionKey
      Element sigElement = encryptedDoc.createElementNS(XMLSIG_NS, DS_KEY_INFO);
      sigElement.setAttributeNS(XMLNS, "xmlns:ds",  XMLSIG_NS);
      sigElement.appendChild(encryptedKeyElement);

      //Insert the Encrypted key before the CipherData element 
      NodeList nodeList = encryptedDocRootElement.getElementsByTagNameNS(XMLENC_NS, CIPHER_DATA_LOCALNAME);
      if (nodeList == null || nodeList.getLength() == 0)  
         throw new IllegalStateException("xenc:CipherData Element Missing"); 

      Element cipherDataElement = (Element) nodeList.item(0); 
      encryptedDocRootElement.insertBefore(sigElement, cipherDataElement);
      return encryptedDoc;
   }

   /**
    * Decrypt a document 
    * @param encryptedDoc
    * @param privateKey
    * @return
    * @throws Exception
    */
   public static Document decrypt(Document encryptedDoc, PrivateKey privateKey) throws Exception
   {
      //First look for enc data
      Element docRoot = encryptedDoc.getDocumentElement();
      Node dataEL = null;
      Node keyEL = null;
       
      if(XMLENC_NS.equals(docRoot.getNamespaceURI()) 
            && ENCRYPTED_DATA_LOCALNAME.equals(docRoot.getLocalName())) 
      {
         //we found it 
         dataEL = docRoot;
      }
      else
      {
         NodeList childs = docRoot.getElementsByTagNameNS(XMLENC_NS, ENCRYPTED_DATA_LOCALNAME);
         if(childs == null || childs.getLength() == 0) 
            throw new IllegalStateException("Encrypted Data not found"); 
         dataEL = childs.item(0);
      }
      
      NodeList keyList = ((Element)dataEL).getElementsByTagNameNS(XMLENC_NS, ENCRYPTED_KEY_LOCALNAME);
      if(keyList == null || keyList.getLength() == 0) 
         throw new IllegalStateException("Encrypted Key not found");
      keyEL = keyList.item(0);
       
      if(dataEL == null)
         throw new IllegalStateException("Encrypted Data not found");
      if(keyEL == null)
         throw new IllegalStateException("Encrypted Key not found");
      
      XMLCipher cipher =  XMLCipher.getInstance(); 
      cipher.init(XMLCipher.DECRYPT_MODE, null); 
      EncryptedData encryptedData =  cipher.loadEncryptedData(encryptedDoc, (Element)dataEL);  
      EncryptedKey encryptedKey =  cipher.loadEncryptedKey(encryptedDoc, (Element)keyEL);
      
      Document decryptedDoc = null;
      
      if (encryptedData != null && encryptedKey != null) 
      {
         String encAlgoURL = encryptedData.getEncryptionMethod().getAlgorithm();
         XMLCipher keyCipher =  XMLCipher.getInstance(); 
         keyCipher.init(XMLCipher.UNWRAP_MODE, privateKey); 
         Key encryptionKey =  keyCipher.decryptKey( encryptedKey, encAlgoURL ); 
         cipher =  XMLCipher.getInstance();  
         cipher.init(XMLCipher.DECRYPT_MODE, encryptionKey); 
         decryptedDoc = cipher.doFinal(encryptedDoc, (Element)dataEL); 
      }
      return decryptedDoc;
   }
   
   /**
    * From the secret key, get the W3C XML Encryption URL
    * @param publicKeyAlgo
    * @param keySize
    * @return
    */
   private static String getXMLEncryptionURLForKeyUnwrap(String publicKeyAlgo, int keySize)
   {
      if("AES".equals(publicKeyAlgo))
      {
         switch(keySize)
         {
            case 192: return XMLCipher.AES_192_KeyWrap;
            case 256: return XMLCipher.AES_256_KeyWrap;
            default:
                      return XMLCipher.AES_128_KeyWrap;
         }
      }
      if(publicKeyAlgo.contains("RSA"))
         return XMLCipher.RSA_v1dot5;
      if(publicKeyAlgo.contains("DES"))
         return XMLCipher.TRIPLEDES_KeyWrap; 
      throw new IllegalArgumentException("unsupported publicKey Algo:" + publicKeyAlgo);
   }
   
   /**
    * From the secret key, get the W3C XML Encryption URL
    * @param secretKey
    * @param keySize
    * @return
    */
   public static String getXMLEncryptionURL(String algo, int keySize)
   { 
      if("AES".equals(algo))
      {
         switch(keySize)
         {
            case 192: return XMLCipher.AES_192;
            case 256: return XMLCipher.AES_256;
            default:
                      return XMLCipher.AES_128;
         }
      }
      if(algo.contains("RSA"))
         return XMLCipher.RSA_v1dot5;
      if(algo.contains("DES"))
         return XMLCipher.TRIPLEDES_KeyWrap; 
      throw new IllegalArgumentException("Secret Key with unsupported algo:" + algo);
   } 
}