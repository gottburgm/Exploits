/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.profileservice.lib.startup;

import org.jboss.kernel.spi.event.KernelEvent;
import org.jboss.kernel.spi.event.KernelEventListener;
import org.jboss.logging.Logger;

/**
 * A bean to test receipt of the profile load notification.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85526 $
 */
public class StartupMonitor
   implements KernelEventListener
{
   private static final Logger log = Logger.getLogger(StartupMonitor.class);
   public void onEvent(KernelEvent event, Object handback)
   {
      log.debug(this + " onEvent=" + event + " handback=" + handback);
   }
}
