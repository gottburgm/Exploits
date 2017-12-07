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
package org.jboss.console.twiddle;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.console.twiddle.command.Command;
import org.jboss.console.twiddle.command.CommandContext;
import org.jboss.console.twiddle.command.CommandException;
import org.jboss.console.twiddle.command.NoSuchCommandException;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.util.Strings;

/**
 * A command to invoke an operation on an MBean (or MBeans).
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 113290 $
 */
public class Twiddle
{
   public static final String PROGRAM_NAME = System.getProperty("program.name", "twiddle");
   public static final String CMD_PROPERTIES = "/org/jboss/console/twiddle/commands.properties";
   public static final String DEFAULT_JNDI_NAME = "jmx/invoker/RMIAdaptor";
   private static final Logger log = Logger.getLogger(Twiddle.class);
   // Command Line Support
   private static Twiddle twiddle = new Twiddle(new PrintWriter(System.out, true),
      new PrintWriter(System.err, true));
   private static String commandName;
   private static String[] commandArgs;
   private static boolean commandHelp;
   private static URL cmdProps;

   private List commandProtoList = new ArrayList();
   private Map commandProtoMap = new HashMap();
   private PrintWriter out;
   private PrintWriter err;
   private String serverURL;
   private String adapterName;
   private String username;
   private String password;
   private boolean quiet;
   private MBeanServerConnection server;

   public Twiddle(final PrintWriter out, final PrintWriter err)
   {
      this.out = out;
      this.err = err;
   }

   public void setServerURL(final String url)
   {
      this.serverURL = url;
   }
   public void setAdapterName(final String name)
   {
      this.adapterName = name;
   }
   public void setUsername(final String username)
   {
      this.username = username;
      SecurityAssociation.setPrincipal(new SimplePrincipal(this.username));
   }
   public void setPasword(final String password)
   {
      this.password = password;
      SecurityAssociation.setCredential(this.password);
   }
   public void setQuiet(final boolean flag)
   {
      this.quiet = flag;
   }

   public void addCommandPrototype(final Command proto)
   {
      String name = proto.getName();

      log.debug("Adding command '" + name + "'; proto: " + proto);

      commandProtoList.add(proto);
      commandProtoMap.put(name, proto);
   }

   private CommandContext createCommandContext()
   {
      return new CommandContext()
      {
         public boolean isQuiet()
         {
            return quiet;
         }

         public PrintWriter getWriter()
         {
            return out;
         }

         public PrintWriter getErrorWriter()
         {
            return err;
         }

         public MBeanServerConnection getServer()
         {
            try
            {
               connect();
            }
            catch (Exception e)
            {
               throw new org.jboss.util.NestedRuntimeException(e);
            }

            return server;
         }
      };
   }

   public Command createCommand(final String name)
      throws NoSuchCommandException, Exception
   {
      //
      // jason: need to change this to accept unique substrings on command names
      //

      Command proto = (Command) commandProtoMap.get(name);
      if (proto == null)
      {
         throw new NoSuchCommandException(name);
      }

      Command command = (Command) proto.clone();
      command.setCommandContext(createCommandContext());

      return command;
   }

   private int getMaxCommandNameLength()
   {
      int max = 0;

      Iterator iter = commandProtoList.iterator();
      while (iter.hasNext())
      {
         Command command = (Command) iter.next();
         String name = command.getName();
         if (name.length() > max)
         {
            max = name.length();
         }
      }

      return max;
   }

   public void displayCommandList()
   {
      if( commandProtoList.size() == 0 )
      {
         try
         {
            loadCommands();
         }
         catch(Exception e)
         {
            System.err.println("Failed to load commands from: "+cmdProps);
            e.printStackTrace();
         }
      }
      Iterator iter = commandProtoList.iterator();

      out.println(PROGRAM_NAME + " commands: ");

      int maxNameLength = getMaxCommandNameLength();
      log.debug("max command name length: " + maxNameLength);

      while (iter.hasNext())
      {
         Command proto = (Command) iter.next();
         String name = proto.getName();
         String desc = proto.getDescription();

         out.print("    ");
         out.print(name);

         // an even pad, so things line up correctly
         out.print(Strings.pad(" ", maxNameLength - name.length()));
         out.print("    ");

         out.println(desc);
      }

      out.flush();
   }

   private MBeanServerConnection createMBeanServerConnection()
      throws NamingException
   {
      InitialContext ctx;

      if (serverURL == null)
      {
         ctx = new InitialContext();
      }
      else
      {
         Hashtable props = new Hashtable(System.getProperties());
         props.put(Context.PROVIDER_URL, serverURL);
         ctx = new InitialContext(props);
      }

      // if adapter is null, the use the default
      if (adapterName == null)
      {
         adapterName = DEFAULT_JNDI_NAME;
      }

      Object obj = ctx.lookup(adapterName);
      ctx.close();

      if (!(obj instanceof RMIAdaptor))
      {
         throw new ClassCastException
            ("Object not of type: RMIAdaptorImpl, but: " +
            (obj == null ? "not found" : obj.getClass().getName()));
      }

      return (MBeanServerConnection) obj;
   }

   private void connect()
      throws NamingException
   {
      if (server == null)
      {
         server = createMBeanServerConnection();
      }
   }

   public static void main(final String[] args)
   {
      Command command = null;

      try
      {
         // initialize java.protocol.handler.pkgs
         initProtocolHandlers();
         
         // Prosess global options
         processArguments(args);
         loadCommands();

         // Now execute the command
         if (commandName == null)
         {
            // Display program help
            displayHelp();
         }
         else
         {
            command = twiddle.createCommand(commandName);

            if (commandHelp)
            {
               System.out.println("Help for command: '" + command.getName() + "'");
               System.out.println();
               
               command.displayHelp();
            }
            else
            {
               // Execute the command
               command.execute(commandArgs);
            }
         }

         System.exit(0);
      }
      catch (CommandException e)
      {
         log.error("Command failure", e);
         System.err.println();
         
         if (e instanceof NoSuchCommandException)
         {
            twiddle.displayCommandList();
         }
         else
         {

            if (command != null)
            {
               System.err.println("Help for command: '" + command.getName() + "'");
               System.err.println();
               
               command.displayHelp();
            }
         }
         System.exit(1);
      }
      catch (Exception e)
      {
         log.error("Exec failed", e);
         System.exit(1);
      }
   }

   private static void initProtocolHandlers()
   {
      // Include the default JBoss protocol handler package
      String handlerPkgs = System.getProperty("java.protocol.handler.pkgs");
      if (handlerPkgs != null)
      {
         handlerPkgs += "|org.jboss.net.protocol";
      }
      else
      {
         handlerPkgs = "org.jboss.net.protocol";
      }
      System.setProperty("java.protocol.handler.pkgs", handlerPkgs);      
   }
   
   private static void loadCommands() throws Exception
   {
      // load command protos from property definitions
      if( cmdProps == null )
         cmdProps = Twiddle.class.getResource(CMD_PROPERTIES);
      if (cmdProps == null)
         throw new IllegalStateException("Failed to find: " + CMD_PROPERTIES);
      InputStream input = cmdProps.openStream();
      log.debug("command proto type properties: " + cmdProps);
      Properties props = new Properties();
      props.load(input);
      input.close();

      Iterator iter = props.keySet().iterator();
      while (iter.hasNext())
      {
         String name = (String) iter.next();
         String typeName = props.getProperty(name);
         Class type = Class.forName(typeName);

         twiddle.addCommandPrototype((Command) type.newInstance());
      }      
   }

   private static void displayHelp()
   {
      java.io.PrintStream out = System.out;

      out.println("A JMX client to 'twiddle' with a remote JBoss server.");
      out.println();
      out.println("usage: " + PROGRAM_NAME + " [options] <command> [command_arguments]");
      out.println();
      out.println("options:");
      out.println("    -h, --help                    Show this help message");
      out.println("        --help-commands           Show a list of commands");
      out.println("    -H<command>                   Show command specific help");
      out.println("    -c=command.properties         Specify the command.properties file to use");
      out.println("    -D<name>[=<value>]            Set a system property");
      out.println("    --                            Stop processing options");
      out.println("    -s, --server=<url>            The JNDI URL of the remote server");
      out.println("    -a, --adapter=<name>          The JNDI name of the RMI adapter to use");
      out.println("    -u, --user=<name>             Specify the username for authentication");
      out.println("    -p, --password=<name>         Specify the password for authentication"); 
      out.println("    -P, --properties=<filename>   Load options from a property file");
      out.println("                                  Command line options override duplicates from the file");
      out.println("                                  Options: twiddle.user, twiddle.password,");
      out.println("                                  twiddle.server, twiddle.adapter, twiddle.command");
      out.println("                                  Anything else is set as a system property");
      out.println("    -q, --quiet                   Be somewhat more quiet");
      out.flush();
   }

   private static void processArguments(final String[] args) throws Exception
   {
      for(int a = 0; a < args.length; a ++)
      {
         if (!logPassword(args, a))
            log.debug("args["+a+"]="+args[a]);
      }
      String sopts = "-:hH:u:p:P:c:D:s:a:q";
      LongOpt[] lopts =
         {
            new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
            new LongOpt("help-commands", LongOpt.NO_ARGUMENT, null, 0x1000),
            new LongOpt("server", LongOpt.REQUIRED_ARGUMENT, null, 's'),
            new LongOpt("adapter", LongOpt.REQUIRED_ARGUMENT, null, 'a'),
            new LongOpt("quiet", LongOpt.NO_ARGUMENT, null, 'q'),
            new LongOpt("user", LongOpt.REQUIRED_ARGUMENT, null, 'u'),
            new LongOpt("password", LongOpt.REQUIRED_ARGUMENT, null, 'p'),
            new LongOpt("properties", LongOpt.REQUIRED_ARGUMENT, null, 'P'),
         };

      Getopt getopt = new Getopt(PROGRAM_NAME, args, sopts, lopts);
      int code;
      String propertyFile = null;

      PROCESS_ARGUMENTS:

        while ((code = getopt.getopt()) != -1)
        {
           switch (code)
           {
              case ':':
              case '?':
                 // for now both of these should exit with error status
                 System.exit(1);
                 break; // for completeness

                 // non-option arguments
              case 1:
                 {
                    // create the command
                    commandName = getopt.getOptarg();
                    log.debug("Command name: " + commandName);

                    // pass the remaining arguments (if any) to the command for processing
                    int i = getopt.getOptind();

                    if (args.length > i)
                    {
                       commandArgs = new String[args.length - i];
                       System.arraycopy(args, i, commandArgs, 0, args.length - i);
                    }
                    else
                    {
                       commandArgs = new String[0];
                    }
                    // Log the command options
                    log.debug("Command arguments: " + Strings.join(commandArgs, ","));

                    // We are done, execute the command
                    break PROCESS_ARGUMENTS;
                 }

                 // show command line help
              case 'h':
                 displayHelp();
                 System.exit(0);
                 break; // for completeness

                 // Show command help
              case 'H':
                 commandName = getopt.getOptarg();
                 commandHelp = true;
                 break PROCESS_ARGUMENTS;

                 // help-commands
              case 0x1000:
                 twiddle.displayCommandList();
                 System.exit(0);
                 break; // for completeness

              case 'c':
                 setCommandProperties(getopt.getOptarg());
                 break;
                 // set a system property
              case 'D':
                 {
                    String arg = getopt.getOptarg();
                    String name, value;
                    int i = arg.indexOf("=");
                    if (i == -1)
                    {
                       name = arg;
                       value = "true";
                    }
                    else
                    {
                       name = arg.substring(0, i);
                       value = arg.substring(i + 1, arg.length());
                    }
                    System.setProperty(name, value);
                    break;
                 }

                 // Set the JNDI server URL
              case 's':
                 twiddle.setServerURL(getopt.getOptarg());
                 break;

                 // Set the adapter JNDI name
              case 'a':
                 twiddle.setAdapterName(getopt.getOptarg());
                 break;
              case 'u':
                 twiddle.setUsername(getopt.getOptarg());                
                 break;
              case 'p':
                 twiddle.setPasword(getopt.getOptarg());               
                 break;
              case 'P':
                 propertyFile = getopt.getOptarg();		  
                 break;
              // Enable quiet operations
              case 'q':
                 twiddle.setQuiet(true);
                 break;
           }
        }
      
	   // done last as we want cmd line option to override the property file entries
	   if (propertyFile != null)
      {
         processPropertyFile(propertyFile);
      }
   }

   private static void setCommandProperties(final String props) throws MalformedURLException
   {
      // Try value as a URL
      try
      {
         cmdProps = new URL(props);
      }
      catch (MalformedURLException e)
      {
         log.debug("Failed to use cmd props as url", e);
         File path = new File(props);
         if( path.exists() == false )
         {
            String msg = "Failed to locate command props: " + props
                         + " as URL or file";
            throw new IllegalArgumentException(msg);
         }
         cmdProps = path.toURL();
      }      
   }
   
   /*
    * Options on the command line take precedence.
    * Unrecognised options are treated as a system property
    */
   private static void processPropertyFile(final String propertyFile) throws IOException
   {
      Properties fileProps = new Properties();
      FileInputStream in = null;
      String key;
      try
      {
         in = new FileInputStream(propertyFile);
         fileProps.load(in);
      }
      finally
      {
         try { in.close(); } catch (Exception e) {}
      }
      
      Enumeration names = fileProps.propertyNames();
      while (names.hasMoreElements())
      {
         key = (String)names.nextElement();
         
         if (!"twiddle.password".equals(key))
         {
            log.debug("Properties : " + key + "=" + fileProps.getProperty(key));
         }
         
         if ("twiddle.user".equals(key) && (twiddle.username == null))
         {
             twiddle.setUsername(fileProps.getProperty(key));
         }
         else if ("twiddle.password".equals(key) && (twiddle.password == null))
         {
            twiddle.setPasword(fileProps.getProperty(key));
         }
         else if ("twiddle.command".equals(key) && (cmdProps == null))
         {
            setCommandProperties(fileProps.getProperty(key));
         }
         else if ("twiddle.adapter".equals(key) && (twiddle.adapterName == null))
         {
            twiddle.setAdapterName(fileProps.getProperty(key));
         }
         else if ("twiddle.server".equals(key) && (twiddle.server == null))
         {
            twiddle.setServerURL(fileProps.getProperty(key));
         }
         else
         {
            if (System.getProperty(key) == null)
            {
               System.setProperty(key, fileProps.getProperty(key));
            }
         }
      }      
   }
   
   private static boolean logPassword(final String args[], int a)
   {
      // check current argument
      if (args[a].startsWith("-p") && args[a].length() > 2)
      {
         log.debug("args["+a+"]=-pxxxx");
         return true;
      }
      else if (args[a].indexOf('=') != -1)
      {
         String[] split = args[a].split("=");
         if ("--password".indexOf(split[0]) != -1)
         {
            log.debug("args["+a+"]="+split[0]+"=xxxx");
            return true;
         }
      }
      // check previous argument
      try {
         if (args[a-1].equals("-p") || (args[a-1].indexOf('=') == -1 && "--password".indexOf(args[a-1]) != -1))
         {
            log.debug("args["+a+"]=xxxx");
            return true;
         }
      }
      catch (IndexOutOfBoundsException ioobe)
      {
      }
      return false;
   }
}
