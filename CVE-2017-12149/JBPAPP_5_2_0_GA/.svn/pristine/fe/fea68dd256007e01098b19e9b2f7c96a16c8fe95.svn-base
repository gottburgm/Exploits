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
package org.jboss.test.compatibility.test.matrix;

import org.apache.log4j.Logger;


import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is a Test container for the MatrixTest.
 * For each instance running it will include a bunch of other tests passed by parameter.
 *
 * This class uses list of variables defined by the testSuite and they have to be in the System.getProperties()
 * jbosstest.hometest = Contains where we are loading the testcases
 * jbosstest.executionlist = A comma based list of .class files. Each file has to begin with ${jbosstest.hometest}
 * jbosstest.versionname = The name of the version being tested
 *
 * @author clebert.suconic@jboss.com
 */
public class MatrixTestContainer extends TestCase
{
    static Logger log = Logger.getLogger("MatrixTestContainer");

    /** Used to similuate tests while renaming its names. */
    private static class DummyTestCase extends TestCase
    {
        DummyTestCase(String name)
        {
            super (name);
        }
    }

    /** We need this proxy just to inform failures*/
    private static class TestSuiteProxy extends TestSuite
    {
        ArrayList loadFailures;
        public TestSuiteProxy(ArrayList loadFailures)
        {
            this.loadFailures=loadFailures;
        }

        public void run(TestResult testResult)
        {
            Iterator iter = loadFailures.iterator();
            while (iter.hasNext())
            {
                LoadFailure load = (LoadFailure)iter.next();
                TestCase test = new DummyTestCase(load.className);
                testResult.startTest(test);
                testResult.addError(test,load.exception);
            }

            loadFailures.clear();

            super.run(testResult);
        }


    }

    private static class LoadFailure
    {
        String className;
        Throwable exception;

        public LoadFailure(String className, Throwable exception)
        {
            this.className=className;
            this.exception=exception;
        }
    }

    /**
     * One of the goals of this class also is to keep original classNames into testNames. So, you will realize several proxies existent here to
     * keep these class names while executing method names.
     */
    static class TestProxy extends TestCase
    {
        Hashtable hashTests = new Hashtable();



        public TestProxy(Test testcase, String name)
        {
            super(name);
            this.testcase = testcase;
        }

        public int countTestCases()
        {
            return testcase.countTestCases();
        }

        /**
         * Create a dummy test renaming its content
         * @param test
         * @return
         */
        private Test createDummyTest(Test test)
        {
            Test dummyTest = (Test)hashTests.get(test);
            if (dummyTest==null)
            {
                if (test instanceof TestCase)
                {
                    dummyTest = new DummyTestCase(this.getName() + ":"+ ((TestCase)test).getName());
                } else
                if (test instanceof TestSuite)
                {
                    dummyTest = new DummyTestCase(this.getName() + ":"+ ((TestCase)test).getName());
                }
                else
                {
                    // if can't recover the name, don't create a proxy
                    log.warn("Couldn't find a name for " + test.toString() + ", class=" + test.getClass().getName());

                    dummyTest = new DummyTestCase(test.getClass().getName());
                }

                hashTests.put(test,dummyTest);
            }

            return dummyTest;
        }

        public void run(final TestResult result)
        {
            TestResult subResult = new TestResult();
            subResult.addListener(new TestListener()
            {
                public void addError(Test subtest, Throwable throwable)
                {
                    Test dummyTest = createDummyTest(subtest);
                    result.addError(dummyTest, throwable);
                }

                public void addFailure(Test subtest, AssertionFailedError assertionFailedError)
                {
                    Test dummyTest = createDummyTest(subtest);
                    result.addFailure(dummyTest, assertionFailedError);
                }

                public void endTest(Test subtest)
                {
                    Test dummyTest = createDummyTest(subtest);
                    result.endTest(dummyTest);
                }

                public void startTest(Test subtest)
                {
                    Test dummyTest = createDummyTest(subtest);
                    result.startTest(dummyTest);
                }
            });
            testcase.run(subResult);
        }

        Test testcase;
    }

    private static Test createSuite(Class clazz) throws Exception
    {
        Method method = null;
        try
        {
            method = clazz.getMethod("suite", null);
        }
        catch (Exception e)
        {
        }

        if (method != null)
        {
            return (Test) method.invoke(null, null);
        } else
        {
            TestSuite suiteTmp = new TestSuite();
            suiteTmp.addTestSuite(clazz);
            return suiteTmp;
        }
    }

    private static void copySuite(Test source, TestSuite destination, String baseName)
    {
        destination.addTest(new TestProxy(source,baseName));
    }

    /** As jdk1.3 doesn't have .slipt as a method on string, I've created this method */
    public static String[] split(String arguments)
    {

        ArrayList list = new ArrayList();
        while (arguments!=null && !arguments.equals(""))
        {
            int position = arguments.indexOf(',');

            if (position>=0)
            {
                 String currentString = arguments.substring(0,position);
                 list.add(currentString);
                 arguments = arguments.substring(position+1);
            }
            else
            {
                list.add(arguments);
                arguments = null;
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }


    public static Test suite()
    {
        try
        {
            String homedir = (String) System.getProperties().get("jbosstest.hometest");

            String executionList = (String) System.getProperties().get("jbosstest.executionlist");
            System.out.println("ExecutionList = " + executionList);

            String[] tests = split(executionList);

            ArrayList loadFailures = new ArrayList();

            TestSuite suite = new TestSuiteProxy(loadFailures);

            for (int classesCount = 0; classesCount < tests.length; classesCount++)
            {
                String testName = null;
                try
                {
                    if (tests[classesCount].trim().equals("")) continue;
                    testName = tests[classesCount].substring(homedir.length() + 1);
                    testName = testName.replace('/', '.');
                    testName = testName.substring(0, testName.length() - 6); // - ".class".length()

                    Class clazz = Class.forName(testName);
                    Test suiteTmp = createSuite(clazz);
                    copySuite(suiteTmp, suite, testName + ":");
                } catch (Throwable e)
                {
                    loadFailures.add(new LoadFailure(testName,e));
                    log.info("Error Loading " + testName);
                    e.printStackTrace();
                    log.warn(e.getMessage());
                }
            }

            log.info("All classes loaded, executing tests");

            return suite;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }


    }

}
