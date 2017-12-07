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
package org.jboss.system.microcontainer;

import java.util.HashMap;
import java.util.Map;

import org.jboss.dependency.plugins.AbstractControllerContextActions;
import org.jboss.dependency.plugins.action.ControllerContextAction;
import org.jboss.dependency.spi.ControllerState;

/**
 * ServiceControllerContextActions.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ServiceControllerContextActions extends AbstractControllerContextActions
{
   /** The single instance */
   private static ServiceControllerContextActions instance;
   /** The lifecycle instance */
   private static ServiceControllerContextActions lifecycleOnly;

   /**
    * Get the instance
    * 
    * @return the actions
    */
   public static ServiceControllerContextActions getInstance()
   {
      if (instance == null)
      {
         Map<ControllerState, ControllerContextAction> actions = new HashMap<ControllerState, ControllerContextAction>();
         actions.put(ControllerState.DESCRIBED, new DescribeAction());
         actions.put(ControllerState.INSTANTIATED, new InstantiateAction());
         actions.put(ControllerState.CONFIGURED, new ConfigureAction());
         actions.put(ControllerState.CREATE, new CreateDestroyLifecycleAction());
         actions.put(ControllerState.START, new StartStopLifecycleAction());
         actions.put(ControllerState.INSTALLED, new InstallAction());
         instance = new ServiceControllerContextActions(actions);
      }
      return instance;
   }

   /**
    * Get the instance
    * 
    * @return the actions
    */
   public static ServiceControllerContextActions getLifecycleOnly()
   {
      if (lifecycleOnly == null)
      {
         Map<ControllerState, ControllerContextAction> actions = new HashMap<ControllerState, ControllerContextAction>();
         actions.put(ControllerState.INSTANTIATED, new OnlyUnregisterAction());
         actions.put(ControllerState.CREATE, new CreateDestroyLifecycleAction());
         actions.put(ControllerState.START, new StartStopLifecycleAction());
         lifecycleOnly = new ServiceControllerContextActions(actions);
      }
      return lifecycleOnly;
   }

   
   /**
    * Create a new ServiceControllerContextActions.
    * 
    * @param actions the actions
    */
   protected ServiceControllerContextActions(Map<ControllerState, ControllerContextAction> actions)
   {
      super(actions);
   }
}
