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
package org.jboss.test.jca.test;

import java.net.URL;

import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.interfaces.JDBCComplianceSession;
import org.jboss.test.jca.interfaces.JDBCComplianceSessionHome;

public class JDBCComplianceUnitTestCase extends JBossTestCase
{
   private JDBCComplianceSession ejb;
   private JDBCComplianceSessionHome home;
   
   public JDBCComplianceUnitTestCase(String name)
   {
      super(name);
      
   }

   public void testCloseJDBCCompliance() throws Exception
   {
      
      try
      {
         
         ejb.testJdbcCloseCompliance();
         
      }catch(Throwable t)
      {
         fail("Duplicate close should not throw exception");
         
      }
      
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      InitialContext ctx = super.getInitialContext();
      Object anon = ctx.lookup("JDBCComplianceBean");
      home = (JDBCComplianceSessionHome)PortableRemoteObject.narrow(anon, JDBCComplianceSessionHome.class);
      ejb = home.create();
      
   }
   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(JDBCComplianceUnitTestCase.class, "jdbc-comp-ejb.jar");
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL url = cl.getResource("jca/compliance/compliance-ds.xml");
      Test t2 = getDeploySetup(t1, url.toString());
      return t2;
   }
}
