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
package org.jboss.test.jmx.test;


import javax.management.ObjectName;
import junit.framework.*;
import org.jboss.test.JBossTestCase;



/**
 * JarInSarJSR77UnitTestCase.java
 *
 *
 * Created: Sat Mar 2 23:54:55 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class JarInSarJSR77UnitTestCase extends JBossTestCase 
{
   public JarInSarJSR77UnitTestCase(String name)
   {
      super(name);
   }

   /**
    * The <code>testFakeParentCreatedAndRemoved</code> method tests if a package
    * consisting of a sar (which has no jsr-77 representation as of this writing) 
    * containing a jar (which does) gets deployed successfully with jsr-77 mbeans.
    * in particular, a fake Application parent for the jar should be created.
    * if and when the jsr77 support is extended to sars, this test should be reexamined.
    * 
    *
    * @exception Exception if an error occurs
    */
   public void testFakeParentCreatedAndRemoved() throws Exception
   {
      String testUrl = "jarinsar.sar";
      getLog().debug("testUrl is : " + testUrl);
      ObjectName fakeApp = new ObjectName("jboss.management.local:J2EEServer=Local,name=jarinsar.sar,j2eeType=J2EEApplication");
      ObjectName ejbModule = new ObjectName("jboss.management.local:name=jarinsar.jar,J2EEServer=Local,J2EEApplication=jarinsar.sar,j2eeType=EJBModule");
      ObjectName bean = new ObjectName("jboss.management.local:J2EEServer=Local,name=TestDataSource,J2EEApplication=jarinsar.sar,EJBModule=jarinsar.jar,j2eeType=StatelessSessionBean");

      //deploy the test package.
      deploy(testUrl);
      try 
      {
         assertTrue("fakeApp jsr-77 mbean is missing", getServer().isRegistered(fakeApp));
         assertTrue("ejbModule jsr-77 mbean is missing", getServer().isRegistered(ejbModule));
         assertTrue("bean jsr-77 mbean is missing", getServer().isRegistered(bean));
      }
      finally
      {
         undeploy(testUrl);
         assertTrue("fakeApp jsr-77 mbean is still present", !getServer().isRegistered(fakeApp));
         assertTrue("ejbModule jsr-77 mbean is still present", !getServer().isRegistered(ejbModule));
         assertTrue("bean jsr-77 mbean is still present", !getServer().isRegistered(bean));

      } // end of try-catch
      
   }


   
}// JarInSarJSR77UnitTestCase
