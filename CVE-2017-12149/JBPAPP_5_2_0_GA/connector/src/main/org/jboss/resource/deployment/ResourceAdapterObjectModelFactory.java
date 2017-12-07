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
package org.jboss.resource.deployment;

import org.jboss.logging.Logger;
import org.jboss.resource.metadata.AdminObjectMetaData;
import org.jboss.resource.metadata.AuthenticationMechanismMetaData;
import org.jboss.resource.metadata.ConfigPropertyMetaData;
import org.jboss.resource.metadata.ConnectionDefinitionMetaData;
import org.jboss.resource.metadata.ConnectorMetaData;
import org.jboss.resource.metadata.DescriptionGroupMetaData;
import org.jboss.resource.metadata.DescriptionMetaData;
import org.jboss.resource.metadata.LicenseMetaData;
import org.jboss.resource.metadata.MessageListenerMetaData;
import org.jboss.resource.metadata.RequiredConfigPropertyMetaData;
import org.jboss.resource.metadata.SecurityPermissionMetaData;
import org.jboss.resource.metadata.TransactionSupportMetaData;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;

/**
 * Object factory for resource adapter metadata
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class ResourceAdapterObjectModelFactory implements ObjectModelFactory
{
   /** The logger */
   private static final Logger log = Logger.getLogger(ResourceAdapterObjectModelFactory.class);
   
   /** Trace enabled */
   private boolean trace = log.isTraceEnabled();

   /**
    * connector child elements
    * 
    * @param cmd the connector meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param attrs the attributes
    */
   public Object newChild(ConnectorMetaData cmd, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if (trace)
         log.trace("connector newChild: nuri=" + namespaceURI + " localName=" + localName + " attrs=" + attrs);

      if (localName.equals("vendor-name") ||
            localName.equals("eis-type") ||
            localName.equals("resourceadapter-version") ||
            (localName.equals("resourceadapter") && cmd.getVersion().equals("1.0") == false) ||
            localName.equals("resourceadapter-class") ||
            localName.equals("reauthentication-support"))
      {
         return null;
      }
      else if (localName.equals("description") ||
               localName.equals("display-name") ||
               localName.equals("small-icon") ||
               localName.equals("large-icon"))
      {
         String language = attrs.getValue("xml:lang");
         DescriptionGroupMetaData dmd = null;
         if (language == null)
            dmd = cmd.getDescription();
         else
            dmd = cmd.getDescription(language);
         if (dmd == null)
            dmd = new DescriptionGroupMetaData(language);
         cmd.addDescription(dmd);
         return dmd;
      }
      else if (localName.equals("icon") && cmd.getVersion().equals("1.0"))
      {
         return null;
      }
      else if (localName.equals("config-property"))
      {
         ConfigPropertyMetaData cpmd = new ConfigPropertyMetaData();
         cmd.addProperty(cpmd);
         return cpmd;
      }
      else if (localName.equals("license"))
      {
         return cmd.getLicense();
      }
      else if (localName.equals("outbound-resourceadapter"))
      {
         return null;
      }
      else if (localName.equals("connection-definition") ||
               (localName.equals("resourceadapter") && cmd.getVersion().equals("1.0")))
      {
         ConnectionDefinitionMetaData cdmd = new ConnectionDefinitionMetaData(cmd);
         cmd.addConnectionDefinition(cdmd);
         return cdmd;
      }
      else if (localName.equals("transaction-support"))
      {
         TransactionSupportMetaData tsmd = new TransactionSupportMetaData();
         cmd.setTransactionSupport(tsmd);
         return tsmd;
      }
      else if (localName.equals("authentication-mechanism"))
      {
         AuthenticationMechanismMetaData ammd = new AuthenticationMechanismMetaData();
         cmd.setAuthenticationMechansim(ammd);
         return ammd;
      }
      else if (localName.equals("inbound-resourceadapter") ||
               localName.equals("messageadapter"))
      {
         return null;
      }
      else if (localName.equals("messagelistener"))
      {
         MessageListenerMetaData mlmd = new MessageListenerMetaData();
         cmd.addMessageListener(mlmd);
         return mlmd;
      }
      else if (localName.equals("adminobject"))
      {
         AdminObjectMetaData aomd = new AdminObjectMetaData();
         cmd.addAdminObject(aomd);
         return aomd;
      }
      else if (localName.equals("security-permission"))
      {
         SecurityPermissionMetaData spmd = new SecurityPermissionMetaData();
         cmd.addSecurityPermission(spmd);
         return spmd;
      }
      // 1.0
      else if (localName.equals("spec-version") ||
               localName.equals("version"))
      {
         return null;
      }
      throw new IllegalArgumentException("Unknown connector newChild: nuri=" +namespaceURI + " localName=" + localName + " attrs=" + attrs);
   }

   /**
    * connector elements
    * 
    * @param cmd the connector meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param value the value
    */
   public void setValue(ConnectorMetaData cmd, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (trace)
         log.trace("connector setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
      if (localName.equals("connector") ||
          localName.equals("resourceadapter") ||
          localName.equals("outbound-resourceadapter") ||
          localName.equals("inbound-resourceadapter") ||
          localName.equals("messageadapter"))
      {
      }
      else if (localName.equals("vendor-name"))
         cmd.setVendorName(value);
      else if (localName.equals("eis-type"))
         cmd.setEISType(value);
      else if (localName.equals("resourceadapter-version"))
         cmd.setRAVersion(value);
      else if (localName.equals("resourceadapter-class"))
         cmd.setRAClass(value);
      else if (localName.equals("reauthentication-support"))
         cmd.setReauthenticationSupport(Boolean.valueOf(value).booleanValue());
      // 1.0
      else if (localName.equals("spec-version"))
         cmd.setVersion(value);
      else if (localName.equals("version"))
         cmd.setRAVersion(value);
      else
         throw new IllegalArgumentException("Unknown connector setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
   }

   /**
    * description group elements
    * 
    * @param dmd the description meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param value the value
    */
   public void setValue(DescriptionGroupMetaData dmd, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (trace)
         log.trace("description group setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
      if (localName.equals("description"))
         dmd.setDescription(value);
      else if (localName.equals("display-name"))
         dmd.setDisplayName(value);
      else if (localName.equals("small-icon"))
         dmd.setSmallIcon(value);
      else if (localName.equals("large-icon"))
         dmd.setLargeIcon(value);
      else
         throw new IllegalArgumentException("Unknown description group setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
   }

   /**
    * description elements
    * 
    * @param dmd the description meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param value the value
    */
   public void setValue(DescriptionMetaData dmd, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (trace)
         log.trace("description setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
      if (localName.equals("description"))
         dmd.setDescription(value);
      else
         throw new IllegalArgumentException("Unknown description setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
   }

   /**
    * config property child elements
    * 
    * @param cpmd the config property meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param attrs the attributes
    */
   public Object newChild(ConfigPropertyMetaData cpmd, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if (trace)
         log.trace("config property newChild: nuri=" + namespaceURI + " localName=" + localName + " attrs=" + attrs);
      if (localName.equals("config-property-name") ||
          localName.equals("config-property-type") ||
          localName.equals("config-property-value"))
      {
         return null;
      }
      else if (localName.equals("description"))
      {
         String language = attrs.getValue("xml:lang");
         DescriptionMetaData dmd = null;
         if (language == null)
            dmd = cpmd.getDescription();
         else
            dmd = cpmd.getDescription(language);
         if (dmd == null)
            dmd = new DescriptionMetaData(language);
         cpmd.addDescription(dmd);
         return dmd;
      }
      
      throw new IllegalArgumentException("Unknown config property newChild: nuri=" +namespaceURI + " localName=" + localName + " attrs=" + attrs);
   }

   /**
    * config property elements
    * 
    * @param cpmd the description meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param value the value
    */
   public void setValue(ConfigPropertyMetaData cpmd, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (trace)
         log.trace("config property setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
      if (localName.equals("config-property"))
      {
      }
      else if (localName.equals("config-property-name"))
         cpmd.setName(value);
      else if (localName.equals("config-property-type"))
         cpmd.setType(value);
      else if (localName.equals("config-property-value"))
         cpmd.setValue(value);
      else
         throw new IllegalArgumentException("Unknown config property setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
   }

   /**
    * license child elements
    * 
    * @param lmd the license meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param attrs the attributes
    */
   public Object newChild(LicenseMetaData lmd, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if (trace)
         log.trace("license newChild: nuri=" + namespaceURI + " localName=" + localName + " attrs=" + attrs);
      if (localName.equals("license"))
      {
         return null;
      }
      else if (localName.equals("license-required"))
      {
         return null;
      }
      else if (localName.equals("description"))
      {
         String language = attrs.getValue("xml:lang");
         DescriptionMetaData dmd = null;
         if (language == null)
            dmd = lmd.getDescription();
         else
            dmd = lmd.getDescription(language);
         if (dmd == null)
            dmd = new DescriptionMetaData(language);
         lmd.addDescription(dmd);
         return dmd;
      }
      
      throw new IllegalArgumentException("Unknown license newChild: nuri=" +namespaceURI + " localName=" + localName + " attrs=" + attrs);
   }

   /**
    * license elements
    * 
    * @param lmd the license meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param value the value
    */
   public void setValue(LicenseMetaData lmd, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (trace)
         log.trace("license setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
      if (localName.equals("license"))
      {
      }
      else if (localName.equals("license-required"))
         lmd.setRequired(Boolean.valueOf(value).booleanValue());
      else
         throw new IllegalArgumentException("Unknown license setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
   }

   /**
    * connection definition child elements
    * 
    * @param cdmd the message listener meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param attrs the attributes
    */
   public Object newChild(ConnectionDefinitionMetaData cdmd, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if (trace)
         log.trace("connection definition newChild: nuri=" + namespaceURI + " localName=" + localName + " attrs=" + attrs);
      if (localName.equals("connection-definition") ||
          localName.equals("managedconnectionfactory-class") ||
          localName.equals("connectionfactory-interface") ||
          localName.equals("connectionfactory-impl-class") ||
          localName.equals("connection-interface") ||
          localName.equals("connection-impl-class"))
      {
         return null;
      }
      else if (localName.equals("config-property"))
      {
         ConfigPropertyMetaData cpmd = new ConfigPropertyMetaData();
         cdmd.addProperty(cpmd);
         return cpmd;
      }
      // 1.0
      else if (localName.equals("transaction-support"))
      {
         TransactionSupportMetaData tsmd = new TransactionSupportMetaData();
         cdmd.getConnector().setTransactionSupport(tsmd);
         return tsmd;
      }
      else if (localName.equals("authentication-mechanism"))
      {
         AuthenticationMechanismMetaData ammd = new AuthenticationMechanismMetaData();
         cdmd.getConnector().setAuthenticationMechansim(ammd);
         return ammd;
      }
      else if (localName.equals("security-permission"))
      {
         SecurityPermissionMetaData spmd = new SecurityPermissionMetaData();
         cdmd.getConnector().addSecurityPermission(spmd);
         return spmd;
      }
      else if (localName.equals("reauthentication-support"))
      {
         return null;
      }
      
      throw new IllegalArgumentException("Unknown connection definition newChild: nuri=" +namespaceURI + " localName=" + localName + " attrs=" + attrs);
   }

   /**
    * connection definition elements
    * 
    * @param cdmd the description meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param value the value
    */
   public void setValue(ConnectionDefinitionMetaData cdmd, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (trace)
         log.trace("connection definition setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
      if (localName.equals("connection-definition"))
      {
      }
      else if (localName.equals("managedconnectionfactory-class"))
         cdmd.setManagedConnectionFactoryClass(value);
      else if (localName.equals("connectionfactory-interface"))
         cdmd.setConnectionFactoryInterfaceClass(value);
      else if (localName.equals("connectionfactory-impl-class"))
         cdmd.setConnectionFactoryImplementationClass(value);
      else if (localName.equals("connection-interface"))
         cdmd.setConnectionInterfaceClass(value);
      else if (localName.equals("connection-impl-class"))
         cdmd.setConnectionImplementationClass(value);
      // 1.0
      else if (localName.equals("reauthentication-support"))
         cdmd.getConnector().setReauthenticationSupport(Boolean.valueOf(value).booleanValue());
      else
         throw new IllegalArgumentException("Unknown connection definition setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
   }

   /**
    * transaction support child elements
    * 
    * @param tsmd the transaction support meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param attrs the attributes
    */
   public Object newChild(TransactionSupportMetaData tsmd, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if (trace)
         log.trace("transaction support newChild: nuri=" + namespaceURI + " localName=" + localName + " attrs=" + attrs);
      if (localName.equals("transaction-support"))
         return null;
      throw new IllegalArgumentException("Unknown transaction support newChild: nuri=" +namespaceURI + " localName=" + localName + " attrs=" + attrs);
   }

   /**
    * transaction support elements
    * 
    * @param tsmd the transaction support meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param value the value
    */
   public void setValue(TransactionSupportMetaData tsmd, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (trace)
         log.trace("transaction support setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
      if (localName.equals("transaction-support") && value.equals("NoTransaction"))
         tsmd.setTransactionSupport(TransactionSupportMetaData.NoTransaction);
      else if (localName.equals("transaction-support") && value.equals("LocalTransaction"))
         tsmd.setTransactionSupport(TransactionSupportMetaData.LocalTransaction);
      else if (localName.equals("transaction-support") && value.equals("XATransaction"))
         tsmd.setTransactionSupport(TransactionSupportMetaData.XATransaction);
      else
         throw new IllegalArgumentException("Unknown transaction support setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
   }

   /**
    * authentication mechanism child elements
    * 
    * @param ammd the authentication mechanism meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param attrs the attributes
    */
   public Object newChild(AuthenticationMechanismMetaData ammd, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if (trace)
         log.trace("authentication mechanism newChild: nuri=" + namespaceURI + " localName=" + localName + " attrs=" + attrs);
      if (localName.equals("authentication-mechanism") ||
          localName.equals("authentication-mechanism-type") ||
          localName.equals("credential-interface"))
      {
         return null;
      }
      else if (localName.equals("description"))
      {
         String language = attrs.getValue("xml:lang");
         DescriptionMetaData dmd = null;
         if (language == null)
            dmd = ammd.getDescription();
         else
            dmd = ammd.getDescription(language);
         if (dmd == null)
            dmd = new DescriptionMetaData(language);
         ammd.addDescription(dmd);
         return dmd;
      }
      
      throw new IllegalArgumentException("Unknown authentication mechanism newChild: nuri=" +namespaceURI + " localName=" + localName + " attrs=" + attrs);
   }

   /**
    * authentication mechanism elements
    * 
    * @param ammd the authentication mechanism meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param value the value
    */
   public void setValue(AuthenticationMechanismMetaData ammd, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (trace)
         log.trace("authentication mechanism setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
      if (localName.equals("authentication-mechanism"))
      {
      }
      else if (localName.equals("authentication-mechanism-type"))
         ammd.setAuthenticationMechansimType(value);
      else if (localName.equals("credential-interface"))
         ammd.setCredentialInterfaceClass(value);
      else
         throw new IllegalArgumentException("Unknown authentication mechanism setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
   }

   /**
    * message listener child elements
    * 
    * @param mlmd the message listener meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param attrs the attributes
    */
   public Object newChild(MessageListenerMetaData mlmd, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if (trace)
         log.trace("message listener newChild: nuri=" + namespaceURI + " localName=" + localName + " attrs=" + attrs);
      if (localName.equals("messagelistener-type") ||
          localName.equals("activationspec") ||
          localName.equals("activationspec-class"))
      {
         return null;
      }
      else if (localName.equals("required-config-property"))
      {
         RequiredConfigPropertyMetaData rcpmd = new RequiredConfigPropertyMetaData();
         mlmd.addRequiredConfigProperty(rcpmd);
         return rcpmd;
      }
      
      throw new IllegalArgumentException("Unknown message listener newChild: nuri=" +namespaceURI + " localName=" + localName + " attrs=" + attrs);
   }

   /**
    * message listener elements
    * 
    * @param mlmd the description meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param value the value
    */
   public void setValue(MessageListenerMetaData mlmd, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (trace)
         log.trace("message listener setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
      if (localName.equals("messagelistener") ||
          localName.equals("activationspec"))
      {
      }
      else if (localName.equals("messagelistener-type"))
         mlmd.setType(value);
      else if (localName.equals("activationspec-class"))
         mlmd.setActivationSpecType(value);
      else
         throw new IllegalArgumentException("Unknown mesasge listener setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
   }

   /**
    * admin object child elements
    * 
    * @param aomd the admin object meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param attrs the attributes
    */
   public Object newChild(AdminObjectMetaData aomd, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if (trace)
         log.trace("admin object newChild: nuri=" + namespaceURI + " localName=" + localName + " attrs=" + attrs);
      if (localName.equals("adminobject") ||
          localName.equals("adminobject-interface") ||
          localName.equals("adminobject-class"))
      {
         return null;
      }
      else if (localName.equals("config-property"))
      {
         ConfigPropertyMetaData cpmd = new ConfigPropertyMetaData();
         aomd.addProperty(cpmd);
         return cpmd;
      }
      
      throw new IllegalArgumentException("Unknown admin object newChild: nuri=" +namespaceURI + " localName=" + localName + " attrs=" + attrs);
   }

   /**
    * admin object definition elements
    * 
    * @param aomd the admin object meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param value the value
    */
   public void setValue(AdminObjectMetaData aomd, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (trace)
         log.trace("admin object setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
      if (localName.equals("adminobject"))
      {
      }
      else if (localName.equals("adminobject-interface"))
         aomd.setAdminObjectInterfaceClass(value);
      else if (localName.equals("adminobject-class"))
         aomd.setAdminObjectImplementationClass(value);
      else
         throw new IllegalArgumentException("Unknown admin object setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
   }

   /**
    * security permission child elements
    * 
    * @param spmd the security permission meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param attrs the attributes
    */
   public Object newChild(SecurityPermissionMetaData spmd, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if (trace)
         log.trace("security permission newChild: nuri=" + namespaceURI + " localName=" + localName + " attrs=" + attrs);
      if (localName.equals("security-permission") ||
          localName.equals("security-permission-spec"))
      {
         return null;
      }
      else if (localName.equals("description"))
      {
         String language = attrs.getValue("xml:lang");
         DescriptionMetaData dmd = null;
         if (language == null)
            dmd = spmd.getDescription();
         else
            dmd = spmd.getDescription(language);
         if (dmd == null)
            dmd = new DescriptionMetaData(language);
         spmd.addDescription(dmd);
         return dmd;
      }
      
      throw new IllegalArgumentException("Unknown security permission newChild: nuri=" +namespaceURI + " localName=" + localName + " attrs=" + attrs);
   }

   /**
    * security permission elements
    * 
    * @param spmd the security permission meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param value the value
    */
   public void setValue(SecurityPermissionMetaData spmd, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (trace)
         log.trace("security permission setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
      if (localName.equals("security-permission"))
      {
      }
      else if (localName.equals("security-permission-spec"))
         spmd.setSecurityPermissionSpec(value);
      else
         throw new IllegalArgumentException("Unknown security permission setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
   }

   /**
    * required config property child elements
    * 
    * @param rcpmd the config property meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param attrs the attributes
    */
   public Object newChild(RequiredConfigPropertyMetaData rcpmd, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if (trace)
         log.trace("required config property newChild: nuri=" + namespaceURI + " localName=" + localName + " attrs=" + attrs);
      if (localName.equals("config-property-name"))
      {
         return null;
      }
      else if (localName.equals("description"))
      {
         String language = attrs.getValue("xml:lang");
         DescriptionMetaData dmd = null;
         if (language == null)
            dmd = rcpmd.getDescription();
         else
            dmd = rcpmd.getDescription(language);
         if (dmd == null)
            dmd = new DescriptionMetaData(language);
         rcpmd.addDescription(dmd);
         return dmd;
      }
      
      throw new IllegalArgumentException("Unknown required config property newChild: nuri=" +namespaceURI + " localName=" + localName + " attrs=" + attrs);
   }

   /**
    * required config property elements
    * 
    * @param rcpmd the required config property meta data
    * @param navigator the content navigator
    * @param namespaceURI the namespace of the element
    * @param localName the local name of the element
    * @param value the value
    */
   public void setValue(RequiredConfigPropertyMetaData rcpmd, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (trace)
         log.trace("required config property setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
      if (localName.equals("required-config-property"))
      {
      }
      else if (localName.equals("config-property-name"))
         rcpmd.setName(value);
      else
         throw new IllegalArgumentException("Unknown required config property setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
   }

   public Object newRoot(Object root,
                               UnmarshallingContext navigator,
                               String namespaceURI,
                               String localName,
                               Attributes attrs)
   {
      if (!localName.equals("connector"))
      {
         throw new IllegalStateException("Unexpected root element: was expecting 'connector' but got '" + localName + "'");
      }

      final ConnectorMetaData cmd = new ConnectorMetaData();
      String version = attrs.getValue("version");
      if (version != null)
      {
         cmd.setVersion(version);
      }
      return cmd;
   }

   public Object completeRoot(Object root, UnmarshallingContext ctx, String uri, String name)
   {
      return root;
   }
}
