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
package org.jboss.ha.singleton;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * StandardMBean interface for {@link HASingletonProfileManager}.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public interface HASingletonProfileManagerMBean
{

   /**
    * Set the uri list
    * 
    * @param list the list
    * @throws IOException
    */
   public abstract void setURIList(final List<URI> list) throws IOException;

   /**
    * Get the uri list
    * 
    * @return the list
    */
   public abstract List<URI> getURIList();

}