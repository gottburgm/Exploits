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

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.RemoveException;


/**
 *	This interface is implemented by any EntityBean persistence managers plugins.
 *
 * <p>Implementations of this interface are called by other plugins in the
 *    container.  If the persistence manager wants to, it may attach any
 *    instance specific metadata to the EntityEnterpriseContext that is
 *    passed in method calls.
 *
 * @see EntityContainer
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 81030 $
 */
public interface EntityPersistenceManager extends ContainerPlugin
{
   /**
    * Returns a new instance of the bean class or a subclass of the bean class.
    * 
    * @return the new instance
    */
   Object createBeanClassInstance() throws Exception;

   /**
    * This method is called whenever an entity is to be created. The
    * persistence manager is responsible for calling the ejbCreate method
    * on the instance and to handle the results properly wrt the persistent
    * store.
    *
    * @param m           the create method in the home interface that was
    *                    called
    * @param args        any create parameters
    * @param instance    the instance being used for this create call
    */
   void createEntity(Method m,
                     Object[] args,
                     EntityEnterpriseContext instance)
      throws Exception;

   /**
    * This method is called whenever an entity is to be created. The
    * persistence manager is responsible for calling the ejbPostCreate method
    * on the instance and to handle the results properly wrt the persistent
    * store.
    *
    * @param m           the create method in the home interface that was
    *                    called
    * @param args        any create parameters
    * @param instance    the instance being used for this create call
    */
   void postCreateEntity(Method m,
                     Object[] args,
                     EntityEnterpriseContext instance)
      throws Exception;

   /**
    * This method is called when single entities are to be found. The
    * persistence manager must find out whether the wanted instance is
    * available in the persistence store, and if so it shall use the
    * EJBProxyFactory plugin to create an EJBObject to the instance, which
    * is to be returned as result.
    *
    * @param finderMethod    the find method in the home interface that was
    *                        called
    * @param args            any finder parameters
    * @param instance        the instance to use for the finder call
    * @return                an EJBObject representing the found entity
    */
   Object findEntity(Method finderMethod,
                     Object[] args,
                     EntityEnterpriseContext instance,
                     GenericEntityObjectFactory factory)
      throws Exception;

   /**
    * This method is called when collections of entities are to be found. The
    * persistence manager must find out whether the wanted instances are
    * available in the persistence store, and if so it shall use the
    * EJBProxyFactory plugin to create EJBObjects to the instances, which are
    * to be returned as result.
    *
    * @param finderMethod    the find method in the home interface that was
    *                        called
    * @param args            any finder parameters
    * @param instance        the instance to use for the finder call
    * @return                an EJBObject collection representing the found
    *                        entities
    */
   Collection findEntities(Method finderMethod,
                           Object[] args,
                           EntityEnterpriseContext instance,
                           GenericEntityObjectFactory factory)
      throws Exception;

   /**
    * This method is called when an entity shall be activated. The persistence
    * manager must call the ejbActivate method on the instance.
    *
    * @param instance    the instance to use for the activation
    * 
    * @throws RemoteException    thrown if some system exception occurs
    */
   void activateEntity(EntityEnterpriseContext instance)
      throws RemoteException;
   
   /**
    * This method is called whenever an entity shall be load from the
    * underlying storage. The persistence manager must load the state from
    * the underlying storage and then call ejbLoad on the supplied instance.
    *
    * @param instance    the instance to synchronize
    * 
    * @throws RemoteException    thrown if some system exception occurs
    */
   void loadEntity(EntityEnterpriseContext instance)
      throws RemoteException;
      
   /**
    * This method is used to determine if an entity should be stored.
    *
    * @param instance    the instance to check
    * @return true, if the entity has been modified
    * @throws Exception    thrown if some system exception occurs
    */
   boolean isStoreRequired(EntityEnterpriseContext instance) throws Exception;

   /**
    * This method is used to determined whether the instance was modified.
    * NOTE, even if the method returns true the isStoreRequired for this same instance
    * might return false, e.g. a CMR field that doesn't have a foreign key was modified.
    *  
    * @param ctx
    * @return
    * @throws Exception
    */
   boolean isModified(EntityEnterpriseContext ctx) throws Exception;

   /**
    * This method is called whenever an entity shall be stored to the
    * underlying storage. The persistence manager must call ejbStore on the
    * supplied instance and then store the state to the underlying storage.
    *
    * @param instance    the instance to synchronize
    * 
    * @throws RemoteException    thrown if some system exception occurs
    */
   void storeEntity(EntityEnterpriseContext instance)
      throws RemoteException;

   /**
    * Invokes ejbStore on the instance.
    *
    * @param instance
    * @throws RemoteException
    */
   void invokeEjbStore(EntityEnterpriseContext instance)
      throws RemoteException;
   
   /**
    * This method is called when an entity shall be passivate. The persistence
    * manager must call the ejbPassivate method on the instance.
    *
    * @param instance    the instance to passivate
    * 
    * @throws RemoteException    thrown if some system exception occurs
    */
   void passivateEntity(EntityEnterpriseContext instance)
      throws RemoteException;

   /**
    * This method is called when an entity shall be removed from the
    * underlying storage. The persistence manager must call ejbRemove on the
    * instance and then remove its state from the underlying storage.
    *
    * @param instance    the instance to remove
    * 
    * @throws RemoteException    thrown if some system exception occurs
    * @throws RemoveException    thrown if the instance could not be removed
    */
   void removeEntity(EntityEnterpriseContext instance)
      throws RemoteException, RemoveException;
}

