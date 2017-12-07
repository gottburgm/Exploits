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
package org.jboss.system;

/**
 * Thrown to indicate that a required attribute has not been set.
 *
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version <tt>$Revision: 81033 $</tt>
 */
public class MissingAttributeException
   extends ConfigurationException
{
   /**
    * Construct a <tt>MissingAttributeException</tt> with the specified detail 
    * message.
    *
    * @param name    The attribute name.
    */
   public MissingAttributeException(final String name) {
      super(makeMessage(name));
   }

   /**
    * Construct a <tt>MissingAttributeException</tt> with the specified detail 
    * message and nested <tt>Throwable</tt>.
    *
    * @param name    The attribute name.
    * @param nested  Nested <tt>Throwable</tt>.
    */
   public MissingAttributeException(final String name, final Throwable nested) {
      super(makeMessage(name), nested);
   }

   /**
    * Make a execption message for the attribute name.
    */
   private static String makeMessage(final String name) {
      return "Missing attribute '" + name + "'";
   }
}
