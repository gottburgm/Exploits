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
package org.jboss.test.testbyvalue.test;

import java.rmi.*;


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ejb.DuplicateKeyException;
import javax.ejb.Handle;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import javax.ejb.HomeHandle;
import javax.rmi.PortableRemoteObject;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.testbyvalue.interfaces.RootStatelessSessionHome;

/**
* @author Clebert Suconic
*/
public class ByValueUnitTestCase
extends JBossTestCase
{
   static boolean deployed = false;
   static int test = 0;
   static Date startDate = new Date();

   protected final String namingFactory =
   System.getProperty(Context.INITIAL_CONTEXT_FACTORY);

   protected final String providerURL =
   System.getProperty(Context.PROVIDER_URL);
    private static final int ITERATIONS = 1000;

    public ByValueUnitTestCase(String name) {
      super(name);
   }

    public void testByValue() throws Exception
    {
        InitialContext ctx = new InitialContext();
        Object obj = ctx.lookup("RootTestByValue");

        RootStatelessSessionHome home = (RootStatelessSessionHome) PortableRemoteObject.narrow(obj, RootStatelessSessionHome.class);
        long time = home.create().doTestByValue(ITERATIONS);
        // I really wanted System.out as I wanted this to be sent to the XML output for ant junit tests
        System.out.println("Time spent into CallByValues over "+ITERATIONS+" iterations=" + time);
    }

    public void testByValueOnEntity() throws Exception
    {
        InitialContext ctx = new InitialContext();
        Object obj = ctx.lookup("RootTestByValue");

        RootStatelessSessionHome home = (RootStatelessSessionHome) PortableRemoteObject.narrow(obj, RootStatelessSessionHome.class);
        long time = home.create().doTestEntity(ITERATIONS);
        // I really wanted System.out as I wanted this to be sent to the XML output for ant junit tests
        System.out.println("Time spent into CallByValues over "+ITERATIONS+" iterations=" + time);
    }

    public void testByReferenceOnEntity() throws Exception
    {
        InitialContext ctx = new InitialContext();
        Object obj = ctx.lookup("RootTestByValue");

        RootStatelessSessionHome home = (RootStatelessSessionHome) PortableRemoteObject.narrow(obj, RootStatelessSessionHome.class);
        long time = home.create().doTestEntityByReference(ITERATIONS);
        // I really wanted System.out as I wanted this to be sent to the XML output for ant junit tests
        System.out.println("Time spent into CallByValues over "+ITERATIONS+" iterations=" + time);
    }

    public void testByReference() throws Exception
    {
        InitialContext ctx = new InitialContext();
        Object obj = ctx.lookup("RootTestByValue");

        RootStatelessSessionHome home = (RootStatelessSessionHome) PortableRemoteObject.narrow(obj, RootStatelessSessionHome.class);
        long time = home.create().doTestByReference(ITERATIONS);
        // I really wanted System.out as I wanted this to be sent to the XML output for ant junit tests
        System.out.println("Time spent into CallByReferences over "+ITERATIONS+" iterations=" + time);
    }

   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(ByValueUnitTestCase.class, "jar-byvalue.jar");
      return t1;
   }

}
