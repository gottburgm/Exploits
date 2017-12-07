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
package org.jboss.test.cmp2.cmrtransaction.test;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.cmp2.cmrtransaction.interfaces.TreeFacadeHome;
import org.jboss.test.cmp2.cmrtransaction.interfaces.TreeFacade;

/**
 * @author  B Stansberry brian_stansberry@wanconcepts.com
 */
public class CMRTransactionUnitTestCase extends JBossTestCase
{
    // -------------------------------------------------------------  Constants

    // -------------------------------------------------------  Instance Fields

    // ----------------------------------------------------------  Constructors

    public CMRTransactionUnitTestCase(String name)
    {
        super(name);
    }

    // --------------------------------------------------------  Public Methods

    public void testCMRTransaction() throws Exception
    {

        InitialContext ctx = getInitialContext();
        Object obj = ctx.lookup("cmrTransactionTest/TreeFacadeRemote");
        TreeFacadeHome home = (TreeFacadeHome)
                PortableRemoteObject.narrow(obj, TreeFacadeHome.class);
        TreeFacade facade = home.create();
        facade.setup();
        facade.createNodes();

        int waitTime = 0;

        CMRTransactionThread rearrange = new CMRTransactionThread(facade);
        rearrange.start();
        rearrange.join();

        if (rearrange.exception != null)
        {
            fail(rearrange.exception.getMessage());
        }

        assertTrue(rearrange.finished);
    }

    // --------------------------------------------------------  Static Methods

    public static Test suite() throws Exception
    {
        return getDeploySetup(CMRTransactionUnitTestCase.class, "cmp2-cmrtransaction.jar");
    }

    // ---------------------------------------------------------  Inner Classes

    class CMRTransactionThread extends Thread
    {
        boolean finished = false;
        Exception exception = null;
        TreeFacade treeFacade = null;

        CMRTransactionThread(TreeFacade facade)
        {
            treeFacade = facade;
        }

        public void run()
        {
            try
            {
                treeFacade.rearrangeNodes();
            }
            catch (Exception e)
            {
                exception = e;
            }
            finally
            {
                finished = true;
            }
        }
    }

}
