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
package org.jboss.test.guid;

import junit.framework.TestCase;
import org.jboss.util.id.GUID;
import org.jboss.util.id.VMID;
import org.jboss.util.id.UID;

import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * $Id: GUIDTest.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
 *
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 * @author Eike Dawid (JIRA user)
 */
public class GUIDTest extends TestCase
{

    private static final int ITERATIONS = 100;

    public void testGuid() throws InterruptedException
    {
        HashSet repositoryIDs = new HashSet();
        System.out.println();
        System.out.println("---------------------- testProveGuidHashCodeIsSame");
        GUID guid = null;
        for (int i = 0; i < ITERATIONS; i++)
        {
            guid = new GUID();

            Integer hashKey = new Integer(guid.hashCode());

            if (!repositoryIDs.contains(hashKey))
            {
                repositoryIDs.add(hashKey);
            }
            System.out.println("guid.hashCode()=" + guid.hashCode());
        }

        if (repositoryIDs.size()==1)
        {
            fail("HashCode is always returning the same hash");
        }
    }

    public void testUID() throws InterruptedException
    {
        HashSet repositoryIDs = new HashSet();
        System.out.println();
        System.out.println("---------------------- testProveUIDHashCodeIsSame");
        UID guid = null;
        for (int i = 0; i < ITERATIONS; i++)
        {
            guid = new UID();

            Integer hashKey = new Integer(guid.hashCode());

            if (!repositoryIDs.contains(hashKey))
            {
                repositoryIDs.add(hashKey);
            }
            System.out.println("guid.hashCode()=" + guid.hashCode());
        }

        if (repositoryIDs.size()==1)
        {
            fail("HashCode is always returning the same hash");
        }
    }

    public void testVMID() throws Exception
    {
        HashSet repositoryIDs = new HashSet();
        System.out.println();
        System.out.println("---------------------- testProveVMIDHashCodeIsSame");
        VMID guid = null;
        Method method = VMID.class.getDeclaredMethod("create", new Class[]{});
        method.setAccessible(true);
        for (int i = 0; i < ITERATIONS; i++)
        {
            guid = (VMID) method.invoke(null, new Object[]{});

            Integer hashKey = new Integer(guid.hashCode());

            if (!repositoryIDs.contains(hashKey))
            {
                repositoryIDs.add(hashKey);
            }
            System.out.println("guid.hashCode()=" + guid.hashCode());
        }

        if (repositoryIDs.size()==1)
        {
            fail("HashCode is always returning the same hash");
        }
    }
}
