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
package org.jboss.resource.metadata;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Connector meta data
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 84262 $
 */
public class ConnectorMetaData extends ConfigPropertyMetaDataContainer
{
   private static final long serialVersionUID = -3049391010669865389L;

   /** The url TODO move to RARDeploymentMetaData */
   private URL url;
   
   /** The version */
   private String version = "1.0";

   /** The vendor name */
   private String vendorName;

   /** The eis type */
   private String eisType;

   /** The resource adapter version */
   private String raVersion;

   /** The resource adapter class */
   private String raClass;

   /** Reauthentication support */
   private boolean reauthenticationSupport;

   /** The license */
   private LicenseMetaData lmd = new LicenseMetaData();

   /** The descriptions */
   private ConcurrentHashMap descriptions = new ConcurrentHashMap();

   /** The connection definitions */
   private HashSet connectionDefinitions = new HashSet();

   /** The transaction support meta data */
   private TransactionSupportMetaData tsmd = new TransactionSupportMetaData();

   /** The authentication mechanism meta data */
   private AuthenticationMechanismMetaData ammd = new AuthenticationMechanismMetaData();

   /** The message listeners */
   private HashSet listeners = new HashSet();

   /** The admin objects */
   private HashSet adminObjects = new HashSet();

   /** The security permissions */
   private HashSet securityPermissions = new HashSet();

   public ConnectorMetaData()
   {
      DescriptionGroupMetaData dmd = new DescriptionGroupMetaData();
      descriptions.put(dmd.getLanguage(), dmd);
   }

   /**
    * Get the url.
    * 
    * @return the url.
    */
   public URL getURL()
   {
      return url;
   }

   /**
    * Set the url.
    * 
    * @param url the url.
    */
   public void setURL(URL url)
   {
      this.url = url;
   }

   /**
    * Get the connector version
    * 
    * @return the connector version
    */
   public String getVersion()
   {
      return version;
   }

   /**
    * Set the connector version
    * 
    * @param version the connector version
    */
   public void setVersion(String version)
   {
      this.version = version;
   }

   /**
    * Get the vendor name
    * 
    * @return the vendor name
    */
   public String getVendorName()
   {
      return vendorName;
   }

   /**
    * Set the vendor name
    * 
    * @param vendorName the vendor name
    */
   public void setVendorName(String vendorName)
   {
      this.vendorName = vendorName;
   }

   /**
    * Get the eis type
    * 
    * @return the eis type
    */
   public String getEISType()
   {
      return eisType;
   }

   /**
    * Set the eis Type
    * 
    * @param eisType the eis type
    */
   public void setEISType(String eisType)
   {
      this.eisType = eisType;
   }

   /**
    * Get the resource adapter version
    * 
    * @return the resource adapter version
    */
   public String getRAVersion()
   {
      return raVersion;
   }

   /**
    * Set the resource adapter version
    * 
    * @param version the resource adapter version
    */
   public void setRAVersion(String version)
   {
      this.raVersion = version;
   }

   /**
    * Get the resource adapter class
    * 
    * @return the resource adapter class
    */
   public String getRAClass()
   {
      return raClass;
   }

   /**
    * Set the resource adapter class
    * 
    * @param raClass the resource adapter class
    */
   public void setRAClass(String raClass)
   {
      this.raClass = raClass;
   }

   /**
    * Get the reauthentication support
    * 
    * @return the reauthentication support
    */
   public boolean getReauthenticationSupport()
   {
      return reauthenticationSupport;
   }

   /**
    * Set the reauthentication support
    * 
    * @param reauthenticationSupport true for support, false otherwise
    */
   public void setReauthenticationSupport(boolean reauthenticationSupport)
   {
      this.reauthenticationSupport = reauthenticationSupport;
   }

   /**
    * Get the license
    * 
    * @return the license
    */
   public LicenseMetaData getLicense()
   {
      return lmd;
   }

   /**
    * Get the description for the default language
    * 
    * @return the description for the default langugage
    */
   public DescriptionGroupMetaData getDescription()
   {
      DescriptionGroupMetaData dgmd = (DescriptionGroupMetaData) descriptions.get(Locale.getDefault().getLanguage());
      // No description using the default locale, just use the first
      if (dgmd == null)
      {
         for (Iterator i = descriptions.values().iterator(); i.hasNext();)
         {
            dgmd = (DescriptionGroupMetaData) i.next();
            break;
         }
      }
      return dgmd;
   }
   
   /**
    * Get the description for the give language
    * 
    * @param lang the language
    * @return the description
    */
   public DescriptionGroupMetaData getDescription(String lang)
   {
      return (DescriptionGroupMetaData) descriptions.get(lang);
   }
   
   /**
    * Add a description
    * 
    * @param dmd the description
    */
   public void addDescription(DescriptionGroupMetaData dmd)
   {
      descriptions.put(dmd.getLanguage(), dmd);
   }

   /**
    * Get the transaction support
    * 
    * @return the transaction support
    */
   public TransactionSupportMetaData getTransactionSupport()
   {
      return tsmd;
   }

   /**
    * Set the transaction support
    * 
    * @param tsmd the transaction support
    */
   public void setTransactionSupport(TransactionSupportMetaData tsmd)
   {
      this.tsmd = tsmd;
   }
   /**
    * Get the authentication mechanism
    * 
    * @return the authentication mechanism
    */
   public AuthenticationMechanismMetaData getAuthenticationMechanism()
   {
      return ammd;
   }

   /**
    * Set the authentication mechansim
    * 
    * @param ammd the authentication mechansim
    */
   public void setAuthenticationMechansim(AuthenticationMechanismMetaData ammd)
   {
      this.ammd = ammd;
   }
   
   /**
    * Add a connection definition
    * 
    * @param cdmd the connection definition
    */
   public void addConnectionDefinition(ConnectionDefinitionMetaData cdmd)
   {
      connectionDefinitions.add(cdmd);
   }
   
   /**
    * Get the connection definition
    *  
    * @param connectionDefinition the idenitifying factory
    * @return the metadata or null if there isn't one
    */
   public ConnectionDefinitionMetaData getConnectionDefinition(String connectionDefinition)
   {
      for (Iterator i = connectionDefinitions.iterator(); i.hasNext();)
      {
         ConnectionDefinitionMetaData cdmd = (ConnectionDefinitionMetaData) i.next();
         if (cdmd.getConnectionFactoryInterfaceClass().equals(connectionDefinition))
            return cdmd;
      }
      return null;
   }
   
   /**
    * Add a message listener
    * 
    * @param mlmd the message listener
    */
   public void addMessageListener(MessageListenerMetaData mlmd)
   {
      listeners.add(mlmd);
   }
   
   /**
    * Get the message listener
    *  
    * @param messagingType the identifying listener interface
    * @return the metadata or null if there isn't one
    */
   public MessageListenerMetaData getMessageListener(String messagingType)
   {
      for (Iterator i = listeners.iterator(); i.hasNext();)
      {
         MessageListenerMetaData mlmd = (MessageListenerMetaData) i.next();
         if (mlmd.getType().equals(messagingType))
            return mlmd;
      }
      return null;
   }
   
   /**
    * Add an administered object
    * 
    * @param aomd the administered object
    */
   public void addAdminObject(AdminObjectMetaData aomd)
   {
      adminObjects.add(aomd);
   }
   
   /**
    * Get the admin object
    *  
    * @param interfaceName the identifying admin object interface
    * @return the metadata or null if there isn't one
    */
   public AdminObjectMetaData getAdminObject(String interfaceName)
   {
      for (Iterator i = adminObjects.iterator(); i.hasNext();)
      {
         AdminObjectMetaData aomd = (AdminObjectMetaData) i.next();
         if (aomd.getAdminObjectInterfaceClass().equals(interfaceName))
            return aomd;
      }
      return null;
   }
   
   public Collection getAdminObjects()
   {
      return adminObjects;
   }
   
   /**
    * Add a security permission
    * 
    * @param spmd the security permission
    */
   public void addSecurityPermission(SecurityPermissionMetaData spmd)
   {
      securityPermissions.add(spmd);
   }
   
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("ConnectorMetaData").append('@');
      buffer.append(Integer.toHexString(System.identityHashCode(this)));
      buffer.append("[version=").append(version);
      buffer.append(" vendorName=").append(vendorName);
      buffer.append(" eisType=").append(eisType);
      buffer.append(" resourceAdapterVersion=").append(raVersion);
      buffer.append(" resourceAdapterClass=").append(raClass);
      buffer.append(" license=").append(lmd);
      buffer.append(" properties=").append(getProperties());
      buffer.append(" descriptions=").append(descriptions.values());
      buffer.append(" connectionDefinitions=").append(connectionDefinitions);
      buffer.append(" transactionSupport=").append(tsmd);
      buffer.append(" authenticationMechanism=").append(ammd);
      buffer.append(" reauthenticationSupport=").append(reauthenticationSupport);
      buffer.append(" messageListeners=").append(listeners);
      buffer.append(" adminobjects=").append(adminObjects);
      buffer.append(" securityPermissions=").append(securityPermissions);
      buffer.append(']');
      return buffer.toString();
   }
}
