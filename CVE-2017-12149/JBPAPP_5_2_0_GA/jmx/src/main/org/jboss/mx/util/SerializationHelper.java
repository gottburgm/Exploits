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
package org.jboss.mx.util;

import org.jboss.mx.server.ObjectInputStreamWithClassLoader;

import java.io.*;

/**
 * SerializationHelper
 *
 * @author Jeff Haynie
 */
public class SerializationHelper
{
    /**
     * deserialize, using the current Thread Context classloader
     *
     * @param byteArray
     * @return deserialized object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object deserialize(byte[] byteArray)
            throws IOException, ClassNotFoundException
    {
        return deserialize(byteArray,Thread.currentThread().getContextClassLoader());
    }
    /**
     * deserialize an object using a specific ClassLoader
     *
     * @param byteArray
     * @param cl
     * @return deserialized object
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Object deserialize(byte[] byteArray, ClassLoader cl)
            throws IOException, ClassNotFoundException
    {
        if (byteArray == null)
        {
            return null;
        }
        if (byteArray.length == 0)
        {
            return null;
        }
        try
        {
            if (cl==null)
            {
                // use system loader
                cl = SerializationHelper.class.getClassLoader();
            }
            ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(byteArray);
            ObjectInputStream objectinputstream = new ObjectInputStreamWithClassLoader(bytearrayinputstream,cl);
            Object obj = objectinputstream.readObject();
            return obj;
        }
        catch (OptionalDataException optionaldataexception)
        {
            throw new IOException(optionaldataexception.getMessage());
        }
    }

    /**
     * serialize an object
     *
     * @param obj
     * @return serialized object
     * @throws java.io.IOException
     */
    public static byte[] serialize(Object obj)
            throws IOException
    {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        ObjectOutputStream objectoutputstream = new ObjectOutputStream(bytearrayoutputstream);
        objectoutputstream.writeObject(obj);
        return bytearrayoutputstream.toByteArray();
    }
}
