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
import javax.naming.InitialContext;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jmx.eardeployment.a.interfaces.SessionAHome;
import org.jboss.test.jmx.eardeployment.a.interfaces.SessionA;


/** Tests of Manifest ClassPath behavior.
 *
 * @author <a href="mailto:julien_viet@yahoo.fr">Julien Viet</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class CPManifestUnitTestCase extends JBossTestCase 
{

   public CPManifestUnitTestCase(String name)
   {
      super(name);
   }

   /** Test that an ear with the following structure:
      cpmanifest.ear
        -+ abstract.jar
        -+ concrete.jar, -> ClassPath: abstract.jar
    whose application.xml only refers to concrete.jar is able to deploy
    because abstract.jar is loaded due to the concrete.jar manifest ClassPath.
    */
   public void testEarJarManifest() throws Exception
   {
      getLog().info("+++ testEarJarManifest");
      deploy("cpmanifest.ear");
      Object home = getInitialContext().lookup("Concrete");
      getLog().info("Found Concrete home="+home);
      undeploy("cpmanifest.ear");
   }

   /** Test that an ear with the following structure:
      cpcircular-manifest.ear
        -+ abstract2.jar, -> ClassPath: concrete2.jar
        -+ concrete2.jar, -> ClassPath: abstract2.jar
    whose application.xml only refers to concrete.jar does not cause the
    server to spin due to the circular ClassPath references.
    */
   public void testCircularManifest() throws Exception
   {
      getLog().info("+++ testCircularManifest");
      deploy("cpcircular-manifest.ear");
      Object home = getInitialContext().lookup("Concrete");
      getLog().info("Found Concrete home="+home);
      undeploy("cpcircular-manifest.ear");
   }

   /** Test that an ear with the following structure:
      external.ear
        -+ external.sar, -> ClassPath: external-util.jar
        -+ external-util.jar
    whose jboss-app.xml only refers to external.sar is able to
    load the mbean service in the external.sar
    */
   public void testSARManifest() throws Exception
   {
      getLog().info("+++ testSARManifest");
      deploy("external.ear");
      ObjectName serviceName = new ObjectName("test:name=ExternalClass");
      boolean isRegisterd = getServer().isRegistered(serviceName);
      assertTrue("ExternalClass service is registered", isRegisterd);
      undeploy("external.ear");
   }

   /** Test that an ear with the following structure:
      cpejbs-manifest.ear
        -+ ejbjar1.jar, -> ClassPath: ejbjar2.jar
        -+ ejbjar2.jar, -> ClassPath: ejbjar1.jar lib/cpm-util.jar
    loads the ejbs.
    */
   public void testEJBJarManifest() throws Exception
   {
      getLog().info("+++ testEJBJarManifest");
      deploy("cpejbs-manifest.ear");
      ObjectName ejb1Name = new ObjectName("jboss.j2ee:service=EJB,jndiName=eardeployment/SessionA");
      boolean isRegisterd = getServer().isRegistered(ejb1Name);
      assertTrue("eardeployment/SessionA is registered", isRegisterd);
      ObjectName ejb2Name = new ObjectName("jboss.j2ee:service=EJB,jndiName=eardeployment/SessionB");
      isRegisterd = getServer().isRegistered(ejb2Name);
      assertTrue("eardeployment/SessionB is registered", isRegisterd);

        InitialContext ctx = new InitialContext();
        SessionAHome home = (SessionAHome) ctx.lookup("eardeployment/SessionA");
        SessionA bean = home.create();
        bean.callB();
        bean.remove();
      undeploy("cpejbs-manifest.ear");
   }
}

