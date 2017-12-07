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

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Comment
 * 
 * @author Brian Stansberry
 * @version $Revision: 60233 $
 */
@Entity
@Cache (usage=CacheConcurrencyStrategy.TRANSACTIONAL)
@NamedQueries({
   @NamedQuery(name="account.totalbalance.default",query="select account.balance from Account as account where account.accountHolder = ?1",
               hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
   @NamedQuery(name="account.totalbalance.namedregion",query="select account.balance from Account as account where account.accountHolder = ?1",
               hints={@QueryHint(name="org.hibernate.cacheRegion",value="AccountRegion"),
                      @QueryHint(name="org.hibernate.cacheable",value="true")
                     }),
   @NamedQuery(name="account.branch.default",query="select account.branch from Account as account where account.accountHolder = ?1",
               hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
   @NamedQuery(name="account.branch.namedregion",query="select account.branch from Account as account where account.accountHolder = ?1",
               hints={@QueryHint(name="org.hibernate.cacheRegion",value="AccountRegion"),
                      @QueryHint(name="org.hibernate.cacheable",value="true")
                     }),
   @NamedQuery(name="account.bybranch.default",query="select account from Account as account where account.branch = ?1",
               hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
   @NamedQuery(name="account.bybranch.namedregion",query="select account from Account as account where account.branch = ?1",
               hints={@QueryHint(name="org.hibernate.cacheRegion",value="AccountRegion"),
                      @QueryHint(name="org.hibernate.cacheable",value="true")
                     })
})
public class Account implements Serializable
{
   
   private static final long serialVersionUID = 1L;
   
   private Integer id;
   private AccountHolderPK accountHolder;
   private Integer balance;
   private String branch;
   
   @Id
   public Integer getId()
   {
      return id;
   }
   public void setId(Integer id)
   {
      this.id = id;
   }
   
//   @Embedded
   @Lob
   public AccountHolderPK getAccountHolder()
   {
      return accountHolder;
   }
   public void setAccountHolder(AccountHolderPK accountHolder)
   {
      this.accountHolder = accountHolder;
   }
   
   public Integer getBalance()
   {
      return balance;
   }
   public void setBalance(Integer balance)
   {
      this.balance = balance;
   }
   public String getBranch()
   {
      return branch;
   }
   public void setBranch(String branch)
   {
      this.branch = branch;
   }
   
   
   
   
   
}
