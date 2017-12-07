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
package org.jboss.iiop.rmi;

import org.omg.CORBA.ParameterMode;


/**
 *  Parameter analysis.
 *
 *  Routines here are conforming to the "Java(TM) Language to IDL Mapping
 *  Specification", version 1.1 (01-06-07).
 *      
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 81018 $
 */
public class ParameterAnalysis
   extends AbstractAnalysis
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   private static final org.jboss.logging.Logger logger = 
               org.jboss.logging.Logger.getLogger(ParameterAnalysis.class);

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   ParameterAnalysis(String javaName, Class cls)
      throws RMIIIOPViolationException
   {
      super(javaName);

      this.cls = cls;

      typeIDLName = Util.getTypeIDLName(cls);
      logger.debug("ParameterAnalysis(): cls=["+cls.getName()+
                   "] typeIDLName=["+typeIDLName+"].");
   }

   // Public --------------------------------------------------------

   /**
    *  Return my attribute mode.
    */
   public ParameterMode getMode()
   {
      // 1.3.4.4 says we always map to IDL "in" parameters.
      return ParameterMode.PARAM_IN;
   }
   
   /**
    *  Return my Java type.
    */
   public Class getCls()
   {
      return cls;
   }
   
   /**
    *  Return the IDL type name of my parameter type.
    */
   public String getTypeIDLName()
   {
      logger.debug("ParameterAnalysis.getTypeIDLName(): cls=["+cls.getName()+
                   "] typeIDLName=["+typeIDLName+"].");
      return typeIDLName;
   }
   
   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   /**
    *  Java type of parameter.
    */
   private Class cls;

   /**
    *  IDL type name of parameter type.
    */
   private String typeIDLName;

}
