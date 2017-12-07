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
package org.jboss.deployment;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import javax.management.ObjectName;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * MainDeployer MBean interface
 * 
 * @version $Revision: 81539 $
 */
public interface MainDeployerMBean extends
   ServiceMBean, DeployerMBean, MainDeployerConstants
{
   /** The default ObjectName */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.system:service=MainDeployer");

   // Attributes ----------------------------------------------------
   public DeployerClient getKernelMainDeployer();
   /** set the kernel MainDeployer which will handle deployments */
   public void setKernelMainDeployer(DeployerClient delegate);

   public KernelController getController();
   public void setController(KernelController controller);

   /** Flag indicating whether directory content will be deployed.
    * The default value is taken from the jboss.deploy.localcopy system property. */
   boolean getCopyFiles();
   void setCopyFiles(boolean copyFiles);
   
   /** The path to the local tmp directory */
   File getTempDir();
   void setTempDir(File tempDir);

   /** The enhanced suffix order */
   String[] getEnhancedSuffixOrder();
   void setEnhancedSuffixOrder(String[] enhancedSuffixOrder);
   
   /** The ObjectName of the ServiceController */
   void setServiceController(ObjectName serviceController);
   
   /** The path to the local tmp directory in String form */
   String getTempDirString();
   
   /** The ordering of the deployment suffixes */
   String[] getSuffixOrder();

   // Operations ----------------------------------------------------

   /**
    * The <code>listDeployed</code> method returns a collection of
    * DeploymemtInfo objects for the currently deployed packages.
    * @return a <code>Collection</code> value
    */
   Collection listDeployed();

   /**
    * The <code>listDeployedModules</code> method returns a collection of
    * SerializableDeploymentInfo objects for the currently deployed packages.
    * @return a <code>Collection</code> value
    */
   Collection listDeployedModules();

   /**
    * Describe <code>listDeployedAsString</code> method here.
    * @return a <code>String</code> value
    */
   String listDeployedAsString();

   /**
    * The <code>listIncompletelyDeployed</code> method returns a list of
    * packages that have not deployed completely. The toString method will 
    * include any exception in the status field.
    * @return a <code>Collection</code> value
    */
   Collection listIncompletelyDeployed();

   /**
    * The <code>listWaitingForDeployer</code> method returns a collection of
    * the packages that currently have no identified deployer.
    * @return a <code>Collection</code> value
    */
   Collection listWaitingForDeployer();

   /**
    * The <code>addDeployer</code> method registers a deployer with the 
    * main deployer. Any waiting packages are tested to see if the new 
    * deployer will deploy them.
    * @param deployer a <code>SubDeployer</code> value
    */
   void addDeployer(SubDeployer deployer);

   /**
    * The <code>removeDeployer</code> method unregisters a deployer with the
    * MainDeployer. Deployed packages deployed with this deployer are undeployed.
    * @param deployer a <code>SubDeployer</code> value
    */
   void removeDeployer(SubDeployer deployer);

   /**
    * The <code>listDeployers</code> method returns a collection of ObjectNames
    * of deployers registered with the MainDeployer.
    * @return a <code>Collection<ObjectName></code> value
    */
   Collection listDeployers();

   /**
    * The <code>shutdown</code> method undeploys all deployed packages
    * in reverse order of their deployement.
    */
   void shutdown();

   /**
    * Describe <code>redeploy</code> method here.
    * @param urlspec a <code>String</code> value
    * @exception DeploymentException if an error occurs
    * @exception java.net.MalformedURLException if an error occurs
    */
   void redeploy(String urlspec) throws DeploymentException, MalformedURLException;

   /**
    * Describe <code>redeploy</code> method here.
    * @param url an <code>URL</code> value
    * @exception DeploymentException if an error occurs
    */
   void redeploy(URL url) throws DeploymentException;

   /**
    * Describe <code>redeploy</code> method here.
    * @param sdi a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    */
   void redeploy(DeploymentInfo sdi) throws DeploymentException;

   /**
    * The <code>undeploy</code> method undeploys a package identified by a URL
    * @param url an <code>URL</code> value
    */
   void undeploy(URL url) throws DeploymentException;

   /**
    * The <code>undeploy</code> method undeploys a package identified by a string representation of a URL.
    * @param urlspec a <code>String</code> value
    * @exception java.net.MalformedURLException if an error occurs
    */
   void undeploy(String urlspec) throws DeploymentException, MalformedURLException;

   /**
    * The <code>undeploy</code> method undeploys a package represented by a DeploymentInfo object.
    * @param di a <code>DeploymentInfo</code> value
    */
   void undeploy(DeploymentInfo di);

   /**
    * The <code>deploy</code> method deploys a package identified by a string representation of a URL.
    * @param urlspec a <code>String</code> value
    * @exception java.net.MalformedURLException if an error occurs
    */
   void deploy(String urlspec) throws DeploymentException, MalformedURLException;

   /**
    * The <code>deploy</code> method deploys a package identified by a URL
    * @param url an <code>URL</code> value
    */
   void deploy(URL url) throws DeploymentException;

   /**
    * The <code>deploy</code> method deploys a package represented by a DeploymentInfo object.
    * @param deployment a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    */
   void deploy(DeploymentInfo deployment) throws DeploymentException;

   /**
    * The <code>start</code> method starts a package identified by a URL
    * @param urlspec an URL string value
    * @jmx.managed-operation
    */
   public void start(String urlspec) throws DeploymentException, MalformedURLException;

   /**
    * The <code>stop</code> method  stop a package identified by a URL
    * @param urlspec an URL string value
    * @jmx.managed-operation
    */
   public void stop(String urlspec) throws DeploymentException, MalformedURLException;

   /**
    * The <code>isDeployed</code> method tells you if a package identified
    * by a string representation of a URL is currently deployed.
    * @param url a <code>String</code> value
    * @return a <code>boolean</code> value
    * @exception java.net.MalformedURLException if an error occurs
    */
   boolean isDeployed(String url) throws MalformedURLException;

   /**
    * The <code>isDeployed</code> method tells you if a packaged identified
    * by a URL is deployed.
    * @param url an <code>URL</code> value
    * @return a <code>boolean</code> value
    */
   boolean isDeployed(URL url);

   /**
    * The <code>getDeployment</code> method returns the Deployment object
    * for the URL supplied.
    * @param url an <code>URL</code> value
    * @return a <code>Deployment</code> value
    */
   Deployment getDeployment(URL url);

   /**
    * The <code>getDeploymentContext</code> method returns the DeploymentContext object
    * for the URL supplied.
    * @param url an <code>URL</code> value
    * @return a <code>DeploymentContext</code> value
    * @deprecated use getDeploymentUnit
    */
   DeploymentContext getDeploymentContext(URL url);

   /**
    * The <code>getDeploymentUnit</code> method returns the DeploymentUnit
    * object for the URL supplied.
    *
    * @param url an <code>URL</code> value
    * @return a <code>DeploymentUnit</code> value
    * @jmx.managed-operation
    */
   DeploymentUnit getDeploymentUnit(URL url);

   /**
    * The <code>getWatchUrl</code> method returns the URL that,
    * when modified, indicates that a redeploy is needed.
    * @param url an <code>URL</code> value
    * @return a <code>URL</code> value
    */
   URL getWatchUrl(URL url);

   /**
    * Check the current deployment states and generate an
    * IncompleteDeploymentException if there are mbeans
    * waiting for depedencies.
    * @exception IncompleteDeploymentException
    */
   void checkIncompleteDeployments() throws DeploymentException;

}
