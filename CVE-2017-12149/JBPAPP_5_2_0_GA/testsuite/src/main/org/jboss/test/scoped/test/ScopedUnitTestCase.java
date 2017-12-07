/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.test.scoped.test;

import org.jboss.deployment.EARDeployerMBean;
import org.jboss.test.JBossTestCase;
import org.jboss.test.scoped.interfaces.a.SessionA;
import org.jboss.test.scoped.interfaces.a.SessionAHome;
import org.jboss.test.scoped.interfaces.dto.SimpleRequestDTO;
import org.jboss.test.scoped.interfaces.dto.SimpleResponseDTO;
import org.jboss.test.scoped.interfaces.b.SessionBHome;
import org.jboss.test.scoped.interfaces.b.SessionB;

import javax.management.Attribute;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.net.URL;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

import org.jboss.mx.loading.UnifiedLoaderRepository3;
import org.jboss.mx.loading.RepositoryClassLoader;
import org.jboss.test.util.ClassMover;
import org.apache.log4j.Logger;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.CtMethod;
import javassist.CtNewMethod;


/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class ScopedUnitTestCase extends JBossTestCase
{
   public ScopedUnitTestCase(String name)
   {
      super(name);
   }

   public void testScoped() throws Exception
   {
//      isolateDeployments(Boolean.TRUE);

      // scoped deployment A

      // have to create response classes with different serialVersionUID for each deployment
      String jbosstestDeployDir = System.getProperty("jbosstest.deploy.dir");
      System.out.println("jbosstestDeployDir = " + jbosstestDeployDir);
      String deployADir = "/scopedA.ear/scopedA.jar";
      File libDir = new File(jbosstestDeployDir, deployADir);
      System.out.println("libDir = " + libDir.getAbsolutePath());
      // Create a SimpleResponseDTO class with a static serialVersionUID of 1L
      //ClassPool defaultPool = ClassPool.getDefault();
      ClassPool classes1Pool = ClassPool.getDefault();
      //ClassPool classes1Pool = new ClassPool(defaultPool);
      CtClass info = classes1Pool.makeClass("org.jboss.test.scoped.interfaces.dto.SimpleResponseDTO");
      info.addInterface(classes1Pool.get("java.io.Serializable"));
      CtClass s = classes1Pool.get("java.lang.String");
      CtField firstName = new CtField(s, "firstName", info);
      firstName.setModifiers(Modifier.PRIVATE);
      info.addField(firstName);
      CtMethod getFirstName = CtNewMethod.getter("getFirstName", firstName);
      getFirstName.setModifiers(Modifier.PUBLIC);
      info.addMethod(getFirstName);
      CtMethod setFirstName = CtNewMethod.setter("setFirstName", firstName);
      setFirstName.setModifiers(Modifier.PUBLIC);
      info.addMethod(setFirstName);
      CtClass s2 = classes1Pool.get("java.lang.String");
      CtField lastName = new CtField(s2, "lastName", info);
      lastName.setModifiers(Modifier.PRIVATE);
      info.addField(lastName);
      CtMethod getLastName = CtNewMethod.getter("getLastName", lastName);
      getLastName.setModifiers(Modifier.PUBLIC);
      info.addMethod(getLastName);
      CtMethod setLastName = CtNewMethod.setter("setLastName", lastName);
      setLastName.setModifiers(Modifier.PUBLIC);
      info.addMethod(setLastName);
      //CtClass s3 = classes1Pool.get("java.lang.Long");
      CtField serialVersion = new CtField(CtClass.longType, "serialVersionUID", info);
      serialVersion.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
      long serialVerionUID = 1L;
      info.addField(serialVersion, CtField.Initializer.constant(serialVerionUID));

      info.writeFile(libDir.getAbsolutePath());

      String classesBDir = "/classes";
      File rootDeployDir = new File(jbosstestDeployDir);
      String rootDeployDirPath = rootDeployDir.getParent();
      File classesDir = new File(rootDeployDirPath, classesBDir);
      System.out.println("classesDir = " + classesDir.getAbsolutePath());

//      info.writeFile(classesDir.getAbsolutePath());

      // copy the generated file in deploy to the classes directory
      SimpleRequestDTO tmpFile = new SimpleRequestDTO();
      String classPath = tmpFile.getClass().getResource("SimpleResponseDTO.class").getPath();
      File fileSrc = new File(libDir.getAbsolutePath() + "/org/jboss/test/scoped/interfaces/dto/SimpleResponseDTO.class");
      File fileDest = new File(classPath);
      copyFiles(fileSrc, fileDest);


      try
      {
         deploy("scopedA.ear");
         try
         {
            // Run the test
//            deploy("scopedB.ear");
            try
            {
               doTest();
            }
            finally
            {
               //undeploy("scopedB.ear");
            }

            // Run the test after a redeployment
//            deploy("scopedB.ear");
//            try                     x
//            {
//               doTest();
//            }
//            finally
//            {
//               undeploy("scopedB.ear");
//            }
         }
         finally
         {
            undeploy("scopedA.ear");
         }
      }
      finally
      {
//         isolateDeployments(Boolean.FALSE);
      }
   }

   private void copyFiles(File fileSrc, File fileDest) throws IOException
   {
      FileInputStream fis = new FileInputStream(fileSrc);
      FileOutputStream fos = new FileOutputStream(fileDest);
      byte[] buf = new byte[1024];
      int i = 0;
      while ((i = fis.read(buf)) != -1)
      {
         fos.write(buf, 0, i);
      }
      fis.close();
      fos.close();
   }


   private void doTest() throws Exception
   {
//      Properties env = new Properties();
//      env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
//      //env.put("java.naming.provider.url", "localhost:1099");
//      env.put("java.naming.provider.url", "localhost:1100");
//      env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
//
//      Context ic = new InitialContext(env);

      SessionAHome home = (SessionAHome) getInitialContext().lookup("SessionA");
      SessionA session = home.create();


         SimpleRequestDTO requestDTO = new SimpleRequestDTO();
         requestDTO.setFirstName("Daffy");
         requestDTO.setLastName("Duck");
         SimpleResponseDTO result = session.runSimpleTest(requestDTO);

         System.out.println(result.getFirstName() + " " + result.getLastName());

   }

   private void isolateDeployments(Boolean value) throws Exception
   {
      //getServer().setAttribute(EARDeployerMBean.OBJECT_NAME, new Attribute("Isolated", value));
      getServer().setAttribute(EARDeployerMBean.OBJECT_NAME, new Attribute("CallByValue", value));
   }
}
