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
package org.jboss.util.stream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

/**
 * Customized object output stream that redefines 
 * <code>writeClassDescriptor()</code> in order to write a short class 
 * descriptor (just the class name) when serializing an object.
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 */
public class CustomObjectOutputStream
      extends ObjectOutputStream 
{
   
   /**
    * Constructs a new instance with the given output stream.
    *
    * @param out     stream to write objects to
    */
   public CustomObjectOutputStream(OutputStream out)
      throws IOException 
   {
      super(out);
   }
   
   /**
    * Writes just the class name to this output stream.
    *
    * @param classdesc class description object
    */
   protected void writeClassDescriptor(ObjectStreamClass classdesc)
      throws IOException 
   {
      writeUTF(classdesc.getName());
   }
   
}
