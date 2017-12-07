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
package org.jboss.security.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.security.auth.callback.CallbackHandler;

import org.jboss.crypto.CryptoUtil;
import org.jboss.managed.api.ManagedOperation.Impact;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementParameter;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.SecurityDomain;
import org.jboss.security.Util;
import org.jboss.security.auth.callback.JBossCallbackHandler; 
import org.jboss.security.integration.JNDIBasedSecurityManagement;
import org.jboss.security.integration.SecurityConstantsBridge;

/**
 * The JaasSecurityDomain is an extension of JaasSecurityManager that addes the notion of a KeyStore, and JSSE
 * KeyManagerFactory and TrustManagerFactory for supporting SSL and other cryptographic use cases.
 * 
 * Attributes:
 * <ul>
 * <li>KeyStoreType: The implementation type name being used, defaults to 'JKS'. </li>
 * 
 * <li>KeyStoreURL: Set the KeyStore database URL string. This is used to obtain an InputStream to initialize the
 * KeyStore. If the string is not a value URL, its treated as a file. </li>
 * 
 * <li>KeyStorePass: the password used to load the KeyStore. Its format is one of:
 * <ul>
 * <li>The plaintext password for the KeyStore(or whatever format is used by the KeyStore). The toCharArray() value of
 * the string is used without any manipulation. </li>
 * <li>A command to execute to obtain the plaintext password. The format is '{EXT}...' where the '...' is the exact
 * command line that will be passed to the Runtime.exec(String) method to execute a platform command. The first line of
 * the command output is used as the password. </li>
 * <li>A class to create to obtain the plaintext password. The format is '{CLASS}classname[:ctorarg]' where the
 * '[:ctorarg]' is an optional string delimited by the ':' from the classname that will be passed to the classname ctor.
 * The password is obtained from classname by invoking a 'char[] toCharArray()' method if found, otherwise, the 'String
 * toString()' method is used. </li>
 * </ul>
 * The KeyStorePass is also used in combination with the Salt and IterationCount attributes to create a PBE secret key
 * used with the encode/decode operations. </li>
 * 
 * <li>ManagerServiceName: The JMX object name string of the security manager service that the domain registers with to
 * function as a security manager for the security domain name passed to the ctor. The makes the JaasSecurityDomain
 * available under the standard JNDI java:/jaas/(domain) binding. </li>
 * 
 * <li>LoadSunJSSEProvider: A flag indicating if the Sun com.sun.net.ssl.internal.ssl.Provider security provider should
 * be loaded on startup. This is needed when using the Sun JSSE jars without them installed as an extension with JDK
 * 1.3. This should be set to false with JDK 1.4 or when using an alternate JSSE provider </li>
 * 
 * <li>Salt: </li>
 * 
 * <li>IterationCount: </li>
 * </ul>
 * 
 * @todo add support for encode/decode based on a SecretKey in the keystore.
 * 
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:jasone@greenrivercomputing.com">Jason Essington</a>
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 * @author <a href="mailto:mmoyses@redhat.com">Marcus Moyses</a>
 * 
 * @version $Revision: 110302 $
 */
@ManagementObject(componentType = @ManagementComponent(type = "MCBean", subtype = "Security"), properties = ManagementProperties.EXPLICIT)
public class JaasSecurityDomain extends JaasSecurityManager implements SecurityDomain, JaasSecurityDomainMBean
{
   /** The permission required to access encode, encode64 */
   private static final RuntimePermission encodePermission = new RuntimePermission(
         "org.jboss.security.plugins.JaasSecurityDomain.encode");

   /** The permission required to access decode, decode64 */
   private static final RuntimePermission decodePermission = new RuntimePermission(
         "org.jboss.security.plugins.JaasSecurityDomain.decode");

   /** The KeyStore associated with the security domain */
   private KeyStore keyStore;

   private KeyManagerFactory keyMgr;
   
   private KeyManager[] keyManagers;

   /** The KeyStore implementation type which defaults to 'JKS' */
   private String keyStoreType = "JKS";

   /** The resource for the keystore location */
   private URL keyStoreURL;

   /** The keystore password for loading */
   private char[] keyStorePassword;
   
   /** The alias of the KeyStore to be used */
   private String keyStoreAlias;
   
   /** The secret key that corresponds to the keystore password */
   private SecretKey cipherKey;

   /** The encode/decode cipher algorigthm */
   private String cipherAlgorithm = "PBEwithMD5andDES";

   private byte[] salt = {1, 2, 3, 4, 5, 6, 7, 8};

   private int iterationCount = 103;

   private PBEParameterSpec cipherSpec;

   /** The JMX object name of the security manager service */
   private ObjectName managerServiceName = JaasSecurityManagerServiceMBean.OBJECT_NAME;

   private KeyStore trustStore;

   private String trustStoreType = "JKS";

   private char[] trustStorePassword;

   private URL trustStoreURL;

   private TrustManagerFactory trustMgr;
   
   private String keyStoreProvider;
   
   private String trustStoreProvider;
   
   private String keyMgrFactoryProvider;
   
   private String trustMgrFactoryProvider;
   
   private String keyMgrFactoryAlgorithm;
   
   private String trustMgrFactoryAlgorithm;
   
   private String keyStoreProviderArgument;
   
   private String trustStoreProviderArgument;
   
   private String clientAlias;
   
   private Properties additionalOptions;
   
   private boolean clientAuth;

   /** Specify the SecurityManagement instance */
   private ISecurityManagement securityManagement = SecurityConstantsBridge.getSecurityManagement();
   
   private char[] serviceAuthToken;

   /**
    * Creates a default JaasSecurityDomain for with a securityDomain name of 'other'.
    */
   public JaasSecurityDomain()
   {
      super();
   }

   /**
    * Creates a JaasSecurityDomain for with a securityDomain name of that given by the 'securityDomain' argument.
    * 
    * @param securityDomain , the name of the security domain
    */
   public JaasSecurityDomain(String securityDomain)
   {
      this(securityDomain, new JBossCallbackHandler());
   }

   /**
    * Creates a JaasSecurityDomain for with a securityDomain name of that given by the 'securityDomain' argument.
    * 
    * @param securityDomain , the name of the security domain
    * @param handler , the CallbackHandler to use to obtain login module info
    */
   public JaasSecurityDomain(String securityDomain, CallbackHandler handler)
   {
      super(securityDomain, handler);
   }

   @Override
   @ManagementObjectID(type = "SecurityDomain")
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The security domain name")
   public String getSecurityDomain()
   {
      return super.getSecurityDomain();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getKeyStoreType()
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The keystore implementation type - default is JKS")
   public String getKeyStoreType()
   {
      return this.keyStoreType;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setKeyStoreType(java.lang.String)
    */
   public void setKeyStoreType(String type)
   {
      this.keyStoreType = type;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getKeyStoreURL()
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The keystore location")
   public String getKeyStoreURL()
   {
      String url = null;
      if (keyStoreURL != null)
         url = keyStoreURL.toExternalForm();
      return url;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setKeyStoreURL(java.lang.String)
    */
   public void setKeyStoreURL(String storeURL) throws IOException
   {
      this.keyStoreURL = this.validateStoreURL(storeURL);
      log.debug("Using KeyStore=" + keyStoreURL.toExternalForm());
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setKeyStorePass(java.lang.String)
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The keystore password", mandatory = true)
   public void setKeyStorePass(String password) throws Exception
   {
      this.keyStorePassword = Util.loadPassword(password);
   }
   
   /*
    * (non-Javadoc)
    *
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setServiceAuthToken(java.lang.String)
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The service authentication token", mandatory = false)
   public void setServiceAuthToken(String serviceAuthToken) throws Exception
   {
      this.serviceAuthToken = Util.loadPassword(serviceAuthToken);
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getKeyStoreAlias()
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The keystore alias with the certificate to be used")
   public String getKeyStoreAlias()
   {
      return this.keyStoreAlias;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setKeyStoreAlias(java.lang.String)
    */
   public void setKeyStoreAlias(String alias)
   {
      this.keyStoreAlias = alias;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getTrustStoreType()
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The truststore implementation type - default is JKS")
   public String getTrustStoreType()
   {
      return this.trustStoreType;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setTrustStoreType(java.lang.String)
    */
   public void setTrustStoreType(String type)
   {
      this.trustStoreType = type;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getTrustStoreURL()
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The truststore location")
   public String getTrustStoreURL()
   {
      String url = null;
      if (trustStoreURL != null)
         url = trustStoreURL.toExternalForm();
      return url;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setTrustStoreURL(java.lang.String)
    */
   public void setTrustStoreURL(String storeURL) throws IOException
   {
      this.trustStoreURL = validateStoreURL(storeURL);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setTrustStorePass(java.lang.String)
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The truststore password")
   public void setTrustStorePass(String password) throws Exception
   {
      this.trustStorePassword = Util.loadPassword(password);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setSalt(java.lang.String)
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The salt for password-based encryption (PBE)")
   public void setSalt(String salt)
   {
      this.salt = salt.getBytes();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setIterationCount(int)
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The iteration count for password-based encryption (PBE)")
   public void setIterationCount(int iterationCount)
   {
      this.iterationCount = iterationCount;
   }

   /**
    * <p>
    * Obtains the cypher algorithm used in then encode and decode operations.
    * </p>
    * 
    * @return a {@code String} representing the name of the cipher algorithm.
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The cipher algorithm used in the encode/decode operations - default is PBEwithMD5andDES")
   public String getCipherAlgorithm()
   {
      return cipherAlgorithm;
   }

   /**
    * <p>
    * Sets the cipher algorithm to be used in the encode and decode operations.
    * </p>
    * 
    * @param cipherAlgorithm a {@code String} representing the name of the cipher algorithm.
    */
   public void setCipherAlgorithm(String cipherAlgorithm)
   {
      this.cipherAlgorithm = cipherAlgorithm;
   }

   /**
    * The JMX object name string of the security manager service.
    * 
    * @return The JMX object name string of the security manager service.
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The object name of the security manager service")
   public ObjectName getManagerServiceName()
   {
      return this.managerServiceName;
   }

   /**
    * Set the JMX object name string of the security manager service.
    */
   public void setManagerServiceName(ObjectName managerServiceName)
   {
      this.managerServiceName = managerServiceName;
   }

   /**
    * <p>
    * Obtains a reference to the {@code ISecurityManagement} implementation that registered this domain.
    * </p>
    * 
    * @return a reference to the {@code ISecurityManagement} bean.
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The security manager service bean where this domain is registered")
   public ISecurityManagement getSecurityManagement()
   {
      return securityManagement;
   }

   /**
    * <p>
    * Sets the {@code ISecurityManagement} implementation that must be used to register this domain.
    * </p>
    * 
    * @param securityManagement a reference to the {@code ISecurityManagement} be to be used.
    */
   public void setSecurityManagement(ISecurityManagement securityManagement)
   {
      this.securityManagement = securityManagement;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.system.ServiceMBeanSupport#getName()
    */
   @Override
   public String getName()
   {
      return "JaasSecurityDomain(" + getSecurityDomain() + ")";
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.SecurityDomain#getKeyStore()
    */
   @ManagementOperation(description = "Get the KeyStore constructed by this domain", impact = Impact.ReadOnly)
   public KeyStore getKeyStore() throws SecurityException
   {
      return keyStore;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.SecurityDomain#getKeyManagerFactory()
    */
   @ManagementOperation(description = "Get the KeyManagerFactory constructed by this domain", impact = Impact.ReadOnly)
   public KeyManagerFactory getKeyManagerFactory() throws SecurityException
   {
      return keyMgr;
   }
   
   public KeyManager[] getKeyManagers()
   {
      return keyManagers;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.SecurityDomain#getTrustStore()
    */
   @ManagementOperation(description = "Get the TrustStore constructed by this domain", impact = Impact.ReadOnly)
   public KeyStore getTrustStore() throws SecurityException
   {
      return trustStore;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.SecurityDomain#getTrustManagerFactory()
    */
   @ManagementOperation(description = "Get the TrustManagerFactory constructed by this domain", impact = Impact.ReadOnly)
   public TrustManagerFactory getTrustManagerFactory() throws SecurityException
   {
      return trustMgr;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#encode(byte[])
    */
   @ManagementOperation(description = "Encode a secret using the cipher algorithm and the KeyStore password", 
         params = {@ManagementParameter(name = "secret", description = "The secret to be encoded")},
         impact = Impact.ReadOnly)
   public byte[] encode(byte[] secret) throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
      {
         if(log.isTraceEnabled())
            log.trace("Checking: " + encodePermission);
         sm.checkPermission(encodePermission);
      }

      Cipher cipher = Cipher.getInstance(cipherAlgorithm);
      cipher.init(Cipher.ENCRYPT_MODE, cipherKey, cipherSpec);
      byte[] encoding = cipher.doFinal(secret);
      return encoding;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#decode(byte[])
    */
   @ManagementOperation(description = "Decode a secret using the cipher algorithm and the KeyStore password", 
         params = {@ManagementParameter(name = "secret", description = "The secret to be encoded")},
         impact = Impact.ReadOnly)
   public byte[] decode(byte[] secret) throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(decodePermission);

      Cipher cipher = Cipher.getInstance(cipherAlgorithm);
      cipher.init(Cipher.DECRYPT_MODE, cipherKey, cipherSpec);
      byte[] decode = cipher.doFinal(secret);
      return decode;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#encode64(byte[])
    */
   @ManagementOperation(description = "Encode a secret as a base64 string using the cipher algorithm and the KeyStore password", 
         params = {@ManagementParameter(name = "secret", description = "The secret to be encoded")},
         impact = Impact.ReadOnly)
   public String encode64(byte[] secret) throws Exception
   {
      byte[] encoding = encode(secret);
      String b64 = CryptoUtil.tob64(encoding);
      return b64;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#decode64(java.lang.String)
    */
   @ManagementOperation(description = "Decode a base64 secret using the cipher algorithm and the KeyStore password", 
         params = {@ManagementParameter(name = "secret", description = "The secret to be encoded")},
         impact = Impact.ReadOnly)
   public byte[] decode64(String secret) throws Exception
   {
      byte[] encoding = CryptoUtil.fromb64(secret);
      
      //JBAS-7094: fix leading zeros
      if (encoding.length % 8 != 0)
      {
         int length = encoding.length;
         int newLength = ((length / 8) + 1) * 8;
         int pad = newLength - length; //number of leading zeros
         byte[] old = encoding;
         encoding = new byte[newLength];
         for (int i = old.length - 1; i >= 0; i--)
         {
            encoding[i + pad] = old[i];
         }
      }
      
      byte[] decode = decode(encoding);
      return decode;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getKeyManagerFactoryProvider
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The security provider of the KeyManagerFactory")
   public String getKeyManagerFactoryProvider()
   {
      return keyMgrFactoryProvider;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setKeyManagerFactoryProvider(java.lang.String)
    */
   public void setKeyManagerFactoryProvider(String provider)
   {
      this.keyMgrFactoryProvider = provider;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getKeyStoreProvider
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The security provider of the KeyStore")
   public String getKeyStoreProvider()
   {
      return keyStoreProvider;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setKeyStoreProvider(java.lang.String)
    */
   public void setKeyStoreProvider(String provider)
   {
      this.keyStoreProvider = provider;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getTrustManagerFactoryProvider
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The security provider of the TrustManagerFactory")
   public String getTrustManagerFactoryProvider()
   {
      return trustMgrFactoryProvider;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setTrustManagerFactoryProvider(java.lang.String)
    */
   public void setTrustManagerFactoryProvider(String provider)
   {
      this.trustMgrFactoryProvider = provider;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getTrustStoreProvider
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The security provider of the TrustStore")
   public String getTrustStoreProvider()
   {
      return trustStoreProvider;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setTrustStoreProvider(java.lang.String)
    */
   public void setTrustStoreProvider(String provider)
   {
      this.trustStoreProvider = provider;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getKeyManagerFactoryAlgorithm
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The algorithm of the KeyManagerFactory")
   public String getKeyManagerFactoryAlgorithm()
   {
      return keyMgrFactoryAlgorithm;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setKeyManagerFactoryAlgorithm(java.lang.String)
    */
   public void setKeyManagerFactoryAlgorithm(String algorithm)
   {
      this.keyMgrFactoryAlgorithm = algorithm;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getTrustManagerFactoryAlgorithm
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The algorithm of the TrustManagerFactory")
   public String getTrustManagerFactoryAlgorithm()
   {
      return trustMgrFactoryAlgorithm;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setTrustManagerFactoryAlgorithm(java.lang.String)
    */
   public void setTrustManagerFactoryAlgorithm(String algorithm)
   {
      this.trustMgrFactoryAlgorithm = algorithm;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getKeyStoreProviderArgument
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The argument of the KeyStore provider constructor")
   public String getKeyStoreProviderArgument()
   {
      return keyStoreProviderArgument;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setKeyStoreProviderArgument(java.lang.String)
    */
   public void setKeyStoreProviderArgument(String argument)
   {
      this.keyStoreProviderArgument = argument;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getTrustStoreProviderArgument
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The argument of the TrustStore provider constructor")
   public String getTrustStoreProviderArgument()
   {
      return trustStoreProviderArgument;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setTrustStoreProviderArgument(java.lang.String)
    */
   public void setTrustStoreProviderArgument(String argument)
   {
      this.trustStoreProviderArgument = argument;
   }
   
   /*
    * (non-Javadoc)
    *  
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getClientAlias
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The preferred client alias to be used in an eventual SSL connection")
   public String getClientAlias()
   {
      return clientAlias;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setClientAlias(java.lang.String)
    */
   public void setClientAlias(String clientAlias)
   {
      this.clientAlias = clientAlias;
   }
   /*
    * (non-Javadoc)
    *  
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#getServerAlias
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The preferred server alias to be used in an eventual SSL connection")
   public String getServerAlias()
   {
      return keyStoreAlias;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#setServerAlias(java.lang.String)
    */
   public void setServerAlias(String serverAlias)
   {
      this.keyStoreAlias = serverAlias;
   }
   
   /*
    *  (non-Javadoc)
    *  
    *  @see org.jboss.security.plugins.JaasSecurityDomainMBean#getAdditionalOptions
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "A map for additional options required by external components")
   public Properties getAdditionalOptions()
   {
      return additionalOptions;
   }
   
   /*
    *  (non-Javadoc)
    *  
    *  @see org.jboss.security.plugins.JaasSecurityDomainMBean#setAdditionalOptions(java.lang.String)
    */
   public void setAdditionalOptions(Properties additionalOptions)
   {
      this.additionalOptions = additionalOptions;
   }
   
   /*
    *  (non-Javadoc)
    *  
    *  @see org.jboss.security.plugins.JaasSecurityDomainMBean#isClientAuth
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "Flag for client authentication")
   public boolean isClientAuth()
   {
      return clientAuth;
   }
   
   /*
    *  (non-Javadoc)
    *  
    *  @see org.jboss.security.plugins.JaasSecurityDomainMBean#setClientAuth(boolean)
    */
   public void setClientAuth(boolean clientAuth)
   {
      this.clientAuth = clientAuth;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.plugins.JaasSecurityDomainMBean#reloadKeyAndTrustStore()
    */
   @ManagementOperation(description = "Reload the key and trust stores", impact = Impact.WriteOnly)
   public void reloadKeyAndTrustStore() throws Exception
   {
      loadKeyAndTrustStore();
   }
   
   /**
    * Returns the key with the given alias from the key store this security domain delegates to.
    * All keys except public keys require a service authentication token. In case of a public key
    * the authentication token will be ignored, and it can be safely null.
    *
    * @param alias - the alias corresponding to the key to be retrieved.
    * @param serviceAuthToken - the authentication token that establishes whether the calling
    *        service has the permission to retrieve the key. If no authentication token provided,
    *        or invalid authentication token is provided, the method will throw SecurityException
    *
    * @return the requested key, or null if the given alias does not exist or does not identify
    *         a key-related entry.
    *
    * @throws SecurityException for missing or invalid serviceAuthToken.
    *
    * @throws IllegalStateException if sensitive information is requested, but no service
    *         authorization token is configured on security domain.
    *
    * @see KeyStore#getKey(String, char[])
    */
   public Key getKey(String alias, String serviceAuthToken) throws Exception
   {
      log.debug(this + " got request for key with alias '" + alias + "'");
   
      Key key = keyStore.getKey(alias, keyStorePassword);
   
      if (key == null || key instanceof PublicKey)
      {
         return key;
      }
   
      verifyServiceAuthToken(serviceAuthToken);
          
      return key;
   }
   
   /**
    * Returns the certificate with the given alias or null if no such certificate exists, from the
    * trust store this security domain delegates to.
    *
    * @param alias - the alias corresponding to the certificate to be retrieved.
    *
    * @return the requested certificate, or null if the given alias does not exist or does not
    *         identify a certificate-related entry.
    *
    * @see KeyStore#getKey(String, char[])
    */
   public Certificate getCertificate(String alias) throws Exception
   {
      log.debug(this + " got request for certifcate with alias '" + alias + "'");
   
      return trustStore.getCertificate(alias);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.system.ServiceMBeanSupport#startService()
    */
   @Override
   @ManagementOperation(description = "Service lifecycle operation", impact = Impact.WriteOnly)
   protected void startService() throws Exception
   {
      // Load the secret key
      loadPBESecretKey();

      // Load the key and/or truststore into memory
      loadKeyAndTrustStore();

      // Only register with the JaasSecurityManagerService if its defined
      if (managerServiceName != null)
      {
         /*
          * Register with the JaasSecurityManagerServiceMBean. This allows this JaasSecurityDomain to function as the
          * security manager for security-domain elements that declare java:/jaas/xxx for our security domain name.
          */
         MBeanServer server = MBeanServerLocator.locateJBoss();
         Object[] params = {getSecurityDomain(), this};
         String[] signature = new String[]{"java.lang.String", "org.jboss.security.SecurityDomain"};
         server.invoke(managerServiceName, "registerSecurityDomain", params, signature);
      }
      // Register yourself with the security management
      if (securityManagement instanceof JNDIBasedSecurityManagement)
      {
         JNDIBasedSecurityManagement jbs = (JNDIBasedSecurityManagement) securityManagement;
         jbs.registerJaasSecurityDomainInstance(this);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.system.ServiceMBeanSupport#stopService()
    */
   @Override
   @ManagementOperation(description = "Service lifecycle operation", impact = Impact.WriteOnly)
   protected void stopService()
   {
      if (keyStorePassword != null)
      {
         Arrays.fill(keyStorePassword, '\0');
         keyStorePassword = null;
      }
      
      if (serviceAuthToken != null)
      {
         Arrays.fill(serviceAuthToken, '\0');
         serviceAuthToken = null;
      }
      
      cipherKey = null;

      // Deregister yourself with the security management
      if (securityManagement instanceof JNDIBasedSecurityManagement)
      {
         JNDIBasedSecurityManagement jbs = (JNDIBasedSecurityManagement) securityManagement;
         jbs.deregisterJaasSecurityDomainInstance(getSecurityDomain());
      }
   }

   /**
    * <p>
    * Loads the PBE secret key.
    * </p>
    * 
    * @throws Exception if an error ocurrs when loading the PBE key.
    */
   private void loadPBESecretKey() throws Exception
   {
      // Create the PBE secret key
      cipherSpec = new PBEParameterSpec(salt, iterationCount);
      PBEKeySpec keySpec = new PBEKeySpec(keyStorePassword);
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEwithMD5andDES");
      cipherKey = factory.generateSecret(keySpec);
   }

   private void loadKeyAndTrustStore() throws Exception
   {
      if (keyStorePassword != null)
      {
         if (keyStoreProvider != null)
         {
            if (keyStoreProviderArgument != null)
            {
               ClassLoader loader = Thread.currentThread().getContextClassLoader();
               Class clazz = loader.loadClass(keyStoreProvider);
               Class[] ctorSig = {String.class};
               Constructor ctor = clazz.getConstructor(ctorSig);
               Object[] ctorArgs = {keyStoreProviderArgument};
               Provider provider = (Provider) ctor.newInstance(ctorArgs);
               keyStore = KeyStore.getInstance(keyStoreType, provider);
            }
            else
               keyStore = KeyStore.getInstance(keyStoreType, keyStoreProvider);
         }
         else
            keyStore = KeyStore.getInstance(keyStoreType);
         InputStream is = null;
         if ((!"PKCS11".equalsIgnoreCase(keyStoreType) || !"PKCS11IMPLKS".equalsIgnoreCase(keyStoreType)) && keyStoreURL != null)
         {
            is = keyStoreURL.openStream();
         }
         keyStore.load(is, keyStorePassword);
         if (keyStoreAlias != null && !keyStore.isKeyEntry(keyStoreAlias))
         {
            throw new IOException("Cannot find key entry with alias " + keyStoreAlias + " in the keyStore");
         }
         String algorithm = null;
         if (keyMgrFactoryAlgorithm != null)
            algorithm = keyMgrFactoryAlgorithm;
         else
            algorithm = KeyManagerFactory.getDefaultAlgorithm();
         if (keyMgrFactoryProvider != null)
            keyMgr = KeyManagerFactory.getInstance(algorithm, keyMgrFactoryProvider);
         else
            keyMgr = KeyManagerFactory.getInstance(algorithm);
         keyMgr.init(keyStore, keyStorePassword);
         keyManagers = keyMgr.getKeyManagers();
         for (int i = 0; i < keyManagers.length; i++)
         {
            keyManagers[i] = new SecurityKeyManager((X509KeyManager) keyManagers[i], keyStoreAlias, clientAlias);
         }
      }
      if (trustStorePassword != null)
      {
         if (trustStoreProvider != null)
         {
            if (trustStoreProviderArgument != null)
            {
               ClassLoader loader = Thread.currentThread().getContextClassLoader();
               Class clazz = loader.loadClass(trustStoreProvider);
               Class[] ctorSig = {String.class};
               Constructor ctor = clazz.getConstructor(ctorSig);
               Object[] ctorArgs = {trustStoreProviderArgument};
               Provider provider = (Provider) ctor.newInstance(ctorArgs);
               trustStore = KeyStore.getInstance(trustStoreType, provider);
            }
            else
               trustStore = KeyStore.getInstance(trustStoreType, trustStoreProvider);
         }
         else
            trustStore = KeyStore.getInstance(trustStoreType);
         InputStream is = null;
         if ((!"PKCS11".equalsIgnoreCase(trustStoreType) || !"PKCS11IMPLKS".equalsIgnoreCase(trustStoreType)) && trustStoreURL != null)
         {
            is = trustStoreURL.openStream();
         }
         trustStore.load(is, trustStorePassword);
         String algorithm = null;
         if (trustMgrFactoryAlgorithm != null)
            algorithm = trustMgrFactoryAlgorithm;
         else
            algorithm = TrustManagerFactory.getDefaultAlgorithm();
         if (trustMgrFactoryProvider != null)
            trustMgr = TrustManagerFactory.getInstance(algorithm, trustStoreProvider);
         else
            trustMgr = TrustManagerFactory.getInstance(algorithm);
         trustMgr.init(trustStore);
      }
      else if (keyStore != null)
      {
         trustStore = keyStore;
         String algorithm = null;
         if (trustMgrFactoryAlgorithm != null)
            algorithm = trustMgrFactoryAlgorithm;
         else
            algorithm = TrustManagerFactory.getDefaultAlgorithm();
         trustMgr = TrustManagerFactory.getInstance(algorithm);
         trustMgr.init(trustStore);
      }
   }

   private URL validateStoreURL(String storeURL) throws IOException
   {
      URL url = null;
      // First see if this is a URL
      try
      {
         url = new URL(storeURL);
      }
      catch (MalformedURLException e)
      {
         // Not a URL or a protocol without a handler
      }

      // Next try to locate this as file path
      if (url == null)
      {
         File tst = new File(storeURL);
         if (tst.exists() == true)
            url = tst.toURL();
      }

      // Last try to locate this as a classpath resource
      if (url == null)
      {
         ClassLoader loader = SubjectActions.getContextClassLoader();
         url = loader.getResource(storeURL);
      }

      // Fail if no valid key store was located
      if (url == null)
      {
         String msg = "Failed to find url=" + storeURL + " as a URL, file or resource";
         throw new MalformedURLException(msg);
      }
      return url;
   }
   
   private void verifyServiceAuthToken(String serviceAuthToken) throws SecurityException
   {
      if (this.serviceAuthToken == null)
      {
         throw new IllegalStateException(
               getName() + " has been requested to provide sensitive security information, but no service authentication token has been configured on it. Use setServiceAuthToken().");
      }
   
      boolean verificationSuccessful = true;
      char[] ca = serviceAuthToken.toCharArray();
          
      if (this.serviceAuthToken.length == ca.length)
      {
         for(int i = 0; i < this.serviceAuthToken.length; i ++)
         {
            if (this.serviceAuthToken[i] != ca[i])
            {
               verificationSuccessful = false;
               break;
            }
         }
   
         if (verificationSuccessful)
         {
            log.debug("valid service authentication token");
            return;
         }
      }
   
      throw new SecurityException("service authentication token verification failed");
   }
}
