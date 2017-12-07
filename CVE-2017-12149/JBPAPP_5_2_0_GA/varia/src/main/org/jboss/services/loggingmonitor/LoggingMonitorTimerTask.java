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
package org.jboss.services.loggingmonitor;

import java.util.TimerTask;

import org.jboss.logging.Logger;


/**
 * This class provides a repeatable task for monitoring to be
 * executed by a timer.
 * 
 * @author <a href="mailto:jimmy.wilson@acxiom.com">James Wilson</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
class LoggingMonitorTimerTask extends TimerTask
{
   private MonitoredMBean[] monitoredObjects;
   private Logger logger;

   /**
    * Constructor.
    *
    * @param monitoredObjects the objects to be monitored.
    * @param logger the logger to use for logging errors
    */
   public LoggingMonitorTimerTask(MonitoredMBean[] monitoredObjects, Logger logger)
   {
      this.monitoredObjects = monitoredObjects;
      this.logger = logger;
      
      for (int i = 0; i < monitoredObjects.length; ++i)
      {
         // log the header message
         monitoredObjects[i].logFormat();
      }
   }

   /**
    * TimerTask implementation.
    */
   public void run()
   {
      for (int i = 0; i < monitoredObjects.length; ++i)
      {
         try
         {
            monitoredObjects[i].logAttributes();
         }
         catch (Exception e)
         {
            logger.warn("Unable to log attributes for mbean: " + monitoredObjects[i].getObjectName(), e);
         }
      }
   }
}
