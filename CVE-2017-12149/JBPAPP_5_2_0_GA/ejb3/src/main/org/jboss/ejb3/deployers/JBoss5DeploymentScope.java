/*
* JBoss, Home of Professional Open Source
* Copyright 2005, Red Hat Middleware LLC., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.ejb3.deployers;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.ObjectName;
import javax.naming.NameNotFoundException;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployment.spi.DeploymentEndpointResolver;
import org.jboss.deployment.spi.EndpointInfo;
import org.jboss.deployment.spi.EndpointType;
import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.deployers.tmp.EjbModuleEjbResolver;
import org.jboss.ejb3.javaee.JavaEEComponentHelper;
import org.jboss.logging.Logger;

/**
 * Abstraction for an EAR/WAR or anything that scopes EJB deployments
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author adrian@jboss.org
 * @author Scott.Stark@jboss.org
 * @version $Revision: 87050 $
 */
public class JBoss5DeploymentScope implements DeploymentScope
{
   public static final String ATTACHMENT_KEY = "org.jboss.ejb3.deployers.JBoss5DeploymentScope.deployments";
   private static final Logger log = Logger.getLogger(JBoss5DeploymentScope.class);
   /** The Map<String,Ejb3Deployment> of the deployment vfs path name to deployment */
   private ConcurrentHashMap<String, Ejb3Deployment> deployments;
   /** The deployment endpoint resolver implementation */
   private DeploymentEndpointResolver endpointResolver;
   private EjbModuleEjbResolver ejbRefResolver;
   /** The parent deployment short name */
   private String shortName;
   /** The deployment short name minus any .suffix */
   private String baseName;
   
   private boolean isEar;

   @SuppressWarnings("unchecked")
   public JBoss5DeploymentScope(VFSDeploymentUnit parent, boolean isEar)
   {
      this.isEar = isEar;
      this.shortName = parent.getSimpleName();
      baseName = null;
      if(isEar)
      {
         this.baseName = shortName;
         int idx = shortName.lastIndexOf('.');
         if( idx > 0 )
            baseName = shortName.substring(0, idx);
      }

      // Create the deployment map attachment if it does not exist
      deployments = (ConcurrentHashMap<String, Ejb3Deployment>)parent.getAttachment(ATTACHMENT_KEY);
      if (deployments == null)
      {
         deployments = new ConcurrentHashMap<String, Ejb3Deployment>();
         parent.addAttachment(ATTACHMENT_KEY, deployments);
      }
      // MappedReferenceMetaDataResolverDeployer output, Look for the endpoint resolver
      endpointResolver = parent.getAttachment(DeploymentEndpointResolver.class);
      /*
      if(endpointResolver == null)
         throw new IllegalStateException("No DeploymentEndpointResolver found in deployment: "+parent);
      */
   }
   /**
    * Temp ctor until EJBTHREE-1291 is resolved
    * @param parent
    * @param shortName
    */
   public JBoss5DeploymentScope(VFSDeploymentUnit parent, boolean isEar, String shortName)
   {
      this(parent, isEar);
      ejbRefResolver = new EjbModuleEjbResolver(this, shortName);
   }
   public void setDeployment(Ejb3Deployment deployment)
   {
      ejbRefResolver.setDeployment(deployment);
   }

   public DeploymentEndpointResolver getEndpointResolver()
   {
      return endpointResolver;
   }
   public void setEndpointResolver(DeploymentEndpointResolver endpointResolver)
   {
      this.endpointResolver = endpointResolver;
   }

   public Collection<Ejb3Deployment> getEjbDeployments()
   {
      return deployments.values();
   }

   public void register(Ejb3Deployment deployment)
   {
      // Create the path name relative to the root
      String pathName = deployment.getDeploymentUnit().getRootFile().getPathName();
      if(pathName.startsWith(shortName))
      {
         if(pathName.length() > shortName.length())
            pathName = pathName.substring(shortName.length()+1);
         else
            pathName = "";
      }
      deployments.put(pathName, deployment);
   }

   public void unregister(Ejb3Deployment deployment)
   {
      String pathName = deployment.getDeploymentUnit().getRootFile().getPathName();
      if(pathName.startsWith(shortName))
         pathName = pathName.substring(shortName.length()+1);
      deployments.remove(pathName);
   }

   public Ejb3Deployment findRelativeDeployment(String relativeName)
   {
      if (relativeName.startsWith("../"))
      {
         relativeName = relativeName.substring(3);
      }
      return deployments.get(relativeName);
   }

   @SuppressWarnings("unchecked")
   public EJBContainer getEjbContainer(Class businessIntf, String vfsContext)
      throws NameNotFoundException
   {
      // Get the deployment endpoint
      if(ejbRefResolver != null)
      {
         // TODO: EJBTHREE-1291
         return ejbRefResolver.getEjbContainer(businessIntf);
      }

      EJBContainer container = null;
      EndpointInfo endpoint = endpointResolver.getEndpointInfo(businessIntf, EndpointType.EJB, vfsContext);
      if(endpoint != null)
      {
         log.debug("Found endpoint for interface: "+businessIntf+", endpoint: "+endpoint);
         Ejb3Deployment deployment = deployments.get(endpoint.getPathName());
         // Note that this should never happen because the dependencies should have been
         // resolved by the MappedReferenceMetaDataResolverDeployer
         if(deployment == null)
            throw new IllegalStateException("JBAS-5713: could not find an ejb3 deployment for " + endpoint.getPathName() + " (yet), try reordering the jars or adding explicit dependencies");
         String ejbObjectName = JavaEEComponentHelper.createObjectName(deployment, endpoint.getName());
         ObjectName ejbON;
         try
         {
            ejbON = new ObjectName(ejbObjectName);
         }
         catch (Exception e)
         {
            throw new IllegalStateException("Failed to build ejb container ObjectName", e);
         }
         container = (EJBContainer) deployment.getContainer(ejbON);
         // TODO: container = deployment.getEjbContainerForEjbName(endpoint.getName());
      }
      else
      {
         log.debug("Failed to find endpoint for interface: "+businessIntf);
      }
      return container;
   }

   @SuppressWarnings("unchecked")
   public EJBContainer getEjbContainer(String ejbLink, Class businessIntf, String vfsContext)
   {
      if(ejbRefResolver != null)
      {
         // TODO: EJBTHREE-1291
         return ejbRefResolver.getEjbContainer(ejbLink, businessIntf);
      }

      EJBContainer container = null;
      // First try the ejbLink
      EndpointInfo endpoint = endpointResolver.getEndpointInfo(ejbLink, EndpointType.EJB, vfsContext);
      if(endpoint != null)
      {
         log.debug("Found endpoint for ejbLink: "+ejbLink+", endpoint: "+endpoint);
         Ejb3Deployment deployment = deployments.get(endpoint.getPathName());
         // Note that this should never happen because the dependencies should have been
         // resolved by the MappedReferenceMetaDataResolverDeployer
         if(deployment == null)
            throw new IllegalStateException("JBAS-5713: could not find an ejb3 deployment for " + endpoint.getPathName() + " (yet), try reordering the jars or adding explicit dependencies");
         String ejbObjectName = JavaEEComponentHelper.createObjectName(deployment, endpoint.getName());
         ObjectName ejbON;
         try
         {
            ejbON = new ObjectName(ejbObjectName);
         }
         catch (Exception e)
         {
            throw new IllegalStateException("Failed to build ejb container ObjectName", e);
         }
         container = (EJBContainer) deployment.getContainer(ejbON);
         // TODO: container = deployment.getEjbContainerForEjbName(endpoint.getName());         container = deployment.getEjbContainerForEjbName(endpoint.getName());
      }
      else
      {
         log.debug("Failed to find endpoint for ejbLink: "+ejbLink);
         // Try the business interface
         try
         {
            container = getEjbContainer(businessIntf, vfsContext);
         }
         catch(NameNotFoundException e)
         {
            log.debug("Did not resolve container by interface: "+businessIntf, e);
         }
      }     
      return container;
   }

   public String getName()
   {
      return shortName;
   }
   
   public String getShortName()
   {
      return shortName;
   }

   public String getBaseName()
   {
      return baseName;
   }

   protected boolean isEar()
   {
      return isEar;
   }
}
