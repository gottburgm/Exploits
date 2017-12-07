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
package org.jboss.web.tomcat.security;

import java.security.PrivilegedActionException;

import org.apache.catalina.InstanceEvent;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.Wrapper;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.jboss.RunAsIdentityMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityUtil;

/**
 * An InstanceListener used to push/pop the servlet run-as identity for the
 * init/destroy lifecycle events.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81037 $
 */
public class RunAsListener implements InstanceListener
{
   /** There is no api to install an initialized listener so the
    * WebMetaData had to be passed via a thread local.
    */
   public static ThreadLocal<JBossWebMetaData> metaDataLocal = new ThreadLocal<JBossWebMetaData>();

   private static Logger log = Logger.getLogger(RunAsListener.class);
   private JBossWebMetaData metaData;

   public RunAsListener()
   {
      this.metaData = metaDataLocal.get();
   }

   /**
    * Push the run-as identity on the before init/destroy, pop it on the
    * after init/destroy events.
    * 
    * @param event - the type of instance event
    */ 
   public void instanceEvent(InstanceEvent event)
   {
      Wrapper servlet = event.getWrapper();
      String type = event.getType();
      if (servlet != null && metaData != null)
      {
         boolean trace = log.isTraceEnabled();
         String name = servlet.getName();
         RunAsIdentityMetaData identity = metaData.getRunAsIdentity(name);
         RunAsIdentity runAsIdentity = null;
         if(identity != null)
         {
            runAsIdentity = new RunAsIdentity(identity.getRoleName(),
                  identity.getPrincipalName(), identity.getRunAsRoles());
         }
         
         if (trace)
            log.trace(name + ", runAs: " + identity);
         // Push the identity on the before init/destroy
         if( type.equals(InstanceEvent.BEFORE_INIT_EVENT)
            || type.equals(InstanceEvent.BEFORE_DESTROY_EVENT)
            || type.equals(InstanceEvent.BEFORE_SERVICE_EVENT) )
         {
            ensureSecurityContext();
            SecurityAssociationActions.pushRunAsIdentity(runAsIdentity);
         }
         // Pop the identity on the after init/destroy
         else if( type.equals(InstanceEvent.AFTER_INIT_EVENT)
            || type.equals(InstanceEvent.AFTER_DESTROY_EVENT) 
            || type.equals(InstanceEvent.AFTER_SERVICE_EVENT))
         {
            ensureSecurityContext();
            SecurityAssociationActions.popRunAsIdentity();
         }
      }
   }
   
   /**
    * Ensure that a security context is present
    */
   private void ensureSecurityContext()
   {
      String securityDomain = metaData.getSecurityDomain();
      if(securityDomain == null)
         securityDomain = SecurityConstants.DEFAULT_APPLICATION_POLICY;
      else
         securityDomain = SecurityUtil.unprefixSecurityDomain(securityDomain);
      
      if(SecurityAssociationActions.getSecurityContext() == null)
      {
         SecurityContext sc = null;
         try
         {
            sc = SecurityAssociationActions.createSecurityContext(securityDomain);
         }
         catch (PrivilegedActionException e)
         {
            throw new RuntimeException(e);
         }
         SecurityAssociationActions.setSecurityContext(sc);
      }
   }
}
