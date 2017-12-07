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
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.jboss.serial.io.JBossObjectInputStream;
import org.jboss.serial.io.JBossObjectOutputStream;

/**
 * A RemoteSerializerImpl.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 71554 $
 */
public class RemoteSerializerImpl implements RemoteSerializer, Serializable
{
   
   private static final RemoteSerializer serializer = new RemoteSerializerImpl();
   
   /** The serialVersionUID */
   private static final long serialVersionUID = 6386719587282465130L;

   public byte[] serializeToByte(final Object target) throws Exception
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputStream oos = new ObjectOutputStream(baos);
      JBossObjectOutputStream jbos = new JBossObjectOutputStream(oos);
      jbos.writeObject(target);
      jbos.close();   
      
      return baos.toByteArray();
      
   }
   
   public Object serialize(final Object target) throws Exception{
      
      return shouldSerialize(target) ? serialize(target) : target;
      
   }
   
   public Object deserialize(Object target) throws Exception{
      
      SerializableWrapper wrapper = (SerializableWrapper)target;
      byte[] payload = wrapper.getPayload();
      ByteArrayInputStream bais = new ByteArrayInputStream(payload);
      ObjectInputStream ois = new ObjectInputStream(bais);
      JBossObjectInputStream jbis = new JBossObjectInputStream(ois);
      Object result = jbis.readObject();
      return result;
      
   }
   public void serialize(final Object[] targets) throws Exception
   {
      
      for (int i = 0; i < targets.length; i++)
      {
         final Object target = targets[i];   
         targets[i] = serialize(target);
           
      }
            
   }
   
   public boolean shouldSerialize(Object target)
   {
      return !(target instanceof Serializable);
      
   }
   
   static RemoteSerializer getInstance(){
      
      return serializer;   
   
   }
}
