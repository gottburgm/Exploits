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
package org.jboss.test.jmx.shutdown;

import javax.naming.InitialContext;

import org.jboss.system.ServiceMBeanSupport;

/** A service that calls System.exit from its stopService method. Note that
 * this service cannot be deployed when the server is shutdown as its call
 * to System.exit(0) will hang the vm in java.lang.Shutdown.exit as the
 * as the Shutdown.class monitor is already held by the signal handler.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 87269 $
 */
public class ExitOnShutdown
   extends ServiceMBeanSupport
   implements ExitOnShutdownMBean
{
   protected void startService() throws Exception
   {
      InitialContext ctx = new InitialContext();
      ctx.bind("ExitOnShutdown", Boolean.TRUE);
   }

   protected void stopService() throws Exception
   {
      Thread thread = new Thread(new Runnable()
      {
         public void run()
         {
            System.exit(0);
         }
      }, "ExitOnShutdown");
      thread.start();
      // JBAS-6759: make sure we call System.exit before we're undeployed
      thread.join(5000);
   }
}
