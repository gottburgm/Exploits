/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.ha.framework.server.managed;

import org.jgroups.conf.ProtocolData;
import org.jgroups.conf.ProtocolStackConfigurator;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class ProtocolDataProtocolStackConfigurator implements ProtocolStackConfigurator
{
   private final ProtocolData[] protocolData;
   
   /**
    * Create a new ProtocolDataProtocolStackConfigurator.
    * 
    */
   public ProtocolDataProtocolStackConfigurator(ProtocolData[] protocolData)
   {
      if (protocolData == null)
      {
         throw new IllegalArgumentException("null protocolData");
      }
      this.protocolData = protocolData;      
   }

   public ProtocolData[] getProtocolStack()
   {
      return this.protocolData;
   }

   public String getProtocolStackString()
   {
      StringBuilder buf=new StringBuilder();
      for (int i = 0; i < protocolData.length; i++)
      {
         buf.append(protocolData[i].getProtocolString(false));
          if(i < protocolData.length - 1) 
          {
              buf.append(':');
          }
      }
      return buf.toString();
   }

}
