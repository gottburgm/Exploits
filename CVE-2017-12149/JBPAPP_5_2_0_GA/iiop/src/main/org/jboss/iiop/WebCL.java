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
package org.jboss.iiop;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.management.ObjectName;

import org.jboss.classloading.spi.RealClassLoader;
import org.jboss.logging.Logger;
import org.jboss.mx.loading.RepositoryClassLoader;
import org.jboss.proxy.compiler.IIOPStubCompiler;
import org.jboss.web.WebClassLoader;

/**
 * A subclass of WebClassLoader that does IIOP bytecode generation on the fly.
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @author adrian@jboss.org
 * @version $Revision: 81018 $
*/
public class WebCL extends WebClassLoader
{
    /** Logger for trace messages */
    static Logger logger = Logger.getLogger(WebCL.class);

    /** Map from stub classes into bytecode arrays (stub bytecode cache) */
    private Map loadedStubMap = Collections.synchronizedMap(new WeakHashMap());

    public WebCL(ObjectName container, RealClassLoader parent)
    {
        super(container, parent);
        logger.debug("Constructed WebCL " + this.toString());
        logger.debug("           parent " + parent.toString());

        // Turn standard loading back on (we do classloading)
        standard = true;
    }

    /** Gets a string key used as the key into the WebServer's loaderMap. */
    public String getKey()
    {
        String className = getClass().getName();
        int dot = className.lastIndexOf('.');
        if( dot >= 0 )
            className = className.substring(dot+1);
        String jndiName = getContainer().getKeyProperty("jndiName");
        String key =  className + '[' + jndiName + ']';
        return key;
    }

    /** Gets the bytecodes for a given stub class. */
    public byte[] getBytes(Class clz) {
        byte[] code = (byte[])loadedStubMap.get(clz);
        return (code == null) ? null : (byte[])code.clone();
    }
   
    protected  Class findClass(String name) 
        throws ClassNotFoundException 
    {
        if (logger.isTraceEnabled()) {
            logger.trace("findClass(" + name + ") called");
        }
        if (name.endsWith("_Stub")) {
            int start = name.lastIndexOf('.') + 1;
            if (name.charAt(start) == '_') {
                String pkg = name.substring(0, start);
                String interfaceName = pkg + name.substring(start + 1, 
                                                            name.length() - 5);

                // This is a workaround for a problem in the RMI/IIOP 
                // stub loading code in SUN JDK 1.4.x, which prepends 
                // "org.omg.stub." to classes in certain name spaces, 
                // such as "com.sun". This non-compliant behavior
                // results in failures when deploying SUN example code, 
                // including ECPerf and PetStore, so we remove the prefix.
                if (interfaceName.startsWith("org.omg.stub.com.sun."))
                    interfaceName = interfaceName.substring(13);

                Class intf = super.loadClass(interfaceName);
                if (logger.isTraceEnabled()) {
                    logger.trace("loaded class " + interfaceName);
                }
                
                try {
                    byte[] code = 
                        IIOPStubCompiler.compile(intf, name);
               
                    if (logger.isTraceEnabled()) {
                        logger.trace("compiled stub class for " 
                                     + interfaceName);
                    }
                    Class clz = defineClass(name, code, 0, code.length);
                    if (logger.isTraceEnabled()) {
                        logger.trace("defined stub class for " 
                                     + interfaceName);
                    }
                    resolveClass(clz);
                    try {
                        clz.newInstance();
                    } 
                    catch (Throwable t) {
                        //t.printStackTrace();
                        throw new org.jboss.util.NestedRuntimeException(t);
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("resolved stub class for " 
                                     + interfaceName);
                    }
                    loadedStubMap.put(clz, code);
                    return clz;
                }
                catch (RuntimeException e) {
                    logger.error("failed finding class " + name, e);
                    //throw e;
                    return super.findClass(name);
                }
            }
            else {
                return super.findClass(name);
            }
        }
        else {
            return super.findClass(name);
        }
    }

}
