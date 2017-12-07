/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2008,
 * @author JBoss Inc.
 */
package org.jboss.test.jbossts.taskdefs;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * General client action that can call JUnit based client tests 
 * and write their result to an xml report.
 * @see JUnitClientTest
 * 
 * @author <a href="istudens@redhat.com">Ivo Studensky</a>
 * @version $Revision: 1.1 $
 */
public class JUnitClientAction implements ClientAction
{
   private boolean isDebug = false;


   public boolean execute(ASTestConfig config, Map<String, String> params)
   {
      String testClass      = null;
      String name           = "Test";
      String reportFile     = null;

      for (Map.Entry<String, String> me : params.entrySet())
      {
         String key = me.getKey().trim();
         String val = me.getValue().trim();

         if ("name".equals(key))
            name = val;
         else if ("debug".equals(key))
            isDebug = val.equalsIgnoreCase("true");
         else if ("reportFile".equals(key))
            reportFile = val;
         else if ("testClass".equals(key))
            testClass = val;
      }

      try
      {
         TestResult result = new TestResult();

         XMLJUnitResultFormatter resultFormatter = new XMLJUnitResultFormatter();

         JUnitTest dummyJUnit = new JUnitTest(name);
         resultFormatter.startTestSuite(dummyJUnit);

         OutputStream writer = new FileOutputStream(new File(reportFile));
         resultFormatter.setOutput(writer);

         result.addListener(resultFormatter);

         TestSuite suite = new TestSuite();

         
         JUnitClientTest test = null;
         try
         {
            test = (JUnitClientTest) Class.forName(testClass).newInstance();
         }
         catch (ClassCastException e)
         {
            System.err.println("Class " + testClass + " does not implement " + JUnitClientTest.class.getName());
         }
         catch (ClassNotFoundException e)
         {
            System.err.println("Cannot locate class " + testClass);
         }
         catch (IllegalAccessException e)
         {
            e.printStackTrace();
         }
         catch (InstantiationException e)
         {
            System.err.println("Class " + testClass + " cannot be instantiated: " + e.getMessage());
         }

         test.setName("testAction");
         test.init(config, params, isDebug);

         suite.addTest(test);

         long startTime = new Date().getTime();
         suite.run(result);
         long endTime = new Date().getTime();
         
         dummyJUnit.setCounts(result.runCount(), result.failureCount(), result.errorCount());
         dummyJUnit.setRunTime(endTime - startTime);
         
         resultFormatter.endTestSuite(dummyJUnit);

         writer.close(); 

         return result.wasSuccessful();
      }
      catch (Exception e)
      {
         if (isDebug)
            e.printStackTrace();

         throw new BuildException(e);
      }
   }

   public boolean cancel() throws UnsupportedOperationException
   {
      throw new UnsupportedOperationException("TODO");
   }

   static void print(String msg)
   {
      System.out.println(msg);
   }

}
