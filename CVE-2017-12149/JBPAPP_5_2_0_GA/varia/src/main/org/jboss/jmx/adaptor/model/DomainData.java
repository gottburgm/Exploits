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
package org.jboss.jmx.adaptor.model;

import java.util.Arrays;
import java.util.TreeSet;
import javax.management.MBeanInfo;
import javax.management.ObjectName;

/** The MBeanData for a given JMX domain name
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81038 $
 */
public class DomainData
{
   String domainName;
   TreeSet domainData = new TreeSet();

   /** Creates a new instance of MBeanInfo */
   public DomainData(String domainName)
   {
      this.domainName = domainName;
   }
   public DomainData(String domainName, MBeanData[] data)
   {
      this.domainName = domainName;
      domainData.addAll(Arrays.asList(data));
   }

   public int hashCode()
   {
      return domainName.hashCode();
   }
   public boolean equals(Object obj)
   {
      DomainData data = (DomainData) obj;
      return domainName.equals(data.domainName);
   }

   public String getDomainName()
   {
      return domainName;
   }
   public MBeanData[] getData()
   {
      MBeanData[] data = new MBeanData[domainData.size()];
      domainData.toArray(data);
      return data;
   }
   public void addData(MBeanData data)
   {
      domainData.add(data);
   }
}
