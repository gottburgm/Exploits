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
package x;

// EXPLICIT IMPORTS
import a.b.C1; // GOOD
import a.b.C2;
import a.b.C3;

// DO NOT WRITE
import a.b.*;  // BAD

// DO NOT USE "TAB" TO INDENT CODE USE *3* SPACES FOR PORTABILITY AMONG EDITORS

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:{email}">{full name}</a>.
 * @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @version $Revision: 81030 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go 
 *            beyond the cvs commit message
 * </ul>
 *  eg: 
 * <p><b>20010516 marc fleury:</b>
 * <ul>
 * <li> Ask all developers to clearly document the Revision, 
 *            changed the header.  
 * </ul>
 */
public class X
   extends Y
   implements Z
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   public void startService() throws Exception
   { // Use the newline for the opening bracket so we can match top and bottom bracket visually
      
      Class cls = Class.forName(dataSourceClass);
      vendorSource = (XADataSource)cls.newInstance();
      
      // JUMP A LINE BETWEEN LOGICALLY DISCTINT **STEPS** AND ADD A LINE OF COMMENT TO IT
      cls = vendorSource.getClass();
      
      if(properties != null && properties.length() > 0)
      {
      
         try
         {
         }
         catch (IOException ioe)
         {
         }
         for (Iterator i = props.entrySet().iterator(); i.hasNext();)
         {
            
            // Get the name and value for the attributes
            Map.Entry entry = (Map.Entry) i.next();
            String attributeName = (String) entry.getKey();
            String attributeValue = (String) entry.getValue();
            
            // Print the debug message
            log.debug("Setting attribute '" + attributeName + "' to '" +
               attributeValue + "'");
            
            // get the attribute 
            Method setAttribute =
            cls.getMethod("set" + attributeName,
               new Class[] { String.class });
            
            // And set the value  
            setAttribute.invoke(vendorSource,
               new Object[] { attributeValue });
         }
      }
      
      // Test database
      vendorSource.getXAConnection().close();
      
      // Bind in JNDI
      bind(new InitialContext(), "java:/"+getPoolName(),
         new Reference(vendorSource.getClass().getName(),
            getClass().getName(), null));
   }
   
   // Z implementation ----------------------------------------------
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
