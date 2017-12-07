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
package org.jboss.test.xslt.support;

import java.util.Hashtable;

import org.jboss.system.ServiceMBean;

/**
 * A test mbean
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public interface XalanCheckMBean extends ServiceMBean
{

   // Attributes ----------------------------------------------------
   
   /** Read the xalan version seeing by this deployment */
   String getXalanVersion();
   
   // Operations ----------------------------------------------------
   
   /** Return the xalan environment properties */
   Hashtable fetchXalanEnvironmentHash();
   
   /** This test throws an Exception when run using xalan 2.5.2 */
   void testXalan25Bug15140() throws Exception;
}