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
package org.jboss.mx.capability;

/**
 * <description> 
 *
 * @see <related>
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81026 $
 *   
 */
public class DispatchClassLoader extends ClassLoader
{

   // Attributes ----------------------------------------------------
   private String name = null;
   private byte[] code = null;
   
   // Constructors --------------------------------------------------
   DispatchClassLoader(ClassLoader parent, String name, byte[] bytecode)
   {
      super(parent);
      
      this.name = name;
      this.code = bytecode;
   }
   
   DispatchClassLoader(String name, byte[] bytecode)
   {
      super();
      
      this.name = name;
      this.code = bytecode;            
   }
   
   // Protected -----------------------------------------------------
   protected Class findClass(String name) throws ClassNotFoundException
   {
      if (!name.equals(this.name))
         throw new ClassNotFoundException("Class not found: " + name + "(I'm a dispatch loader, I only know " + this.name + ")");
         
      return defineClass(name, code, 0, code.length);

   }
}
      



