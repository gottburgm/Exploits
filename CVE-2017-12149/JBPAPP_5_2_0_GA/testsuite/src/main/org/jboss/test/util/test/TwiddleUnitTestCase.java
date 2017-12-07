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
package org.jboss.test.util.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;

/**
 * Unit tests for the Twiddle command line utility.
 *
 * @author <a href="mailto:stan@jboss.org">Stan Silvert</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 */
public class TwiddleUnitTestCase extends JBossTestCase
{
  public TwiddleUnitTestCase(String name)
  {
     super(name);
  }

  public static Test suite() throws Exception
  {
     TestSuite suite = new TestSuite();
     suite.addTest(new TestSuite(TwiddleUnitTestCase.class));
     return suite;
  }

  /**
   * This really just tests that twiddle can be invoked.
   */
  public void testHelp() throws Exception
  {
     String result = runTwiddle("-h");
     assertTrue(result.contains("usage: twiddle"));
     assertTrue(result.contains("[options] <command> [command_arguments]"));
  }

  /**
   *  Test getting an attribute value from an MBean.
   */
  public void testGet() throws Exception
  {
     String result = runTwiddle("get", "\"jboss.system:type=Server\"", "Started");
     assertTrue(result.startsWith("Started=true"));
  }

  /**
   * Test invoking an operation on an MBean
   */
  public void testInvoke() throws Exception
  {
     try
     {
        // JBAS-5108 - check also that returned objects without
        // an available property editor are printed out 
        deploy("twiddle-test.sar");
        String result = runTwiddle("invoke", "\"test:service=SimpleService\"", "showHashMapAttr");
        assertTrue(result.indexOf("Exec failed") == -1);
     }
     finally
     {
        undeploy("twiddle-test.sar");
     }
  }
  
  /**
   * Run twiddle with the given arguments. This method relies on finding the twiddle
   * bat or sh file using the jboss.dist system property. It will automatically pass
   * in the -s parameter for the host of the server being used for testing.
   *
   * @param args The arguments passed to twiddle.  
   */
  protected String runTwiddle(String... args) throws IOException, InterruptedException
  {
     List<String> command = new ArrayList<String>();

     if (isWindows())
     {
        command.add("cmd");
        command.add("/C");
        command.add("twiddle");
        command.add("-s");
        command.add(getServerHost());
        command.addAll(Arrays.asList(args));
     }
     else
     {
        command.add("/bin/sh");
        command.add("-c");
        String twiddleCmd = "./twiddle.sh ";
        twiddleCmd += "-s ";
        twiddleCmd += getServerHost();
        twiddleCmd += makeTwiddleArgs(args);
        command.add(twiddleCmd);
     }

     ProcessBuilder builder = new ProcessBuilder(command);
     builder.directory(getTwiddleWorkingDir());
     builder.environment().put("JBOSS_HOME", getJBossHome());
     Process proc = builder.start();
     StringBuilder buffer = readStream(proc.getInputStream());

     if (log.isDebugEnabled()) debugTwiddle(builder, buffer, proc.getErrorStream());

     return buffer.toString();
  }

  protected StringBuilder readStream(InputStream in) throws IOException
  {
     StringBuilder buffer = new StringBuilder();
     int readByte = 0;
     while (readByte != -1)
     {
       readByte = in.read();
       if (readByte != -1)
       {
          buffer.append((char)readByte);
       }
     }
     return buffer;
  }

  protected String makeTwiddleArgs(String[] args)
  {
     String result = "";
     for (int i=0; i < args.length; i++)
     {
        result += " ";
        result += args[i];
     }
     return result;
  }

  protected void debugTwiddle(ProcessBuilder builder,  
                              StringBuilder buffer, 
                              InputStream errorStream) throws IOException
  {
     String command = "";
     for (String param: builder.command() )
     {
        command += param;
        command += " ";
     }

     log.debug("executed: " + command);
     log.debug("returned: " + buffer.toString());
     log.debug("error stream: " + readStream(errorStream).toString());
  }

  protected boolean isWindows()
  {
     return System.getProperty("os.name").toLowerCase().startsWith("windows");
  }

  protected String getJBossHome()
  {
     // usually, the jboss.dist system property is set in the ant <junit> task using
     // <sysproperty key="jboss.dist" value="${jboss.dist}"/>
     String jbossDist = System.getProperty("jboss.dist");
     if (jbossDist == null) throw new IllegalStateException("jboss.dist System property is not set");
     return jbossDist;
  }

  protected File getTwiddleWorkingDir()
  {
     String jbossBin = getJBossHome() + "/bin";
     log.debug("Twiddle working dir = " + jbossBin);
     return new File(jbossBin);
  }
}
