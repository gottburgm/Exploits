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
package org.jboss.mx.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.management.MBeanInfo;

import org.jboss.logging.Logger;

import java.io.FileNotFoundException;

/**
 * MBean Info Object Database. <p>
 *
 * @author    Matt Munz
 */
public class MBeanInfoOdb
{
   protected Logger fLogger;

   // Constructors --------------------------------------------------
   
   public MBeanInfoOdb()
   {
      super();
   }

   // Protected -----------------------------------------------------
   
   protected void store(MBeanInfo metadata, File location) throws IOException
   {
       location.createNewFile();
       FileOutputStream fos = new FileOutputStream(location);
       ObjectOutputStream oos = new ObjectOutputStream(fos);
       oos.writeObject(metadata);
   }
   
   protected MBeanInfo load(File location) 
     throws IOException, FileNotFoundException, ClassNotFoundException
   {
       logger().info("Loading mbean info from location: " + location.getAbsolutePath());
       FileInputStream fis = new FileInputStream(location);
       ObjectInputStream ois = new ObjectInputStream(fis);
       MBeanInfo obj = (MBeanInfo) ois.readObject();
       ois.close();
       return obj;
   }

   protected Logger logger()
   {
      if (fLogger == null)
      {
         fLogger = Logger.getLogger("" + getClass().getName());
      }
      return fLogger;
   }
}

