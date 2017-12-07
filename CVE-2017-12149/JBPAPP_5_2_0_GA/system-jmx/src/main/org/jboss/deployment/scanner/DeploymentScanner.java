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
package org.jboss.deployment.scanner;

import javax.management.ObjectName;

import org.jboss.system.Service;

/**
 * Provides the basic interface for a deployment scanner.
 *
 * <p>A deployment scanner scans for new, removed or changed
 *    deployments.
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 *
 * @version <tt>$Revision: 81033 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public interface DeploymentScanner
   extends Service
{
   /**
    * The ObjectName of the {@link Deployer} which we will use.
    *
    * @param deployerName    The object name of the deployer to use.
    *
    * @jmx:managed-attribute
    */
   void setDeployer(ObjectName deployerName);

   /**
    * Get the ObjectName of the {@link Deployer} which we are using.
    *
    * @return    The object name of the deployer we are using.
    *
    * @jmx:managed-attribute
    */
   ObjectName getDeployer();

   /**
    * Set the scan period for the scanner.
    *
    * @param period    This is the time in milliseconds between scans.
    *
    * @throws IllegalArgumentException    Period value out of range.
    *
    * @jmx:managed-attribute
    */
   void setScanPeriod(long period);

   /**
    * Get the scan period for the scanner.
    *
    * @return    This is the time in milliseconds between scans.
    */
   long getScanPeriod();

   /**
    * Disable or enable the period based deployment scanning.
    *
    * <p>Manual scanning can still be performed by calling
    *    {@link #scan}.
    *
    * @param flag    True to enable or false to disable period
    *                based scanning.
    *
    * @jmx:managed-attribute
    */
   void setScanEnabled(boolean flag);

   /**
    * Check if period based scanning is enabled.
    *
    * @return    True if enabled, false if disabled.
    *
    * @jmx:managed-attribute
    */
   boolean isScanEnabled();

   /**
    * Scan for deployment changes.
    *
    * @throws IllegalStateException    Not initialized.
    * @throws Exception                Scan failed.
    *
    * @jmx:managed-operation
    */
   void scan() throws Exception;
}
