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
package org.jboss.test.jbossts.recovery;

import java.io.Serializable;

/**
 * Specification of what to do when a failure is injected.
 */
public class ASFailureMode implements Serializable
{
   private static final int _NONE = 0;
   private static final int _HALT = 1;
   private static final int _EXIT = 2;
   private static final int _SUSPEND = 3;
   private static final int _XAEXCEPTION = 4;
   private static final int _EJBEXCEPTION = 5;
   private static final int _ROLLBACK_ONLY = 6;

   private static final String _NONE_NAME = "NONE";
   private static final String _HALT_NAME = "HALT";
   private static final String _EXIT_NAME = "EXIT";
   private static final String _SUSPEND_NAME = "SUSPEND";
   private static final String _XAEXCEPTION_NAME = "XAEXCEPTION";
   private static final String _EJBEXCEPTION_NAME = "EJBEXCEPTION";
   private static final String _ROLLBACK_ONLY_NAME = "ROLLBACK_ONLY";

   /* just for simpler transition from the enum type */
   public static final ASFailureMode NONE = new ASFailureMode(_NONE);
   public static final ASFailureMode HALT = new ASFailureMode(_HALT);
   public static final ASFailureMode EXIT = new ASFailureMode(_EXIT);
   public static final ASFailureMode SUSPEND = new ASFailureMode(_SUSPEND);
   public static final ASFailureMode XAEXCEPTION = new ASFailureMode(_XAEXCEPTION);
   public static final ASFailureMode EJBEXCEPTION = new ASFailureMode(_EJBEXCEPTION);
   public static final ASFailureMode ROLLBACK_ONLY = new ASFailureMode(_ROLLBACK_ONLY);
   
   private int mode;
   
   public ASFailureMode(int mode)
   {
      this.mode = mode;
   }

   public boolean willTerminateVM()
   {
       switch (mode)
       {
          case _HALT:
          case _EXIT:
             return true;
          default:
             return false;
       }
   }

   public boolean isException()
   {
       switch (mode)
       {
          case _XAEXCEPTION:
          case _EJBEXCEPTION:
             return true;
          default:
             return false;
       }
   }

   /* enum like methods */
   public String name()
   {
      switch (mode)
      {
         case _NONE:
            return _NONE_NAME;
         case _HALT:
            return _HALT_NAME;
         case _EXIT:
            return _EXIT_NAME;
         case _SUSPEND:
            return _SUSPEND_NAME;
         case _XAEXCEPTION:
            return _XAEXCEPTION_NAME;
         case _EJBEXCEPTION:
            return _EJBEXCEPTION_NAME;
         case _ROLLBACK_ONLY:
            return _ROLLBACK_ONLY_NAME;
         default:
            return "uninstantiated";
      }
   }
   
   public int ordinal()
   {
      return mode;
   }

   public static ASFailureMode valueOf(String mode)
   {
      if (_NONE_NAME.equalsIgnoreCase(mode))
         return NONE;
      else if (_HALT_NAME.equalsIgnoreCase(mode))
         return HALT;
      else if (_EXIT_NAME.equalsIgnoreCase(mode))
         return EXIT;
      else if (_SUSPEND_NAME.equalsIgnoreCase(mode))
         return SUSPEND;
      else if (_XAEXCEPTION_NAME.equalsIgnoreCase(mode))
         return XAEXCEPTION;
      else if (_EJBEXCEPTION_NAME.equalsIgnoreCase(mode))
         return EJBEXCEPTION;
      else if (_ROLLBACK_ONLY_NAME.equalsIgnoreCase(mode))
         return ROLLBACK_ONLY;
      else
         throw new IllegalArgumentException("There is no such value of ASFailureMode like " + mode);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + mode;
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ASFailureMode other = (ASFailureMode) obj;
      if (mode != other.mode)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return name();
   }
   
   
/*   
    NONE
    
    ,HALT     // halt the JVM
    ,EXIT     // exit the JVM
    ,SUSPEND    // suspend the calling thread
    ,XAEXCEPTION    // fail via one of the xa exception codes
    ,EJBEXCEPTION   // fail via EJBException -- for using only with ASFailureType.PRE_PREPARE and SYNCH_BEFORE
    ,ROLLBACK_ONLY   // mark a transaction as rollback-only -- for using only with ASFailureType.PRE_PREPARE and SYNCH_BEFORE
    ;

    public boolean willTerminateVM()
    {
        switch (this)
        {
           case HALT:
           case EXIT:
              return true;
           default:
              return false;
        }
    }

    public static ASFailureMode toEnum(String mode)
    {
        return ASFailureMode.valueOf(mode.toUpperCase());
    }
    */
}
