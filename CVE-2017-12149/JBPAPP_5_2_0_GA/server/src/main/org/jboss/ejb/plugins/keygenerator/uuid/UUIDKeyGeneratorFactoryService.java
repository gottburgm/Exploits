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
package org.jboss.ejb.plugins.keygenerator.uuid;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.naming.Util;

import org.jboss.ejb.plugins.keygenerator.KeyGeneratorFactory;

/**
 * Implements UUID key generator factory service
 *
 * @jjmx:mbean name="jboss.system:service=KeyGeneratorFactory,type=UUID"
 *            extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:loubyansky@ukr.net">Alex Loubyansky</a>
 *
 * @version $Revision: 81030 $
 */
public class UUIDKeyGeneratorFactoryService
   extends ServiceMBeanSupport
   implements UUIDKeyGeneratorFactoryServiceMBean
{

   // Attributes ----------------------------------------------------

   /** uuid key generator factory implementation */
   KeyGeneratorFactory keyGeneratorFactory;

   // ServiceMBeanSupport overridding ------------------------------

   public void startService()
   {
      // create uuid key generator factory instance
      try
      {
         keyGeneratorFactory = new UUIDKeyGeneratorFactory();
      }
      catch( Exception e ) {
         log.error( "Caught exception during startService()", e );
         // Ingore
      }

      // bind the factory
      try
      {
         Context ctx = (Context) new InitialContext();
         Util.rebind( ctx, keyGeneratorFactory.getFactoryName(),
            keyGeneratorFactory );
      }
      catch( Exception e ) {
         log.error( "Caught exception during startService()", e );
         // Ingore
      }
   }

   public void stopService()
   {
      // unbind the factory
      try
      {
         Context ctx = (Context) new InitialContext();
         Util.unbind( ctx, keyGeneratorFactory.getFactoryName() );
      }
      catch( Exception e ) {
         log.error( "Caught exception during stopService()", e );
         // Ingore
      }
   }
}
