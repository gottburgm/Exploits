/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Inc., and individual contributors as indicated
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

import org.hornetq.api.core.management.AddressSettingsInfo;
import org.hornetq.api.core.management.RoleInfo;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.TopicConfiguration;
import org.jboss.metatype.api.types.*;
import org.jboss.metatype.api.values.CollectionValueSupport;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.spi.values.MetaMapper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 * Created 15-Mar-2010
 */
public class AddressSettingsMapper extends MetaMapper<Object[]>
{
   public static final CompositeMetaType ADDRESS_SETTINGS_TYPE;
   public static final CompositeMetaType SECURITY_TYPE;
   public static final CollectionMetaType TYPE;

   static
   {
      String[] securityNames = {
         "name",
            "send",
            "consume",
            "createDurableQueue",
            "deleteDurableQueue",
            "createNonDurableQueue",
            "deleteNonDurableQueue",
            "manage"
      };
      String[] securityDescription = {
         "name",
            "send",
            "consume" ,
            "createDurableQueue",
            "deleteDurableQueue",
            "createNonDurableQueue",
            "deleteNonDurableQueue",
            "manage"
      };
      MetaType[] securityTypes = {
      SimpleMetaType.STRING,
      SimpleMetaType.BOOLEAN_PRIMITIVE,
      SimpleMetaType.BOOLEAN_PRIMITIVE,
      SimpleMetaType.BOOLEAN_PRIMITIVE,
      SimpleMetaType.BOOLEAN_PRIMITIVE,
      SimpleMetaType.BOOLEAN_PRIMITIVE,
      SimpleMetaType.BOOLEAN_PRIMITIVE,
      SimpleMetaType.BOOLEAN_PRIMITIVE
      };
      SECURITY_TYPE = new ImmutableCompositeMetaType("org.hornetq.api.core.management.RoleInfo", "Roles", securityNames, securityDescription, securityTypes);

      TYPE = new CollectionMetaType("java.util.List", SECURITY_TYPE);
      String[] itemNames = {
          "name",
          "jndiBindings",
          "dla",
          "expiryAddress",
          "maxSize",
          "pageSize",
          "pageMaxCacheSize",
          "maxDeliveryAttempts",
          "redeliveryDelay",
          "lastValueQueue",
          "redistributionDelay",
          "sendToDLAOnNoRoute",
          "addressFullMessagePolicy",
          "roles"
      };
      String[] itemDescriptions = {
          "name",
          "jndiBindings",
          "dla",
          "expiryAddress",
          "maxSize",
          "pageSize",
          "pageMaxCacheSize",
          "maxDeliveryAttempts",
          "redeliveryDelay",
          "lastValueQueue",
          "redistributionDelay",
          "sendToDLAOnNoRoute",
          "addressFullMessagePolicy",
          "roles"
      };
      MetaType[] itemTypes = {
          SimpleMetaType.STRING,
          SimpleMetaType.STRING,
          SimpleMetaType.STRING,
          SimpleMetaType.STRING,
          SimpleMetaType.INTEGER_PRIMITIVE,
          SimpleMetaType.INTEGER_PRIMITIVE,
          SimpleMetaType.INTEGER_PRIMITIVE,
          SimpleMetaType.INTEGER_PRIMITIVE,
          SimpleMetaType.LONG_PRIMITIVE,
          SimpleMetaType.BOOLEAN_PRIMITIVE,
          SimpleMetaType.LONG_PRIMITIVE,
          SimpleMetaType.BOOLEAN_PRIMITIVE,
          SimpleMetaType.STRING,
          TYPE
      };
      ADDRESS_SETTINGS_TYPE = new ImmutableCompositeMetaType("org.hornetq.api.core.management.AddressSettingsInfo", "Address Settings",
          itemNames, itemDescriptions, itemTypes);
   }

   @Override
   public MetaValue createMetaValue(MetaType metaType, Object[] val)
   {
      CompositeValueSupport cvs = new CompositeValueSupport(ADDRESS_SETTINGS_TYPE);
      if (val[0] instanceof JMSQueueConfiguration)
      {
         JMSQueueConfiguration queueConfiguration = (JMSQueueConfiguration) val[0];
         cvs.set("name", new SimpleValueSupport(SimpleMetaType.STRING, queueConfiguration.getName()));
         cvs.set("jndiBindings", new SimpleValueSupport(SimpleMetaType.STRING, getJndiString(queueConfiguration.getBindings())));
      }
      else
      {
         TopicConfiguration topicConfiguration = (TopicConfiguration) val[0];
         cvs.set("name", new SimpleValueSupport(SimpleMetaType.STRING, topicConfiguration.getName()));
         cvs.set("jndiBindings", new SimpleValueSupport(SimpleMetaType.STRING, getJndiString(topicConfiguration.getBindings())));
      }
      AddressSettingsInfo addressSettings = (AddressSettingsInfo) val[1];
      cvs.set("dla", new SimpleValueSupport(SimpleMetaType.STRING, addressSettings.getDeadLetterAddress()));
      cvs.set("expiryAddress", new SimpleValueSupport(SimpleMetaType.STRING, addressSettings.getExpiryAddress()));
      cvs.set("maxSize", new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, addressSettings.getMaxSizeBytes()));
      cvs.set("pageSize", new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, addressSettings.getPageSizeBytes()));
      cvs.set("pageMaxCacheSize", new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, addressSettings.getPageCacheMaxSize()));
      cvs.set("maxDeliveryAttempts", new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, addressSettings.getMaxDeliveryAttempts()));
      cvs.set("redeliveryDelay", new SimpleValueSupport(SimpleMetaType.LONG_PRIMITIVE, addressSettings.getRedeliveryDelay()));
      cvs.set("lastValueQueue", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, addressSettings.isLastValueQueue()));
      cvs.set("redistributionDelay", new SimpleValueSupport(SimpleMetaType.LONG_PRIMITIVE, addressSettings.getRedistributionDelay()));
      cvs.set("sendToDLAOnNoRoute", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, addressSettings.isSendToDLAOnNoRoute()));
      cvs.set("addressFullMessagePolicy", new SimpleValueSupport(SimpleMetaType.STRING, addressSettings.getAddressFullMessagePolicy()));
      if(val.length == 3)
      {
         ArrayList<MetaValue> tmp = new ArrayList<MetaValue>();
         RoleInfo[] roles = (RoleInfo[]) val[2];
         for (RoleInfo role : roles)
         {
            CompositeValueSupport cvs2 = new CompositeValueSupport(SECURITY_TYPE);
            cvs2.set("name", new SimpleValueSupport(SimpleMetaType.STRING, role.getName()));
            cvs2.set("send", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, role.isSend()));
            cvs2.set("consume", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, role.isConsume()));
            cvs2.set("createDurableQueue", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, role.isCreateDurableQueue()));
            cvs2.set("deleteDurableQueue", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, role.isDeleteDurableQueue()));
            cvs2.set("createNonDurableQueue", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, role.isCreateNonDurableQueue()));
            cvs2.set("deleteNonDurableQueue", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, role.isDeleteNonDurableQueue()));
            cvs2.set("manage", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, role.isManage()));
            tmp.add(cvs2);
         }
         MetaValue[] elements = new MetaValue[tmp.size()];
         tmp.toArray(elements);
         CollectionValueSupport sec = new CollectionValueSupport(TYPE, elements);
         cvs.set("roles", sec);
      }
      return cvs;
   }

   @Override
   public Object[] unwrapMetaValue(MetaValue metaValue)
   {
      return null;
   }

   @Override
   public Type mapToType()
   {
      return Object[].class;
   }

   @Override
   public MetaType getMetaType()
   {
      return ADDRESS_SETTINGS_TYPE;
   }

   private String getJndiString(String[] array)
   {
      StringBuffer sb = new StringBuffer();
      for (int i = 0, arrayLength = array.length; i < arrayLength; i++)
      {
         if (i > 0)
         {
            sb.append(",");
         }
         sb.append(array[i]);
      }
      return sb.toString();
   }
}
