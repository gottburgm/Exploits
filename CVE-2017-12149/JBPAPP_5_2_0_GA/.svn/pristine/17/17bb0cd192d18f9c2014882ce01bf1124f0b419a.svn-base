/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.test.ejb3.jbpapp923;

import static javax.ejb.TransactionAttributeType.REQUIRED;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
@Stateful
@TransactionAttribute(TransactionAttributeType.NEVER)
public class BeanA implements RemoteA {
    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager em;
    @EJB
    private LocalB beanB;

    public boolean check() {
        MyClassA a = find(2L);
        if (!em.contains(a))
            throw new IllegalStateException("EM should contain #2");
        beanB.find("myId"); // JBPAPP-923: Now every object returned from A's EM should still be attached.
        return em.contains(a);
    }

    @TransactionAttribute(REQUIRED)
    public boolean checkInTx() {
        return check();
    }

    public void create() {
        final MyClassA entity = new MyClassA();
        entity.setId(2L);
        em.persist(entity);
    }

    public MyClassA find(Long id) {
        return em.find(MyClassA.class, id);
    }
}