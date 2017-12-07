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
package org.jboss.system.security;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;

/**
 * A Security Manager that is useful for debugging access exceptions
 * because the default security manager in the JDK can be very verbose
 * and difficult to debug
 * 
 * <b> NOTE:</b> Do not use this security manager in production.
 * 
 * <b> USAGE:</b> -Djava.security.manager -Djava.security.manager=org.jboss.system.security.DebuggingJavaSecurityManager
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Mar 26, 2010
 */
@SuppressWarnings("unchecked")
public class DebuggingJavaSecurityManager extends SecurityManager
{  
   FilteringPrintStream fps = null;

   /**
    * JBoss Logging Subsystem tries to install its own stdout/stderr which can
    * mess around with this debugging security manager which installs a filtering
    * print stream (as the JDK Java Security Manager runs in a full debug mode)
    */
   private boolean settingStream = false;

   /**
    * We are filtering the log entries. We do not want to see the verbose
    * "access allowed" console entries from the Java Security Manager
    */
   private static boolean turnOnLogging = false;

   public DebuggingJavaSecurityManager() 
   {   
      AccessController.doPrivileged( new PrivilegedAction()
      {
         @Override
         public Object run() {
            try
            { 
               fps = new  FilteringPrintStream(); 
               setStreams();
            }
            catch(Exception e)
            {
               e.printStackTrace();
            }return null;
         }}); 
   }   

   public void checkRead(final String file) 
   {
      //We blank this method to avoid a stack over flow error
   }

   @Override
   public void checkPermission(final Permission perm) 
   {
      turnOnLogging = false; 

      try
      {
         if(System.err != fps )
            setStreams();
         super.checkPermission(perm); 

      }
      catch( AccessControlException ace )
      {  
         turnOnLogging = true;
         throw ace;
      }
   } 


   private static class FilteringPrintStream extends PrintStream
   {   
      private PrintStream ps;

      public FilteringPrintStream( ) throws FileNotFoundException
      {
         super( new DummyOutputStream() );
         this.ps = System.err;
      }

      @Override
      public void println(String x) 
      {
         if( DebuggingJavaSecurityManager.turnOnLogging == false )
         {
            if( x.contains( "allowed") )
               return; 
            if( x.contains( "domain that failed ProtectionDomain") || x.contains( "Confirming") 
                  || x.contains("denied") || x.contains( "Exception" ) )
            {
               ps.println(x); 
            } 
         } 
      }  

      public void print(String s)
      {
         ps.print(s);
      }
   }

   private static class DummyOutputStream extends OutputStream
   {

      @Override
      public void write(int b) throws IOException
      {

      } 
   }

   /**
    * Since the JBoss Logging Subsystem can install its own std err/output stream, it is very
    * important for debugging purposes to set the security manager output to our custom
    * PrintStream.
    */
   private void setStreams()
   {
      AccessController.doPrivileged( new PrivilegedAction()
      {
         @Override
         public Object run() {
            try
            {
               //recursive check may be happening
               if (settingStream == true )
                  return null;
               settingStream = true;

               System.err.println( " ");
               System.err.println( " ");
               
               System.err.println( "*** Java Security Manager is the one for debugging.  DO NOT USE THIS IN PRODUCTION ****");
               System.err.println( " ");
               System.err.println( " ");
               System.err.println( " ");
               System.err.println( " ");

               System.err.println( "WE ARE SETTING THE error and output streams to FILTERINGPRINTSTREAM (Not for ***Production*** Use)");
               if( fps == null)
                  fps = new  FilteringPrintStream(); 
               System.setErr( fps);

               System.setOut(fps);

               System.err.println( "Confirming that the error stream is set to FILTERINGPRINTSTREAM : " + (fps == System.err));  

               settingStream = false;
            }
            catch(Exception e)
            {
               e.printStackTrace();
            }return null;
         }});  
   }
}