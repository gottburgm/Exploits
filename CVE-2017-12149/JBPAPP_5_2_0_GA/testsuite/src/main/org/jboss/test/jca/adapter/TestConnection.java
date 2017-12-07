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

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.ResultSetInfo;

/**
 * TestConnection.java
 *
 *
 * Created: Sun Mar 10 19:35:48 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */
public class TestConnection implements Connection
{
   private TestManagedConnection mc;

   private boolean mcIsNull = true;

   public TestConnection(TestManagedConnection mc)
   {
      this.mc = mc;
      mcIsNull = false;
   }

   public boolean getMCIsNull()
   {
      return mcIsNull;
   }

   public void setFailInPrepare(final boolean fail, final int xaCode)
   {
      mc.setFailInPrepare(fail, xaCode);
   }
   
   public void setFailInStart(final boolean fail, final int xaCode)
   {
      
   }
   public void setFailInCommit(final boolean fail, final int xaCode)
   {
      mc.setFailInCommit(fail, xaCode);
   }

   public void fireConnectionError()
   {
      mc.connectionError(this, new Exception("ConnectionError"));
   }

   public boolean isInTx()
   {
      return mc.isInTx();
   }

   void setMc(TestManagedConnection mc)
   {
      if (mc == null)
      {
         mcIsNull = true;
      } // end of if ()
      else
      {
         this.mc = mc;
      } // end of else
   }

   public String getLocalState()
   {
      return mc.getLocalState();
   }
   
   public void begin() throws Exception
   {
      mc.sendBegin();
   }
   
   public void commit() throws Exception
   {
      mc.sendCommit();
   }
   
   public void rollback() throws Exception
   {
      mc.sendRollback();
   }
   
   // implementation of javax.resource.cci.Connection interface

   /**
    *
    * @exception javax.resource.ResourceException <description>
    */
   public void close()
   {
      mc.connectionClosed(this);
      mcIsNull = true;
   }

   public TestManagedConnection getMC()
   {
      return mc;
   }

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public Interaction createInteraction() throws ResourceException
   {
      // TODO: implement this javax.resource.cci.Connection method
      return null;
   }

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      // TODO: implement this javax.resource.cci.Connection method
      return null;
   }

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public ConnectionMetaData getMetaData() throws ResourceException
   {
      // TODO: implement this javax.resource.cci.Connection method
      return null;
   }

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public ResultSetInfo getResultSetInfo() throws ResourceException
   {
      // TODO: implement this javax.resource.cci.Connection method
      return null;
   }

   /**
    * Similate a connection error
    */
   public void simulateConnectionError() throws Exception
   {
      Exception e = new Exception("Simulated exception");
      mc.connectionError(this, e);
      throw e;
   }

}// TestConnection

