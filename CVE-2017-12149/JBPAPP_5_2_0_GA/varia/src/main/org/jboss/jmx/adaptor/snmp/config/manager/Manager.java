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
package org.jboss.jmx.adaptor.snmp.config.manager;

/**
 * Simple POJO class to model XML data
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * 
 * @version $Revision: 81038 $
 */
public class Manager
{
   // Private Data --------------------------------------------------
   
   private String  address;
   private int     port;
   private String  localAddress;
   private int     localPort;
   private int     version;

   // Constructors -------------------------------------------------
    
   /**
    * Default CTOR
    */
   public Manager()
   {
      // empty
   }
   
   // Accessors/Modifiers -------------------------------------------

   /**
    * Method getAddress returns the value of field 'address'.
    * 
    * @return the value of field 'address'.
    */
   public String getAddress()
   {
      return address;
   } 

   /**
    * Method getLocalAddress returns the value of field
    * 'localAddress'.
    * 
    * @return the value of field 'localAddress'.
    */
   public String getLocalAddress()
   {
      return localAddress;
   }
   
   /**
    * Method getLocalPort returns the value of field 'localPort'.
    * 
    * @return the value of field 'localPort'.
    */
   public int getLocalPort()
   {
      return localPort;
   }
    
   /**
    * Method getPort returns the value of field 'port'.
    * 
    * @return the value of field 'port'.
    */
   public int getPort()
   {
      return port;
   } 

   /**
    * Method getVersion returns the value of field 'version'.
    * 
    * @return the value of field 'version'.
    */
   public int getVersion()
   {
      return version;
   }

   /**
    * Method setAddress sets the value of field 'address'.
    * 
    * @param address the value of field 'address'.
    */
   public void setAddress(String address)
   {
      this.address = address;
   } 

   /**
    * Method setLocalAddress sets the value of field
    * 'localAddress'.
    * 
    * @param localAddress the value of field 'localAddress'.
    */
   public void setLocalAddress(String localAddress)
   {
      this.localAddress = localAddress;
   } 

   /**
    * Method setLocalPort sets the value of field 'localPort'.
    * 
    * @param localPort the value of field 'localPort'.
    */
   public void setLocalPort(int localPort)
   {
      this.localPort = localPort;
   } 

   /**
    * Method setPort sets the value of field 'port'.
    * 
    * @param port the value of field 'port'.
    */
   public void setPort(int port)
   {
      this.port = port;
   }

   /**
    * Method setVersion sets the value of field 'version'.
    * 
    * @param version the value of field 'version'.
    */
   public void setVersion(int version)
   {
      this.version = version;
   }
   
   // Object overrides ----------------------------------------------
   
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer(256);
      
      sbuf.append('[')
      .append("address=").append(address)
      .append(", port=").append(port)
      .append(", localAddress=").append(localAddress)
      .append(", localPort=").append(localPort)
      .append(", version=").append(version)      
      .append(']');
      
      return sbuf.toString();      
   }
}
