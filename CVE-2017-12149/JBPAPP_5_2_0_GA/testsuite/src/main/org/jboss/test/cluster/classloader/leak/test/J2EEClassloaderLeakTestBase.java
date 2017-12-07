/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cluster.classloader.leak.test;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.classloader.leak.ejb.interfaces.StatefulSession;
import org.jboss.test.classloader.leak.ejb.interfaces.StatefulSessionHome;
import org.jboss.test.classloader.leak.ejb.interfaces.StatelessSession;
import org.jboss.test.classloader.leak.ejb.interfaces.StatelessSessionHome;

/**
 * Base class for tests for classloader leaks following deployment, use and undeployment
 * of various J2EE packages (wars, ejb jars and ears with and without scoped
 * classloaders).
 * <p/>
 * If these tests are run with JBoss Profiler's jbossAgent (.dll or .so) on the path
 * and the AS is started with -agentlib:jbossAgent, in case of classloader leakage
 * an extensive report will be logged to the server log, showing the path to root of
 * all references to the classloader.
 * 
 * @author Brian Stansberry
 */
public class J2EEClassloaderLeakTestBase extends ClassloaderLeakTestBase
{
   private static final String EJB2_SLSB = "EJB2_SLSB";
   private static final String EJB2_SFSB = "EJB2_SFSB";
   private static final String EJB2_SLSB_TCCL = "EJB2_SLSB_TCCL";
   private static final String EJB2_SFSB_TCCL = "EJB2_SFSB_TCCL";
   
   private static final String[] EJB2 = new String[]{ EJB2_SLSB, EJB2_SLSB_TCCL, EJB2_SFSB, EJB2_SFSB_TCCL };
   
   
   public J2EEClassloaderLeakTestBase(String name)
   {
      super(name);
   }
   
   protected String getWarContextPath()
   {
      return "clustered-clleak";
   }
   
   protected String[] getEjbKeys()
   {
      return EJB2;
   }
   
   protected void makeEjbRequests() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatelessSessionHome slsbhome = (StatelessSessionHome) ctx.lookup("ClassloaderLeakStatelessSession");
      StatelessSession slsbbean = slsbhome.create();
      slsbbean.log("EJB");
      StatefulSessionHome sfsbhome = (StatefulSessionHome) ctx.lookup("ClassloaderLeakStatefulSession");
      StatefulSession sfsbbean = sfsbhome.create();
      sfsbbean.log("EJB");
   }
}
