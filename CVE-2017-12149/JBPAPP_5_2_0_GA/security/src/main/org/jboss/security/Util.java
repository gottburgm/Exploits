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
package org.jboss.security;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jboss.logging.Logger;

/**
 * Util.
 * 
 * @author Scott.Stark@jboss.org
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class Util
{
   private static Logger log = Logger.getLogger(Util.class);

   /**
    * Execute a password load command to obtain the char[] contents of a
    * password.
    * @param  passwordCmd  - A command to execute to obtain the plaintext
    * password. The format is one of:
    * '{EXT}...' where the '...' is the exact command
    * line that will be passed to the Runtime.exec(String) method to execute a
    * platform command. The first line of the command output is used as the
    * password.
    * '{CLASS}classname[:ctorargs]' where the '[:ctorargs]' is an optional
    * string delimited by the ':' from the classname that will be passed to the
    * classname ctor. The ctorargs itself is a comma delimited list of strings.
    * The password is obtained from classname by invoking a
    * 'char[] toCharArray()' method if found, otherwise, the 'String toString()'
    * method is used.
    * @return the password characters
    * @throws Exception
    */ 
   public static char[] loadPassword(String passwordCmd)
      throws Exception
   {
      char[] password = null;
      String passwordCmdType = null;
      
      // Look for a {...} prefix indicating a password command
      if( passwordCmd.charAt(0) == '{' )
      {
         StringTokenizer tokenizer = new StringTokenizer(passwordCmd, "{}");
         passwordCmdType = tokenizer.nextToken();
         passwordCmd = tokenizer.nextToken();
      }
      else
      {
         // Its just the password string
         password = passwordCmd.toCharArray();
      }

      if( password == null )
      {
         // Load the password
         if( passwordCmdType.equals("EXT") )
            password = execPasswordCmd(passwordCmd);
         else if( passwordCmdType.equals("CLASS") )
            password = invokePasswordClass(passwordCmd);
         else
            throw new IllegalArgumentException("Unknown passwordCmdType: "+passwordCmdType);
      }
      return password;
   }

   /**
    * Execute a Runtime command to load a password.
    * @param passwordCmd
    * @return
    * @throws Exception
    */
   private static char[] execPasswordCmd(String passwordCmd)
      throws Exception
   {
      log.debug("Executing command: "+passwordCmd);
      String password = execCmd(passwordCmd);
      return password.toCharArray();
   }

   private static char[] invokePasswordClass(String passwordCmd)
      throws Exception
   {
      char[] password = null;

      // Check for a ctor argument delimited by ':'
      String classname = passwordCmd;
      String ctorArgs = null;
      int colon = passwordCmd.indexOf(':');
      if( colon > 0 )
      {
         classname = passwordCmd.substring(0, colon);
         ctorArgs = passwordCmd.substring(colon+1);
      }
      log.debug("Loading class: "+classname+", ctorArgs="+ctorArgs);
      ClassLoader loader = AccessController.doPrivileged(GetTCLAction.ACTION);
      Class<?> c = loader.loadClass(classname);
      Object instance = null;
      // Check for a ctor(String,...) if ctorArg is not null
      if( ctorArgs != null )
      {
         Object[] args = ctorArgs.split(",");
         Class<?>[] sig = new Class[args.length];
         ArrayList<Class<?>> sigl = new ArrayList<Class<?>>();
         for(int n = 0; n < args.length; n ++)
            sigl.add(String.class);
         sigl.toArray(sig);
         Constructor<?> ctor = c.getConstructor(sig);
         instance = ctor.newInstance(args);
      }
      else
      {
         // Use the default ctor
         instance = c.newInstance();
      }

      // Look for a toCharArray() method
      try
      {
         log.debug("Checking for toCharArray");
         Class<?>[] sig = {};
         Method toCharArray = c.getMethod("toCharArray", sig);
         Object[] args = {};
         log.debug("Invoking toCharArray");
         password = (char[]) toCharArray.invoke(instance, args);
      }
      catch(NoSuchMethodException e)
      {
         log.debug("No toCharArray found, invoking toString");
         String tmp = instance.toString();
         if( tmp != null )
            password = tmp.toCharArray();
      }
      return password;
   }

   private static class GetTCLAction implements PrivilegedAction<ClassLoader>
   {
      static PrivilegedAction<ClassLoader> ACTION = new GetTCLAction();
      public ClassLoader run()
      {
         return Thread.currentThread().getContextClassLoader();
      }
   }

   private static String execCmd(String cmd) throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      String line;
      if( sm != null )
      {
         line = RuntimeActions.PRIVILEGED.execCmd(cmd);
      }
      else
      {
         line = RuntimeActions.NON_PRIVILEGED.execCmd(cmd);
      }
      return line;
   }

   
   interface RuntimeActions
   {
      RuntimeActions PRIVILEGED = new RuntimeActions()
      {
         public String execCmd(final String cmd)
            throws Exception
         {
            try
            {
               String line = AccessController.doPrivileged(
               new PrivilegedExceptionAction<String>()
                  {
                     public String run() throws Exception
                     {
                        return NON_PRIVILEGED.execCmd(cmd);
                     }
                  }
               );
               return line;
            }
            catch(PrivilegedActionException e)
            {
               throw e.getException();
            }
         }
      };
      RuntimeActions NON_PRIVILEGED = new RuntimeActions()
      {
         public String execCmd(final String cmd)
            throws Exception
         {
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec(cmd);
            InputStream stdin = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
            String line = reader.readLine();
            stdin.close();
            int exitCode = p.waitFor();
            log.debug("Command exited with: "+exitCode);
            return line;
         }
      };
      String execCmd(String cmd) throws Exception;
   }
}
