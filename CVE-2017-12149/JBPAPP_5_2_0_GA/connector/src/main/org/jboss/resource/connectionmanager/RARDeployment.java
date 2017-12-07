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

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;

import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.metadata.MetaData;
import org.jboss.resource.deployment.ConfigProperty;
import org.jboss.resource.deployment.ConfigPropertyHandler;
import org.jboss.resource.metadata.ConfigPropertyMetaData;
import org.jboss.resource.metadata.ConnectionDefinitionMetaData;
import org.jboss.resource.metadata.ConnectorMetaData;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.Classes;
import org.jboss.util.NestedRuntimeException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The RARDeployment mbean manages instantiation and configuration of a
 * ManagedConnectionFactory instance. It is intended to be configured
 * primarily by xslt transformation of the ra.xml from a jca adapter.
 * Until that is implemented, it uses the old RARDeployment and RARDeployer
 * mechanism to obtain information from the ra.xml.  Properties for the
 * ManagedConectionFactory should be supplied with their values in the
 * ManagedConnectionFactoryProperties element.
 *
 * @author <a href="toby.allsopp@peace.com">Toby Allsopp</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 113110 $
 */
public class RARDeployment extends ServiceMBeanSupport
   implements RARDeploymentMBean, ManagedConnectionFactory
{
   static final long serialVersionUID = -294341806721616790L;

   public final static String MCF_ATTRIBUTE_CHANGED_NOTIFICATION = "jboss.mcfattributechangednotification";

   private Logger log = Logger.getLogger(getClass());

   //Hack to use previous ra.xml parsing code until xslt deployment is written.
   private ObjectName oldRarDeployment;

   private String rarName;

   private String connectionDefinition;

   private String vendorName;

   private String specVersion;

   private String eisType;

   private String version;

   private String managedConnectionFactoryClass;

   private String connectionFactoryInterface;

   private String connectionFactoryImplClass;

   private String connectionInterface;

   private String connectionImplClass;

   private String transactionSupport;

   private Element managedConnectionFactoryProperties;

   private String authenticationMechanismType;

   private String credentialInterface;

   private boolean reauthenticationSupport;

   private Class mcfClass;

   private ManagedConnectionFactory mcf;

   private ConfigPropertyHandler configPropertyHandler;

   /**
    * Default managed constructor for RARDeployment mbeans.
    */
   public RARDeployment()
   {
   }

   public ObjectName getOldRarDeployment()
   {
      return oldRarDeployment;
   }

   public void setOldRarDeployment(final ObjectName oldRarDeployment)
   {
      this.oldRarDeployment = oldRarDeployment;
   }

   public String getRARName()
   {
      return rarName;
   }

   public void setRARName(String rarName)
   {
      this.rarName = rarName;
   }

   public String getConnectionDefinition()
   {
      return connectionDefinition;
   }

   public void setConnectionDefinition(String connectionDefinition)
   {
      this.connectionDefinition = connectionDefinition;
   }

   public String getVendorName()
   {
      return vendorName;
   }

   public void setVendorName(String vendorName)
   {
      this.vendorName = vendorName;
   }

   public String getSpecVersion()
   {
      return specVersion;
   }

   public void setSpecVersion(String specVersion)
   {
      this.specVersion = specVersion;
   }

   public String getEisType()
   {
      return eisType;
   }

   public void setEisType(String eisType)
   {
      this.eisType = eisType;
   }

   public String getVersion()
   {
      return version;
   }

   public void setVersion(String version)
   {
      this.version = version;
   }

   public String getManagedConnectionFactoryClass()
   {
      return managedConnectionFactoryClass;
   }

   public void setManagedConnectionFactoryClass(final String managedConnectionFactoryClass)
   {
      this.managedConnectionFactoryClass = managedConnectionFactoryClass;
   }

   public String getConnectionFactoryInterface()
   {
      return connectionFactoryInterface;
   }

   public void setConnectionFactoryInterface(String connectionFactoryInterface)
   {
      this.connectionFactoryInterface = connectionFactoryInterface;
   }

   public String getConnectionFactoryImplClass()
   {
      return connectionFactoryImplClass;
   }

   public void setConnectionFactoryImplClass(String connectionFactoryImplClass)
   {
      this.connectionFactoryImplClass = connectionFactoryImplClass;
   }

   public String getConnectionInterface()
   {
      return connectionInterface;
   }

   public void setConnectionInterface(String connectionInterface)
   {
      this.connectionInterface = connectionInterface;
   }

   public String getConnectionImplClass()
   {
      return connectionImplClass;
   }

   public void setConnectionImplClass(String connectionImplClass)
   {
      this.connectionImplClass = connectionImplClass;
   }

   public String getTransactionSupport()
   {
      return transactionSupport;
   }

   public void setTransactionSupport(String transactionSupport)
   {
      this.transactionSupport = transactionSupport;
   }

   public Element getManagedConnectionFactoryProperties()
   {
      return managedConnectionFactoryProperties;
   }

   public void setManagedConnectionFactoryProperties(Element managedConnectionFactoryProperties)
   {
      this.managedConnectionFactoryProperties = managedConnectionFactoryProperties;
   }

   public String getAuthenticationMechanismType()
   {
      return authenticationMechanismType;
   }

   public void setAuthenticationMechanismType(String authenticationMechanismType)
   {
      this.authenticationMechanismType = authenticationMechanismType;
   }

   public String getCredentialInterface()
   {
      return credentialInterface;
   }

   public void setCredentialInterface(String credentialInterface)
   {
      this.credentialInterface = credentialInterface;
   }

   public boolean isReauthenticationSupport()
   {
      return reauthenticationSupport;
   }

   public void setReauthenticationSupport(boolean reauthenticationSupport)
   {
      this.reauthenticationSupport = reauthenticationSupport;
   }

   public ManagedConnectionFactory getMcfInstance()
   {
      return mcf;
   }

   protected void startService() throws Exception
   {
      if (mcf != null)
         throw new DeploymentException("Stop the RARDeployment before restarting it");

      ConnectorMetaData cmd = null;
      ConnectionDefinitionMetaData cdmd = null;
      ResourceAdapter resourceAdapter = null;
      if (oldRarDeployment != null)
      {
         try
         {
            resourceAdapter = (ResourceAdapter) getServer().getAttribute(oldRarDeployment, "ResourceAdapter");
            cmd = (ConnectorMetaData) getServer().getAttribute(oldRarDeployment, "MetaData");
            cdmd = cmd.getConnectionDefinition(connectionDefinition);
            if (cdmd == null)
               throw new DeploymentException("ConnectionDefinition '" + connectionDefinition + "' not found in rar '" + rarName + "'");
            setManagedConnectionFactoryClass(cdmd.getManagedConnectionFactoryClass());
            setReauthenticationSupport(cmd.getReauthenticationSupport());
         }
         catch (Exception e)
         {
            throw new DeploymentException("couldn't get oldRarDeployment! " + oldRarDeployment, e);
         }
      }
      try
      {
         mcfClass = Thread.currentThread().getContextClassLoader().loadClass(managedConnectionFactoryClass);
      }
      catch (ClassNotFoundException cnfe)
      {
         log.error("Could not find ManagedConnectionFactory class: " + managedConnectionFactoryClass, cnfe);
         throw new DeploymentException("Could not find ManagedConnectionFactory class: "
               + managedConnectionFactoryClass);
      }
      try
      {
         mcf = (ManagedConnectionFactory) mcfClass.newInstance();
      }
      catch (Exception e)
      {
         log.error("Could not instantiate ManagedConnectionFactory: " + managedConnectionFactoryClass, e);
         throw new DeploymentException("Could not instantiate ManagedConnectionFactory: "
               + managedConnectionFactoryClass);
      }
      if (cmd != null)
      {
         // Set the resource adapter properties
         setMcfProperties(cmd.getProperties(), false);
         // Set the connection definition properties
         setMcfProperties(cdmd.getProperties(), true);
      }
      //set overridden properties;
      setMcfProperties(managedConnectionFactoryProperties);

      if (resourceAdapter != null && mcf instanceof ResourceAdapterAssociation)
      {
         ResourceAdapterAssociation raa = (ResourceAdapterAssociation) mcf;
         raa.setResourceAdapter(resourceAdapter);
      }
   }

   protected void stopService()
   {
      mcf = null;
      mcfClass = null;
   }

   public void setManagedConnectionFactoryAttribute(String name, Class clazz, Object value)
   {
      setManagedConnectionFactoryAttribute(name, clazz, value, false);
   }

   protected void setManagedConnectionFactoryAttribute(String name, Class clazz, Object value, boolean mustExist)
   {
      try
      {
         getConfigPropertyHandler().handle(new ConfigProperty(name, clazz, value), mustExist);
      }
      catch (Exception e)
      {
         String error = "Unable to set property '" + name + "' " + "on object '" + mcf + "'";
         if (e instanceof InvocationTargetException)
            throw new NestedRuntimeException(error, ((InvocationTargetException) e).getCause());
         else
            throw new NestedRuntimeException(error, e);
      }
      sendNotification(new Notification(MCF_ATTRIBUTE_CHANGED_NOTIFICATION, getServiceName(),
            getNextNotificationSequenceNumber()));
   }

   public Object getManagedConnectionFactoryAttribute(String name)
   {
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("Null or empty attribute name " + name);
      String getterName = "get" + Character.toUpperCase(name.charAt(0));
      if (name.length() > 1)
         getterName = getterName.concat(name.substring(1));
      Method getter;
      try
      {
         getter = mcfClass.getMethod(getterName, new Class[] {});
      }
      catch (NoSuchMethodException e)
      {
         String msg = "The class '" + mcfClass + "' has no getter("
            + getterName + ") for config property '" + name + "'";
         log.debug(msg, e);
         throw new IllegalArgumentException(msg);
      }
      try
      {
         Object value = getter.invoke(mcf, new Object[]{});
         log.debug("get property " + name + ": value " + value);
         return value;
      }
      catch (Exception e)
      {
         String error = "Unable to invoke getter method '" + getter + "' " + "on object '" + mcf + "'";
         log.debug(error, e);
         if (e instanceof InvocationTargetException)
            throw new NestedRuntimeException(error, ((InvocationTargetException) e).getCause());
         else
            throw new NestedRuntimeException(error, e);
      }
   }

   protected void setMcfProperties(Collection properties, boolean mustExist) throws DeploymentException
   {
      for (Iterator i = properties.iterator(); i.hasNext();)
      {
         ConfigPropertyMetaData cpmd = (ConfigPropertyMetaData) i.next();
         String name = cpmd.getName();
         String value = cpmd.getValue();
         try
         {
            getConfigPropertyHandler().handle(cpmd, mustExist);
         }
         catch (Exception e)
         {
            String error = "Unable to set property '" + name + "' " + "on object '" + mcf + "'";
            if (e instanceof InvocationTargetException)
               throw new NestedRuntimeException(error, ((InvocationTargetException) e).getCause());
            else
               throw new NestedRuntimeException(error, e);
         }
         sendNotification(new Notification(MCF_ATTRIBUTE_CHANGED_NOTIFICATION, getServiceName(),
               getNextNotificationSequenceNumber()));
      }
   }

   protected void setMcfProperties(Element mcfProps) throws DeploymentException
   {
      if (mcfProps == null)
         return;
      // the properties that the deployment descriptor says we need to set
      NodeList props = mcfProps.getChildNodes();
      for (int i = 0; i < props.getLength(); i++)
      {
         if (props.item(i).getNodeType() == Node.ELEMENT_NODE)
         {
            Element prop = (Element) props.item(i);
            if (prop.getTagName().equals("config-property"))
            {
               String name = null;
               String type = null;
               String value = null;
               //Support for more friendly config style
               //<config-property name="" type=""></config-property>
               if (prop.hasAttribute("name"))
               {
                  name = prop.getAttribute("name");
                  type = prop.getAttribute("type");
                  value = MetaData.getElementContent(prop, null, false);
               }
               else
               {
                  name = MetaData.getElementContent(MetaData.getUniqueChild(prop, "config-property-name"));
                  type = MetaData.getElementContent(MetaData.getUniqueChild(prop, "config-property-type"));
                  value = MetaData.getElementContent(MetaData.getOptionalChild(prop, "config-property-value"), null, false);
               }
               if (name == null || name.length() == 0 || value == null || value.length() == 0)
               {
                  log.debug("Not setting config property '" + name + "'");
                  continue;
               }
               if (type == null || type.length() == 0)
               {
                  // Default to String for convenience.
                  type = "java.lang.String";
               }
               // see if it is a primitive type first
               Class clazz = Classes.getPrimitiveTypeForName(type);
               if (clazz == null)
               {
                  //not primitive, look for it.
                  try
                  {
                     clazz = Thread.currentThread().getContextClassLoader().loadClass(type);
                  }
                  catch (ClassNotFoundException cnfe)
                  {
                     log.warn("Unable to find class '" + type + "' for " + "property '" + name
                           + "' - skipping property.");
                     continue;
                  }
               }
               PropertyEditor pe = PropertyEditorFinder.getInstance().find(clazz);
               if (pe == null)
               {
                  log.warn("Unable to find a PropertyEditor for class '" + clazz + "' of property '" + name + "' - "
                        + "skipping property");
                  continue;
               }
               log.debug("setting property: " + name + " to value " + value);
               try
               {
                  pe.setAsText(value);
               }
               catch (IllegalArgumentException iae)
               {
                  log.warn("Value '" + value + "' is not valid for property '" + name + "' of class '" + clazz
                        + "' - skipping " + "property");
                  continue;
               }
               Object v = pe.getValue();
               setManagedConnectionFactoryAttribute(name, clazz, v);
            }
         }
      }
   }

   public Object createConnectionFactory() throws ResourceException
   {
      return mcf.createConnectionFactory();
   }

   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      return mcf.createConnectionFactory(cxManager);
   }

   public ManagedConnection createManagedConnection(javax.security.auth.Subject subject,
         ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      return mcf.createManagedConnection(subject, cxRequestInfo);
   }

   public boolean equals(Object other)
   {
      return mcf.equals(other);
   }

   public java.io.PrintWriter getLogWriter() throws ResourceException
   {
      return mcf.getLogWriter();
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(getClass().getName());
      buffer.append('@');
      buffer.append(Integer.toHexString(System.identityHashCode(this)));
      return buffer.toString();
   }

   public int hashCode()
   {
      return mcf.hashCode();
   }

   public ManagedConnection matchManagedConnections(java.util.Set connectionSet, javax.security.auth.Subject subject,
         ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      return mcf.matchManagedConnections(connectionSet, subject, cxRequestInfo);
   }

   public void setLogWriter(java.io.PrintWriter out) throws ResourceException
   {
      mcf.setLogWriter(out);
   }
   
   protected ConfigPropertyHandler getConfigPropertyHandler()
   {
      if (configPropertyHandler == null)
      {
         configPropertyHandler = new ConfigPropertyHandler(mcf, mcfClass);
      }
      return configPropertyHandler;
   }
}
