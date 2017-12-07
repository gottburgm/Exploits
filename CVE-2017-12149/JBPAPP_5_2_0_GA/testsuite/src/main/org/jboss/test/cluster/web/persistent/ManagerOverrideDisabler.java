/**
 * 
 */
package org.jboss.test.cluster.web.persistent;

import org.jboss.web.tomcat.service.deployers.TomcatDeployer;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class ManagerOverrideDisabler
{
   private final TomcatDeployer deployer;

   /**
    * 
    */
   public ManagerOverrideDisabler(TomcatDeployer deployer)
   {
      if (deployer == null)
      {
         throw new IllegalArgumentException("Null deployer");
      }
      this.deployer = deployer;
   }
   
   public void start() throws Exception
   {
      this.deployer.setOverrideDistributableManager(false);
   }
   
   public void stop() throws Exception
   {
      this.deployer.setOverrideDistributableManager(true);
   }

}
