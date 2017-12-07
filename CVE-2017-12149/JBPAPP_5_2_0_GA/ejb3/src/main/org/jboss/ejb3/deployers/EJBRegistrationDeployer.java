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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServer;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployment.security.JaccPolicyUtil;
import org.jboss.deployment.spi.DeploymentEndpointResolver;
import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.virtual.VirtualFile;

/**
 * Creates initial EJB deployments and initializes only basic metadata.
 * A registration process is required so that
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 * @deprecated use Ejb3Deployer
 */
@Deprecated
public class EJBRegistrationDeployer extends AbstractVFSRealDeployer
{
   private static final Logger log = Logger.getLogger(EJBRegistrationDeployer.class);

   private HashSet ignoredJarsSet;
   private MBeanServer mbeanServer;
   private Kernel kernel;
   private Properties defaultPersistenceProperties;
   private List<String> allowedSuffixes;
   private boolean requireDeploymentDescriptor;

   public static boolean has30EjbJarXml(InputStream ddStream)
   {
      try
      {
         // look for version="3.0" in the file
         byte[] stringToFind = "version=\"3.0\"".getBytes();
         InputStreamReader reader = new InputStreamReader(ddStream);
         try
         {
            int idx = 0;
            int len = stringToFind.length;
            while (reader.ready())
            {
               int read = reader.read();
               if (read == stringToFind[idx])
               {
                  idx++;
                  if (idx == len)
                  {
                     return true;
                  }
               }
               else
               {
                  idx = 0;
               }
            }

         }
         finally
         {
            try
            {
               reader.close();
               ddStream.close();
            }
            catch (IOException ignored)
            {
            }
         }
      }
      catch (Exception ignore)
      {
      }
      return false;
   }

   /**
    * Create a new EJBRegistrationDeployer.
    */
   public EJBRegistrationDeployer()
   {
      setOutputs(Ejb3Deployment.class.getName(), JaccPolicyUtil.IGNORE_ME_NAME);
   }
   
   public List<String> getAllowedSuffixes()
   {
      return allowedSuffixes;
   }

   public void setAllowedSuffixes(List<String> allowedSuffixes)
   {
      this.allowedSuffixes = allowedSuffixes;
   }

   public HashSet getIgnoredJarsSet()
   {
      return ignoredJarsSet;
   }

   public void setIgnoredJarsSet(HashSet ignoredJarsSet)
   {
      this.ignoredJarsSet = ignoredJarsSet;
   }

   public MBeanServer getMbeanServer()
   {
      return mbeanServer;
   }

   public void setMbeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }

   public Kernel getKernel()
   {
      return kernel;
   }

   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }

   public Properties getDefaultPersistenceProperties()
   {
      return defaultPersistenceProperties;
   }

   public void setDefaultPersistenceProperties(Properties defaultPersistenceProperties)
   {
      this.defaultPersistenceProperties = defaultPersistenceProperties;
   }

   public boolean getRequireDeploymentDescriptor()
   {
      return requireDeploymentDescriptor;
   }

   public void setRequireDeploymentDescriptor(boolean requireDeploymentDescriptor)
   {
      this.requireDeploymentDescriptor = requireDeploymentDescriptor;
   }

   public void deploy(VFSDeploymentUnit unit) throws DeploymentException
   {
      try
      {
         // Initialize
         boolean hasEjbDd = false;
         boolean hasJbossDd = false;
         
         VirtualFile jar = unit.getRoot();
         if (jar.isLeaf() || ignoredJarsSet.contains(jar.getName()))
         {
            log.trace("EJBRegistrationDeployer ignoring: " + jar.getName());
            return;
         }
         if(!hasAllowedSuffix(jar.getName()))
         {
            log.trace("EJBRegistrationDeployer suffix not allowed: " + jar.getName());
            return;
         }
         
         VirtualFile ejbjar = unit.getMetaDataFile("ejb-jar.xml");
         if (ejbjar != null)
         {
            InputStream is = ejbjar.openStream();
            boolean has30EjbJarXml = has30EjbJarXml(is);
            is.close();
            if (!has30EjbJarXml) {
               return;
            }
            else {
               hasEjbDd = true;
            }
         }
         
         // Determine if jboss.xml is specified
         hasJbossDd = unit.getMetaDataFile("jboss.xml")!=null;
         
         // If DDs are required and none are present, skip deployment
         // EJBTHREE-1040
         if (this.getRequireDeploymentDescriptor() && !(hasJbossDd || hasEjbDd))
         {
            log.trace(EJBRegistrationDeployer.class.getSimpleName() + " skipping deployment \"" + unit.getSimpleName()
                  + "\", jar: \"" + jar.getName()
                  + "\" - either EJB3 Deployment Descriptor or \"jboss.xml\" is required and neither were found.");
            return;
         }
            
         log.debug("********* EJBRegistrationDepoyer Begin Unit: " + unit.getSimpleName() + " jar: " + jar.getName());
         JBoss5DeploymentScope scope = null;
         VFSDeploymentUnit parent = unit.getParent();
         boolean initScopeDeployment = false;
         if (parent != null && parent.isAttachmentPresent(JBossAppMetaData.class))
         {
            // An ear parent
            scope = (JBoss5DeploymentScope) parent.getAttachment(DeploymentScope.class);
            if (scope == null)
            {
               boolean isEar = unit != unit.getTopLevel();
               if(parent.isAttachmentPresent(DeploymentEndpointResolver.class) == true)
                  scope = new JBoss5DeploymentScope(parent, isEar);
               else
               {
                  // EJBTHREE-1291
                  scope = new JBoss5DeploymentScope(parent, isEar, unit.getSimpleName());
                  initScopeDeployment = true;
               }
               parent.addAttachment(DeploymentScope.class, scope);
            }
         }
         JBoss5DeploymentUnit du = new JBoss5DeploymentUnit(unit);
         du.setDefaultPersistenceProperties(defaultPersistenceProperties);
         Ejb3JBoss5Deployment deployment = new Ejb3JBoss5Deployment(du, kernel, mbeanServer, unit, scope, null);
         if (scope != null)
            scope.register(deployment);
         if(initScopeDeployment)
         {
            scope.setDeployment(deployment);
         }
         // create() creates initial EJB containers and initializes metadata.
         deployment.create();
         if (deployment.getEjbContainers().size() == 0)
         {
            log.trace("EJBRegistrationDeployer no containers in scanned jar, consider adding it to the ignore list: " + jar.getName() + " url: " + jar.toURL() + " unit: " + unit.getSimpleName());
            return;
         }
         unit.addAttachment(Ejb3Deployment.class, deployment);
         // TODO: temporarily disable the security deployment
         unit.addAttachment(JaccPolicyUtil.IGNORE_ME_NAME, true, Boolean.class);
      }
      catch (Exception e)
      {
         throw new DeploymentException(e);
      }
   }

   private boolean hasAllowedSuffix(String name)
   {
      if(allowedSuffixes == null)
         return true;
      
      for (String suffix : allowedSuffixes)
      {
         if (name.endsWith(suffix))
         {
            return true;
         }
      }
      return false;
   }
   
   public void undeploy(VFSDeploymentUnit unit)
   {
      Ejb3Deployment deployment = unit.getAttachment(Ejb3Deployment.class);
      if (deployment == null) return;
      try
      {
         deployment.stop();
      }
      catch (Exception e)
      {
         log.error("failed to stop deployment", e);
      }
      try
      {
         deployment.destroy();
      }
      catch (Exception e)
      {
         log.error("failed to destroy deployment", e);
      }
   }
}
