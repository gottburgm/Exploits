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
package org.jboss.test.cluster.clusteredentity.classloader;

/**
 * Comment
 * 
 * @author Brian Stansberry
 * @version $Revision: 60233 $
 */
public interface EntityQueryTest
{
   public abstract void getCache(boolean optimistic);

//   public abstract void createAccountHolder(AccountHolderPK pk, String postCode);
   
   public abstract void updateAccountBranch(Integer id, String branch);   

   public abstract void createAccount(AccountHolderPK pk, Integer id, Integer openingBalance, String branch);
   
   public abstract void updateAccountBalance(Integer id, Integer newBalance);

   
   public abstract int getCountForBranch(String branch, boolean useNamed, boolean useRegion);
   
   public abstract String getBranch(AccountHolderPK pk, boolean useNamed, boolean useRegion);

   public abstract int getTotalBalance(AccountHolderPK pk, boolean useNamed, boolean useRegion);

   public abstract boolean getSawRegionModification(String regionName);

   public abstract boolean getSawRegionAccess(String regionName);
   
   public abstract void cleanup();
   
   public abstract void remove(boolean removeEntities);

}