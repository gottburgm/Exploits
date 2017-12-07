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
package org.jboss.test.jrmp.ejb;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** A custom socket that uses the GZIPInputStream and GZIPOutputStream streams
for compression.

@see java.net.ServerSocket
@see java.util.zip.GZIPInputStream
@see java.util.zip.GZIPOutputStream

@author  Scott_Stark@displayscape.com
@version $Revision: 81036 $
*/
class CompressionSocket extends Socket
{

    /* InputStream used by socket */
    private InputStream in;
    /* OutputStream used by socket */
    private OutputStream out;

    /* 
     * No-arg constructor for class CompressionSocket  
     */
    public CompressionSocket()
    {
        super();
    }

    /* 
     * Constructor for class CompressionSocket 
     */
    public CompressionSocket(String host, int port) throws IOException 
    {
        super(host, port);
    }

    /* 
     * Returns a stream of type CompressionInputStream 
     */
    public InputStream getInputStream() throws IOException 
    {
        if (in == null)
        {
            in = new CompressionInputStream(super.getInputStream());
        }
        return in;
    }

    /* 
     * Returns a stream of type CompressionOutputStream 
     */
    public OutputStream getOutputStream() throws IOException 
    {
        if (out == null)
        {
            out = new CompressionOutputStream(super.getOutputStream());
        }
        return out;
    }

    /*
     * Flush the CompressionOutputStream before 
     * closing the socket.
     */
    public synchronized void close() throws IOException
    {
        OutputStream o = getOutputStream();
        o.flush();
	super.close();
    }
}
