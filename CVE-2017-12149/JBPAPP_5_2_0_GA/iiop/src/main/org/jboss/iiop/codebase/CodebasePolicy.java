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
package org.jboss.iiop.codebase;

import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;

/**
 * Implements <code>org.omg.CORBA.Policy</code> objects containing codebase
 * strings. 
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 */
public class CodebasePolicy 
      extends LocalObject
      implements Policy
{
   // Private -----------------------------------------------------------------

   private final String codebase;

   // Static  -----------------------------------------------------------------

   public static final int TYPE = 0x12345678; // TODO: contact request@omg.org
                                              //       to get a policy type

   // Constructor -------------------------------------------------------------

   public CodebasePolicy(String codebase)
   {
      this.codebase = codebase;
   }

   /**
    * Returns the codebase string contained in this Policy.
    */
   public String getCodebase()
   {
      return codebase;
   }

   // org.omg.CORBA.Policy operations -----------------------------------------

   /**
    * Returns a copy of the Policy object.
    */
   public Policy copy() 
   {
      return new CodebasePolicy(codebase);
   }
   
   /**
    * Destroys the Policy object.
    */
   public void destroy() 
   {
   }

   /**
    * Returns the constant value that corresponds to the type of the policy 
    * object.
    */
   public int policy_type() 
   {
      return TYPE;
   }

    public String toString()
    {
        return "CodebasePolicy[" + codebase + "]";
    }

}
