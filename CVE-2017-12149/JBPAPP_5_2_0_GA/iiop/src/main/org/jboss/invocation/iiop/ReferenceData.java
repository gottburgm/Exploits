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
package org.jboss.invocation.iiop;

import java.io.IOException;
import java.io.Serializable;
import org.jboss.util.Conversion;

/**
 * Helper class used to create a byte array ("reference data") to be embedded
 * into a CORBA reference and to extract object/servant identification info
 * from this byte array.  If this info consists simply of an 
 * <code>objectId</code>, this id is serialized into the byte array. If this 
 * info consists of a pair (servantId, objectId), a <code>ReferenceData</code>
 * instance containing the pair is is serialized into the byte array. 
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 */
public class ReferenceData
      implements Serializable
{
   private Object servantId;
   private Object objectId;

   public static byte[] create(Object servantId, Object objectId)
   {
      return Conversion.toByteArray(new ReferenceData(servantId, objectId));
   }

   public static byte[] create(Object id)
   {
      return Conversion.toByteArray(id);
   }

   public static Object extractServantId(byte[] refData, ClassLoader cl) 
         throws IOException, ClassNotFoundException 
   {
      Object obj = Conversion.toObject(refData, cl);
      if (obj != null && obj instanceof ReferenceData)
         return ((ReferenceData)obj).servantId;
      else
         return obj;
   }

   public static Object extractServantId(byte[] refData) 
         throws IOException, ClassNotFoundException 
   {
      return extractServantId(refData, 
                              Thread.currentThread().getContextClassLoader());
   }

   public static Object extractObjectId(byte[] refData, ClassLoader cl) 
         throws IOException, ClassNotFoundException 
   {
      Object obj = Conversion.toObject(refData, cl);
      if (obj != null && obj instanceof ReferenceData)
         return ((ReferenceData)obj).objectId;
      else
         return obj;
   }
   
   public static Object extractObjectId(byte[] refData) 
         throws IOException, ClassNotFoundException 
   {
      return extractObjectId(refData, 
                             Thread.currentThread().getContextClassLoader());
   }
   
   private ReferenceData(Object servantId, Object objectId) 
   {
      this.servantId = servantId;
      this.objectId = objectId;
   }
   
}

