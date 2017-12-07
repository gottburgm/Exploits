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
package org.jboss.test.classloader.leak.test;

import javax.naming.InitialContext;

import org.jboss.test.classloader.leak.ejb3.Ejb3StatefulSession;
import org.jboss.test.classloader.leak.ejb3.Ejb3StatelessSession;

/**
 * Similar to {@link J2EEClassloaderLeakTestBase}, but uses EJB3 instead
 * of EJB2.
 * <p/>
 * If these tests are run with JBoss Profiler's jbossAgent (.dll or .so) on the path
 * and the AS is started with -agentlib:jbossAgent, in case of classloader leakage
 * an extensive report will be logged to the server log, showing the path to root of
 * all references to the classloader.
 * 
 * @author Brian Stansberry
 */
public class Ejb3ClassloaderLeakTestBase extends ClassloaderLeakTestBase
{
   
   private static final String EJB3_SLSB = "EJB3_SLSB";
   private static final String EJB3_SFSB = "EJB3_SFSB";
   private static final String EJB3_SLSB_TCCL = "EJB3_SLSB_TCCL";
   private static final String EJB3_SFSB_TCCL = "EJB3_SFSB_TCCL";
   private static final String EJB3_TLP_SLSB = "EJB3_TLP_SLSB";
   private static final String EJB3_TLP_SLSB_TCCL = "EJB3_TLP_SLSB_TCCL";
   
   private static final String[] EJB3 = new String[]{ EJB3_SLSB, EJB3_SLSB_TCCL, EJB3_SFSB, EJB3_SFSB_TCCL, EJB3_TLP_SLSB, EJB3_TLP_SLSB_TCCL  };
   
   
   public Ejb3ClassloaderLeakTestBase(String name)
   {
      super(name);
   }
   
   protected String getWarContextPath()
   {
      return "classloader-leak-ejb3";
   }
   
   protected String[] getEjbKeys()
   {
      return EJB3;
   }
   
   protected void makeEjbRequests() throws Exception
   {
      InitialContext ctx = new InitialContext();
      Ejb3StatelessSession ejb3slsb = (Ejb3StatelessSession) ctx.lookup("Ejb3StatelessSession/remote");
      ejb3slsb.log("EJB");
      Ejb3StatefulSession ejb3sfsb = (Ejb3StatefulSession) ctx.lookup("Ejb3StatefulSession/remote");
      ejb3sfsb.log("EJB");
      Ejb3StatelessSession tlpejb3slsb = (Ejb3StatelessSession) ctx.lookup("ThreadLocalPoolEjb3StatelessSession/remote");
      tlpejb3slsb.log("EJB");
   }
}
