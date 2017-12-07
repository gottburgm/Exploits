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
package org.jboss.ejb.txtimer;

// $Id: TimedObjectId.java 81030 2008-11-14 12:59:42Z dimitris@jboss.org $

import org.jboss.mx.util.ObjectNameFactory;

import javax.management.ObjectName;
import java.io.Serializable;

/**
 * The combined TimedObjectId consists of a String that identifies
 * the "class" of the TimedObject and optionally an instance primary key object.
 * 
 * When the TimedObject is an EJB deployed on JBoss, the containerId is the JMX
 * name of the component, and the instancePk is the entity's primary key.
 * If the component is not an entity, the instancePk should be null.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 81030 $
 * @since 09-Apr-2004
 */
public class TimedObjectId implements Serializable
{
   private ObjectName containerId;
   private Object instancePk;
   private int hashCode;

   /**
    * Construct a combined TimedObjectId
    *
    * @param containerId The TimedObject identifier
    * @param instancePk  The TimedObject instance identifier, can be null
    */
   public TimedObjectId(ObjectName containerId, Object instancePk)
   {
      if (containerId == null)
         throw new IllegalArgumentException("containerId cannot be null");

      this.containerId = containerId;
      this.instancePk = instancePk;
   }

   /**
    * Construct a TimedObjectId
    *
    * @param timedObjectId The TimedObject identifier
    */
   public TimedObjectId(ObjectName timedObjectId)
   {
      this(timedObjectId, null);
   }

   public ObjectName getContainerId()
   {
      return containerId;
   }

   public Object getInstancePk()
   {
      return instancePk;
   }

   /**
    * Parse the timed object id from external form.
    * "[id=contatinerId,pk=instancePk]"
    */
   public static TimedObjectId parse(String externalForm)
   {
      if (externalForm.startsWith("[") == false || externalForm.endsWith("]") == false)
         throw new IllegalArgumentException("Square brackets expected arround: " + externalForm);

      // take first and last char off
      String inStr = externalForm.substring(1, externalForm.length() - 1);

      if (inStr.startsWith("target=") == false)
         throw new IllegalArgumentException("Cannot parse: " + externalForm);
      String jmxStr = inStr.substring(7);

      String pkStr = null;
      int pkIndex = jmxStr.indexOf(",pk=");
      if (pkIndex > 0)
      {
         pkStr = jmxStr.substring(pkIndex + 4);
         jmxStr = jmxStr.substring(0, pkIndex);
      }

      ObjectName contatinerId = ObjectNameFactory.create(jmxStr);
      return new TimedObjectId(contatinerId, pkStr);
   }

   /**
    * Returns the external representation of the TimedObjectId.
    * "[id=contatinerId,pk=instancePk]"
    */
   public String toExternalForm()
   {
      String pkStr = (instancePk != null ? ",pk=" + instancePk : "");
      return "[target=" + containerId + pkStr + "]";
   }

   public int hashCode()
   {
      if (hashCode == 0)
         hashCode = toString().hashCode();
      return hashCode;
   }

   public boolean equals(Object obj)
   {
      if (obj == this) return true;
      if (obj instanceof TimedObjectId)
      {
         TimedObjectId other = (TimedObjectId)obj;
         if (containerId.equals(other.containerId))
            return (instancePk != null ? instancePk.equals(other.instancePk) : other.instancePk == null);
      }
      return false;
   }

   public String toString()
   {
      return toExternalForm();
   }
}
