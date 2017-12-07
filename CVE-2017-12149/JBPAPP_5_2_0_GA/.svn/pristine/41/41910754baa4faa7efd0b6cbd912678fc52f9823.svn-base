/**
 * 
 */
package org.jboss.web.tomcat.service.session.persistent;

import javax.sql.DataSource;

import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class DataSourcePersistentManager<O extends OutgoingDistributableSessionData> 
   extends AbstractPersistentManager<DataSourcePersistentStore, O>
{


   // --------------------------------------------------------- Instance Fields
   
   private String jndiName;

   // ------------------------------------------------------------- Constructors
   
   public DataSourcePersistentManager()
   {
      super(new DataSourcePersistentStore());
   }
   
   public DataSourcePersistentManager(DataSource datasource)
   {
      super(new DataSourcePersistentStore(datasource));
   }

   // ------------------------------------------------------------- Properties

   public String getDataSourceJndiName()
   {
      return jndiName;
   }

   public void setDataSourceJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   // ------------------------------------------------------------- Protected

   @Override
   protected void configureStore()
   {
      super.configureStore();
      DataSourcePersistentStore store = getPersistentStore();
      store.setDataSourceJndiName(jndiName);
   }

}
