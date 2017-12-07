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
package org.jboss.iiop.csiv2;

import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA.ORB;

import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;

import org.jboss.iiop.CorbaORBService;
import org.jboss.logging.Logger;
import org.jboss.metadata.IorSecurityConfigMetaData;

/**
 * Implements <code>org.omg.CORBA.Policy</code> objects containing
 * csiv2 ior security config info
 *
 * @author  Dimitris.Andreadis@jboss.org
 * @version $Revision: 81018 $
 */
public class CSIv2Policy 
   extends LocalObject
   implements Policy
{
   // Static  -----------------------------------------------------------------
   private static final Logger log = Logger.getLogger(CSIv2Policy.class);
   
   // TODO: contact request@omg.org to get a policy type
   public static final int TYPE = 0x87654321; 
   
   // Private -----------------------------------------------------------------
   private TaggedComponent sslTaggedComponent;
   private TaggedComponent secTaggedComponent;
   
   // Constructor -------------------------------------------------------------
   public CSIv2Policy(TaggedComponent sslTaggedComponent, 
                      TaggedComponent secTaggedComponent)
   {
      this.sslTaggedComponent = sslTaggedComponent;
      this.secTaggedComponent = secTaggedComponent;
   }
   
   public CSIv2Policy(IorSecurityConfigMetaData metadata, Codec codec)
   {
      log.debug(metadata);
      
      // convert the ior metadata to a cached security tagged component
      
      try {
         // get the singleton orb
         ORB orb = ORB.init();

         this.sslTaggedComponent =
            CSIv2Util.createSSLTaggedComponent(
               metadata,
               codec,
               CorbaORBService.getTheActualSSLPort(),
               orb);

         this.secTaggedComponent =
            CSIv2Util.createSecurityTaggedComponent(
               metadata,
               codec,
               CorbaORBService.getTheActualSSLPort(),
               orb);
      }
      catch (Exception e) {
         throw new RuntimeException("Unexpected exception " + e);
      }
   }

   /**
    * Return a copy of the cached SSL TaggedComponent
   **/
   public TaggedComponent getSSLTaggedComponent()
   {
      return CSIv2Util.createCopy(this.sslTaggedComponent);
   }
   
   /**
    * Return a copy of the cached CSI TaggedComponent
   **/
   public TaggedComponent getSecurityTaggedComponent()
   {
      return CSIv2Util.createCopy(this.secTaggedComponent);
   }
   
   // org.omg.CORBA.Policy operations -----------------------------------------
   /**
    * Returns a copy of the Policy object.
    */
   public Policy copy() 
   {
      return new CSIv2Policy(getSSLTaggedComponent(),
                             getSecurityTaggedComponent());
   }
   
   /**
    * Destroys the Policy object.
    */
   public void destroy() 
   {
      this.sslTaggedComponent = null;
      this.secTaggedComponent = null;
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
        return "CSIv2Policy[" + this.sslTaggedComponent + ", " 
                              + this.secTaggedComponent + "]";
    }
}
