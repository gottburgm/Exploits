/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.invocation.unified.marshall;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.marshal.serializable.SerializableMarshaller;
import org.jboss.remoting.marshal.Marshaller;
import org.jboss.remoting.marshal.http.HTTPMarshaller;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextUtil;

import javax.transaction.SystemException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This marshaller is to be used in conjunction with the UnifiedInvoker and will
 * look for an InvocationRequest to be passed to it, which is specific to EJB
 * invocations.
 *
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class HTTPInvocationMarshaller extends HTTPMarshaller
{
   /** @since 4.2.0 */
   static final long serialVersionUID = 1611946070051056241L;
   
   public final static String DATATYPE = "invocationhttp";

   private static final Logger log = Logger.getLogger(HTTPInvocationMarshaller.class);

   /**
    * Marshaller will need to take the dataObject and convert
    * into primitive java data types and write to the
    * given output.  Will check to see if dataObject being passed is
    * an InvocationRequest, and if is, process it (including handling propagation of
    * transaction).  If is not an instance of InvocationRequest, will default back to
    * SerializableMarshaller for processing.
    *
    * @param dataObject Object to be writen to output
    * @param output     The data output to write the object
    *                   data to.
    */
   public void write(Object dataObject, OutputStream output, int version) throws IOException
   {
      if(dataObject instanceof InvocationRequest)
      {
         InvocationRequest remoteInv = (InvocationRequest) dataObject;

         if(remoteInv.getParameter() instanceof Invocation)
         {
            Invocation inv = (Invocation) remoteInv.getParameter();

            MarshalledInvocation marshInv = new MarshalledInvocation(inv);

            if(inv != null)
            {
               // now that have invocation object related to ejb invocations,
               // need to get the possible known payload objects and make sure
               // they get serialized.

               try
               {
                  marshInv.setTransactionPropagationContext(getTransactionPropagationContext());
               }
               catch(SystemException e)
               {
                  log.error("Error setting transaction propagation context.", e);
                  throw new IOException("Error setting transaction context.  Message: " + e.getMessage());
               }

               // reset the invocation parameter within remote invocation
               remoteInv.setParameter(marshInv);
            }
            else
            {
               //Should never get here, but will check anyways
               log.error("Attempting to marshall Invocation but is null.  Can not proceed.");
               throw new IOException("Can not process data object due to the InvocationRequest's parameter being null.");
            }

         }

         super.write(dataObject, output, version);

      }
      else  // assume this is going to be the response
      {
         super.write(dataObject, output);
      }
   }

   public Object getTransactionPropagationContext()
         throws SystemException
   {
      TransactionPropagationContextFactory tpcFactory = TransactionPropagationContextUtil.getTPCFactoryClientSide();
      return (tpcFactory == null) ? null : tpcFactory.getTransactionPropagationContext();
   }

   public Marshaller cloneMarshaller() throws CloneNotSupportedException
   {
      return new HTTPInvocationMarshaller();
   }

}