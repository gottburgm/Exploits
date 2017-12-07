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
package org.jboss.test.cluster.hapartition.rpc;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

/** Tests of clustered RPC calls
*
* @author Jerry Gauthier
* @version $Revision: 85945 $
*/
public class RPCUser extends ServiceMBeanSupport implements RPCUserMBean
{  
   protected static Logger log = Logger.getLogger(RPCUser.class);
   private static final String METHOD_GET_PERSON = "getPerson";
   private static final String METHOD_GET_PERSON_MATCH = "getPersonMatch";
   private static final String METHOD_NOTIFY_PERSON = "notifyPerson";

   protected String rpcServiceName;
   protected HAPartition partition = null;
   private Person myPerson = null;

   public String getPartitionName()
   {
      return (partition == null) ? null : partition.getPartitionName();
   }
   
   public HAPartition getHAPartition()
   {
      return partition;
   }
   
   public void setHAPartition(HAPartition partition)
   {
      this.partition = partition;
   }
   
   public void startService() throws Exception
   {
      rpcServiceName = this.serviceName.toString();
      log.debug("RPCTestCase.startService() - " + rpcServiceName);
      
      if (partition == null)
         throw new IllegalStateException("RPCUser partition is null");

      ClusterNode me = partition.getClusterNode();
      ClusterNode[] nodes = partition.getClusterNodes();
      boolean isFirstNode = (nodes != null && nodes[0].equals(me));
      
      // register the service with the partition
      // note that "OneNode" services are only registered on the first node
      if (rpcServiceName.indexOf("OneNode") < 0 || isFirstNode)
      {
         // ClassLoader services register with a classloader reference
         if (rpcServiceName.indexOf("ClassLoader") >= 0)
            partition.registerRPCHandler(rpcServiceName, this, Thread.currentThread().getContextClassLoader());
         else
            partition.registerRPCHandler(rpcServiceName, this);
      }
      
      // add a different person to each of the two nodes
      Person p;
      if (isFirstNode)
      {
         p = new Person("John White");
         p.setAddress("Main Street", "Boston", "MA", "02101");
         p.setDob(new GregorianCalendar(1965, GregorianCalendar.MARCH, 30));
         p.setEmployer("WidgetsRUs");
      }
      else
      {
         p = new Person("Jane Brown");
         p.setAddress("High Street", "Cambridge", "MA", "02141");
         p.setDob(new GregorianCalendar(1970, GregorianCalendar.JULY, 15));
         p.setEmployer("AcmeLtd");
      }
      myPerson = p;
       
   }   
  
   public ArrayList runRetrieveAll() throws Exception
   {
      return partition.callMethodOnCluster(rpcServiceName, METHOD_GET_PERSON, null, null, false);
   }
   
   public ArrayList runRetrieveQuery(PersonQuery query) throws Exception
   {
      Object[] parms = new Object[]{query};
      Class[] types = new Class[]{PersonQuery.class};
      return partition.callMethodOnCluster(rpcServiceName, METHOD_GET_PERSON_MATCH, parms, types, false);
   }
   
   public ArrayList runRetrieveFromCoordinator() throws Exception
   {
      return partition.callMethodOnCoordinatorNode(rpcServiceName, METHOD_GET_PERSON, null, null, false);
   }
   
   public void runNotifyAllAsynch() throws Exception
   {
      Object[] parms = new Object[]{Boolean.TRUE};
      Class[] types = new Class[]{Boolean.class};
      partition.callAsynchMethodOnCluster(rpcServiceName, METHOD_NOTIFY_PERSON, parms, types, false);
   }
   
   public Person getPerson()
   {
      return myPerson;
   }
   
   public Person getPersonMatch(PersonQuery query)
   {
      if (query.isMatch(myPerson))
         return myPerson;
      else
         return null;
   }
   
   public void notifyPerson(Boolean notify)
   {
      myPerson.setNotified(notify.booleanValue());
   }

}
