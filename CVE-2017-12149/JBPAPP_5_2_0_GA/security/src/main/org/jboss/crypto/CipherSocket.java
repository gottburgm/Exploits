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
package org.jboss.crypto;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.SecretKey;

/**
 *
 * @author  Scott.Stark@jboss.org
 */
public class CipherSocket extends Socket
{
   private Cipher cipher;
   private Socket delegate;
   String algorithm;
   SecretKey key;

   /** Creates a new instance of CipherSocket */
   public CipherSocket(String host, int port, String algorithm, SecretKey key)
      throws IOException
   {
      super(host, port);
      this.algorithm = algorithm;
      this.key = key;
   }
   public CipherSocket(Socket delegate, String algorithm, SecretKey key)
      throws IOException
   {
      this.delegate = delegate;
      this.algorithm = algorithm;
      this.key = key;
   }

   public InputStream getInputStream() throws IOException
   {
      InputStream is = delegate == null ? super.getInputStream() : delegate.getInputStream();
      Cipher cipher = null;
      try
      {
         cipher = Cipher.getInstance(algorithm);
         int size = cipher.getBlockSize();
         byte[] tmp = new byte[size];
         Arrays.fill(tmp, (byte)15);
         IvParameterSpec iv = new IvParameterSpec(tmp);
         cipher.init(Cipher.DECRYPT_MODE, key, iv);
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw new IOException("Failed to init cipher: "+e.getMessage());
      }
      CipherInputStream cis = new CipherInputStream(is, cipher);
      return cis;
   }

   public OutputStream getOutputStream() throws IOException
   {
      OutputStream os = delegate == null ? super.getOutputStream() : delegate.getOutputStream();
      Cipher cipher = null;
      try
      {
         cipher = Cipher.getInstance(algorithm);
         int size = cipher.getBlockSize();
         byte[] tmp = new byte[size];
         Arrays.fill(tmp, (byte)15);
         IvParameterSpec iv = new IvParameterSpec(tmp);
         cipher.init(Cipher.ENCRYPT_MODE, key, iv);
      }
      catch(Exception e)
      {
         throw new IOException("Failed to init cipher: "+e.getMessage());
      }
      CipherOutputStream cos = new CipherOutputStream(os, cipher);
      return cos;
   }
}
