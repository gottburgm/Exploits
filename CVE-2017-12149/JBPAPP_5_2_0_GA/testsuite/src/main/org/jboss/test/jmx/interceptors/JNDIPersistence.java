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
package org.jboss.test.jmx.interceptors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;

import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.jboss.mx.interceptor.AbstractInterceptor;
import org.jboss.mx.interceptor.Interceptor;
import org.jboss.mx.server.Invocation;
import org.jboss.logging.Logger;

/** A simple file based persistence interceptor that saves the value of
 * Naming.bind() calls as serialized objects.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public final class JNDIPersistence
   extends AbstractInterceptor
{
   private static Logger log = Logger.getLogger(JNDIPersistence.class);

   private File storeDirectory;

   public File getStoreDirectory()
   {
      return storeDirectory;
   }
   public void setStoreDirectory(File storeDirectory)
   {
      log.info("setStoreDirectory: "+storeDirectory);
      if( storeDirectory.exists() == false )
         storeDirectory.mkdir();
      this.storeDirectory = storeDirectory;
   }

   // Interceptor overrides -----------------------------------------
   public Object invoke(Invocation invocation) throws Throwable
   {
      String opName = invocation.getName();
      log.info("invoke, opName="+opName);

      // If this is not the invoke(Invocation) op just pass it along
      if( opName == null || opName.equals("invoke") == false )
      {
         Interceptor i = invocation.nextInterceptor();
         return i.invoke(invocation);
      }
      
      Object[] args = invocation.getArgs();
      org.jboss.invocation.Invocation invokeInfo =
         (org.jboss.invocation.Invocation) args[0];

      Object[] iargs = invokeInfo.getArguments();
      for(int a = 0; a < args.length; a ++)
         log.info("  args["+a+"]="+iargs[a]);
      Method method = invokeInfo.getMethod();
      String methodName = method.getName();
      log.info("methodName: "+methodName);
      Object value = null;
      if( methodName.equals("bind") )
      {
         log.info("Dispatching bind");
         invocation.nextInterceptor().invoke(invocation);
         // Bind succeeded, save the value
         log.info("Saving bind data");
         Name name = (Name) iargs[0];
         Object data = iargs[1];
         try
         {
            writeBinding(name, data);
         }
         catch(Throwable e)
         {
            log.error("Failed to write binding", e);
            throw e;
         }
      }
      else if( methodName.equals("lookup") )
      {
         log.info("Dispatching lookup");
         try
         {
            value = invocation.nextInterceptor().invoke(invocation);
            log.info("lookup returned: "+value);
         }
         catch(Throwable ex)
         {
            ex = getException(ex);
            log.info("InvocationException: ", ex);
            if( ex instanceof NameNotFoundException )
            {
               log.info("NameNotFoundException in lookup, finding data");
               Name name = (Name) iargs[0];
               try
               {
                  value = readBinding(name);
                  if( value == null )
                     throw ex;
               }
               catch(Throwable e2)
               {
                  log.error("Failed to read binding", e2);
                  throw e2;
               }
            }
         }
      }
      else
      {
         value = invocation.nextInterceptor().invoke(invocation);
      }

      return value;
   }

   private void writeBinding(Name name, Object data)
      throws IOException
   {
      File dataFile = new File(storeDirectory, name.toString());
      FileOutputStream fos = new FileOutputStream(dataFile);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(data);
      oos.close();
      fos.close();
      log.info("Wrote data binding to: "+dataFile);
   }

   private Object readBinding(Name name)
      throws IOException, ClassNotFoundException
   {
      File dataFile = new File(storeDirectory, name.toString());
      if( dataFile.exists() == false )
         return null;

      FileInputStream fis = new FileInputStream(dataFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      Object data = ois.readObject();
      ois.close();
      fis.close();
      log.info("Read data binding from: "+dataFile);
      return data;
   }

   /** Unwrap the InvocationException to see what the Naming service
    * exception really was.
    *
    * @param ex the wrapped InvocationException
    * @return the underlying initial exception
    */
   Throwable getException(Throwable ex)
   {
      if( ex instanceof MBeanException )
      {
         MBeanException mbe = (MBeanException) ex;
         ex = mbe.getTargetException();
      }
      else if( ex instanceof ReflectionException )
      {
         ReflectionException re = (ReflectionException) ex;
         ex = re.getTargetException();
      }
      return ex;
   }
}
