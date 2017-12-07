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
package org.jboss.ha.jndi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import org.jboss.ha.client.loadbalance.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.RoundRobin;
import org.jboss.ha.framework.server.HARMIServerImpl;
import org.jnp.interfaces.Naming;

/** Management Bean for HA-JNDI service for the legacy version that is coupled
 * to the RMI/JRMP protocol. The DetachedHANamingService should be used with
 * the appropriate detached invoker service.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81001 $
 */
public class HANamingService
   extends DetachedHANamingService
   implements HANamingServiceMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   /** An optional custom client socket factory */
   private RMIClientSocketFactory clientSocketFactory;
   /** An optional custom server socket factory */
   private RMIServerSocketFactory serverSocketFactory;
   /** The class name of the optional custom client socket factory */
   private String clientSocketFactoryName;
   /** The class name of the optional custom server socket factory */
   private String serverSocketFactoryName;
   /** The class name of the load balancing policy */
   private String loadBalancePolicy = RoundRobin.class.getName();
   /** The RMI port on which the Naming implementation will be exported. The
    default is 0 which means use any available port. */
   private int rmiPort = 0;
   private InetAddress rmiBindAddress;
   protected String replicantName = "HAJNDI";
   private HARMIServerImpl rmiserver;

   // Public --------------------------------------------------------

   public HANamingService()
   {
      // for JMX
   }

   public void setRmiPort(int p)
   {
      this.rmiPort = p;
   }
   public int getRmiPort()
   {
      return this.rmiPort;
   }
   
   public String getRmiBindAddress()
   {
      return (this.rmiBindAddress != null) ? this.rmiBindAddress.getHostAddress() : null;
   }

   public void setRmiBindAddress(String address) throws UnknownHostException
   {
      this.rmiBindAddress = InetAddress.getByName(address);
   }

   public String getClientSocketFactory()
   {
      return this.serverSocketFactoryName;
   }
   
   public void setClientSocketFactory(String factoryClassName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      this.clientSocketFactoryName = factoryClassName;
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> clazz = loader.loadClass(this.clientSocketFactoryName);
      this.clientSocketFactory = (RMIClientSocketFactory) clazz.newInstance();
   }
   
   public String getServerSocketFactory()
   {
      return this.serverSocketFactoryName;
   }
   public void setServerSocketFactory(String factoryClassName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      this.serverSocketFactoryName = factoryClassName;
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> clazz = loader.loadClass(this.serverSocketFactoryName);
      this.serverSocketFactory = (RMIServerSocketFactory) clazz.newInstance();
   }

   public String getLoadBalancePolicy()
   {
      return this.loadBalancePolicy;
   }
   public void setLoadBalancePolicy(String policyClassName)
   {
      this.loadBalancePolicy = policyClassName;
   }
   
   @Override
   protected void stopService() throws Exception
   {
      super.stopService();
      // Unexport server
      this.log.debug("destroy ha rmiserver");
      this.rmiserver.destroy ();
   }

   @Override
   protected Naming getNamingProxy() throws Exception
   {
      this.rmiserver = new HARMIServerImpl(this.clusterPartition, this.replicantName, Naming.class,
         this.theServer, this.rmiPort, this.clientSocketFactory, this.serverSocketFactory, this.rmiBindAddress);

      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class<?> clazz = cl.loadClass(this.loadBalancePolicy);
      LoadBalancePolicy policy = (LoadBalancePolicy)clazz.newInstance();

      Naming proxy = (Naming) this.rmiserver.createHAStub(policy);
      return proxy;
   }
}
