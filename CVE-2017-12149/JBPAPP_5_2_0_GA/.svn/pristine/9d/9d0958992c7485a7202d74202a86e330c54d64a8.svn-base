/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.jboss.ejb.txtimer;

import java.io.Serializable;

import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerService;

/**
 * A interface to allow the specification of the timerId when creating
 * a timer. This interface adds to the
 * <code>javax.ejb.TimerService</code> interface.  The additional
 * <code>createTimer</code> method that takes the timerId as a parameter.
 * <p>
 * This class is used address JBPAPP-3926.
 *
 * @author <a href=mailto:miclark@redhat.com">Mike M. Clark</a> 
 * @version $Revision: $
 */
public interface PersistentIdTimerService extends TimerService
{  
	public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info, String timerId)
			throws IllegalArgumentException, IllegalStateException, EJBException;
}
