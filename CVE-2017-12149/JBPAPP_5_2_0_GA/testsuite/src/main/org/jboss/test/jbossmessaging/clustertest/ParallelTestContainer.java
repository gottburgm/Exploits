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
package org.jboss.test.jbossmessaging.clustertest;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;

/**
 * 
 * @author <a href="clebert.suconic@jboss.com">Clebert Suconic</a>
 *
 */
public abstract class ParallelTestContainer implements Test
{

    public abstract ParallelTest[] getTests();
    
    public void run(TestResult res)
    {
        AggregatorListener listener = new AggregatorListener(res);
        
        ParallelTest tests[] = getTests();
        
        for (ParallelTest test: tests)
        {
            test.setListener(listener);
            test.startTest();
        }
        
        try
        {
            doTest();
        }
        catch (Throwable e)
        {
            res.addError(this, e);
        }
        
        for (ParallelTest test: tests)
        {
            try
            {
                test.join();
            } catch (InterruptedException ignored)
            {
                // this should never happen, but case it ever happens doesn't hurt anything some debug info on the System.out
                ignored.printStackTrace();
            }
        }
        
    }
    
    
    // This execution point could be used to do some logic as tests are being executed
    public abstract void doTest() throws Throwable;
    
    
   
    class AggregatorListener implements TestListener
    {
        
        TestResult target;

        public AggregatorListener(TestResult target)
        {
            this.target=target;
        }

        public void addError(Test test, Throwable t)
        {
            
            target.addError(test, t);
        }

        public void addFailure(Test test, AssertionFailedError t)
        {
            target.addFailure(test, t);
        }

        public void endTest(Test test)
        {
            target.endTest(test);
        }

        public void startTest(Test test)
        {
            target.startTest(test);
        }
    }
    
    public int countTestCases()
    {
        return 1;
    }
    
    

}
