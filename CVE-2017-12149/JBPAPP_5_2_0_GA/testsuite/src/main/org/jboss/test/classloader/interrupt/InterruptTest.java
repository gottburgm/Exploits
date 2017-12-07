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
package org.jboss.test.classloader.interrupt;

import org.jboss.system.ServiceMBeanSupport;

/** A simple service that creates a thread that tries to load a class
 while its interrupted flag is set. This is based on the example
 submitted with bug 563988 submitted by Harald Gliebe.

@author Scott.Stark@jboss.org
@version $Revision: 81036 $
 */
public class InterruptTest extends ServiceMBeanSupport
   implements InterruptTestMBean
{

	protected void startService() throws Exception
   {
		log.debug("Starting the TestThread");
		TestThread thread = new TestThread(this);
      thread.start();
      try
      {
         thread.join();
      }
      catch(InterruptedException e)
      {
         log.debug("Was interrupted during join", e);
      }
      log.debug("TestThread complete, ex="+thread.ex);
      if( thread.ex != null )
         throw new ExceptionInInitializerError(thread.ex);
	}
}
