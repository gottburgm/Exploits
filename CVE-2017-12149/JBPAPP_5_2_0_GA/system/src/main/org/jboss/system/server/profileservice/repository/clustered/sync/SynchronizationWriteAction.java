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

import java.io.IOException;


/**
 * A SynchronizationAction that involves writing content to an item.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public interface SynchronizationWriteAction<T extends SynchronizationActionContext> 
   extends SynchronizationAction<T>
{
   /**
    * Write the given bytes to the item referenced by this action.
    * 
    * @param bytes the bytes. Cannot be <code>null</code>. The
    *              {@link ByteChunk#getByteCount() byte count} must be greater
    *              than -1.
    * 
    * @throws IOException
    * @throws IllegalStateException if {@link #isComplete()} would return <code>true</code>
    */
   void writeBytes(ByteChunk bytes) throws IOException;
}
