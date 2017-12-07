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
package org.jboss.mx.interceptor;

import java.util.Hashtable;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.jboss.logging.Logger;
import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.modelmbean.RequiredModelMBeanInstantiator;
import org.jboss.mx.server.Invocation;
import org.jboss.mx.service.ServiceConstants;
import org.jboss.mx.util.AgentID;


/**
 * Base class for shared interceptors. This class provides some default method
 * implementations for shared interceptors.
 *
 * @see org.jboss.mx.interceptor.SharedInterceptor
 * @see org.jboss.mx.server.MBeanInvoker
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81026 $   
 */
public abstract class AbstractSharedInterceptor 
   extends AbstractInterceptor
   implements SharedInterceptor
{

   /**
    * MBean server reference for shared interceptors.
    */
   protected MBeanServer server     = null;
   
   /**
    * Object name of this interceptor. Shared interceptors must always contain
    * a valid object name.
    */
   protected ObjectName objectName  = null;
   
   
   // Constructors --------------------------------------------------
   
   /**
    * Constructs a new shared interceptor instance. The interceptor is not
    * automatically registered to the MBean server. Notice that the interceptor
    * name must be set before the call to {@link #register} method. Shared
    * interceptor names must be unique within the MBean server.
    */
   public AbstractSharedInterceptor() {}
   
   /**
    * Constructs a new shared interceptor instance with a given name. The interceptor
    * is not automatically registered to the MBean server. Notice that the 
    * shared interceptor name must be unique name among all shared interceptors
    * within the MBean server.
    *
    * @param name name of this interceptor
    *
    * @throws IllegalArgumentException if name contains <tt>null</tt> reference
    */
   public AbstractSharedInterceptor(String name)
   {
      super(name);
   }
   
   
   // SharedInterceptor implementation ------------------------------
   
   public ObjectName getObjectName()
   {
      return objectName;
   }
   
   public MBeanServer getMBeanServer()
   {
      return server;
   }
   
   /**
    * Registers the interceptor to the MBean server.  <p>
    *
    * The interceptor is registered under the 
    * {@link org.jboss.mx.service.ServiceConstants#JBOSSMX_DOMAIN JBOSSMX_DOMAIN}
    * name. An interceptor's object name contains a <tt>type</tt> property and
    * a <tt>name</tt> property. Property <tt>type</tt> always contains string
    * <tt>'Interceptor'</tt> as its value. Interceptor's name is used as a value
    * for the <tt>name</tt> property. Therefore, an interceptor created with
    * name <tt>'Bart'</tt> can be found from the MBean server under object name:   <br><pre>
    *
    *    {@link org.jboss.mx.service.ServiceConstants#JBOSSMX_DOMAIN JBOSSMX_DOMAIN}:type=Interceptor,name=Bart,*
    *
    * </pre>
    *
    * If the log reference has not been set for this interceptor when it is
    * registered, this implementation will register a log MBean via the system
    * log manager under {@link org.jboss.mx.service.ServiceConstants#JBOSSMX_DOMAIN JBOSSMX}
    * domain (see {@link org.jboss.mx.logging.SystemLogManager SystemLogManager}
    * for details). The log instance's name will match the pattern:   <br><pre>
    *
    *    "JBossMX.Interceptor.&lt;interceptor name&gt;"
    *
    * </pre>
    *
    * @param   server   MBean server where this shared interceptor is registered
    */
   public synchronized ObjectName register(MBeanServer server) 
         throws InterceptorNameConflictException
   {
      
      // store MBean server reference
      this.server = server;
      
      // check if log instance has been set
      if (log == null)
         log = Logger.getLogger("JBossMX.Interceptor." + name);
         
      try
      {
         // store the object name for later use
         objectName = createObjectName(); 
      
         // query the server for this name
         Set names = server.queryNames(objectName, null /* NO QUERY EXPR. */);
         
         // if the query returns a non empty set, throw an exception
         if (names.size() > 0)
            throw new InterceptorNameConflictException(
                  "A shared interceptor named '" + name + "' already registered " +
                  "to this MBean server (" + AgentID.get(server) + ")"
            );
            
         // register the interceptor to server
         ModelMBean rmm = RequiredModelMBeanInstantiator.instantiate();
         rmm.setManagedResource(this, ModelMBeanConstants.OBJECT_REF);
         rmm.setModelMBeanInfo(getManagementInterface());
         server.registerMBean(rmm, objectName);
         
         // mark the interceptor as shared
         isShared = true;
      }
      
      catch (InstanceAlreadyExistsException e)
      {
         // we already checked that the instance doesn't exist with a query,
         // however it is possible it was created by another thread before we
         // actually had a chance to register
         throw new InterceptorNameConflictException(
               "A shared interceptor named '" + name + "' already registered " +
               "to this MBean server (" + AgentID.get(server) + ")"
         );
      }
      
      catch (Exception e)
      {
         // anything else indicates there's something much more wrong if
         // we can't register a simple MBean, so just log an error
         if (log != null)
            log.error(e.toString(), e);
      }
      
      return objectName;
   }

   /**
    * This method is part of the interceptor MBean's registration lifecycle.
    * It is called before the MBean is registered to the server. Concrete
    * interceptor implementations can override this method to provide
    * initialization code that should be executed before the interceptor
    * is registered.   <p>
    *
    * Any exception that is propagated from this method to its caller will
    * cancel the interceptor registration.
    *
    * @throws Exception if you want to cancel the interceptor registration
    */
   public void init() throws Exception {}

   /**
    * This method is part of the interceptor MBean's registration lifecycle.
    * It is called after the MBean is registered to the server. Concrete
    * interceptor implementations can override this method to provide
    * initialization code that should be executed once the MBean server and
    * object name references for this interceptor have been resolved.
    */
   public void start() {}

   /**
    * This method is part of the interceptor MBean's registration lifecycle.
    * It is called before the MBean is unregistered from the server. Concrete
    * interceptor implementations can override this method to provide
    * cleanup code that should be executed before the interceptor is
    * unregistered.   <p>
    *
    * Any exception that is propagated from this method to its caller will
    * cancel the interceptor unregistration.
    *
    * @throws Exception if you want to cancel the interceptor unregistration
    */
   public void stop() throws Exception {}

   /**
    * This method is part of the interceptor MBean's registration lifecycle.
    * It is called after the MBean has been unregistered from the server. Concrete
    * interceptor implementations can override this method to provide
    * cleanup code that should be executed once the interceptor is no longer
    * registered to the MBean server.
    */
   public void destroy() {}

   
   // MBeanRegistration implementation ------------------------------
   
   public ObjectName preRegister(MBeanServer server, ObjectName oname) throws Exception
   {
      this.server = server;
      
      if (oname == null)
         this.objectName = createObjectName();
      else  
         this.objectName = oname;
     
      init();
      
      return objectName;
   }
   
   public void postRegister(Boolean registrationSuccesful) { 
      isShared = true;
      
      start();
   }
   
   public void preDeregister() throws Exception {
      stop();

      isShared    = false;
      objectName  = null;      
   }
   
   public void postDeregister() {
      destroy();
   }
   
   
   // AbstractInterceptor overrides ---------------------------------
   
   /**
    * Shared interceptors allows their name to be set only before they have
    * been registered to the MBean server. After that the name is fixed and
    * any attempt to invoke this method to change the name will yield a 
    * IllegalArgumentException.
    *
    * @param   name  name of this shared interceptor
    *
    * @throws IllegalArgumentException if there was an attempt to change the
    *         name after the interceptor had been registered to the server
    */
   public synchronized void setName(String name)
   {
      if (isShared())
         throw new IllegalArgumentException("Cannot change the interceptor name. Already registered.");
         
      this.name = name;
   }      
   
   
   // Object overrides ----------------------------------------------

   /**
    * Returns a string representation of this interceptor instance.
    *
    * @return  string representation
    */
   public String toString()
   {
      String className = getClass().getName();
      int index        = className.lastIndexOf('.');
      
      return className.substring((index < 0) ? 0 : index) + "[" +
             "name=" + name + "SHARED " + objectName + "]";
   }
   
   
   // Protected -----------------------------------------------------
   
   /**
    * Creates an object name for this interceptor.  The object name contains a
    * <tt>type</tt> property and a <tt>name</tt> property. Property <tt>type</tt>
    * always contains string <tt>'Interceptor'</tt> as its value. Interceptor's
    * name is used as a value for the <tt>name</tt> property. Therefore, an
    * interceptor created with name <tt>'Bart'</tt> will generate an object name
    * matching to pattern:   <br><pre>
    *
    *    {@link org.jboss.mx.service.ServiceConstants#JBOSSMX_DOMAIN JBOSSMX_DOMAIN}:type=Interceptor,name=Bart,*
    *
    * </pre>
    *
    * @return  generated object name for this interceptor
    *
    * @throws MalformedObjectNameException if the object name could not be
    *         created
    */
   protected ObjectName createObjectName() throws MalformedObjectNameException
   {
      // create the object name for this shared interceptor
      Hashtable props = new Hashtable(2);
      props.put("type", "Interceptor");
      props.put("name", name);
      props.put("ID", "0");
      
      return new ObjectName(ServiceConstants.JBOSSMX_DOMAIN, props);
   }

   
   // Private -------------------------------------------------------
   
   private ModelMBeanInfo getManagementInterface() 
   {
      return new ModelMBeanInfoSupport(
         this.getClass().getName(),             // resource object class name
         "Interceptor invocation interface",    // description of the MBean
         
         null,                                  // attributes
         
         null,                                  // constructors
         
         new ModelMBeanOperationInfo[]          // operations
         {
            new ModelMBeanOperationInfo(
                  "invoke",                                 // name
                  "Shared interceptor invoke operation.",   // description
                  new MBeanParameterInfo[]                  // arguments
                  {
                     new MBeanParameterInfo(
                           "invocation",                    // name
                           Invocation.class.getName(),      // type
                           "The invocation object."         // description
                     )
                  },
                  Object.class.getName(),        // return type
                  MBeanOperationInfo.ACTION_INFO // impact
            )
         },
         
         null                                   // notifications
      );
   }
   
}


