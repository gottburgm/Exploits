/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.as.integration.hornetq.management.jms;

import org.jboss.logging.Logger;
import org.jboss.metatype.api.types.*;
import org.jboss.metatype.api.values.CollectionValueSupport;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.spi.values.MetaMapper;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 *         Created: 17-Mar-2010
 */
public class SubscriptionInfoMapper  extends MetaMapper<Object[]>
{
   private static final Logger log = Logger.getLogger(MessageListMapper.class);
   public static final CollectionMetaType TYPE;
   public static final CompositeMetaType MSG_TYPE;

   static
   {
      String[] itemNames = {
            "name",
            "clientID",
            "subName",
            "durable",
            "messageCount",
            "filter"
      };
      String[] itemDescriptions = {
            "name",
            "clientID",
            "subName",
            "durable",
            "messageCount",
            "filter"
      };
      MetaType[] itemTypes = {
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING
      };
      MSG_TYPE = new ImmutableCompositeMetaType("javax.jms.Message", "JMS Message",
            itemNames, itemDescriptions, itemTypes);
      TYPE = new CollectionMetaType("java.util.List", MSG_TYPE);
   }

   @Override
   public MetaValue createMetaValue(MetaType metaType, Object[] object)
   {
      ArrayList<MetaValue> tmp = new ArrayList<MetaValue>();
      if(object != null)
      {
         for(Object o : object)
         {
            Object[] m = (Object[]) o;
            try
            {
               CompositeValueSupport cvs = new CompositeValueSupport(MSG_TYPE);
               if(m[0] != null)
                  cvs.set("name", SimpleValueSupport.wrap((Serializable) m[0].toString()));
               if(m[1] != null)
                  cvs.set("clientID", SimpleValueSupport.wrap((Serializable) m[1].toString()));
               if(m[2] != null)
                  cvs.set("subName", SimpleValueSupport.wrap((Serializable) m[2].toString()));
               if(m[3] != null)
                  cvs.set("durable", SimpleValueSupport.wrap((Serializable) m[3].toString()));
               if(m[4] != null)
                  cvs.set("messageCount", SimpleValueSupport.wrap((Serializable) m[4].toString()));
               if(m[5] != null)
                  cvs.set("filter", SimpleValueSupport.wrap((Serializable) m[5].toString()));
               tmp.add(cvs);
            }
            catch(Exception e)
            {
               log.warn("Skipping msg: "+m, e);
            }
         }
      }
      MetaValue[] elements = new MetaValue[tmp.size()];
      tmp.toArray(elements);
      CollectionValueSupport msgs = new CollectionValueSupport(TYPE, elements);
      return msgs;
   }

   @Override
   public MetaType getMetaType()
   {
      return TYPE;
   }

   @Override
   public Type mapToType()
   {
      return Object[].class;
   }

   @Override
   public Object[] unwrapMetaValue(MetaValue metaValue)
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }
}
