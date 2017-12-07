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
package org.jboss.invocation;

import java.util.Map;
import java.util.HashMap;

import org.jboss.invocation.Invoker;

/**
 * The Invocation Context
 *
 * <p>Describes the context in which this Invocation is being executed in 
 *    the interceptors
 *
 * <p>The heart of it is the payload map that can contain anything we then 
 *    put readers on them. The first "reader" is this "Invocation" object that
 *    can interpret the data in it. 
 * 
 * <p>Essentially we can carry ANYTHING from the client to the server, we 
 *    keep a series of redifined variables and method calls to get at the 
 *    pointers.  But really it is just a repository of objects. 
 *
 * @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @version $Revision: 81030 $
 */
public class InvocationContext
   implements java.io.Serializable
{
   /** Serial Version Identifier. @since 1.5 */
   private static final long serialVersionUID = 7679468692447241311L;

   // Context is a map
   public Map context;

   /**
    * Exposed for externalization only.
    */
   public InvocationContext() {
      context = new HashMap();
   }
   
   /**
    * Invocation creation
    */
   public InvocationContext(final Map context) {
      this.context = context;
   }
      
   // 
   // The generic getter and setter is really all that one needs to talk to 
   // this object.  We introduce typed getters and setters for convenience 
   // and code readability in the codebase
   //
   
   /**
    * The generic store of variables
    */
   public void setValue(Object key, Object value) {
      context.put(key,value);
   }
   
   /**
    * Get a value from the stores.
    */
   public Object getValue(Object key) 
   { 
      return context.get(key);
   }
   
   /**
    * A container for server side association.
    */
   public void setObjectName(Object objectName) {
      context.put(InvocationKey.OBJECT_NAME, objectName);
   }
   
   public Object getObjectName() {
      return context.get(InvocationKey.OBJECT_NAME);
   }
   
   /**
    * Return the invocation target ID.  Can be used to identify a cached object.
    */
   public void setCacheId(Object id) {
      context.put(InvocationKey.CACHE_ID, id);
   }
   
   public Object getCacheId() {
      return context.get(InvocationKey.CACHE_ID);
   }
   
   public void setInvoker(Invoker invoker) {
      context.put(InvocationKey.INVOKER, invoker);
   }
   
   public Invoker getInvoker() {
      return (Invoker) context.get(InvocationKey.INVOKER);
   }
   
   public void setInvokerProxyBinding(String binding) {
      context.put(InvocationKey.INVOKER_PROXY_BINDING, binding);
   }
   
   public String getInvokerProxyBinding() {
      return (String) context.get(InvocationKey.INVOKER_PROXY_BINDING);
   }
}
