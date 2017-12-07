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
package org.jboss.embedded;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;

import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SingletonSchemaResolverFactory;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class   BeanCompiler
{
   public static void main(String[] args) throws Exception
   {
      for (String arg : args)
      {
         compile(arg);
      }
   }

   public static void compile(String arg) throws Exception
   {
      System.out.println("compiling: " + arg);
      File fp = new File(arg);
      KernelDeployment deployment = parse(fp);

      long start = System.currentTimeMillis();

      FileOutputStream fos = new FileOutputStream(arg + ".bin");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(deployment);
      oos.close();
      fos.close();

      FileInputStream fis = new FileInputStream(arg + ".bin");
      ObjectInputStream ois = new ObjectInputStream(fis);
      deployment = (KernelDeployment)ois.readObject();
      ois.close();
      fis.close();

      long end = System.currentTimeMillis() - start;

      System.out.println("object loading took: " + end);
   }

   public static KernelDeployment parse(File fp)
           throws JBossXBException, MalformedURLException
   {
      long start = System.currentTimeMillis();
      SchemaBindingResolver resolver = SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();
      UnmarshallerFactory factory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = factory.newUnmarshaller();
      KernelDeployment deployment = (KernelDeployment) unmarshaller.unmarshal(fp.toURL().toString(), resolver);
      long end = System.currentTimeMillis() - start;
      System.out.println("XML parsing took: " + end);
      return deployment;
   }
}
