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
package org.jboss.embedded.test;

import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployer;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployment;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SingletonSchemaResolverFactory;
import org.jboss.embedded.BeanCompiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.lang.reflect.Field;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class BeanCompilerTester
{
   protected static Kernel createKernel()
   {
      BasicBootstrap bootstrap1 = new BasicBootstrap();
      bootstrap1.run();
      return bootstrap1.getKernel();
   }

   protected static void deploy(KernelDeployment deployment) throws Throwable 
   {
      AbstractKernelDeployer deployer = new AbstractKernelDeployer(createKernel());
      deployer.deploy(deployment);
      deployer.validate();

   }

   
   public static void main(String[] args) throws Throwable
   {
      System.out.println("compiling: " + args[0]);
      File fp = new File(args[0]);

      KernelDeployment deployment = parse(fp);

      long start = System.currentTimeMillis();

      FileOutputStream fos = new FileOutputStream(args[0] +".bin");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(deployment);
      oos.close();
      fos.close();

      FileInputStream fis = new FileInputStream(args[0] +".bin");
      ObjectInputStream ois = new ObjectInputStream(fis);
      KernelDeployment deployment2 = (KernelDeployment)ois.readObject();
      ois.close();
      fis.close();

      long end = System.currentTimeMillis() - start;

      System.out.println("object loading took: " + end);

      System.out.println("deployment1");
      deploy(deployment);
      System.out.println("deployment2");
      deploy(deployment2);



   }

   public static KernelDeployment parse(File fp)
           throws JBossXBException, MalformedURLException
   {
      SchemaBindingResolver resolver = SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();
      long start = System.currentTimeMillis();
      UnmarshallerFactory factory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = factory.newUnmarshaller();
      KernelDeployment deployment = (KernelDeployment) unmarshaller.unmarshal(fp.toURL().toString(), resolver);
      long end = System.currentTimeMillis() - start;
      System.out.println("XML parsing took: " + end);
      return deployment;
   }
}
