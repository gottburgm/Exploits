/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.deployment;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.management.ObjectName;
import javax.resource.spi.ActivationSpec;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.resource.metadata.MessageListenerMetaData;
import org.jboss.resource.metadata.RequiredConfigPropertyMetaData;

/**
 * An activation spec factory
 *
 * @author  <a href="adrian@jboss.com">Adrian Brock</a>
 * @author  <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ActivationSpecFactory
{
   /** The logger */
   private static final Logger log = Logger.getLogger(ActivationSpecFactory.class);

   @SuppressWarnings("deprecation")
   public static ActivationSpec createActivationSpec(ObjectName rarName,
                                                     String messagingType, Collection activationConfig,
                                                     MessageListenerMetaData mlmd)
      throws Exception
   {
      boolean trace = log.isTraceEnabled();
      
      if (trace)
         log.trace("Create ActivationSpec rar=" + rarName + " messagingType=" + messagingType +
            " activationConfig=" + activationConfig + " messageListner=" + mlmd);
      
      // Check we have all the required properties
      for (Iterator i = mlmd.getRequiredConfigProperties().iterator(); i.hasNext();)
      {
         RequiredConfigPropertyMetaData rcpmd = (RequiredConfigPropertyMetaData) i.next();

         String rcp = rcpmd.getName();
         String rcpName = rcp.substring(0, 1).toUpperCase();
         if (rcp.length() > 1)
            rcpName = rcpName.concat(rcp.substring(1));
         if (trace)
            log.trace("Checking required config " + rcpName);

         boolean found = false;
         for (Iterator j = activationConfig.iterator(); j.hasNext();)
         {
            org.jboss.metadata.ActivationConfigPropertyMetaData acpmd = (org.jboss.metadata.ActivationConfigPropertyMetaData) j.next();
            
            String acp = acpmd.getName();
            String acpName = acp.substring(0, 1).toUpperCase();
            if (acp.length() > 1)
               acpName = acpName.concat(acp.substring(1));

            if (trace)
               log.trace("Checking required config " + rcpName + " against " + acpName + " result=" + rcpName.equals(acpName));
            
            if (rcpName.equals(acpName))
            {
               if (trace)
                  log.trace("Found required config " + rcp + " " + acpmd);
               found = true;
               break;
            }
         }
         if (found == false)
            throw new DeploymentException("Required config property " + rcpmd + " for messagingType '" + messagingType +
               "' not found in activation config " + activationConfig + " ra=" + rarName);
      }

      // Determine the activation spec class
      String className = mlmd.getActivationSpecType();
      if (className == null)
         throw new DeploymentException("No activation spec type for messagingType '" + messagingType + "' ra=" + rarName);
      
      // Load the class
      if (trace)
         log.trace("Loading ActivationSpec class=" + className);
      Class asClass = Thread.currentThread().getContextClassLoader().loadClass(className);
      if (ActivationSpec.class.isAssignableFrom(asClass) == false)
         throw new DeploymentException(asClass.getName() + " is not an activation spec class '" + messagingType + "' ra=" + rarName);
      ActivationSpec result = (ActivationSpec) asClass.newInstance();
      if (trace)
         log.trace("Instantiated ActivationSpec class=" + result);


      Injection injector = new Injection();

      for (Iterator i = activationConfig.iterator(); i.hasNext();)
      {
         org.jboss.metadata.ActivationConfigPropertyMetaData acpmd = (org.jboss.metadata.ActivationConfigPropertyMetaData) i.next();
         String name = acpmd.getName();
         String value = acpmd.getValue();

         if (trace)
            log.trace(name + "=" + value + " to " + asClass.getName());

         try
         {
            injector.inject(result, name, value);
         }
         catch (Throwable t)
         {
            log.warn("Unable to set '" + name + "' property on " + asClass.getName());
         }
      }

      // Validate the activation spec
      try
      {
         if (trace)
            log.trace("Trying to validate ActivationSpec " + result);
         result.validate();
      }
      catch (UnsupportedOperationException e)
      {
         log.debug("Validation is not supported for ActivationSpec: " + className);
      }
      
      return result;
   }
}
