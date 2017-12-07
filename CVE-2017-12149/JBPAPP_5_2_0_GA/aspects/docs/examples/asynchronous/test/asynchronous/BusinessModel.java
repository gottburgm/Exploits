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
package test.asynchronous;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 *
 * @version <tt>$Revision: 80996 $</tt>
 * @author  <a href="mailto:chussenet@yahoo.com">{Claude Hussenet Independent Consultant}</a>.
 */


public class BusinessModel {
	protected long sleepTime = 100;
	public boolean bCleanupCalled = false;
	public BusinessModel() {}
	public BusinessModel(long sleepTime) {
		this.sleepTime = sleepTime;
	}
	public static void sleep(long sleep) {
		try {
			Thread.sleep(sleep);
		} catch (Exception e) {}
	}
	/**
	 * @@org.jboss.aspects.asynchronous.aspects.jboss.Asynchronous
 	 */
	public void processBusinessModel() {
		sleep(sleepTime);
	}
	/**
	 * @@org.jboss.aspects.asynchronous.aspects.jboss.Asynchronous
	 */
	private void processBusinessModel3() {
		sleep(sleepTime);
	}

	/**
	 * @@org.jboss.aspects.asynchronous.aspects.jboss.Asynchronous
	 */
	public void callPrivateMethod() {
		processBusinessModel3();
	}

 	/**
 	 * @@org.jboss.aspects.asynchronous.aspects.jboss.Asynchronous
	 */
	public long processBusinessModel2(long aSleepTime) {
		if (aSleepTime < 0) {
			int a = 0 / 0;
		}
		sleep(aSleepTime);
		return aSleepTime;
	}
 	/**
 	 * @@org.jboss.aspects.asynchronous.aspects.jboss.Asynchronous
	 */
	static public long processBusinessModel4(
		long aSleepTime,
		Parameter parameter) {
		sleep(aSleepTime);
		return aSleepTime;
	}
}
