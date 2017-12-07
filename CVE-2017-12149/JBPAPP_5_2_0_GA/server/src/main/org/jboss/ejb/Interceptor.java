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
package org.jboss.ejb;

import org.jboss.invocation.Invocation;


/**
 * Provides the interface for all container interceptors.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 81030 $
 *
 *<p><b>20011219 marc fleury:</b>
 * <ul>
 *  <li>Method Invocation is replaced by Invocation
 *</ul>
 */
public interface Interceptor
   extends ContainerPlugin
{
   /**
    * Set the next interceptor in the chain.
    *
    * @param interceptor        The next interceptor in the chain.
    */
   void setNext(Interceptor interceptor);

   /**
    * Get the next interceptor in the chain.
    *
    * @return   The next interceptor in the chain.
    */
   Interceptor getNext();
   
   /**
    * ???
    *
    * @param mi         ???
    * @return           ???
    *
    * @throws Exception ???
    */
   Object invokeHome(Invocation mi) throws Exception;

   /**
    * ???
    *
    * @param mi         ???
    * @return           ???
    *
    * @throws Exception ???
    */
   Object invoke(Invocation mi) throws Exception;
}

