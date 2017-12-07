/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.tm.webmbean;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.memory.MemoryProtocolHandler;
import org.apache.tomcat.util.buf.ByteChunk;
import org.jboss.system.ServiceMBeanSupport;

/** 
 * Server Side Web test.
 *
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class WebTest extends ServiceMBeanSupport implements WebTestMBean
{
   public void test() throws Exception
   {
      MBeanServer server = getServer();
      ObjectName name = new ObjectName("jboss.web:type=Service,serviceName=jboss.web");
      Connector connector = new Connector("org.apache.coyote.memory.MemoryProtocolHandler");
      MemoryProtocolHandler handler = (MemoryProtocolHandler) connector.getProtocolHandler();
      server.invoke(name, "addConnector", new Object[] { connector }, new String[] { Connector.class.getName() });
      try
      {
         ByteChunk input = new ByteChunk(1024);
         ByteChunk output = new ByteChunk(1024);
         org.apache.coyote.Request req = new org.apache.coyote.Request();
         req.decodedURI().setString("/webbmtcleanuptest/test1.jsp");
         req.method().setString("GET");
         org.apache.coyote.Response resp = new org.apache.coyote.Response();
         handler.process(req, input, resp, output);
         if (resp.getStatus() != 200)
            throw new Error(output.toString());

         input = new ByteChunk(1024);
         output = new ByteChunk(1024);
         req = new org.apache.coyote.Request();
         req.decodedURI().setString("/webbmtcleanuptest/test2.jsp");
         req.method().setString("GET");
         resp = new org.apache.coyote.Response();
         handler.process(req, input, resp, output);
         if (resp.getStatus() != 200)
            throw new Error(output.toString());
      }
      finally
      {
         try
         {
            connector.stop();
         }
         finally
         {
            connector.destroy();
         }
      }
   }
}
