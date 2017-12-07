/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.system.server.profileservice.repository.clustered.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;

import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryRootMetadata;


/**
 * Abstract superclass of classes that generate a list of 
 * {@link ContentModification} from a pair
 * of {@link RepositoryContentMetadata}.
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public abstract class AbstractContentModificationGenerator
{
   // -----------------------------------------------------------------  Public
   
   public List<ContentModification> getModificationList(RepositoryContentMetadata base,
                                                                  RepositoryContentMetadata modified)
         throws InconsistentRepositoryStructureException
   {
      if (base == null)
      {
         throw new IllegalArgumentException("Null base");
      }
      if (modified == null)
      {
         throw new IllegalArgumentException("Null modified");
      }
      
      Collection<RepositoryRootMetadata> baseRoots = base.getRepositories();
      Collection<RepositoryRootMetadata> modifiedRoots = modified.getRepositories();
      
      // We validate consistent repository structure by 1) confirming
      // same number of roots and 2) (below) confirming all base roots are in modified
      if (baseRoots.size() != modifiedRoots.size())
      {
         throw new InconsistentRepositoryStructureException(base, modified);
      }
      
      List<ContentModification> mods = new ArrayList<ContentModification>();
      for (RepositoryRootMetadata root : base.getRepositories())
      {
         RepositoryRootMetadata newRoot = modified.getRepositoryRootMetadata(root.getName());
         if (newRoot != null)
         {
            mods.addAll(getModificationList(root, newRoot));
         }
         else
         {
            throw new InconsistentRepositoryStructureException(base, modified);
         }
      }
      
      return mods;
   }

   // --------------------------------------------------------------  Protected

   protected abstract void handleAddition(String rootName, 
         RepositoryItemMetadata item, GeneratedModifications mods);

   protected abstract void handleMissing(String rootName, 
         RepositoryItemMetadata item, GeneratedModifications mods);



   protected void handleMatch(String rootName, 
         RepositoryItemMetadata base, 
         RepositoryItemMetadata modified, 
         GeneratedModifications mods)
   {
      // Most common case of all is no change
      if (base.equals(modified) == false)
      {         
         // There was a change; how we handle depends on what changed
         
         if (base.isRemoved() && !modified.isRemoved())
         {
            // Reincarnation
            handleAddition(rootName, modified, base, mods);
         }
         else if (modified.isRemoved() && !base.isRemoved())
         {
            // Item was removed on the remote node
            handleRemoval(rootName, base, mods);
         }
         else if (base.isDirectory() && !modified.isDirectory())
         {
            // An exploded archive replaced by a zipped one
            handleChangeFromDirectory(rootName, base, modified, mods);
         }
         else if (modified.isDirectory() && !base.isDirectory())
         {
            // A zipped archive replaced by an exploded one
            handleChangeToDirectory(rootName, base, modified, mods);
         }
         else if (modified.isDirectory())
         {
            // Directory timestamp modification
            handleDirectoryTimestampModification(rootName, base, modified, mods);
         }
         else
         {
            // Simple file change
            handleSimpleModification(rootName, base, modified, mods);
         }
      }
   }

   protected abstract void handleSimpleModification(String rootName, RepositoryItemMetadata base, RepositoryItemMetadata modified,
         GeneratedModifications mods);

   protected abstract void handleDirectoryTimestampModification(String rootName, RepositoryItemMetadata base,
         RepositoryItemMetadata modified, GeneratedModifications mods);

   protected abstract void handleChangeToDirectory(String rootName, RepositoryItemMetadata base, RepositoryItemMetadata modified,
         GeneratedModifications mods);

   protected abstract void handleChangeFromDirectory(String rootName, RepositoryItemMetadata base,
         RepositoryItemMetadata modified, GeneratedModifications mods);

   protected abstract void handleRemoval(String rootName, RepositoryItemMetadata base, GeneratedModifications mods);

   protected abstract void handleAddition(String rootName, RepositoryItemMetadata modified, RepositoryItemMetadata base,
         GeneratedModifications mods);

   protected void drainPreapprovedRemovals(GeneratedModifications mods)
   {
      ContentModification preapprovedRemoval;
      while ((preapprovedRemoval = mods.popPreapprovedRemoveParent()) != null)
      {
         mods.addModification(preapprovedRemoval);
      }
   }
   
   protected static RepositoryItemMetadata getMarkedRemovedItem(RepositoryItemMetadata base)
   {
      RepositoryItemMetadata result = base;
      if (result.isRemoved() == false)
      {
         result = new RepositoryItemMetadata(result);
         result.setRemoved(true);
      }
      
      return result;
   }

   // ----------------------------------------------------------------  Private
   
   private List<ContentModification> getModificationList(RepositoryRootMetadata base,
                                                                   RepositoryRootMetadata modified)
   {
      List<ContentModification> mods = new ArrayList<ContentModification>();
      
      OwnedItem[] items = getOwnedItems(base, modified);

      // Track directories that we have added or removed so we can flag
      // their children for automatic modification approval/rejection. We 
      // don't want to add/remove a directory and then later do something
      // inconsistent with a child. For stuff involving a removal we use
      // a Stack<RepositoryContentModification> so we can add the root removal
      // to the overall list of mods *after* all the children. This is needed
      // to allow the removal to be rolled back.
      RepositoryItemMetadata preapprovedAddParent = null;
      Stack<ContentModification> preapprovedRemoveParent = new Stack<ContentModification>();
      Stack<ContentModification> prerejectedAddParent = new Stack<ContentModification>();
      RepositoryItemMetadata prerejectedRemoveParent = null;
      
      for(int first = 0; first < items.length; first++)
      {
         GeneratedModifications pairmod = new GeneratedModifications(preapprovedAddParent, preapprovedRemoveParent, prerejectedAddParent, prerejectedRemoveParent);
         
         int next = first + 1;
         if (next >= items.length)
         {
            // Last unmatched item
            if (items[first].base)
            {
               // base w/o match == missing
               handleMissing(base.getName(), items[first].item, pairmod);
            }
            else
            {
               // !base w/o match == addition
               handleAddition(base.getName(), items[first].item, pairmod);
            }
         }
         else if (items[first].itemPath.equals(items[next].itemPath))
         {
            handleMatch(base.getName(), items[first].item, items[next].item, pairmod);            
            first++; // we just consumed "next"
         }
         else if (items[first].base)
         {
            // base w/o match == removal
            handleMissing(base.getName(), items[first].item, pairmod);
         }
         else
         {
            // !base w/o match == addition
            handleAddition(base.getName(), items[first].item, pairmod);
         }
         
         mods.addAll(pairmod.getModifications());            
         preapprovedAddParent = pairmod.getPreapprovedAddParent();
         prerejectedRemoveParent = pairmod.getPrerejectedRemoveParent();
      }
      
      // Any remaining mods on our stacks need to be flushed to the list
      for (ContentModification mod : preapprovedRemoveParent)
      {
         mods.add(mod);
      }
      for (ContentModification mod : prerejectedAddParent)
      {
         mods.add(mod);
      }
      return mods;
   }

   private OwnedItem[] getOwnedItems(RepositoryRootMetadata base, RepositoryRootMetadata modified)
   {
      TreeSet<OwnedItem> ownedItems = new TreeSet<OwnedItem>();
      for (RepositoryItemMetadata item : base.getContent())
      {
         ownedItems.add(new OwnedItem(item, true));
      }
      for (RepositoryItemMetadata item : modified.getContent())
      {
         ownedItems.add(new OwnedItem(item, false));
      }
      
      OwnedItem[] ownedItemArray = ownedItems.toArray(new OwnedItem[ownedItems.size()]);
      return ownedItemArray;
   }
   
   protected static class GeneratedModifications
   {
      private RepositoryItemMetadata preapprovedAddParent;
      private final Stack<ContentModification>  preapprovedRemoveParent;
      private final Stack<ContentModification>  prerejectedAddParent;
      private RepositoryItemMetadata prerejectedRemoveParent;
      private final List<ContentModification> modifications = new ArrayList<ContentModification>();
      
      public GeneratedModifications(RepositoryItemMetadata preapprovedAddParent,
            Stack<ContentModification> preapprovedRemoveParent, 
            Stack<ContentModification> prerejectedAddParent,
            RepositoryItemMetadata prerejectedRemoveParent)
      {
         if (preapprovedRemoveParent == null)
         {
            throw new IllegalArgumentException("Null preapprovedRemoveParent");
         }
         if (prerejectedAddParent == null)
         {
            throw new IllegalArgumentException("Null prerejectedAddParent");
         }
         this.preapprovedAddParent = preapprovedAddParent;
         this.preapprovedRemoveParent = preapprovedRemoveParent;
         this.prerejectedAddParent = prerejectedAddParent;
         this.prerejectedRemoveParent = prerejectedRemoveParent;
      }
      
      // ---------------------------------------------------------------  Public
      
      public void addModification(ContentModification mod)
      {
         modifications.add(mod);
      }      

      public List<ContentModification> getModifications()
      {
         return modifications;
      }

      public RepositoryItemMetadata getPreapprovedAddParent()
      {
         return preapprovedAddParent;
      }

      public void setPreapprovedAddParent(RepositoryItemMetadata preapprovedAddParent)
      {
         if (preapprovedAddParent != null)
         {
            validateCanCallSet();
         }
         this.preapprovedAddParent = preapprovedAddParent;
      }

      public ContentModification peekPreapprovedRemoveParent()
      {
         return preapprovedRemoveParent.size() == 0 ? null : preapprovedRemoveParent.peek();
      }

      public ContentModification popPreapprovedRemoveParent()
      {
         return preapprovedRemoveParent.size() == 0 ? null : preapprovedRemoveParent.pop();
      }

      public void pushPreapprovedRemoveParent(ContentModification toPush)
      {
         if (toPush == null)
         {
            throw new IllegalArgumentException("Null prerejectedAddParent");            
         }
         if (this.preapprovedAddParent != null)
         {
            throw new IllegalStateException("preapprovedAddParent already set");
         }
         else if (this.prerejectedAddParent.size() > 0)
         {
            throw new IllegalStateException("prerejectedAddParent already set");
         }
         // We allow removing content under something we've rejected
         // This happens when RepositoryItemMetadata w/ removed=false needs
         // to be pushed to a node
//         else if (this.prerejectedRemoveParent != null)
//         {
//            throw new IllegalStateException("prerejectedRemoveParent already set");
//         }
         
         ContentModification peeked = peekPreapprovedRemoveParent();
         if (peeked != null && toPush.getItem().isChildOf(peeked.getItem()) == false)
         {
            throw new IllegalArgumentException(toPush.getItem() + 
                  " is not a child of existing item " + peeked.getItem());
         }
         
         this.preapprovedRemoveParent.push(toPush);
      }

      public ContentModification peekPrerejectedAddParent()
      {
         return prerejectedAddParent.size() == 0 ? null : prerejectedAddParent.peek();
      }

      public ContentModification popPrerejectedAddParent()
      {
         return prerejectedAddParent.size() == 0 ? null : prerejectedAddParent.pop();
      }

      public void pushPrerejectedAddParent(ContentModification toPush)
      {
         if (toPush == null)
         {
            throw new IllegalArgumentException("Null prerejectedAddParent");            
         }
         if (this.preapprovedAddParent != null)
         {
            throw new IllegalStateException("preapprovedAddParent already set");
         }
         else if (this.preapprovedRemoveParent.size() > 0)
         {
            throw new IllegalStateException("preapprovedRemoveParent already set");
         }
         else if (this.prerejectedRemoveParent != null)
         {
            throw new IllegalStateException("prerejectedRemoveParent already set");
         }
         ContentModification peeked = peekPrerejectedAddParent();
         if (peeked != null && toPush.getItem().isChildOf(peeked.getItem()) == false)
         {
            throw new IllegalArgumentException(toPush.getItem() + 
                  " is not a child of existing item " + peeked.getItem());
         }
         
         this.prerejectedAddParent.push(toPush);
      }
      
      public RepositoryItemMetadata getPrerejectedRemoveParent()
      {
         return prerejectedRemoveParent;
      }

      public void setPrerejectedRemoveParent(RepositoryItemMetadata prerejectedRemoveParent)
      {
         if (prerejectedRemoveParent != null)
         {
            validateCanCallSet();
         }
         this.prerejectedRemoveParent = prerejectedRemoveParent;
      }

      private void validateCanCallSet()
      {
         if (this.preapprovedAddParent != null)
         {
            throw new IllegalStateException("preapprovedAddParent already set");
         }
         else if (this.preapprovedRemoveParent.size() > 0)
         {
            throw new IllegalStateException("preapprovedRemoveParent already set");
         }
         else if (this.prerejectedAddParent.size() > 0)
         {
            throw new IllegalStateException("prerejectedAddParent already set");
         }
         else if (this.prerejectedRemoveParent != null)
         {
            throw new IllegalStateException("prerejectedRemoveParent already set");
         }
      }
      
   }
   
   private static class OwnedItem implements Comparable<OwnedItem>
   {
      public final boolean base;
      public final RepositoryItemMetadata item;
      public final List<String> itemPath;
      
      private OwnedItem(RepositoryItemMetadata item, boolean base)
      {
         assert item != null : "item is null";
         this.item = item;
         this.itemPath = item.getRelativePathElements();
         this.base = base;
      }
      
      public int compareTo(OwnedItem other)
      {
         List<String> ourPath = itemPath;
         List<String> otherPath = other.itemPath;
         int result = 0;
         for (int i = 0; i < ourPath.size(); i++)
         {
            if (i >= otherPath.size())
            {
               // We've got extra levels they don't
               result = 1;
               break;
            }
            
            int comp = ourPath.get(i).compareTo(otherPath.get(i));
            if (comp != 0)
            {
               result = (comp < 0) ? -1 : 1;
               break;
            }
         }
         
         if (result == 0 && otherPath.size() != ourPath.size())
         {
            // They've got extra levels we don't
            result = -1;
         }
         
         if (result == 0)
         {
            // base comes before !base
            if (base && !other.base)
            {
               result = -1;
            }
            else if (other.base && !base)
            {
               result = 1;
            }
         }
         return result;
      }
   }
}
