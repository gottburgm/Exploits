/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.test.ejb3.jbpapp923.unit;

import junit.framework.Test;
import org.jboss.test.ejb3.common.EJB3TestCase;
import org.jboss.test.ejb3.jbpapp923.RemoteA;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class ExtendedPersistenceContextPropagationUnitTestCase extends EJB3TestCase {
    public ExtendedPersistenceContextPropagationUnitTestCase(String name) {
        super(name);
    }

    public void testCheck() throws Exception {
        final RemoteA beanA = lookup("BeanA/remote", RemoteA.class);
        beanA.create();
        final boolean result = beanA.check();
        assertTrue("JBPAPP-923: entity should remain attached after calling another EJB", result);
    }

    public void testCheckInTx() throws Exception {
        final RemoteA beanA = lookup("BeanA/remote", RemoteA.class);
        beanA.create();
        final boolean result = beanA.checkInTx();
        assertTrue("JBPAPP-923: entity should remain attached after calling another EJB", result);
    }

    public static Test suite() throws Exception {
        return getDeploySetup(ExtendedPersistenceContextPropagationUnitTestCase.class, "jbpapp923.jar");
    }
}
