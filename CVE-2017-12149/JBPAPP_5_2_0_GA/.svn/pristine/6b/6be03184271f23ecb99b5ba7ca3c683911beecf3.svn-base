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
 * A {@ link SynchronizationAction} that involves reading content from an item.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public interface SynchronizationReadAction<T extends SynchronizationActionContext> 
   extends SynchronizationAction<T>
{
   /**
    * Gets the next chunk of bytes from the item associated with this action.
    * Each call to this method will retrieve more bytes
    * 
    * @return a ByteChunk.
    * 
    * @throws IOException if there is a problem reading the bytes.
    * @throws IllegalStateException if {@link #isComplete()} would return <code>true</code>
    */
   ByteChunk getNextBytes() throws IOException;
}
