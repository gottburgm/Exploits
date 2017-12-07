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

import javax.management.j2ee.statistics.BoundaryStatistic;

/**
 * This class is the JBoss specific Boundary Statistics class allowing
 * just to increase and resetStats the instance.
 *
 * @author <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>
 * @version $Revision: 81025 $
 */
public class BoundaryStatisticImpl
        extends StatisticImpl
        implements BoundaryStatistic
{
   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------
      
   /** @since 4.0.2 */
   private static final long serialVersionUID = 4718840772715705031L;
   
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------

   protected long mLowerBound;
   protected long mUpperBound;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * Default (no-args) constructor
    */
   public BoundaryStatisticImpl(String pName, String pUnit, String pDescription,
                                long lowerBound, long upperBound)
   {
      super(pName, pUnit, pDescription);
      mLowerBound = lowerBound;
      mUpperBound = upperBound;
   }

   // -------------------------------------------------------------------------
   // BoundaryStatistic Implementation
   // -------------------------------------------------------------------------

   /**
    * @return The value of LowerBound
    */
   public long getLowerBound()
   {
      return mLowerBound;
   }

   /**
    * @return The value of UpperBound
    */
   public long getUpperBound()
   {
      return mUpperBound;
   }

   /**
    * @return Debug Information about this Instance
    */
   public String toString()
   {
      return "BoundryStatistics[ " + getLowerBound() + ", " +
              getUpperBound() + ", " + super.toString() + " ]";
   }

}
