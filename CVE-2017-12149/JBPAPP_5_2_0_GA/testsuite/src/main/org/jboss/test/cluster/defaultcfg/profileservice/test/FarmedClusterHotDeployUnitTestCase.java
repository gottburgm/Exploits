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

package org.jboss.test.cluster.defaultcfg.profileservice.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.virtual.VFS;

/**
 * @author Brian Stansberry
 *
 */
public class FarmedClusterHotDeployUnitTestCase extends JBossClusteredTestCase
{  
   /** We use the default profile, defined by DeploymentManager to deploy apps. */
   public static final ProfileKey farmProfile = new ProfileKey("farm");
   public static final String SCANNER_ONAME = "jboss.deployment:flavor=URL,type=DeploymentScanner";
   private ManagementView activeView;
   private File farmDir;
   
   /**
    * Create a new FarmedClusterHotDeployUnitTestCase.
    * 
    * @param name
    */
   public FarmedClusterHotDeployUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testFarmHotDeployment() throws Exception
   {
      ManagementView mgtView = getManagementView(getNamingContext(0));
      ComponentType type = new ComponentType("MCBean", "ServerConfig");
      ManagedComponent mc = mgtView.getComponent("jboss.system:type=ServerConfig", type);
      assertNotNull(mc);
      String homeDir = (String) ((SimpleValue) mc.getProperty("serverHomeDir").getValue()).getValue();
      assertNotNull(homeDir);
      
      this.farmDir = new File(homeDir, "farm");      
      assertTrue(farmDir + " exists", farmDir.exists());
      
      ObjectName scanner = new ObjectName(SCANNER_ONAME);      
      try
      {
         getAdaptors()[0].invoke(scanner, "stop", new Object[]{}, new String[]{});
         validateInitialState();
         performModifications();
      }
      finally
      {
         getAdaptors()[0].invoke(scanner, "start", new Object[]{}, new String[]{});
      }      

      validateFinalState();
   }
   
   private void validateInitialState() throws Exception
   {
      // Simple add
      checkContentRemoved("jboss.system:service=HDAddTestThreadPool", 0);
      
      // Nested add
      checkContentRemoved("jboss.system:service=HDNestedAddTestThreadPool", 0);
      
      // New dir
      checkContentRemoved("jboss.system:service=HDAddDirTestThreadPool", 0);
      
      // Simple removal
      checkContent("jboss.system:service=HDRemoveTestThreadPool", "RemoveFarmThreadPool", 0);
      
      // Nested removal
      checkContent("jboss.system:service=HDNestedRemoveTestThreadPool", "NestedRemoveFarmThreadPool", 0);
      
      // Dir removal
      checkContent("jboss.system:service=HDRemoveDirFarmTestThreadPool", "RemoveDirFarmThreadPool", 0);
      
      // Nested mod
      checkContent("jboss.system:service=HDModifyNestedTestThreadPool", "UnmodifiedNestedFarmThreadPool", 0); 
      
      // Simple mod
      checkContent("jboss.system:service=HDModifyTestThreadPool", "UnmodifiedFarmThreadPool", 0);     
   }

   private void performModifications() throws Exception
   {
      // Simple add
      copyFile(farmDir, "hd-add-deployment-service.xml");
      
      // Nested add
      File dir = new File(farmDir, "hd-nestedAdd");
      dir.mkdir();
      copyFile(dir, "hd-nested-add-deployment-service.xml");
      
      // New dir
      dir = new File(farmDir, "hd-newDirAdd");
      dir.mkdir();
      copyFile(dir, "hd-add-dir-deployment-service.xml");
      
      // Simple removal
      File toRemove = new File(farmDir, "hd-remove-deployment-service.xml");
      removeFile(toRemove);
      
      // Nested removal
      dir = new File(farmDir, "hd-nestedRemove");
      toRemove = new File(dir, "hd-nested-remove-deployment-service.xml");
      removeFile(toRemove);
      
      // Dir removal
      toRemove = new File(farmDir, "hd-removeDir");
      removeFile(toRemove);
      
      // Nested mod
      dir = new File(farmDir, "hd-nestedMod.sar"); 
      dir = new File(dir, "META-INF");
      copyFile(dir, "hd-nested-mod-deployment-service.xml", "jboss-service.xml");
      
      // Simple mod
      copyFile(farmDir, "hd-mod-deployment-service.xml");
   }
   
   private void validateFinalState() throws Exception
   {
      // Simple add
      // For this one we give a long timeout; once this one passes all the others
      // should be there as well, since we stop the scanner during the mods
      // and do them all at once
      checkContent("jboss.system:service=HDAddTestThreadPool", "AddFarmThreadPool", 12000);
      
      // Nested add
      checkContent("jboss.system:service=HDNestedAddTestThreadPool", "NestedAddFarmThreadPool", 10000);
      
      // New dir
      checkContent("jboss.system:service=HDAddDirTestThreadPool", "AddDirFarmThreadPool", 0);
      
      // Simple removal
      checkContentRemoved("jboss.system:service=HDRemoveTestThreadPool", 0);
      
      // Nested removal
      checkContentRemoved("jboss.system:service=HDNestedRemoveTestThreadPool", 0);
      
      // Dir removal
      checkContentRemoved("jboss.system:service=HDRemoveDirFarmTestThreadPool", 0);
      
      // Nested mod
      checkContent("jboss.system:service=HDModifyNestedTestThreadPool", "ModifiedNestedFarmThreadPool", 0);
      
      // Simple mod
      checkContent("jboss.system:service=HDModifyTestThreadPool", "ModifiedFarmThreadPool", 0);
   }

   /**
    * Obtain the ProfileService.ManagementView
    * @return
    * @throws Exception
    */
   private ManagementView getManagementView(Context ctx)
      throws Exception
   {
      if( activeView == null )
      {
         ProfileService ps = (ProfileService) ctx.lookup("ProfileService");
         activeView = ps.getViewManager();
         // Init the VFS to setup the vfs* protocol handlers
         VFS.init();
      }
      // Reload
      activeView.load();
      return activeView;
   }
   
   private Context getNamingContext(int nodeIndex) throws Exception
   {
      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[nodeIndex]);
      return new InitialContext(env1);
   }

   private void copyFile(File dir, String fileName) throws Exception
   {
      copyFile(dir, fileName, fileName);
   }

   private void copyFile(File dir, String sourceName, String targetName) throws Exception
   {
      InputStream is = getDeployURL(sourceName).openStream();
      try
      {
         File output = new File(dir, targetName);
         FileOutputStream fos = new FileOutputStream(output);
         try
         {
            byte[] tmp = new byte[1024];
            int read;
            while((read = is.read(tmp)) > 0)
            {
               fos.write(tmp, 0, read);
            }
            fos.flush();
         }
         finally
         {
            fos.close();
         }         
      }
      finally
      {
         is.close();
      }
   }
   
   private void removeFile(File toRemove) throws Exception
   {
      if (toRemove.exists())
      {
         if (toRemove.isDirectory())
         {
            File[] children = toRemove.listFiles();
            if (children != null)
            {
               for (File child : children)
               {
                  removeFile(child);
               }
            }
         }         
         if (!toRemove.delete())
            throw new IllegalStateException(toRemove + " cannot be deleted");
      }
      else
      {
         throw new IllegalStateException(toRemove + " does not exist");
      }
   }
   
   private void checkContent(String objectName, String poolName, long timeout) throws Exception
   {
      boolean node0OK = false;
      boolean node1OK = false;
      
      MBeanServerConnection[] adaptors = getAdaptors();
      ObjectName oname = new ObjectName(objectName);
      
      long deadline = System.currentTimeMillis() + timeout;
      do
      {
         if (!node0OK)
         {
            try
            {
               node0OK = poolName.equals(adaptors[0].getAttribute(oname, "Name"));
            }
            catch (Exception ignored) {}
         }
         if (!node1OK)
         {
            try
            {
               node1OK = poolName.equals(adaptors[1].getAttribute(oname, "Name"));
            }
            catch (Exception ignored) {}                  
         }
         
         if (node0OK && node1OK)
         {
            break;
         }
         
         Thread.sleep(200);
      }
      while (System.currentTimeMillis() < deadline);
      
      assertTrue(objectName + " -- node0 OK", node0OK);
      assertTrue(objectName + " -- node1 OK", node1OK);
   }
   
   private void checkContentRemoved(String objectName, long timeout) throws Exception
   {
      boolean node0OK = false;
      boolean node1OK = false;
      
      MBeanServerConnection[] adaptors = getAdaptors();
      ObjectName oname = new ObjectName(objectName);
      
      long deadline = System.currentTimeMillis() + 12000;
      do
      {
         if (!node0OK)
         {
            try
            {
               node0OK = (adaptors[0].isRegistered(oname) == false);
            }
            catch (Exception ignored) {}
         }
         if (!node1OK)
         {
            try
            {
               node1OK = (adaptors[1].isRegistered(oname) == false);
            }
            catch (Exception ignored) {}                  
         }
         
         if (node0OK && node1OK)
         {
            break;
         }
         
         Thread.sleep(200);
      }
      while (System.currentTimeMillis() < deadline);
      
      assertTrue(objectName + " -- node0 OK", node0OK);
      assertTrue(objectName + " -- node1 OK", node1OK);
   }
   
   /** 
    * Does some file copies into the dirs the test normally uses. 
    * Just a tool for debugging 
    */
   public static void main(String[] args)
   {
      try
      {
         File source = new File("/home/bes/dev/jboss/Branch_5_x/testsuite/output/resources/cluster/farm/hotdeploy/hd-add-deployment-service.xml");
         
         File dest = new File("/home/bes/dev/jboss/Branch_5_x/build/output/jboss-5.1.0.CR1/server/cluster-profilesvc-0/farm/hd-add-deployment-service.xml");
         copyFile(source, dest);
         source = new File("/home/bes/dev/jboss/Branch_5_x/testsuite/output/resources/cluster/farm/hotdeploy/hd-mod-deployment-service.xml");
         
         dest = new File("/home/bes/dev/jboss/Branch_5_x/build/output/jboss-5.1.0.CR1/server/cluster-profilesvc-0/farm/hd-mod-deployment-service.xml");
         copyFile(source, dest);
         
         Thread.sleep(6000);
         
         source = new File("/home/bes/dev/jboss/Branch_5_x/testsuite/output/resources/cluster/farm/hotdeploy/hd-mod-deployment-service.xml");
         
         dest = new File("/home/bes/dev/jboss/Branch_5_x/build/output/jboss-5.1.0.CR1/server/cluster-profilesvc-0/farm/hd-mod-deployment-service.xml");
         copyFile(source, dest);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      
      System.out.println(new java.util.Date());
   }

   private static void copyFile(File source, File dest) throws FileNotFoundException, IOException
   {
      InputStream is = new java.io.FileInputStream(source);
      try
      {
         FileOutputStream fos = new FileOutputStream(dest);
         try
         {
            byte[] tmp = new byte[1024];
            int read;
            while((read = is.read(tmp)) > 0)
            {
               fos.write(tmp, 0, read);
            }
            fos.flush();
         }
         finally
         {
            fos.close();
         }         
      }
      finally
      {
         is.close();
      }
   }

}
