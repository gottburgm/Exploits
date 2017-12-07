/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.profileservice.remoting;

import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.aspects.remoting.AOPRemotingInvocationHandler;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.SecurityContext;

/**
 * The remoting ServerInvocationHandler implementation for the ProfileService.
 * 
 * @see AOPRemotingInvocationHandler
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 111209 $
 */
public class ProfileServiceInvocationHandler extends AOPRemotingInvocationHandler
   implements ServerInvocationHandler
{
   private static Logger log = Logger.getLogger(ProfileServiceInvocationHandler.class);

   /** The ManagementView proxy */
   private ManagementView mgtViewProxy;
   /** The DeploymentManager proxy */
   private DeploymentManager deployMgrProxy;
   /** The profile service security domain name */
   private String securityDomain = "jmx-console";
   /** The security management layer to use in the security context setup */
   private ISecurityManagement securityManagement;

   public ManagementView getManagementViewProxy()
   {
      return mgtViewProxy;
   }
   public void setManagementViewProxy(ManagementView mgtViewProxy)
   {
      this.mgtViewProxy = mgtViewProxy;
   }

   public DeploymentManager getDeployMgrProxy()
   {
      return deployMgrProxy;
   }
   public void setDeployMgrProxy(DeploymentManager deployMgrProxy)
   {
      this.deployMgrProxy = deployMgrProxy;
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
   public Object invoke(InvocationRequest invocation)
      throws Throwable
   {
      // Create a security context for the invocation
      establishSecurityContext(invocation);
      org.jboss.aop.joinpoint.Invocation inv =(org.jboss.aop.joinpoint.Invocation)invocation.getParameter();
      SecurityContainer.setInvocation(inv);

      InvocationResponse value = (InvocationResponse) super.invoke(invocation);
      if( value.getResponse() instanceof ManagementView )
      {
         // Replace the ManagementView with its proxy
         value.setResponse(mgtViewProxy);
      }
      if( value.getResponse() instanceof DeploymentManager )
      {
         // Replace the DeploymentManager with its proxy
         value.setResponse(deployMgrProxy);
      }

      return value;
   }

   private void establishSecurityContext(InvocationRequest invocation) throws Exception
   { 
      SecurityContext newSC = SecurityActions.createAndSetSecurityContext(securityDomain);  

      // Set the SecurityManagement on the context
      SecurityActions.setSecurityManagement(newSC, securityManagement);
      if (log.isTraceEnabled())
      {
         log.trace("establishSecurityIdentity:SecCtx="+SecurityActions.trace(newSC));
      }
   }
}
