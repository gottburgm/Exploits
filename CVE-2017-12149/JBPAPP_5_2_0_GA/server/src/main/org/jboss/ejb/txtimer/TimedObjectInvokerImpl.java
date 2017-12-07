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
package org.jboss.ejb.txtimer;

// $Id: TimedObjectInvokerImpl.java 81030 2008-11-14 12:59:42Z dimitris@jboss.org $

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.PayloadKey;
import org.jboss.security.RunAs;
import org.jboss.security.RunAsIdentity; 
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SecurityIdentityMetaData;
import org.jboss.metadata.AssemblyDescriptorMetaData;
import org.jboss.metadata.ApplicationMetaData;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * An implementation of a TimedObjectInvoker, that can invoke deployed
 * EB, SLSB, and MDB
 *
 * @author Thomas.Diesler@jboss.org
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 * @since 07-Apr-2004
 */
public class TimedObjectInvokerImpl implements TimedObjectInvoker
{
   private Container container;
   private TimedObjectId timedObjectId;
   private Method method;
   boolean pushedRunAs = false;

   public TimedObjectInvokerImpl(TimedObjectId timedObjectId, Container container)
   {
      try
      {
         this.container = container;
         this.timedObjectId = timedObjectId;
         this.method = TimedObject.class.getMethod("ejbTimeout", new Class[]{Timer.class});

      }
      catch (NoSuchMethodException ignore)
      {
      }
   }

   /**
    * Invokes the ejbTimeout method on the TimedObject with the given id.
    *
    * @param timer The Timer that is passed to ejbTimeout
    */
   public void callTimeout(Timer timer)
           throws Exception
   {
      ClassLoader callerClassLoader = SecurityActions.getContextClassLoader();
      SecurityActions.setContextClassLoader(container.getClassLoader());
      container.pushENC();
      try
      {
         Invocation inv = new Invocation(timedObjectId.getInstancePk(), method, new Object[]{timer}, null, null, null);
         inv.setValue(InvocationKey.INVOKER_PROXY_BINDING, null, PayloadKey.AS_IS);
         inv.setType(InvocationType.LOCAL); 
         
         BeanMetaData bmd = container.getBeanMetaData();
         SecurityIdentityMetaData ejbTimeoutIdentity = bmd.isEntity() ? null : bmd.getEjbTimeoutIdentity();
         if( ejbTimeoutIdentity != null && ejbTimeoutIdentity.getUseCallerIdentity() == false )
         {
            ApplicationMetaData applicationMetaData = bmd.getApplicationMetaData();
            AssemblyDescriptorMetaData assemblyDescriptor = applicationMetaData.getAssemblyDescriptor();
            String roleName = ejbTimeoutIdentity.getRunAsRoleName();
            String principalName = ejbTimeoutIdentity.getRunAsPrincipalName();
            // the run-as principal might have extra roles mapped in the assembly-descriptor
            Set extraRoleNames = assemblyDescriptor.getSecurityRoleNamesByPrincipal(principalName);
            RunAs runAsIdentity = new RunAsIdentity(roleName, principalName, extraRoleNames);
            SecurityActions.pushRunAsIdentity(runAsIdentity);
            pushedRunAs = true;
         }
         container.invoke(inv);
      }
      finally
      {
         container.popENC();
         if(pushedRunAs)
            SecurityActions.popRunAsIdentity();
         SecurityActions.setContextClassLoader(callerClassLoader);
      }
   } 
}
