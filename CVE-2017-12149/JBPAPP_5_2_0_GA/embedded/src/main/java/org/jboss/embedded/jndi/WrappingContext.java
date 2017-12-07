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
package org.jboss.embedded.jndi;

import java.util.Hashtable;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class WrappingContext implements Context
{
   private Context namingContext;

   public WrappingContext(Context namingContext)
   {
      this.namingContext = namingContext;
   }

   public Object lookup(Name name)
           throws NamingException
   {
      return getNamingContext().lookup(name);
   }

   public Object lookup(String name)
           throws NamingException
   {
      return getNamingContext().lookup(name);
   }

   public void bind(Name name, Object obj)
           throws NamingException
   {
      getNamingContext().bind(name, obj);
   }

   public void bind(String name, Object obj)
           throws NamingException
   {
      getNamingContext().bind(name, obj);
   }

   public void rebind(Name name, Object obj)
           throws NamingException
   {
      getNamingContext().rebind(name, obj);
   }

   public void rebind(String name, Object obj)
           throws NamingException
   {
      getNamingContext().rebind(name, obj);
   }

   public void unbind(Name name)
           throws NamingException
   {
      getNamingContext().unbind(name);
   }

   public void unbind(String name)
           throws NamingException
   {
      getNamingContext().unbind(name);
   }

   public void rename(Name oldName, Name newName)
           throws NamingException
   {
      getNamingContext().rename(oldName, newName);
   }

   public void rename(String oldName, String newName)
           throws NamingException
   {
      getNamingContext().rename(oldName, newName);
   }

   public NamingEnumeration<NameClassPair> list(Name name)
           throws NamingException
   {
      return getNamingContext().list(name);
   }

   public NamingEnumeration<NameClassPair> list(String name)
           throws NamingException
   {
      return getNamingContext().list(name);
   }

   public NamingEnumeration<Binding> listBindings(Name name)
           throws NamingException
   {
      return getNamingContext().listBindings(name);
   }

   public NamingEnumeration<Binding> listBindings(String name)
           throws NamingException
   {
      return getNamingContext().listBindings(name);
   }

   public void destroySubcontext(Name name)
           throws NamingException
   {
      getNamingContext().destroySubcontext(name);
   }

   public void destroySubcontext(String name)
           throws NamingException
   {
      getNamingContext().destroySubcontext(name);
   }

   public Context createSubcontext(Name name)
           throws NamingException
   {
      return getNamingContext().createSubcontext(name);
   }

   public Context createSubcontext(String name)
           throws NamingException
   {
      return getNamingContext().createSubcontext(name);
   }

   public Object lookupLink(Name name)
           throws NamingException
   {
      return getNamingContext().lookupLink(name);
   }

   public Object lookupLink(String name)
           throws NamingException
   {
      return getNamingContext().lookupLink(name);
   }

   public NameParser getNameParser(Name name)
           throws NamingException
   {
      return getNamingContext().getNameParser(name);
   }

   public NameParser getNameParser(String name)
           throws NamingException
   {
      return getNamingContext().getNameParser(name);
   }

   public Name composeName(Name name, Name prefix)
           throws NamingException
   {
      return getNamingContext().composeName(name, prefix);
   }

   public String composeName(String name, String prefix)
           throws NamingException
   {
      return getNamingContext().composeName(name, prefix);
   }

   public Object addToEnvironment(String propName, Object propVal)
           throws NamingException
   {
      return getNamingContext().addToEnvironment(propName, propVal);
   }

   public Object removeFromEnvironment(String propName)
           throws NamingException
   {
      return getNamingContext().removeFromEnvironment(propName);
   }

   public Hashtable<?, ?> getEnvironment()
           throws NamingException
   {
      return getNamingContext().getEnvironment();
   }

   public void close()
           throws NamingException
   {
      getNamingContext().close();
   }

   public String getNameInNamespace()
           throws NamingException
   {
      return getNamingContext().getNameInNamespace();
   }

   protected Context getNamingContext()
   {
      return namingContext;
   }
}
