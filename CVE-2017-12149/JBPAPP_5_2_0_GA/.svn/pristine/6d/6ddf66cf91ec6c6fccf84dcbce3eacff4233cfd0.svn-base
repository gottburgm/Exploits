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
package org.jboss.system.server;

import javax.management.ObjectName;

import org.jboss.bootstrap.spi.Server;
import org.jboss.mx.util.ObjectNameFactory;

/**
 * MBean interface.
 */
public interface ServerImplMBean extends Server
{
   /** The default object name */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.system:type=Server");

   // Attributes ----------------------------------------------------
   
   /**
    * Hint to the JVM to run the garbage collector.
    */
   void runGarbageCollector();

   /**
    * Hint to the JVM to run any pending object finailizations.
    */
   void runFinalization();

   /**
    * Enable or disable tracing method calls at the Runtime level.
    */
   void traceMethodCalls(Boolean flag);

   /**
    * Enable or disable tracing instructions the Runtime level.
    */
   void traceInstructions(Boolean flag);

}
