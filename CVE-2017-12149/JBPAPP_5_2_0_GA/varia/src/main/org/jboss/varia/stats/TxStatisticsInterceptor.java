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
package org.jboss.varia.stats;

import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.BeanMetaData;
import org.jboss.deployment.DeploymentException;
import org.jboss.mx.util.MBeanServerLocator;
import org.w3c.dom.Element;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.reflect.Method;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81038 $</tt>
 */
public class TxStatisticsInterceptor
   extends AbstractInterceptor
   implements XmlLoadable
{
   private MBeanServer server;
   private ObjectName serviceName;

   private String local;
   private String localHome;
   private String remote;
   private String home;

   // XmlLoadable implementation

   public void importXml(Element element) throws Exception
   {
      String service = element.getAttribute("service");
      if(service == null || service.trim().length() == 0)
      {
         throw new DeploymentException("Required attribute 'service' is not set.");
      }

      serviceName = new ObjectName(service);
      server = MBeanServerLocator.locateJBoss();
   }

   // Interceptor implementation

   public void start()
   {
      BeanMetaData bean = container.getBeanMetaData();
      local = bean.getLocal();
      localHome = bean.getLocalHome();
      remote = bean.getRemote();
      home = bean.getHome();
   }

   public Object invokeHome(final Invocation mi) throws Exception
   {
      Method method = mi.getMethod();
      if(method != null)
      {
         String className = mi.getType() == InvocationType.LOCALHOME ? localHome : home;
         logInvocation(className + "." + method.getName());
      }

      return super.invokeHome(mi);
   }

   public Object invoke(final Invocation mi) throws Exception
   {
      Method method = mi.getMethod();
      if(method != null)
      {
         String className = mi.getType() == InvocationType.LOCAL ? local : remote;
         logInvocation(className + "." + method.getName());
      }

      return super.invoke(mi);
   }

   // Private

   private void logInvocation(String method)
   {
      try
      {
         StatisticalItem item = new TxReport.MethodStats(method);
         server.invoke(serviceName, "addStatisticalItem",
            new Object[]{item},
            new String[]{StatisticalItem.class.getName()});
      }
      catch(Exception e)
      {
         log.error("Failed to add invocation.", e);
      }
   }
}
