/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.mq.server.jmx;

import java.util.List;

import javax.management.ObjectName;

import org.w3c.dom.Element;

public interface QueueMBean
{

   public void create() throws Exception;

   public void destroy();

   public int getDownCacheSize();

   public int getFullSize();

   public String getJNDIName();

   public int getMessageCount() throws Exception;

   public String getName();

   public int getPageSize();

   public ObjectName getServerPeer();

   public boolean isCreatedProgrammatically();

   public List listMessages(String arg0) throws Exception;

   public void removeAllMessages() throws Exception;

   public void setDownCacheSize(int arg0);

   public void setFullSize(int arg0);

   public void setJNDIName(String arg0) throws Exception;

   public void setPageSize(int arg0);

   public void setSecurityConfig(Element arg0) throws Exception;
   public void setSecurityConf(Element arg0) throws Exception;
   public void setSecurityManager(ObjectName arg0);

   public void setServerPeer(ObjectName arg0);
   public void setDestinationManager(ObjectName arg0) throws Exception;

   /**
    * Returns the expiry destination.
    */
   public ObjectName getExpiryDestination();

   /**
    * Sets the expiry destination.
    */
   public void setExpiryDestination(ObjectName destination);

   public void setMessageCounterHistoryDayLimit(int arg0);

   public void start() throws Exception;

   public void stop();

}