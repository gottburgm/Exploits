/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jca.support;

import java.util.List;

import org.jboss.resource.adapter.jdbc.URLSelectorStrategy;
import org.jboss.resource.adapter.jdbc.xa.XAManagedConnectionFactory.XAData;

public class MyXADataSelector implements URLSelectorStrategy
{
   private List<XAData> xaDataList;
   private int xaDataIndex;
   private XAData xaData;
   
   public MyXADataSelector(List<XAData> xaDataList)
   {
      if(xaDataList == null || xaDataList.size() == 0)
      {
         throw new IllegalStateException("Expected non-empty list of XADataSource/URL pairs but got: " + xaDataList);
      }

      this.xaDataList = xaDataList;
   }

   public synchronized XAData getXAData()
   {
      if(xaData == null)
      {
         if(xaDataIndex == xaDataList.size())
         {
            xaDataIndex = 0;
         }
         xaData = (XAData)xaDataList.get(xaDataIndex++);
      }
      return xaData;
   }

   public synchronized void failedXAData(XAData xads)
   {
      if(xads.equals(this.xaData))
      {
         this.xaData = null;
      }
   }

   /* URLSelectorStrategy Implementation goes here*/   
   public List<XAData> getCustomSortedUrls()
   {
      return xaDataList;
   }
   public void failedUrlObject(Object urlObject)
   {
      failedXAData((XAData)urlObject);
   }
   public List<XAData> getAllUrlObjects()
   {
      return xaDataList;
   }
   public Object getUrlObject()
   {
      return getXAData();
   }   
}
