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

import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;

import org.w3c.dom.Element;

/**
 * Imutable class which holds all the information about read-ahead settings.
 * It loads its data from standardjbosscmp-jdbc.xml and jbosscmp-jdbc.xml
 *
 * @author <a href="mailto:on@ibis.odessa.ua">Oleg Nitz</a>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version $Revision: 81030 $
 */
public class JDBCReadAheadMetaData
{
   public static final JDBCReadAheadMetaData DEFAULT = new JDBCReadAheadMetaData();

   /** Don't read ahead. */
   private static final byte NONE = 0;

   /** Read ahead when some entity is being loaded (lazily, good for all queries). */
   private static final byte ON_LOAD = 1;

   /** Read ahead during "find" (not lazily, the best for queries with small result set). */
   private static final byte ON_FIND = 2;

   private static final List STRATEGIES = Arrays.asList(new String[]{"none", "on-load", "on-find"});

   /** The strategy of reading ahead, one of {@link #NONE}, {@link #ON_LOAD}, {@link #ON_FIND}. */
   private final byte strategy;

   /** The page size of the read ahead buffer */
   private final int pageSize;

   /** The name of the load group to eager load. */
   private final String eagerLoadGroup;

   /** a list of left-join */
   private final List leftJoinList;

   /**
    * Constructs default read ahead meta data: no read ahead.
    */
   private JDBCReadAheadMetaData()
   {
      strategy = ON_LOAD;
      pageSize = 255;
      eagerLoadGroup = "*";
      leftJoinList = Collections.EMPTY_LIST;
   }

   /**
    * Constructs read ahead meta data with specified strategy, pageSize and
    * eagerLoadGroup.
    * NOTE: used only in tests.
    */
   public JDBCReadAheadMetaData(String strategy, int pageSize, String eagerLoadGroup)
   {
      this(strategy, pageSize, eagerLoadGroup, Collections.EMPTY_LIST);
   }

   public JDBCReadAheadMetaData(String strategy, int pageSize, String eagerLoadGroup, List leftJoins)
   {
      this.strategy = (byte)STRATEGIES.indexOf(strategy);
      if(this.strategy < 0)
      {
         throw new IllegalArgumentException("Unknown read ahead strategy '" + strategy + "'.");
      }
      this.pageSize = pageSize;
      this.eagerLoadGroup = eagerLoadGroup;
      leftJoinList = leftJoins;
   }

   /**
    * Constructs read ahead meta data with the data contained in the read-ahead
    * xml element from a jbosscmp-jdbc xml file. Optional values of the xml
    * element that are not present are instead loaded from the defalutValues
    * parameter.
    *
    * @param element the xml Element which contains the read-ahead metadata
    * @throws DeploymentException if the xml element is invalid
    */
   public JDBCReadAheadMetaData(Element element, JDBCReadAheadMetaData defaultValue)
      throws DeploymentException
   {
      // Strategy
      String strategyStr = MetaData.getUniqueChildContent(element, "strategy");
      strategy = (byte)STRATEGIES.indexOf(strategyStr);
      if(strategy < 0)
      {
         throw new DeploymentException("Unknown read ahead strategy '" + strategyStr + "'.");
      }

      // page-size
      String pageSizeStr = MetaData.getOptionalChildContent(element, "page-size");
      if(pageSizeStr != null)
      {
         try
         {
            pageSize = Integer.parseInt(pageSizeStr);
         }
         catch(NumberFormatException ex)
         {
            throw new DeploymentException("Invalid number format in read-ahead page-size '" + pageSizeStr + "': " + ex);
         }
         if(pageSize < 0)
         {
            throw new DeploymentException("Negative value for read ahead page-size '" + pageSizeStr + "'.");
         }
      }
      else
      {
         pageSize = defaultValue.getPageSize();
      }

      // eager-load-group
      Element eagerLoadGroupElement = MetaData.getOptionalChild(element, "eager-load-group");
      if(eagerLoadGroupElement != null)
      {
         eagerLoadGroup = MetaData.getElementContent(eagerLoadGroupElement);
      }
      else
      {
         eagerLoadGroup = defaultValue.getEagerLoadGroup();
      }

      // left-join
      Iterator iter = MetaData.getChildrenByTagName(element, "left-join");
      leftJoinList = JDBCLeftJoinMetaData.readLeftJoinList(iter);
   }

   /**
    * Is read ahead strategy is none.
    */
   public boolean isNone()
   {
      return (strategy == NONE);
   }

   /**
    * Is the read ahead stratey on-load
    */
   public boolean isOnLoad()
   {
      return (strategy == ON_LOAD);
   }

   /**
    * Is the read ahead stratey on-find
    */
   public boolean isOnFind()
   {
      return (strategy == ON_FIND);
   }

   /**
    * Gets the read ahead page size.
    */
   public int getPageSize()
   {
      return pageSize;
   }

   /**
    * Gets the eager load group.
    */
   public String getEagerLoadGroup()
   {
      return eagerLoadGroup;
   }

   public Iterator getLeftJoins()
   {
      return leftJoinList.iterator();
   }

   /**
    * Returns a string describing this JDBCReadAheadMetaData.
    * @return a string representation of the object
    */
   public String toString()
   {
      return "[JDBCReadAheadMetaData :" +
         " strategy=" + STRATEGIES.get(strategy) +
         ", pageSize=" + pageSize +
         ", eagerLoadGroup=" + eagerLoadGroup +
         ", left-join" + leftJoinList + "]";
   }
}
