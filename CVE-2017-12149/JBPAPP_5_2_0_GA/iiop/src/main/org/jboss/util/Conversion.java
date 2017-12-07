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
package org.jboss.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import org.jboss.util.stream.CustomObjectInputStreamWithClassloader;
import org.jboss.util.stream.CustomObjectOutputStream;

/**
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 */
public class Conversion
{
   /**
    * Receives an object and converts it into a byte array. Used to embed
    * a JBoss oid into the "reference data" (object id) field of a CORBA 
    * reference.
    */
   public static byte[] toByteArray(Object obj) 
   {
      try {
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         ObjectOutputStream oos = new CustomObjectOutputStream(os);

         oos.writeObject(obj);
         oos.flush();
         byte[] a = os.toByteArray();
         os.close();
         return a;
      }
      catch (IOException ioe) {
         throw new RuntimeException("Object id serialization error:\n" + ioe);
      }
   }

   /**
    * Receives a classloader and a byte array previously returned by a call to 
    * <code>toByteArray</code> and retrieves an object from it. Used to 
    * extract a JBoss oid from the "reference data" (object id) field of a
    * CORBA reference. 
    */
   public static Object toObject(byte[] a, ClassLoader cl)
         throws IOException, ClassNotFoundException 
   {
      ByteArrayInputStream is = new ByteArrayInputStream(a);
      ObjectInputStream ois = 
	 new CustomObjectInputStreamWithClassloader(is, cl);
      Object obj = ois.readObject();
      is.close();
      return obj;
   }

   /**
    * Receives a byte array previously returned by a call to 
    * <code>toByteArray</code> and retrieves an object from it. Used to
    * extract a JBoss oid from the "reference data" (object id) field of a
    * CORBA reference. 
    */
   public static Object toObject(byte[] a) 
         throws IOException, ClassNotFoundException 
   {
      return toObject(a, Thread.currentThread().getContextClassLoader());
   }

}
