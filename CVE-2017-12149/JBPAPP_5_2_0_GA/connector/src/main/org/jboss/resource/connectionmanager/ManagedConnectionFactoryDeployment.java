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
import java.io.File;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.resource.connectionmanager.xa.XAResourceWrapperImpl;
import org.jboss.resource.metadata.ConfigPropertyMetaData;
import org.jboss.resource.metadata.ConnectionDefinitionMetaData;
import org.jboss.resource.metadata.ConnectorMetaData;
import org.jboss.resource.metadata.DescriptionGroupMetaData;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryPropertyMetaData;
import org.jboss.resource.metadata.mcf.SecurityMetaData;
import org.jboss.resource.metadata.mcf.XAConnectionPropertyMetaData;
import org.jboss.resource.metadata.mcf.XADataSourceDeploymentMetaData;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.SubjectFactory;
import org.jboss.system.ServiceDynamicMBeanSupport;
import org.jboss.tm.XAResourceRecovery;
import org.jboss.tm.XAResourceRecoveryRegistry;
import org.jboss.util.Classes;
import org.jboss.util.NestedRuntimeException;
import org.jboss.util.StringPropertyReplacer;
import org.w3c.dom.Element;


/**
 * A ManagedConnectionFactoryDeployment.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 113342 $
 */
public class ManagedConnectionFactoryDeployment extends ServiceDynamicMBeanSupport 
   implements ManagedConnectionFactory, XAResourceRecovery
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -8448602289610025849L;

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

   private String jndiName;
   
   private ManagedConnectionFactory mcf;
   
   private ManagedConnectionFactoryDeploymentMetaData dmd;
   
   private ConnectorMetaData cmd;

   private boolean recoveryRegistered = false;
   private XAResourceRecoveryRegistry xrrr = null;
   private SubjectFactory subjectFactory = null;
   private ManagedConnection recoverMC = null;
   private String recoverSecurityDomain = null;
   private String recoverUserName = null;
   private String recoverPassword = null;
   private String connectionManager = null;
   
   /**
    * Default managed constructor for RARDeployment mbeans.
    */
   public ManagedConnectionFactoryDeployment()
   {
   }
   
   public ManagedConnectionFactoryDeployment(ConnectorMetaData cmd, 
                                             ManagedConnectionFactoryDeploymentMetaData dmd,
                                             String connectionManager)
   {
      this.cmd = cmd;
      this.dmd = dmd;
      this.connectionManager = connectionManager;
   }
   
   public String getJndiName()
   {
      return this.jndiName;
   }
   
   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
      
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

   public XAResourceRecoveryRegistry getXAResourceRecoveryRegistry()
   {
      return xrrr;
   }

   public void setXAResourceRecoveryRegistry(XAResourceRecoveryRegistry xrrr)
   {
      this.xrrr = xrrr;
   }

   public SubjectFactory getSubjectFactory()
   {
      return subjectFactory;
   }

   public void setSubjectFactory(SubjectFactory sf)
   {
      this.subjectFactory = sf;
   }

   public String getConnectionManager()
   {
      return connectionManager;
   }

   protected void startService() throws Exception
   {
      if (mcf != null)
         throw new DeploymentException("Stop the RARDeployment before restarting it");

      ResourceAdapter resourceAdapter = null;
      ConnectionDefinitionMetaData cdmd = cmd.getConnectionDefinition(dmd.getConnectionDefinition());
      rarName = dmd.getRarName();
      
      try
      {
         resourceAdapter = (ResourceAdapter) getServer().getAttribute(oldRarDeployment, "ResourceAdapter");

         if (cdmd == null)
            throw new DeploymentException("ConnectionDefinition '" + connectionDefinition + "' not found in rar '"
                  + rarName + "'");

         setManagedConnectionFactoryClass(cdmd.getManagedConnectionFactoryClass());
         setReauthenticationSupport(cmd.getReauthenticationSupport());
      }
      catch (Exception e)
      {
         throw new DeploymentException("couldn't get oldRarDeployment! " + oldRarDeployment, e);
      }
      
      try
      {
         mcfClass = Thread.currentThread().getContextClassLoader().loadClass(cdmd.getManagedConnectionFactoryClass());
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
      
      setManagedConnectionFactoryProperties(dmd.getManagedConnectionFactoryProperties());

      if (resourceAdapter != null && mcf instanceof ResourceAdapterAssociation)
      {
         ResourceAdapterAssociation raa = (ResourceAdapterAssociation) mcf;
         raa.setResourceAdapter(resourceAdapter);
      }

      if (dmd instanceof XADataSourceDeploymentMetaData)
      {
         XADataSourceDeploymentMetaData xdsdm = (XADataSourceDeploymentMetaData)dmd;

         if (getXAResourceRecoveryRegistry() != null && !xdsdm.isNoRecover())
         {
            // If we have an XAResourceRecoveryRegistry and the deployment is XA
            // lets register it for XA Resource Recovery using the "recover" definitions
            // from the -ds.xml file. Fallback to the standard definitions for
            // user name, password. Keep a seperate reference to the security-domain

            SecurityMetaData recoverSecurityDomainMD = xdsdm.getRecoverSecurityMetaData();
            if (recoverSecurityDomainMD != null)
               recoverSecurityDomain = recoverSecurityDomainMD.getDomain();

            SecurityMetaData securityDomainMD = xdsdm.getSecurityMetaData();
            String securityDomain = null;

            if (securityDomainMD != null)
               securityDomain = securityDomainMD.getDomain();

            recoverUserName = xdsdm.getRecoverUserName();
            recoverPassword = xdsdm.getRecoverPassWord();

            if (recoverUserName == null)
            {
               for (XAConnectionPropertyMetaData xcpmd : xdsdm.getXADataSourceProperties())
               {
                  if (xcpmd.getName().equals("User"))
                     recoverUserName = xcpmd.getValue();
               }
            }

            if (recoverPassword == null)
            {
               for (XAConnectionPropertyMetaData xcpmd : xdsdm.getXADataSourceProperties())
               {
                  if (xcpmd.getName().equals("Password"))
                     recoverPassword = xcpmd.getValue();
               }
            }

            if (recoverUserName == null)
               recoverUserName = xdsdm.getUserName();

            if (recoverPassword == null) 
               recoverPassword = xdsdm.getPassWord();

            if (recoverSecurityDomain == null)
               recoverSecurityDomain = securityDomain;

            if (log.isDebugEnabled())
               log.debug("Registered for XA Resource Recovery: " + xdsdm.getJndiName());

            // Variable substitution
            recoverUserName = getSubstitutionValue(recoverUserName);
            recoverPassword = getSubstitutionValue(recoverPassword);
            recoverSecurityDomain = getSubstitutionValue(recoverSecurityDomain);

            if (log.isDebugEnabled())
            {
               if (recoverUserName != null)
               {
                  log.debug("RecoverUserName=" + recoverUserName);
               }
               else if (recoverSecurityDomain != null)
               {
                  log.debug("RecoverSecurityDomain=" + recoverSecurityDomain);
               }
            }

            if ((recoverUserName != null && recoverPassword != null) ||
                (recoverSecurityDomain != null))
            {
               getXAResourceRecoveryRegistry().addXAResourceRecovery(this);
               recoveryRegistered = true;
            }
            else
            {
               log.warn("Unable to register recovery for " + dmd.getJndiName());
            }
         }
      }
   }

   protected void stopService()
   {
      if (recoveryRegistered)
      {
         if (getXAResourceRecoveryRegistry() != null)
         {
            close(recoverMC);

            getXAResourceRecoveryRegistry().removeXAResourceRecovery(this);
            recoveryRegistered = false;

            if (log.isDebugEnabled())
               log.debug("Unregistered for XA Resource Recovery: " + dmd.getJndiName());
         }
      }

      mcf = null;
      mcfClass = null;
   }

   /**
    * Provides XAResource(s) to the transaction system for recovery purposes.
    *
    * @return An array of XAResource objects for use in transaction recovery
    * In most cases the implementation will need to return only a single XAResource in the array.
    * For more sophisticated cases, such as where multiple different connection types are supported,
    * it may be necessary to return more than one.
    *
    * The Resource should be instantiated in such a way as to carry the necessary permissions to
    * allow transaction recovery. For some deployments it may therefore be necessary or desirable to
    * provide resource(s) based on e.g. database connection parameters such as username other than those
    * used for the regular application connections to the same resource manager. 
    */
   public XAResource[] getXAResources()
   {
      if (recoveryRegistered)
      {
         try
         {
            Subject subject = getSubject();

            // Check if we got a valid Subject instance; requirement for recovery
            if (subject != null)
            {
               ManagedConnection mc = open(subject);
               XAResource xaResource = null;

               Object connection = null;
               try
               {
                  connection = openConnection(mc, subject);
                  xaResource = mc.getXAResource();
               }
               catch (ResourceException reconnect)
               {
                  closeConnection(connection);
                  connection = null;
                  close(mc);
                  mc = open(subject);
                  xaResource = mc.getXAResource();
               }
               finally
               {
                  boolean forceDestroy = closeConnection(connection);
                  connection = null;

                  if (forceDestroy)
                  {
                     close(mc);
                     mc = open(subject);
                     xaResource = mc.getXAResource();
                  }
               }

               try
               {
                  ObjectName on = new ObjectName(connectionManager);

                  boolean wrapXAResource = false;
                  boolean padXid = false;
                  Boolean isSameRMOverrideValue = Boolean.FALSE;

                  Boolean b = null;

                  b = (Boolean)getServer().getAttribute(on, "WrapXAResource");
                  if (b != null)
                     wrapXAResource = b.booleanValue();

                  b = (Boolean)getServer().getAttribute(on, "PadXid");
                  if (b != null)
                     padXid = b.booleanValue();

                  b = (Boolean)getServer().getAttribute(on, "IsSameRMOverrideValue");
                  if (b != null)
                     isSameRMOverrideValue = b;

                  if (wrapXAResource)
                  {
                     String eisProductName = null;
                     String eisProductVersion = null;

                     try
                     {
                        if (mc.getMetaData() != null)
                        {
                           eisProductName = mc.getMetaData().getEISProductName();
                           eisProductVersion = mc.getMetaData().getEISProductVersion();
                        }
                     }
                     catch (ResourceException re)
                     {
                        // Ignore
                     }

                     if (eisProductName == null)
                        eisProductName = dmd.getJndiName();

                     if (eisProductVersion == null)
                        eisProductVersion = dmd.getJndiName();

                     xaResource = new XAResourceWrapperImpl(xaResource, 
                                                            padXid,
                                                            isSameRMOverrideValue,
                                                            eisProductName,
                                                            eisProductVersion);
                  }
               }
               catch (Throwable t)
               {
                  if (log.isDebugEnabled())
                     log.debug("Unable to verify wrap-xa settings for " + dmd.getJndiName(), t);
               }

               if (log.isDebugEnabled())
                  log.debug("Recovery XAResource=" + xaResource + " for " + dmd.getJndiName());
            
               return new XAResource[] {xaResource};
            }
            else
            {
               if (log.isDebugEnabled())
                  log.debug("Subject for recovery was null");
            }
         }
         catch (ResourceException re)
         {
            log.warn("Error during recovery", re);
         }
      }
      else
      {
         if (log.isDebugEnabled())
            log.debug("Recovery not registered for " + dmd.getJndiName());
      }

      return new XAResource[0];
   }

   /**
    * This method provide the Subject used for the XA Resource Recovery
    * integration with the XAResourceRecoveryRegistry.
    *
    * This isn't done through the SecurityAssociation functionality of JBossSX
    * as the Subject returned here should only be used for recovery.
    *
    * @return The recovery subject; <code>null</code> if no Subject could be created
    */
   private Subject getSubject()
   {
      return AccessController.doPrivileged(new PrivilegedAction<Subject>() 
      {
         public Subject run()
         {
            if (recoverUserName != null && recoverPassword != null)
            {
               // User name and password use-case
               Subject subject = new Subject();

               // Principals
               Principal p = new SimplePrincipal(recoverUserName);
               subject.getPrincipals().add(p);

               // PrivateCredentials
               PasswordCredential pc = new PasswordCredential(recoverUserName, recoverPassword.toCharArray());
               pc.setManagedConnectionFactory(mcf);
               subject.getPrivateCredentials().add(pc);

               // PublicCredentials
               // None

               if (log.isDebugEnabled())
                  log.debug("Recovery Subject=" + subject);

               return subject;
            }
            else
            {
               // Security-domain use-case
               try
               {
                  if (recoverSecurityDomain != null)
                  {
                     // Create a security context on the association
                     SecurityContext securityContext = SecurityContextFactory.createSecurityContext(recoverSecurityDomain);
                     SecurityContextAssociation.setSecurityContext(securityContext);
               
                     // Unauthenticated
                     Subject unauthenticated = new Subject();
                  
                     // Leave the subject empty as we don't have any information to do the
                     // authentication with - and we only need it to be able to get the
                     // real subject from the SubjectFactory
                  
                     // Set the authenticated subject
                     securityContext.getSubjectInfo().setAuthenticatedSubject(unauthenticated);

                     // Use the unauthenticated subject to get the real recovery subject instance
                     Subject subject = subjectFactory.createSubject(recoverSecurityDomain);

                     if (log.isDebugEnabled())
                        log.debug("Recovery Subject=" + subject);
                     
                     return subject;
                  }
                  else
                  {
                     if (log.isDebugEnabled())
                        log.debug("RecoverySecurityDomain was empty");
                  }
               }
               catch (Throwable t)
               {
                  log.warn("Exception during getSubject()" + t.getMessage(), t);
               }

               return null;
            }
         }
      });
   }

   /**
    * Open a managed connection
    * @param s The subject
    * @return The managed connection
    * @exception ResourceException Thrown in case of an error
    */
   private ManagedConnection open(Subject s) throws ResourceException
   {
      if (recoverMC == null)
      {
         if (log.isDebugEnabled())
            log.debug("Creating new managed connection for recovery");

         recoverMC = createManagedConnection(s, null);
      }

      return recoverMC;
   }

   /**
    * Close a managed connection
    * @param mc The managed connection
    */
   private void close(ManagedConnection mc)
   {
      if (log.isDebugEnabled())
         log.debug("Closing managed connection for recovery");

      if (mc != null)
      {
         try
         {
            mc.cleanup();
         }
         catch (ResourceException ire)
         {
            log.warn("Error during recovery cleanup", ire);
         }
      }

      if (mc != null)
      {
         try
         {
            mc.destroy();
         }
         catch (ResourceException ire)
         {
            log.warn("Error during recovery destroy", ire);
         }
      }

      mc = null;
      recoverMC = null;
   }

   /**
    * Open a connection
    * @param mc The managed connection
    * @param s The subject
    * @return The connection handle
    * @exception ResourceException Thrown in case of an error
    */
   private Object openConnection(ManagedConnection mc, Subject s) throws ResourceException
   {
      if (log.isDebugEnabled())
         log.debug("Creating connection for recovery check");

      return mc.getConnection(s, null);
   }

   /**
    * Close a connection
    * @param c The connection
    * @return Should the managed connection be forced closed
    */
   private boolean closeConnection(Object c)
   {
      if (log.isDebugEnabled())
         log.debug("Closing connection for recovery check");

      if (c != null)
      {
         if (c instanceof javax.resource.cci.Connection)
         {
            try
            {
               javax.resource.cci.Connection cci = (javax.resource.cci.Connection)c;
               cci.close();
            }
            catch (ResourceException ire)
            {
               log.warn("Error during recovery connection close", ire);

               if (log.isDebugEnabled())
                  log.debug("Forcing recreate of managed connection");

               return true;
            }
         }
         else
         {
            if ("false".equals(SecurityActions.getSystemProperty("recover-connection-validation")))
            {
               if (log.isDebugEnabled())
                  log.debug("Recovery connection validation is disabled.  Forcing recreate of managed connection.");

               return true;
            }

            Method method = null;
            try
            {
               method = c.getClass().getMethod("isValid", new Class[] {int.class});
               method.setAccessible(true);
               Boolean b = (Boolean)method.invoke(c, new Object[] {new Integer(5)});
               return !b.booleanValue();
            }
            catch (NoSuchMethodException nsme)
            {
               try
               {
                  method = c.getClass().getMethod("close", (Class<?>)null);
                  method.setAccessible(true);
                  method.invoke(c, (Object)null);
               }
               catch (Throwable it)
               {
                  log.warn("No close() method defined on connection interface. Destroying managed connection to clean-up", it);

                  if (log.isDebugEnabled())
                     log.debug("Forcing recreate of managed connection");

                  return true;
               }
            }
            catch (InvocationTargetException ite)
            {
               if (log.isDebugEnabled())
               {
                  log.debug("Unable to execute isValid() due to " + ite.getMessage());
                  log.debug("Forcing recreate of managed connection");
               }

               return true;
            }
            catch (Throwable t)
            {
               log.warn("Error during recovery connection isValid", t);

               if (log.isDebugEnabled())
                  log.debug("Forcing recreate of managed connection");

               return true;
            }
         }
      }

      return false;
   }
   
   public void setManagedConnectionFactoryAttribute(String name, Class clazz, Object value)
   {
      setManagedConnectionFactoryAttribute(name, clazz, value, false);
   }
   
   protected void setManagedConnectionFactoryAttribute(String name, Class clazz, Object value, boolean mustExist)
   {
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("Null or empty attribute name " + name);
      String setterName = "set" + Character.toUpperCase(name.charAt(0));
      if (name.length() > 1)
         setterName = setterName.concat(name.substring(1));
      Method setter;
      try
      {
         setter = mcfClass.getMethod(setterName, new Class[] {clazz});
      }
      catch (NoSuchMethodException nsme)
      {
         String error = "The class '" + mcfClass.toString() + "' has no setter for config property '" + name + "'"; 
         if (mustExist)
            throw new IllegalArgumentException(error);
         else
         {
            log.trace(error, nsme);
            return;
         }
      }
      try
      {
         setter.invoke(mcf, new Object[] {value});
         log.debug("set property " + name + " to value " + value);
      }
      catch (Exception e)
      {
         String error = "Unable to invoke setter method '" + setter + "' " + "on object '" + mcf + "'";
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
         String type = cpmd.getType();
         String value = cpmd.getValue();
         if (name == null || name.length() == 0 || value == null || value.length() == 0)
         {
            log.debug("Not setting config property '" + name + "'");
            continue;
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
               log.warn("Unable to find class '" + type + "' for " + "property '" + name + "' - skipping property.");
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
         // TODO: should happen in parsing layer?
         value = StringPropertyReplacer.replaceProperties(value);
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
         setManagedConnectionFactoryAttribute(name, clazz, v, mustExist);
      }
   }
   
   protected void setManagedConnectionFactoryProperties(List<ManagedConnectionFactoryPropertyMetaData> properties)
   {
      
      for (ManagedConnectionFactoryPropertyMetaData property : properties)
      {
         String name = property.getName();
         String type = property.getType();
         String value = property.getValue();
         
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
         // TODO: should happen in parsing layer?
         value = StringPropertyReplacer.replaceProperties(value);
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

   public Object createConnectionFactory() throws ResourceException
   {
      return mcf.createConnectionFactory();
   }

   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      return mcf.createConnectionFactory(cxManager);
   }

   public ManagedConnection createManagedConnection(Subject subject,
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

   public ManagedConnection matchManagedConnections(java.util.Set connectionSet, Subject subject,
         ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      return mcf.matchManagedConnections(connectionSet, subject, cxRequestInfo);
   }

   public void setLogWriter(java.io.PrintWriter out) throws ResourceException
   {
      mcf.setLogWriter(out);
   }

   @Override
   protected MBeanAttributeInfo[] getInternalAttributeInfo()
   {
      List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
      attributes.add(new MBeanAttributeInfo("RARName", String.class.getName(), "The ResourceAdapter deployment name.", true, false, false));
      attributes.add(new MBeanAttributeInfo("OldRarDeployment", javax.management.ObjectName.class.getName(), "The Connection Defintion class name.", true, true, false));
      attributes.add(new MBeanAttributeInfo("ConnectionDefinition", String.class.getName(), "The Connection Defintion class name.", true, false, false));
      attributes.add(new MBeanAttributeInfo("VendorName", String.class.getName(), "The Vendor Name.", true, false, false));
      attributes.add(new MBeanAttributeInfo("SpecVersion", String.class.getName(), "The Specification Version.", true, false, false));
      attributes.add(new MBeanAttributeInfo("EisType", String.class.getName(), "The Enterprise Information System type.", true, false, false));
      attributes.add(new MBeanAttributeInfo("Version", String.class.getName(), "The ResourceAdapter version.", true, false, false));
      attributes.add(new MBeanAttributeInfo("ManagedConnectionFactoryClass", String.class.getName(), "The ManagedConnectionFactory class", true, false, false));
      attributes.add(new MBeanAttributeInfo("ConnectionFactoryImpl", String.class.getName(), "The Connection Factory implementation", true, false, false));
      attributes.add(new MBeanAttributeInfo("ConnectionInterface", String.class.getName(), "The Connection Inteface", true, false, false));
      attributes.add(new MBeanAttributeInfo("ConnectionImplClass", String.class.getName(), "The Connection Implementation class", true, false, false));
      attributes.add(new MBeanAttributeInfo("TransactionSupport", String.class.getName(), "The Transaction Support", true, false, false));
      attributes.add(new MBeanAttributeInfo("AuthenticationMechanismType", String.class.getName(), "The Authentication Mechanism Type", true, false, false));
      attributes.add(new MBeanAttributeInfo("CredentialInterface", String.class.getName(), "The Credential Interface", true, false, false));
      attributes.add(new MBeanAttributeInfo("ReauthenticationSupport", Boolean.class.getName(), "The Reauthentication Support", true, false, true));
      attributes.add(new MBeanAttributeInfo("McfInstance", "javax.resource.spi.ManagedConnectionFactory", "The ManagedConnectionFactory instance", true, false, false));
      attributes.add(new MBeanAttributeInfo("XAResourceRecoveryRegistry", XAResourceRecoveryRegistry.class.getName(), "The XAResource Recovery Registry", true, false, false));
      attributes.add(new MBeanAttributeInfo("SubjectFactory", SubjectFactory.class.getName(), "The subject factory used for recovery", true, false, false));
      attributes.add(new MBeanAttributeInfo("ConnectionManager", String.class.getName(), "The connection manager object name", true, false, false));
      
//      ConnectionDefinitionMetaData cdmd = cmd.getConnectionDefinition(this.connectionDefinition);
      
//      Collection mcfProps = cdmd.getProperties();
//      
//      for(Iterator iter = mcfProps.iterator(); iter.hasNext();)
//      {
//         ConfigPropertyMetaData cpmd = (ConfigPropertyMetaData)iter.next();         
//         attributes.add(new MBeanAttributeInfo(cpmd.getName(), cpmd.getType(), cpmd.getDescription().getDescription(), true, false, false));         
//      }
      MBeanAttributeInfo[] info = attributes.toArray(new MBeanAttributeInfo[attributes.size()]);
      return info;
            
   }
   
   @Override
   protected void setInternalAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      if("OldRarDeployment".equals(attribute.getName()))
      {
         this.oldRarDeployment = (ObjectName)attribute.getValue();
      }
      else if("XAResourceRecoveryRegistry".equals(attribute.getName()))
      {
         this.xrrr = (XAResourceRecoveryRegistry)attribute.getValue();
      }
      else if("SubjectFactory".equals(attribute.getName()))
      {
         this.subjectFactory = (SubjectFactory)attribute.getValue();
      }
   }

   @Override
   protected MBeanOperationInfo[] getInternalOperationInfo()
   {
      MBeanOperationInfo[] operations = new MBeanOperationInfo[1]; 
      MBeanParameterInfo[] getMCFAttributeParamInfo = new MBeanParameterInfo[]{ new MBeanParameterInfo("ManagedConnectionFactoryAttributeName", String.class.getName(), "The ManagedConnectionFactoryAttribute name")};
      operations[0] = new MBeanOperationInfo("getManagedConnectionFactoryAttribute", "Inspect the value of a ManagedConnectionFactory attribute", getMCFAttributeParamInfo, Object.class.getName(), MBeanOperationInfo.INFO);
      return operations;
      
   }
   
   @Override
   protected Object internalInvoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException
   {
      if(actionName.equals("getManagedConnectionFactoryAttribute"))
      {
         String param = (String)params[0];
         return getManagedConnectionFactoryAttribute(param);
   
      }
      else
      {
         return super.internalInvoke(actionName, params, signature);
      }
   
   }

   @Override
   protected Object getInternalAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      ConnectionDefinitionMetaData cdmd = cmd.getConnectionDefinition(dmd.getConnectionDefinition());
      
      Object result = null;
      
      if("RARName".equals(attribute))
      {
         result = rarName;         
      
      }else if("OldRarDeployment".equals(attribute))
      {
         result = oldRarDeployment;
      }
      else if("ConnectionDefinition".equals(attribute))
      {
         result = dmd.getConnectionDefinition();         
      
      }else if("VendorName".equals(attribute))
      {
         result = cmd.getVendorName();         
      
      }else if("SpecVersion".equals(attribute))
      {
         result = cmd.getVersion();         
      
      }else if("EisType".equals(attribute))
      {
         result = cmd.getEISType();               
      
      }else if("Version".equals(attribute))
      {
         result = cmd.getRAVersion();
      
      }else if("ManagedConnectionFactoryClass".equals(attribute))
      {
         result = cdmd.getManagedConnectionFactoryClass();         

      }else if("ConnectionInterface".equals(attribute))
      {
         result = cdmd.getConnectionInterfaceClass();         
      }
      else if("ConnectionFactoryImpl".equals(attribute))
      {
         result = cdmd.getConnectionFactoryImplementationClass();
         
      }else if("ConnectionImplClass".equals(attribute))
      {
         result = cdmd.getConnectionImplementationClass();
      
      }
      else if("TransactionSupport".equals(attribute))
      {
         result = dmd.getTransactionSupportMetaData();         
      
      }
      else if("AuthenticationMechanismType".equals(attribute))
      {
         result = cmd.getAuthenticationMechanism().getAuthenticationMechansimType();      
      
      }
      else if("CredentialInterface".equals(attribute))
      {
         result = cmd.getAuthenticationMechanism().getCredentialInterfaceClass(); 
      
      }
      else if("ReauthenticationSupport".equals(attribute))
      {
         result = cmd.getReauthenticationSupport();
               
      }
      else if("McfInstance".equals(attribute))
      {
         result = mcf;               
      }
      else if("XAResourceRecoveryRegistry".equals(attribute))
      {
         result = xrrr;               
      }
      else if("SubjectFactory".equals(attribute))
      {
         result = subjectFactory;               
      }
      else if("ConnectionManager".equals(attribute))
      {
         result = connectionManager;               
      }
      
      if(result == null)
      {
         result = super.getInternalAttribute(attribute);
      }
      
      return result;
   }
   
   protected String getInternalDescription()
   {
      String description = null;
      DescriptionGroupMetaData dgmd = cmd.getDescription();
      if (dgmd != null)
         description = dgmd.getDescription();
      if (description == null)
         description = "RAR Deployment ";
      return description;
   }

   /**
    * System property substitution
    * @param input The input string
    * @return The output
    */
   private String getSubstitutionValue(String input)
   {
      if (input == null || input.trim().equals(""))
         return input;

      while ((input.indexOf("${")) != -1)
      {
         int from = input.indexOf("${");
         int to = input.indexOf("}");
         int dv = input.indexOf(":", from + 2);

         if (dv != -1)
         {
            if (dv > to)
               dv = -1;
         }

         String systemProperty = "";
         String defaultValue = "";
         String s = input.substring(from + 2, to);
         if (dv == -1)
         {
            if ("/".equals(s))
            {
               systemProperty = File.separator;
            }
            else if (":".equals(s))
            {
               systemProperty = File.pathSeparator;
            }
            else
            {
               systemProperty = SecurityActions.getSystemProperty(s);
            }
         }
         else
         {
            s = input.substring(from + 2, dv);
            systemProperty = SecurityActions.getSystemProperty(s);
            defaultValue = input.substring(dv + 1, to);
         }
         String prefix = "";
         String postfix = "";

         if (from != 0)
         {
            prefix = input.substring(0, from);
         }

         if (to + 1 < input.length() - 1)
         {
            postfix = input.substring(to + 1);
         }

         if (systemProperty != null && !systemProperty.trim().equals(""))
         {
            input = prefix + systemProperty + postfix;
         }
         else if (defaultValue != null && !defaultValue.trim().equals(""))
         {
            input = prefix + defaultValue + postfix;
         }
         else
         {
            input = prefix + postfix;
            log.debugf("System property %s not set", s);
         }
      }
      return input;
   }

   private static class SecurityActions
   {
      /**
       * Constructor
       */
      private SecurityActions()
      {
      }

      /**
       * Get a system property
       * @param name The property name
       * @return The property value
       */
      static String getSystemProperty(final String name)
      {
         if (System.getSecurityManager() == null)
         {
            return System.getProperty(name);
         }
         else
         {
            return (String) AccessController.doPrivileged(new PrivilegedAction<Object>()
            {
               public Object run()
               {
                  return System.getProperty(name);
               }
            });
         }
      }
   }
}
