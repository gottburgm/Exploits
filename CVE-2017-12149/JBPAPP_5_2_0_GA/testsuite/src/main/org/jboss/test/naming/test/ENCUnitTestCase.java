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
package org.jboss.test.naming.test;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.naming.interfaces.TestENCHome;
import org.jboss.test.naming.interfaces.TestENCHome2;
import org.jboss.test.naming.interfaces.TestENC;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Tests of the secure access to EJBs.
 *
 * @author   Scott.Stark@jboss.org
 * @author   <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 105321 $
 */
public class ENCUnitTestCase extends JBossTestCase
{
   /**
    * Constructor for the ENCUnitTestCase object
    *
    * @param name  Testcase name
    */
   public ENCUnitTestCase(String name)
   {
      super(name);
   }

   /** Tests of accessing the various types of java:comp entries
    *
    * @exception Exception  Description of Exception
    */
   public void testENC() throws Exception
   {
      Object obj = getInitialContext().lookup("ENCBean");
      obj = PortableRemoteObject.narrow(obj, TestENCHome.class);
      TestENCHome home = (TestENCHome)obj;
      getLog().debug("Found TestENCHome");

      TestENC bean = home.create();
      getLog().debug("Created ENCBean");
      bean.accessENC();
      bean.remove();
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testENC2() throws Exception
   {
      Object obj = getInitialContext().lookup("ENCBean0");
      obj = PortableRemoteObject.narrow(obj, TestENCHome2.class);
      TestENCHome2 home = (TestENCHome2)obj;
      getLog().debug("Found TestENCHome2");

      TestENC bean = home.create();
      getLog().debug("Created ENCBean0");
      bean.accessENC();
      bean.remove();
   }
   
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
   
      suite.addTest(new JBossTestSetup(new TestSuite(ENCUnitTestCase.class))
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            JMSDestinationsUtil.setupBasicDestinations();
            deploy ("naming.jar");
         }
         protected void tearDown() throws Exception
         {
            super.tearDown();
            undeploy ("naming.jar");
            JMSDestinationsUtil.destroyDestinations();
         }
      });
      
      return suite;
   }


}
