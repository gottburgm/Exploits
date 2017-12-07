 /* distribution for a full listing of individual contributors.
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
package org.jboss.test.deployment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Principal;
import java.util.Collection;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.system.server.ServerConfigImplMBean;
import org.jboss.test.JBossTestCase;

/**
 * JBPAPP-6716 MainDeployer tests
 * 
 * @author Shaun Appleton
 * @author bmaxwell
 * @author klape
 */
public class JBPAPP6716UnitTestCase extends JBossTestCase
{
   private MainDeployerMBean deployer;
   private ServerConfigImplMBean serverInfo;
   private URL dummyUrl;
   private URL dummyExplodedUrl;
   private URL dummyIndexUrl;
   private String hostname;
   private String JBOSS_HOME;
   private String JBOSS_DEPLOY;
   private Object[] previousSecurity = new Object[2];
   private int maxWaitForHDScanner = 10;

   public JBPAPP6716UnitTestCase(String name) throws Exception
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      
      setSecurity(new SimplePrincipal("admin"), "admin");      
      
      dummyUrl = getDeployURL("dummy.war");
      dummyExplodedUrl = getDeployURL("dummy-exploded.war");
      
      MBeanServerConnection server = getServer();
      
      ObjectName objectName = ObjectNameFactory.create("jboss.system:service=MainDeployer");
      deployer = JMX.newMBeanProxy(server, objectName, MainDeployerMBean.class);
      
      objectName = ObjectNameFactory.create("jboss.system:type=ServerConfig");
      serverInfo = JMX.newMBeanProxy(server, objectName, ServerConfigImplMBean.class);
      
      JBOSS_HOME = System.getProperty("jbosstest.dist");

      // find the jboss config being run for this test      
      File deployDir = new File(serverInfo.getServerHomeDir(), "deploy");
      JBOSS_DEPLOY = deployDir.getAbsolutePath();
      
      hostname = System.getProperty("jbosstest.server.host");
      dummyIndexUrl = new URL("http://" + hostname + ":8080/dummy/index.html");
   }
      
   protected void tearDown() throws Exception
   {   
      super.tearDown();
      setSecurity((Principal) previousSecurity[0], previousSecurity[1]);      
   }
           
   //***** Deploy Tests *****//

   /* this tests these methods with exploded and non-exploded deploymenets
    * deploy(URL)
    * deploy(String)
    * undeploy(URL)
    * undeploy(String)
    * redeploy(URL)
    * redeploy(String)
    * isDeployed(URL)    
    */
   public void testDeployUnDeployReDeployIsDeployed() throws Exception
   {                 
      log.info("+++ testDeployUnDeployReDeployIsDeployed");      

      for(URL testURL : new URL[] {dummyUrl, dummyExplodedUrl } )
      {
         String testURLString = testURL.toExternalForm();
         try
         {
            //Deploy & undeploy with URL
            deployer.deploy(testURL);
            assertTrue("Failed to deploy URL: " + testURL, deployer.isDeployed(testURL));
   
            deployer.undeploy(testURL);
            assertTrue("Failed to undeploy URL: " + testURL, !deployer.isDeployed(testURL));
   
            //Deploy & undeploy with String
            deployer.deploy(testURLString);
            assertTrue("Failed to deploy String: " + testURLString, deployer.isDeployed(testURLString));
   
            deployer.undeploy(testURLString);
            assertTrue("Failed to undeploy String: " + testURLString, !deployer.isDeployed(testURLString));
   
            //Redploy 
            deployer.deploy(testURL);
            assertTrue("Failed to deploy URL: " + testURL, deployer.isDeployed(testURL));
   
            //With URL
            deployer.redeploy(testURL);
            assertTrue("Failed to redeploy URL: " + testURL, deployer.isDeployed(testURL));
   
            //With String
            deployer.redeploy(testURLString);
            assertTrue("Failed to redeploy String: " + testURLString, deployer.isDeployed(testURLString));
         }
         catch (Exception e)
         {
            e.printStackTrace();
            super.fail("Caught exception, message: " + e.getMessage());
         }
         finally
         {
            try
            {
               //make sure it's undeployed before moving on
               deployer.undeploy(testURL);
            }
            catch(Exception ignore)
            {
               //ignore
            }
         }
      }
   }
   
   // run tests using $JBOSS_HOME/bin/twiddle.sh on exploded and non-exploded deployments with path and url strings
   public void testTwiddle()
   {
      // $JBOSS_HOME/bin/twiddle.sh -u admin -p admin -s hostname invoke jboss.system:service=MainDeployer listDeployedAsString      
      String invoke = "-u admin -p admin -s " + hostname + " invoke jboss.system:service=MainDeployer ";
      String[] deployments = new String[] 
            { 
               dummyUrl.toExternalForm(), 
               dummyUrl.getPath(),
               dummyExplodedUrl.toExternalForm(),
               dummyExplodedUrl.getPath()
            };
      try
      {
         // test file:/path/to/dummy.war and /path/to/dummy.war
         for(String deployment : deployments)
         {
            log.info("testing: " + deployment);
            
            // deploy(String)
            twiddle(invoke + "deploy " + deployment, null);
            
            // isDeployed(String)
            twiddle(invoke + "isDeployed " + deployment, "true");
            
            // listDeployedAsString()
            twiddle(invoke + "listDeployedAsString", deployment);
            
            // listDeployed()
            twiddle(invoke + "listDeployed", deployment);
            
            // listDeployedModules()
            twiddle(invoke + "listDeployedModules", deployment);
            
            // undeploy(String)
            twiddle(invoke + "undeploy " + deployment, null);
      
            // isDeployed(String)
            twiddle(invoke + "isDeployed " + deployment, "false");
            
            // redeploy(String)
            twiddle(invoke + "redeploy " + deployment, null);
            
            // isDeployed(String)
            twiddle(invoke + "isDeployed " + deployment, "true");
            
            // redeploy(String)
            twiddle(invoke + "redeploy " + deployment, null);
   
            // isDeployed(String)
            twiddle(invoke + "isDeployed " + deployment, "true");
         }
      }
      finally
      {
         cleanUp();
      }      
   }
   
   // This tests is to test when deployements are copied into the deploy directory
   // undeploy on deployments int the deploy directory should not delete the deployement from deploy, it should just stop/undeploy the deployment
   // calling deploy on deployment in the deploy dir should start/deploy it
   public void testDeploymentsInDeployDirectory()
   {
      File dummyWarFile = new File(JBOSS_DEPLOY, "dummy.war");
      try
      {
         // copy dummy.war to deploy directory
         copyUrlToFile(dummyUrl, dummyWarFile);
         
         // wait 1 second for deployment, max of 5 seconds, then fail
         failIfIsFileDeployedNotEqual(dummyWarFile, true);
            
         // call undeploy, confirm dummy.war file is still in deploy dir and that it is not running, else fail
         log.info("call undeploy, confirm dummy.war file is still in deploy dir and that it is not running, else fail");
         deployer.undeploy(dummyWarFile.getAbsolutePath());
         
         if( ! dummyWarFile.exists() )
         {
            fail("The file: " + dummyWarFile.getAbsolutePath() + " should exist, undeploy should have not removed it from: " + JBOSS_DEPLOY);
         }
         if( isDummyRunning() )
         {
            fail("The url: " + dummyWarFile.getAbsolutePath() + " should not be accessible, because dummy.war should be stopped, after the previous undeploy call");
         }
         
         // call deploy, confirm dummy.war is started again and is accessible
         deployer.deploy(dummyWarFile.getAbsolutePath());
         
         if(deployer.isDeployed(dummyWarFile.getAbsolutePath()) == false)
         {
            fail("Failure: " + dummyWarFile.getAbsolutePath() + " should be deployed and started");
         }
         if( ! isDummyRunning() )
         {
            fail("The url: " + dummyWarFile.getAbsolutePath() + " should be accessible, because dummy.war should have been started again by the previous deploy call");
         }       
      }
      catch(Exception e)
      {
         log.error("testDeploymentsInDeployDirectory failed", e);
         fail(e.getMessage());
      }
      finally
      {
         //Give time for the reaper to let go of the file lock
         try
         {
            Thread.currentThread().sleep(15000);
         }
         catch (InterruptedException ie)
         {
            //Keep on moving
         }

         // remove dummy.war from deploy directory
         if(dummyWarFile.delete() == false)
         {
            fail("Failed to remove: " + dummyWarFile.getAbsolutePath() + " test deployment from: " + JBOSS_DEPLOY);
         }
         
         failIfIsFileDeployedNotEqual(dummyWarFile, false);          
      }
   }      
   
   //***** Listing Methods *****//

   public void testListDeployedAsString() throws Exception
   {
      log.info("+++ testListDeployedAsString");
            
      for(URL testURL : new URL[] {dummyUrl, dummyExplodedUrl } )
      {      
         try
         {
            deployer.deploy(testURL);
            log.info("calling assert: " + testURL.toExternalForm());
            log.info(deployer.listDeployedAsString());
            assertTrue("listDeployedAsString does not contain: " + testURL, deployer.listDeployedAsString().contains(testURL.toExternalForm()));
         }
         catch (Exception e)
         {
            super.fail("Caught exception, message: " + e.getMessage());
         }
         finally
         {
            cleanUp();
         }
      }
   }
      
   // this tests methods: listDeployed, listDeployedModules
   public void testListDeployedListDeployedModules() throws Exception
   {
      log.info("+++ testListDeployedListDeployedModules");

      for(URL testURL : new URL[] {dummyUrl, dummyExplodedUrl } )
      {      
         try
         {
            // deploy the dummy.war & make sure it is deployed
            deployer.deploy(testURL);
            assertTrue("listDeployedAsString does not contain: " + testURL, deployer.listDeployedAsString().contains(testURL.toExternalForm()));
                                                                 
            if(collectionContains(deployer.listDeployed(), testURL.toExternalForm()) == false)
               fail("listDeployed does not contain: " + testURL);
            
            if(collectionContains(deployer.listDeployedModules(), testURL.toExternalForm()) == false)
               fail("listDeployedModules does not contain: " + testURL);         
         }
         catch (Exception e)
         {
            super.fail("Caught exception, message: " + e.getMessage());
         }
         finally
         {
            cleanUp();
         }
      }
   }
   
   // tests isDeployed, undeploy, deploy, redeploy with a URL/String does not exist
   public void testNonExistantDeployments()
   {
      // this file does not exist
      File nonExistantFile = new File(JBOSS_DEPLOY, "jbpapp6716-non-existant.war");
      
      if(nonExistantFile.exists())
      {
         fail("The file: " + nonExistantFile + " should not exist");
      }
      
      // isDeployed should return false
      try
      {
         assertFalse("isDeployed should return false for non-existant deployment: " + nonExistantFile, deployer.isDeployed(nonExistantFile.getAbsolutePath()));;
      }
      catch(Exception e)
      {
         log.error("isDeployed threw an exception checking non-existant file: " + nonExistantFile, e);
         fail("isDeployed should not throw an exception checking non-existant file: " + nonExistantFile);
      }
            
      // undeploy should not throw an exception, it should just display a warning in the server.log
      try
      {
         deployer.undeploy(nonExistantFile.getAbsolutePath());
      }
      catch(Exception e)
      {
         log.error("undeploy threw an exception checking non-existant file: " + nonExistantFile, e);
         fail("undeploy should not throw an exception checking non-existant file: " + nonExistantFile + " , it should just have a warning in the JBoss server.log");
      }
      
      // deploy should throw an exception
      boolean fail = true;
      try
      {
         deployer.deploy(nonExistantFile.getAbsolutePath());         
      }
      catch(MalformedURLException me)
      {
         log.error("deploy threw an MalformedURLException checking non-existant file: " + nonExistantFile, me);
         fail("deploy should not throw a MalformedURLException checking non-existant file: " + nonExistantFile);         
      }
      catch(DeploymentException de)
      {
         log.error("deploy threw an exception as expected when checking non-existant file: " + nonExistantFile, de);
         fail = false;
      }
      if(fail)
      {
         fail("deploy should have thrown a DeploymentException checking non-existant file: " + nonExistantFile);
      }
      
      // redeploy should throw an exception
      fail = true;
      try
      {
         deployer.redeploy(nonExistantFile.getAbsolutePath());         
      }
      catch(MalformedURLException me)
      {
         log.error("redeploy threw an MalformedURLException checking non-existant file: " + nonExistantFile, me);
         fail("redeploy should not throw a MalformedURLException checking non-existant file: " + nonExistantFile);         
      }
      catch(DeploymentException de)
      {
         log.error("redeploy threw an exception as expected when checking non-existant file: " + nonExistantFile, de);
         fail = false;
      }
      if(fail)
      {
         fail("redeploy should have thrown a DeploymentException checking non-existant file: " + nonExistantFile);
      }
   }
   
   /** Utility Methods **/
   
   private void cleanUp()
   {
      URL[] testURLs = new URL[] {
            dummyUrl,
            dummyExplodedUrl
      } ;   
      for(URL testURL : testURLs)
      {
         try
         {
            deployer.undeploy(testURL);
         }
         catch(Exception e)
         {
            // eat it
         }
      }
   }
   
   private void setSecurity(Principal username, Object password)
   {
      previousSecurity[0] = SecurityAssociation.getPrincipal();
      previousSecurity[1] = SecurityAssociation.getCredential();
      
      SecurityAssociation.setPrincipal(username);
      SecurityAssociation.setCredential(password);       
   }
      
   private void failIfIsFileDeployedNotEqual(File file, boolean isDeployed)
   {
      int attempts = 0;
      String message = "undeploy";
      if(isDeployed)
      {         
         message = "deploy";
      }   
      try
      {
         try
         {
            // wait 1 second for deployment, max of 5 seconds, then fail                  
            do
            {
               Thread.sleep(1000);
               attempts++;
            } while (deployer.isDeployed(file.getAbsolutePath()) != isDeployed && attempts < maxWaitForHDScanner);
            
            if(attempts >= maxWaitForHDScanner)
            {                  
               fail("Failed to " + message + file.getAbsolutePath() + ", waited " + attempts + " seconds and it has not been " + message + "ed");
            }
            else
            {
               log.info("Waited " + attempts + " seconds for hd scanner to " + message + " " + file.getAbsolutePath());         
            }
         }
         catch(InterruptedException ie)
         {
            if(deployer.isDeployed(file.getAbsolutePath()) != isDeployed)
               fail("Failed to " + message + file.getAbsolutePath() + ", waited " + attempts + " seconds and it has not been " + message + "ed, and sleep interrupted");         
         }
      }
      catch(MalformedURLException me)
      {
         log.error(file.getAbsolutePath() + " is malformed", me);
         fail(file.getAbsolutePath() + " is malformed");         
      }            
   }
   
   private void twiddle(String args, String searchString)
   {
      String command = null;

      boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");

      if (isWindows)
      {
         command = JBOSS_HOME + "\\bin\\twiddle.bat " + args;
      }
      else
      {
         command = JBOSS_HOME + "/bin/twiddle.sh " + args;
      }

      Process child = null;
      
      // default to true since searchString may not be checked 
      boolean pass = true;
      try
      {
         if (isWindows)
         {
            File userDir = new File(JBOSS_HOME + "\\bin");
            child = Runtime.getRuntime().exec(command, null, userDir);
         }
         else
         {
            child = Runtime.getRuntime().exec(command, new String[]{"JBOSS_HOME="+JBOSS_HOME}, null);
         }

         String st;
         // get input stream from process
         BufferedReader in = new BufferedReader(new InputStreamReader(child.getInputStream()));

         String output = "";
         while ((st = in.readLine()) != null)
         {
            output = output + st;
         }
         in.close();

         // if a searchString is specified, then the output should contain it else pass will be false
         if (searchString != null)
         {
            pass &= output.contains(searchString);
         }

         // if exit code is not 0, then we also fail
         pass &= (child.waitFor() == 0);

         if (!pass)
            fail(command + " failed, exitCode=" + child.exitValue() + " should be 0 and output should contain: "
                  + searchString + " output:" + output);
      }
      catch (IOException ioe)
      {
         log.error("twiddle process failed to execute: " + command, ioe);
         fail("twiddle process failed to execute: " + command);
      }
      catch (InterruptedException e)
      {
         log.error("twiddle process failed to finish: " + command, e);
         fail("twiddle process failed to finish: " + command);
      }
      finally
      {
         if (child != null)
            child.destroy();
      }
   }
   
   private void copyUrlToFile(URL url, File file) throws Exception
   {  
      log.info("Copying url: " + url + " to file: " + file);
      InputStream is = url.openStream();            
      FileOutputStream fos = new FileOutputStream(file);
      
      byte[] buf = new byte[1024];
      int len;
      while ((len = is.read(buf)) > 0)
      {                       
         fos.write(buf, 0, len);
      }
            
      is.close();      
      fos.close();      
   }
   
   private boolean collectionContains(Collection collection, String string)
   {      
      for(Object o : collection)
      {
         log.info(o.toString());
         if(o.toString().contains(string))
         {
            log.debug(o.toString() + " matched " + string);
            return true;            
         }
      }
      return false;
   }
   
   private boolean isDummyRunning()
   {
      try
      {
         String content = readURL(dummyIndexUrl, false);
         log.info("dummy.war content: " + content);
         if(content != null && content.contains("Test html"))
         {
            log.info("dummy.war is running at: " + dummyIndexUrl);
            return true;
         }
      }
      catch(Exception e)
      {
         fail("Failed to test if dummy.war is running: " + e.getMessage());
      }
      
      log.info("dummy.war is not running at: " + dummyIndexUrl);
      return false;      
   }
   
   private String readURL(URL url, boolean failOnException)
   {                
      BufferedReader in = null;
      try
      {
         URLConnection uc = url.openConnection();  
         in = new BufferedReader( new InputStreamReader(uc.getInputStream()));
         
         String inputLine = in.readLine();            
         return inputLine;
      }
      catch (Exception e)
      {         
         if(failOnException)
         {
            log.error("Error occured trying to read: " + url, e);
            fail("Unable to read url: " + url);
         }
         return null;
      }
      finally
      {
         if ( in != null )
         {
            try
            {
               in.close();   
            }
            catch (Exception e)
            {
               // eat it
            }
         }            
      }
   }
}
