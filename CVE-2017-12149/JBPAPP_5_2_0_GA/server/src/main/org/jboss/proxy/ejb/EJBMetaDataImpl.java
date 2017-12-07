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
package org.jboss.proxy.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.ejb.HomeHandle;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import javax.ejb.EJBException;

/**
 * An implementation of the EJBMetaData interface which allows a
 * client to obtain the enterprise Bean's meta-data information.
 *
 * @author  Rickard Oberg (rickard.oberg@telkel.com)
 * @author  <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 81030 $
 */
public class EJBMetaDataImpl
      implements EJBMetaData, Serializable
{
   /** Serial Version Identifier. @since 1.1 */
   private static final long serialVersionUID = -3698855455664391097L;

   // Attributes ----------------------------------------------------
   private final Class remote;
   private final Class home;
   private final Class pkClass;

   private final boolean session;
   private final boolean statelessSession;
   private final HomeHandle homeHandle;

   // Constructors --------------------------------------------------

   /**
    * Construct an <tt>EJBMetaDataImpl</tt>.
    * this should only be accessible from the factory.
    */
   public EJBMetaDataImpl(final Class remote,
         final Class home,
         final Class pkClass,
         final boolean session,
         final boolean statelessSession,
         final HomeHandle homeHandle)
   {
      this.remote = remote;
      this.home = home;
      this.pkClass = pkClass;
      this.session = session;
      this.statelessSession = statelessSession;
      this.homeHandle = homeHandle;
   }

   // Constructors --------------------------------------------------

   // EJBMetaData ---------------------------------------------------


   // EJBMetaData ---------------------------------------------------

   /**
    * Obtain the home interface of the enterprise Bean.
    *
    * @throws EJBException     Failed to get EJBHome object.
    */

   public EJBHome getEJBHome()
   {
      try
      {
         return homeHandle.getEJBHome();
      }
      catch (RemoteException e)
      {
         e.printStackTrace();
         throw new EJBException(e);
      }
   }

   /**
    * Obtain the Class object for the enterprise Bean's home interface.
    */
   public Class getHomeInterfaceClass()
   {
      return home;
   }

   /**
    * Obtain the Class object for the enterprise Bean's remote interface.
    */
   public Class getRemoteInterfaceClass()
   {
      return remote;
   }

   /**
    * Obtain the Class object for the enterprise Bean's primary key class.
    */
   public Class getPrimaryKeyClass()
   {
      if (session == true)
         throw new RuntimeException("A session bean does not have a primary key class");

      return pkClass;
   }

   /**
    * Test if the enterprise Bean's type is "session".
    *
    * @return True if the type of the enterprise Bean is session bean.
    */
   public boolean isSession()
   {
      return session;
   }

   /**
    * Test if the enterprise Bean's type is "stateless session".
    *
    * @return True if the type of the enterprise Bean is stateless session.
    */
   public boolean isStatelessSession()
   {
      return statelessSession;
   }
}
