/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, Red Hat Middleware LLC, and individual contributors as indicated
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

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.management.MBeanServer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.KernelAbstraction;
import org.jboss.ejb3.clientmodule.ClientENCInjectionContainer;
import org.jboss.jpa.resolvers.PersistenceUnitDependencyResolver;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.metadata.client.jboss.JBossClientMetaData;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.naming.Util;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;

/**
 * Deploys a client application jar.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class Ejb3ClientDeployer extends AbstractSimpleVFSRealDeployer<JBossClientMetaData>
{
   private Kernel kernel;
   private MBeanServer server;
   /** A flag indicating if a deployment based jndi should be linked to the JBossClientMetaData.jndiName */
   private boolean linkDeploymentJndiName = true;
   
   private PersistenceUnitDependencyResolver persistenceUnitDependencyResolver;

   /**
    * Create a new Ejb3ClientDeployer.
    */
   public Ejb3ClientDeployer()
   {
      super(JBossClientMetaData.class);
      setOutput(ClientENCInjectionContainer.class);
      // JBossASKernel install output
      setOutput(KernelDeployment.class);
   }

   public boolean isLinkDeploymentJndiName()
   {
      return linkDeploymentJndiName;
   }

   public void setLinkDeploymentJndiName(boolean linkDeploymentJndiName)
   {
      this.linkDeploymentJndiName = linkDeploymentJndiName;
   }

   /**
    * Deploy a client application
    * @param unit - the application jar unit
    * @param metaData - the metadata for the application
    */
   @Override
   public void deploy(VFSDeploymentUnit unit, JBossClientMetaData metaData) throws DeploymentException
   {
      log.debug("deploy " + unit.getName());
      
      String appClientName = getJndiName(metaData);
      String deploymentClientName = null;
      if(appClientName == null)
         appClientName = getDeploymentJndiName(unit);
      else if(linkDeploymentJndiName)
         deploymentClientName = getDeploymentJndiName(unit);

      try
      {
         // I create the namespace here, because I destroy it in undeploy
         InitialContext iniCtx = InitialContextFactory.getInitialContext();
         Context encCtx = Util.createSubcontext(iniCtx, appClientName);
         log.debug("Creating client ENC binding under: " + appClientName);
         if(deploymentClientName != null && deploymentClientName.equals(appClientName) == false)
         {
            Util.createLinkRef(iniCtx, deploymentClientName, appClientName);
         }

         // Notify the client launcher of extra class path entries in an EAR (See EE 8.2)
         List<VirtualFile> classPath = unit.getClassPath();
         ArrayList<String> cpURIs = new ArrayList<String>();
         for(VirtualFile vf : classPath)
         {
            String uri = vf.toURI().toString();
            cpURIs.add(uri);
         }
         // Also need to include the ear lib dir entries
         VFSDeploymentUnit earUnit = unit.getParent();
         if(earUnit != null)
         {
            List<VirtualFile> earClassPath = earUnit.getClassPath();
            JBossAppMetaData earMD = earUnit.getAttachment(JBossAppMetaData.class);
            if(earMD != null)
            {
               String libDir = earMD.getLibraryDirectory();
               if(libDir == null)
                  libDir = "lib";
               String libDirPrefix = libDir + "/";
               for(VirtualFile vf : earClassPath)
               {
                  if(vf.getPathName().startsWith(libDirPrefix))
                  {
                     String uri = vf.toURI().toString();
                     cpURIs.add(uri);
                  }
               }
            }
         }

         encCtx.bind("classPathEntries", cpURIs);
         // java:comp/UserTransaction -> UserTransaction
         Util.createLinkRef(encCtx, "UserTransaction", "UserTransaction");

         // TODO: Notify the client launcher of other metadata stuff (injectors, lifecycle callbacks etc)
         // FIXME: For now I expose the entire metadata
         encCtx.bind("metaData", metaData);
         
         String mainClassName = getMainClassName(unit, true);

         Class<?> mainClass = loadClass(unit, mainClassName);

         ClientENCInjectionContainer container = new ClientENCInjectionContainer(unit, metaData, mainClass, appClientName, unit.getClassLoader(), encCtx, persistenceUnitDependencyResolver);

         //di.deployedObject = container.getObjectName();
         unit.addAttachment(ClientENCInjectionContainer.class, container);
         JBoss5DeploymentUnit ejb3Unit = new JBoss5DeploymentUnit(unit);
         getKernelAbstraction().install(container.getObjectName().getCanonicalName(),
               container.getDependencyPolicy(), ejb3Unit, container);
      }
      catch(Exception e)
      {
         log.error("Could not deploy " + unit.getName(), e);
         undeploy(unit, metaData);
         throw new DeploymentException("Could not deploy " + unit.getName(), e);
      }
   }

   /**
    * Get the class path entries which have been determined by the EARStructure.
    * Will return null if this unit is not part of an ear deployment.
    */
   private List<ClassPathEntry> getClassPathEntries(VFSDeploymentUnit unit)
   {
//      log.info("class path = " + unit.getTopLevel().getAttachment(StructureMetaData.class).getContext("").getClassPath());
      StructureMetaData smd = unit.getTopLevel().getAttachment(StructureMetaData.class);
      if(smd == null)
         return null;
      // A context without a path is the one we want (see AbstractStructureDeployer)
      return smd.getContext("").getClassPath();
   }
   
   /**
    * If there is no deployment descriptor, or it doesn't specify a JNDI name, then we make up one.
    * We use the basename from di.shortName.
    *
    * @param unit
    * @param dd
    * @return   a good JNDI name
    */
   private String getJndiName(JBossClientMetaData dd)
   {
      String jndiName = dd.getJndiName();
      return jndiName;
   }
   private String getDeploymentJndiName(DeploymentUnit unit)
   {
      String jndiName;
      String shortName = unit.getSimpleName();
      if(shortName.endsWith(".jar/"))
         jndiName = shortName.substring(0, shortName.length() - 5);
      else if(shortName.endsWith(".jar"))
         jndiName = shortName.substring(0, shortName.length() - 4);
      else
         throw new IllegalStateException("Expected either '.jar' or '.jar/' at the end of " + shortName);

      return jndiName;
   }

//   public Kernel getKernel()
//   {
//      return kernel;
//   }

   private KernelAbstraction getKernelAbstraction()
   {
      return new JBossASKernel(kernel);
   }

   // TODO: move this method either to a utility class or to the scanning deployer
   protected String getMainClassName(VFSDeploymentUnit unit, boolean fail) throws Exception
   {
      VirtualFile file = unit.getMetaDataFile("MANIFEST.MF");
      log.trace("parsing " + file);
      // Default to the jboss client main
      String mainClassName = "org.jboss.client.AppClientMain";

      if (file != null)
      {
         try
         {
            Manifest mf = VFSUtils.readManifest(file);
            Attributes attrs = mf.getMainAttributes();
            String className = attrs.getValue(Attributes.Name.MAIN_CLASS);
            if (className != null)
            {
               mainClassName = className;
            }
         }
         finally
         {
            file.close();
         }
      }
      return mainClassName;
   }

   private Class<?> loadClass(DeploymentUnit unit, String className) throws ClassNotFoundException
   {
      ClassLoader old = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(unit.getClassLoader());
         return Thread.currentThread().getContextClassLoader().loadClass(className);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(old);
      }
   }

   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }

   public void setMbeanServer(MBeanServer server)
   {
      this.server = server;
   }

   @Inject
   public void setPersistenceUnitDependencyResolver(PersistenceUnitDependencyResolver resolver)
   {
      this.persistenceUnitDependencyResolver = resolver;
   }
   
   @Override
   public void undeploy(VFSDeploymentUnit unit, JBossClientMetaData metaData)
   {
      log.debug("undeploy " + unit.getName());

      ClientENCInjectionContainer container = unit.getAttachment(ClientENCInjectionContainer.class);
      if(container != null)
         getKernelAbstraction().uninstall(container.getObjectName().getCanonicalName());

      String appClientName = getJndiName(metaData);
      String deploymentClientName = null;
      if(appClientName == null)
         appClientName = getDeploymentJndiName(unit);
      else if(linkDeploymentJndiName)
         deploymentClientName = getDeploymentJndiName(unit);

      log.debug("Removing client ENC from: " + appClientName);
      try
      {
         InitialContext iniCtx = InitialContextFactory.getInitialContext();
         Util.unbind(iniCtx, appClientName);
         if(deploymentClientName != null && deploymentClientName.equals(appClientName) == false)
            Util.removeLinkRef(deploymentClientName);
      }
      catch(NameNotFoundException e)
      {
         // make sure stop doesn't fail for no reason
         log.debug("Could not find client ENC");
      }
      catch (NamingException e)
      {
         log.error("Failed to remove client ENC", e);
      }
   }

}
