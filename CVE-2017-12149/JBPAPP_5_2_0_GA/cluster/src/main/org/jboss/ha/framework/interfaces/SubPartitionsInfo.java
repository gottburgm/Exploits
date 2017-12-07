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

import java.io.Serializable;

/**
 *  Holder class that knows about a set of HA(sub)Partition currently
 *  building the overall cluster. Exchanged between HASessionState
 *  instances to share the same knowledge.
 *
 *  @see SubPartitionInfo
 *  @see org.jboss.ha.hasessionstate.interfaces.HASessionState
 *  @see org.jboss.ha.hasessionstate.server.HASessionStateImpl
 *
 *  @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 *  @version $Revision: 81001 $
 */
public class SubPartitionsInfo implements Serializable, Cloneable
{
   // Constants -----------------------------------------------------
   /** The serialVersionUID
    * @since 1.2
    */ 
   private static final long serialVersionUID = 3231573521328800529L;

   // Attributes ----------------------------------------------------
   
    public SubPartitionInfo[] partitions = null;    
    protected long groupId = 0;

    // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
    public SubPartitionsInfo () {}

   // Public --------------------------------------------------------
   
    /**
     * return the next distinct id for a new group
     */    
    public long getNextGroupId ()
    {
       return groupId++;
    }
    
    /**
     * Returns the {@link SubPartitionInfo} instance in this group that has the given name.
     */    
    public SubPartitionInfo getSubPartitionWithName (String name)
    {
       if (partitions != null)
       {
         for (int i=0; i<partitions.length; i++)
            if ((partitions[i]).containsNode (name))
               return partitions[i];
       }

       return null;
    }
    
   // Cloneable implementation ----------------------------------------------
   
    public Object clone ()
    {
       SubPartitionsInfo theClone = new SubPartitionsInfo ();
       
       if (partitions != null)
       {
          theClone.partitions = new SubPartitionInfo[partitions.length];
         for (int i=0; i<partitions.length; i++)
            theClone.partitions[i] = (SubPartitionInfo)partitions[i].clone ();
       }
       
       theClone.groupId = groupId;            
       
       return theClone;
       
    }
    
   // Object overrides ---------------------------------------------------
   
    public String toString ()
    {
       String result = null;
       
       if (partitions == null)
          result = "{null}";
       else
       {
          result = "{";
          for (int i=0; i<partitions.length; i++)
             result+= "\n " + partitions[i].toString ();
          result+= "\n}";
       }
       
       return result;
    }

    // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
    
}
