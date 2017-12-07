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
package org.jboss.resource.deployers;

import java.net.URL;
import java.util.Collection;
import java.util.Timer;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;

import org.jboss.bootstrap.spi.util.ServerConfigUtil;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.resource.deployment.ActivationSpecFactory;
import org.jboss.resource.deployment.ResourceAdapterFactory;
import org.jboss.resource.metadata.ConfigPropertyMetaData;
import org.jboss.resource.metadata.ConnectorMetaData;
import org.jboss.resource.metadata.DescriptionGroupMetaData;
import org.jboss.resource.metadata.JBossRAMetaData;
import org.jboss.resource.metadata.MessageListenerMetaData;
import org.jboss.resource.metadata.RARDeploymentMetaData;
import org.jboss.system.ServiceDynamicMBeanSupport;

/**
 * A resource adapter deployment
 *
 * @author  <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class RARDeployment extends ServiceDynamicMBeanSupport implements BootstrapContext
{
   /** The RARDeploymentMetaData */
   protected RARDeploymentMetaData rdmd;
   
   /** The ConnectorMetaData */
   protected ConnectorMetaData cmd;
   
   /** The JBossRAMetaData */
   protected JBossRAMetaData ramd;
   
   /** The resource adapter */
   protected ResourceAdapter resourceAdapter;

   /** The work manager */
   private WorkManager workManager;

   /** The xa terminator */
   private XATerminator xaTerminator;
   
   /**
    * Create a new RAR deployment
    * 
    * @param rdmd the metadata
    */
   public RARDeployment(RARDeploymentMetaData rdmd)
   {
	  if (rdmd == null)
		  throw new IllegalArgumentException("Null metadata");
	  this.rdmd = rdmd;
	  this.cmd = rdmd.getConnectorMetaData(); 
      this.ramd = rdmd.getRaXmlMetaData();      
   }

   public Timer createTimer() throws UnavailableException
   {
      return new Timer(true);
   }

   /**
    * Get the workManager.
    * 
    * @return the workManager.
    */
   public WorkManager getWorkManager()
   {
      return workManager;
   }

   /**
    * Set the workManager.
    * 
    * @param workManager the workManager.
    */
   public void setWorkManager(WorkManager workManager)
   {
      this.workManager = workManager;
   }

   /**
    * Get the XATerminator.
    * 
    * @return the xaTerminator.
    */
   public XATerminator getXATerminator()
   {
      return xaTerminator;
   }

   /**
    * Set the xXATerminator.
    * 
    * @param xaTerminator the XATerminator.
    */
   public void setXATerminator(XATerminator xaTerminator)
   {
      this.xaTerminator = xaTerminator;
   }

   protected void startService() throws Exception
   {
      URL url = cmd.getURL();      
      if (cmd.getLicense().getRequired())
      {    	  
         log.info ("Required license terms exist, view " + ServerConfigUtil.shortUrlFromServerHome(url.toString()));         
         log.debug("License terms full URL: " + url);
      }           
      resourceAdapter = ResourceAdapterFactory.createResourceAdapter(rdmd);
      resourceAdapter.start(this);
   }
   
   protected void stopService() throws Exception
   {
      resourceAdapter.stop();
   }
   
   protected String getInternalDescription()
   {
      String description = null;
      DescriptionGroupMetaData dgmd = cmd.getDescription();
      if (dgmd != null)
         description = dgmd.getDescription();
      if (description == null)
         description = "RAR Deployment " + cmd.getURL();
      return description;
   }

   protected MBeanAttributeInfo[] getInternalAttributeInfo()
   {
      Collection<ConfigPropertyMetaData> properties = cmd.getProperties();
      MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[13+properties.size()];
      attrs[0] = new MBeanAttributeInfo("MetaData", ConnectorMetaData.class.getName(), "The meta data", true, false, false);
      attrs[1] = new MBeanAttributeInfo("AuthenticationMechanism", String.class.getName(), "The authentication mechanism", true, false, false);
      attrs[2] = new MBeanAttributeInfo("EISType", String.class.getName(), "The EIS type", true, false, false);
      attrs[3] = new MBeanAttributeInfo("License", String.class.getName(), "The license", true, false, false);
      attrs[4] = new MBeanAttributeInfo("RAClass", String.class.getName(), "The resource adapter class", true, false, false);
      attrs[5] = new MBeanAttributeInfo("RAVersion", String.class.getName(), "The resource adapter version", true, false, false);
      attrs[6] = new MBeanAttributeInfo("TransactionSupport", String.class.getName(), "The transaction support", true, false, false);
      attrs[7] = new MBeanAttributeInfo("VendorName", String.class.getName(), "The vendor name", true, false, false);
      attrs[8] = new MBeanAttributeInfo("Version", String.class.getName(), "The spec version", true, false, false);
      attrs[9] = new MBeanAttributeInfo("ReauthenticationSupport", Boolean.TYPE.getName(), "Whether reauthentication support is supported", true, false, false);
      attrs[10] = new MBeanAttributeInfo("ResourceAdapter", ResourceAdapter.class.getName(), "The resource adapter instance", true, false, false);
      attrs[11] = new MBeanAttributeInfo("WorkManager", WorkManager.class.getName(), "The work manager", true, true, false);
      attrs[12] = new MBeanAttributeInfo("XATerminator", XATerminator.class.getName(), "The xa terminator", true, true, false);
      int n = 13;
      for (ConfigPropertyMetaData cpmd : properties)
      {
         attrs[n++] = new MBeanAttributeInfo(cpmd.getName(), cpmd.getType(), cpmd.getDescription().getDescription(), true, false, false);
      }
      return attrs;
   }
   
   protected Object getInternalAttribute(String attribute)
      throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      if ("MetaData".equals(attribute))
         return cmd;
      else if ("AuthenticationMechanism".equals(attribute))
         return cmd.getAuthenticationMechanism().getAuthenticationMechansimType();
      else if ("EISType".equals(attribute))
         return cmd.getEISType();
      else if ("License".equals(attribute))
         return cmd.getLicense().getDescription().getDescription();
      else if ("RAClass".equals(attribute))
         return cmd.getRAClass();
      else if ("RAVersion".equals(attribute))
         return cmd.getRAVersion();
      else if ("TransactionSupport".equals(attribute))
         return cmd.getTransactionSupport();
      else if ("VendorName".equals(attribute))
         return cmd.getVendorName();
      else if ("Version".equals(attribute))
         return cmd.getVersion();
      else if ("ReauthenticationSupport".equals(attribute))
         return new Boolean(cmd.getReauthenticationSupport());
      else if ("ResourceAdapter".equals(attribute))
         return resourceAdapter;
      else if ("WorkManager".equals(attribute))
         return workManager;
      else if ("XATerminator".equals(attribute))
         return xaTerminator;
      Object property = cmd.getProperty(attribute);
      if (property != null)
         return property;
      
      return super.getInternalAttribute(attribute);
   }

   protected void setInternalAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      String name = attribute.getName();
      if ("WorkManager".equals(name))
         this.workManager = (WorkManager) attribute.getValue();
      else if ("XATerminator".equals(name))
         this.xaTerminator = (XATerminator) attribute.getValue();
      else
         super.setInternalAttribute(attribute);
   }

   protected MBeanOperationInfo[] getInternalOperationInfo()
   {
      MBeanOperationInfo[] ops = new MBeanOperationInfo[3];

      MBeanParameterInfo[] createActivationSpecParams = new MBeanParameterInfo[]
      {
         new MBeanParameterInfo("MessagingType", Class.class.getName(), "The type of the message listener"),
         new MBeanParameterInfo("ActivationConfig", Collection.class.getName(), "A collection of activation config properties")
      };
      ops[0] = new MBeanOperationInfo("createActivationSpec", "Create an activation spec",
            createActivationSpecParams, ActivationSpec.class.getName(), MBeanOperationInfo.ACTION);

      MBeanParameterInfo[] activationParams = new MBeanParameterInfo[]
      {
         new MBeanParameterInfo("MessageEndpointFactory", MessageEndpointFactory.class.getName(), "The message endpoint factory"),
         new MBeanParameterInfo("ActivationSpec", ActivationSpec.class.getName(), "The activation spec")
      };
      ops[1] = new MBeanOperationInfo("endpointActivation", "Active the endpoint",
            activationParams, Void.class.getName(), MBeanOperationInfo.ACTION);
      ops[2] = new MBeanOperationInfo("endpointDeactivation", "Deactive the endpoint",
            activationParams, Void.class.getName(), MBeanOperationInfo.ACTION);

      return ops;
   }
   
   @SuppressWarnings("unchecked")
   protected Object internalInvoke(String actionName, Object[] params, String[] signature) throws MBeanException,
         ReflectionException
   {
      if ("createActivationSpec".equals(actionName))
      {
         if (params.length != 2)
            throw new IllegalArgumentException("Wrong number of parameters for " + actionName);
         Class messagingType = (Class) params[0];
         Collection activationConfig = (Collection) params[1];
         return createActivationSpec(messagingType, activationConfig);
      }
      else if ("endpointActivation".equals(actionName))
      {
         if (params.length != 2)
            throw new IllegalArgumentException("Wrong number of parameters for " + actionName);
         MessageEndpointFactory messageEndpointFactory = (MessageEndpointFactory) params[0];
         ActivationSpec activationSpec = (ActivationSpec) params[1];
         endpointActivation(messageEndpointFactory, activationSpec);
         return null;
      }
      else if ("endpointDeactivation".equals(actionName))
      {
         if (params.length != 2)
            throw new IllegalArgumentException("Wrong number of parameters for " + actionName);
         MessageEndpointFactory messageEndpointFactory = (MessageEndpointFactory) params[0];
         ActivationSpec activationSpec = (ActivationSpec) params[1];
         endpointDeactivation(messageEndpointFactory, activationSpec);
         return null;
      }
      return super.internalInvoke(actionName, params, signature);
   }

   @SuppressWarnings("unchecked")
   protected ActivationSpec createActivationSpec(Class messagingType, Collection activationConfig) throws MBeanException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("CreateActivateSpec rar=" + getServiceName() + " messagingType=" + messagingType.getName() + " activationConfig=" + activationConfig);
      
      try
      {
         // Find the meta data
         MessageListenerMetaData mlmd = cmd.getMessageListener(messagingType.getName());
         if (mlmd == null)
            throw new DeploymentException("MessagingType '" + messagingType.getName() + "' not found in resource deployment " + getServiceName());
         
         return ActivationSpecFactory.createActivationSpec(getServiceName(), messagingType.getName(), activationConfig, mlmd);
      }
      catch (Exception e)
      {
         throw new MBeanException(e, "Error in create activation spec " + getServiceName());
      }
   }

   protected void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) throws MBeanException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("EndpointActivation rar=" + getServiceName() + " messagingEndpointFactory=" + messageEndpointFactory + " activationSpec=" + activationSpec);

      try
      {
         activationSpec.setResourceAdapter(resourceAdapter);
         resourceAdapter.endpointActivation(messageEndpointFactory, activationSpec);
      }
      catch (Exception e)
      {
         throw new MBeanException(e, "Error in endpoint activation " + getServiceName());
      }
   }

   protected void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) throws MBeanException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("EndpointDeactivation rar=" + getServiceName() + " messagingEndpointFactory=" + messageEndpointFactory + " activationSpec=" + activationSpec);

      try
      {
         resourceAdapter.endpointDeactivation(messageEndpointFactory, activationSpec);
      }
      catch (Exception e)
      {
         throw new MBeanException(e, "Error in endpoint deactivation " + getServiceName());
      }
   }
}
