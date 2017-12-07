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
package org.jboss.services.binding;


/**
 * ServiceBindingManager StandardMBean interface
 * 
 * @author <a href="mailto:bitpushr@rochester.rr.com">Mike Finn</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author Brian Stansberry
 * 
 * @version $Revision: 88766 $
 */
public interface ServiceBindingManagerMBean
{
   // Attributes ----------------------------------------------------

   /**
    * The name of the server this manager is associated with. This is a
    * logical name used to lookup {@link ServiceBinding}s from the {@link ServiceBindingStore}.
    */
   String getServerName();

}
