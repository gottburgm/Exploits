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
package org.jboss.jmx.connector.invoker;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.system.ServiceMBeanSupport;

/**
 * MBeanProxyRemote.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81030 $
 */
public class MBeanProxyRemote extends ServiceMBeanSupport implements MBeanProxyRemoteMBean
{
   /** The mbeanServer connection */
   private ObjectName mbeanServerConnection;

   /**
    * Get the mbeanServerConnection.
    * 
    * @return the mbeanServerConnection.
    */
   public ObjectName getMBeanServerConnection()
   {
      return mbeanServerConnection;
   }

   /**
    * Set the mbeanServerConnection.
    * 
    * @param mbeanServerConnection the mbeanServerConnection.
    */
   public void setMBeanServerConnection(ObjectName mbeanServerConnection)
   {
      this.mbeanServerConnection = mbeanServerConnection;
   }
   
   protected void startService() throws Exception
   {
      if (MBeanProxyExt.remote != null)
         throw new IllegalStateException("Remote MBeanServerConnection is already set " + MBeanProxyExt.remote);
      
      Object o = server.getAttribute(mbeanServerConnection, "Proxy");
      if (o instanceof MBeanServerConnection == false)
         throw new DeploymentException(mbeanServerConnection + " does not define an MBeanServerConnection");
      MBeanProxyExt.remote = (MBeanServerConnection) o;
   }
   
   protected void stopService() throws Exception
   {
      MBeanProxyExt.remote = null;
   }
}
