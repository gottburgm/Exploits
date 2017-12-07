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
package org.jboss.ejb.plugins;

import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteStub;
import java.security.PrivilegedAction;
import java.security.AccessController;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.ejb.Handle;
import javax.ejb.SessionContext;
import javax.transaction.UserTransaction;


/**
 * The SessionObjectOutputStream is used to serialize stateful session beans
 * when they are passivated
 *      
 * @see org.jboss.ejb.plugins.SessionObjectInputStream
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 81030 $
 */
public class SessionObjectOutputStream
   extends ObjectOutputStream
{
   // Constructors -------------------------------------------------
   public SessionObjectOutputStream(OutputStream out)
      throws IOException
   {
      super(out);
      EnableReplaceObjectAction.enableReplaceObject(this);
   }

   // ObjectOutputStream overrides ----------------------------------
   protected Object replaceObject(Object obj)
      throws IOException
   {
      Object replacement = obj;
      // section 6.4.1 of the ejb1.1 specification states what must be taken care of 
      
      // ejb reference (remote interface) : store handle
      if (obj instanceof EJBObject)
         replacement = ((EJBObject)obj).getHandle();
      
      // ejb reference (home interface) : store handle
      else if (obj instanceof EJBHome)
         replacement = ((EJBHome)obj).getHomeHandle();
      
      // session context : store a typed dummy object
      else if (obj instanceof SessionContext)
         replacement = new StatefulSessionBeanField(StatefulSessionBeanField.SESSION_CONTEXT);

      // naming context : the jnp implementation is serializable, do nothing

      // user transaction : store a typed dummy object
      else if (obj instanceof UserTransaction)
         replacement = new StatefulSessionBeanField(StatefulSessionBeanField.USER_TRANSACTION);      

      else if( obj instanceof Handle )
         replacement = new HandleWrapper((Handle)obj);

      else if( (obj instanceof Remote) && !(obj instanceof RemoteStub) )
      {
         Remote remote = (Remote) obj;
         try
         {
            replacement = RemoteObject.toStub(remote);
         }
         catch(IOException ignore)
         {
            // Let the Serialization layer try with original object
         }
      }

      return replacement;
   }

   private static class EnableReplaceObjectAction implements PrivilegedAction
   {
      SessionObjectOutputStream os;
      EnableReplaceObjectAction(SessionObjectOutputStream os)
      {
         this.os = os;
      }
      public Object run()
      {
         os.enableReplaceObject(true);
         return null;
      }
      static void enableReplaceObject(SessionObjectOutputStream os)
      {
         EnableReplaceObjectAction action = new EnableReplaceObjectAction(os);
         AccessController.doPrivileged(action);
      }
   }
}

