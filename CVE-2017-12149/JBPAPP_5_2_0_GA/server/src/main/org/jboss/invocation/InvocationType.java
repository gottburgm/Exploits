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
package org.jboss.invocation;

import java.io.Serializable;
import java.io.ObjectStreamException;

/** Type safe enumeration used for to identify the invocation types.
 *
 * @author Scott.Stark@jboss.org
 * @author Christoph.Jung@infor.de
 * @version $Revision: 81030 $
 */
public final class InvocationType implements Serializable
{
   /** Serial Version Identifier. @since 1.2 */
   private static final long serialVersionUID = 6460196085190851775L;
   /** The method-intf names for the InvocationType enums */
   private static final String[] INTERFACE_NAMES = { "Remote",
      "Local", "Home", "LocalHome", "ServiceEndpoint"
   };

   /** The max ordinal value in use for the InvocationType enums. When you add a
    * new key enum value you must assign it an ordinal value of the current
    * MAX_TYPE_ID+1 and update the MAX_TYPE_ID value.
    */
   private static final int MAX_TYPE_ID = 4;

   /** The array of InvocationKey indexed by ordinal value of the key */
   private static final InvocationType[] values = new InvocationType[MAX_TYPE_ID+1];
   public static final InvocationType REMOTE =
         new InvocationType("REMOTE", 0);
   public static final InvocationType LOCAL =
         new InvocationType("LOCAL", 1);
   public static final InvocationType HOME =
         new InvocationType("HOME", 2);
   public static final InvocationType LOCALHOME =
         new InvocationType("LOCALHOME", 3);
   public static final InvocationType SERVICE_ENDPOINT =
		 new InvocationType("SERVICE_ENDPOINT", 4);

   private final transient String name;

   // this is the only value serialized
   private final int ordinal;

   private InvocationType(String name, int ordinal)
   {
      this.name = name;
      this.ordinal = ordinal;
      values[ordinal] = this;
   }

   public String toString()
   {
      return name;
   }
   /** Get the method-intf name for the type
    * 
    * @return one of: "Remote", "Local", "Home", "LocalHome", "ServiceEndpoint"
    */ 
   public String toInterfaceString()
   {
      return INTERFACE_NAMES[ordinal];
   }

   Object readResolve() throws ObjectStreamException
   {
      return values[ordinal];
   }
}
