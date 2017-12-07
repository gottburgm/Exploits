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
package org.jboss.test.server.profileservice.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.bootstrap.spi.Server;
import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.system.server.profileservice.repository.AbstractProfileFactory;
import org.jboss.test.BaseTestCase;
import org.jboss.test.server.profileservice.support.FilteredProfileFactory;
import org.jboss.test.server.profileservice.support.MockMainDeployer;
import org.jboss.test.server.profileservice.support.MockServer;
import org.jboss.test.server.profileservice.support.MockServerConfig;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 86174 $
 */
public class AbstractProfileServiceTestBase extends BaseTestCase
{

   public AbstractProfileServiceTestBase(String name)
   {
      super(name);
   }
   
   protected Server createDefaultServer(String name) throws MalformedURLException
   {
      File configDir = new File("src/resources/parsing-tests/config/");
      File commonDir = new File("src/resources/parsing-tests/common/");
      return createServer(name, configDir.toURL(), commonDir.toURL());
   }

   protected Server createServer(String name, URL configDir, URL commonDir)
   {
      ServerConfig config = new MockServerConfig(name, configDir, commonDir);
      return createServer(config);
   }
   
   protected Server createServer(ServerConfig config)
   {
      return new MockServer(config);
   }
   
   protected MainDeployer createMainDeployer()
   {
      return new MockMainDeployer();
   }  

   protected AbstractProfileFactory createProfileFactory()
   {
      return new FilteredProfileFactory();
   }
   
}

