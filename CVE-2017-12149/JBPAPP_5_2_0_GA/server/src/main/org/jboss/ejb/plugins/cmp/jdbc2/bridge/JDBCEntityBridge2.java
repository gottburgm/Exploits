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
package org.jboss.ejb.plugins.cmp.jdbc2.bridge;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.bridge.EntityBridgeInvocationHandler;
import org.jboss.ejb.plugins.cmp.bridge.FieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCEntityPersistenceStore;
import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCAbstractEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCAbstractCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCCMPFieldMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationshipRoleMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCOptimisticLockingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc2.JDBCStoreManager2;
import org.jboss.ejb.plugins.cmp.jdbc2.PersistentContext;
import org.jboss.ejb.plugins.cmp.jdbc2.schema.EntityTable;
import org.jboss.logging.Logger;
import org.jboss.proxy.compiler.InvocationHandler;
import org.jboss.proxy.compiler.Proxies;

import javax.ejb.EJBException;
import javax.ejb.RemoveException;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81030 $</tt>
 */
public class JDBCEntityBridge2
   implements JDBCAbstractEntityBridge
{
   private final JDBCStoreManager2 manager;
   private final JDBCEntityMetaData metadata;
   private final EntityTable table;
   private final String tableName;
   private final String qualifiedTableName;
   private final Logger log;

   private JDBCCMPFieldBridge2[] pkFields;
   private JDBCCMPFieldBridge2[] cmpFields;
   private JDBCCMPFieldBridge2[] tableFields;
   private JDBCCMRFieldBridge2[] cmrFields;
   private JDBCCMPFieldBridge2 versionField;

   private int cmrCount;

   public JDBCEntityBridge2(JDBCStoreManager2 manager, JDBCEntityMetaData metadata) throws DeploymentException
   {
      this.manager = manager;
      this.metadata = metadata;
      log = Logger.getLogger(this.getClass().getName() + "." + metadata.getName());

      table = manager.getSchema().createEntityTable(metadata, this);
      tableName = SQLUtil.getTableNameWithoutSchema(metadata.getDefaultTableName());
      qualifiedTableName = SQLUtil.fixTableName(metadata.getDefaultTableName(), table.getDataSource());
   }

   // Public

   public void init() throws DeploymentException
   {
      loadCMPFields(metadata);
      loadCMRFields(metadata);

      JDBCOptimisticLockingMetaData olMD = metadata.getOptimisticLocking();
      if(olMD != null)
      {
         if(olMD.getLockingStrategy() != JDBCOptimisticLockingMetaData.VERSION_COLUMN_STRATEGY)
         {
            throw new DeploymentException(
               "Only version-column optimistic locking strategy is supported at the moment.");
         }

         JDBCCMPFieldMetaData versionMD = olMD.getLockingField();
         versionField = (JDBCCMPFieldBridge2) getFieldByName(versionMD.getFieldName());
      }
   }

   public JDBCCMPFieldBridge2 getVersionField()
   {
      return versionField;
   }

   public void resolveRelationships() throws DeploymentException
   {
      for(int i = 0; i < cmrFields.length; ++i)
      {
         cmrFields[i].resolveRelationship();
      }
   }

   public void start() throws DeploymentException
   {
      if(versionField != null)
      {
         versionField.initVersion();
      }

      table.start();

      if(cmrFields != null)
      {
         for(int i = 0; i < cmrFields.length; ++i)
         {
            cmrFields[i].initLoader();
         }
      }
   }

   public void stop() throws Exception
   {
      table.stop();
   }
   
   public JDBCEntityMetaData getMetaData()
   {
      return metadata;
   }

   public EntityTable getTable()
   {
      return table;
   }

   public JDBCFieldBridge[] getPrimaryKeyFields()
   {
      return pkFields;
   }

   public JDBCFieldBridge[] getTableFields()
   {
      return tableFields;
   }

   public JDBCAbstractCMRFieldBridge[] getCMRFields()
   {
      return cmrFields;
   }

   public JDBCEntityPersistenceStore getManager()
   {
      return manager;
   }

   public EntityContainer getContainer()
   {
      return manager.getContainer();
   }

   public Object extractPrimaryKeyFromInstance(EntityEnterpriseContext ctx)
   {
      try
      {
         Object pk = null;
         for(int i = 0; i < pkFields.length; ++i)
         {
            JDBCCMPFieldBridge2 pkField = pkFields[i];
            Object fieldValue = pkField.getValue(ctx);
            pk = pkField.setPrimaryKeyValue(pk, fieldValue);
         }
         return pk;
      }
      catch(EJBException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         throw new EJBException("Internal error extracting primary key from instance", e);
      }
   }

   public static void destroyPersistenceContext(EntityEnterpriseContext ctx)
   {
      // If we have an EJB 2.0 dynaymic proxy,
      // notify the handler of the assigned context.
      Object instance = ctx.getInstance();
      if(instance instanceof Proxies.ProxyTarget)
      {
         InvocationHandler handler = ((Proxies.ProxyTarget) instance).getInvocationHandler();
         if(handler instanceof EntityBridgeInvocationHandler)
         {
            ((EntityBridgeInvocationHandler) handler).setContext(null);
         }
      }
      ctx.setPersistenceContext(null);
   }

   public void initPersistenceContext(EntityEnterpriseContext ctx)
   {
      // If we have an EJB 2.0 dynaymic proxy,
      // notify the handler of the assigned context.
      Object instance = ctx.getInstance();
      if(instance instanceof Proxies.ProxyTarget)
      {
         InvocationHandler handler = ((Proxies.ProxyTarget) instance).getInvocationHandler();
         if(handler instanceof EntityBridgeInvocationHandler)
         {
            ((EntityBridgeInvocationHandler) handler).setContext(ctx);
         }
      }
   }

   public void initInstance(EntityEnterpriseContext ctx)
   {
      ctx.setPersistenceContext(new PersistentContext(this, table.getRow(ctx.getId())));
      for(int i = 0; i < tableFields.length; ++i)
      {
         tableFields[i].initInstance(ctx);
      }

      for(int i = 0; i < cmrFields.length; ++i)
      {
         cmrFields[i].initInstance(ctx);
      }
   }

   /**
    * hacky method needed at deployment time
    */
   public List getFields()
   {
      List fields = new ArrayList();
      for(int i = 0; i < pkFields.length; ++i)
      {
         fields.add(pkFields[i]);
      }

      for(int i = 0; i < cmpFields.length; ++i)
      {
         fields.add(cmpFields[i]);
      }

      for(int i = 0; i < cmrFields.length; ++i)
      {
         fields.add(cmrFields[i]);
      }

      return fields;
   }

   public boolean isStoreRequired(EntityEnterpriseContext instance)
   {
      PersistentContext pctx = (PersistentContext) instance.getPersistenceContext();
      return pctx.isDirty();
   }

   public boolean isModified(EntityEnterpriseContext instance)
   {
      PersistentContext pctx = (PersistentContext) instance.getPersistenceContext();
      boolean modified = pctx.isDirty();

      if(!modified && cmrFields != null)
      {
         for(int i = 0; i < cmrFields.length; ++i)
         {
            final JDBCCMRFieldBridge2.FieldState cmrState = pctx.getCMRState(i);
            if(cmrState != null && cmrState.isModified())
            {
               modified = true;
               break;
            }
         }
      }
      return modified;
   }
   
   public Class getPrimaryKeyClass()
   {
      return metadata.getPrimaryKeyClass();
   }

   public Class getHomeClass()
   {
      return metadata.getHomeClass();
   }

   public Class getLocalHomeClass()
   {
      return metadata.getLocalHomeClass();
   }

   public String getTableName()
   {
      return tableName;
   }

   public String getQualifiedTableName()
   {
      return qualifiedTableName;
   }

   public DataSource getDataSource()
   {
      return table.getDataSource();
   }

   public boolean[] getLoadGroupMask(String eagerLoadGroupName)
   {
      // todo
      throw new UnsupportedOperationException();
   }

   public int getNextCMRIndex()
   {
      return cmrCount++;
   }

   public void remove(EntityEnterpriseContext ctx) throws RemoveException
   {
      if(cmrFields != null)
      {
         for(int i = 0; i < cmrFields.length; ++i)
         {
            cmrFields[i].remove(ctx);
         }
      }
   }

   // EntityBridge implementation

   public String getEntityName()
   {
      return metadata.getName();
   }

   public String getAbstractSchemaName()
   {
      return metadata.getAbstractSchemaName();
   }

   public FieldBridge getFieldByName(String fieldName)
   {
      FieldBridge field;
      for(int i = 0; i < pkFields.length; ++i)
      {
         field = pkFields[i];
         if(field.getFieldName().equals(fieldName))
         {
            return field;
         }
      }

      for(int i = 0; i < cmpFields.length; ++i)
      {
         field = cmpFields[i];
         if(field.getFieldName().equals(fieldName))
         {
            return field;
         }
      }

      for(int i = 0; i < cmrFields.length; ++i)
      {
         field = cmrFields[i];
         if(field.getFieldName().equals(fieldName))
         {
            return field;
         }
      }

      throw new IllegalStateException("Field " + fieldName + " not found in entity " + getEntityName());
   }

   public Class getRemoteInterface()
   {
      return metadata.getRemoteClass();
   }

   public Class getLocalInterface()
   {
      return metadata.getLocalClass();
   }

   // Package

   JDBCCMPFieldBridge2 addTableField(JDBCCMPFieldMetaData metadata) throws DeploymentException
   {
      table.addField();
      if(tableFields == null)
      {
         tableFields = new JDBCCMPFieldBridge2[1];
      }
      else
      {
         JDBCCMPFieldBridge2[] tmp = tableFields;
         tableFields = new JDBCCMPFieldBridge2[tableFields.length + 1];
         System.arraycopy(tmp, 0, tableFields, 0, tmp.length);
      }
      int tableIndex = tableFields.length - 1;
      JDBCCMPFieldBridge2 cmpField = new JDBCCMPFieldBridge2(manager, this, metadata, tableIndex);
      tableFields[tableFields.length - 1] = cmpField;
      return cmpField;
   }

   // Private

   private void loadCMPFields(JDBCEntityMetaData metadata) throws DeploymentException
   {
      // only non pk fields are stored here at first and then later
      // the pk fields are added to the front (makes sql easier to read)
      List cmpFieldsList = new ArrayList(metadata.getCMPFields().size());
      // primary key cmp fields
      List pkFieldsList = new ArrayList(metadata.getCMPFields().size());

      // create each field
      Iterator iter = metadata.getCMPFields().iterator();
      while(iter.hasNext())
      {
         JDBCCMPFieldMetaData cmpFieldMetaData = (JDBCCMPFieldMetaData) iter.next();
         JDBCCMPFieldBridge2 cmpField = addTableField(cmpFieldMetaData);
         if(cmpFieldMetaData.isPrimaryKeyMember())
         {
            pkFieldsList.add(cmpField);
         }
         else
         {
            cmpFieldsList.add(cmpField);
         }
      }

      // save the pk fields in the pk field array
      pkFields = new JDBCCMPFieldBridge2[pkFieldsList.size()];
      for(int i = 0; i < pkFieldsList.size(); ++i)
      {
         pkFields[i] = (JDBCCMPFieldBridge2) pkFieldsList.get(i);
      }

      // add the pk fields to the front of the cmp list, per guarantee above
      cmpFields = new JDBCCMPFieldBridge2[metadata.getCMPFields().size() - pkFields.length];
      int cmpFieldIndex = 0;
      for(int i = 0; i < cmpFieldsList.size(); ++i)
      {
         cmpFields[cmpFieldIndex++] = (JDBCCMPFieldBridge2) cmpFieldsList.get(i);
      }
   }

   private void loadCMRFields(JDBCEntityMetaData metadata)
      throws DeploymentException
   {
      cmrFields = new JDBCCMRFieldBridge2[metadata.getRelationshipRoles().size()];
      // create each field
      int cmrFieldIndex = 0;
      for(Iterator iter = metadata.getRelationshipRoles().iterator(); iter.hasNext();)
      {
         JDBCRelationshipRoleMetaData relationshipRole = (JDBCRelationshipRoleMetaData) iter.next();
         JDBCCMRFieldBridge2 cmrField = new JDBCCMRFieldBridge2(this, manager, relationshipRole);
         cmrFields[cmrFieldIndex++] = cmrField;
      }
   }
}
