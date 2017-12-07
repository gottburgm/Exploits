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
package org.jboss.test.ann.clustered.test;

import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.ann.clustered.SimpleCounter;

/**
 * @author <a href="mailto:pskopekf@redhat.com">Peter Skopek</a>
 * @version $Revision: $
 */
public class Ejb3ClusteredAnnTestCase extends JBossClusteredTestCase {

	public static final String DEPLOYMENT_NAME = "clusterannottest.jar";

	public static int INCREMENT = 10;
	public static int CYCLES = 50;

	private static boolean deployed0_ = true;
	private static boolean deployed1_ = true;

	public Ejb3ClusteredAnnTestCase(String name) {
		super(name);
	}

	public void testAppSetup() throws Exception {

		String[] urls = getNamingURLs();
		Properties env = new Properties();
		env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		env.setProperty(Context.PROVIDER_URL, urls[0]);
		Context ctx = new InitialContext(env);
		getLog().debug("InitialContext URLs " + urls[0]);

		SimpleCounter counter = (SimpleCounter) ctx
				.lookup(SimpleCounter.JNDI_BINDING);
		for (int i = 0; i < CYCLES; i++) {
			counter.increment(INCREMENT);
		}

		assertTrue("Counter has to show " + CYCLES * INCREMENT
				+ ", but shows " + counter.getCounterStatus(), counter
				.getCounterStatus() == CYCLES * INCREMENT);

	}

	public void testOneNodeFail() throws Exception {

		MBeanServerConnection[] adaptors = getAdaptors();

		String[] urls = getNamingURLs();
		Properties env = new Properties();
		env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		env.setProperty(Context.PROVIDER_URL, urls[0]);
		Context ctx = new InitialContext(env);
		getLog().debug("InitialContext URLs " + urls[0]);

		SimpleCounter counter = (SimpleCounter) ctx
				.lookup(SimpleCounter.JNDI_BINDING);
		for (int i = 0; i < CYCLES; i++) {
			counter.increment(INCREMENT);

			if (i == CYCLES / 2) {
				MBeanServerConnection adaptor = adaptors[0];
				log.debug("Trying to undeploy " + DEPLOYMENT_NAME + " from " + adaptor);
				undeploy(adaptors[0], DEPLOYMENT_NAME);
				log.debug("Trying to undeployed " + DEPLOYMENT_NAME + " from " + adaptor);
				
				sleep(2000);
			}

		}

		assertTrue("Counter has to show " + CYCLES * INCREMENT + ", but shows "
				+ counter.getCounterStatus(),
				counter.getCounterStatus() == CYCLES * INCREMENT);

	}

	protected void setUp() throws Exception {
		super.setUp();
		configureCluster();
	}

	public static Test suite() throws Exception {
		return JBossClusteredTestCase.getDeploySetup(
				Ejb3ClusteredAnnTestCase.class, DEPLOYMENT_NAME);
	}

	protected void configureCluster() throws Exception {
		MBeanServerConnection[] adaptors = getAdaptors();
		if (!deployed0_) {
			deploy(adaptors[0], DEPLOYMENT_NAME);
			getLog().debug("Deployed " + DEPLOYMENT_NAME + " on server0");
			deployed0_ = true;
		}
		if (!deployed1_) {
			deploy(adaptors[1], DEPLOYMENT_NAME);
			getLog().debug("Deployed " + DEPLOYMENT_NAME + " on server1");
			deployed1_ = true;
		}

		sleep(2000);
	}

}
