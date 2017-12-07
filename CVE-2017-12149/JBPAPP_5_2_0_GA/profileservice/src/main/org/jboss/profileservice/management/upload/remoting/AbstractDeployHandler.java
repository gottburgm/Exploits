/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.profileservice.management.upload.remoting;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.aspects.remoting.AOPRemotingInvocationHandler;
import org.jboss.deployers.spi.management.deploy.DeploymentID;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.logging.Logger;
import org.jboss.profileservice.management.client.upload.SerializableDeploymentID;
import org.jboss.profileservice.remoting.SecurityContainer;
import org.jboss.profileservice.spi.DeploymentRepository;
import org.jboss.profileservice.spi.NoSuchProfileException;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileRepository;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.callback.InvokerCallbackHandler;
import org.jboss.remoting.stream.StreamInvocationHandler;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.SecurityContext;
import org.jboss.system.server.profileservice.repository.DefaultProfileDeploymentFactory;
import org.jboss.system.server.profileservice.repository.MainDeployerAdapter;
import org.jboss.virtual.VirtualFile;

/**
 * A remoting StreamInvocationHandler installed as the profile service subsystem
 * handler and used by the StreamingDeploymentTarget implementation.
 * 
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 98984 $
 */
public abstract class AbstractDeployHandler extends AOPRemotingInvocationHandler
   implements StreamInvocationHandler
{

   /** The profile repository. */
   protected ProfileRepository profileRepository;
   
   /** The deployer. */
   private MainDeployerAdapter deployer;
   /** The profile service security domain name */
   private String securityDomain = "jmx-console";
   /** The security management layer to use in the security context setup */
   private ISecurityManagement securityManagement;

   /** The deployment factory */
   private static final DefaultProfileDeploymentFactory deploymentFactory = DefaultProfileDeploymentFactory.getInstance();
   
   /** The logger. */
   protected static final Logger log = Logger.getLogger(DeployHandler.class);
   
   public ProfileRepository getProfileRepository()
   {
      return profileRepository;
   }
   
   public void setProfileRepository(ProfileRepository profileRepository)
   {
      this.profileRepository = profileRepository;
   }
   
   public MainDeployerAdapter getDeployer()
   {
      return deployer;
   }
   
   public void setDeployer(MainDeployerAdapter deployer)
   {
      this.deployer = deployer;
   }
   
   protected String[] getRepositoryNames(String[] names, DeploymentRepository deploymentRepository) throws Exception
   {
      if(names == null || names.length == 0)
         return new String[0];
      
      ArrayList<String> resolvedNames = new ArrayList<String>();
      for(ProfileKey key : this.profileRepository.getProfileKeys())
      {
         DeploymentRepository repository = this.profileRepository.getProfileDeploymentRepository(key);
         String[] repositoryNames = repository.getRepositoryNames(names);
         if(repositoryNames != null && repositoryNames.length > 0)
         {
            resolvedNames.addAll(Arrays.asList(repositoryNames));
         }
      }
      return resolvedNames.toArray(new String[resolvedNames.size()]);
   }
   
   public String getSecurityDomain()
   {
      return securityDomain;
   }
   public void setSecurityDomain(String securityDomain)
   {
      this.securityDomain = securityDomain;
   }

   public ISecurityManagement getSecurityManagement()
   {
      return securityManagement;
   }
   public void setSecurityManagement(ISecurityManagement securityManagement)
   {
      this.securityManagement = securityManagement;
   }

   public void addListener(InvokerCallbackHandler arg0)
   {
   }
   
   public void removeListener(InvokerCallbackHandler arg0)
   {
   }

   public void setInvoker(ServerInvoker arg0)
   {
   }

   public void setMBeanServer(MBeanServer arg0)
   {
   }
   
   /**
    * Get the deployment repository for a given DeploymentID.
    * 
    * @param dtID the DeploymentID
    * @return the deployment repository
    * @throws NoSuchProfileException if the deployment repository does not exist 
    */
   protected DeploymentRepository getDeploymentRepository(DeploymentID dtID) throws Exception
   {
      ProfileKey key = dtID.getProfile();
      if(key == null)
         throw new IllegalStateException("No profile key attached to deploymentID "+ dtID);
      
      return this.profileRepository.getProfileDeploymentRepository(key);
   }
   
   /**
    * Try to resolve a deployment repository based on the deployment name.
    * 
    * @param name the deployment name
    * @param defaultKey the fallback profile key
    * @return the resolved deployment repository
    * @throws Exception
    */
   protected DeploymentRepository resolveDeploymentRepository(String name, ProfileKey defaultKey) throws Exception
   {
      for(ProfileKey key : this.profileRepository.getProfileKeys())
      {
         DeploymentRepository repository = this.profileRepository.getProfileDeploymentRepository(key);
         if(repository.getDeploymentNames().contains(name))
         {
            return repository;
         }
      }
      return this.profileRepository.getProfileDeploymentRepository(defaultKey);
   }
   
   /**
    * Handle a DeploymentManager invocation other than distribute
    * 
    * @param request - the remoting invocation
    * @return the result of the invocation
    */
   public Object invoke(InvocationRequest request) throws Throwable
   {
      // Create a security context for the invocation
      establishSecurityContext(request);
      Object parameter = request.getParameter();
      
      Object returnValue = null;

      if(parameter instanceof Invocation)
      {
         Invocation inv =(Invocation) parameter;
         SecurityContainer.setInvocation(inv);
         returnValue = super.invoke(request);
      }
      else
      {
         Map<?, ?> payload = request.getRequestPayload();
         DeploymentID dtID = (DeploymentID) payload.get("DeploymentTargetID");
         if(dtID == null)
            throw new IllegalStateException("Null deployment target ID.");
         
         log.debug("invoke, payload: "+payload+", parameter: "+parameter);
         try
         {
            if( parameter.equals("getRepositoryNames"))
            {
               String[] names = dtID.getNames();
               DeploymentRepository deploymentRepository = getDeploymentRepository(dtID);
               returnValue = getRepositoryNames(names, deploymentRepository);
            }
            else if( parameter.equals("distribute") )
            {
               returnValue = distribute(dtID);
            }
            else if( parameter.equals("prepare"))
            {
               // TODO
            }
            else if( parameter.equals("start") )
            {
               start(dtID);
            }
            else if( parameter.equals("stop") )
            {
               stop(dtID);
            }
            else if( parameter.equals("remove"))
            {
               remove(dtID);
            }
            // Keep for backward compatibility
            else if( parameter.equals("undeploy") )
            {
               remove(dtID);
            }
            else if (parameter.equals("redeploy"))
            {
               redeploy(dtID);
            }
         }
         catch(Exception e)
         {
            // Server side logging
            log.info("Failed to complete command: ["+ parameter +"] for deployment: " + dtID, e);
            throw e;
         }

      }
      return returnValue;
   }

   protected abstract String[] distribute(DeploymentID dtID) throws Exception;
   
   /**
    * Handle a DeploymentManager distribute invocation for copyContent == true
    * 
    * @see DeploymentManager#distribute(String, java.net.URL, boolean)
    * @param request - the remoting invocation
    */
   public InvocationResponse handleStream(InputStream contentIS, InvocationRequest request) throws Throwable
   {
      // Get the deployment repository for this deploymentID
      SerializableDeploymentID deploymentTarget = (SerializableDeploymentID) request.getParameter();
      DeploymentRepository deploymentRepository = getDeploymentRepository(deploymentTarget);
      // Start to handle stream
      log.info("Handle stream, deploymentTarget: " + deploymentTarget);
      deploymentTarget.setContentIS(contentIS);
      String[] names = deploymentTarget.getNames();
      
      // Add deployment content to the repository
      String repositoryName = deploymentRepository.addDeploymentContent(names[0], contentIS,
            deploymentTarget.getDeploymentOptions());
      
      // FIXME make deployment visible to management view
      VirtualFile vf = deploymentRepository.getDeploymentContent(repositoryName);
      ProfileDeployment deployment = createDeployment(vf);
      deploymentRepository.addDeployment(deployment.getName(), deployment);
      deploymentRepository.lockDeploymentContent(deployment.getName());
      
      log.info("End handle stream, repositoryName: " + repositoryName);
      // Return the repository names
      String[] rnames = {repositoryName};
      deploymentTarget.setRepositoryNames(rnames);
     
      return new InvocationResponse(repositoryName);
   }
   
   /**
    * Start a deployment.
    * 
    * @param dtID the deployment id
    * @throws Exception for any error
    */
   protected void start(DeploymentID dtID) throws Exception
   {
      String[] names = dtID.getNames();
      log.info("Begin start, "+Arrays.asList(names));
      
      List<String> deployments = new ArrayList<String>(); 
      for(String name : names)
      {
         DeploymentRepository deploymentRepository = resolveDeploymentRepository(name, dtID.getProfile());
         // Schedule start for the deployment
         deployments.add(start(name, deploymentRepository));
      }
      // Process
      deployer.process();
      // CheckComplete
      deployer.checkComplete(
            deployments.toArray(new String[deployments.size()]));
      
      log.info("End start, "+ deployments);
   }

   protected String start(String name, DeploymentRepository repository) throws Exception
   {
      ProfileDeployment deployment = scheduleStart(name, repository);
      deployer.addDeployment(deployment);
      log.debug("Scheduling start for deployment: " + deployment);
      return deployment.getName();
   }
   
   protected abstract ProfileDeployment scheduleStart(String name, DeploymentRepository deploymentRepository) throws Exception;
   
   /**
    * Stop the deployments.
    * 
    * @param dtID the deployment id
    * @throws Exception for any error
    */
   protected void stop(DeploymentID dtID) throws Exception
   {
      String[] names = dtID.getNames();
      log.info("Stop, "+Arrays.asList(names));
      
      List<String> deployments = new ArrayList<String>();
      for(String name : names)
      {
         DeploymentRepository deploymentRepository = resolveDeploymentRepository(name, dtID.getProfile());
         // Schedule stop
         deployments.add(stop(name, deploymentRepository));
      }
      // CheckComplete
      deployer.process();
      
      // TODO check if there is still a deploymentContext ?
      log.info("End stop, "+ deployments);
   }
   
   protected String stop(String name, DeploymentRepository repository) throws Exception
   {
      ProfileDeployment deployment = scheduleStop(name, repository);
      deployer.removeDeployment(deployment);
      log.debug("Scheduling stop for deployment: " + deployment);
      
      return deployment.getName();
   }
   
   protected abstract ProfileDeployment scheduleStop(String name, DeploymentRepository repository) throws Exception;
   
   /**
    * Remove a deployment from the deployment repository.
    * This will delete the file for non-transient deployments.
    * 
    * @param dtID the deployment id
    * @throws Exception for any error
    */
   protected void remove(DeploymentID dtID) throws Exception
   {
      String[] names = dtID.getNames();
      log.info("Remove, "+Arrays.asList(names));
      
      for(String name : names)
      {
         DeploymentRepository deploymentRepository = resolveDeploymentRepository(name, dtID.getProfile());
         // Remove from repository
         removeDeployment(name, deploymentRepository);
      }
   }
   
   protected abstract void removeDeployment(String name, DeploymentRepository repository) throws Exception;

   /**
    * Redeploy a deployment.
    * 
    * @param dtID the deployment id
    * @throws Exception for any error
    */
   protected void redeploy(DeploymentID dtID) throws Exception
   {
      // Stop
      stop(dtID);
      // Start
      start(dtID);
   }
   
   /**
    * Create a profile deployment.
    * 
    * @param file the root file
    * @return the deployment
    */
   protected ProfileDeployment createDeployment(VirtualFile file)
   {
      if (file == null)
         throw new IllegalArgumentException("Null file");
      
      return deploymentFactory.createProfileDeployment(file);
   }
   
   private void establishSecurityContext(InvocationRequest invocation) throws Exception
   { 
      SecurityContext newSC = SecurityActions.createAndSetSecurityContext(securityDomain);  

      // Set the SecurityManagement on the context
      SecurityActions.setSecurityManagement(newSC, securityManagement);
      log.trace("establishSecurityIdentity:SecCtx="+SecurityActions.trace(newSC));
   }
}
