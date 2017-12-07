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
package org.jboss.ejb.plugins;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.ejb.EJBException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;

import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.ejb.Container;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.StatefulSessionEnterpriseContext;
import org.jboss.ejb.StatefulSessionPersistenceManager;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.util.id.UID;

/**
 * A file-based stateful session bean persistence manager.
 * <p>
 * Reads and writes session bean objects to files by using the
 * standard Java serialization mechanism.
 * <p>
 * Passivated state files are stored under:
 * <tt><em>jboss-server-data-dir</em>/<em>storeDirectoryName</em>/<em>ejb-name</em>-<em>unique-id</em></tt>.
 * <p>
 * Since ejb-name is not unique across deployments we generate a <em>unique-id</em> to make
 * sure that beans with the same EJB name do not collide.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version <tt>$Revision: 81030 $</tt>
 */
public class StatefulSessionFilePersistenceManager extends ServiceMBeanSupport
   implements StatefulSessionPersistenceManager, StatefulSessionFilePersistenceManagerMBean
{
   /** The default store directory name ("<tt>sessions</tt>"). */
   public static final String DEFAULT_STORE_DIRECTORY_NAME = "sessions";

   /** Our container. */
   private StatefulSessionContainer con;

   /**
    * The sub-directory name under the server data directory where
    * session data is stored.
    *
    * @see #DEFAULT_STORE_DIRECTORY_NAME
    * @see #setStoreDirectoryName
    */
   private String storeDirName = DEFAULT_STORE_DIRECTORY_NAME;

   /** The base directory where sessions state files are stored for our container. */
   private File storeDir;

   /**
    * Enable purging leftover state files at create and destroy
    * time (default is true).
    */
   private boolean purgeEnabled = true;

   /**
    * Saves a reference to the {@link StatefulSessionContainer} for
    * its bean type.
    *
    * @throws ClassCastException  Container is not a StatefulSessionContainer.
    */
   public void setContainer(final Container con)
   {
      this.con = (StatefulSessionContainer) con;
   }

   //
   // jason: these properties are intended to be used when plugins/interceptors 
   //        can take configuration values (need to update xml schema and processors).
   //
   
   /**
    * Set the sub-directory name under the server data directory
    * where session data will be stored.
    *
    * <p>
    * This value will be appened to the value of
    * <tt><em>jboss-server-data-dir</em></tt>.
    *
    * <p>
    * This value is only used during creation and will not dynamically
    * change the store directory when set after the create step has finished.
    *
    * @jmx:managed-attribute
    *
    * @param dirName   A sub-directory name.
    */
   public void setStoreDirectoryName(final String dirName)
   {
      this.storeDirName = dirName;
   }

   /**
    * Get the sub-directory name under the server data directory
    * where session data is stored.
    *
    * jmx:managed-attribute
    *
    * @see #setStoreDirectoryName
    *
    * @return A sub-directory name.
    */
   public String getStoreDirectoryName()
   {
      return storeDirName;
   }

   /**
    * Set the stale session state purge enabled flag.
    *
    * jmx:managed-attribute
    *
    * @param flag   The toggle flag to enable or disable purging.
    */
   public void setPurgeEnabled(final boolean flag)
   {
      this.purgeEnabled = flag;
   }

   /**
    * Get the stale session state purge enabled flag.
    *
    * jmx:managed-attribute
    *
    * @return  True if purge is enabled.
    */
   public boolean getPurgeEnabled()
   {
      return purgeEnabled;
   }

   /**
    * Returns the directory used to store session passivation state files.
    *
    * jmx:managed-attribute
    * 
    * @return The directory used to store session passivation state files.
    */
   public File getStoreDirectory()
   {
      return storeDir;
   }

   /**
    * Setup the session data storage directory.
    *
    * <p>Purges any existing session data found.
    */
   protected void createService() throws Exception
   {
      // Initialize the dataStore
      String ejbName = con.getBeanMetaData().getEjbName();

      // Get the system data directory
      File dir = ServerConfigLocator.locate().getServerTempDir();

      // Setup the reference to the session data store directory
      dir = new File(dir, storeDirName);
      // ejbName is not unique across all deployments, so use a unique token
      dir = new File(dir, ejbName + "-" + new UID().toString());
      storeDir = dir;

      log.debug("Storing sessions for '" + ejbName + "' in: " + storeDir);

      // if the directory does not exist then try to create it
      if( !storeDir.exists() )
      {
         if( MkdirsFileAction.mkdirs(storeDir) == false )
         {
            throw new IOException("Failed to create directory: " + storeDir);
         }
      }
      
      // make sure we have a directory
      if( !storeDir.isDirectory() )
      {
         throw new IOException("File exists where directory expected: " + storeDir);
      }

      // make sure we can read and write to it
      if( !storeDir.canWrite() || !storeDir.canRead() )
      {
         throw new IOException("Directory must be readable and writable: " + storeDir);
      }
      
      // Purge state session state files, should be none, due to unique directory
      purgeAllSessionData();
   }

   /**
    * Removes any state files left in the storgage directory.
    */
   private void purgeAllSessionData()
   {
      if( !purgeEnabled ) return;

      log.debug("Purging all session data in: " + storeDir);

      File[] sessions = storeDir.listFiles();
      for(int i = 0; i < sessions.length; i++)
      {
         if( !sessions[i].delete() )
         {
            log.warn("Failed to delete session state file: " + sessions[i]);
         }
         else
         {
            log.debug("Removed stale session state: " + sessions[i]);
         }
      }
   }

   /**
    * Purge any data in the store, and then the store directory too.
    */
   protected void destroyService() throws Exception
   {
      // Purge data and attempt to delete directory
      purgeAllSessionData();

      // Nuke the directory too if purge is enabled
      if( purgeEnabled && !storeDir.delete() )
      {
         log.warn("Failed to delete session state storage directory: " + storeDir);
      }
   }

   /**
    * Make a session state file for the given instance id.
    */
   private File getFile(final Object id)
   {
      //
      // jason: may have to translate id into a os-safe string, though
      //        the format of UID is safe on Unix and win32 already...
      //
      
      return new File(storeDir, String.valueOf(id) + ".ser");
   }

   /**
    * @return  A {@link UID}.
    */
   public Object createId(StatefulSessionEnterpriseContext ctx)
      throws Exception
   {
      return new UID();
   }

   /**
    * Non-operation.
    */
   public void createdSession(StatefulSessionEnterpriseContext ctx)
      throws Exception
   {
      // nothing
   }

   /**
    * Restores session state from the serialized file & invokes
    * {@link SessionBean#ejbActivate} on the target bean.
    */
   public void activateSession(final StatefulSessionEnterpriseContext ctx)
      throws RemoteException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
      {
         log.trace("Attempting to activate; ctx=" + ctx);
      }

      Object id = ctx.getId();
      
      // Load state
      File file = getFile(id);
      if (trace)
      {
         log.trace("Reading session state from: " + file);
      }

      try
      {
         FileInputStream fis = FISAction.open(file);
         SessionObjectInputStream in = new SessionObjectInputStream(ctx,
            new BufferedInputStream(fis));

         try
         {
            Object obj = in.readObject();
            if (trace)
            {
               log.trace("Session state: " + obj);
            }
            ctx.setInstance(obj);
         }
         finally
         {
            in.close();
         }
      }
      catch(Exception e)
      {
         throw new EJBException("Could not activate; failed to " +
            "restore state", e);
      }

      removePassivated(id);
            
      try
      {
         // Instruct the bean to perform activation logic         
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_ACTIVATE);
         SessionBean bean = (SessionBean) ctx.getInstance();         
         bean.ejbActivate();
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }

      if (trace)
      {
         log.trace("Activation complete; ctx=" + ctx);
      }
   }

   /**
    * Invokes {@link SessionBean#ejbPassivate} on the target bean and saves the
    * state of the session to a file.
    */
   public void passivateSession(final StatefulSessionEnterpriseContext ctx)
      throws RemoteException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
      {
         log.trace("Attempting to passivate; ctx=" + ctx);
      }

      try
      {
         // Instruct the bean to perform passivation logic    
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_PASSIVATE);
         SessionBean bean = (SessionBean) ctx.getInstance();
         bean.ejbPassivate();
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
      
      // Store state
      
      File file = getFile(ctx.getId());
      if (trace)
      {
         log.trace("Saving session state to: " + file);
      }

      try
      {
         FileOutputStream fos = FOSAction.open(file);
         SessionObjectOutputStream out = new SessionObjectOutputStream(
            new BufferedOutputStream(fos));

         Object obj = ctx.getInstance();
         if (trace)
         {
            log.trace("Writing session state: " + obj);
         }

         try
         {
            out.writeObject(obj);
         }
         finally
         {
            out.close();
         }
      }
      catch(Exception e)
      {
         throw new EJBException("Could not passivate; failed to save state", e);
      }

      if (trace)
      {
         log.trace("Passivation complete; ctx=" + ctx);
      }
   }

   /**
    * Invokes {@link SessionBean#ejbRemove} on the target bean.
    */
   public void removeSession(final StatefulSessionEnterpriseContext ctx)
      throws RemoteException, RemoveException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
      {
         log.trace("Attempting to remove; ctx=" + ctx);
      }
      
      // Instruct the bean to perform removal logic
      SessionBean bean = (SessionBean) ctx.getInstance();
      bean.ejbRemove();

      if (trace)
      {
         log.trace("Removal complete; ctx=" + ctx);
      }
   }

   /**
    * Removes the saved state file (if any) for the given session id.
    */
   public void removePassivated(final Object id)
   {
      boolean trace = log.isTraceEnabled();

      File file = getFile(id);

      // only attempt to delete if the file exists
      if( file.exists() )
      {
         if (trace)
         {
            log.trace("Removing passivated state file: " + file);
         }

         if( DeleteFileAction.delete(file) == false )
         {
            log.warn("Failed to delete passivated state file: " + file);
         }
      }
   }

   static class DeleteFileAction implements PrivilegedAction
   {
      File file;
      DeleteFileAction(File file)
      {
         this.file = file;
      }
      public Object run()
      {
         boolean deleted = file.delete();
         return new Boolean(deleted);
      }
      static boolean delete(File file)
      {
         DeleteFileAction action = new DeleteFileAction(file);
         Boolean deleted = (Boolean) AccessController.doPrivileged(action);
         return deleted.booleanValue();
      }
   }
   static class MkdirsFileAction implements PrivilegedAction
   {
      File file;
      MkdirsFileAction(File file)
      {
         this.file = file;
      }
      public Object run()
      {
         boolean ok = file.mkdirs();
         return new Boolean(ok);
      }
      static boolean mkdirs(File file)
      {
         MkdirsFileAction action = new MkdirsFileAction(file);
         Boolean ok = (Boolean) AccessController.doPrivileged(action);
         return ok.booleanValue();
      }
   }

   static class FISAction implements PrivilegedExceptionAction
   {
      File file;
      FISAction(File file)
      {
         this.file = file;
      }
      public Object run() throws Exception
      {
         FileInputStream fis = new FileInputStream(file);
         return fis;
      }
      static FileInputStream open(File file) throws FileNotFoundException
      {
         FISAction action = new FISAction(file);
         FileInputStream fis = null;
         try
         {
            fis = (FileInputStream) AccessController.doPrivileged(action);
         }
         catch(PrivilegedActionException e)
         {
            throw (FileNotFoundException) e.getException();
         }

         return fis;
      }
   }

   static class FOSAction implements PrivilegedExceptionAction
   {
      File file;
      FOSAction(File file)
      {
         this.file = file;
      }
      public Object run() throws Exception
      {
         FileOutputStream fis = new FileOutputStream(file);
         return fis;
      }
      static FileOutputStream open(File file) throws FileNotFoundException
      {
         FOSAction action = new FOSAction(file);
         FileOutputStream fos = null;
         try
         {
            fos = (FileOutputStream) AccessController.doPrivileged(action);
         }
         catch(PrivilegedActionException e)
         {
            throw (FileNotFoundException) e.getException();
         }

         return fos;
      }
   }
}
