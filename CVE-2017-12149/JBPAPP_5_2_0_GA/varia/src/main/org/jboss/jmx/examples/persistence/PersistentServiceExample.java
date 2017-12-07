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
package org.jboss.jmx.examples.persistence;

import java.io.FileDescriptor;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.jboss.system.ServiceMBeanSupport;
import org.w3c.dom.Element;

/**
 * PersistentServiceExample.
 * 
 * Demonstrates the usage of XMBean attribute persistence.
 *  
 * @jmx:mbean
 *    extends="org.jboss.system.ServiceMBean"
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
**/
public class PersistentServiceExample
   extends ServiceMBeanSupport
   implements PersistentServiceExampleMBean
{
   // Private Data --------------------------------------------------

   // Primitives
   private boolean someBoolean;
   private int     someInt;
   
   // Simple types with a property editor
   private Integer    someInteger;
   private BigDecimal someBigDecimal;
   private String     someString;
   
   // an XML Element
   private Element someElement;
   
   // a serializable object without a property editor
   private Timestamp someTimestamp;
   
   // a serializable object containing non-serializable objects
   private ArrayList someArrayList;
   
   // a non-serializable object without a property editor
   private FileDescriptor someFileDescriptor;
   
   // a null object
   private Object someNullObject;
   
   // Constructors -------------------------------------------------
    
   /**
    * Constructs a <tt>PersistentServiceExample</tt>.
    */
   public PersistentServiceExample()
   {
      super(PersistentServiceExample.class);
      
      this.someBoolean = true;
      this.someInt     = 666;
      
      this.someInteger = new Integer(999);
      this.someBigDecimal = new BigDecimal("3.14e66");
      this.someString  = new String("I've got the devil inside me");
      
      this.someElement = null;

      this.someTimestamp = new Timestamp(System.currentTimeMillis());
      
      this.someArrayList = new ArrayList();
      this.someArrayList.add(new FileDescriptor());
      
      this.someFileDescriptor = new FileDescriptor();
      
      this.someNullObject = null;
   }

   // Attributes ----------------------------------------------------

   /**
    * @return Returns the someBigDecimal.
    * @jmx:managed-attribute
    */
   public BigDecimal getSomeBigDecimal() {
      return someBigDecimal;
   }
   
   /**
    * @param someBigDecimal The someBigDecimal to set.
    * @jmx:managed-attribute
    */
   public void setSomeBigDecimal(BigDecimal someBigDecimal) {
      this.someBigDecimal = someBigDecimal;
   }
   
   /**
    * @return Returns the someBoolean.
    * @jmx:managed-attribute    
    */
   public boolean isSomeBoolean() {
      return someBoolean;
   }
   
   /**
    * @param someBoolean The someBoolean to set.
    * @jmx:managed-attribute
    */
   public void setSomeBoolean(boolean someBoolean) {
      this.someBoolean = someBoolean;
   }
   
   /**
    * @return Returns the someElement.
    * @jmx:managed-attribute
    */
   public Element getSomeElement() {
      return someElement;
   }
   
   /**
    * @param someElement The someElement to set.
    * @jmx:managed-attribute
    */
   public void setSomeElement(Element someElement) {
      this.someElement = someElement;
   }
   
   /**
    * @return Returns the someFileDescriptor.
    * @jmx:managed-attribute
    */
   public FileDescriptor getSomeFileDescriptor() {
      return someFileDescriptor;
   }
   
   /**
    * @param someFileDescriptor The someFileDescriptor to set.
    * @jmx:managed-attribute
    */
   public void setSomeFileDescriptor(FileDescriptor someFileDescriptor) {
      this.someFileDescriptor = someFileDescriptor;
   }
   
   /**
    * @return Returns the someInt.
    * @jmx:managed-attribute
    */
   public int getSomeInt() {
      return someInt;
   }
   
   /**
    * @param someInt The someInt to set.
    * @jmx:managed-attribute
    */
   public void setSomeInt(int someInt) {
      this.someInt = someInt;
   }
   
   /**
    * @return Returns the someInteger.
    * @jmx:managed-attribute
    */
   public Integer getSomeInteger() {
      return someInteger;
   }
   
   /**
    * @param someInteger The someInteger to set.
    * @jmx:managed-attribute
    */
   public void setSomeInteger(Integer someInteger) {
      this.someInteger = someInteger;
   }
   
   /**
    * @return Returns the someString.
    * @jmx:managed-attribute
    */
   public String getSomeString() {
      return someString;
   }
   
   /**
    * @param someString The someString to set.
    * @jmx:managed-attribute
    */
   public void setSomeString(String someString) {
      this.someString = someString;
   }
   
   /**
    * @return Returns the someTimestamp.
    * @jmx:managed-attribute
    */
   public Timestamp getSomeTimestamp() {
      someTimestamp = new Timestamp(System.currentTimeMillis());
      return someTimestamp;
   }
   
   /**
    * @param someTimestamp The someTimestamp to set.
    * @jmx:managed-attribute
    */
   public void setSomeTimestamp(Timestamp someTimestamp) {
      this.someTimestamp = someTimestamp;
   }
   
   /**
    * @return Returns the someNullObject.
    * @jmx:managed-attribute
    */
   public Object getSomeNullObject() {
      return someNullObject;
   }
   
   /**
    * @param someNullObject The someNullObject to set.
    * @jmx:managed-attribute
    */
   public void setSomeNullObject(Object someNullObject) {
      // ignore
   }
   
   /**
    * @return Returns the someArrayList.
    * @jmx:managed-attribute
    */
   public ArrayList getSomeArrayList() {
      return someArrayList;
   }
   
   /**
    * @param someArrayList The someArrayList to set.
    * @jmx:managed-attribute
    */
   public void setSomeArrayList(ArrayList someArrayList) {
      this.someArrayList = someArrayList;
   }
}