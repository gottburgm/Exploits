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
package org.jboss.mx.util;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * MBeanTyper is a helper class that creates a typed-object from an MBean ObjectName and a main
 * interface class that the MBean implements.  You can then use the returned object (casted to the appropriate
 * main interface class) with the correct typed signatures instead of <tt>mbeanserver.invoke(objectname,<sig>,etc.)</tt>.
 * <P>
 * Example usage: <BR>
 * <code><tt>
 *      MyInterfaceMBean mbean=(MyInterfaceMBean)MBeanTyper.typeMBean(server,new ObjectName(":type=MyBean"),MyInterfaceMBean.class);
 *      mbean.foobar();
 * </tt></code> <P>
 *
 * To turn debug on for this package, set the System property <tt>vocalos.jmx.mbeantyper.debug</tt> to true.
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 */
public class MBeanTyper
{
    static final boolean DEBUG = Boolean.getBoolean("jboss.jmx.debug");

    /**
     * create a typed object from an mbean
     */
    public static final Object typeMBean(MBeanServer server, ObjectName mbean, Class mainInterface)
            throws Exception
    {
        List interfaces = new ArrayList();
        if (mainInterface.isInterface())
        {
            interfaces.add(mainInterface);
        }
        addInterfaces(mainInterface.getInterfaces(), interfaces);
        Class cl[] = (Class[]) interfaces.toArray(new Class[interfaces.size()]);
        if (DEBUG)
        {
            System.err.println("typeMean->server=" + server + ",mbean=" + mbean + ",mainInterface=" + mainInterface);
            for (int c = 0; c < cl.length; c++)
            {
                System.err.println("     :" + cl[c]);
            }
        }

        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), cl, new MBeanTyperInvoker(server, mbean));
    }

    private static final void addInterfaces(Class cl[], List list)
    {
        if (cl == null) return;
        for (int c = 0; c < cl.length; c++)
        {
            list.add(cl[c]);
            addInterfaces(cl[c].getInterfaces(), list);
        }
    }
}
