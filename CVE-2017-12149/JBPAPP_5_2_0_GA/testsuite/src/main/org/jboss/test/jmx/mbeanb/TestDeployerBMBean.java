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
package org.jboss.test.jmx.mbeanb;

import javax.management.ObjectName;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.jboss.mx.util.ObjectNameFactory;

import org.jboss.system.Service;
import org.jboss.system.ServiceMBean;

/** 
 * This is a little class to test deploying jsrs
 *   
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 *
 * @version $Revision: 81036 $
 *
 * <p><b>david jencks:</b>
 * <ul>
 *   <li> initial import
 * </ul>
 */
public interface TestDeployerBMBean
   extends Service, ServiceMBean
{
   /** The default object name. */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("test:service=TestDeployerB");
}
