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
package org.jboss.ha.singleton.examples;

import org.jboss.logging.Logger;

/**
 * <p>
 * An sample singleton MBean.
 * </p>
 * 
 * @author  Ivelin Ivanov <ivelin@apache.org>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81001 $
 */
public class HASingletonMBeanExample
   implements HASingletonMBeanExampleMBean
{
   private static Logger log = Logger.getLogger(HASingletonMBeanExample.class);
   private boolean isMasterNode = false;

   public void startSingleton()
   {
      isMasterNode = true;
      log.info("Notified to start as singleton");
   }

   public boolean isMasterNode()
   {
      return isMasterNode;
   }

  public void stopSingleton( String gracefulShutdown )
   {
      isMasterNode = false;
      log.info("Notified to stop as singleton with argument: " + gracefulShutdown);
   }

}
