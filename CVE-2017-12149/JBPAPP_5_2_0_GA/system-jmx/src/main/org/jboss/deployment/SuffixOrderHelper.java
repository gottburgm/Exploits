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
package org.jboss.deployment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.jboss.logging.Logger;

/**
 * SuffixOrderHelper.
 * 
 * This class wraps the SuffixOrder and EnhandedSuffixes attributes
 * of MainDeployer.
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $
 */
public final class SuffixOrderHelper
{
   // Constants -----------------------------------------------------
   
   /**
    * Default EnhancedSuffixes
    * 
    * Those values are indicative - we just know they'll work with
    * the compiled order of subdeployers like the aop or ejb3,
    * but any order can be set using the EnhancedSuffixes
    * attribute and/or the individual subdeployer's relative order.
    * 
    * The commented out entries indicate those are dynamically
    * added by their respective subdeployers when they register.
    */
   public static final String[] DEFAULT_ENHANCED_SUFFIXES = {};
   /*
    Moved this list to org.jboss.deployment.MainDeployer-xmbean.xml
    so there are no hardcoded defaults.
   {
       //"050:.deployer",
       //"050:-deployer.xml",
       //"100:.aop",
       //"100:-aop.xml",
       //"150:.sar",
       //"150:-service.xml",
       //"200:.beans",
         "250:.rar",
         "300:-ds.xml",
       //"350:.har",
         "400:.jar",   // ejb .jar
       //"450:.ejb3",
       //"450:.par",
         "500:.war",   // don't comment out this!
         "600:.wsr",
         "650:.ear",
       //"700:.jar",   // plain .jar
       //"750:.zip",
         "800:.bsh",
         "900:.last"   // the JARDeployer really handles those?
   };
   */
   
   /** A default relative order just before 900:.last */
   public static final int DEFAULT_RELATIVE_ORDER = 850;
   
   /** The Logger */
   public static final Logger log = Logger.getLogger(SuffixOrderHelper.class);
   
   // Private Data --------------------------------------------------
   
   /** Wrapped DeploymentSorter that stores the value for SuffixOrder attribute */
   private final DeploymentSorter sorter;
   
   /** The actual value of EnhancedSuffixes attribute */
   private String[] enhancedSuffixes;
   
   /** List of sorted EnhancedSuffix instances */
   private List suffixes;
   
   /** Set of static String suffixes that cannot be overriden/removed */
   private Set staticSuffixes;
   
   // Constructor ---------------------------------------------------
   
   public SuffixOrderHelper(DeploymentSorter sorter)
   {
      this.sorter = sorter;
      this.suffixes = new ArrayList();
      this.staticSuffixes = new HashSet();
   }

   // Accessors -----------------------------------------------------
   
   /**
    * Getter only for the SuffixOrder as known by the MainDeployer and the Scanners
    * 
    * The value is updated during init() with suffixes that remain constant.
    * After that suffixes are added/removed using the corresponding methods.
    * 
    * @return the SuffixOrder string array
    */
   public String[] getSuffixOrder()
   {
      return sorter.getSuffixOrder();
   }
   
   /**
    * Getter for the EnhancedSuffixes attribute
    * 
    * @return the EnhancedSuffixes string array
    */
   public String[] getEnhancedSuffixes()
   {
      return enhancedSuffixes;
   }
   
   /**
    * Setter for the EnhancedSuffixes attribute
    * 
    * @param enhancedSuffixes the EnhancedSuffixes string array
    */
   public void setEnhancedSuffixes(String[] enhancedSuffixes)
   {
      this.enhancedSuffixes = enhancedSuffixes;
   }
   
   /**
    * Initialise the SuffixOrder from EnhancedSuffixes.
    * 
    * If no enchangedSuffixes is specified, DEFAULT_ENHANCED_SUFFIXES
    * will be used. Individual entries may contain an additional order
    * element of the form [order:]suffix, e.g. 100:.sar
    * 
    * The suffixes specified during init, will remain constant,
    * i.e. they can't be overriden or removed.
    */
   public void initialize()
   {
      // if enhancedSuffixes not provided, use the default
      if (enhancedSuffixes == null)
      {
         enhancedSuffixes = DEFAULT_ENHANCED_SUFFIXES;
      }

      // reset, just in case we are called more than once
      suffixes.clear();
      staticSuffixes.clear();
      
      // add all enhanced suffixes; mark them as static, too.
      for (int i = 0; i < enhancedSuffixes.length; i++)
      {
         EnhancedSuffix es = new EnhancedSuffix(enhancedSuffixes[i]);
         addSuffix(es);
         
         // mark all initial entries as static!
         staticSuffixes.add(es.suffix);
      }
      
      // set the resulting SuffixOrder
      sorter.setSuffixOrder(produceSuffixOrder());
   }

   /**
    * Add the specified enhanced suffixes in the correct
    * position(s) and regenerate the SuffixOrder, if needed.
    * 
    * A suffix that exists already and is marked as static
    * will be skipped. Otherwise, duplicate entries are allowed.
    */
   public void addEnhancedSuffixes(String [] enhancedSuffixes)
   {
      if (enhancedSuffixes != null)
      {
         // remember the initial size of the list
         int size = suffixes.size();
         
         // add all enhanced suffixes
         for (int i = 0; i < enhancedSuffixes.length; i++)
         {
            EnhancedSuffix es = new EnhancedSuffix(enhancedSuffixes[i]);
            addSuffix(es);
         }
         if (suffixes.size() > size)
         {
            // suffixes were added, recreate the resulting SuffixOrder
            sorter.setSuffixOrder(produceSuffixOrder());
         }        
      }
   }
   
   /**
    * Insert the specified suffixes in the correct position
    * and regenerate the SuffixOrder array, if needed.
    * 
    * A suffix that exists already and is marked as static
    * will be skipped. Otherwise, duplicate entries are allowed.
    */
   public void addSuffixes(String[] suffixes, int relativeOrder)
   {
      if (suffixes != null)
      {
         // remember the initial size of the list
         int size = this.suffixes.size();
         
         for (int i = 0; i < suffixes.length; i++)
         {
            addSuffix(new EnhancedSuffix(suffixes[i], relativeOrder));
         }
         
         if (this.suffixes.size() > size)
         {
            // suffixes were added, recreate the resulting SuffixOrder
            sorter.setSuffixOrder(produceSuffixOrder());
         }
      }
   }
   
   /**
    * Remove the enhanced suffixes if they are not marked as static
    * and regenerate the SuffixOrder, if needed.
    */
   public void removeEnhancedSuffixes(String[] enhancedSuffixes)
   {
      if (enhancedSuffixes != null)
      {
         // remember the initial size of the list
         int size = suffixes.size();
         
         for (int i = 0; i < enhancedSuffixes.length; i++)
         {
            EnhancedSuffix es = new EnhancedSuffix(enhancedSuffixes[i]);

            // if this is a static suffix, don't remove
            if (staticSuffixes.contains(es.suffix))
            {
               continue;
            }
            else
            {
               // remove if exists
               suffixes.remove(es);
            }            
         }
         
         if (this.suffixes.size() < size)
         {
            // entries removed, recreate the resulting SuffixOrder
            sorter.setSuffixOrder(produceSuffixOrder());            
         }    
      }
   }
   
   /**
    * Remove the specified suffixes if they are not marked as static
    * and regenerate the SuffixOrder, if needed.
    */
   public void removeSuffixes(String[] suffixes, int relativeOrder)
   {
      if (suffixes != null)
      {
         // remember the initial size of the list
         int size = this.suffixes.size();

         for (int i = 0; i < suffixes.length; i++)
         {         
            // if this is a static suffix, don't remove
            if (staticSuffixes.contains(suffixes[i]))
            {
               continue;
            }
            else
            {
               // remove if exists
               this.suffixes.remove(new EnhancedSuffix(suffixes[i], relativeOrder));
            }
         }
         
         if (this.suffixes.size() < size)
         {
            // entries removed, recreate the resulting SuffixOrder
            sorter.setSuffixOrder(produceSuffixOrder());            
         }
      }
   }
   
   // Private -------------------------------------------------------
   
   /**
    * Produce the SuffixOrder from the sorted suffixes ArrayList
    */
   private String[] produceSuffixOrder()
   {
      String[] suffixOrder = new String[suffixes.size()];
      
      for (int i = 0; i < suffixes.size(); i++)
      {
         suffixOrder[i] = ((EnhancedSuffix)suffixes.get(i)).suffix;
      }
      return suffixOrder;
   }

   /**
    * Add an EnhancedSuffix at the correct position in the sorted List.
    * 
    * Sorting is based on EnhancedSuffix.order. A new entry with an equal
    * order value to an existing entry is placed AFTER the existing entry. 
    * 
    * If EnhancedSuffix.suffix exists in the staticSuffixes Set the entry
    * is NOT added. Otherwise, they EnhancedSuffix will be added, even
    * if it is a duplicate of an existing one.
    * 
    * @param enhancedsuffix the enhanced suffix
    */
   private void addSuffix(EnhancedSuffix enhancedSuffix)
   {
      // if this is a static suffix, don't add it
      if (staticSuffixes.contains(enhancedSuffix.suffix))
      {
         log.debug("Static suffix exists; ignoring request for adding enhanced suffix: " + enhancedSuffix);
      }
      else
      {
         int size = suffixes.size();
         
         // if List empty, just add the suffix
         if (size == 0)
         {
            suffixes.add(enhancedSuffix);
         }
         else
         {
            // insertion sort starting from the last element
            for (int i = size - 1; i > -1; i--)
            {
               EnhancedSuffix entry = (EnhancedSuffix)suffixes.get(i);
               if (enhancedSuffix.order >= entry.order)
               {
                  // add the suffix AFTER the entry and stop
                  suffixes.add(i + 1, enhancedSuffix);
                  break;
               }
               else if (i == 0)
               {
                  // reached the beginning so add the suffix right there
                  suffixes.add(0, enhancedSuffix);
               }
            }
         }
      }
   }
   
   /**
    * Inner class that encapsulates an enhanceSuffix
    * consisting of suffix + order
    */
   public final static class EnhancedSuffix
   {
      /** The suffix, e.g. .sar */
      public String suffix;
      
      /** The order, by convention a 3 digit number, e.g. 100 */
      public int order;
      
      /**
       * Simple CTOR
       */
      public EnhancedSuffix(String suffix, int order)
      {
         this.suffix = suffix;
         this.order  = order;
      }
      
      /**
       * CTOR that parses an enhancedSuffix string of the form: [order:]suffix
       * If the optional 'order' is missing, use DEFAULT_RELATIVE_ORDER
       */
      public EnhancedSuffix(String enhancedSuffix) throws IllegalArgumentException
      {
         StringTokenizer tokenizer = new StringTokenizer(enhancedSuffix, ":");
         int tokens = tokenizer.countTokens();
         
         switch (tokens)
         {
            case 1:
               this.order  = DEFAULT_RELATIVE_ORDER;               
               this.suffix = enhancedSuffix;
               break;
             
            case 2:
               this.order  = Integer.parseInt(tokenizer.nextToken());               
               this.suffix = tokenizer.nextToken();
               break;
               
            default:
               throw new IllegalArgumentException("Cannot parse enhancedSuffix: " + enhancedSuffix);
         }
      }

      /**
       * Override equals to allow EnhancedSuffix to be searchable
       * using ArrayList.indexOf()/ArrayList.lastIndexOf()
       * 
       * Base equality on both suffix and order
       */
      public boolean equals(Object other)
      {
         if (other == this)
            return true;

         if (!(other instanceof EnhancedSuffix))
            return false;

         EnhancedSuffix that = (EnhancedSuffix)other;
            
         // suffix shouldn't be null
         return this.suffix.equals(that.suffix) && this.order == that.order;
      }
      
      /**
       * Use both fields
       */
      public int hashCode()
      {
         int result = 17;
         result = 37 * result + suffix.hashCode();
         result = 37 * result + order;
         return result;
      }
      
      /**
       * Pretty print
       */
      public String toString()
      {
         return order + ":" + suffix;
      }
   }
}
