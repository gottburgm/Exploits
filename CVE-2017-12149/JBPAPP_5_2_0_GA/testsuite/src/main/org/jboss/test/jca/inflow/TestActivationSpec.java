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
package org.jboss.test.jca.inflow;

import java.net.InetAddress;
import java.util.Properties;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

/**
 * A TestActivationSpec that has non-string java bean properties.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class TestActivationSpec implements ActivationSpec
{
   private ResourceAdapter ra;
   
   private String name;
   /** An int between 1-10 */
   private int anInt;
   /** An Integer between 50-100 */
   private Integer anInteger;
   /** The 127.0.0.1 address */
   private InetAddress localhost;
   /** Properties of the form key1=*;key2=*;... */
   private Properties props;

   /**
    * 
    * @throws InvalidPropertyException
    */ 
   public void validate() throws InvalidPropertyException
   {
      /** An int between 1-10 */
      if( anInt <= 0 || anInt > 10 )
         throw new InvalidPropertyException("anInt is not between 1-10");
      /** An int between 50-100 */
      if( anInteger.intValue() <= 49 || anInteger.intValue() > 100 )
         throw new InvalidPropertyException("anInt is not between 50-100");
      /** The 127.0.0.1 address */
      if( localhost.getHostAddress().equals("127.0.0.1") == false )
         throw new InvalidPropertyException("localhost is not 127.0.0.1");         
      /** Properties of the key1=*;key2=*;... */
      if( props.size() == 0 )
         throw new InvalidPropertyException("props has no values");         
   }

   public String getName()
   {
      return name;
   } 
   public void setName(String name)
   {
      this.name = name;
   }

   public int getAnInt()
   {
      return anInt;
   }
   public void setAnInt(int anInt)
   {
      this.anInt = anInt;
   }

   public Integer getAnInteger()
   {
      return anInteger;
   }
   public void setAnInteger(Integer anInteger)
   {
      this.anInteger = anInteger;
   }

   public InetAddress getLocalhost()
   {
      return localhost;
   }
   public void setLocalhost(InetAddress localhost)
   {
      this.localhost = localhost;
   }

   public Properties getProps()
   {
      return props;
   }
   public void setProps(Properties props)
   {
      this.props = props;
   }

   public ResourceAdapter getResourceAdapter()
   {
      return ra;
   }
   
   public void setResourceAdapter(ResourceAdapter ra) throws ResourceException
   {
      this.ra = ra;
   }
   
   public String toString()
   {
      return "TestActivationSpec with name " + name;
   }
}
