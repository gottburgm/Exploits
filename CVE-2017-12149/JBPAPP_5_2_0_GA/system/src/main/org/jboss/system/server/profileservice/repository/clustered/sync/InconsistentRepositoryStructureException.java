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
import java.util.List;

import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryRootMetadata;


/**
 * Exception indicating that 
 * {@link RepositoryContentMetadata#getRepositories() the list of repository roots} 
 * is inconsistent between two <code>RepositoryContentMetadata</code> objects. 
 * The expected cause of this would be two repositories set up with incompatible
 * sets of backing URIs.
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class InconsistentRepositoryStructureException extends Exception
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 8104994059142991240L;

   public InconsistentRepositoryStructureException(RepositoryContentMetadata base, 
                                                   RepositoryContentMetadata other)
   {
      super(buildExceptionMessage(base, other));
   }

   private static String buildExceptionMessage(RepositoryContentMetadata base, RepositoryContentMetadata other)
   {
      List<String> missing = new ArrayList<String>();
      for (RepositoryRootMetadata root : base.getRepositories())
      {
         RepositoryRootMetadata otherRoot = other.getRepositoryRootMetadata(root.getName());
         if (otherRoot == null)
         {
            missing.add(root.getName());
         }
      }
      List<String> extra = new ArrayList<String>();
      for (RepositoryRootMetadata root : other.getRepositories())
      {
         RepositoryRootMetadata baseRoot = base.getRepositoryRootMetadata(root.getName());
         if (baseRoot == null)
         {
            extra.add(root.getName());
         }
      }
      
      StringBuilder sb = new StringBuilder("Inconsistent structure between repositories");
      if (missing.size() > 0)
      {
         sb.append("; Other repository is missing roots ");
         boolean first = true;
         for (String name : missing)
         {
            if (!first)
            {
               sb.append(',');
            }
            else
            {
               first = false;
            }
            sb.append(name);
         }
      }
      if (extra.size() > 0)
      {
         sb.append("; Other repository has extra roots ");
         boolean first = true;
         for (String name : extra)
         {
            if (!first)
            {
               sb.append(',');
            }
            else
            {
               first = false;
            }
            sb.append(name);
         }
         
      }
      
      return sb.toString();
   }
}
