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
package org.jboss.test.security.ejb.project.support;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.spi.DirectoryManager;

/** An implementation of NamingEnumeration for listing the Bindings
 in a context. It accepts an Iterator of DirBindings and transforms
 the raw object and attributes into the output object using the
 DirectoryManager.getObjectInstance method.

@see DirBinding
@see DirectoryManager.getObjectInstance(Object,Name,Context,Hashtable,Attributes)

@author Scott_Stark@displayscape.com
@version $Id: NameBindingIterator.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
*/
public class NameBindingIterator implements NamingEnumeration
{
	private Iterator bindings;
	private DirContext context;

	/** Creates new NameBindingIterator for enumerating a list of Bindings.
	 *@param names, an Iterator of DirBindings for the raw context bindings.
	 * This is the name and raw object data/attributes that should be input into 
	 * DirectoryManager.getObjectInstance().
	 */
    public NameBindingIterator(Iterator bindings, DirContext context)
	{
		this.bindings = bindings;
		this.context = context;
    }

	public void close() throws NamingException
	{
	}

	public boolean hasMore() throws NamingException
	{
		return bindings.hasNext();
	}

	public Object next() throws NamingException
	{
		DirBinding binding = (DirBinding) bindings.next();
		Object rawObject = binding.getObject();
		Name name = new DefaultName(binding.getName());
		Hashtable env = context.getEnvironment();
		try
		{
			Object instanceObject = DirectoryManager.getObjectInstance(rawObject,
				name, context, env, binding.getAttributes());
			binding.setObject(instanceObject);
		}
		catch(Exception e)
		{
			NamingException ne = new NamingException("getObjectInstance failed");
			ne.setRootCause(e);
			throw ne;
		}
		return binding;
	}

	public boolean hasMoreElements()
	{
		boolean hasMore = false;
		try
		{
			hasMore = hasMore();
		}
		catch(NamingException e)
		{
		}
		return hasMore;
	}
	
	public Object nextElement()
	{
		Object next = null;
		try
		{
			next = next();
		}
		catch(NamingException e)
		{
			throw new NoSuchElementException(e.toString());
		}
		return next;
	}	
}
