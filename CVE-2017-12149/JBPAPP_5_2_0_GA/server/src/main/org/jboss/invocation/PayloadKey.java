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

/** Type safe enumeration used for to identify the payloads.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public final class PayloadKey implements Serializable
{
   /** Serial Version Identifier. @since 1.1.4.1 */
   private static final long serialVersionUID = 5436722659170811314L;

   /** The max ordinal value in use for the PayloadKey enums. When you add a
    * new key enum value you must assign it an ordinal value of the current
    * MAX_KEY_ID+1 and update the MAX_KEY_ID value.
    */
   private static final int MAX_KEY_ID = 3;

   /** The array of InvocationKey indexed by ordinal value of the key */
   private static final PayloadKey[] values = new PayloadKey[MAX_KEY_ID+1];

   /** Put me in the transient map, not part of payload. */
   public final static PayloadKey TRANSIENT = new PayloadKey("TRANSIENT", 0);
   
   /** Do not serialize me, part of payload as is. */
   public final static PayloadKey AS_IS = new PayloadKey("AS_IS", 1);

   /** Put me in the payload map. */
   public final static PayloadKey PAYLOAD = new PayloadKey("PAYLOAD", 2);

   private final transient String name;

   // this is the only value serialized
   private final int ordinal;
 
   private PayloadKey(String name, int ordinal)
   {
      this.name = name;
      this.ordinal = ordinal;
      values[ordinal] = this;
   }

   public String toString()
   {
      return name;
   }

   Object readResolve() throws ObjectStreamException
   {
      return values[ordinal];
   }
}
