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
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NameParser;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;

/** An abstract implementation of DirContext that simply takes every DirContext
method that accepts the String form of a Name and invokes the corresponding
method that accecpts a Name.

@author Scott_Stark@displayscape.com
@version $Id: DirContextStringImpl.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
*/
public abstract class DirContextStringImpl implements DirContext
{
	private NameParser nameParser;

	/** Creates new DirContextStringImpl */
    public DirContextStringImpl(NameParser nameParser)
	{
		this.nameParser = nameParser;
    }
    public DirContextStringImpl()
	{
		this(DefaultName.getNameParser());
    }

// --- Begin DirContext interface methods that accept a String name
	public void bind(java.lang.String name, Object obj) throws NamingException
	{
		bind(nameParser.parse(name), obj);
	}
	
	public String composeName(String name,String name1) throws NamingException
	{
		return null;
	}
	
	public Context createSubcontext(java.lang.String name) throws NamingException
	{
		return createSubcontext(nameParser.parse(name));
	}
	
	public void destroySubcontext(java.lang.String name) throws NamingException
	{
		destroySubcontext(nameParser.parse(name));
	}
	
	public NameParser getNameParser(java.lang.String name) throws NamingException
	{
		return getNameParser(nameParser.parse(name));
	}
	
	public NamingEnumeration list(java.lang.String name) throws NamingException
	{
		return list(nameParser.parse(name));
	}
	
	public NamingEnumeration listBindings(java.lang.String name) throws NamingException
	{
		return listBindings(nameParser.parse(name));
	}
	
	public java.lang.Object lookup(String name) throws NamingException
	{
		return lookup(nameParser.parse(name));
	}
	public java.lang.Object lookupLink(String name) throws NamingException
	{
		return lookupLink(nameParser.parse(name));
	}

	public void rebind(String name,Object obj) throws NamingException
	{
		rebind(nameParser.parse(name), obj);
	}
	
	public void rename(String name,String name1) throws NamingException
	{
		rename(nameParser.parse(name), nameParser.parse(name1));
	}

	public void unbind(String name) throws NamingException
	{
		unbind(nameParser.parse(name));
	}

	public void bind(String name,Object obj, Attributes attributes) throws NamingException
	{
		bind(nameParser.parse(name), obj, attributes);
	}
	
	public DirContext createSubcontext(String name, Attributes attributes) throws NamingException
	{
		return createSubcontext(nameParser.parse(name), attributes);
	}

	public Attributes getAttributes(String name) throws NamingException
	{
		return getAttributes(nameParser.parse(name));
	}

	public Attributes getAttributes(String name,String[] attrNames) throws NamingException
	{
		return getAttributes(nameParser.parse(name), attrNames);
	}
	
	public DirContext getSchema(String name) throws NamingException
	{
		return getSchema(nameParser.parse(name));
	}
	
	public DirContext getSchemaClassDefinition(String name) throws NamingException
	{
		return getSchemaClassDefinition(nameParser.parse(name));
	}

	public void modifyAttributes(String name, ModificationItem[] modificationItem) throws NamingException
	{
		modifyAttributes(nameParser.parse(name), modificationItem);
	}

	public void modifyAttributes(String name,int index, Attributes attributes) throws NamingException
	{
		modifyAttributes(nameParser.parse(name), index, attributes);
	}

	public void rebind(String name,Object obj, Attributes attributes) throws NamingException
	{
		rebind(nameParser.parse(name), obj, attributes);
	}

	public NamingEnumeration search(String name, Attributes attributes) throws NamingException
	{
		return search(nameParser.parse(name), attributes);
	}

	public NamingEnumeration search(String name,String name1, SearchControls searchControls) throws NamingException
	{
		return search(nameParser.parse(name), name1, searchControls);
	}
	
	public  NamingEnumeration search(String name, Attributes attributes, String[] str2) throws NamingException
	{
		return search(nameParser.parse(name), attributes, str2);
	}
	
	public NamingEnumeration search(String name,String name1, Object[] obj, SearchControls searchControls) throws NamingException {
		return null;
	}
	
// --- End DirContext interface methods

}
