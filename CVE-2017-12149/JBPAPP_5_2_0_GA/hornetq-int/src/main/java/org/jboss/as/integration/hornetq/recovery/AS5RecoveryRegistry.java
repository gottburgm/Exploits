/*
* JBoss, Home of Professional Open Source.
* Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.as.integration.hornetq.recovery;

import org.hornetq.jms.server.recovery.HornetQRegistryBase;
import org.jboss.logging.Logger;
import org.jboss.tm.XAResourceRecoveryRegistry;


/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 * @author Clebert Suconic
 *         9/19/11
 */
public class AS5RecoveryRegistry extends HornetQRegistryBase
{
   private static final Logger log = Logger
         .getLogger(AS5RecoveryRegistry.class);
   static volatile XAResourceRecoveryRegistry registry;


   public AS5RecoveryRegistry()
   {
      super();
   }

   public AS5RecoveryRegistry(XAResourceRecoveryRegistry registry)
   {
      AS5RecoveryRegistry.registry = registry;
   }
   
   public XAResourceRecoveryRegistry getTMRegistry()
   {
      return registry;
   }
   

}
