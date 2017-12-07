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

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class SampleConfig implements SampleConfigMBean
{
   private static final Logger log = Logger.getLogger(SampleConfig.class.getName());

   private String testAttribute = "Initial Value";

   public String getTestAttribute()
   {
      return testAttribute;
   }

   public void setTestAttribute(String testValue)
   {
      log.debug("Settting testAttribute for SampleConfig.");
      log.debug("Old value = " + testAttribute);
      log.debug("New value = " + testValue);
      this.testAttribute = testValue;
   }
}