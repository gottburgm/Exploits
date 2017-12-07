/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.adapter.jdbc.vendor;

import org.jboss.logging.Logger;
import org.jboss.resource.adapter.jdbc.ValidConnectionChecker;
import org.jboss.util.NestedRuntimeException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Implements check valid connection sql
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 78074 $
 */
public class OracleValidConnectionCheckerFixed
   implements ValidConnectionChecker, Serializable
{
   private static final long serialVersionUID = 5379340663276548636L;

   private static final Logger log = Logger.getLogger(OracleValidConnectionCheckerFixed.class);

   // ping timeout
   private static final String timeout = System.getProperty(
           "org.jboss.resource.adapter.jdbc.vendor.OracleValidConnectionChecker.TIMEOUT", "5");  
   private static final Object[] params = new Object[] { new Integer(timeout) };

   private transient Method ping;

    public OracleValidConnectionCheckerFixed()
   {
      try
      {
         initPing();
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException("Unable to resolve pingDatabase method:", e);
      }
   }

   public SQLException isValidConnection(Connection c)
   {
      try
      {
         c = unwrapConnection(c);
         Integer status = (Integer) ping.invoke(c, params);
         log.trace("isValidConnection(): " + status);

         if (status == null || status.intValue() < 0) {
            log.trace("SQLException: pingDatabase failed with status = " + status);
            return new SQLException("pingDatabase failed with status = " + status);
         }
      }
      catch (Exception e)
      {
         // What do we do here? Assume it is a misconfiguration to be fixed
         log.error("Unexpected error in pingDatabase", e);
         log.error("Coonnection class: " + c.getClass().getName());
         try {
            log.error("Class source: " + c.getClass().getProtectionDomain().getCodeSource().getLocation().toString());
         } catch (Exception e1) {
            // skip
         }
         return new SQLException("pingDatabase failed with exception: ", e.getMessage());
      }
      // OK
      return null;
   }

    private Connection unwrapConnection(Connection c)
    {
        String wrappedClass = "net.sf.log4jdbc.ConnectionSpy";
        if (wrappedClass.equals(c.getClass().getName())) {
            // TODO: move unwraping to net.sf.log4jdbc.JBossValidConnectionChecker
            // works only since log4jdbc 1.2 alpha 2
            try {
                log.trace("Unwrapping " + c);
                // skipping reflection-avoiding caching in test environment
                c = (Connection) Thread.currentThread().getContextClassLoader()
                        .loadClass(wrappedClass).getMethod("getRealConnection").invoke(c);
            } catch (Exception e) {
                log.error("Failed unwrapping " + wrappedClass, e);
            }
        }
        return c;
    }

   private void initPing() throws ClassNotFoundException, NoSuchMethodException
   {
      log.trace("initPing(), timeout: " + timeout);
      Class oracleConnection = Thread.currentThread().getContextClassLoader().loadClass("oracle.jdbc.OracleConnection");
      ping = oracleConnection.getMethod("pingDatabase", new Class[] { Integer.TYPE });
   }

   private void writeObject(ObjectOutputStream stream) throws IOException
   {
      // nothing
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
   {
      try
      {
         initPing();
      }
      catch (Exception e)
      {
         IOException ioe = new IOException("Unable to resolve pingDatabase method: " + e.getMessage());
         ioe.initCause(e);
         throw ioe;
      }
   }
}
