/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.xop.doclit;

import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;

public class FakeInputStream extends InputStream
{
   private long size;

   public FakeInputStream(long size)
   {
      this.size = size;
   }

   public int read(byte[] b, int off, int len) throws IOException
   {
      if (len < 1)
         return 0;

      if (size == 0)
         return -1;

      int ret = (int)Math.min(size, len);
      Arrays.fill(b, off, off + ret, (byte)1);
      size -= ret;

      return ret;
   }

   public int read() throws IOException
   {
      if (size > 0)
      {
         size--;
         return 1;
      }

      return -1;
   }
}
