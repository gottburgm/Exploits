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
package org.jboss.invocation.pooled.interfaces;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.rmi.Remote;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteStub;

/**
 * An ObjectOutputStream subclass used by the MarshalledValue class to
 * ensure the classes and proxies are loaded using the thread context
 * class loader. Currently this does not do anything as neither class or
 * proxy annotations are used.
 *
 * @author Scott.Stark@jboss.org
 * @author Clebert.Suconic@jboss.org
 * @version $Revision: 81030 $
 */
public class OptimizedObjectOutputStream
   extends ObjectOutputStream
{
	
   /** Creates a new instance of MarshalledValueOutputStream
    If there is a security manager installed, this method requires a
    SerializablePermission("enableSubstitution") permission to ensure it's
    ok to enable the stream to do replacement of objects in the stream.
    */
   public OptimizedObjectOutputStream(OutputStream os) throws IOException
   {
      super(os);
      enableReplaceObject(true);
   }

   /**
    * Writes just the class name to this output stream.
    *
    * @param classdesc class description object
    */
   protected void writeClassDescriptor(ObjectStreamClass classdesc)
      throws IOException
   {
	  if (CompatibilityVersion.pooledInvokerLegacy)
	  {
		  writeUTF(classdesc.getName());
	  }
	  else
	  {
		  super.writeClassDescriptor(classdesc);
	  }
   }

   /** Override replaceObject to check for Remote objects that are
    not RemoteStubs.
   */
   protected Object replaceObject(Object obj) throws IOException
   {
      if( (obj instanceof Remote) && !(obj instanceof RemoteStub) )
      {
         Remote remote = (Remote) obj;
         try
         {
            obj = RemoteObject.toStub(remote);
         }
         catch(IOException ignore)
         {
            // Let the Serialization layer try with the orignal obj
         }
      }
      return obj;
   }
}
