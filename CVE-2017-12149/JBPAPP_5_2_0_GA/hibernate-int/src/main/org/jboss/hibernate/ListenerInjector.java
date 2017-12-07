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
package org.jboss.hibernate;

import org.hibernate.cfg.Configuration;
import org.jboss.deployment.DeploymentException;

/**
 * Implementors are responsible for injecting any custom Hibernate3 event listeners into the {@link
 * org.hibernate.cfg.Configuration} which will then later be used to build the {@link org.hibernate.SessionFactory}.
 * <p/>
 * Implementors should have a no-arg constructor.
 *
 * @author <a href="mailto:steve@hibernate.org">Steve Ebersole</a>
 * @version <tt>$Revision: 81017 $</tt>
 */
public interface ListenerInjector
{
	/**
	 * Called by the {@link org.jboss.hibernate.jmx.Hibernate} MBean when it is time to generate any custom listeners.
	 * <p/>
	 * Implementors should use the {@link Configuration#setListener(String, Object)} method to inject the appropriate
	 * listener instance(s).
	 * <p/>
	 * Note that the {@link org.hibernate.SessionFactory} is not yet available; it has not even beeen built at this time.
	 * <p/>
	 * Note that it is possible to actually set some properties on the incoming configuration instance.  It is not
	 * advisable to do this with any Hibernate-specific settings as the MBean will have final say after execution of this
	 * method regarding any settings it manages (potentially over-writing a setting done during this execution).  Maybe
	 * useful for the listeners, themselves, being able to read custom settings later from the {@link
	 * org.hibernate.SessionFactory}.
	 *
    * @param objectName The bean name.
	 * @param configuration The configuration into which the customer listeners should be injected.
	 *
	 * @throws DeploymentException If any problems occur which should lead to a deployment failure (i.e. do not build the
	 * {@link org.hibernate.SessionFactory}).
	 */
	public void injectListeners(Object objectName, Configuration configuration) throws DeploymentException;
}
