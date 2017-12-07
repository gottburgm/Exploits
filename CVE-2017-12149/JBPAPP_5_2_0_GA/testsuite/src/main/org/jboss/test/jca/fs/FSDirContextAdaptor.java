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
package org.jboss.test.jca.fs;

import java.util.concurrent.ConcurrentHashMap;

import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.work.WorkManager;
import javax.resource.ResourceException;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/** The ResourceAdapter for the file system based DirContext
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class FSDirContextAdaptor implements ResourceAdapter
{
   private static final Logger log = Logger.getLogger(FSDirContextAdaptor.class);
   
   private BootstrapContext ctx;
   /** The activations by activation spec */
   private ConcurrentHashMap activations = new ConcurrentHashMap();

   /**
    * Get the work manager
    * 
    * @return the work manager
    */
   public WorkManager getWorkManager()
   {
      return ctx.getWorkManager();
   }

   // --- Begin ResourceAdapter interface methods
   public void start(BootstrapContext ctx)
      throws ResourceAdapterInternalException
   {
      log.debug("start");
      this.ctx = ctx;
      WorkManager mgr = ctx.getWorkManager();
   }

   public void stop()
   {
      log.debug("stop");
   }

   public void endpointActivation(MessageEndpointFactory endpointFactory,
      ActivationSpec spec)
      throws ResourceException
   {
      log.debug("endpointActivation, spec="+spec);
      FSActivationSpec fsSpec = (FSActivationSpec) spec;
      FSActivation activation = new FSActivation(this, endpointFactory,
         fsSpec);
      activations.put(spec, activation);
   }

   public void endpointDeactivation(MessageEndpointFactory endpointFactory,
      ActivationSpec spec)
   {
      log.debug("endpointDeactivation, spec="+spec);
      FSActivation activation = (FSActivation) activations.remove(spec);
   }

   public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException
   {
      return new XAResource[0];
   }
   // --- End ResourceAdapter interface methods
}
