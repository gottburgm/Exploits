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
package org.jboss.test.retry.test;

import java.rmi.*;


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ejb.DuplicateKeyException;
import javax.ejb.Handle;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import javax.ejb.HomeHandle;

import java.util.Date;
import java.util.Properties;
import java.util.Collection;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.test.retry.interfaces.*;

import org.jboss.test.JBossTestCase;
import org.jboss.invocation.MarshalledValue;

/**
* Sample client retry tester
*
* @author <a href="mailto:bill@ejboss.org">Bill Burke</a>
* @author <a href="mailto:hugo@hugopinto.com">Hugo Pinto</a>
* @version $Id: RetryUnitTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
*/
public class RetryUnitTestCase
   extends JBossTestCase
{
   public RetryUnitTestCase(String name) {
      super(name);
   }


   public void testRetry()
      throws Exception
   {
      RetrySessionHome home = (RetrySessionHome)new InitialContext().lookup("RetrySession");
      RetrySession bean = home.create();
      bean.retry();
   }

   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(RetryUnitTestCase.class, "retry.jar");
      return t1;
   }

}
