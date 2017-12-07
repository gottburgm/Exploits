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
 * Specification of when to inject a failure.
 */
public class ASFailureType implements Serializable
{
   private static final int _NONE            = 1;
   private static final int _PRE_PREPARE     = 2;
   private static final int _XARES_START     = 3;
   private static final int _XARES_END       = 4;
   private static final int _XARES_PREPARE   = 5;
   private static final int _XARES_ROLLBACK  = 6;
   private static final int _XARES_COMMIT    = 7;
   private static final int _XARES_RECOVER   = 8;
   private static final int _XARES_FORGET    = 9;
   private static final int _SYNCH_BEFORE    = 10;
   private static final int _SYNCH_AFTER     = 11;
   
   private static final String _NONE_NAME            = "NONE";
   private static final String _PRE_PREPARE_NAME     = "PRE_PREPARE";
   private static final String _XARES_START_NAME     = "XARES_START";
   private static final String _XARES_END_NAME       = "XARES_END";
   private static final String _XARES_PREPARE_NAME   = "XARES_PREPARE";
   private static final String _XARES_ROLLBACK_NAME  = "XARES_ROLLBACK";
   private static final String _XARES_COMMIT_NAME    = "XARES_COMMIT";
   private static final String _XARES_RECOVER_NAME   = "XARES_RECOVER";
   private static final String _XARES_FORGET_NAME    = "XARES_FORGET";
   private static final String _SYNCH_BEFORE_NAME    = "SYNCH_BEFORE";
   private static final String _SYNCH_AFTER_NAME     = "SYNCH_AFTER";
   
   public static final ASFailureType NONE             = new ASFailureType(_NONE);
   public static final ASFailureType PRE_PREPARE      = new ASFailureType(_PRE_PREPARE);
   public static final ASFailureType XARES_START      = new ASFailureType(_XARES_START);
   public static final ASFailureType XARES_END        = new ASFailureType(_XARES_END);
   public static final ASFailureType XARES_PREPARE    = new ASFailureType(_XARES_PREPARE);
   public static final ASFailureType XARES_ROLLBACK   = new ASFailureType(_XARES_ROLLBACK);
   public static final ASFailureType XARES_COMMIT     = new ASFailureType(_XARES_COMMIT);
   public static final ASFailureType XARES_RECOVER    = new ASFailureType(_XARES_RECOVER);
   public static final ASFailureType XARES_FORGET     = new ASFailureType(_XARES_FORGET);
   public static final ASFailureType SYNCH_BEFORE     = new ASFailureType(_SYNCH_BEFORE);
   public static final ASFailureType SYNCH_AFTER      = new ASFailureType(_SYNCH_AFTER);
   
   private int type;
   
   public ASFailureType(int type)
   {
      this.type = type;
   }

   public boolean isXA()
   {
       return name().startsWith("XARES");
   }

   public boolean isSynchronization()
   {
       return name().startsWith("SYNCH");
   }

   public boolean isPreCommit()
   {
       return equals(PRE_PREPARE);
   }

   /* enum like methods */
   public String name()
   {
      switch (type)
      {
         case _NONE:
            return _NONE_NAME;
         case _PRE_PREPARE:
            return _PRE_PREPARE_NAME;
         case _XARES_START:
            return _XARES_START_NAME;
         case _XARES_END:
            return _XARES_END_NAME;
         case _XARES_PREPARE:
            return _XARES_PREPARE_NAME;
         case _XARES_ROLLBACK:
            return _XARES_ROLLBACK_NAME;
         case _XARES_COMMIT:
            return _XARES_COMMIT_NAME;
         case _XARES_RECOVER:
            return _XARES_RECOVER_NAME;
         case _XARES_FORGET:
            return _XARES_FORGET_NAME;
         case _SYNCH_BEFORE:
            return _SYNCH_BEFORE_NAME;
         case _SYNCH_AFTER:
            return _SYNCH_AFTER_NAME;
         default:
            return "uninstantiated";
      }
   }
   
   public int ordinal()
   {
      return type;
   }

   public static ASFailureType valueOf(String mode)
   {
      if (_NONE_NAME.equalsIgnoreCase(mode))
         return NONE;
      else if (_PRE_PREPARE_NAME.equalsIgnoreCase(mode))
         return PRE_PREPARE;
      else if (_XARES_START_NAME.equalsIgnoreCase(mode))
         return XARES_START;
      else if (_XARES_END_NAME.equalsIgnoreCase(mode))
         return XARES_END;
      else if (_XARES_PREPARE_NAME.equalsIgnoreCase(mode))
         return XARES_PREPARE;
      else if (_XARES_ROLLBACK_NAME.equalsIgnoreCase(mode))
         return XARES_ROLLBACK;
      else if (_XARES_COMMIT_NAME.equalsIgnoreCase(mode))
         return XARES_COMMIT;
      else if (_XARES_RECOVER_NAME.equalsIgnoreCase(mode))
         return XARES_RECOVER;
      else if (_XARES_FORGET_NAME.equalsIgnoreCase(mode))
         return XARES_FORGET;
      else if (_SYNCH_BEFORE_NAME.equalsIgnoreCase(mode))
         return SYNCH_BEFORE;
      else if (_SYNCH_AFTER_NAME.equalsIgnoreCase(mode))
         return SYNCH_AFTER;
      else
         throw new IllegalArgumentException("There is no such value of ASFailureType like " + mode);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + type;
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
      ASFailureType other = (ASFailureType) obj;
      if (type != other.type)
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

    ,PRE_PREPARE // do something before prepare is called

    ,XARES_START    // failures specific to the XA protocol
    ,XARES_END
    ,XARES_PREPARE
    ,XARES_ROLLBACK
    ,XARES_COMMIT
    ,XARES_RECOVER
    ,XARES_FORGET

    ,SYNCH_BEFORE   // do something before completion
    ,SYNCH_AFTER
    ;
    
    public static ASFailureType toEnum(String type)
    {
        return ASFailureType.valueOf(type.toUpperCase());
    }

    public boolean isXA()
    {
        return name().startsWith("XARES");
    }

    public boolean isSynchronization()
    {
        return name().startsWith("SYNCH");
    }

    public boolean isPreCommit()
    {
        return equals(PRE_PREPARE);
    }
*/
}
