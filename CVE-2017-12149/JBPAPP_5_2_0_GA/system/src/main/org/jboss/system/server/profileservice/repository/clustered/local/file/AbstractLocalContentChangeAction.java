package org.jboss.system.server.profileservice.repository.clustered.local.file;

import java.io.File;
import java.io.IOException;

import org.jboss.logging.Logger;
import org.jboss.system.server.profileservice.repository.clustered.sync.AbstractContentMetadataMutatorAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification;

public abstract class AbstractLocalContentChangeAction
   extends AbstractContentMetadataMutatorAction<FileBasedSynchronizationActionContext>
{
   private final File targetFile;
   private final boolean targetWasDir;
   private final boolean targetExists;
   private final long targetTimestamp;
   private File tempRollback;

   /**
    * Create a new AbstractLocalContentChangeAction.
    * 
    * @param targetFile the file whose content is to be changed
    * @param context the overall context of the modification
    * @param modification the modification
    */
   protected AbstractLocalContentChangeAction(File targetFile, FileBasedSynchronizationActionContext context, 
         ContentModification modification)
   {
      super(context, modification);
      
      if (targetFile == null)
      {
         throw new IllegalArgumentException("Null targetFile");
      }
      
      this.targetFile = targetFile;
      this.targetWasDir = targetFile.isDirectory();
      this.targetExists = targetFile.exists();
      this.targetTimestamp = targetFile.lastModified();
   }
   
   protected abstract Logger getLogger();

   protected abstract boolean modifyTarget() throws IOException;

   protected File getTargetFile()
   {
      return targetFile;
   }
   
   @Override
   protected void doCancel()
   {
      safeCleanup();
   }
   
   @Override
   protected void doComplete() throws Exception
   {
      if (getLogger().isTraceEnabled())
      {
         ContentModification mod = getRepositoryContentModification();
         getLogger().trace("doComplete(): " + mod.getType() + " for " + mod.getItem().getRelativePath());
      }
   }

   @Override
   protected boolean doPrepare()
   {
      File backup = null;
      try
      {
         if (targetExists)
         {
            if (!targetWasDir)
            {
               // Make a backup copy of target in case of rollback
               backup = createTempFile();
               FileUtil.localMove(targetFile, backup, targetTimestamp);
               // assign after creation so the ref to the file
               // indicates a successful write -- useful in rollback
               this.tempRollback = backup;
            }
            else
            {
               // No backup copy needed; we can just recreate
               targetFile.delete();
            }
         }
         
         boolean result = modifyTarget();
         if (getLogger().isTraceEnabled())
         {
            ContentModification mod = getRepositoryContentModification();
            getLogger().trace("doPrepare(): modifyTarget result for " + 
                  mod.getType() + " for " + mod.getItem().getRelativePath() + 
                  " is " + result);
         }
         return result;
      }
      catch (Exception e)
      {
         getLogger().error("Caught exception in doPrepare() ", e);
         if (backup != null && tempRollback == null)
         {
            // We failed during backup creation.
            // Discard unneeded backup copy
            backup.delete();
         }
      }
      return false;
   }

   @Override
   protected void doRollbackFromCancelled()
   {
      // no-op
   }

   @Override
   protected void doRollbackFromComplete()
   {
      safeCleanup();
   }

   @Override
   protected void doRollbackFromOpen()
   {
      safeCleanup();
   }

   @Override
   protected void doRollbackFromPrepared()
   {
      boolean cleanRollback = true;
      
      rollbackContentMetadata();
      
      if (targetWasDir)
      {
         targetFile.delete();
         targetFile.mkdirs();
         targetFile.setLastModified(targetTimestamp);
      }
      else if (targetExists)
      {
         // Restore it
         try
         {
            FileUtil.localMove(tempRollback, targetFile, this.targetTimestamp);
         }
         catch (IOException e)
         {
            getLogger().error("Failed restoring " + targetFile + " during rollback. " +
                    "Backup copy is stored in " + tempRollback, e);
            // Don't discard the rollback file
            cleanRollback = false;
         }
      }
      else
      {
         targetFile.delete();
      }  
      
      safeCleanup(cleanRollback);
   }

   @Override
   protected void doRollbackFromRollbackOnly()
   {
      boolean cleanRollback = true;
      // We get here either from complete or from prepare. We
      // can tell which by whether tempRollback exists
      if (tempRollback != null && tempRollback.exists())
      {
         // Restore it
         try
         {
            FileUtil.localMove(tempRollback, targetFile, targetTimestamp);
         }
         catch (IOException e)
         {
            getLogger().error("Failed restoring " + targetFile + " during rollback. " +
            		"Backup copy is stored in " + tempRollback, e);
            // Don't discard the rollback file
            cleanRollback = false;
         }
      }
      else if (targetWasDir)
      {
         if (targetFile.exists())
         {
            if (targetFile.isDirectory() == false)
            {
               targetFile.delete();
               targetFile.mkdirs();
               targetFile.setLastModified(targetTimestamp);
            }
         }
         else
         {
            targetFile.mkdirs();
            targetFile.setLastModified(targetTimestamp);
         }
      }
      else if (targetExists == false)
      {
         // if we created one, get rid of it
         targetFile.delete();
      } 
      
      safeCleanup(cleanRollback);
   }

   @Override
   protected void doCommit()
   {
      updateContentMetadata();
      safeCleanup();
   }

   protected File createTempFile() throws IOException
   {
      FileBasedSynchronizationActionContext ctx = getContext();
      File f = FileUtil.createTempFile(ctx.getTempDir(), ctx.getStoreName());
      f.deleteOnExit();
      return f;
   }

   protected synchronized void safeCleanup(boolean cleanRollback)
   {
      if (cleanRollback && tempRollback != null)
      {
         tempRollback.delete();
      }      
   }

   protected void safeCleanup()
   {
      safeCleanup(true);
   }

}