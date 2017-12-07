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
package org.jboss.proxy.ejb.handle;

import javax.ejb.HomeHandle;
import javax.ejb.EJBHome;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.rmi.ServerException;
import java.rmi.RemoteException;
import java.io.ObjectStreamField;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import org.jboss.naming.NamingContextFactory;


/**
 * An EJB home handle implementation.
 *
 * @author  <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public class HomeHandleImpl
      implements HomeHandle
{
   // Constants -----------------------------------------------------

   /** Serial Version Identifier. */
   static final long serialVersionUID = 208629381571948124L;
   /** The persistent field defintions */
   private static final ObjectStreamField[] serialPersistentFields =
      new ObjectStreamField[]
   {
      new ObjectStreamField("jndiName", String.class),
      new ObjectStreamField("jndiEnv", Hashtable.class)
   };

   /** The JNDI name of the home inteface binding */
   private String jndiName;
   /** The JNDI env in effect when the home handle was created */
   private Hashtable jndiEnv;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a <tt>HomeHandleImpl</tt>.
    *
    * @param handle    The initial context handle that will be used
    *                  to restore the naming context or null to use
    *                  a fresh InitialContext object.
    * @param name      JNDI name.
    */
   public HomeHandleImpl(String jndiName)
   {
      this.jndiName = jndiName;
      this.jndiEnv = (Hashtable) NamingContextFactory.lastInitialContextEnv.get();
   }

   // Public --------------------------------------------------------

   // Handle implementation -----------------------------------------

   /**
    * HomeHandle implementation.
    *
    * @return  <tt>EJBHome</tt> reference.
    *
    * @throws ServerException    Could not get EJBObject.
    * @throws RemoteException
    */
   public EJBHome getEJBHome() throws RemoteException
   {
      try
      {
         InitialContext ic = null;
         if( jndiEnv != null )
            ic = new InitialContext(jndiEnv);
         else
            ic = new InitialContext();
         EJBHome home = (EJBHome) ic.lookup(jndiName);
         return home;
      }
      catch (NamingException e)
      {
         throw new ServerException("Could not get EJBHome", e);
      }
   }

   /**
    * @return the jndi name
    */
   public String getJNDIName()
   {
      return jndiName;
   }

   private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException
   {
      ObjectInputStream.GetField getField = ois.readFields();
      jndiName = (String) getField.get("jndiName", null);
      jndiEnv = (Hashtable) getField.get("jndiEnv", null);
   }

   private void writeObject(ObjectOutputStream oos)
      throws IOException
   {
      ObjectOutputStream.PutField putField = oos.putFields();
      putField.put("jndiName", jndiName);
      putField.put("jndiEnv", jndiEnv);
      oos.writeFields();
   }
}

