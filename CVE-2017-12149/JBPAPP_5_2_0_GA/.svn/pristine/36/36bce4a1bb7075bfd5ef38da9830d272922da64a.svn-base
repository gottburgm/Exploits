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

package org.jboss.web.tomcat.service.session;

import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;

/**
 * @author Brian Stansberry
 */
public class OutgoingDistributableSessionDataImpl implements OutgoingDistributableSessionData
{
   private final String realId;
   private final int version;
   private final Long timestamp;
   private final DistributableSessionMetadata metadata;
   
   public OutgoingDistributableSessionDataImpl(String realId, int version, 
                                               Long timestamp, 
                                               DistributableSessionMetadata metadata)
   {
      assert realId != null : "realId is null";
      
      this.realId = realId;
      this.version = version;
      this.timestamp = timestamp;
      this.metadata = metadata;
   }
   
   public DistributableSessionMetadata getMetadata()
   {
      return metadata;
   }

   public String getRealId()
   {
      return realId;
   }

   public Long getTimestamp()
   {
      return timestamp;
   }

   public int getVersion()
   {
      return version;
   }

}
