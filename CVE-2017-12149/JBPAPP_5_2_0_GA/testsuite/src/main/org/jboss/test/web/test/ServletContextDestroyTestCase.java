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
package org.jboss.test.web.test;

import java.io.File;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;

/**
 *  JBAS-850: Tomcat doesn't call contextDestroyed() on JBoss shutdown
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since  Jan 10, 2006
 *  @version $Revision: 81036 $
 */
public class ServletContextDestroyTestCase extends JBossTestCase
{ 
   File tmpLocation = null;
   
   public void setup()
   {
      try
      {
         tmpLocation = getTmpLocation();
         File file = new File(tmpLocation, "ServletContextDestroyed.txt");
         if(file.exists())
            file.delete();
      }catch(Exception e)
      {
         fail(e.getLocalizedMessage());
      } 
   } 
   
   public ServletContextDestroyTestCase(String name)
   {
      super(name); 
   }
   
   public void testContextDestroyEvent() throws Exception
   {
      deploy("jbosstest-ctx-destroy.war");
      undeploy("jbosstest-ctx-destroy.war");
      File file = new File(getTmpLocation(), "ServletContextDestroyed.txt");
      assertTrue("File ServletContextDestroyed.txt exists?", file.exists());
      if(file.exists())
         file.delete();
   } 
   
   public void testShutdownContextDestroy() throws Exception
   { 
      tmpLocation = getTmpLocation();
      deploy("jbosstest-ctx-destroy.war");
      //Shutdown JBoss
      shutDownJBoss();
      this.sleep(20000);//20 secs
      File file = new File(tmpLocation, "ServletContextDestroyed.txt"); 
      assertTrue("File ServletContextDestroyed.txt exists?", file.exists());  
      if(file.exists())
         file.delete();
   }
   
   private File getTmpLocation() throws Exception
   { 
      MBeanServerConnection server = this.getServer();
      ObjectName oname = new ObjectName("jboss.system:type=ServerConfig");
      return (File)server.getAttribute(oname,"ServerTempDir"); 
   }
   
   private void shutDownJBoss() throws Exception
   {
      MBeanServerConnection server = this.getServer();
      ObjectName oname = new ObjectName("jboss.system:type=Server");
      server.invoke(oname,"shutdown", new Object[]{}, new String[]{}); 
   }
}
