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
package org.jboss.system.server.profileservice.repository;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileFactory;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.metadata.ProfileMetaData;
import org.jboss.profileservice.spi.metadata.ProfileSourceMetaData;
import org.jboss.system.server.profile.repository.AbstractImmutableProfile;
import org.jboss.system.server.profile.repository.metadata.FilteredProfileMetaData;

/**
 * A filtered profile factory. This should create a profile based on the 
 * deployments defined in the meta data.
 * Currently this only creates a immutable profile, based on it's source.
 * 
 * @see {org.jboss.test.server.profileservice.support.FilteredProfileFactory}
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class FilteredProfileFactory extends AbstractProfileFactory implements ProfileFactory
{

   /** The profile meta data types. */
   public static final Collection<String> types;
   
   static
   {
      types = Arrays.asList(FilteredProfileMetaData.class.getName());
   }
   
   public String[] getTypes()
   {
      return types.toArray(new String[types.size()]);
   }
   
   @Override
   public Profile createProfile(ProfileKey key, ProfileMetaData metaData, List<ProfileKey> subProfiles)
         throws URISyntaxException
   {
      return new AbstractImmutableProfile(key, createURIs(metaData), subProfiles);
   }
   
   protected URI[] createURIs(ProfileMetaData metaData) throws URISyntaxException
   {
      ProfileSourceMetaData profileSource = metaData.getSource();
      if(profileSource == null)
         throw new IllegalArgumentException("Null profile source.");
      
      List<URI> uris = new ArrayList<URI>();
      for(String source : profileSource.getSources())
      {
         URI uri = new URI(source);
         uris.add(uri);
      }
      return uris.toArray(new URI[uris.size()]);
   }
   
}
