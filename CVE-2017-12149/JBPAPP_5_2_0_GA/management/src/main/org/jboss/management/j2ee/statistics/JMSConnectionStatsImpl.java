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
package org.jboss.management.j2ee.statistics;

import javax.management.j2ee.statistics.JMSConnectionStats;
import javax.management.j2ee.statistics.JMSSessionStats;

/**
 * Represents the statistics provided by a JMS Connection.
 *
 * @author Andreas Schaefer
 * @version $Revision: 81025 $
 */
public final class JMSConnectionStatsImpl extends StatsBase
        implements JMSConnectionStats
{
   // Constants -----------------------------------------------------
   
   /** @since 4.0.2 */
   private static final long serialVersionUID = -6805324618929625115L;
   
   // Attributes ----------------------------------------------------

   private JMSSessionStats[] mSessions;
   private boolean mTransactional;

   // Constructors --------------------------------------------------

   public JMSConnectionStatsImpl(JMSSessionStats[] pSessions, boolean pIsTransactional)
   {
      if (pSessions == null)
      {
         pSessions = new JMSSessionStats[0];
      }
      mSessions = pSessions;
      mTransactional = pIsTransactional;
   }

   // Public --------------------------------------------------------

   // javax.management.j2ee.JMSConnectionStats implementation -------

   /**
    * @return The list of JMSSessionStats that provide statistics about the sessions
    *         associated with the referencing JMSConnectionStats.
    */
   public JMSSessionStats[] getSessions()
   {
      return mSessions;
   }

   /**
    * @return The transactional state of this JMS connection. If true, indicates that
    *         this JMS connection is transactional.
    */
   public boolean isTransactional()
   {
      return mTransactional;
   }
}
