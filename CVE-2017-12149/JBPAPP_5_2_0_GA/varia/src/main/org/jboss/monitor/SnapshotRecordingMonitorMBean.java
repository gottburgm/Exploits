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
package org.jboss.monitor;

import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Date;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81038 $
 *
 **/
public interface SnapshotRecordingMonitorMBean
{
   String getMonitorName();

   void setMonitorName(String name);

   ObjectName getObservedObject();

   void setObservedObject(ObjectName oname);

   String getObservedAttribute();

   void setObservedAttribute(String attr);

   long getPeriod();

   void setPeriod(long period);

   ArrayList getData();

   void clearData();

   void startSnapshot();

   void endSnapshot();

   long getStartTime();

   long getEndTime();
   boolean isRecording();

}
