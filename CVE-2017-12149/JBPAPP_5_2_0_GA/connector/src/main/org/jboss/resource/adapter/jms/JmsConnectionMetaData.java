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

import java.util.Enumeration;
import java.util.Vector;

import javax.jms.ConnectionMetaData;

/**
 * This class implements javax.jms.ConnectionMetaData
 * 
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Norbert.Lataille@m4x.org)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class JmsConnectionMetaData implements ConnectionMetaData
{
   public String getJMSVersion()
   {
      return "1.1";
   }

   public int getJMSMajorVersion()
   {
      return 1;
   }

   public int getJMSMinorVersion()
   {
      return 1;
   }

   public String getJMSProviderName()
   {
      return "JBoss";
   }

   public String getProviderVersion()
   {
      return "5.0";
   }

   public int getProviderMajorVersion()
   {
      return 5;
   }

   public int getProviderMinorVersion()
   {
      return 0;
   }

   public Enumeration getJMSXPropertyNames()
   {
      Vector vector = new Vector();
      vector.add("JMSXGroupID");
      vector.add("JMSXGroupSeq");
      vector.add("JMSXDeliveryCount");
      return vector.elements();
   }
}
