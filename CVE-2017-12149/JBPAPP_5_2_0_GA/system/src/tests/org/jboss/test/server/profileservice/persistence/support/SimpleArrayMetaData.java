/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.server.profileservice.persistence.support;

import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementProperty;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
@ManagementObject
public class SimpleArrayMetaData
{
   /** one dimension */
   char[] test1D;
   
   /** two dimensions */
   char[][] test2D;
   
   /** three dimensions */
   char[][][] test3D;

   @ManagementProperty
   public char[] getTest1D()
   {
      return test1D;
   }

   public void setTest1D(char[] test1D)
   {
      this.test1D = test1D;
   }

   @ManagementProperty
   public char[][] getTest2D()
   {
      return test2D;
   }

   public void setTest2D(char[][] test2D)
   {
      this.test2D = test2D;
   }

   @ManagementProperty
   public char[][][] getTest3D()
   {
      return test3D;
   }

   public void setTest3D(char[][][] test3D)
   {
      this.test3D = test3D;
   }
   
}
