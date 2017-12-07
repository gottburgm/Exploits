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
package org.jboss.monitor;

/**
 * Metrics constants interface contains JMS message types
 * used to identify different monitoring point
 * message producers in the server. When publishing a message
 * to the metrics topic, you should type the message source.
 * For example:  <br><pre>
 *
 *      Message myMessage;
 *      myMessage.setJMSType(INVOCATION_METRICS);
 *
 * </pre>
 *
 * In addition, this interface contains some generic JMS property
 * identifiers for the metrics messages.
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 * @version $Revision: 81030 $
 */     
public interface MetricsConstants {
    
	// Constants ----------------------------------------------------
    /** Method invocation metrics producer. */
    final static String INVOCATION_METRICS = "Invocation";
    /** Bean cache metrics producer. */
    final static String BEANCACHE_METRICS  = "BeanCache";
    /** System resource metrics producer. */
    final static String SYSTEM_METRICS     = "System";
    

    /** Message property 'TIME' */
    final static String TIME        = "TIME";
    /** Message property 'APPLICATION' */
    final static String APPLICATION = "APPLICATION";
    /** Message property 'BEAN' */
    final static String BEAN        = "BEAN";
    /** Message propertu 'PRIMARY_KEY' */
    final static String PRIMARY_KEY = "PRIMARY_KEY";
    /** Message property 'TYPE' */
    final static String TYPE        = "TYPE";
    /** Message property 'ACTIVITY' */
    final static String ACTIVITY    = "ACTIVITY";
    /** Message property 'CHECKPOINT' */
    final static String CHECKPOINT  = "CHECKPOINT";
    /** Message property 'METHOD' */
    final static String METHOD      = "METHOD";
   
   
    /** System Monitor TYPE */
    final static String THREAD_MONITOR = "ThreadMonitor";
    /** System Monitor TYPE */
    final static String MEMORY_MONITOR = "MemoryMonitor";
    
}


