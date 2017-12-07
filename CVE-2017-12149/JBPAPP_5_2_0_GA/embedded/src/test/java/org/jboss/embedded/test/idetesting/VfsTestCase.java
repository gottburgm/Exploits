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
package org.jboss.embedded.test.idetesting;

import junit.framework.TestCase;
import org.jboss.embedded.junit.EmbeddedTestSetup;
import org.jboss.embedded.test.vfs.HelloWorld;
import org.jboss.embedded.test.vfs.HelloWorldBean;
import org.jboss.embedded.test.vfs.DAO;
import org.jboss.embedded.test.vfs.Customer;
import org.jboss.embedded.Bootstrap;
import org.jboss.virtual.AssembledDirectory;
import org.jboss.virtual.plugins.context.vfs.AssembledContextFactory;

import javax.naming.InitialContext;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class VfsTestCase extends TestCase
{
   public VfsTestCase()
   {
      super("BootstrapTestCase");
   }


   @Override
   protected void setUp() throws Exception
   {
      EmbeddedTestSetup.testSetup();
   }


   @Override
   protected void tearDown() throws Exception
   {
      EmbeddedTestSetup.testTearDown();
   }

   public void testEJB() throws Exception
   {
      AssembledDirectory jar = AssembledContextFactory.getInstance().create("vfs-test.jar");
      jar.addClass(HelloWorld.class);
      jar.addClass(HelloWorldBean.class);

      Bootstrap.getInstance().deploy(jar);
      HelloWorld hello = (HelloWorld)new InitialContext().lookup("HelloWorldBean/local");
      hello.hello();
      Bootstrap.getInstance().undeploy(jar);
   }

   public void testVfs() throws Exception
   {
      AssembledDirectory jar = AssembledContextFactory.getInstance().create("vfs-test2.jar");
      String[] includes = {"org/jboss/embedded/test/vfs/*.class"};
      jar.addResources(DAO.class, includes, null);
      jar.mkdir("META-INF").addResource("vfs-test-persistence.xml", "persistence.xml");
      Bootstrap.getInstance().deploy(jar);
      DAO dao = (DAO)new InitialContext().lookup("DAOBean/local");
      dao.create("Bill");
      Customer cust = dao.find("Bill");
      assertNotNull(cust);

   }
}
