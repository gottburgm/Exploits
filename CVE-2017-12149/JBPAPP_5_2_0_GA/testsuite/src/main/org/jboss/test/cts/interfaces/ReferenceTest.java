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
package org.jboss.test.cts.interfaces;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/** A simple Serializable that is used to test the call optimization
of EJBs. When a call is optimized an instance of this class should be
passed by reference rather than being serialized.

@author Scott.Stark@jboss.org
@version $Revision: 81036 $
*/
public class ReferenceTest implements Serializable
{
    private transient boolean wasSerialized;

    /** Creates new ReferenceTest */
    public ReferenceTest()
    {
    }

    public boolean getWasSerialized()
    {
        return wasSerialized;
    }

    private void readObject(ObjectInputStream in)
         throws IOException, ClassNotFoundException
    {
        wasSerialized = true;
    }
}
