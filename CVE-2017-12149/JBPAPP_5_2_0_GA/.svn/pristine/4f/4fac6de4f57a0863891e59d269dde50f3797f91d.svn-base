/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.container.jboss50.endpoint;

import javax.jms.Destination;

import org.jboss.logging.Logger;
import org.hornetq.jms.client.HornetQDestination;
import org.jboss.wsf.framework.management.DefaultJMSEndpointResolver;

/**
 * A JMS endpoint resolver meant for working with HornetQ destination implementation
 * 
 * @author alessio.soldano@jboss.com
 *
 */
public class HornetQJMSResolver extends DefaultJMSEndpointResolver
{
   public void setDestination(Destination destination)
   {
      if (destination instanceof HornetQDestination)
         setFromName(destination, ((HornetQDestination)destination).isQueue());
      else
      {
         Logger.getLogger(HornetQJMSResolver.class).warn(
               "Destination '" + destination + "' is not a org.hornetq.jms.client.HornetQDestination instance, falling back to default JMS endpoint resolution");
         super.setDestination(destination);
      }
   }

}
