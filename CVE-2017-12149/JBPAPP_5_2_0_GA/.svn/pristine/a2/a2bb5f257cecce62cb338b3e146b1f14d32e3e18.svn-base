/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.jboss.system.deployers;

import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.helpers.DefaultDeploymentContextComparator;

/**
 * Orders deployments based on numeric prefixes.
 * 
 * <p>This class is a comparator to sort deployment s based on the existence
 * of a numeric prefix.  The name portion of the URL is evaluated for any 
 * leading digits.  If they exist, then they will define a numerical ordering
 * for this comparator.  If there is no leading digits, then they will
 * compare as less than any name with leading digits.  In the case of a 
 * tie, the order is determined by the behavior of the 
 * {@link LegacyDeploymentContextComparator} superclass.
 * 
 * <p>The behavior of this comparator is to reproduce the behavior of the 
 * PrefixDeploymentSorter in JBoss 4.x.
 * 
 * <p>For example, these names are in ascending order:
 *   <ul>
 *     <li>test.sar</li>
 *     <li>component.ear</li>
 *     <li>001test.jar</li>
 *     <li>5test.rar</li>
 *     <li>5foo.jar</li>
 *     <li>120bar.jar</li>
 *   </ul>
 *
 * @author <a href="mailto:miclark@redhat.com">Mike M. Clark</a> 
 * 
 * @version $Revision: $
 */
public class LegacyPrefixDeploymentContextComparator extends LegacyDeploymentContextComparator
{
   /** The instance */
   public static final LegacyPrefixDeploymentContextComparator INSTANCE = new LegacyPrefixDeploymentContextComparator();
  
   
   /**
    * Get the instance.
    *
    * @return the instance
    */
   public static LegacyPrefixDeploymentContextComparator getInstance()
   {
      return INSTANCE;
   }
   
   @Override
   public int compare(DeploymentContext first, DeploymentContext second)
   {
      int firstPrefixValue = getPrefixValue(first);
      int secondPrefixValue = getPrefixValue(second);
      int diff = firstPrefixValue - secondPrefixValue;
      if (diff != 0)
      {
         return diff;
      }
      else
      {
         return super.compare(first, second);
      }
   }
   
   private int getPrefixValue(DeploymentContext deploymentContext)
   {
      String name = deploymentContext.getSimpleName();
      
      // calculate where the digit-prefix ends
      int prefixEnd = 0;
      int nameEnd = name.length() - 1;
      
      while (prefixEnd <= nameEnd && Character.isDigit(name.charAt(prefixEnd)))
      {
         ++prefixEnd;
      }
      
      // If zero length prefix, return -1
      if (prefixEnd == 0)
      {
         return -1;
      }
      
      // Strip leading zeros
      int nameStart = 0;
      while (nameStart < prefixEnd && name.charAt(nameStart) == '0')
      {
         ++nameStart;
      }
      
      return (nameStart == prefixEnd) ? 0 : Integer.parseInt(name.substring(nameStart, prefixEnd));  
   }
}
