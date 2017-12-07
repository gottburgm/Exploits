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

import org.jboss.dependency.plugins.action.SimpleControllerContextAction;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.logging.Logger;

/**
 * ServiceControllerContextAction.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 85945 $
 */
public class ServiceControllerContextAction extends SimpleControllerContextAction<ServiceControllerContext>
{
   protected Logger log = Logger.getLogger(getClass());

   protected ServiceControllerContext contextCast(ControllerContext context)
   {
      return ServiceControllerContext.class.cast(context);
   }

   protected boolean validateContext(ControllerContext context)
   {
      return (context instanceof ServiceControllerContext);
   }

   public void installAction(ServiceControllerContext context) throws Throwable
   {
   }

   public void uninstallAction(ServiceControllerContext context)
   {
   }
}
