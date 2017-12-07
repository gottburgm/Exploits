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
package org.jboss.services.deployment;

import org.jboss.logging.Logger;

/**
 * Helper class to pass in to velocity templates and
 * do things that are more easily performed in java code.
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public final class ContextHelper
{
   // Static --------------------------------------------------------
   
   /** the Logger instance */
   private static final Logger log = Logger.getLogger(ContextHelper.class);
   
   // Private Data --------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
    * Default CTOR
    */
   public ContextHelper()
   {
      // empty
   }
   
   // Public --------------------------------------------------------
   
   /**
    * Convenience method to find out if a variable is defined (not null).
    * This helps particularly to check if a Boolean variable has been
    * passed to the velocity context, since the #if( $boolean )
    * construct is only satisfied, if the value both exists and is true.
    * 
    * @param obj any object
    * @return true if the object is not null, false otherwise
    */
   public static boolean isDefined(Object obj)
   {
      return obj != null ? true : false;
   }
}
