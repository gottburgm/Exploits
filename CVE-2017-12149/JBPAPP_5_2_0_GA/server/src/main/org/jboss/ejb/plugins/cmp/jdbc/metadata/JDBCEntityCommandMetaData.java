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
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.util.Iterator;
import java.util.HashMap;
import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

/**
 * This immutable class contains information about entity command
 *
 * @author <a href="mailto:loubyansky@ua.fm">Alex Loubyansky</a>
 * @version $Revision: 81030 $
 */
public final class JDBCEntityCommandMetaData
{

   // Attributes -----------------------------------------------------

   /** The name (alias) of the command. */
   private final String commandName;

   /** The class of the command */
   private final Class commandClass;

   /** Command attributes */
   private final HashMap attributes = new HashMap();
  

   // Constructor ----------------------------------------------------

   /**
    * Constructs a JDBCEntityCommandMetaData reading the entity-command element
    * @param element - entity-command element
    */
   public JDBCEntityCommandMetaData( Element element )
      throws DeploymentException
   {
      // command name
      commandName = element.getAttribute( "name" );
      if( commandName.trim().length() < 1 )
      {
         throw new DeploymentException( "entity-command element must have "
            + " not empty name attribute" );
      }

      String commandClassStr = element.getAttribute( "class" );
      if(commandClassStr != null)
      {
         try
         {
            commandClass = GetTCLAction.
               getContextClassLoader().loadClass( commandClassStr );
         } catch (ClassNotFoundException e) {
            throw new DeploymentException( "Could not load class: "
               + commandClassStr);
         }
      }
      else
      {
         commandClass = null;
      }

      // attributes
      for( Iterator iter = MetaData.getChildrenByTagName( element, "attribute" );
         iter.hasNext(); )
      {
         Element attrEl = (Element) iter.next();

         // attribute name
         String attrName = attrEl.getAttribute( "name" );
         if( attrName == null )
         {
            throw new DeploymentException( "entity-command " + commandName
               + " has an attribute with no name" );
         }

         // attribute value
         String attrValue = MetaData.getElementContent( attrEl );

         attributes.put( attrName, attrValue );
      }
   }

   /**
    * Constructs a JDBCEntityCommandMetaData from entity-command  xml element
    * and default values
    * @param element entity-command element
    */
   public JDBCEntityCommandMetaData( Element element,
                                     JDBCEntityCommandMetaData defaultValues )
      throws DeploymentException
   {
      // command name
      commandName = defaultValues.getCommandName();

      String commandClassStr = element.getAttribute( "class" );
      if( (commandClassStr != null)
         && (commandClassStr.trim().length() > 0) )
      {
         try
         {
            commandClass = GetTCLAction.
               getContextClassLoader().loadClass( commandClassStr );
         } catch (ClassNotFoundException e) {
            throw new DeploymentException( "Could not load class: "
               + commandClassStr);
         }
      }
      else
      {
         commandClass = defaultValues.getCommandClass();
      }

      // attributes
      attributes.putAll( defaultValues.attributes );
      for( Iterator iter = MetaData.getChildrenByTagName( element, "attribute" );
         iter.hasNext(); )
      {
         Element attrEl = (Element) iter.next();

         // attribute name
         String attrName = attrEl.getAttribute( "name" );
         if( attrName == null )
         {
            throw new DeploymentException( "entity-command " + commandName
               + " has an attribute with no name" );
         }

         // attribute value
         String attrValue = MetaData.getElementContent( attrEl );

         attributes.put( attrName, attrValue );
      }
   }

   // Public ----------------------------------------------------------

   /**
    * @return the name of the command
    */
   public String getCommandName() {
      return commandName;
   }

   /**
    * @return the class of the command
    */
   public Class getCommandClass() {
      return commandClass;
   }

   /**
    * @return value for the passed in parameter name
    */
   public String getAttribute( String name )
   {
      return (String) attributes.get( name );
   }

   // Object overrides --------------------------------------------------

   public String toString()
   {
      return new StringBuffer( "[commandName=" ).append( commandName ).
         append( ",commandClass=" ).append( commandClass ).
         append( ",attributes=" ).append( attributes.toString() ).
         append( "]" ).toString();
   }
}
