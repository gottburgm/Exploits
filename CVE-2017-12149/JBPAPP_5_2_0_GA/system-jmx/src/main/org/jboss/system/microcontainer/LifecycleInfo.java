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

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;

import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.ControllerStateModel;
import org.jboss.system.ServiceController;

/**
 * Holds the needed kernel bus lifecycle invocation info.
 * Used by {@link ServiceControllerContext#lifecycleInvocation(String, Object[], String[])}
 * to make invocations on the MBean lifecycle methods.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class LifecycleInfo
{
   private ServiceControllerContext context;
   private Map<String, StateInfo> lifecycleOps;

   /**
    * @param context the ServiceControllerContext
    * @throws Throwable for any error
    */
   public LifecycleInfo(ServiceControllerContext context) throws Throwable
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");

      this.context = context;
      // build possible lifecycle ops
      lifecycleOps = new HashMap<String, StateInfo>();
      lifecycleOps.put("create", new StateInfo(false, true, ControllerState.CREATE));
      lifecycleOps.put("start", new StateInfo(false, true, ControllerState.INSTALLED));
      lifecycleOps.put("stop", new StateInfo(false, false, ControllerState.CREATE));
      lifecycleOps.put("destroy", new StateInfo(false, false, ControllerState.CONFIGURED));

      ServiceController controller = context.getServiceController();
      MBeanServer server = controller.getMBeanServer();
      if (server != null)
      {
         MBeanInfo info = server.getMBeanInfo(context.getObjectName());
         MBeanOperationInfo[] ops = info.getOperations();
         if (ops != null)
         {
            for (MBeanOperationInfo op : ops)
            {
               String name = op.getName();

               StateInfo flag = lifecycleOps.get(name);
               if (flag == null)
               {
                  continue;
               }

               // Validate that is a no-arg void return type method
               if (op.getReturnType().equals("void") == false)
               {
                  continue;
               }
               if (op.getSignature().length != 0)
               {
                  continue;
               }

               flag.opExists = true;
            }
         }
      }
   }

   /**
    * Is this invocation a lifecycle invocation.
    *
    * Return state value to which this context should be moved
    * or return current state if we're already past the lifecycle state
    * or null if the invocation is actually not a lifecycle invocation.
    *
    * @param opName operation name
    * @param signature method's parameter types / signatures
    * @return state to which we should move this context, or null if this is not lifecycle invocation
    * @throws Throwable for any error
    */
   public ControllerState lifecycleInvocation(String opName, String[] signature) throws Throwable
   {
      if (signature != null && signature.length > 0)
         return null;

      StateInfo flag = lifecycleOps.get(opName);
      if (flag == null || flag.opExists == false)
         return null;

      Controller controller = context.getController();
      ControllerStateModel model = controller.getStates();
      ControllerState state = context.getState();
      if (flag.installPhase)
      {
         if (model.isAfterState(flag.state, state))
            return flag.state;
         else
            return state;
      }
      else
      {
         if (model.isBeforeState(flag.state, state))
            return flag.state;
         else
            return state;
      }
   }

   /**
    * State info holder.
    */
   private class StateInfo
   {
      boolean opExists;
      boolean installPhase;
      ControllerState state;

      private StateInfo(boolean opExists, boolean installPhase, ControllerState state)
      {
         this.opExists = opExists;
         this.installPhase = installPhase;
         this.state = state;
      }
   }
}
