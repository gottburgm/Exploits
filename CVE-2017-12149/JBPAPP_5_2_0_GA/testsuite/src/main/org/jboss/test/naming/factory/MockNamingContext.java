/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.naming.factory;

import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class MockNamingContext
   implements Context
{
   private Hashtable env;
   private HashMap bindings = new HashMap();

   MockNamingContext(Hashtable env)
   {
      this.env = env;
   }

   public Object addToEnvironment(String name, Object value) throws NamingException
   {
      return env.put(name, value);
   }

   public void bind(Name name, Object obj) throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public void bind(String name, Object obj) throws NamingException
   {
      bindings.put(name, obj);
   }

   public void close() throws NamingException
   {      
   }

   public Name composeName(Name name, Name prefix) throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public String composeName(String name, String prefix) throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public Context createSubcontext(Name name) throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public Context createSubcontext(String name) throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public void destroySubcontext(Name name) throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public void destroySubcontext(String name) throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public Hashtable getEnvironment() throws NamingException
   {
      return env;
   }

   public String getNameInNamespace() throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public NameParser getNameParser(Name name) throws NamingException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public NameParser getNameParser(String name) throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public NamingEnumeration list(Name name) throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public NamingEnumeration list(String name) throws NamingException
   {
      return new MockNamingEnumeration(bindings.values().iterator());
   }

   public NamingEnumeration listBindings(Name name) throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public NamingEnumeration listBindings(String name) throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public Object lookup(Name name) throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public Object lookup(String name) throws NamingException
   {
      Object value = bindings.get(name);
      if( value == null )
         throw new NameNotFoundException(name);
      return value;
   }

   public Object lookupLink(Name name) throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public Object lookupLink(String name) throws NamingException
   {
      throw new NamingException("Unsupported op");
   }

   public void rebind(Name name, Object obj) throws NamingException
   {
      throw new NamingException("Unsupported op");      
   }

   public void rebind(String name, Object obj) throws NamingException
   {
      bindings.put(name, obj);
   }

   public Object removeFromEnvironment(String name) throws NamingException
   {
      return env.remove(name);
   }

   public void rename(Name oldName, Name newName) throws NamingException
   {
      throw new NamingException("Unsupported op");      
   }

   public void rename(String oldName, String newName) throws NamingException
   {
      Object value = bindings.remove(oldName);
      if( value == null )
         throw new NameNotFoundException(oldName);
      bindings.put(newName, value);
   }

   public void unbind(Name name) throws NamingException
   {
      throw new NamingException("Unsupported op");      
   }

   public void unbind(String name) throws NamingException
   {
      if( bindings.containsKey(name) == false )
         throw new NameNotFoundException(name);
      bindings.remove(name);
   }

}
