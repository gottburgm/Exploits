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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.rmi.dgc.VMID;
import java.rmi.server.UID;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.bootstrap.spi.util.ServerConfigUtil;
import org.jboss.ha.framework.server.managed.OpenChannelsMapper;
import org.jboss.ha.framework.server.managed.ProtocolStackConfigurationsMapper;
import org.jboss.logging.Logger;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.metatype.api.annotations.MetaMapping;
import org.jboss.system.ServiceMBean;
import org.jboss.util.loading.ContextClassLoaderSwitcher;
import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.ChannelFactory;
import org.jgroups.ChannelListenerAdapter;
import org.jgroups.Event;
import org.jgroups.Global;
import org.jgroups.JChannel;
import org.jgroups.conf.ConfiguratorFactory;
import org.jgroups.conf.ProtocolData;
import org.jgroups.conf.ProtocolParameter;
import org.jgroups.conf.ProtocolStackConfigurator;
import org.jgroups.jmx.JmxConfigurator;
import org.jgroups.protocols.TP;
import org.jgroups.stack.IpAddress;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.DefaultThreadFactory;
import org.jgroups.util.LazyThreadFactory;
import org.jgroups.util.ThreadDecorator;
import org.jgroups.util.ThreadFactory;
import org.jgroups.util.ThreadManager;
import org.jgroups.util.Util;
import org.w3c.dom.Element;

/**
 * Implementation of the JGroups <code>ChannelFactory</code> that supports a 
 * number of JBoss AS-specific behaviors:
 * <p>
 * <ul>
 * <li>Passing a config event to newly created channels containing 
 * "additional_data" that will be associated with the JGroups 
 * <code>IpAddress</code> for the peer. Used to provide logical addresses
 * to cluster peers that remain consistent across channel and server restarts.</li>
 * <li>Never returns instances of {@link org.jgroups.mux.MuxChannel} from
 * the <code>createMultiplexerChannel</code> methods.  Instead always returns
 * a channel with a shared transport protocol.</li>
 * <li>Configures the channel's thread pools and thread factories to ensure
 * that application thread context classloaders don't leak to the channel
 * threads.</li>
 * <li>Exposes a ProfileService ManagementView interface.</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto://brian.stansberry@jboss.com">Brian Stansberry</a>
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * 
 * @version $Revision: 113225 $
 */
@ManagementObject(name="JChannelFactory", 
      componentType=@ManagementComponent(type="MCBean", subtype="JGroupsChannelFactory"),
      properties=ManagementProperties.EXPLICIT,
      isRuntime=true)
public class JChannelFactory
      implements ChannelFactory, JChannelFactoryMBean, MBeanRegistration
{
   protected static final Logger log = Logger.getLogger(JChannelFactory.class);
   
   /** 
    * Prefix prepended to the protocol stack name to create a synthetic
    * transport protocol <code>singleton_name</code> value for channels
    * that don't configure a <code>singleton_name</code>. 
    */
   public static final String UNSHARED_TRANSPORT_NAME_BASE = "unnamed_";
   
   /** Default value for property {@link #getDomain() domain}. */
   public static final String DEFAULT_JMX_DOMAIN = "jgroups";
   
   private static final int CREATED = ServiceMBean.CREATED;
   private static final int STARTING = ServiceMBean.STARTING;
   private static final int STARTED = ServiceMBean.STARTED;
   private static final int STOPPING = ServiceMBean.STOPPING;
   private static final int STOPPED = ServiceMBean.STOPPED;
   private static final int DESTROYED = ServiceMBean.DESTROYED;
   private static final int FAILED = ServiceMBean.FAILED;
   
   private InetAddress nodeAddress;
   private String nodeName;
   private int namingServicePort = -1;
   private int state = ServiceMBean.UNREGISTERED;
   private boolean assignLogicalAddresses = true;
   private boolean manageNewThreadClassLoader = true;
   private boolean manageReleasedThreadClassLoader = false;
   private boolean addMissingSingletonName = true;
   private final ContextClassLoaderSwitcher classLoaderSwitcher;
   private final Map<Channel, ChannelInfo> registeredChannels = 
      new ConcurrentHashMap<Channel, ChannelInfo>(16, 0.75f, 2);
   
   private ChannelCloseListener closeListener = new ChannelCloseListener();

   /**
    * Map<String,ProtocolStackConfigurator>. Hashmap which maps stack names to JGroups
    * configurations. Keys are stack names, values are plain JGroups stack
    * configs. This is (re-)populated whenever a setMultiplexerConfig() method
    * is called
    */
   private final Map<String,ProtocolStackConfigInfo> stacks = 
      new ConcurrentHashMap<String, ProtocolStackConfigInfo>(16, 0.75f, 2);

   /** 
    * Placeholder for stacks injected via {@link #setProtocolStackConfigurations(Map)}
    * until createService is called.
    */
   private Map<String,ProtocolStackConfigInfo> injectedStacks;
   
   /**
    * The MBeanServer to expose JMX management data with (no management data
    * will be available if null)
    */
   private MBeanServer server = null;

   /** To expose the channels and protocols */
   private String domain = DEFAULT_JMX_DOMAIN;
   private boolean domainSet = false;

   /** Whether or not to expose channels via JMX */
   private boolean expose_channels=true;

   /** Whether to expose the factory only, or all protocols as well */
   private boolean expose_protocols=true;

   /**
    * Creates a new JChannelFactory.
    */
   @SuppressWarnings("unchecked")
   public JChannelFactory()
   {
      this.classLoaderSwitcher = (ContextClassLoaderSwitcher) AccessController.doPrivileged(ContextClassLoaderSwitcher.INSTANTIATOR);
   }   

   /**
    * Always throws <code>ChannelException</code>; this method is not supported.
    */   
   public Channel createChannel() throws ChannelException
   {
      throw new ChannelException("No-arg createChannel() is not supported");
   }

   /**
    * Creates a channel by passing <code>properties</code> to the 
    * <code>org.jgroups.JChannel</code> constructor.
    * 
    * @param properties protocol stack configuration object; can be <code>null</code>
    *                   in which case a default stack will be used
    * 
    * @return the channel
    */
   public Channel createChannel(Object properties) throws ChannelException
   {
      checkStarted();

      if (properties == null)
         properties = JChannel.DEFAULT_PROTOCOL_STACK;

      ProtocolStackConfigurator config = null;

      try
      {
         @SuppressWarnings("deprecation")
         ProtocolStackConfigurator c = ConfiguratorFactory.getStackConfigurator(properties);
         config = c;
      }
      catch (Exception x)
      {
         throw new ChannelException("unable to load protocol stack", x);
      }

      JChannel channel = initializeChannel(config, null, false);

      try
      {
         registerChannel(channel, null, null, ProtocolStackUtil.getProtocolData(config));
      }
      catch (ChannelException ce)
      {
         throw ce;
      }
      catch (Exception e)
      {
         throw new ChannelException("unable to register channel", e);
      }

      return channel;
   }

   /**
    * Create a {@link Channel} using the specified stack. Channel will use a 
    * shared transport.
    * 
    * @param stack_name 
    *            The name of the stack to be used. All stacks are defined in
    *            the configuration with which the factory is configured (see
    *            {@link #setMultiplexerConfig(Object)} for example. If
    *            clients attempt to create a Channel for an undefined stack 
    *            name an exception will be thrown.
    * 
    * @return an implementation of Channel configured with a shared transport.
    * 
    * @throws IllegalArgumentException if <code>stack_name</code> is 
    * <code>null</code> or {@link #getConfig(String)} returns <code>null</code>
    * when <code>stack_name</code> is used.
    * 
    * @throws Exception
    */
   public Channel createChannel(String stack_name) throws Exception
   {
      return createChannelFromRegisteredStack(stack_name, null, false);
   }
   
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
    * @param id  Only used if {@link #isExposeChannels()} returns <code>true</code>,
    *            in which case, if not <code>null</code>, is used as part of
    *            the <code>ObjectName</code> for the JMX mbeans that represent
    *            the channel and its protocols. Can be <code>null</code>.
    *            
    * @return an implementation of Channel configured with a shared transport.
    *         
    * @throws IllegalStateException if the specified protocol stack does not
    *                               declare a <code>singleton_name</code> and
    *                               {@link #getAddMissingSingletonName()} returns
    *                               <code>false</code>.
    * @throws ChannelException
    */
   public Channel createMultiplexerChannel(String stack_name, String id) throws Exception
   {
      return createChannelFromRegisteredStack(stack_name, id, true);
   }  
   
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
    *         
    * @throws IllegalStateException if the specified protocol stack does not
    *                               declare a <code>singleton_name</code> and
    *                               {@link #getAddMissingSingletonName()} returns
    *                               <code>false</code>.
    * @throws ChannelException
    */
   public Channel createMultiplexerChannel(String stack_name, String id, boolean register_for_state_transfer, String substate_id) throws Exception
   {
      return createMultiplexerChannel(stack_name, id);
   }
   
   /**
    * {@link #parse(Element) Parses <code>properties</code>} and then adds
    * the resulting protocol stack configurations to the set available for use.
    * Same as 
    * {@link #setMultiplexerConfig(Element, boolean) <code>setMultiplexerConfig(properties, true</code>}.
    * 
    * @param properties document root node for XML content in the JGroups 
    *                   <code>stacks.xml</code> format
    */
   public void setMultiplexerConfig(Element properties) throws Exception
   {
      setMultiplexerConfig(properties, true);
   }

   /**
    * {@link #parse(InputStream) Parses} an input stream created from 
    * <code>properties</code> and then adds the resulting protocol stack 
    * configurations to the set available for use. Same as 
    * {@link #setMultiplexerConfig(File, boolean) <code>setMultiplexerConfig(properties, true</code>}.
    * 
    * @param properties file which must contain XML content in the JGroups 
    *              <code>stacks.xml</code> format
    */
   public void setMultiplexerConfig(File properties) throws Exception
   {
      setMultiplexerConfig(properties, true);      
   }

   /**
    * {@link #parse(InputStream) Parses} an input stream created from 
    * <code>properties</code> and then adds the resulting protocol stack 
    * configurations to the set available for use. Same as 
    * {@link #setMultiplexerConfig(Object, boolean) <code>setMultiplexerConfig(properties, true</code>}.
    * 
    * @param properties object that can be {@link ConfiguratorFactory#getConfigStream(Object) converted into a stream}
    *                   which must contain XML content in the JGroups 
    *                   <code>stacks.xml</code> format
    */
   public void setMultiplexerConfig(Object properties) throws Exception
   {
      setMultiplexerConfig(properties, true);
   }

   /**
    * {@link #parse(InputStream) Parses} an input stream created from 
    * <code>properties</code> and then adds the resulting protocol stack 
    * configurations to the set available for use. Same as 
    * {@link #setMultiplexerConfig(String, boolean) <code>setMultiplexerConfig(properties, true</code>}.
    * 
    * @param properties string that can be {@link ConfiguratorFactory#getConfigStream(String) converted into a stream}
    *                   which must contain XML content in the JGroups 
    *                   <code>stacks.xml</code> format
    */
   public void setMultiplexerConfig(String properties) throws Exception
   {
      setMultiplexerConfig(properties, true);
   }

   /**
    * {@link #parse(InputStream) Parses} an input stream created from 
    * <code>properties</code> and then adds the resulting protocol stack 
    * configurations to the set available for use. Same as 
    * {@link #setMultiplexerConfig(URL, boolean) <code>setMultiplexerConfig(properties, true</code>}.
    * 
    * @param properties URL which must contain XML content in the JGroups 
    *              <code>stacks.xml</code> format
    */
   public void setMultiplexerConfig(URL properties) throws Exception
   {
      setMultiplexerConfig(properties, true);
   }

   // -------------------------------------------------------------  Properties

   /**
    * Gets the MBeanServer to use to register mbeans for channels and protocols
    * we create.
    * 
    * @return the MBeanServer, or <code>null</code> if one isn't registered
    */
   public MBeanServer getServer() 
   {
       return server;
   }

   /**
    * Sets the MBeanServer to use to register mbeans for channels and protocols
    * we create.
    * 
    * @param server the MBeanServer. May be <code>null</code>
    */
   public void setServer(MBeanServer server) 
   {
       this.server=server;
   }

   /**
    * Gets the domain portion of the JMX ObjectName to use when registering channels and protocols
    * 
    * @return the domain. Will not return <code>null</code> after {@link #create()}
    *         has been invoked.
    */
   @ManagementProperty(use={ViewUse.CONFIGURATION}, description="The domain portion of the JMX ObjectName to use when registering channels and protocols")
   public String getDomain() 
   {
       return domain == null ? "jgroups" : domain;
   }
   
   public void setDomain(String domain)
   {
      this.domain = domain;
      this.domainSet = true;
   }

   @ManagementProperty(use={ViewUse.CONFIGURATION}, description="Whether to expose channels we create via JMX")
   public boolean isExposeChannels() 
   {
       return expose_channels;
   }

   public void setExposeChannels(boolean expose_channels) 
   {
       this.expose_channels=expose_channels;
   }

   @ManagementProperty(use={ViewUse.CONFIGURATION}, description="Whether to expose protocols via JMX as well if we expose channels")
   public boolean isExposeProtocols() 
   {
       return expose_protocols;
   }

   public void setExposeProtocols(boolean expose_protocols) 
   {      
       this.expose_protocols=expose_protocols;
       if (expose_protocols)
          this.expose_channels=true;
   }

   /**
    * Get any logical name assigned to this server; if not null this value
    * will be the value of the 
    * {@link #setAssignLogicalAddresses(boolean) logical address} assigned
    * to the channels this factory creates.
    * 
    * @return the logical name for this server, or <code>null</code>.
    */
   @ManagementProperty(use={ViewUse.CONFIGURATION}, description="The cluster-unique logical name of this node")
   public String getNodeName()
   {
      return nodeName;
   }

   /**
    * Sets the logical name assigned to this server; if not null this value
    * will be the value of the 
    * {@link #setAssignLogicalAddresses(boolean) logical address} assigned
    * to the channels this factory creates.
    * 
    * @param nodeName the logical name for this server, or <code>null</code>.
    */
   public void setNodeName(String nodeName)
   {
      this.nodeName = nodeName;
   }
   
   /**
    * Gets the address to which this server is bound; typically the value
    * passed to <code>-b</code> when JBoss is started. Used in combination 
    * with {@link #getNamingServicePort() the naming service port} to create
    * a logical name for this server if no {@link #SetNodeName(String) node name}
    * is specified.
    * 
    * @return the address to which this server is bound, or <code>null</code>
    *         if not set
    */
   public InetAddress getNodeAddress()
   {
      return nodeAddress;
   }
   
   /**
    * Sets the address to which this server is bound; typically the value
    * passed to <code>-b</code> when JBoss is started. Used in combination 
    * with {@link #getNamingServicePort() the naming service port} to create
    * a logical name for this server if no {@link #SetNodeName(String) node name}
    * is specified.
    * 
    * @param nodeAddress the address to which this server is bound, 
    *                    or <code>null</code>
    */
   public void setNodeAddress(InetAddress nodeAddress)
   {
      this.nodeAddress = nodeAddress;
   }

   /**
    * Gets the port on which this server's naming service is listening. Used in 
    * combination with {@link #getNodeAddress() the server bind address} to create
    * a logical name for this server if no {@link #SetNodeName(String) node name}
    * is specified.
    * 
    * @return the port on which JNDI is listening, or <code>-1</code> if not set.
    */
   public int getNamingServicePort()
   {
      return namingServicePort;
   }

   /**
    * Sets the port on which this server's naming service is listening. Used in 
    * combination with {@link #getNodeAddress() the server bind address} to create
    * a logical name for this server if no {@link #SetNodeName(String) node name}
    * is specified.
    * 
    * @param jndiPort the port on which JNDI is listening.
    */
   public void setNamingServicePort(int jndiPort)
   {
      this.namingServicePort = jndiPort;
   }
   
   /**
    * Gets whether this factory should create a "logical address" (or use
    * one set via {@link #setNodeName(String)} and assign it to
    * any newly created <code>Channel</code> as JGroups "additional_data".
    * 
    * @see #setAssignLogicalAddresses(boolean)
    */
   @ManagementProperty(use={ViewUse.CONFIGURATION}, description="Whether this factory should assign a logical address for this node to all channels")
   public boolean getAssignLogicalAddresses()
   {
      return assignLogicalAddresses;
   }

   /**
    * Sets whether this factory should create a "logical address" (or use
    * one set via {@link #setNodeName(String)} and assign it to
    * any newly created <code>Channel</code> as JGroups "additional_data".
    * <p>
    * Any such logical address will be used by <code>HAPartition</code>
    * to assign a name to the <code>ClusterNode</code> object representing 
    * this node. If a logical address is not set, the <code>ClusterNode</code> 
    * will use the address and port JGroups is using to receive messages to
    * create its name.
    * </p>
    * <p>
    * Default is <code>true</code>.
    * </p>
    */
   public void setAssignLogicalAddresses(boolean logicalAddresses)
   {
      this.assignLogicalAddresses = logicalAddresses;
   }

   /**
    * Gets whether this factory should update the standard JGroups
    * thread factories to ensure application classloaders do not leak to 
    * newly created channel threads.
    * 
    * @return <code>true</code> if the factories should be updated.
    *         Default is <code>true</code>.
    */
   @ManagementProperty(use={ViewUse.CONFIGURATION}, description="Whether this factory should update the standard JGroups thread factories to ensure classloader leaks do not occur")
   public boolean getManageNewThreadClassLoader()
   {
      return manageNewThreadClassLoader;
   }

   /**
    * Sets whether this factory should update the standard JGroups
    * thread factories to ensure application classloaders do not leak to 
    * newly created channel threads. This should only be set to <code>false</code>
    * if a JGroups release is used that itself prevents such classloader leaks.
    * 
    * @param manage <code>true</code> if the factories should be updated.
    */
   public void setManageNewThreadClassLoader(boolean manage)
   {
      this.manageNewThreadClassLoader = manage;
   }

   /**
    * Gets whether this factory should update the standard JGroups
    * thread pools to ensure application classloaders have not leaked to 
    * threads returned to the pool.
    * 
    * @return <code>true</code> if the pools should be updated.
    *         Default is <code>false</code>.
    */
   @ManagementProperty(use={ViewUse.CONFIGURATION}, description="Whether this factory should update the standard JGroups thread pools to ensure classloader leaks do not occur")
   public boolean getManageReleasedThreadClassLoader()
   {
      return manageReleasedThreadClassLoader;
   }

   /**
    * Sets whether this factory should update the standard JGroups
    * thread pools to ensure application classloaders have not leaked to 
    * threads returned to the pool.
    * <p>
    * There is a small performance cost to enabling this, and applications
    * can prevent any need to enable it by properly restoring the thread
    * context classloader if they change it.  Therefore, by default this
    * is set to <code>false</code>.
    * </p>
    * 
    * @param manage <code>true</code> if the factories should be updated.
    */
   public void setManageReleasedThreadClassLoader(boolean manage)
   {
      this.manageReleasedThreadClassLoader = manage;
   }

   /**
    * Gets whether {@link #createMultiplexerChannel(String, String)} should 
    * create a synthetic singleton name attribute for a channel's transport
    * protocol if one isn't configured.  If this is <code>false</code> and
    * no <code>singleton_name</code> is configured, 
    * {@link #createMultiplexerChannel(String, String)} will throw an
    * <code>IllegalStateException</code>. 
    * 
    * @return <code>true</code> if synthetic singleton names should be created.
    *         Default is <code>true</code>.
    */
   @ManagementProperty(use={ViewUse.CONFIGURATION}, 
         description="Whether this factory should create a synthetic singleton name attribute for a channel's transport protocol if one isn't configured")
   public boolean getAddMissingSingletonName()
   {
      return addMissingSingletonName;
   }

   /**
    * Sets whether {@link #createMultiplexerChannel(String, String)} should 
    * create a synthetic singleton name attribute for a channel's transport
    * protocol if one isn't configured.
    * 
    * @param addMissingSingletonName <code>true</code> if synthetic singleton 
    *                                names should be created.
    */
   public void setAddMissingSingletonName(boolean addMissingSingletonName)
   {
      this.addMissingSingletonName = addMissingSingletonName;
   }
   
   // -------------------------------------------------------------  Public

   /**
    * {@link #parse(Element) Parses <code>properties</code>} and then adds
    * the resulting protocol stack configurations to the set available for use.
    * 
    * @param properties document root node for XML content in the JGroups 
    *                   <code>stacks.xml</code> format
    * @param replace <code>true</code> if a configuration with the same
    *                stack name as an already registered configuration should
    *                replace that configuration; <code>false</code> if it
    *                should be discarded.
    */
   public void setMultiplexerConfig(Element properties, boolean replace) throws Exception
   {
      Map<String, ProtocolStackConfigInfo> map = ProtocolStackUtil.parse(properties);
      
      for (Map.Entry<String, ProtocolStackConfigInfo> entry : map.entrySet())
      {
         addConfig(entry.getKey(), entry.getValue(), replace);
      }
   }

   /**
    * {@link #parse(InputStream) Parses} an input stream created from 
    * <code>properties</code> and then adds the resulting protocol stack 
    * configurations to the set available for use.
    * 
    * @param properties file which must contain XML content in the JGroups 
    *              <code>stacks.xml</code> format
    * @param replace <code>true</code> if a configuration with the same
    *                stack name as an already registered configuration should
    *                replace that configuration; <code>false</code> if it
    *                should be discarded.
    */
   public void setMultiplexerConfig(File properties, boolean replace) throws Exception
   {
      InputStream input=ConfiguratorFactory.getConfigStream(properties);      
      addConfigs(input, properties.toString(), replace);
   }

   /**
    * {@link #parse(InputStream) Parses} an input stream created from 
    * <code>properties</code> and then adds the resulting protocol stack 
    * configurations to the set available for use. 
    * 
    * @param properties object that can be {@link ConfiguratorFactory#getConfigStream(Object) converted into a stream}
    *                   which must contain XML content in the JGroups 
    *                   <code>stacks.xml</code> format
    * @param replace <code>true</code> if a configuration with the same
    *                stack name as an already registered configuration should
    *                replace that configuration; <code>false</code> if it
    *                should be discarded.
    */
   public void setMultiplexerConfig(Object properties, boolean replace) throws Exception
   {
      InputStream input=ConfiguratorFactory.getConfigStream(properties);
      addConfigs(input, properties.toString(), replace);   
   }

   /**
    * {@link #parse(InputStream) Parses} an input stream created from 
    * <code>properties</code> and then adds the resulting protocol stack 
    * configurations to the set available for use.
    * 
    * @param properties string that can be {@link ConfiguratorFactory#getConfigStream(String) converted into a stream}
    *                   which must contain XML content in the JGroups 
    *                   <code>stacks.xml</code> format
    * @param replace <code>true</code> if a configuration with the same
    *                stack name as an already registered configuration should
    *                replace that configuration; <code>false</code> if it
    *                should be discarded.
    */
   public void setMultiplexerConfig(String properties, boolean replace) throws Exception
   {
      InputStream input=ConfiguratorFactory.getConfigStream(properties);      
      addConfigs(input, properties, replace);
   }

   /**
    * {@link #parse(InputStream) Parses} an input stream created from 
    * <code>properties</code> and then adds the resulting protocol stack 
    * configurations to the set available for use.
    * 
    * @param properties URL which must contain XML content in the JGroups 
    *              <code>stacks.xml</code> format
    * @param replace <code>true</code> if a configuration with the same
    *                stack name as an already registered configuration should
    *                replace that configuration; <code>false</code> if it
    *                should be discarded.
    */
   public void setMultiplexerConfig(URL url, boolean replace) throws Exception
   {
      InputStream input=ConfiguratorFactory.getConfigStream(url);      
      addConfigs(input, url.toString(), replace);
   }
   

   // --------------------------------------------------------  Management View
   
   /**
    * Gets information on channels created by this factory that are currently
    * open.
    */
   @ManagementProperty(use={ViewUse.STATISTIC}, 
         description="Information on channels created by this factory that are currently open",
         readOnly=true)
   @MetaMapping(value=OpenChannelsMapper.class)
   public Set<ChannelInfo> getOpenChannels()
   {
      Set<ChannelInfo> openChannels = new HashSet<ChannelInfo>();
      for ( ChannelInfo channel : registeredChannels.values() )
      {  
         if ( channel.getChannel().isOpen() )
            openChannels.add ( channel );
      }
      return openChannels;

   }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION, ViewUse.RUNTIME}, 
         description="Protocol stack configurations available for use")
   @MetaMapping(value=ProtocolStackConfigurationsMapper.class)
   public Map<String, ProtocolStackConfigInfo> getProtocolStackConfigurations()
   {
      return Collections.unmodifiableMap(stacks);
   }
   
   public void setProtocolStackConfigurations(Map<String, ProtocolStackConfigInfo> configs)
   {
      this.injectedStacks = configs;      
      
      if (state == STARTED)
      {
         // We're already running so this must be a ManagedComponent update
         // so apply immediately
         processInjectedStacks();
      }
   }

   // ---------------------------------------------------  JChannelFactoryMBean

   /**
    * {@inheritDoc}
    */
   public void clearConfigurations()
   {
      this.stacks.clear();
   }

   /**
    * {@inheritDoc}
    */
   public String dumpChannels()
   {
      return "";
   }

   /**
    * {@inheritDoc}
    */
   public String dumpConfiguration()
   {
      return stacks.keySet().toString();
   }

   /**
    * {@inheritDoc}
    */
   public String getConfig(String stack_name) throws Exception
   {
      ProtocolStackConfigInfo cfg = stacks.get(stack_name);
      if (cfg == null)
         throw new Exception("stack \"" + stack_name + "\" not found in " + stacks.keySet());
      return cfg.getConfigurator().getProtocolStackString();
   }

   /**
    * {@inheritDoc}
    */
   public String getMultiplexerConfig()
   {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<String, ProtocolStackConfigInfo> entry : stacks.entrySet())
      {
         sb.append(entry.getKey()).append(": ").append(entry.getValue().getConfigurator().getProtocolStackString()).append("\n");
      }
      return sb.toString();
   }

   /**
    * {@inheritDoc}
    */
   public boolean removeConfig(String stack_name)
   {
      return stack_name != null && this.stacks.remove(stack_name) != null;
   }
   
   // -------------------------------------------------------------  Lifecycle

   /**
    * {@inheritDoc}
    * <p>
    * This method largely directly concerns itself with the {@link #getStateString() state}
    * field, delegating the real work to {@link #createService()}.
    * </p>
    */
   public void create() throws Exception
   {

      if (state == CREATED || state == STARTING || state == STARTED
         || state == STOPPING || state == STOPPED)
      {
         log.debug("Ignoring create call; current state is " + getStateString());
         return;
      }
      
      log.debug("Creating JChannelFactory");
      
      try
      {
         createService();
         state = CREATED;
      }
      catch (Exception e)
      {
         log.debug("Initialization failed JChannelFactory", e);
         throw e;
      }
      
      log.debug("Created JChannelFactory");
   }

   /**
    * {@inheritDoc}
    * <p>
    * This method largely directly concerns itself with the {@link #getStateString() state}
    * field, delegating the real work to {@link #startService()}.
    * </p>
    */
   public void start() throws Exception
   {
      if (state == STARTING || state == STARTED || state == STOPPING)
      {
         log.debug("Ignoring start call; current state is " + getStateString());
         return;
      }
      
      if (state != CREATED && state != STOPPED && state != FAILED)
      {
         log.debug("Start requested before create, calling create now");         
         create();
      }
      
      state = STARTING;
      log.debug("Starting JChannelFactory");

      try
      {
         startService();
      }
      catch (Exception e)
      {
         state = FAILED;
         log.debug("Starting failed JChannelFactory", e);
         throw e;
      }

      state = STARTED;
      log.debug("Started JChannelFactory");
      
   }

   /**
    * {@inheritDoc}
    * <p>
    * This method largely directly concerns itself with the {@link #getStateString() state}
    * field, delegating the real work to {@link #stopService()}.
    * </p>
    */
   public void stop()
   {
      if (state != STARTED)
      {
         log.debug("Ignoring stop call; current state is " + getStateString());
         return;
      }
      
      state = STOPPING;
      log.debug("Stopping JChannelFactory");

      try
      {
         stopService();
      }
      catch (Throwable e)
      {
         state = FAILED;
         log.warn("Stopping failed JChannelFactory", e);
         return;
      }
      
      state = STOPPED;
      log.debug("Stopped JChannelFactory");
   }

   /**
    * {@inheritDoc}
    * <p>
    * This method largely directly concerns itself with the {@link #getStateString() state}
    * field, delegating the real work to {@link #destroyService()}.
    * </p>
    */
   public void destroy()
   {
      if (state == DESTROYED)
      {
         log.debug("Ignoring destroy call; current state is " + getStateString());
         return;
      }
      
      if (state == STARTED)
      {
         log.debug("Destroy requested before stop, calling stop now");
         stop();
      }
      
      log.debug("Destroying JChannelFactory");
      
      try
      {         
         destroyService();
      }
      catch (Throwable t)
      {
         log.warn("Destroying failed JChannelFactory", t);
      }
      state = DESTROYED;
      log.debug("Destroyed JChannelFactory");
   }

   // ------------------------------------------------------- MBeanRegistration

   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      setServer(server);
      if (!this.domainSet || this.domain == null)
      {
         setDomain(name.getDomain());
      }
      return name;
   }

   public void postRegister(Boolean registrationDone)
   {
      if (registrationDone != null && registrationDone.booleanValue()
            && state == ServiceMBean.UNREGISTERED)
      {
         state = ServiceMBean.REGISTERED;
      }
   }

   public void preDeregister() throws Exception
   { 
   }

   public void postDeregister()
   { 
      setServer(null);
      if (state == ServiceMBean.DESTROYED)
         state = ServiceMBean.UNREGISTERED;
   }

   // --------------------------------------------------------------- Protected
   
   /**
    * Gets the classloader that channel threads should be set to if
    * {@link #getManageNewThreadClassloader()} or {@link #getManageReleasedThreadClassLoader()}
    * are <code>true</code>.
    * <p>
    * This implementation returns this class' classloader.
    * 
    * @return the classloader.
    */
   protected ClassLoader getDefaultChannelThreadContextClassLoader()
   {
      return getClass().getClassLoader();
   }

   protected void createService() throws Exception
   {
      if(expose_channels) 
      {
         if(server == null)
         {
            throw new Exception("No MBeanServer found; JChannelFactory needs to " +
            		"be run with an MBeanServer present, or with ExposeChannels " +
            		"set to false");
         }
         
         if(domain == null)
         {
            domain= DEFAULT_JMX_DOMAIN;
         }
      }
   }

   /**
    * The actual startup work.
    * 
    * @throws Exception
    */
   protected void startService() throws Exception
   {
      // If the ProfileService injected stacks, process them now
      processInjectedStacks();
   }

   /**
    * The actual service stop work. This base implementation does nothing.
    * 
    * @throws Exception
    */
   protected void stopService() throws Exception
   {
      // no-op
   }

   /**
    * The actual service destruction work.
    *
    */
   protected void destroyService()
   {  
      for (Channel ch : registeredChannels.keySet())
      {
         unregisterChannel(ch);
      }
   }

   // ----------------------------------------------------------------- Private


   private void checkStarted()
   {
      if (state != ServiceMBean.STARTED)
         throw new IllegalStateException("Cannot use factory; state is " + getStateString());
   }
   
   private void addConfigs(InputStream input, String source, boolean replace) throws Exception
   {
      if(input == null)
      {
         throw new FileNotFoundException(source);
      }
      
      Map<String, ProtocolStackConfigInfo> map = null;
      try 
      {
          map = ProtocolStackUtil.parse(input);
      }
      catch(Exception ex) 
      {
          throw new Exception("failed parsing " + source, ex);
      }
      finally 
      {
          Util.close(input);
      }
      
      for (Map.Entry<String, ProtocolStackConfigInfo> entry : map.entrySet())
      {
         addConfig(entry.getKey(), entry.getValue(), replace);
      }
   }

   private boolean addConfig(String st_name, ProtocolStackConfigInfo val, boolean replace)
   {
      boolean added = replace;
      if (replace)
      {
         stacks.put(st_name, val);
         if (log.isTraceEnabled())
            log.trace("added config '" + st_name + "'");
      }
      else
      {
         if (!stacks.containsKey(st_name))
         {
            stacks.put(st_name, val);
            if (log.isTraceEnabled())
               log.trace("added config '" + st_name + "'");
            added = true;
         }
         else
         {
            if (log.isTraceEnabled())
               log.trace("didn't add config '" + st_name + " because one of the same name already existed");
         }
      }
      return added;      
   }
   
   private synchronized void processInjectedStacks()
   {
      if (injectedStacks != null)
      {
         clearConfigurations();
         stacks.putAll(injectedStacks);
         injectedStacks = null;
      }      
   }

   /**
    * Creates a channel from one of the known stack configurations.
    * 
    * @param stack_name the name of the stack config
    * @param id optional id for the channel
    * @param forceSingletonStack <code>true</code> if a singleton_name must be
    *             either configured, or addMissingSingletonName must be true
    *             
    * @return the channel
    * 
    * @throws IllegalArgumentException if stack_name is unknown
    * @throws IllegalStateException if forceSingletonStack is <code>true</code>
    *                               but a singleton_name couldn't be configured
    *           
    * @throws Exception
    */
   private Channel createChannelFromRegisteredStack(String stack_name, String id, boolean forceSingletonStack) throws Exception
   {
      checkStarted();
      
      ProtocolStackConfigInfo config = stacks.get(stack_name);
      
      if (config == null)
         throw new IllegalArgumentException("Unknown stack_name " + stack_name);
      
      JChannel channel = initializeChannel(config.getConfigurator(), stack_name, forceSingletonStack);
      
      registerChannel(channel, id, stack_name, ProtocolStackUtil.getProtocolData(config.getConfigurator()));      
      
      return channel;
   }

   /**
    * Construct a JChannel from the given config and then do post-construction
    * processing like fixing up thread managment or setting a unique id.
    * 
    * @param config the config
    * 
    * @return the channel
    * 
    * @throws ChannelException
    */
   private JChannel initializeChannel(ProtocolStackConfigurator config, String stack_name,
         boolean forceSingletonStack) throws ChannelException
   {  
      Map<String, String> tpProps = getTransportProperties(config);
   
      if (!tpProps.containsKey(Global.SINGLETON_NAME))
      {
         if (addMissingSingletonName && stack_name != null)
         {
            String singletonName = UNSHARED_TRANSPORT_NAME_BASE + stack_name;
            
            log.warn("Config for " + stack_name + " does not include " +
                      "singleton_name; adding a name of " + singletonName +
                      ". You should configure a singleton_name for this stack.");
            
            config = addSingletonName(config, singletonName);
            log.debug("Stack config after adding singleton_name is " + config.getProtocolStackString());
            tpProps = getTransportProperties(config);                       
         }
         else if (forceSingletonStack)
         {
            throw new IllegalStateException("Config for " + stack_name + " does not include " +
                      "singleton_name and MuxChannels are not supported.");
         }
      }
      JChannel channel = new JChannel(config);
      
      if (manageNewThreadClassLoader || manageReleasedThreadClassLoader)
      {
         fixChannelThreadManagement(channel);
      }
      
      if (assignLogicalAddresses)
      {
         setChannelUniqueId(channel);
      }
      
      return channel;
   }
   
   /**
    * Gets the current runtime lifecycle state (e.g. CREATED, STARTED).
    */
   private String getStateString()
   {
      return ServiceMBean.states[state];
   }
   
   private void setChannelUniqueId(Channel channel)
   {
      IpAddress address = (IpAddress) channel.getLocalAddress();
      if (address == null)
      {
         // We push the independent name in the protocol stack before connecting to the cluster
         if (this.nodeName == null || "".equals(this.nodeName)) {
            this.nodeName = generateUniqueNodeName();
         }
         
         log.debug("Passing unique node id " + nodeName + " to the channel as additional data");
         
         HashMap<String, byte[]> staticNodeName = new HashMap<String, byte[]>();
         staticNodeName.put("additional_data", this.nodeName.getBytes());
         channel.down(new Event(Event.CONFIG, staticNodeName));
         
      }
      else if (address.getAdditionalData() == null)
      {
         if (channel.isConnected())
         {
            throw new IllegalStateException("Underlying JChannel was " +
                    "connected before additional_data was set");
         }
      }
      else if (this.nodeName == null || "".equals(this.nodeName))
      {         
         this.nodeName = new String(address.getAdditionalData());
         log.warn("Field nodeName was not set but mux channel already had " +
                "additional data -- setting nodeName to " + nodeName);
      }
   }

   private String generateUniqueNodeName ()
   {
      // we first try to find a simple meaningful name:
      // 1st) "local-IP:JNDI_PORT" if JNDI is running on this machine
      // 2nd) "local-IP:JMV_GUID" otherwise
      // 3rd) return a fully GUID-based representation
      //

      // Before anything we determine the local host IP (and NOT name as this could be
      // resolved differently by other nodes...)

      // But use the specified node address for multi-homing

      String hostIP = null;
      InetAddress address = ServerConfigUtil.fixRemoteAddress(nodeAddress);
      if (address == null)
      {
         log.debug ("unable to create a GUID for this cluster, check network configuration is correctly setup (getLocalHost has returned an exception)");
         log.debug ("using a full GUID strategy");
         return new VMID().toString();
      }
      else
      {
         hostIP = address.getHostAddress();
      }

      // 1st: is JNDI up and running?
      //
      if (namingServicePort > 0)
      {
         // we can proceed with the JNDI trick!
         return hostIP + ":" + namingServicePort;
      }
      else
      {
         log.warn("JNDI has been found but the service wasn't started. Most likely, " +
         		    "HAPartition bean is missing dependency on JBoss Naming. " +
         		    "Instead using host based UID strategy for defining a node " +
         		    "GUID for the cluster.");
      }

      // 2nd: host-GUID strategy
      //
      String uid = new UID().toString();
      return hostIP + ":" + uid;
   }
   
   private Map<String, String> getTransportProperties(ProtocolStackConfigurator config)
   {
      Map<String, String> tpProps = null;
      ProtocolData[] protocols= ProtocolStackUtil.getProtocolData(config);
      ProtocolData transport=protocols[0];
      @SuppressWarnings("unchecked")
      Map<String,ProtocolParameter> tmp=transport.getParameters();
      tpProps = new HashMap<String,String>();
      for(Map.Entry<String,ProtocolParameter> entry: tmp.entrySet())
      {
          tpProps.put(entry.getKey(), entry.getValue().getValue());
      }
      
      return tpProps;
   }
  
   private ProtocolStackConfigurator addSingletonName(ProtocolStackConfigurator orig, String singletonName)
      throws ChannelException
   {
      ProtocolStackConfigurator result = null;
      try
      {
         ProtocolData[] protocols=orig.getProtocolStack();
         ProtocolData transport=protocols[0];
         ProtocolParameter singletonParam = new ProtocolParameter(Global.SINGLETON_NAME, singletonName);
         transport.override(new ProtocolParameter[]{ singletonParam});
         result = orig;
      }
      catch (UnsupportedOperationException uoe)
      {
         // JGroups version hasn't implemented ProtocolStackConfigurator.getProtocolStack()
         // So we do things manually via string manipulation         
         String config = orig.getProtocolStackString();
         int idx = config.indexOf('(') + 1;
         StringBuilder builder = new StringBuilder(config.substring(0, idx));
         builder.append(Global.SINGLETON_NAME);
         builder.append('=');
         builder.append(singletonName);
         builder.append(';');
         builder.append(config.substring(idx));
         
         result = ConfiguratorFactory.getStackConfigurator(builder.toString());
      }
      
      return result;
   }
   
   private void fixChannelThreadManagement(Channel channel) throws ChannelException
   {
      if (!(channel instanceof JChannel))
      {
         log.debug("Cannot fix thread pools for unknown Channel type " + channel.getClass().getName());
         return;
      }
      
      JChannel jchannel = (JChannel) channel;
      
      ProtocolStack stack = jchannel.getProtocolStack();
      List<Protocol> protocols = stack.getProtocols();
      TP tp = null;
      for (int i = protocols.size() - 1; i >= 0; i--)
      {
         if (protocols.get(i) instanceof TP)
         {
            tp = (TP) protocols.get(i);
            break;
         }
      }
      
      ClassLoader defaultTCCL = getDefaultChannelThreadContextClassLoader();
      ThreadDecoratorImpl threadDecorator = new ThreadDecoratorImpl(defaultTCCL);
      if (manageNewThreadClassLoader)
      {
         fixProtocolThreadFactories(tp, threadDecorator);
      }
      
      if (manageReleasedThreadClassLoader)
      {
         fixTransportThreadPools(tp, threadDecorator);
      }
   }

   private void fixProtocolThreadFactories(TP tp, ThreadDecoratorImpl threadDecorator)
   {
      ThreadFactory stackFactory = tp.getThreadFactory();
      if (stackFactory == null)
      {
         stackFactory = new DefaultThreadFactory(Util.getGlobalThreadGroup(), "", false);
         tp.setThreadFactory(stackFactory);
      }
      fixThreadManager(stackFactory, threadDecorator, "TP.getThreadFactory()");
      
      log.debug("Fixed thread factory for " + tp);
      
      ThreadFactory timerFactory = tp.getTimerThreadFactory();
      if (timerFactory == null)
      {
         timerFactory = new LazyThreadFactory(Util.getGlobalThreadGroup(), "Timer", true, true);
         tp.setTimerThreadFactory(timerFactory);            
      }
      fixThreadManager(timerFactory, threadDecorator, "TP.getTimerThreadFactory()");
      
      log.debug("Fixed timer thread factory for " + tp);
      
      ThreadGroup pool_thread_group = null;
      if (tp.isDefaulThreadPoolEnabled())
      {
         ThreadFactory defaultPoolFactory = tp.getDefaultThreadPoolThreadFactory();
         if (defaultPoolFactory == null)
         {
            pool_thread_group=new ThreadGroup(Util.getGlobalThreadGroup(), "Thread Pools");
            defaultPoolFactory = new DefaultThreadFactory(pool_thread_group, "Incoming", false, true);
            tp.setThreadFactory(defaultPoolFactory);
         }
         fixThreadManager(defaultPoolFactory, threadDecorator, "TP.getDefaultThreadPoolThreadFactory()");
         
         log.debug("Fixed default pool thread factory for " + tp);
      }
      
      if (tp.isOOBThreadPoolEnabled())
      {
         ThreadFactory oobPoolFactory = tp.getOOBThreadPoolThreadFactory();
         if (oobPoolFactory == null)
         {
            if (pool_thread_group == null)
               pool_thread_group=new ThreadGroup(Util.getGlobalThreadGroup(), "Thread Pools");
            oobPoolFactory = new DefaultThreadFactory(pool_thread_group, "OOB", false, true);
            tp.setThreadFactory(oobPoolFactory);
         }
         fixThreadManager(oobPoolFactory, threadDecorator, "TP.getOOBThreadPoolThreadFactory()");
         
         log.debug("Fixed oob pool thread factory for " + tp);
      }
      
      Map<ThreadFactory, Protocol> factories= new HashMap<ThreadFactory, Protocol>();
      Protocol tmp=tp.getUpProtocol();
      while(tmp != null) {
        ThreadFactory f=tmp.getThreadFactory();
         if(f != null && !factories.containsKey(f))
         {
            factories.put(f, tmp);
         }
         tmp=tmp.getUpProtocol();
      }
      
      for (Map.Entry<ThreadFactory, Protocol> entry : factories.entrySet())
      {
         fixThreadManager(entry.getKey(), threadDecorator, entry.getValue().getClass().getSimpleName() + ".getThreadFactory()");
      }
      
      log.debug("Fixed Protocol thread factories");
   }

   private void fixTransportThreadPools(TP tp, ThreadDecoratorImpl threadDecorator)
   {
      Executor threadPool = tp.getDefaultThreadPool();
      if (tp.isDefaulThreadPoolEnabled())
      {
         fixThreadManager(threadPool, threadDecorator, "TP.getDefaultThreadPool()");
         
         log.debug("Fixed default thread pool for " + tp);
      }
      
      threadPool = tp.getOOBThreadPool();
      if (tp.isOOBThreadPoolEnabled())
      {
         fixThreadManager(threadPool, threadDecorator, "TP.getOOBThreadPool()"); 
         
         log.debug("Fixed OOB thread pool for " + tp);
      }
   }
   
   private void fixThreadManager(Object manager, ThreadDecoratorImpl decorator, String managerSource)
   {
      if (manager instanceof ThreadManager)
      {
         ThreadManager threadManager = (ThreadManager) manager;
         
         ThreadDecorator existing = threadManager.getThreadDecorator();
         if (existing instanceof ThreadDecoratorImpl)
         {
            // already been handled
            return;
         }
         else if (existing != null)
         {
            // someone else has added one; integrate with it
            decorator.setParent(existing);
         }
         threadManager.setThreadDecorator(decorator);
      }
      else
      {
         log.warn(managerSource + " is not a ThreadManager");
      }
   }
   
   /** 
    * Sets the context class loader on <code>thread</code> to the classloader
    * in effect when this factory was instantiated.
    * 
    * @param thread the thread to set
    */
   private void setDefaultThreadContextClassLoader(Thread thread, ClassLoader classLoader)
   {
      classLoaderSwitcher.setContextClassLoader(thread, classLoader);
   }
   
   private void registerChannel(JChannel ch, String channelId, String stackName, ProtocolData[] config) throws Exception
   {
      // Register for channel closed notification so we can unregister
      ch.addChannelListener(closeListener);
      
      ObjectName chName = null;
      List<ObjectName> protNames = null;
      List<ObjectName> allNames = registerInJmx(ch, channelId);
      if (allNames != null && allNames.size() > 0)
      {
         chName = allNames.get(0);
         if (allNames.size() > 1)
         {
            protNames = allNames.subList(1, allNames.size());
         }
      }
      
      ChannelInfo info = new ChannelInfo(channelId, stackName, ch, config, chName, protNames);
      registeredChannels.put(ch, info);
   }
   
   private List<ObjectName> registerInJmx(JChannel ch, String channelId) throws Exception 
   {
      List<ObjectName> allNames = null;
      
      if(isExposeChannels() && getServer() != null && channelId != null && channelId.length() > 0)
      {
         ObjectName channelName = new ObjectName(getDomain() + ":type=channel,cluster=" + channelId);
         if (!getServer().isRegistered(channelName))
         {
            allNames = new ArrayList<ObjectName>();
            getServer().registerMBean(new org.jgroups.jmx.JChannel(ch), channelName);
            allNames.add(channelName);
            if (isExposeProtocols())
            {
               String baseName = getDomain() + ":type=protocol,cluster=" + channelId;
               ProtocolStack stack=ch.getProtocolStack();
               List<Protocol> protocols=stack.getProtocols();
            
               for(Protocol prot : protocols)
               {
                   // Don't register the fake protocol used by singleton transports
                   if ( prot instanceof TP.ProtocolAdapter )
                       continue;

                   org.jgroups.jmx.Protocol p=null;
                   try {
                      String prot_name = prot.getClass().getName();
                      String clname = prot_name.replaceFirst("org.jgroups.", "org.jgroups.jmx.");
                      Class<?> cl = Util.loadClass(clname, JmxConfigurator.class);
                      if (cl != null)
                      {
                         p = (org.jgroups.jmx.Protocol) cl.newInstance();
                      }
                   }
                   catch(ClassNotFoundException e) 
                   {
                      // ignore;
                   }
                   catch(Throwable e) {
                       log.error("failed creating a JMX wrapper instance for " + prot, e);
                       p = null;
                   }
                   if(p == null)
                   {
                      // Use default
                      p = new org.jgroups.jmx.Protocol(prot);
                   }
                   else
                   {
                      p.attachProtocol(prot);
                   }
                   ObjectName prot_name=new ObjectName(baseName + ",protocol=" + prot.getName());
                   server.registerMBean(p, prot_name);
                   allNames.add(prot_name);
               }
            }
         }
      }
      
      return allNames;
   }
   
   private void unregisterChannel(Channel ch)
   {
      ChannelInfo info = registeredChannels.remove(ch);
      if (info == null)
      {
         log.warn("Unknown channel " + ch.getClusterName());
      }
      else
      {
         unregisterFromJmx(info);
      }
      
      ch.removeChannelListener(closeListener);
   }
   
   private void unregisterFromJmx(ChannelInfo info) 
   {
      ObjectName oname = info.getChannelObjectName();
      MBeanServer mbs = getServer();
      if(info != null && mbs != null && getServer().isRegistered(oname))
      {
         try
         {
            mbs.unregisterMBean(oname);
         }
         catch(Exception e)
         {
            log.error("failed unregistering " + oname, e);
         }
         
         List<ObjectName> onames = info.getProtocolObjectNames();
         if (onames != null)
         {
            for (ObjectName protName : onames)
            {
               try
               {
                  mbs.unregisterMBean(protName);
               }
               catch(Exception e)
               {
                  log.error("failed unregistering " + protName, e);
               }
               
            }
         }
      }
   }
   
   private class ThreadDecoratorImpl implements ThreadDecorator
   {
      private final ClassLoader classloader;
      private ThreadDecorator parent;
      
      private ThreadDecoratorImpl(ClassLoader classloader)
      {
         this.classloader = classloader;
      }

      public void threadCreated(Thread thread)
      {
         if (parent != null)
            parent.threadCreated(thread);
         setDefaultThreadContextClassLoader(thread, classloader);
      }

      public void threadReleased(Thread thread)
      {
         if (parent != null)
            parent.threadCreated(thread);
         setDefaultThreadContextClassLoader(thread, classloader);
      }

      public ThreadDecorator getParent()
      {
         return parent;
      }

      public void setParent(ThreadDecorator parent)
      {
         this.parent = parent;
      }
      
   }

   private class ChannelCloseListener extends ChannelListenerAdapter
   {
      public void channelClosed(Channel channel) 
      {
         ChannelInfo info = registeredChannels.get(channel);
         if(info != null)
            unregisterFromJmx(info);
      }            

      public void channelConnected(Channel channel) 
      {
         ChannelInfo info = registeredChannels.get(channel);

         try 
         {
            if(info != null)
               registerInJmx((JChannel)channel, info.getId());
         }
         catch ( Exception e )
         {
            log.error("failed re-registering channel in JMX", e);
         }
      }
   }
   
}
