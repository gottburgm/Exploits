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
package org.jboss.test.txtimer.test;

import java.util.HashMap;

import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.TimedObject;
import javax.ejb.Timer;

import org.jboss.ejb.txtimer.TimedObjectId;
import org.jboss.ejb.txtimer.TimedObjectInvoker;
import org.jboss.mx.util.ObjectNameFactory;

/**
 * Invokes the ejbTimeout method on the TimedObject with the given id.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class SimpleTimedObjectInvoker implements TimedObjectInvoker
{
   private TimedObjectId timedObjectId;
   private HashMap objectMap = new HashMap();

   /**
    * Invokes the ejbTimeout method on the TimedObject with the given id.
    *
    * @param timer the Timer that is passed to ejbTimeout
    */
   public void callTimeout(Timer timer)
   {
      TimedObject timedObject = (TimedObject)objectMap.get(timedObjectId);
      if (timedObject == null)
         throw new NoSuchObjectLocalException("Cannot find TimedObject: " + timedObjectId);
      timedObject.ejbTimeout(timer);
   }

   public TimedObjectId addTimedObject(TimedObject timedObject)
   {
      timedObjectId = new TimedObjectId(ObjectNameFactory.create("txtimer:timedObject=" + timedObject.hashCode()));
      objectMap.put(timedObjectId, timedObject);
      return timedObjectId;
   }
}
