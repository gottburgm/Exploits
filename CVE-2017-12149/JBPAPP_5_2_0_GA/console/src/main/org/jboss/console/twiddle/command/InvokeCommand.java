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
package org.jboss.console.twiddle.command;

import java.beans.PropertyEditor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.jboss.common.beans.property.BeanUtils;
import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.util.Strings;


/**
 * Invoke an operation on an MBean.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version <tt>$Revision: 113110 $</tt>
 */
public class InvokeCommand
   extends MBeanServerCommand
{
   public static final int QUERY_FIRST = 0;
   public static final int QUERY_ALL = 1;

   private int type = QUERY_FIRST;

   private String query;

   private String opName;

   private List opArgs = new ArrayList(5); // probably not more than 5 args

   public InvokeCommand()
   {
      super("invoke", "Invoke an operation on an MBean");
   }

   public void displayHelp()
   {
      PrintWriter out = context.getWriter();

      out.println(desc);
      out.println();
      out.println("usage: " + name + " [options] <query> <operation> (<arg>)*");
      out.println();
      out.println("options:");
      out.println("    -q, --query-type[=<type>]    Treat object name as a query");
      out.println("    --                           Stop processing options");
      out.println();
      out.println("query type:");
      out.println("    f[irst]    Only invoke on the first matching name [default]");
      out.println("    a[ll]      Invoke on all matching names");
   }

   private void processArguments(final String[] args)
      throws CommandException
   {
      log.debug("processing arguments: " + Strings.join(args, ","));

      if (args.length == 0)
      {
         throw new CommandException("Command requires arguments");
      }

      String sopts = "-:q:";
      LongOpt[] lopts =
         {
            new LongOpt("query-type", LongOpt.OPTIONAL_ARGUMENT, null, 'q'),
         };

      Getopt getopt = new Getopt(null, args, sopts, lopts);
      getopt.setOpterr(false);

      int code;
      int argidx = 0;

      while ((code = getopt.getopt()) != -1)
      {
         switch (code)
         {
            case ':':
               throw new CommandException
                  ("Option requires an argument: " + args[getopt.getOptind() - 1]);

            case '?':
               throw new CommandException
                  ("Invalid (or ambiguous) option: " + args[getopt.getOptind() - 1]);

               // non-option arguments
            case 1:
               {
                  String arg = getopt.getOptarg();

                  switch (argidx++)
                  {
                     case 0:
                        query = arg;
                        log.debug("query: " + query);
                        break;

                     case 1:
                        opName = arg;
                        log.debug("operation name: " + opName);
                        break;

                     default:
                        opArgs.add(arg);
                        break;
                  }
                  break;
               }

               // Set the query type
            case 'q':
               {
                  String arg = getopt.getOptarg();

                  //
                  // jason: need a uniqueness mapper, like getopt uses for options...
                  //

                  if (arg.equals("f") || arg.equals("first"))
                  {
                     type = QUERY_FIRST;
                  }
                  else if (arg.equals("a") || arg.equals("all"))
                  {
                     type = QUERY_ALL;
                  }
                  else
                  {
                     throw new CommandException("Invalid query type: " + arg);
                  }

                  log.debug("Query type: " + type);

                  break;
               }
         }
      }
   }

   private void invoke(final ObjectName name)
      throws Exception
   {
      log.debug("Invoke " + name);

      MBeanServerConnection server = getMBeanServer();
      
      // get mbean info for this mbean
      MBeanInfo info = server.getMBeanInfo(name);
      
      // does it even have an operation of this name?
      MBeanOperationInfo[] ops = info.getOperations();
      MBeanOp inputOp = new MBeanOp(opName, opArgs.size());
      MBeanOp matchOp = null;
      ArrayList opList = new ArrayList();
      for (int i = 0; i < ops.length; i++)
      {
         MBeanOperationInfo opInfo = ops[i];
         MBeanOp op = new MBeanOp(opInfo.getName(), opInfo.getSignature());
         if (inputOp.equals(op) == true)
         {
            matchOp = op;
            break;
         }
         opList.add(op);
      }

      if (matchOp == null)
      {
         // If there was not explicit match on type, look for a match on arg count
         OpCountComparator comparator = new OpCountComparator();
         Collections.sort(opList, comparator);
         int match = Collections.binarySearch(opList, inputOp, comparator);
         if (match >= 0)
         {
            // Validate that the match op equates to the input op
            matchOp = (MBeanOp) opList.get(match);
            match = comparator.compare(matchOp, inputOp);
            if (match != 0)
            {
               throw new CommandException("MBean has no such operation named '" +
                  opName + "' with signature compatible with: " + opArgs);
            }
         }
         else
         {
            throw new CommandException("MBean has no such operation named '" +
               opName + "' with signature compatible with: " + opArgs);
         }
      }
      
      // convert parameters with PropertyEditor
      int count = matchOp.getArgCount();
      Object[] params = new Object[count];
      for (int i = 0; i < count; i++)
      {
         String argType = matchOp.getArgType(i);
         PropertyEditor editor = PropertyEditorFinder.getInstance().find(BeanUtils.findClass(argType));
         editor.setAsText((String) opArgs.get(i));
         params[i] = editor.getValue();
      }
      log.debug("Using params: " + Strings.join(params, ","));
      
      // invoke the operation
      Object result = server.invoke(name, opName, params, matchOp.getSignature());
      log.debug("Raw result: " + result);

      if (!context.isQuiet())
      {
         // Translate the result to text
         String resultText = null;

         if (result != null)
         {
            try
            {
               PropertyEditor editor = PropertyEditorFinder.getInstance().find(result.getClass());
               editor.setValue(result);
               resultText = editor.getAsText();
               log.debug("Converted result: " + resultText);
            }
            catch (RuntimeException e)
            {
               // No property editor found or some conversion problem
               resultText = result.toString();
            }
         }
         else
         {
            resultText = "'null'";
         }
      
         // render results to out
         PrintWriter out = context.getWriter();
         out.println(resultText);
         out.flush();
      }
   }

   public void execute(String[] args) throws Exception
   {
      processArguments(args);

      if (query == null)
         throw new CommandException("Missing MBean query");

      if (opName == null)
         throw new CommandException("Missing operation name");

      log.debug("operation arguments: " + opArgs);
      
      // get the list of object names to work with
      ObjectName[] names = queryMBeans(query);
      if (type == QUERY_FIRST)
      {
         names = new ObjectName[]{names[0]};
      }

      for (int i = 0; i < names.length; i++)
      {
         invoke(names[i]);
      }
   }
}
