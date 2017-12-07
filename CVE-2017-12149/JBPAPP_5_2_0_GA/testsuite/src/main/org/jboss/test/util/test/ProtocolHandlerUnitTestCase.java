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
package org.jboss.test.util.test;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;

import org.jboss.test.JBossTestCase;
import org.jboss.net.protocol.URLStreamHandlerFactory;
import org.jboss.net.protocol.resource.ResourceURLConnection;

/** Unit tests for the custom JBoss protocol handler
 * 
 * @see org.jboss.net.protocol.URLStreamHandlerFactory
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class ProtocolHandlerUnitTestCase extends JBossTestCase
{
   public ProtocolHandlerUnitTestCase(String name)
   {
      super(name);
   }

   public void testJBossHandlers() throws Exception
   {
      getLog().debug("+++ testJBossHandlers");
      // Install a URLStreamHandlerFactory that uses the TCL
      URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory());
      File cwd = new File(".");
      URL cwdURL = cwd.toURL();
      URLConnection conn = cwdURL.openConnection();
      getLog().debug("File URLConnection: "+conn);
      // JBCOMMON-55, jboss custom org.jboss.net.protocol.file.FileURLConnection not used anymore
      // assertTrue("URLConnection is JBoss FileURLConnection", conn instanceof FileURLConnection);
      long lastModified = conn.getLastModified();
      getLog().debug("CWD lastModified: "+lastModified);
      assertTrue("CWD lastModified != 0", lastModified != 0);

      URL resURL = new URL("resource:log4j.xml");
      conn = resURL.openConnection();
      getLog().debug("log4j.xml URLConnection: "+conn);
      assertTrue("URLConnection is JBoss ResourceURLConnection", conn instanceof ResourceURLConnection);
      lastModified = conn.getLastModified();
      getLog().debug("log4j.xml lastModified: "+lastModified);
      assertTrue("log4j.xml lastModified != 0", lastModified != 0);
   }

   /** Override the testServerFound since these test don't need the JBoss server
    */
   public void testServerFound()
   {
   }

}

