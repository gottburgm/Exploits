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

import java.util.Collection;

import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;

/**
 * This is an extension to the EJBProxyFactory interface. Although some
 * implementations of the ProxyFactory interface may provide access
 * to local interfaces, others (e.g. which provide remote distribution)
 * will not. Good example: the JRMP delegates do not need to implement
 * this interface.
 *
 * @see EJBProxyFactory
 * 
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @version $Revision: 81030 $
 */
public interface LocalProxyFactory
   extends GenericEntityObjectFactory, ContainerPlugin
{
   /**
    * This method is called whenever the EJBLocalHome implementation for this
    * container is needed.
    *
    * @return    an implementation of the local home interface for this
    *            container
    */
   EJBLocalHome getEJBLocalHome();

   /**
    * This method is called whenever an EJBLocalObject implementation for a
    * stateless session bean is needed.
    *
    * @return    an implementation of the local interface for this container
    */
   EJBLocalObject getStatelessSessionEJBLocalObject();

   /**
    * This method is called whenever an EJBLocalObject implementation for a
    * stateful session bean is needed.
    *
    * @param id    the id of the session
    * @return       an implementation of the local interface for this container
    */
   EJBLocalObject getStatefulSessionEJBLocalObject(Object id);
      
   /**
    * This method is called whenever an EJBLocalObject implementation for an
    * entitybean is needed.
    *
    * @param id    the primary key of the entity
    * @return      an implementation of the local interface for this container
    */
   EJBLocalObject getEntityEJBLocalObject(Object id);

   /**
    * This method is called whenever a new EJBLocalObject should be created.
    * Called when the instance is created.
    *
    * @param id    the primary key of the entity
    * @return      an implementation of the local interface for this container
    */
   EJBLocalObject getEntityEJBLocalObject(Object id, boolean create);

   /**
    * This method is called whenever a collection of EJBLocalObjects for a
    * collection of primary keys is needed.
    *
    * @param c       collection of primary keys
    * @return        a collection of EJBLocalObjects implementing the remote
    *                interface for this container
    */
   Collection getEntityLocalCollection(Collection c);
}
