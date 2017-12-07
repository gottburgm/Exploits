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
import java.net.URI;
import java.net.URL;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployer;
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
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class BinaryBootstrap extends Bootstrap
{
   private static Bootstrap instance;

   public BinaryBootstrap(Kernel kernel)
   {
      super(kernel);
   }

   /**
    * For those applications that need a singelton Bootstrap instance
    *
    * @return the bootstrap
    */
   public static synchronized Bootstrap getInstance()
   {
      if (instance == null)
         instance = new BinaryBootstrap(createKernel());

      return instance;
   }

   public static KernelDeployment parse(URL url) throws JBossXBException, MalformedURLException
   {
      SchemaBindingResolver resolver = SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();
      UnmarshallerFactory factory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = factory.newUnmarshaller();
      return (KernelDeployment) unmarshaller.unmarshal(url.toString(), resolver);
   }

   public static void store(KernelDeployment deployment, File binFile) throws Exception
   {
      FileOutputStream fos = new FileOutputStream(binFile);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      try
      {
         oos.writeObject(deployment);
      }
      finally
      {
         oos.close();
         fos.close();
      }
   }

   public static KernelDeployment load(File binFile) throws Exception
   {
      FileInputStream fis = new FileInputStream(binFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      try
      {
         return (KernelDeployment)ois.readObject();
      }
      finally
      {
         ois.close();
         fis.close();
      }
   }

   @Override
   protected void deployBaseBootstrapUrl(URL url) throws DeploymentException
   {
      try
      {
         URI uri = url.toURI();
         URI binUri = new URI(uri.toString() + ".bin");
         File textFile = new File(uri);
         File binFile = new File(binUri);
         KernelDeployment deployment;

         if (!binFile.exists() && !textFile.exists())
         {
            throw new DeploymentException("Unable to locate bootstrap file: " + url);
         }
         else if (binFile.exists() && textFile.exists())
         {
            if (textFile.lastModified() > binFile.lastModified())
            {
               System.out.println("new bootstrap file...");
               deployment = parse(url);
               store(deployment, binFile);
            }
            else
            {
               deployment = load(binFile);
            }
         }
         else if (textFile.exists())
         {
            deployment = parse(url);
            store(deployment, binFile);
         }
         else
         {
            deployment = load(binFile);
         }

         AbstractKernelDeployer deployer = new AbstractKernelDeployer(kernel);
         deployer.deploy(deployment);
      }
      catch (Throwable throwable)
      {
         throw new RuntimeException("Unable to bootstrap: ", throwable);
      }
   }
}
