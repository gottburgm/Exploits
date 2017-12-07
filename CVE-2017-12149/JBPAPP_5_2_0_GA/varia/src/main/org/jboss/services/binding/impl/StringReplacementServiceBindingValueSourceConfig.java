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

package org.jboss.services.binding.impl;


/**
 * A StringReplacementServiceBindingValueSourceConfig.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class StringReplacementServiceBindingValueSourceConfig
{
   public static final String DEFAULT_HOST_MARKER = "${host}";
   public static final String DEFAULT_PORT_MARKER = "${port}";
   
   private String portMarker;
   private String hostMarker;
   
   public StringReplacementServiceBindingValueSourceConfig()
   {
      this(DEFAULT_HOST_MARKER, DEFAULT_PORT_MARKER);
   }
   
   public StringReplacementServiceBindingValueSourceConfig(String hostMarker, String portMarker)
   {
      this.hostMarker = (hostMarker == null ? DEFAULT_HOST_MARKER : hostMarker);
      this.portMarker = (portMarker == null ? DEFAULT_PORT_MARKER : portMarker);
   }

   public String getPortMarker()
   {
      return portMarker;
   }

   public String getHostMarker()
   {
      return hostMarker;
   }
   
}
