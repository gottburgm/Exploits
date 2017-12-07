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
package org.jboss.verifier.strategy;

// $Id: AbstractEJB2xVerifier.java 81030 2008-11-14 12:59:42Z dimitris@jboss.org $

import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.QueryMetaData;
import org.jboss.util.Classes;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract EJB 2.x bean verifier.
 *
 * @author Thomas.Diesler@jboss.org
 * @since  08-Feb-2005
 */

public abstract class AbstractEJB2xVerifier extends AbstractVerifier
{
   protected EJBVerifier11 cmp1XVerifier;

   // The classes for an EJB
   protected Class bean;
   protected Class home;
   protected Class remote;
   protected Class localHome;
   protected Class local;
   protected Class serviceEndpointInterface;

   /*
    * Constructor
    */
   public AbstractEJB2xVerifier(VerificationContext context)
   {
      super(context);
      cmp1XVerifier = new EJBVerifier11(context);
   }

   /**
    * Check whether the given method is a create(...) method
    */
   public boolean isCreateMethod(Method m)
   {
      return m.getName().startsWith(CREATE_METHOD);
   }

   public boolean isEjbCreateMethod(Method m)
   {
      return m.getName().startsWith(EJB_CREATE_METHOD);
   }

   public boolean isEjbRemoveMethod(Method m)
   {
      return m.getName().startsWith(EJB_REMOVE_METHOD);
   }

   public boolean isEjbSelectMethod(Method m)
   {
      return m.getName().startsWith(EJB_SELECT_METHOD);
   }

   public boolean isEjbHomeMethod(Method m)
   {
      return m.getName().startsWith(EJB_HOME_METHOD);
   }

   /** Finds java.rmi.Remote interface from the class
    */
   public boolean hasRemoteInterface(Class c)
   {
      return isAssignableFrom("java.rmi.Remote", c);
   }

   /**
    * Return all ejbSelect methods
    */
   public Iterator getEjbSelectMethods(Class c)
   {
      List selects = new LinkedList();
      Method[] method = c.getMethods();

      for (int i = 0; i < method.length; ++i)
      {
         if (isEjbSelectMethod(method[i]))
         {
            selects.add(method[i]);
         }
      }

      return selects.iterator();
   }

   /**
    * Searches for an instance of an ejbRemove method from the class
    */
   public boolean hasEJBRemoveMethod(Class c)
   {
      Method[] method = c.getMethods();
      for (int i = 0; i < method.length; ++i)
      {
         if (isEjbRemoveMethod(method[i]))
            return true;
      }

      return false;
   }

   /**
    * Returns the ejbRemove(...) methods of a bean
    */
   public Iterator getEJBRemoveMethods(Class c)
   {
      List ejbRemoves = new LinkedList();
      Method[] method = c.getMethods();

      for (int i = 0; i < method.length; ++i)
      {
         if (isEjbRemoveMethod(method[i]))
            ejbRemoves.add(method[i]);
      }

      return ejbRemoves.iterator();
   }

   /**
    * Home methods are any method on the home interface which is
    * neither a create or find method.
    */
   public Iterator getHomeMethods(Class c)
   {
      List homes = new LinkedList();
      Method[] method = c.getMethods();

      for (int i = 0; i < method.length; ++i)
      {
         if (!isCreateMethod(method[i]) && !isFinderMethod(method[i]))
            homes.add(method[i]);
      }

      return homes.iterator();
   }

   public Iterator getEjbHomeMethods(Class c)
   {
      List homes = new LinkedList();
      Method[] method = c.getMethods();

      for (int i = 0; i < method.length; ++i)
      {
         if (isEjbHomeMethod(method[i]))
            homes.add(method[i]);
      }

      return homes.iterator();
   }

   /**
    * Check whether there is a matching &lt;query&gt; Element defined
    * for the Method m
    *
    * @param m Method to check, should be either a Finder or a Select
    * @param e EntityMetaData
    *
    * @return <code>true</code> if a matching &lt;query&gt; Element
    *         was located.
    */
   protected boolean hasMatchingQuery(Method m, EntityMetaData e)
   {
      boolean result = false;

      Iterator qIt = e.getQueries();
      while (qIt.hasNext())
      {
         QueryMetaData qmd = (QueryMetaData)qIt.next();

         // matching names
         if (!qmd.getMethodName().equals(m.getName()))
         {
            continue;
         }

         Class[] methodParameter = m.getParameterTypes();
         Class[] queryParameter = null;

         try
         {
            queryParameter = Classes.convertToJavaClasses(qmd.getMethodParams(), classloader);
         }
         catch (ClassNotFoundException cnfe)
         {
            // FIXME: this should be handled differently, especially
            //        there shouldn't be a DeploymentException being
            //        thrown ...
            continue;
         }

         // number of parameters has to match
         if (methodParameter.length != queryParameter.length)
         {
            continue;
         }

         // walk the parameter list and compare for equality
         boolean parametersMatch = true;
         for (int i = 0; i < methodParameter.length; i++)
         {
            if (!methodParameter[i].equals(queryParameter[i]))
            {
               parametersMatch = false;
               break;
            }
         }

         if (parametersMatch)
         {
            result = true;
            break;
         }
      }

      return result;
   }
}
