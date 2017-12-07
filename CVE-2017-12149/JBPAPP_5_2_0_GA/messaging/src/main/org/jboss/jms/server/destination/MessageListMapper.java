/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jms.server.destination;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Message;

import org.jboss.logging.Logger;
import org.jboss.metatype.api.types.CollectionMetaType;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.ImmutableCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CollectionValueSupport;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.spi.values.MetaMapper;

/**
 * Maps a List<JBossMessage> into a CollectMetaType of CompositeValues.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 90346 $
 */
public class MessageListMapper extends MetaMapper<List<Message>>
{
   private static final Logger log = Logger.getLogger(MessageListMapper.class);
   public static final CollectionMetaType TYPE;
   public static final CompositeMetaType MSG_TYPE;

   static
   {
      String[] itemNames = {
            "JMSMessageID",
            "JMSTimestamp",
            "JMSCorrelationID"
      };
      String[] itemDescriptions = {
            "JMSMessageID",
            "JMSTimestamp",
            "JMSCorrelationID"
      };
      MetaType[] itemTypes = {
            SimpleMetaType.STRING,
            SimpleMetaType.LONG_PRIMITIVE,
            SimpleMetaType.STRING
      };
      MSG_TYPE = new ImmutableCompositeMetaType("javax.jms.Message", "JMS Message",
            itemNames, itemDescriptions, itemTypes);
      TYPE = new CollectionMetaType("java.util.List", MSG_TYPE);
   }

   @Override
   public MetaValue createMetaValue(MetaType metaType, List<Message> object)
   {
      ArrayList<MetaValue> tmp = new ArrayList<MetaValue>();
      if(object != null)
      {
         for(Message m : object)
         {
            try
            {
               CompositeValueSupport cvs = new CompositeValueSupport(MSG_TYPE);
               cvs.set("JMSCorrelationID", SimpleValueSupport.wrap(m.getJMSCorrelationID()));
               cvs.set("JMSTimestamp", SimpleValueSupport.wrap(m.getJMSTimestamp()));
               cvs.set("JMSMessageID", SimpleValueSupport.wrap(m.getJMSMessageID()));
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
      return List.class;
   }

   /**
    * This always returns null as Messages cannot be created from a meta value
    */
   @Override
   public List<Message> unwrapMetaValue(MetaValue metaValue)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
