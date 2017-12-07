/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.adminclient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.SimpleCompletor;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.jboss.adminclient.command.ClientCommand;
import org.jboss.adminclient.command.ConnectCommand;
import org.jboss.adminclient.command.DisconnectCommand;
import org.jboss.adminclient.command.ListComponentsCommand;
import org.jboss.adminclient.command.ListDeploymentsCommand;
import org.jboss.adminclient.command.QuitCommand;
import org.jboss.adminclient.command.ReloadCommand;
import org.jboss.adminclient.command.HelpCommand;
import org.jboss.adminclient.command.LoadCommand;
import org.jboss.adminclient.connection.ProfileServiceConnection;

/**
 * @author Ian Springer
 */
public class AdminClientMain
{
    private static Class[] COMMAND_CLASSES = new Class[]{
            ConnectCommand.class,
            DisconnectCommand.class,
            HelpCommand.class,
            ListComponentsCommand.class,
            ListDeploymentsCommand.class,
            LoadCommand.class,
            QuitCommand.class,
            ReloadCommand.class
    };
    // Use a TreeMap, so the commands will be sorted by name (e.g. for display by the help command).
    private static final Map<String, ClientCommand> COMMANDS = new TreeMap<String, ClientCommand>();

    static
    {
        for (Class commandClass : COMMAND_CLASSES)
        {
            ClientCommand command;
            try
            {
                command = (ClientCommand)commandClass.newInstance();
                COMMANDS.put(command.getName(), command);
            }
            catch (Exception e)
            {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * This is the thread that is running the input loop; it accepts prompt commands from the user.
     */
    private Thread inputLoopThread;

    private BufferedReader inputReader;

    private ConsoleReader consoleReader;

    private boolean stdinInput = true;

    private PrintWriter outputWriter;

    private ProfileServiceConnection connection;

    String host;
    Integer port;
    String username;
    String password;
    boolean verbose;

    public static void main(String[] args) throws Exception
    {
        /*Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                new UnixTerminal().restoreTerminal();     }
                catch (Exception e) { }
            }
        }));*/
        try
        {
//            new UnixTerminal().initializeTerminal();

            AdminClientMain main = new AdminClientMain();
            main.processArguments(args);
            main.inputLoop();
        }
        finally
        {
            //new UnixTerminal().restoreTerminal();
        }
    }

    public AdminClientMain() throws Exception
    {

//        this.inputReader = new BufferedReader(new InputStreamReader(System.in));
        this.outputWriter = new PrintWriter(System.out, true);

        consoleReader = new jline.ConsoleReader();
        consoleReader.addCompletor(
                new SimpleCompletor(COMMANDS.keySet().toArray(new String[COMMANDS.size()])));
        consoleReader.addCompletor(
                new ArgumentCompletor(
                        new Completor[]{
                                new SimpleCompletor("help"),
                                new SimpleCompletor(COMMANDS.keySet().toArray(new String[COMMANDS.size()]))}));

        consoleReader.setUsePagination(true);

    }

    public void start()
    {
        outputWriter = new PrintWriter(System.out);
//        inputReader = new BufferedReader(new InputStreamReader(System.in));

    }

    public String getUserInput(String prompt)
    {
        String inputString = "";
        boolean useDefaultPrompt = (prompt == null);

        while ((inputString != null) && (inputString.trim().length() == 0))
        {
            if (prompt == null)
                prompt = isConnected() ? (this.host + ":" + this.port + "> ") : "disconnected> ";
            try
            {
                this.outputWriter.flush();
                inputString = this.consoleReader.readLine(prompt);
            }
            catch (Exception e)
            {
                inputString = null;
            }
        }

        if (inputString != null)
        {
            // if we are processing a script, show the input that was just read in
            if (!stdinInput)
            {
                outputWriter.println(inputString);
            }
        }
        else if (!stdinInput)
        {
            // if we are processing a script, we hit the EOF, so close the input stream
            try
            {
                inputReader.close();
            }
            catch (IOException e1)
            {
            }

            // if we are not in daemon mode, let's now start processing prompt commands coming in via stdin
//            if (!m_daemonMode) {
//                inputReader = new BufferedReader(new InputStreamReader(System.in));
//                stdinInput = true;
//                input_string = "";
//            } else {
//                inputReader = null;
//            }
        }

        return inputString;
    }

    public boolean isConnected()
    {
        return (this.connection != null && this.connection.getConnectionProvider().isConnected());
    }

    /**
     * This enters in an infinite loop. Because this never returns, the current thread never dies and hence the agent
     * stays up and running. The user can enter agent commands at the prompt - the commands are sent to the agent as if
     * the user is a remote client.
     */
    private void inputLoop()
    {
        // we need to start a new thread and run our loop in it; otherwise, our shutdown hook doesn't work
        Runnable loopRunnable = new Runnable()
        {
            public void run()
            {
                while (true)
                {
                    // get a command from the user
                    // if in daemon mode, only get input if reading from an input file; ignore stdin
                    String cmd;
//                        if ((m_daemonMode == false) || (stdinInput == false)) {
                    cmd = getUserInput(null);
//                        } else {
//                            cmd = null;
//                        }

                    boolean continueRunning = executeCommand(cmd);
                    // break the input loop if the prompt command told us to exit
                    // if we are not in daemon mode, this really will end up killing the agent
                    if (!continueRunning)
                        break;
                }

                return;
            }
        };

        // Start the input thread.
        inputLoopThread = new Thread(loopRunnable);
        inputLoopThread.setName("JBoss AS Admin Client Prompt Input Thread");
        inputLoopThread.setDaemon(false);
        inputLoopThread.start();

        return;
    }

    private boolean executeCommand(String cmd)
    {
        boolean continueRunning;
        try
        {
            String[] cmdArgs = parseCommandLine(cmd);
            continueRunning = executeCommand(cmdArgs);
        }
        catch (RuntimeException e)
        {
            e.printStackTrace(getPrintWriter()); // TODO: handle better
            continueRunning = true;
        }
        return continueRunning;
    }

    private boolean executeCommand(String[] args)
    {
        String commandName = args[0];
        if (COMMANDS.containsKey(commandName))
        {
            ClientCommand command = COMMANDS.get(commandName);
            if (command.isConnectionRequired() && !isConnected())
            {
                outputWriter.println("The '" + commandName
                        + "' command requires a connection. Please run the 'connect' command first.");
                return true;
            }
            String[] params = new String[args.length - 1];
            System.arraycopy(args, 1, params, 0, args.length - 1);
            OptionParser optionParser = command.getOptionParser();
            optionParser.acceptsAll(asList("h", "?", "help"), "display help");
            OptionSet options = optionParser.parse(params);
            if (options.has("help"))
            {
                try
                {
                    optionParser.printHelpOn(this.outputWriter);
                }
                catch (IOException e)
                {
                    throw new IllegalStateException(e);
                }
            }
            else
            {
                try
                {
                    return command.execute(this, options);
                }
                catch (Exception e)
                {
                    getPrintWriter().write("Command failed: " + e.getLocalizedMessage());
                    e.printStackTrace(getPrintWriter());
                }
            }
        }
        else
        {
            //return COMMANDS.get("exec").execute(this, args);
            outputWriter.println("Unknown command: " + commandName);
        }
        return true;
    }


    /**
     * Given a command line, this will parse each argument and return the argument array.
     *
     * @param cmdLine the command line
     * @return the array of command line arguments
     */
    private String[] parseCommandLine(String cmdLine)
    {
        ByteArrayInputStream in = new ByteArrayInputStream(cmdLine.getBytes());
        StreamTokenizer strtok = new StreamTokenizer(new InputStreamReader(in));
        List<String> args = new ArrayList<String>();
        boolean keepGoing = true;

        // we don't want to parse numbers and we want ' to be a normal word character
        strtok.ordinaryChars('0', '9');
        strtok.ordinaryChar('.');
        strtok.ordinaryChar('-');
        strtok.ordinaryChar('\'');
        strtok.wordChars(33, 127);
        strtok.quoteChar('\"');

        // parse the command line
        while (keepGoing)
        {
            int nextToken;

            try
            {
                nextToken = strtok.nextToken();
            }
            catch (IOException e)
            {
                nextToken = StreamTokenizer.TT_EOF;
            }

            if (nextToken == java.io.StreamTokenizer.TT_WORD)
            {
                args.add(strtok.sval);
            }
            else if (nextToken == '\"')
            {
                args.add(strtok.sval);
            }
            else if ((nextToken == java.io.StreamTokenizer.TT_EOF) || (nextToken == java.io.StreamTokenizer.TT_EOL))
            {
                keepGoing = false;
            }
        }

        return args.toArray(new String[args.size()]);
    }


    private void displayUsage()
    {
        outputWriter.println("rhq-client.sh [-h] [-u user] [-p pass] [-s host] [-t port] [-f file]");
    }


    void processArguments(String[] args) throws IllegalArgumentException, IOException
    {
        OptionParser optionParser = new OptionParser()
        {
            {
                acceptsAll(asList("H", "host")).withRequiredArg().ofType(String.class)
                        .describedAs("the JBoss AS instance's JNP host (may be a hostname or an IP address)");
                acceptsAll(asList("P", "port")).withRequiredArg().ofType(Integer.class)
                        .describedAs("the JBoss AS instance's the JNP port");
                acceptsAll(asList("u", "username")).withRequiredArg().ofType(String.class)
                        .describedAs("the username used to authenticate against the JBoss AS Profile Service");
                acceptsAll(asList("p", "password")).withRequiredArg().ofType(String.class)
                        .describedAs("the password used to authenticate against the JBoss AS Profile Service");
                acceptsAll(asList("e", "execute")).withRequiredArg().ofType(String.class)
                        .describedAs("a semicolon-separated list of commands to execute");
                acceptsAll(asList("h", "?", "help"), "display help");
                acceptsAll(asList("v", "verbose"), "enable verbose output");
                //accepts( "output-file" ).withOptionalArg().ofType( File.class ).describedAs( "file" );
            }
        };

        OptionSet options = optionParser.parse(args);

        if (options.has("help"))
            optionParser.printHelpOn(this.outputWriter);

        this.host = (String)options.valueOf("host");
        this.port = (Integer)options.valueOf("port");
        this.username = (String)options.valueOf("username");
        this.password = (String)options.valueOf("password");
        this.verbose = options.has("verbose");

        String execute = (String)options.valueOf("execute");
        if (execute != null) {
            String[] tokens = execute.split(";");
            for (String token : tokens)
            {
                String commandName = token.trim();
                if (!executeCommand(commandName))
                    System.exit(0);
            }
        }

        ClientCommand connectCommand = COMMANDS.get(ConnectCommand.COMMAND_NAME);
        OptionParser connectOptionParser = connectCommand.getOptionParser();
        List<String> connectOptions = new ArrayList<String>();
        if (this.host != null)
        {
            connectOptions.add("--host");
            connectOptions.add(this.host);
        }
        if (this.port != null)
        {
            connectOptions.add("--port");
            connectOptions.add(this.port.toString());
        }
        if (this.username != null)
        {
            connectOptions.add("--username");
            connectOptions.add(this.username);
        }
        if (this.password != null)
        {
            connectOptions.add("--password");
            connectOptions.add(this.password);
        }
        OptionSet connectOptionSet = connectOptionParser.parse(connectOptions.toArray(new String[connectOptions.size()]));
        connectCommand.execute(this, connectOptionSet);
    }

/*
    public RHQRemoteClient getRemoteClient() {
        return remoteClient;
    }

    public void setRemoteClient(RHQRemoteClient remoteClient) {
        this.remoteClient = remoteClient;
        if (remoteClient != null) {
            consoleReader.addCompletor(
                    new ArgumentCompletor(
                            new Completor[]{
                                    new SimpleCompletor("help"),
                                    new SimpleCompletor("api"),
                                    new SimpleCompletor(this.getRemoteClient().getAllServices().keySet().toArray(new String[this.getRemoteClient().getAllServices().size()]))}));

            consoleReader.addCompletor(new ServiceCompletor(this.getRemoteClient().getAllServices()));
        }
    }
*/

    public PrintWriter getPrintWriter()
    {
        return outputWriter;
    }

    public int getConsoleWidth()
    {
        return this.consoleReader.getTermwidth();
    }

    public Map<String, ClientCommand> getCommands()
    {
        return COMMANDS;
    }

    public ProfileServiceConnection getConnection()
    {
        return connection;
    }

    public void setConnection(ProfileServiceConnection connection)
    {
        this.connection = connection;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setPort(Integer port)
    {
        this.port = port;
    }

    public boolean isVerbose()
    {
        return this.verbose;
    }

    private static <T> List<T> asList(T... items)
    {
        return Arrays.asList(items);
    }
}
