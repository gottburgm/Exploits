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
package org.jboss.test.jrmp.test;

import javax.naming.InitialContext;

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jrmp.interfaces.StatelessSession;
import org.jboss.test.jrmp.interfaces.StatelessSessionHome;

/**
 * Test of using custom RMI socket factories with the JRMP ejb container
 * invoker.
 *
 * @author  Scott.Stark@jboss.org
 * @author    david jencks d_jencks@users.sourceforge.net
 * @version   $Revision: 81036 $
 */
public class CustomSocketsUnitTestCase extends JBossTestCase
{
   /**
    * Constructor for the CustomSocketsUnitTestCase object
    *
    * @param name  Description of Parameter
    */
   public CustomSocketsUnitTestCase(String name)
   {
      super(name);
   }


   /**
    * Test access of a custom type over the compressed socket
    *
    * @exception Exception  Description of Exception
    */
   public void testCustomAccess() throws Exception
   {
      log.info("+++ testCustomAccess");
      InitialContext jndiContext = new InitialContext();
      log.debug("Lookup StatelessSessionWithGZip");
      Object obj = jndiContext.lookup("StatelessSessionWithGZip");
      StatelessSessionHome home = (StatelessSessionHome)obj;
      log.debug("Found StatelessSessionWithGZip Home");
      StatelessSession bean = home.create();
      log.debug("Created StatelessSessionWithGZip");
      // Test that the Entity bean sees username as its principal
      String echo = bean.echo("jrmp-comp");
      log.debug("bean.echo(jrmp-comp) = " + echo);
      bean.remove();
   }

   /**
    * Test basic ejb access over the compressed socket
    *
    * @exception Exception  Description of Exception
    */
   public void testAccess() throws Exception
   {
      log.info("+++ testAccess");
      InitialContext jndiContext = new InitialContext();
      log.debug("Lookup StatelessSessionWithGZip");
      Object obj = jndiContext.lookup("StatelessSessionWithGZip");
      StatelessSessionHome home = (StatelessSessionHome)obj;
      log.debug("Found StatelessSessionWithGZip Home");
      StatelessSession bean = home.create();
      log.debug("Created StatelessSessionWithGZip");
      // Test that the Entity bean sees username as its principal
      String echo = bean.echo("jrmp");
      log.debug("bean.echo(jrmp) = " + echo);
      bean.remove();
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(CustomSocketsUnitTestCase.class, "jrmp-comp.jar");
   }

}
