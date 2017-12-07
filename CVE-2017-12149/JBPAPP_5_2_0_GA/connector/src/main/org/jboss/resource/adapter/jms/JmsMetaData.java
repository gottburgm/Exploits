/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.adapter.jms;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;

/**
 * Jms Metadata
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class JmsMetaData
   implements ManagedConnectionMetaData
{
   private JmsManagedConnection mc;
   
   public JmsMetaData(final JmsManagedConnection mc) {
      this.mc = mc;
   }
   
   public String getEISProductName() throws ResourceException {
      return "JMS CA Resource Adapter";
   }

   public String getEISProductVersion() throws ResourceException {
      return "0.1";//Is this possible to get another way
   }

   public int getMaxConnections() throws ResourceException {
      // Dont know how to get this, from Jms, we
      // set it to unlimited
      return 0;
   }
    
   public String getUserName() throws ResourceException {
      return mc.getUserName();
   }
}
