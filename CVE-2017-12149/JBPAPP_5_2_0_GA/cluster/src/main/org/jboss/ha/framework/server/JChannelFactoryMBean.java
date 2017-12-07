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
package org.jboss.ha.framework.server;

import javax.management.ObjectName;

import org.jgroups.Channel;
import org.jgroups.ChannelException;

/**
 * StandardMBean interface for {@link JChannelFactory}.
 * <p>
 * The plain-old-java-interface for the channel factory is 
 * <code>org.jgroups.ChannelFactory</code>; users are encouraged to dependency
 * inject a <code>org.jgroups.ChannelFactory</code>.
 * 
 * 
 * @author Bela Ban
 * @author Brian Stansberry
 * 
 * @version $Revision: 90534 $
 */
public interface JChannelFactoryMBean 
{   
   /**
    * Returns all configurations as a string
    */
   String getMultiplexerConfig();
   
   /**
    * Instructs JGroups to load a set of XML protocol stack configurations.
    * Same as {@link #setMultiplexerConfig(String, boolean) setMultiplexerConfig(properties, true)}. 
    * 
    * @param properties a string representing a system resource containing a
    *                   JGroups XML configuration, a string representing a URL
    *                   pointing to a JGroups XML configuration, or a string
    *                   representing a file name that contains a JGroups XML
    *                   configuration.
    *                   
    * @throws Exception
    */
   void setMultiplexerConfig(String properties) throws Exception;
   
   /**
    * Instructs JGroups to load a set of XML protocol stack configurations.
    * 
    * @param properties a string representing a system resource containing a
    *                   JGroups XML configuration, a string representing a URL
    *                   pointing to a JGroups XML configuration, or a string
    *                   representing a file name that contains a JGroups XML
    *                   configuration.
    * @param replace    <code>true</code> if any protocol stack configuration
    *                   in <code>properties</code> that has the same name
    *                   as an existing configuration should replace that
    *                   existing configuration; <code>false</code> if the
    *                   existing configuration should take precedence.
    * 
    * @throws Exception
    */
   void setMultiplexerConfig(String properties, boolean replace) throws Exception;

   /**
    * Gets the domain portion of any {@link ObjectName} the factory should
    * use when registering channels or protocols in JMX.
    * 
    * @return the domain portion of the object name, or <code>null</code>.
    */
   String getDomain();
   
   /**
    * Sets the domain portion of any {@link ObjectName} the factory should
    * use when registering channels or protocols in JMX.
    * 
    * @param name the domain portion of the object name. Must conform to
    *             the rules for elements in an ObjectName.
    */
   void setDomain(String name);

   /**
    * Gets whether this factory should register channels it creates in JMX.
    * 
    * @return <code>true</code> if channels should be registered, 
    *         <code>false</code> if not
    */
   boolean isExposeChannels();
  
   /**
    * Sets whether this factory should register channels it creates in JMX.
    * 
    * @param flag <code>true</code> if channels should be registered, 
    *             <code>false</code> if not
    */
   void setExposeChannels(boolean flag);

   /**
    * Gets whether this factory should register protocols it creates in JMX.
    * 
    * @return <code>true</code> if protocols should be registered, 
    *         <code>false</code> if not
    */
   boolean isExposeProtocols();
   
   /**
    * Sets whether this factory should register protocols it creates in JMX.
    * 
    * @param flag <code>true</code> if protocols should be registered, 
    *             <code>false</code> if not
    */
   void setExposeProtocols(boolean f);

   /**
    * Returns the stack configuration as a string (valid to be fed into new JChannel(String)). Throws an exception
    * if the stack_name is not found. One of the setMultiplexerConfig() methods had to be called beforehand.
    * 
    * @return The protocol stack config as a plain string
    */
   String getConfig(String stack_name) throws Exception;
   
   /**
    * Removes the given stack from the configuration.
    * 
    * @param stack_name the name of the stack
    * @return <code>true</code> if the stack was removed; <code>false</code> if
    *         it wasn't registered
    */
   boolean removeConfig(String stack_name);
   
   /** Removes all protocol stack configurations */
   void clearConfigurations();

   /**
    * Create a {@link Channel} using the specified stack. Channel will use a 
    * shared transport if the <code>singleton-name</code> attribute is
    * set on the stack's transport protocol.
    * 
    * @param stack_name 
    *            The name of the stack to be used. All stacks are defined in
    *            the configuration with which the factory is configured (see
    *            {@link #setMultiplexerConfig(Object)} for example. If
    *            clients attempt to create a Channel for an undefined stack 
    *            name an exception will be thrown.
    * 
    * @return the channel
    * 
    * @throws IllegalArgumentException if <code>stack_name</code> is 
    * <code>null</code> or {@link #getConfig(String)} returns <code>null</code>
    * when <code>stack_name</code> is used.
    * 
    * @throws Exception
    */
   Channel createChannel(String stack_name) throws Exception;
    
   /**
    * Creates and returns a shared transport Channel configured with the specified 
    * {@link #getConfig(String) protocol stack configuration}.
    * <p>
    * <emphasis>NOTE:</emphasis> The implementation of this method is somewhat
    * different from what is described in 
    * {@link org.jgroups.ChannelFactory#createMultiplexerChannel(String, String)}.
    * The returned channel will not be an instance of 
    * <code>org.jgroups.mux.MuxChannel</code>; rather a channel that uses a
    * shared transport will be returned.  This will be the case whether or
    * not the protocol stack specified by <code>stack_name</code> includes
    * a <code>singleton_name</code> attribute in its 
    * {@link org.jgroups.protocols.TP transport protocol} configuration. If no 
    * <code>singleton_name</code> attribute is present, this factory will create
    * a synthetic one by prepending "unnamed_" to the provided
    * <code>id</code> param and will use that for the returned channel's 
    * transport protocol. (Note this will not effect the protocol stack
    * configuration named by <code>stack_name</code>; i.e. another request
    * that passes the same <code>stack_name</code> will not inherit the
    * synthetic singleton name.) 
    * 
    * @param stack_name
    *            The name of the stack to be used. All stacks are defined in
    *            the configuration with which the factory is configured (see
    *            {@link #setMultiplexerConfig(Object)} for example. If
    *            clients attempt to create a Channel for an undefined stack 
    *            name an Exception will be thrown.
    * @param id  Only used if the transport protocol configuration for the
    *            specified stack does not include the <code>singleton_name</code>
    *            attribute; then it is used to create a synthetic singleton-name
    *            for the channel's protocol stack.
    *            
    * @return An implementation of Channel configured with a shared transport.
    *         
    * @throws ChannelException
    */
   Channel createMultiplexerChannel(String stack_name, String id) throws Exception;
   
   
   /**
    * Creates and returns a shared transport Channel configured with the specified 
    * {@link #getConfig(String) protocol stack configuration}.
    * 
    * See {@link #createMultiplexerChannel(String, String)}; the additional
    * attributes specified in this overloaded version of that method are ignored.
    *
    * @param register_for_state_transfer ignored in JBoss AS. Treated as <code>false</code>.
    * 
    * @param substate_id ignored in JBoss AS
    *            
    * @return An implementation of Channel configured with a shared transport.
    *         
    * @throws ChannelException
    */
   Channel createMultiplexerChannel(String stack_name, String id, boolean register_for_state_transfer, String substate_id) throws Exception;
   
   /**
    * Execute the create phase of the 4 step lifecycle.
    * 
    * @throws Exception
    */
   void create() throws Exception;
   
   /**
    * Execute the start phase of the 4 step lifecycle.
    * 
    * @throws Exception
    */
   void start() throws Exception;
   
   /**
    * Execute the stop phase of the 4 step lifecycle.
    * 
    * @throws Exception
    */
   void stop();
   
   /**
    * Execute the destroy phase of the 4 step lifecycle.
    * 
    * @throws Exception
    */
   void destroy();
    
   /**
    * Returns the names of the currently registered protocol stack configurations.
    */
   String dumpConfiguration();
   
   /**
    * Dumps the names of any currently running multiplexer channels along with
    * the id's of any services that are using them.  Information about
    * currently running non-multiplexer channels are not returned.
    */
   String dumpChannels();
}
