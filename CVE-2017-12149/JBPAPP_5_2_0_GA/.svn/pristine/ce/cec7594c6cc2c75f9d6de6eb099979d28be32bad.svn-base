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
package org.jboss.test.bmp.test;

import java.util.Iterator;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.bmp.interfaces.BMPHelperSession;
import org.jboss.test.bmp.interfaces.BMPHelperSessionHome;
import org.jboss.test.bmp.interfaces.SimpleBMP;
import org.jboss.test.bmp.interfaces.SimpleBMPHome;

public class SmallCacheBmpUnitTestCase
    extends JBossTestCase
{
    public SmallCacheBmpUnitTestCase(String name)
    {
        super(name);
    }

    public void testBMP() throws Exception
   {
      BMPHelperSessionHome sessionHome = (BMPHelperSessionHome)new InitialContext ().lookup ("bmp.BMPHelperSession");
      BMPHelperSession session = sessionHome.create ();
      
      getLog().debug ("looking up table:");
      boolean exists =  session.existsSimpleBeanTable ();
      if (exists)
      {
         getLog().debug ("table exists.");
         getLog().debug ("delete it...");
         session.dropSimpleBeanTable();
         getLog().debug ("done.");
      }
      
      getLog().debug ("table does not exist.");
      getLog().debug ("create it...");
      session.createSimpleBeanTable();
      try
      {
         getLog().debug ("done.");
         
         getLog().debug ("start playing with bmp beans.");
         SimpleBMPHome home = (SimpleBMPHome)new InitialContext ().lookup ("bmp.SmallCacheBMP");
         SimpleBMP[] beans = new SimpleBMP[10];
         for (int i = 0; i < 10; ++i)
         {
            getLog().debug ("create bean " + i);
            beans[i] = home.create (i, "Bean " + i);
            getLog().debug ("getName (): "+ beans[i].getName ());
         }

         Thread.sleep(10000);
         
         for (int i = 0; i < 10; ++i)
            assertEquals(beans[i].getPrimaryKey(), beans[i].getIdViaEJBObject());

         getLog().debug ("removing all beans:");
         Iterator it = home.findAll ().iterator ();
         while (it.hasNext ())
            ((SimpleBMP)it.next ()).remove ();
      }
      finally
      {
         getLog().debug ("table exists.");
         getLog().debug ("delete it...");
         session.dropSimpleBeanTable();
         getLog().debug ("done.");
      }
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(SmallCacheBmpUnitTestCase.class, "bmp.jar");
   }
}
