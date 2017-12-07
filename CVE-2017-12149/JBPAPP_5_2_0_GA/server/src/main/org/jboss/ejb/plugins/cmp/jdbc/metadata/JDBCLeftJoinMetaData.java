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
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import org.w3c.dom.Element;
import org.jboss.metadata.MetaData;
import org.jboss.deployment.DeploymentException;

import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Represents
 *    <left-join cmr-field="lineItems">
 *       <left-join cmr-field="product" eager-load-group="product"/>
 *    </left-join>
 *
 * @version <tt>$Revision: 81030 $</tt>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 */
public final class JDBCLeftJoinMetaData
{
   private final String cmrField;
   private final String eagerLoadGroup;
   private final List leftJoinList;

   public static List readLeftJoinList(Iterator leftJoinIterator)
      throws DeploymentException
   {
      List leftJoinList;
      if(leftJoinIterator.hasNext())
      {
         leftJoinList = new ArrayList();
         while(leftJoinIterator.hasNext())
         {
            Element leftJoinElement = (Element)leftJoinIterator.next();
            JDBCLeftJoinMetaData leftJoin = new JDBCLeftJoinMetaData(leftJoinElement);
            leftJoinList.add(leftJoin);
         }
      }
      else
      {
         leftJoinList = Collections.EMPTY_LIST;
      }
      return leftJoinList;
   }

   /**
    * Used only from the testsuite.
    */ 
   public JDBCLeftJoinMetaData(String cmrField, String eagerLoadGroup, List leftJoinList)
   {
      this.cmrField = cmrField;
      this.eagerLoadGroup = eagerLoadGroup;
      this.leftJoinList = leftJoinList;
   }

   public JDBCLeftJoinMetaData(Element element) throws DeploymentException
   {
      cmrField = element.getAttribute("cmr-field");
      if(cmrField == null || cmrField.trim().length() == 0)
      {
         throw new DeploymentException("left-join MUST have non-empty cmr-field attribute.");
      }

      String eagerLoadGroup = element.getAttribute("eager-load-group");
      if(eagerLoadGroup == null || eagerLoadGroup.trim().length() == 0)
      {
         this.eagerLoadGroup = "*";
      }
      else
      {
         this.eagerLoadGroup = eagerLoadGroup;
      }

      Iterator leftJoinIterator = MetaData.getChildrenByTagName(element, "left-join");
      leftJoinList = readLeftJoinList(leftJoinIterator);
   }

   public String getCmrField()
   {
      return cmrField;
   }

   public String getEagerLoadGroup()
   {
      return eagerLoadGroup;
   }

   public Iterator getLeftJoins()
   {
      return leftJoinList.iterator();
   }

   public boolean equals(Object o)
   {
      boolean result;
      if(o == this)
      {
         result = true;
      }
      else if(o instanceof JDBCLeftJoinMetaData)
      {
         JDBCLeftJoinMetaData other = (JDBCLeftJoinMetaData)o;
         result =
            (cmrField == null ? other.cmrField == null : cmrField.equals(other.cmrField)) &&
            (eagerLoadGroup == null ? other.eagerLoadGroup == null : eagerLoadGroup.equals(other.eagerLoadGroup)) &&
            (leftJoinList == null ? other.leftJoinList == null : leftJoinList.equals(other.leftJoinList));
      }
      else
      {
         result = false;
      }
      return result;
   }

   public int hashCode()
   {
      int result = Integer.MIN_VALUE;
      result += (cmrField == null ? 0 : cmrField.hashCode());
      result += (eagerLoadGroup == null ? 0 : eagerLoadGroup.hashCode());
      result += (leftJoinList == null ? 0 : leftJoinList.hashCode());
      return result;
   }

   public String toString()
   {
      return "[cmr-field=" + cmrField + ", eager-load-group=" + eagerLoadGroup + ", left-join=" + leftJoinList + ']';
   }
}
