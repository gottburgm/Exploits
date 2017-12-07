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
package org.jboss.naming;

import javax.naming.MalformedLinkException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

/**
 * A pair of addresses, one to be used in the local machine,
 * the other in remote machines. 
 *   
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision: 81030 $
 */
public class LinkRefPair extends Reference
{
   // Constants -----------------------------------------------------

   /** Serial version UID */
   private static final long serialVersionUID = 6036946190113161492L;

   /** Our class name */
   private static final String linkRefPairClassName = LinkRefPair.class.getName();

   /** The remote jndi object */
   static final String remoteAddress = "remoteAddress";

   /** The local jndi object */
   static final String localAddress = "localAddress";

   /** The guid used to determine whether we are local to the VM */
   private static final String guidAddress = "guid";

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Create a new link ref pair with the give remote and local names.
    * 
    * @param remote the remote name
    * @param local the local name
    */
   public LinkRefPair(String remote, String local)
   {
      super(linkRefPairClassName, LinkRefPairObjectFactory.className, null);
      add(new StringRefAddr(guidAddress, LinkRefPairObjectFactory.guid));
      add(new StringRefAddr(remoteAddress, remote));
      add(new StringRefAddr(localAddress, local));
   }

   // Public --------------------------------------------------------

   /**
    * Get the guid link name
    * 
    * @return the guid
    * @throws MalformedLinkException when the reference is malformed
    */
   public String getGUID() throws MalformedLinkException
   {
      if (className != null && className.equals(linkRefPairClassName))
      {
         RefAddr refAddr = get(guidAddress);
         if (refAddr != null && refAddr instanceof StringRefAddr)
         {
            Object content = refAddr.getContent();
            if (content != null && content instanceof String)
               return (String) content;
            else
               throw new MalformedLinkException("Content is not a string: " + content);
         }
         else
            throw new MalformedLinkException("RefAddr is not a string reference: " + refAddr);
      }
      else
         throw new MalformedLinkException("Class is not a LinkRefPair: " + className);
   }

   /**
    * Get the remote link name
    * 
    * @return the remote link
    * @throws MalformedLinkException when the reference is malformed
    */
   public String getRemoteLinkName() throws MalformedLinkException
   {
      if (className != null && className.equals(linkRefPairClassName))
      {
         RefAddr refAddr = get(remoteAddress);
         if (refAddr != null && refAddr instanceof StringRefAddr)
         {
            Object content = refAddr.getContent();
            if (content != null && content instanceof String)
               return (String) content;
            else
               throw new MalformedLinkException("Content is not a string: " + content);
         }
         else
            throw new MalformedLinkException("RefAddr is not a string reference: " + refAddr);
      }
      else
         throw new MalformedLinkException("Class is not a LinkRefPair: " + className);
   }

   /**
    * Get the local link name
    * 
    * @return the remote link
    * @throws MalformedLinkException when the reference is malformed
    */
   public String getLocalLinkName() throws MalformedLinkException
   {
      if (className != null && className.equals(linkRefPairClassName))
      {
         RefAddr refAddr = get(localAddress);
         if (refAddr != null && refAddr instanceof StringRefAddr)
         {
            Object content = refAddr.getContent();
            if (content != null && content instanceof String)
               return (String) content;
            else
               throw new MalformedLinkException("Content is not a string: " + content);
         }
         else
            throw new MalformedLinkException("RefAddr is not a string reference: " + refAddr);
      }
      else
         throw new MalformedLinkException("Class is not a LinkRefPair: " + className);
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
