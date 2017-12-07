/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.connectionmanager;

import javax.management.ObjectName;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;
import org.w3c.dom.Element;

/**
 * MBean interface.
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public interface RARDeploymentMBean extends ServiceMBean, ManagedConnectionFactory
{
   public static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.jca:service=RARDeployment");

   /**
    * The OldRarDeployment attribute refers to a previous-generation RARDeployment. THIS IS A HACK UNTIL XSLT DEPLOYMENT IS WRITTEN
    * 
    * @return value of OldRarDeployment
    */
   ObjectName getOldRarDeployment();

   /**
    * Set the value of OldRarDeployment
    * 
    * @param oldRarDeployment - Value to assign to OldRarDeployment
    */
   void setOldRarDeployment(ObjectName oldRarDeployment);

   /**
    * The RARName attribute holds the file name of the rar
    * 
    * @return the rar name value.
    */
   String getRARName();

   /**
    * Set the RARName value.
    * 
    * @param rarName The new DisplayName value.
    */
   void setRARName(String rarName);

   /**
    * The connection definition inside the rar, it identifies the unique connection factory
    * 
    * @return the rar name value.
    */
   String getConnectionDefinition();

   /**
    * Set the connection definition.
    * 
    * @param connectionDefinition - the connection definition
    */
   void setConnectionDefinition(String connectionDefinition);

   /**
    * The VendorName attribute holds the VendorName from the ra.xml It should be supplied by xslt from ra.xml
    * 
    * @return the VendorName value.
    */
   String getVendorName();

   /**
    * Set the VendorName value.
    * 
    * @param vendorName The new VendorName value.
    */
   void setVendorName(String vendorName);

   /**
    * The SpecVersion attribute holds the SpecVersion from the ra.xml It should be supplied by xslt from ra.xml
    * 
    * @return the SpecVersion value.
    */
   String getSpecVersion();

   /**
    * Set the SpecVersion value.
    * 
    * @param specVersion The new SpecVersion value.
    */
   void setSpecVersion(String specVersion);

   /**
    * The EisType attribute holds the EisType from the ra.xml. It should be supplied by xslt from ra.xml
    * 
    * @return the EisType value.
    */
   String getEisType();

   /**
    * Set the EisType value.
    * 
    * @param eisType The new EisType value.
    */
   void setEisType(String eisType);

   /**
    * The Version attribute holds the Version from the ra.xml. It should be supplied by xslt from ra.xml
    * 
    * @return the Version value.
    */
   String getVersion();

   /**
    * Set the Version value.
    * 
    * @param version The new Version value.
    */
   void setVersion(String version);

   /**
    * The ManagedConnectionFactoryClass attribute holds the ManagedConnectionFactoryClass from the ra.xml. It should be supplied by xslt from ra.xml
    * 
    * @return the ManagedConnectionFactoryClass value.
    */
   String getManagedConnectionFactoryClass();

   /**
    * Set the ManagedConnectionFactoryClass value.
    * 
    * @param managedConnectionFactoryClass The new ManagedConnectionFactoryClass value.
    */
   void setManagedConnectionFactoryClass(String managedConnectionFactoryClass);

   /**
    * The ConnectionFactoryInterface attribute holds the ConnectionFactoryInterface from the ra.xml. It should be supplied by xslt from ra.xml
    * 
    * @return the ConnectionFactoryInterface value.
    */
   String getConnectionFactoryInterface();

   /**
    * Set the ConnectionFactoryInterface value.
    * 
    * @param connectionFactoryInterface The ConnectionFactoryInterface value.
    */
   void setConnectionFactoryInterface(String connectionFactoryInterface);

   /**
    * The ConnectionFactoryImplClass attribute holds the ConnectionFactoryImplClass from the ra.xml. It should be supplied by xslt from ra.xml
    * 
    * @return the ConnectionFactoryImplClass value.
    */
   String getConnectionFactoryImplClass();

   /**
    * Set the ConnectionFactoryImplClass value.
    * 
    * @param connectionFactoryImplClass The ConnectionFactoryImplClass value.
    */
   void setConnectionFactoryImplClass(String connectionFactoryImplClass);

   /**
    * The ConnectionInterface attribute holds the ConnectionInterface from the ra.xml. It should be supplied by xslt from ra.xml
    * 
    * @return the ConnectionInterface value.
    */
   String getConnectionInterface();

   /**
    * Set the ConnectionInterface value.
    * 
    * @param connectionInterface The ConnectionInterface value.
    */
   void setConnectionInterface(String connectionInterface);

   /**
    * The ConnectionImplClass attribute holds the ConnectionImplClass from the ra.xml. It should be supplied by xslt from ra.xml
    * 
    * @return the connectionImplClass value.
    */
   String getConnectionImplClass();

   /**
    * Set the ConnectionImplClass value.
    * 
    * @param connectionImplClass The ConnectionImplClass value.
    */
   void setConnectionImplClass(String connectionImplClass);

   /**
    * The TransactionSupport attribute holds the TransactionSupport from the ra.xml. It should be supplied by xslt from ra.xml It is ignored, and choice of ConnectionManager implementations determine transaction support. Get the TransactionSupport value.
    * 
    * @return the TransactionSupport value.
    */
   String getTransactionSupport();

   /**
    * Set the TransactionSupport value.
    * 
    * @param transactionSupport The TransactionSupport value.
    */
   void setTransactionSupport(String transactionSupport);

   /**
    * The ManagedConnectionFactoryProperties attribute holds the ManagedConnectionFactoryProperties from the ra.xml, together with user supplied values for all or some of these properties. This must be supplied as an element in the same format as in ra.xml, wrapped in a properties tag. It should be supplied by xslt from ra.xml merged with an user configuration xml file. An alternative format has a config-property element with attributes for name and type and the value as content.
    * 
    * @return the ManagedConnectionFactoryProperties value.
    */
   Element getManagedConnectionFactoryProperties();

   /**
    * Set the ManagedConnectionFactoryProperties value.
    * 
    * @param managedConnectionFactoryProperties The ManagedConnectionFactoryProperties value.
    */
   void setManagedConnectionFactoryProperties(Element managedConnectionFactoryProperties);

   /**
    * The AuthenticationMechanismType attribute holds the AuthenticationMechanismType from the ra.xml. It should be supplied by xslt from ra.xml
    * 
    * @return the AuthenticationMechanismType value.
    */
   String getAuthenticationMechanismType();

   /**
    * Set the AuthenticationMechanismType value.
    * 
    * @param authenticationMechanismType The AuthenticationMechanismType value.
    */
   void setAuthenticationMechanismType(String authenticationMechanismType);

   /**
    * The CredentialInterface attribute holds the CredentialInterface from the ra.xml. It should be supplied by xslt from ra.xml
    * 
    * @return the CredentialInterface value.
    */
   String getCredentialInterface();

   /**
    * Set the CredentialInterface value.
    * 
    * @param credentialInterface The CredentialInterface value.
    */
   void setCredentialInterface(String credentialInterface);

   /**
    * The ReauthenticationSupport attribute holds the ReauthenticationSupport from the ra.xml. It should be supplied by xslt from ra.xml
    * 
    * @return the ReauthenticationSupport value.
    */
   boolean isReauthenticationSupport();

   /**
    * Set the ReauthenticationSupport value.
    * 
    * @param reauthenticationSupport The ReauthenticationSupport value.
    */
   void setReauthenticationSupport(boolean reauthenticationSupport);

   /**
    * The <code>getMcfInstance</code> method returns the ManagedConnectionFactory instance represented by this mbean. It is needed so PasswordCredentials can match up correctly. This will probably have to be implemented as an interceptor when the mcf is directly deployed as an mbean.
    * @return a <code>ManagedConnectionFactory</code> value
    */
   ManagedConnectionFactory getMcfInstance();

   /**
    * The setManagedConnectionFactoryAttribute method can be used to set attributes on the ManagedConnectionFactory from code, without using the xml configuration.
    * 
    * @param name a <code>String</code> value
    * @param clazz a <code>Class</code> value
    * @param value an <code>Object</code> value
    */
   void setManagedConnectionFactoryAttribute(String name, Class clazz, Object value);

   /**
    * The <code>getManagedConnectionFactoryAttribute</code> method can be used to examine the managed connection factory properties.
    * 
    * @param name a <code>String</code> value
    * @return an <code>Object</code> value
    */
   Object getManagedConnectionFactoryAttribute(String name);
}
