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
package org.jboss.ejb.plugins.cmp.jdbc2;

import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCCMRFieldBridge2;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.CMRInvocation;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.logging.Logger;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.LocalEJBInvocation;

import javax.ejb.EJBException;
import java.io.Serializable;
import java.io.ObjectStreamException;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81030 $</tt>
 */
public class RelationInterceptor
   extends AbstractInterceptor
{
   private Logger log;

   // AbstractInterceptor overrides

   public void setContainer(Container container)
   {
      this.container = (EntityContainer)container;
      if(container != null)
      {
         log = Logger.getLogger(
            this.getClass().getName() +
            "." +
            container.getBeanMetaData().getEjbName());
      }
   }

   // Interceptor implementation

   public Object invoke(Invocation mi) throws Exception
   {
      if(!(mi instanceof CMRInvocation))
      {
         return getNext().invoke(mi);
      }

      org.jboss.ejb.plugins.cmp.jdbc.bridge.CMRMessage msg = ((CMRInvocation)mi).getCmrMessage();

      // We are going to work with the context a lot
      EntityEnterpriseContext ctx = (EntityEnterpriseContext)mi.getEnterpriseContext();
      JDBCCMRFieldBridge2 cmrField = (JDBCCMRFieldBridge2)mi.getArguments()[0];

      if(org.jboss.ejb.plugins.cmp.jdbc.bridge.CMRMessage.ADD_RELATION == msg)
      {
         Object relatedId = mi.getArguments()[1];
         if(log.isTraceEnabled())
         {
            log.trace("Add relation: field=" + cmrField.getFieldName() +
               " id=" + ctx.getId() +
               " relatedId=" + relatedId);
         }

         cmrField.addRelatedId(ctx, relatedId);
      }
      else if(org.jboss.ejb.plugins.cmp.jdbc.bridge.CMRMessage.REMOVE_RELATION == msg)
      {
         // call removeRelation
         Object relatedId = mi.getArguments()[1];
         if(log.isTraceEnabled())
         {
            log.trace("Remove relation: field=" + cmrField.getFieldName() +
               " id=" + ctx.getId() +
               " relatedId=" + relatedId);
         }

         cmrField.removeRelatedId(ctx, relatedId);
      }
      else
      {
         // this should not be possible we are using a type safe enum
         throw new EJBException("Unknown cmp2.0-relationship-message=" + msg);
      }

      return null;
   }

   // Inner

   public static class RelationInvocation extends LocalEJBInvocation
   {
      public final CMRMessage msg;

      public RelationInvocation(CMRMessage msg)
      {
         this.msg = msg;
      }
   }

   public static final class CMRMessage implements Serializable
   {
      private static int nextOrdinal = 0;
      private static final CMRMessage[] VALUES = new CMRMessage[5];

      public static final CMRMessage ADD_RELATED_ID = new CMRMessage("ADD_RELATED_ID");
      public static final CMRMessage REMOVE_RELATED_ID = new CMRMessage("REMOVE_RELATED_ID");
      public static final CMRMessage DESTROY_EXISTING_RELATIONSHIPS = new CMRMessage("DESTROY_EXISTING_RELATIONSHIPS");

      private final transient String name;
      private final int ordinal;

      private CMRMessage(String name)
      {
         this.name = name;
         this.ordinal = nextOrdinal++;
         VALUES[ordinal] = this;
      }

      public String toString()
      {
         return name;
      }

      Object readResolve() throws ObjectStreamException
      {
         return VALUES[ordinal];
      }
   }
}
