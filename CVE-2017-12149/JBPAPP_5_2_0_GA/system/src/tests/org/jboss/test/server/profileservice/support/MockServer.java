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
package org.jboss.test.server.profileservice.support;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.jboss.bootstrap.spi.Server;
import org.jboss.bootstrap.spi.ServerConfig;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85526 $
 */
public class MockServer implements Server
{

   public ServerConfig config;
   
   public MockServer(ServerConfig config)
   {
      this.config = config;
   }
   
   public ServerConfig getConfig() throws IllegalStateException
   {
      return this.config;
   }
   
   public String getBuildDate()
   {
      // FIXME getBuildDate
      return null;
   }

   public String getBuildID()
   {
      // FIXME getBuildID
      return null;
   }

   public String getBuildJVM()
   {
      // FIXME getBuildJVM
      return null;
   }

   public String getBuildNumber()
   {
      // FIXME getBuildNumber
      return null;
   }

   public String getBuildOS()
   {
      // FIXME getBuildOS
      return null;
   }

   public Date getStartDate()
   {
      // FIXME getStartDate
      return null;
   }

   public String getVersion()
   {
      // FIXME getVersion
      return null;
   }

   public String getVersionName()
   {
      // FIXME getVersionName
      return null;
   }

   public String getVersionNumber()
   {
      // FIXME getVersionNumber
      return null;
   }

   public void init(Properties props) throws IllegalStateException, Exception
   {
      init(props, null);
   }
   public void init(Properties props, Map<String, Object> metadata) throws IllegalStateException, Exception
   {
      // FIXME init
      
   }
   public Map<String, Object> getMetaData()
   {
      return Collections.emptyMap();
   }
   public boolean isInShutdown()
   {
      // FIXME isInShutdown
      return false;
   }

   public boolean isStarted()
   {
      // FIXME isStarted
      return false;
   }

   public void shutdown() throws IllegalStateException
   {
      // FIXME shutdown
      
   }

   public void start() throws IllegalStateException, Exception
   {
      // FIXME start
      
   }

}

