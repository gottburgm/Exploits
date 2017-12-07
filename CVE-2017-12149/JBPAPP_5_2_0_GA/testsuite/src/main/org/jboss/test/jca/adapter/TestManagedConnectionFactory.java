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
package org.jboss.test.jca.adapter; 

import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * ManagedConnectionFactory.java
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class TestManagedConnectionFactory implements ManagedConnectionFactory
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private Logger log = Logger.getLogger(TestManagedConnectionFactory.class); 
   
   //number the managed connections
   AtomicInteger id = new AtomicInteger(0);

   String failure;

   boolean failJoin;

   long sleepInStart;

   long sleepInEnd;
   
   Map xids = new WrapperMap(new HashMap());
   
   public TestManagedConnectionFactory()
   {
   }

   public void setFailure(String failure)
   {
      this.failure = failure;
   }

   public boolean getFailJoin()
   {
      return failJoin;
   }
   
   public void setFailJoin(boolean failJoin)
   {
      this.failJoin = failJoin;
   }

   public long getSleepInStart()
   {
      return sleepInStart;
   }
   
   public void setSleepInStart(long sleep)
   {
      this.sleepInStart = sleep;
   }

   public long getSleepInEnd()
   {
      return sleepInEnd;
   }
   
   public void setSleepInEnd(long sleep)
   {
      this.sleepInEnd = sleep;
   }
   
   // implementation of javax.resource.spi.ManagedConnectionFactory interface

   public int hashCode()
   {
     return getClass().hashCode();
   }

   public boolean equals(Object other)
   {
      return (other != null) && (other.getClass() == getClass());
   }

   public void setLogWriter(PrintWriter param1) throws ResourceException
   {
   }

   public PrintWriter getLogWriter() throws ResourceException
   {
     return null;
   }

   public Object createConnectionFactory(ConnectionManager cm) throws ResourceException
   {
     return new TestConnectionFactory(cm, this);
   }

   public Object createConnectionFactory() throws ResourceException
   {
      throw new ResourceException("not yet implemented");
   }

   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      if (failure != null && failure.equals("createManagedConnectionResource"))
         throw new ResourceException("");
      if (failure != null && failure.equals("createManagedConnectionRuntime"))
         throw new RuntimeException("");
      return new TestManagedConnection(this, subject, (TestConnectionRequestInfo)cri, id.incrementAndGet());
   }

   public ManagedConnection matchManagedConnections(Set candidates, Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      if (failure != null && failure.equals("matchManagedConnectionResource"))
         throw new ResourceException("");
      if (failure != null && failure.equals("matchManagedConnectionRuntime"))
         throw new RuntimeException("");
      if (candidates.isEmpty()) 
         return null;
      return (ManagedConnection)candidates.iterator().next();
   }

   Integer integerProperty;

   public Integer getIntegerProperty()
   {
      return integerProperty;
   }

   /**
    * Set the IntegerProperty value.
    * 
    * @param newIntegerProperty The new IntegerProperty value.
    */
   public void setIntegerProperty(Integer integerProperty)
   {
      this.integerProperty = integerProperty;
   }

   Integer defaultIntegerProperty;

   /**
    * Get the DefaultIntegerProperty value.
    * 
    * @return the DefaultIntegerProperty value.
    */
   public Integer getDefaultIntegerProperty()
   {
      return defaultIntegerProperty;
   }

   /**
    * Set the DefaultIntegerProperty value.
    * 
    * @param newDefaultIntegerProperty The new DefaultIntegerProperty value.
    */
   public void setDefaultIntegerProperty(Integer defaultIntegerProperty)
   {
      this.defaultIntegerProperty = defaultIntegerProperty;
   }

   Boolean booleanProperty;

   /**
    * Get the BooleanProperty value.
    * 
    * @return the BooleanProperty value.
    */
   public Boolean getBooleanProperty()
   {
      return booleanProperty;
   }

   /**
    * Set the BooleanProperty value.
    * 
    * @param newBooleanProperty The new BooleanProperty value.
    */
   public void setBooleanProperty(Boolean booleanProperty)
   {
      this.booleanProperty = booleanProperty;
   }

   Long longProperty;

   /**
    * Get the LongProperty value.
    * 
    * @return the LongProperty value.
    */
   public Long getLongProperty()
   {
      return longProperty;
   }

   /**
    * Set the LongProperty value.
    * 
    * @param newLongProperty The new LongProperty value.
    */
   public void setLongProperty(Long longProperty)
   {
      this.longProperty = longProperty;
   }

   Double doubleProperty;

   /**
    * Get the DoubleProperty value.
    * 
    * @return the DoubleProperty value.
    */
   public Double getDoubleProperty()
   {
      return doubleProperty;
   }

   /**
    * Set the DoubleProperty value.
    * 
    * @param newDoubleProperty The new DoubleProperty value.
    */
   public void setDoubleProperty(Double doubleProperty)
   {
      this.doubleProperty = doubleProperty;
   }

   URL urlProperty;

   /**
    * Get the UrlProperty value. (this is a jboss specific property editor)
    * 
    * @return the UrlProperty value.
    */
   public URL getUrlProperty()
   {
      return urlProperty;
   }

   /**
    * Set the UrlProperty value.
    * 
    * @param newUrlProperty The new UrlProperty value.
    */
   public void setUrlProperty(URL urlProperty)
   {
      this.urlProperty = urlProperty;
   }
   
   Map getXids()
   {
      return xids;
   }
   
   public class WrapperMap implements Map
   {
      Map delegate;
      
      public WrapperMap(Map delegate)
      {
         this.delegate = delegate;
      }
      
      public void clear()
      {
         delegate.clear();
      }

      public boolean containsKey(Object key)
      {
         return delegate.containsKey(key);
      }

      public boolean containsValue(Object value)
      {
         return delegate.containsValue(value);
      }

      public Set entrySet()
      {
         return delegate.entrySet();
      }

      public Object get(Object key)
      {
         return delegate.get(key);
      }

      public boolean isEmpty()
      {
         return delegate.isEmpty();
      }

      public Set keySet()
      {
         return delegate.keySet();
      }

      public Object put(Object key, Object value)
      {
         Object result = delegate.put(key, value);
         log.info("Change xid=" + key + " from " + result + " to " + value);
         return result;
      }

      public void putAll(Map t)
      {
         delegate.putAll(t);
      }

      public Object remove(Object key)
      {
         return delegate.remove(key);
      }

      public int size()
      {
         return delegate.size();
      }

      public Collection values()
      {
         return delegate.values();
      }
   }
}
