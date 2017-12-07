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
package org.jboss.test.aop.jdk15annotated;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 85945 $
 */
public class ExternalizableMixin implements Externalizable
{
   public static boolean read = false;
   public static boolean write = false;

   NoInterfacesPOJO pojo;
   
   public ExternalizableMixin(NoInterfacesPOJO pojo)
   {
      System.out.println("ExternalizableMixin constructor");
      this.pojo = pojo;
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      System.out.println("ExternalizableMixin readExternal");
      read = true;
      pojo.stuff = in.readUTF();  
   }
   
   public void writeExternal(ObjectOutput out) throws IOException
   {
      System.out.println("ExternalizableMixin writeExternal");
      write = true;
      out.writeUTF(pojo.stuff);
   }
   
   
}
