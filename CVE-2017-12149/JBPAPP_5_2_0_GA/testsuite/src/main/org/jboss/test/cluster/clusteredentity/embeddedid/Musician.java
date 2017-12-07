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
package org.jboss.test.cluster.clusteredentity.embeddedid;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Entity used for testing replicated query caching with an @EmbeddedId.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1.1 $
 */
@Entity
@Cache (usage=CacheConcurrencyStrategy.TRANSACTIONAL)
@NamedQueries({
   @NamedQuery(name="musician.byinstrument.default",query="select musician from Musician as musician where musician.instrument = ?1",
               hints={@QueryHint(name="org.hibernate.cacheable",value="true")}),
   @NamedQuery(name="musician.byinstrument.namedregion",query="select musician from Musician as musician where musician.instrument = ?1",
         hints={@QueryHint(name="org.hibernate.cacheable",value="true"),
                @QueryHint(name="org.hibernate.cacheable",value="true")
               })
})
public class Musician
{
   private MusicianPK id;
   private String instrument;
   
   @EmbeddedId
   public MusicianPK getId()
   {
      return id;
   }
   public void setId(MusicianPK id)
   {
      this.id = id;
   }
   
   public String getInstrument()
   {
      return instrument;
   }
   public void setInstrument(String instrument)
   {
      this.instrument = instrument;
   }
   
}
