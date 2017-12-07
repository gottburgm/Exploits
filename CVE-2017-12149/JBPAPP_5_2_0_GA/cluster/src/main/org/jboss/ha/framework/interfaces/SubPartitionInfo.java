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
package org.jboss.ha.framework.interfaces;

import java.util.ArrayList;
import java.util.HashSet;

/**
 *  Holder class that knows about a particular HA(sub)Partition i.e. member nodes,
 *  partition name and some utility functions.
 *
 *  @see org.jboss.ha.hasessionstate.interfaces.HASessionState
 *  @see org.jboss.ha.hasessionstate.server.HASessionStateImpl
 *
 *  @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 *  @version $Revision: 81001 $
 */
public class SubPartitionInfo 
   implements Comparable, Cloneable, java.io.Serializable
{
   // Constants -----------------------------------------------------
   /** The serialVersionUID
    * @since 1.2
    */ 
   private static final long serialVersionUID = -4116262958129610472L;

   // Attributes ----------------------------------------------------
   
   /**
    * Name of the current sub-partition (will be used to create a JGroups group)
    */
   public String subPartitionName = null;
   
   /**
    * When sub-partitions are merged, some names will disappear (eg. Merge G1 and G2 in G1)
    * this structure remembers the removed named so that HAPartition can know which new group
    * they should join
    */
   public HashSet subPartitionMergedNames = new HashSet ();
   
   /**
    * List of nodes part of this sub-partition
    */
   public ArrayList memberNodeNames = new ArrayList ();

   private transient boolean newGroup = false;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public SubPartitionInfo () {}
   
   public SubPartitionInfo (String partitionName, String[] members)
   {
      super ();
      this.subPartitionName = partitionName;
      if (members != null)
         for (int i=0; i<members.length; i++)
            this.memberNodeNames.add (members[i]);
   }
   
   // Public --------------------------------------------------------
   
   public void setIsNewGroup ()
   {
      this.newGroup = true;
   }
   
   public void merge (SubPartitionInfo merged)
   {
      this.memberNodeNames.addAll (merged.memberNodeNames);
      if (this.newGroup && !merged.newGroup)
         this.subPartitionName = merged.subPartitionName;
      else if (!merged.newGroup)
         this.subPartitionMergedNames.add (merged.subPartitionName);
      
      
      if (!merged.newGroup)
         this.subPartitionMergedNames.add (merged.subPartitionName);
      this.subPartitionMergedNames.addAll (merged.subPartitionMergedNames); // ? needed ?
      merged.memberNodeNames.clear ();
      merged.subPartitionMergedNames.clear ();
   }
   
   public String toString ()
   {
      return subPartitionName + ":[" + memberNodeNames + "] aka '" + subPartitionMergedNames + "'";
   }
   
   public boolean actsForSubPartition (String subPartitionName)
   {
      return (subPartitionName.equals (subPartitionName) || subPartitionMergedNames.contains (subPartitionName));      
   }
   
   public boolean containsNode (String node)
   {
      return memberNodeNames.contains (node);
   }

   // Comparable implementation ----------------------------------------------
   
   /**
    * "Note: this class has a natural ordering that is
    * inconsistent with equals."
    */
   public int compareTo (Object o)
   {
      int mySize = memberNodeNames.size ();
      int itsSize = ((SubPartitionInfo)o).memberNodeNames.size ();
      
      if (mySize==itsSize)
         return 0;
      else if (mySize > itsSize)
         return 1;
      else
         return -1;
      
   }
   
   // Cloneable implementation ----------------------------------------------
   
   public Object clone ()
   {
      SubPartitionInfo clonedObject = new SubPartitionInfo ();
      clonedObject.subPartitionName = this.subPartitionName;
      clonedObject.memberNodeNames = (ArrayList)this.memberNodeNames.clone ();
      clonedObject.subPartitionMergedNames = (HashSet)this.subPartitionMergedNames.clone ();
      
      return clonedObject;
   }
   
}
