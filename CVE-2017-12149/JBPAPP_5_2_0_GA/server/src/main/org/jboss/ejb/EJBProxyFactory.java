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

import javax.ejb.EJBMetaData;

import org.jboss.invocation.Invocation;
import org.jboss.metadata.InvokerProxyBindingMetaData;

/**
 * This is an interface for Container plugins. Implementations of this
 * interface are responsible for receiving remote invocations of EJB's
 * and to forward these requests to the Container it is being used with.
 *
 * <p>It is responsible for providing any EJBObject and EJBHome 
 *    implementations (which may be statically or dynamically created). 
 *
 * <p>Before forwarding a call to the container it must call
 *    Thread.setContextClassLoader() with the classloader of the container.
 *    It must also handle any propagated transaction and security contexts
 *    properly. It may acquire the TransactionManager from JNDI.
 *
 * @see Container
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @version $Revision: 81030 $
 *
 *<p><b>20011219 marc fleury:</b>
* <ul>
*  <li>Moved from typed return to Object to allow for optimizations in creation
*  <li>Per Dain Sundstrom requests removed the Remote Exception.
*  <li>The Container Invoker is going to be replaced by the ProxyFactory 
*</ul>
 */
public interface EJBProxyFactory
extends GenericEntityObjectFactory, ContainerPlugin
{
   /**
    * Set the invoker meta data so that the ProxyFactory can initialize properly
    */
   void setInvokerMetaData(InvokerProxyBindingMetaData imd);
   /**
    * Set the invoker jndi binding
    */
   void setInvokerBinding(String binding);
   /**
    * Protocol specific isIdentical implementation
    *
    * @param container the container
    * @param mi the invocation 
    * @return true when identical, false otherwise
    */
   boolean isIdentical(Container container, Invocation mi);
   /**
    * This method is called whenever the metadata for this container is
    * needed.
    *
    * @return    An implementation of the EJBMetaData interface.
    */
   EJBMetaData getEJBMetaData();

   /**
    * This method is called whenever the EJBHome implementation for this
    * container is needed.
    *
    * @return    An implementation of the home interface for this container.
    */
   Object getEJBHome();

   /**
    * This method is called whenever an EJBObject implementation for a
    * stateless session bean is needed.
    *
    * @return    An implementation of the remote interface for this container.
    */
   Object getStatelessSessionEJBObject();

   /**
    * This method is called whenever an EJBObject implementation for a stateful
    * session bean is needed.
    *
    * @param id    The id of the session.
    * @return      An implementation of the remote interface for this
    *              container.
    */
   Object getStatefulSessionEJBObject(Object id);

   /**
    * This method is called whenever an EJBObject implementation for an 
    * entitybean is needed.
    *
    * @param id    The primary key of the entity.
    * @return      An implementation of the remote interface for this
    *              container.
    */
   Object getEntityEJBObject(Object id);

   /**
    * This method is called whenever a collection of EJBObjects for a
    * collection of primary keys is needed.
    *
    * @param en      Enumeration of primary keys.
    * @return        A collection of EJBObjects implementing the remote
    *                interface for this container.
    */
   Collection getEntityCollection(Collection en);
}

