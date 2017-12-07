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
package org.jboss.test.cluster.cache.bean;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.cache.Fqn;
import org.jboss.cache.pojo.PojoCache;
import org.jboss.test.cluster.cache.aop.CacheObject;

/**
 * @ejb.bean name="CacheObjectMeanTester"
 *           type="Stateless" 
 *           view-type="remote"
 *           jndi-name="test/CacheObjectMeanTester"
 *           
 * @ejb.home pattern="{0}Home"
 * 
 * @ejb.interface pattern="{0}"
 */
public class CacheObjectMeanTesterBean implements SessionBean
{
   /** @ejb.interface-method */
   public void bind(String id) throws Exception
   {
      MBeanServer server = (MBeanServer)MBeanServerFactory.findMBeanServer(null).get(0);
      PojoCache cache = (PojoCache)server.getAttribute(new ObjectName("jboss.cache:service=testTreeCacheAop"),
              "PojoCache");
      cache.attach(new Fqn(new Object[] {"sessions", id}).toString(), new CacheObject(id));
   }

   public void ejbCreate() throws CreateException
   {
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
   }

   public void setSessionContext(SessionContext arg0)
   {
   }

}
