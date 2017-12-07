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
package org.jboss.resource.adapter.jdbc.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

/**
 * @author <a href="mailto:telrod@e2technologies.net">Tom Elrod</a>
 * @version $Revision: 71554 $
 */
public class SerializableReader extends Reader implements Serializable
{
   /** @since 1.1 */
   static final long serialVersionUID = 1244952470388397765L;
   private char[] data = null;

   protected char buf[];
   protected int pos;
   protected int mark = 0;
   protected int count;

   public SerializableReader(Reader reader) throws IOException
   {
      BufferedReader in = new BufferedReader(reader);
      String line = in.readLine();
      while (line != null)
      {
         String current = (data == null) ? "" : new String(data);
         String newData = current + line;
         data = newData.toCharArray();
         line = in.readLine();
      }

      reader.close();

      this.buf = this.data;
      this.pos = 0;
      this.count = this.buf.length;

   }

   /**
    * Close the stream.  Once a stream has been closed, further read(),
    * ready(), mark(), or reset() invocations will throw an IOException.
    * Closing a previously-closed stream, however, has no effect.
    *
    * @throws java.io.IOException If an I/O error occurs
    */
   public void close() throws IOException
   {
      // Do nothing
   }

   /**
    * Read characters into a portion of an array.  This method will block
    * until some input is available, an I/O error occurs, or the end of the
    * stream is reached.
    *
    * @param cbuf Destination buffer
    * @param off  Offset at which to start storing characters
    * @param len  Maximum number of characters to read
    * @return The number of characters read, or -1 if the end of the
    *         stream has been reached
    * @throws java.io.IOException If an I/O error occurs
    */
   public int read(char cbuf[], int off, int len) throws IOException
   {
      if (cbuf == null)
      {
         throw new NullPointerException();
      }
      else if ((off < 0) || (off > cbuf.length) || (len < 0) ||
            ((off + len) > cbuf.length) || ((off + len) < 0))
      {
         throw new IndexOutOfBoundsException();
      }

      if (pos >= count)
      {
         return -1;
      }
      if (pos + len > count)
      {
         len = count - pos;
      }
      if (len <= 0)
      {
         return 0;
      }
      System.arraycopy(buf, pos, cbuf, off, len);
      pos += len;
      return len;
   }

}
