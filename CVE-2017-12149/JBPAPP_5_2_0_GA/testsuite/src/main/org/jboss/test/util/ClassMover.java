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
package org.jboss.test.util;

import java.net.URL;
import java.io.File;

/** A utility class that moves a class file to a .bak file to effectively
 * remove it from the classpath using #move(String) and restores it using
 * #restore(File).
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class ClassMover
{
   public static File move(String className)
      throws ClassNotFoundException
   {
      File classFileBak = null;
      String resName = className.replace('.', '/');
      URL classRes = ClassMover.class.getResource("/" + resName + ".class");
      if (classRes != null)
      {
         System.out.println("Found "+className+" impl at: " + classRes);
         File classFile = new File(classRes.getFile());
         classFileBak = new File(classFile.getAbsolutePath() + ".bak");
         classFileBak.delete();
         boolean moved = classFile.renameTo(classFileBak);
         System.out.println("Moved to .bak: " + moved);
      }
      else
      {
         throw new ClassNotFoundException("No class file found: "+className);
      }
      return classFileBak;
   }

   public static void restore(File classFileBak)
   {
      if( classFileBak.exists() )
      {
         String name = classFileBak.getAbsolutePath();
         String origName = name.substring(0, name.length() - 4);
         File classFile = new File(origName);
         boolean restored = classFileBak.renameTo(classFile);
         System.out.println("Restored from .bak: " + restored);
      }
   }
}
