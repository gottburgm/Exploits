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

import java.io.IOException;
import java.util.Properties;

import javax.management.ObjectName;

import org.jboss.system.ServiceMBean;


/** The JaasSecurityDomainMBean adds support for KeyStore management.

 @author Scott.Stark@jboss.org
 @author <a href="mailto:jasone@greenrivercomputing.com">Jason Essington</a>
 @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 @author <a href="mailto:mmoyses@redhat.com">Marcus Moyses</a>
 @version $Revision: 109851 $
*/
public interface JaasSecurityDomainMBean extends ServiceMBean
{
   /** KeyStore implementation type being used.
    @return the KeyStore implementation type being used.
    */
   public String getKeyStoreType();
   /** Set the type of KeyStore implementation to use. This is
    passed to the KeyStore.getInstance() factory method.
    */
   public void setKeyStoreType(String type);
   /** Get the KeyStore database URL string.
    */
   public String getKeyStoreURL();
   /** Set the KeyStore database URL string. This is used to obtain
    an InputStream to initialize the KeyStore.
    */
   public void setKeyStoreURL(String storeURL) throws IOException;
    /** Set the credential string for the KeyStore.
    */
   public void setKeyStorePass(String password) throws Exception;
   /** Set the service authorization token for this security domain. Services requesting sensitive
    * information from this domain (PrivateKeys, for example) must present this authorization token
    * otherwise the call will fail with SecurityException.
    */
   public void setServiceAuthToken(String serviceAuthToken) throws Exception;
   /** Get the alias of the KeyStore.
    */
   public String getKeyStoreAlias();
   /** Set the alias of the KeyStore.
    */
   public void setKeyStoreAlias(String alias);
   /** Get the type of the trust store
    * @return the type of the trust store
    */ 
   public String getTrustStoreType();
   /** Set the type of the trust store
    * @param type - the trust store implementation type
    */ 
   public void setTrustStoreType(String type);
   /** Set the credential string for the trust store.
   */
   public void setTrustStorePass(String password) throws Exception;   
   /** Get the trust store database URL string.
    */
   public String getTrustStoreURL();
   /** Set the trust store database URL string. This is used to obtain
    an InputStream to initialize the trust store.
    */
   public void setTrustStoreURL(String storeURL) throws IOException;
   /**
       Reload the key- and truststore
   */
   public void reloadKeyAndTrustStore() throws Exception;
   /** The JMX object name string of the security manager service.
    @return The JMX object name string of the security manager service.
    */
   public ObjectName getManagerServiceName();
   /** Set the JMX object name string of the security manager service.
    */
   public void setManagerServiceName(ObjectName jmxName);

   /** Set the salt used with PBE based on the keystore password.
    * @param salt - an 8 char randomization string
    */ 
   public void setSalt(String salt);
   /** Set the iteration count used with PBE based on the keystore password.
    * @param count - an iteration count randomization value
    */ 
   public void setIterationCount(int count);

   /** Encode a secret using the keystore password and PBEwithMD5andDES algo
    * @param secret - the byte sequence to encrypt
    * @return the encrypted byte sequence
    * @throws Exception
    */ 
   public byte[] encode(byte[] secret)
      throws Exception;

   /** Decode a secret using the keystore password and PBEwithMD5andDES algo
    * @param secret - the byte sequence to decrypt
    * @return the decrypted byte sequence
    * @throws Exception
    */ 
   public byte[] decode(byte[] secret)
      throws Exception;

   /** Encode a secret using the keystore password and PBEwithMD5andDES algo
    * @param secret - the byte sequence to encrypt as a base64 string using
    *    the Util.tob64() function
    * @return the encrypted byte sequence
    * @throws Exception
    */    
   public String encode64(byte[] secret)
      throws Exception;

   /** Decode a secret using the keystore password and PBEwithMD5andDES algo
    * @param secret - the Util.tob64 string represention to decrypt
    * @return the decrypted byte sequence
    * @throws Exception
    */ 
   public byte[] decode64(String secret)
      throws Exception;
   
   /**
    * Returns the KeyStore provider
    * @return provider of the KeyStore
    */
   public String getKeyStoreProvider();
   
   /**
    * Sets the KeyStore provider
    * @param provider provider name of the KeyStore
    */
   public void setKeyStoreProvider(String provider);
   
   /**
    * Returns the KeyManagerFactory provider 
    * @return provider of the KeyManagerFactory
    */
   public String getKeyManagerFactoryProvider();
   
   /**
    * Sets the KeyManagerFactory provider
    * @param provider provider name of the KeyManagerFactory
    */
   public void setKeyManagerFactoryProvider(String provider);
   
   /**
    * Returns the TrustStore provider
    * @return provider of the TrustStore
    */
   public String getTrustStoreProvider();
   
   /**
    * Sets the TrustStore provider
    * @param provider provider name of the TrustStore
    */
   public void setTrustStoreProvider(String provider);
   
   /**
    * Returns the TrustManagerFactory provider 
    * @return provider of the TrustManagerFactory
    */
   public String getTrustManagerFactoryProvider();
   
   /**
    * Sets the TrustManagerFactory provider
    * @param provider provider name of the TrustManagerFactory
    */
   public void setTrustManagerFactoryProvider(String provider);
   
   /**
    * Returns the KeyManagerFactory algorithm
    * @return algorithm of the KeyManagerFactory
    */
   public String getKeyManagerFactoryAlgorithm();
   
   /**
    * Sets the KeyManagerFactory algorithm
    * @param algorithm algorithm of the KeyManagerFactory
    */
   public void setKeyManagerFactoryAlgorithm(String algorithm);
   
   /**
    * Returns the TrustManagerFactory algorithm
    * @return algorithm of the TrustManagerFactory
    */
   public String getTrustManagerFactoryAlgorithm();
   
   /**
    * Sets the TrustManagerFactory algorithm
    * @param algorithm algorithm of the TrustManagerFactory
    */
   public void setTrustManagerFactoryAlgorithm(String algorithm);
   
   /**
    * Returns the argument for the KeyStore provider constructor
    * @return argument for the KeyStore provider
    */
   public String getKeyStoreProviderArgument();

   /**
    * Sets the argument for the KeyStore provider constructor
    * @param argument for the KeyStore provider
    */
   public void setKeyStoreProviderArgument(String argument);

   /**
    * Returns the argument for the TrustStore provider constructor
    * @return argument for the TrustStore provider
    */
   public String getTrustStoreProviderArgument();

   /**
    * Sets the argument for the TrustStore provider constructor
    * @param argument for the TrustStore provider
    */
   public void setTrustStoreProviderArgument(String argument);
   
   /**
    * Gets the preferred client alias to be used in an eventual SSL connection
    * @return client alias name
    */
   public String getClientAlias();
   
   /**
    * Sets the preferred client alias to be used in an eventual SSL connection
    * @param clientAlias client alias name
    */
   public void setClientAlias(String clientAlias);
   
   /**
    * Gets the preferred server alias to be used in an eventual SSL connection
    * @deprecated use getKeyStoreAlias() instead
    * @return client alias name
    */
   public String getServerAlias();
   
   /**
    * Sets the preferred server alias to be used in an eventual SSL connection
    * @deprecated use setKeyStoreAlias(String) instead
    * @param clientAlias client alias name
    */
   public void setServerAlias(String serverAlias);
   
   /**
    * Gets the additionalOptions map
    * 
    * @return the map
    */
   public Properties getAdditionalOptions();
   
   /**
    * Sets the additionalOptions map
    * 
    * @param additionalOptions the map
    */
   public void setAdditionalOptions(Properties additionalOptions);
   
   /**
    * Gets the clientAuth flag
    * 
    * @return flag
    */
   public boolean isClientAuth();

   /**
    * Sets the clientAuth flag
    * 
    * @param clientAuth the flag
    */
   public void setClientAuth(boolean clientAuth);
}
