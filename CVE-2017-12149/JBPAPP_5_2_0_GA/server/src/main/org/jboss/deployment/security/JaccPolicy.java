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
package org.jboss.deployment.security;

import java.security.Policy;
import java.util.ArrayList;
import java.util.List;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;

import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.support.IdMetaData;


/**
 *  A Service Bean representing the JACC Policy for the top level deployment
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Dec 11, 2006 
 *  @version $Revision: 85945 $
 */
public class JaccPolicy<T extends IdMetaData> implements JaccPolicyMBean
{ 
   private static Logger log = Logger.getLogger(JaccPolicy.class);
   private boolean trace = log.isTraceEnabled();
   
   private PolicyConfiguration parentPC = null;
   private String contextID = null;  
    
   private T metaData; 
   
   private Boolean standaloneDeployment = Boolean.FALSE;    
   
   private List<PolicyConfigurationFacadeMBean> children = new ArrayList<PolicyConfigurationFacadeMBean>();
    
   
   /**
    * Ctr
    * @param id Jacc Context Id for the top level deployment
    * @throws IllegalArgumentException if id passed is null
    */
   public JaccPolicy(String id)
   {
      if(id == null)
         throw new IllegalArgumentException("Jacc Context Id passed is null");
      this.contextID = id;  
   }
   
   public JaccPolicy(String id, T metaData, Boolean standaloneDeployment)
   {
      this(id);
      this.metaData = metaData; 
      this.standaloneDeployment = standaloneDeployment;
   }
   
   /**
    * @see JaccPolicyMBean#create()
    */
   public void create()
   { 
      try
      {
         createPolicyConfiguration();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      if(this.standaloneDeployment == Boolean.TRUE)
      {
         try
         {   
            if (metaData != null) 
               createPermissions(metaData,parentPC);
            else
               log.warn("Cannot create permissions with 'null' metaData for id=" + contextID);
         }
         catch (PolicyContextException e)
         {

            throw new RuntimeException("Cannot create permissions:",e);
         }
      }
      if(trace)
         log.trace("create():" + this.contextID);
   }
   
   /**
    * @see JaccPolicyMBean#destroy()
    */
   public void destroy()
   {  
      if(trace)
         log.trace("destroy:" + this.contextID);
      parentPC= null; 
   }

   /**
    * @see JaccPolicyMBean#start()
    */
   public void start()
   {  
      //All the sub deployments have started
      try
      {
         //Let us link all the policy configurations
         for(PolicyConfigurationFacadeMBean pcfm:children)
         {
            /** The idea is that if any of the linking policy configuration
             * have committed (i.e. they are in a inService state), then they
             * cannot be linked. So we bring them to the open state by getting
             * the policy configuration from the factory and then we commit.
             */
            String jaccContextIdChild = pcfm.getJaccContextID();
            
            PolicyConfigurationFactory policyConfigurationFactory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
            PolicyConfiguration pcChild = policyConfigurationFactory.getPolicyConfiguration(jaccContextIdChild, false);
            if(pcChild != null)
            { 
               parentPC.linkConfiguration(pcChild); 
               //Commit the linked PC
               pcChild.commit();
            }
         }
         parentPC.commit();
         // Allow the policy to incorporate the policy configs
         Policy.getPolicy().refresh();
      }
      catch (Exception e)
      {
         log.error("Cannot commit Parent Policy Configuration:",e);
      }
      if(trace)
         log.trace("start():" + this.contextID);
   }

   /**
    * @see JaccPolicyMBean#stop()
    */
   public void stop()
   {  
      try
      {
         //The linked PCs will delete themselves via the PolicyConfigurationFacade
         this.parentPC.delete(); 
      }
      catch (PolicyContextException e)
      {
         throw new RuntimeException(e);
      }
      if(trace)
         log.trace("stop():" + this.contextID);
   } 
   
   /**
    * @see JaccPolicyMBean#setPolicyConfigurationFacadeMBean(PolicyConfigurationFacadeMBean)
    */
   public void setPolicyConfigurationFacadeMBean(PolicyConfigurationFacadeMBean mbeanName)
   {
      this.children.add(mbeanName);
   }
   
   private void createPolicyConfiguration() throws PolicyContextException, ClassNotFoundException 
   {
      if(parentPC == null)
      {
         PolicyConfigurationFactory pcf = PolicyConfigurationFactory.getPolicyConfigurationFactory();
         parentPC = pcf.getPolicyConfiguration(contextID, false);
      }
   }
   
   protected void createPermissions(T metaData, PolicyConfiguration policyConfiguration) 
   throws PolicyContextException
   {
      throw new RuntimeException("Need to override");
   }
}