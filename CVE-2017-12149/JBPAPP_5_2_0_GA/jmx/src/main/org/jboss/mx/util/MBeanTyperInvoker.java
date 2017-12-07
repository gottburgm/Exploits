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

import javax.management.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * MBeanTyperInvoker handles method invocations against the MBeanTyper target object
 * and forwards them to the MBeanServer and ObjectName for invocation.
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 */
final class MBeanTyperInvoker implements java.lang.reflect.InvocationHandler
{
    private final MBeanServer server;
    private final ObjectName mbean;
    private final Map signatureCache = Collections.synchronizedMap(new HashMap());

    MBeanTyperInvoker(MBeanServer server, ObjectName mbean)
    {
        this.server = server;
        this.mbean = mbean;
    }

    private boolean isJMXAttribute(Method m)
    {
        String name = m.getName();
        return (name.startsWith("get"));

    }

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
    {
        if (MBeanTyper.DEBUG)
        {
            System.err.println("  ++ method=" + method.getName() + ",args=" + args);
        }
        try
        {
            if (method.getDeclaringClass() == Object.class)
            {
                String name = method.getName();
                if (name.equals("hashCode"))
                {
                    return new Integer(this.hashCode());
                }
                else if (name.equals("toString"))
                {
                    return this.toString();
                }
                else if (name.equals("equals"))
                {
                    // FIXME: this needs to be reviewed - we should be
                    // smarter about this ...
                    return new Boolean(equals(args[0]));
                }
            }
            else if (isJMXAttribute(method) && (args == null || args.length <= 0))
            {
                String name = method.getName().substring(3);
                return server.getAttribute(mbean, name);
            }

            String sig[] = (String[]) signatureCache.get(method);
            if (sig == null)
            {
                // get the method signature from the method argument directly
                // vs. the arguments passed, since there may be primitives that
                // are wrapped as objects in the arguments
                Class _args[] = method.getParameterTypes();
                if (_args != null && _args.length > 0)
                {
                    sig = new String[_args.length];
                    for (int c = 0; c < sig.length; c++)
                    {
                        if (_args[c] != null)
                        {
                            sig[c] = _args[c].getName();
                        }
                    }
                }
                else
                {
                    sig = new String[0];
                }
                signatureCache.put(method, sig);
            }
            return server.invoke(mbean, method.getName(), args, sig);
        }
        catch (Throwable t)
        {
            if (MBeanTyper.DEBUG)
            {
                t.printStackTrace();
            }
            if (t instanceof UndeclaredThrowableException)
            {
                UndeclaredThrowableException ut = (UndeclaredThrowableException) t;
                throw ut.getUndeclaredThrowable();
            }
            else if (t instanceof InvocationTargetException)
            {
                InvocationTargetException it = (InvocationTargetException) t;
                throw it.getTargetException();
            }
            else if (t instanceof MBeanException)
            {
                MBeanException me=(MBeanException)t;
                throw me.getTargetException();
            }
            else
            {
                throw t;
            }
        }
    }
}
