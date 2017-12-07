/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.messagedriven.mbeans;

import java.util.ArrayList;
import java.util.Properties;

import javax.jms.Message;
import javax.management.ObjectName;
import javax.transaction.Transaction;

import org.jboss.deployment.EjbParsingDeployerMBean;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * Management of the test message driven bean 
 *
 * @author <a href="mailto:adrian@jboss.com>Adrian Brock</a>
 * @version <tt>$Revision: 1.4</tt>
 */
public interface TestMessageDrivenManagementMBean extends ServiceMBean
{
   static ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.test:service=TestMessageDrivenManagement");
   void initProperties(Properties props);
   void setEjbParsingDeployer(EjbParsingDeployerMBean deployer);
   void addMessage(Message message);
   ArrayList<Object[]> getMessages();
   Transaction getTransaction();
}
