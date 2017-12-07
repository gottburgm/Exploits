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
package org.jboss.console.navtree;

import java.net.URL;
import java.applet.AppletStub;
import java.applet.AppletContext;
import java.util.Properties;

/** A simple AppletStub to allow for testing the applet as a java application
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81010 $
 */
public class MainAppletStub implements AppletStub
{
   private URL docBase;
   private URL codeBase;
   private Properties params = new Properties(System.getProperties());

   public MainAppletStub() throws Exception
   {
      docBase = new URL("http://localhost:8080/web-console/");
      codeBase = new URL("http://localhost:8080/web-console/");
      params.setProperty("RefreshTime", "5");
      params.setProperty("PMJMXName", "jboss.admin:service=PluginManager");
   }

   public boolean isActive()
   {
      return true;
   }

   public String getParameter(String name)
   {
      return System.getProperty(name);
   }

   public AppletContext getAppletContext()
   {
      return null;
   }

   public void appletResize(int width, int height)
   {
   }

   public URL getDocumentBase()
   {
      return docBase;
   }
   public URL getCodeBase()
   {
      return codeBase;
   }
}
