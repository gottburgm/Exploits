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
package org.jboss.ejb;

import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;
import javax.ejb.EnterpriseBean;

/** A PolicyContextHandler for the EJB invocation arguments.
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class EJBArgsPolicyContextHandler implements PolicyContextHandler
{
   public static final String EJB_ARGS_KEY = "javax.ejb.arguments";
   private static ThreadLocal ejbContext = new ThreadLocal();

   public static void setArgs(Object[] args)
   {
      ejbContext.set(args);
   }

   /** Access the EJB policy context data.
    * @param key  "javax.ejb.arguments"
    * @param data currently unused
    * @return Object[] for the active invocation args
    * @throws javax.security.jacc.PolicyContextException
    */ 
   public Object getContext(String key, Object data)
      throws PolicyContextException
   {
      Object context = null;
      if( key.equalsIgnoreCase(EJB_ARGS_KEY) == true )
         context = ejbContext.get();
      return context;
   }

   public String[] getKeys()
      throws PolicyContextException
   {
      String[] keys = {EJB_ARGS_KEY};
      return keys;
   }

   public boolean supports(String key)
      throws PolicyContextException
   {
      return key.equalsIgnoreCase(EJB_ARGS_KEY);
   }
}
