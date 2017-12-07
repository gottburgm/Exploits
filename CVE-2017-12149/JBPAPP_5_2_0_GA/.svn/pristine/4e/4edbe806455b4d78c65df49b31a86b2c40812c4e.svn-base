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
package org.jboss.mx.server;


import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.IOException;

import java.lang.reflect.Proxy;

/**
 * This replaces the EjbossInputStream in the storage package.
 * The input stream will take a class loader in its constructor and look
 * into it to retrieve the class definitions.
 * It is used throughout the server to deserialize parameters and objects
 * whose definition are in a jar and not the global classpath
 * It also has better comments than the previous version.
 *
 * @author  <a href="rickard@dreambean.com">Rickard Oberg</a>
 * @since   Ejboss 0.9
 */


public class ObjectInputStreamWithClassLoader
    extends ObjectInputStream {

    /**
    * The classloader to use when the default classloader cannot find
    * the classes in the stream.
    */
    ClassLoader cl;

    
/******************************************************************************/
/******************************************************************************/
/*
/*   CONSTRUCTORS
/*
/******************************************************************************/
/******************************************************************************/

    /**
    * Construct a new instance with the given classloader and input stream.
    *
    * @param  ClassLoader      classloader to use
    * @param  InputStream      stream to read objects from
    */
    public ObjectInputStreamWithClassLoader(InputStream in, ClassLoader cl)
         throws IOException {

         super(in);

         this.cl = cl;
    }


/******************************************************************************/
/******************************************************************************/
/*
/*   OVERWRITING  <ObjectInputStream>
/*
/******************************************************************************/
/******************************************************************************/

    /**
    * Resolve the class described in the osc parameter. First, try the
    * default classloader (implemented by the super class). If it cannot
    * load the class, try the classloader given to this instance.
    *
    * @param  ObjectStreamClass     class description object
    * @return      the Class corresponding to class description
    * @exception   IOException     if an I/O error occurs
    * @exception   ClassNotFoundException  if the class cannot be found by the classloader
    */
    protected Class resolveClass(ObjectStreamClass osc)
        throws IOException, ClassNotFoundException {

        return cl.loadClass(osc.getName());
    }
    
    protected Class resolveProxyClass( String[] interfaces )
       	throws IOException, ClassNotFoundException {
		
		Class[] interfacesClass = new Class[interfaces.length];
		for( int i=0; i< interfaces.length; i++ )
		{
			interfacesClass[i] = Class.forName(interfaces[i], false, cl);
		}
		
    	return Proxy.getProxyClass(cl, interfacesClass);
    }
}




