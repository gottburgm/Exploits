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

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;

import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.support.IdMetaData;


/**
 *  A facade for the JACC Policy Configuration
 *  for deployments that are not the top level deployments
 *  (Eg: WARs, EJB-Jars sitting inside EAR deployments)
 *  @author Anil.Saldhana@redhat.com
 *  @since  Apr 23, 2007 
 *  @version $Revision: 85945 $
 */
public abstract class PolicyConfigurationFacade<T extends IdMetaData> 
implements PolicyConfigurationFacadeMBean
{   
   protected static Logger log = Logger.getLogger(PolicyConfigurationFacade.class);
   protected boolean trace = log.isTraceEnabled();
   private String jaccContextId;
   private PolicyConfiguration policyConfiguration; 
   private T metaData; 
   
   public PolicyConfigurationFacade(String id, T md)
   {
      if(id == null)
         throw new IllegalArgumentException("Context ID is null");
      if(md == null)
         throw new IllegalArgumentException("Meta Data is null");
      this.jaccContextId = id; 
      this.metaData = md;
   }
   
   public void create()
   { 
      try
      { 
         PolicyConfigurationFactory pcf = PolicyConfigurationFactory.getPolicyConfigurationFactory();
         policyConfiguration = pcf.getPolicyConfiguration(this.jaccContextId, false);
         
         createPermissions(metaData, policyConfiguration);
      }
      catch (ClassNotFoundException e)
      {
         new RuntimeException(e);
      }
      catch (PolicyContextException e)
      {
         new RuntimeException(e);
      }
      if(trace)
        log.trace("Create:contextID=" + this.jaccContextId);    
   }

   public void destroy()
   { 
      if(trace)
         log.trace("destroy : " + this.jaccContextId);
      this.policyConfiguration = null;
   }

   public void start()
   {
      if(trace)
        log.trace("Start : contextId=" + this.jaccContextId);
   }

   public void stop()
   { 
      try
      {
         this.policyConfiguration.delete();
      }
      catch (PolicyContextException e)
      {
         new RuntimeException(e);
      }
      if(trace)
         log.trace("stop : " + this.jaccContextId);
   } 
   
   /**
    * @see PolicyConfigurationFacadeMBean#getPolicyConfiguration()
    */
   public PolicyConfiguration getPolicyConfiguration()
   {
      return this.policyConfiguration;
   }
   
   /**
    * @see PolicyConfigurationFacadeMBean#getJaccContextID()
    */
   public String getJaccContextID()
   {
      return this.jaccContextId;
   } 
   
   /**
    * Subclasses should override to create the Jacc Permissions
    * in the PolicyConfiguration
    * @param metaData
    * @param policyConfiguration
    * @throws PolicyContextException
    */
   protected abstract void createPermissions(T metaData, 
         PolicyConfiguration policyConfiguration) throws PolicyContextException;
}