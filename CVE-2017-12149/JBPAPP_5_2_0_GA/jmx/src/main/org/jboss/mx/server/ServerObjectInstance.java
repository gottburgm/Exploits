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
package org.jboss.mx.server;

import java.io.ObjectStreamException;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * An Object Instance that differentiates between MBeanServers.
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 81022 $
 */
public class ServerObjectInstance
   extends ObjectInstance
{
   // Attributes --------------------------------------------------

   /**
    * The agent id
    */
   String agentID;


   // Constructors ------------------------------------------------

   /**
    * Create a new Server Object Instance
    * 
    * @param name the object name
    * @param className the class name
    * @param agentID the agentID
    */
   public ServerObjectInstance(ObjectName name, String className, String agentID)
   {
      super(name, className);
      this.agentID = agentID;
   }

   // Public ------------------------------------------------------

   /**
    * Retrieve the agent id of the object instance
    *
    * @return the agent id
    */
   String getAgentID()
   {
      return agentID;
   }

 
   // ObjectInstance overrides ------------------------------------

   public boolean equals(Object object)
   {
     if (object instanceof ServerObjectInstance)
       return (super.equals(object) == true 
               && this.agentID.equals(((ServerObjectInstance)object).agentID));
     else
       return super.equals(object);
   }

 
   // Serialization implementation ----------------------------------

   /**
    * We replace ourself with an ObjectInstance in the stream.
    * This loses the agentId which isn't part of the spec.
    *
    * @return an ObjectInstance version of ourself
    * @exception ObjectStreamException for a serialization error
    */
   private Object writeReplace()
      throws ObjectStreamException
   {
      return new ObjectInstance(getObjectName(), getClassName());
   }

}
