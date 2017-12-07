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
package org.jboss.test.aop.bean;

import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.test.aop.simpleejb.SimpleHome;
import org.jboss.test.aop.simpleejb.Simple;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
/**
 *
 * @see Monitorable
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81036 $
 */
public class SimpleBeanTester
   extends ServiceMBeanSupport
   implements MBeanRegistration, SimpleBeanTesterMBean
{
   // Constants ----------------------------------------------------
   // Attributes ---------------------------------------------------
   static Logger log = Logger.getLogger(SimpleBeanTester.class);
   MBeanServer m_mbeanServer;

   // Static -------------------------------------------------------
   
   // Constructors -------------------------------------------------
   public SimpleBeanTester()
   {}
   
   // Public -------------------------------------------------------
   
   // MBeanRegistration implementation -----------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
   throws Exception
   {
      m_mbeanServer = server;
      return name;
   }
   
   public void postRegister(Boolean registrationDone)
   {}
   public void preDeregister() throws Exception
   {}
   public void postDeregister()
   {}

   protected void startService()
      throws Exception
   {
   }

   protected void stopService() {
   }

   public void testEJBCallside() throws Exception
   {
      SimpleHome home = (SimpleHome)new InitialContext().lookup("ejb/test/Simple");
      Simple bean = home.create();
      SimpleBeanCallerInterceptor.wasCalled = false;
      String value = bean.getTest();
      if (!SimpleBeanCallerInterceptor.wasCalled) throw new Exception("Caller interceptor wasn't called");
      if (!value.equals(SimpleBeanInterceptor.RETURN_VALUE)) throw new Exception("bean interceptor failed");


   }


}

