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
package org.jboss.iiop;

import org.jboss.logging.Logger;
import org.jboss.proxy.compiler.IIOPStubCompiler;

/**
 * This class loader dynamically generates and loads client stub classes.
 * It is intended to be used by clients, as an interim solution.
 * Should not be necessary when the IORs contain a JAVA_CODE_BASE tag.
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 */
public class StubClassLoader 
      extends ClassLoader
{

   // Static ------------------------------------------------------------------

   private static final Logger logger = 
                          Logger.getLogger(StubClassLoader.class);

   // Constructor -------------------------------------------------------------
   
   public StubClassLoader(ClassLoader parent)
   {
      super(parent);
   }

   // Protected ---------------------------------------------------------------

   protected  Class findClass(String name) 
         throws ClassNotFoundException 
   {
      logger.debug("findClass(" + name + ") called");
      if (name.endsWith("_Stub")) {
         int start = name.lastIndexOf('.') + 1;
         if (name.charAt(start) == '_') {
            String pkg = name.substring(0, start);
            String interfaceName = pkg + name.substring(start + 1, 
                                                        name.length() - 5);
            logger.debug("interface name " + interfaceName);
            Class intf = loadClass(interfaceName);
            logger.debug("loaded class " + interfaceName);

            try {
               byte[] code = IIOPStubCompiler.compile(intf, name);
               
               logger.debug("compiled stub class for " + interfaceName);
               Class clz = defineClass(name, code, 0, code.length);
               logger.debug("defined stub class for " + interfaceName);
               resolveClass(clz);
               logger.debug("resolved stub class for " + interfaceName);
               return clz;
            }
            catch (RuntimeException e) {
               logger.debug("Exception generating IIOP stub " + name, e);
               throw e;
            }
         }
      }
      throw new ClassNotFoundException(name);
   }

}
