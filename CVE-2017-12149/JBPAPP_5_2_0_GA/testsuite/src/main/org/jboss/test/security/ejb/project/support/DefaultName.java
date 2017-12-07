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

import java.util.Enumeration;
import java.util.Properties;
import javax.naming.CompoundName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;

/** A simple subclass of CompoundName that fixes the name syntax to:
	jndi.syntax.direction = left_to_right
	jndi.syntax.separator = "/"

@author Scott_Stark@displayscape.com
@version $Revision: 81036 $
*/
public class DefaultName extends CompoundName
{
	/** The Properties used for the project directory heirarchical names */
	static Name emptyName;
	static Properties nameSyntax = new Properties();
	static
	{
		nameSyntax.put("jndi.syntax.direction", "left_to_right");
		nameSyntax.put("jndi.syntax.separator", "/");
		try
		{
			emptyName = new DefaultName("");
		}
		catch(InvalidNameException e)
		{
		}	
	}

    private static class DefaultNameParser implements NameParser
    {
        public Name parse(String path) throws NamingException
        {
            DefaultName name = new DefaultName(path);
            return name;
        }
    }

    public static NameParser getNameParser()
    {
        return new DefaultNameParser();
    }

	/** Creates new DefaultName */
    public DefaultName(Enumeration comps)
	{
		super(comps, nameSyntax);
    }
    public DefaultName(String name) throws InvalidNameException
	{
		super(name, nameSyntax);
    }
    public DefaultName(Name name)
	{
		super(name.getAll(), nameSyntax);
    }
    public DefaultName()
	{
		this(emptyName);
    }

}
