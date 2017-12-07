/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.system.server.profileservice.repository.clustered.sync;

import java.io.Serializable;

/**
 * Encapsulates the results of an IO read operation for transmission
 * across the cluster. 
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class ByteChunk implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -1278778998152090786L;
   
   private final int byteCount;
   private final byte[] bytes;
   
   /**
    * Create a new ByteChunk.
    * 
    * @param bytes source bytes. Note that this array will not necessarily
    *              be defensively copied, so callers should not alter it
    *              after passing it to this object.
    * @param byteCount number of bytes in <code>bytes</code> that are valid;
    *                  rest are filler. <code>-1</code> indicates end of stream.
    */
   public ByteChunk(byte[] bytes, int byteCount)
   {
      if (bytes == null)
      {
         throw new IllegalArgumentException("Null bytes");
      }
      if (byteCount < 0)
      {
         this.byteCount = -1;
         this.bytes = null;
      }
      else
      {
         this.byteCount = byteCount;
         int diff = bytes.length - byteCount;
         // If we're wasting too much, discard unneeded bytes at the
         // cost of a copy. TODO refine or discard this
         if (diff > (10 * 1024) && diff > (bytes.length / 4))
         {
            this.bytes = new byte[byteCount];
            System.arraycopy(bytes, 0, this.bytes, 0, byteCount);
         }
         else
         {
            this.bytes = bytes;
         }
      }         
   }
   
   /**
    * Gets the number of valid bytes.
    * 
    * @return the number of valid bytes, or <code>-1</code> to indicate end of stream
    */
   public int getByteCount()
   {
      return byteCount;
   }
   
   /**
    * The bytes contained by this chunk. The length of the array could be
    * longer than {@link #getByteCount()} in which case the excess bytes
    * are filler.
    * 
    * @return the bytes, or <code>null</code> if {@link #getByteCount()} would
    *         return <code>-1</code>. Not that this may be a direct reference
    *         to this object's internal byte buffer, so callers should not
    *         alter the byte array.
    */
   public byte[] getBytes()
   {      
      return this.bytes;
   }
}