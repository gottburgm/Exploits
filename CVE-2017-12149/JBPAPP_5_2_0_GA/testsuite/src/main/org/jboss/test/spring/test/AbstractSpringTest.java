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
package org.jboss.test.spring.test;

import java.net.URL;
import javax.management.MBeanServerConnection;

import org.jboss.deployment.MainDeployerMBean;
import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractSpringTest extends JBossTestCase
{
   protected AbstractSpringTest(String s)
   {
      super(s);
   }

   protected <T> T invokeMainDeployer(String methodName, Object[] args, String[] sig, Class<T> clazz) throws Exception
   {
      if (clazz == null)
         throw new IllegalArgumentException("Null class.");

      MBeanServerConnection server = getServer();
      Object result = server.invoke(MainDeployerMBean.OBJECT_NAME, methodName, args, sig);
      return clazz.cast(result);
   }

   protected boolean isDeployed(String deployment) throws Exception
   {
      URL deployURL = getDeployURL(deployment);
      String[] sig = {URL.class.getName()};
      Object[] args = {deployURL};
      return invokeMainDeployer("isDeployed", args, sig, Boolean.class);
   }
}