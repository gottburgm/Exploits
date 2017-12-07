package org.jboss.test.commons_logging.jbpapp6523.war;

import org.apache.commons.logging.Log;

/**
 * A dummy log class to cause the behavior described in JBPAPP-6523 when use_tccl=true.
 */
public class DummyLog implements Log
{
   public DummyLog()
   {
      // This should never print as the problem happens before commons logging
      // logger consruction.
      System.out.println("DummyLog consructor...");
   }

   public void debug(Object arg0)
   {
   }

   public void debug(Object arg0, Throwable arg1)
   {
   }

   public void error(Object arg0)
   {
   }

   public void error(Object arg0, Throwable arg1)
   {
   }

   public void fatal(Object arg0)
   {
   }

   public void fatal(Object arg0, Throwable arg1)
   {
   }

   public void info(Object arg0)
   {
   }

   public void info(Object arg0, Throwable arg1)
   {
   }

   public boolean isDebugEnabled()
   {
      return true;
   }

   public boolean isErrorEnabled()
   {
      return true;
   }

   public boolean isFatalEnabled()
   {
      return true;
   }

   public boolean isInfoEnabled()
   {
      return true;
   }

   public boolean isTraceEnabled()
   {
      return true;
   }

   public boolean isWarnEnabled()
   {
      return true;
   }

   public void trace(Object arg0)
   {
   }

   public void trace(Object arg0, Throwable arg1)
   {
   }

   public void warn(Object arg0)
   {
   }

   public void warn(Object arg0, Throwable arg1)
   {
   }
}