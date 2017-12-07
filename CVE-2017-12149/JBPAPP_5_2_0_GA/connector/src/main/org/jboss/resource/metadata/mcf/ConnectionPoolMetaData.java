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
package org.jboss.resource.metadata.mcf;

/**
 * A PoolMetaData.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 85945 $
 */
public interface ConnectionPoolMetaData
{
   public int getMinSize();
   public void setMinSize(int minSize);
   
   public int getMaxSize();
   public void setMaxSize(int maxSize);
   
   public long getBlockingTimeoutMilliSeconds();
   public void setBlockingTimeoutMilliSeconds(long blockTimeout);
   
   public int getIdleTimeoutMinutes();
   public void setIdleTimeoutMinutes(int idleTimeout);
   
   public void setPrefill(Boolean prefill);
   public Boolean getPrefill();
   
   public void setBackgroundValidationMillis(long interval);
   public long getBackgroundValidationMillis();
   
   public void setValidateOnMatch(boolean validateOnMatch);
   public boolean isValidateOnMatch();
   
   public void setNoTxSeparatePools(Boolean notxpool);
   public Boolean getNoTxSeparatePools();      
}
