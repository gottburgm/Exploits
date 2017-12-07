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

package org.jboss.web.tomcat.service.session.persistent;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;

/**
 * Base implementation of {@link DistributableSessionData}.
 * 
 * @author Brian Stansberry
 */
public class IncomingDistributableSessionDataImpl implements IncomingDistributableSessionData
{
   private final int version;
   private final long timestamp;
   private final DistributableSessionMetadata metadata;
   private final Map<String, Object> attributes;
   
   public IncomingDistributableSessionDataImpl(Integer version, Long timestamp, 
                                              DistributableSessionMetadata metadata,
                                              Map<String, Object> attributes)
   {
      if (version == null)
         throw new IllegalStateException("version is null");
      if (timestamp == null)
         throw new IllegalStateException("timestamp is null");
      if (metadata == null)
         throw new IllegalStateException("metadata is null");
      
      this.version = version.intValue();
      this.timestamp = timestamp.longValue();
      this.metadata = metadata;
      this.attributes = attributes;
   }
   
   public IncomingDistributableSessionDataImpl(AtomicInteger version, AtomicLong timestamp, 
                                              DistributableSessionMetadata metadata,
                                              Map<String, Object> attributes)
   {
      if (version == null)
         throw new IllegalStateException("version is null");
      if (timestamp == null)
         throw new IllegalStateException("timestamp is null");
      if (metadata == null)
         throw new IllegalStateException("metadata is null");
      
      this.version = version.get();
      this.timestamp = timestamp.get();
      this.metadata = metadata;
      this.attributes = attributes;
   }

   public boolean providesSessionAttributes()
   {
      return attributes != null;
   }

   public Map<String, Object> getSessionAttributes()
   {
      if (attributes == null)
      {
         throw new IllegalStateException("Not configured to provide session attributes");
      }
      return attributes;
   }   
   
   public DistributableSessionMetadata getMetadata()
   {
      return metadata;
   }

   public long getTimestamp()
   {
      return timestamp;
   }

   public int getVersion()
   {
      return version;
   }

}
