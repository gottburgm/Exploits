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
package org.jboss.test.ann.clustered;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.Clustered;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * @author <a href="mailto:pskopekf@redhat.com">Peter Skopek</a>
 * @version $Revision: $
 */
@Stateful
@Remote(SimpleCounter.class)
@RemoteBinding(jndiBinding=SimpleCounter.JNDI_BINDING)
@Clustered
public class SimpleCounterBean implements SimpleCounter {

	private int counter = 0;

	/* (non-Javadoc)
	 * @see org.jboss.test.ann.clustered.SimpleCounter#getCounterStatus()
	 */
	@Override
	public int getCounterStatus() {
		return counter;
	}

	/* (non-Javadoc)
	 * @see org.jboss.test.ann.clustered.SimpleCounter#increment(int)
	 */
	@Override
	public void increment(int amount) {

		counter += amount;
		
	}

	/* (non-Javadoc)
	 * @see org.jboss.test.ann.clustered.SimpleCounter#resetCounter()
	 */
	@Override
	public void resetCounter() {
		counter = 0;
	}
	
}
