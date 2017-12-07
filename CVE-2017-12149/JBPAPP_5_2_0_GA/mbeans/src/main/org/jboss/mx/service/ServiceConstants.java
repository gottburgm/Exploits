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
package org.jboss.mx.service;


/**
 * Defines constants for JBossMX services.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>. 
 * @version $Revision: 81026 $
 */
public interface ServiceConstants
{
   /** 
    * The object name domain <tt>'JBossMX'<tt> can be used by
    * JBossMX service implementations.
    */
   final static String JBOSSMX_DOMAIN           = "JBossMX";
   
   /** 
    * Default object name for persistence interceptor with <tt>ON_TIMER</tt>
    * policy.
    */
   final static String PERSISTENCE_TIMER        = new String(JBOSSMX_DOMAIN + ":name=PersistenceTimer");
   
   /**
    * DTD file name for XMLMBeanLoader, version 1.0 
    */
   final static String MBEAN_LOADER_DTD_1_0     = "JBossMX_MBeanLoader_1_0.dtd";

   /** DTD file name for JBossMX XMBean, version 1.0 */
   final static String JBOSSMX_XMBEAN_DTD_1_0   = "jboss_xmbean_1_0.dtd";
   final static String PUBLIC_JBOSSMX_XMBEAN_DTD_1_0   = "-//JBoss//DTD JBOSS XMBEAN 1.0//EN";

   /** DTD file name for JBossMX XMBean, version 1.1 */
   final static String JBOSSMX_XMBEAN_DTD_1_1   = "jboss_xmbean_1_1.dtd";
   final static String PUBLIC_JBOSSMX_XMBEAN_DTD_1_1   = "-//JBoss//DTD JBOSS XMBEAN 1.1//EN";
  
   /** DTD file name for JBossMX XMBean, version 1.2 */
   final static String JBOSSMX_XMBEAN_DTD_1_2   = "jboss_xmbean_1_2.dtd";
   final static String PUBLIC_JBOSSMX_XMBEAN_DTD_1_2   = "-//JBoss//DTD JBOSS XMBEAN 1.2//EN";
   
}
      



