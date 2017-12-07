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

import org.jboss.system.Service;

/**
 * This is a superinterface for all Container plugins.
 * 
 * <p>All plugin interfaces must extend this interface.
 *      
 * @see Service
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @version $Revision: 81030 $
 */
public interface ContainerPlugin
   extends Service, AllowedOperationsFlags
{
   /**
    * This callback is set by the container so that the plugin may access it
    *
    * @param con The container using this plugin. This may be null if the
    plugin is being disassociated from a container.
    */
   void setContainer(Container con);
}
