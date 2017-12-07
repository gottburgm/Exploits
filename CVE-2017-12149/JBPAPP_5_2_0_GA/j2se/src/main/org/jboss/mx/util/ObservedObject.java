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
package org.jboss.mx.util;

import javax.management.ObjectName;

/**
 * An observed object
 */
public class ObservedObject
{
  // Constants -----------------------------------------------------
   /**
    * Used to reset errors in {@link #alreadyNotified}.
    */
   public static final int RESET_FLAGS_ALREADY_NOTIFIED = 0;

   /**
    * An observed attribute type error has been notified.
    */
   public static final int RUNTIME_ERROR_NOTIFIED = 8;

   /**
    * An observed object error has been notified.
    */
   public static final int OBSERVED_OBJECT_ERROR_NOTIFIED = 1;

   /**
    * An observed attribute error has been notified.
    */
   public static final int OBSERVED_ATTRIBUTE_ERROR_NOTIFIED = 2;

   /**
    * An observed attribute type error has been notified.
    */
   public static final int OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED = 4;

  // Attributes ----------------------------------------------------
  
  /**
   * The object name.
   */
  private ObjectName objectName;
  
  /**
   * The notified attribute.
   */
  private int alreadyNotified = RESET_FLAGS_ALREADY_NOTIFIED;
  
  /**
   * The derived gauge.
   */
  private Object derivedGauge;
  
  /**
   * The last value.
   */
  private Object lastValue;
  
  /**
   * The derived gauge timestamp.
   */
  private long derivedGaugeTimeStamp;
  
  /**
   * The threshold.
   */
  private Object threshold;

  // Static --------------------------------------------------------
  
  // Constructors --------------------------------------------------

  /**
   * Construct a new observed object.
   *
   * @param objectName the object name.
   */
  public ObservedObject(ObjectName objectName)
  {
     if (objectName == null)
        throw new IllegalArgumentException("Null object name");
     this.objectName = objectName;
  }

  // Public --------------------------------------------------------

  public ObjectName getObjectName()
  {
     return objectName;
  }

  public int getAlreadyNotified()
  {
     return alreadyNotified;
  }

  public boolean isAlreadyNotified(int mask)
  {
     return (alreadyNotified & mask) != 0;
  }

  public boolean notAlreadyNotified(int mask)
  {
     if ((alreadyNotified & mask) == 0)
     {
        alreadyNotified |= mask;
        return true;
     }
     return false;
  }

  public void setNotAlreadyNotified(int mask)
  {
     alreadyNotified &= ~mask;
  }

  public void setAlreadyNotified(int mask)
  {
     alreadyNotified |= mask;
  }

  public void resetAlreadyNotified()
  {
     alreadyNotified = RESET_FLAGS_ALREADY_NOTIFIED;
  }

  public Object getDerivedGauge()
  {
     return derivedGauge;
  }

  public void setDerivedGauge(Object gauge)
  {
     derivedGauge = gauge;
  }

  public Object getLastValue()
  {
     return lastValue;
  }

  public void setLastValue(Object last)
  {
     lastValue = last;
  }

  public long getDerivedGaugeTimeStamp()
  {
     return derivedGaugeTimeStamp;
  }

  public void setDerivedGaugeTimeStamp(long ts)
  {
     derivedGaugeTimeStamp = ts;
  }

  public Object getThreshold()
  {
     return threshold;
  }

  public void setThreshold(Object threshold)
  {
     this.threshold = threshold;
  }

   /**
    * @return human readable string.
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(getClass().getName()).append("@").append(System.identityHashCode(this)).append("{");
      buffer.append(" objectName=").append(getObjectName());
      buffer.append(" alreadyNotified=").append(getAlreadyNotified());
      buffer.append(" threshold=").append(getThreshold());
      buffer.append(" derivedGauge=").append(getDerivedGauge());
      buffer.append(" derivedGaugeTS=").append(getDerivedGaugeTimeStamp());
      buffer.append(" lastValue=").append(getLastValue());
      return buffer.append("}").toString();
   }

}
