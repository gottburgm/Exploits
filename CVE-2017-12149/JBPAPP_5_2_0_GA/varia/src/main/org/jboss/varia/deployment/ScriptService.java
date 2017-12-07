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
package org.jboss.varia.deployment;

import org.jboss.system.ServiceMBeanSupport;

/**
 * Various methods that may be implemented by a bean shell script.
 *
 * Service lifecycle methods such as <code>start</code> and <code>stop</code>,
 * if available, are also called at service deployment and undeployment.
 *
 * @see BeanShellSubDeployer
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81038 $
 */
public interface ScriptService
   extends org.jboss.system.Service
{
   /**
    * Returns a list of <code>ObjectName</code>s as strings that
    * this service depends on.
    */
   String[] dependsOn() throws Exception;

   /**
    * Returns the <code>ObjectName</code> of this service.
    */
   String objectName() throws Exception;

   /**
    * Returns any MBean interfaces implemented by this script.
    */
   Class[] getInterfaces() throws Exception;
   
   /**
    * Called before <code>create</code> is called.
    * @param wrapper ServiceMBeanSupport wrapper that is controlling
    * this service
    */
   void setCtx(ServiceMBeanSupport wrapper) throws Exception;

}
