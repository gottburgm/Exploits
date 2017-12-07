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

/**
 * 
 * Sample Singleton MBean interface
 * 
 * @author  Ivelin Ivanov <ivelin@apache.org>
 *
 */
public interface HASingletonMBeanExampleMBean
{

  /**
   * 
   * @return true if the node that this MBean is registered with
   * is the master node for the singleton service
   * 
   */
  public boolean isMasterNode();
  
  /**
   * 
   * Invoked when this mbean is elected to run the singleton service,
   * or in other words when this node is elected for master.
   *
   */
  public void startSingleton();
  
  /**
   * 
   * Invoked when this mbean is elected to no longer run the singleton service,
   * or in other words when this node is elected for slave.
   * 
   * @param String gracefulShutdown is an example argument passed on singleton stop
   *
   */
  public void stopSingleton( String gracefulShutdown );

  
}
