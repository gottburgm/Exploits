/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.system.server.jmx;

import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.system.server.jmx.JMXKernel;

/**
 * An MBean that exposes through JMX the ability to shutdown the server with a particular
 * exit code.
 *
 * @author <a href="mailto:csams@redhat.com">Chris Sams</a>
 */
@JMX(name="jboss.system:type=JVMShutdown", exposedInterface=JVMShutdownMBean.class)
public class JVMShutdown implements JVMShutdownMBean {
	
	private JMXKernel server;

	public void exit(final int exitStatus)
	{
		server.exit(exitStatus);
	}

	public void exit()
	{
		server.exit();
	}

	public void halt(final int exitStatus)
	{
		server.halt(exitStatus);
	}

	public void halt()
	{
		server.halt();
	}

	public void shutdown()
	{
		server.shutdown();
	}
	
	public void setJMXKernel(final JMXKernel server)
	{
		this.server = server;
	}

}
