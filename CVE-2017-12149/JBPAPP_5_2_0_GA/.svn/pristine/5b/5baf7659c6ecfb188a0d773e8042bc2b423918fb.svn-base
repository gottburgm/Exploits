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
package org.jboss.jmx.examples.configuration;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.logging.Logger;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class SampleService extends ServiceMBeanSupport implements SampleServiceMBean
{
   private static final Logger log = Logger.getLogger(ServiceMBeanSupport.class.getName());

   /**
    * Use the short class name as the default for the service name.
    */
   public String getName()
   {
      return "SampleService";
   }

   /**
    * Sub-classes should override this method to provide
    * custum 'start' logic.
    * <p/>
    * <p>This method is empty, and is provided for convenience
    * when concrete service classes do not need to perform
    * anything specific for this state change.
    */
   protected void startService() throws Exception
   {
      super.startService();
      log.debug("SampleService:startService() called");
      createSampleConfigMBean();
   }
   
   protected void stopService() throws Exception
   {
      ObjectName objname = new ObjectName("sample:name=SampleConfig");
      getServer().unregisterMBean(objname);
   }

   public void createSampleConfigMBean()
         throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, javax.management.NotCompliantMBeanException
   {
      log.debug("SampleService:createSampleConfigMBean() called");
      SampleConfig config = new SampleConfig();
      ObjectName objname = new ObjectName("sample:name=SampleConfig");
      getServer().registerMBean(config, objname);
   }
}