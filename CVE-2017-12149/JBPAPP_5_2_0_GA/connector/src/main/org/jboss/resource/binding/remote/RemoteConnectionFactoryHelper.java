/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.binding.remote;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.rmi.server.UID;
import java.util.Hashtable;

import javax.naming.BinaryRefAddr;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

import org.jboss.util.naming.NonSerializableFactory;

/**
 * A RemoteConnectionFactoryHelper.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 71554 $
 */
public class RemoteConnectionFactoryHelper implements ObjectFactory
{
   /** The class VM ID */
   public static final UID vmID = new UID();

   public Object getObjectInstance(final Object obj, final Name name, final Context ctx, final Hashtable env) throws Exception
   {
     
      Object instance = null;
     
      if (obj instanceof Reference)
      {
         Reference ref = (Reference) obj;
         // Check the local id
         BinaryRefAddr localID = (BinaryRefAddr) ref.get("VMID");
         byte[] idBytes = (byte[]) localID.getContent();
         ByteArrayInputStream bais = new ByteArrayInputStream(idBytes);
         ObjectInputStream ois = new ObjectInputStream(bais);
         UID id = (UID) ois.readObject();
      
         if( id.equals(vmID) == true )
         {
            // Use the local datasource
            StringRefAddr jndiAddr = (StringRefAddr) ref.get("JndiName");
            String jndiName = (String) jndiAddr.getContent();
            instance = NonSerializableFactory.lookup(jndiName);
         }
         else
         {
            // Use the embedded proxy
            BinaryRefAddr proxyAddr = (BinaryRefAddr) ref.get("ProxyData");
            byte[] proxyBytes = (byte[]) proxyAddr.getContent();
            ByteArrayInputStream bais2 = new ByteArrayInputStream(proxyBytes);
            ObjectInputStream ois2 = new ObjectInputStream(bais2);
            instance = ois2.readObject();
         }
      }
      return instance;

   }
}
