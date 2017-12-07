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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.logging.Logger;

/**
 * SFSB used for testing replicated query caching with an @EmbeddedId.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1.1 $
 */
@Stateful
@Remote(EmbeddedIdTest.class)
@RemoteBinding(jndiBinding="EmbeddedIdTestBean/remote")
public class EmbeddedIdTestBean implements EmbeddedIdTest
{
   private static final Logger log = Logger.getLogger(EmbeddedIdTestBean.class);
   
   @PersistenceContext
   private EntityManager manager;

   public void createMusician(MusicianPK pk, String instrument)
   {
      Musician musician = new Musician();
      musician.setId(pk);
      musician.setInstrument(instrument);
      manager.persist(musician);
   }

   public List<MusicianPK> getMusiciansForInstrument(String instrument, boolean useNamedRegion)
   {
      String queryName = useNamedRegion ? "musician.byinstrument.namedregion"
                                        : "musician.byinstrument.default";
      Query query = manager.createNamedQuery(queryName);
      query.setParameter(1, instrument);
      List<MusicianPK> result = new ArrayList<MusicianPK>();
      List users = query.getResultList();
      if (users != null)
      {
         for (Iterator it = users.iterator(); it.hasNext();)
         {
            result.add(((Musician) it.next()).getId());
         }
      }
      return result;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.test.clusteredentity.classloader.UserTest#cleanup()
    */
   public void cleanup()
   {
      internalCleanup();
   }
   
   private void internalCleanup()
   {
      if (manager != null)
      {
         Query query = manager.createQuery("select musician from Musician as musician");
         List accts = query.getResultList();
         if (accts != null)
         {
            for (Iterator it = accts.iterator(); it.hasNext();)
            {
               try
               {
                  Musician musician = (Musician) it.next();
                  log.info("Removing " + musician);
                  manager.remove(musician);
               }
               catch (Exception ignored) {}
            }
         }
      }
   }

   @PreDestroy
   @Remove
   public void remove()
   {
      try
      {
         internalCleanup();
      }
      catch (Exception e)
      {
         log.error("Caught exception in remove", e);
      }
   }

}
