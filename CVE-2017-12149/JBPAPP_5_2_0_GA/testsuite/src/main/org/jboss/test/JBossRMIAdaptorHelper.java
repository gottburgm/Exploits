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
package org.jboss.test;


import org.jboss.logging.Logger;

import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;


/**
 * Helper Class that connects to the RMA Adaptor on any JBoss node
 * to provide some services like start/stop JBoss services registered
 * in the MBean server.  Uses MBeanServerConnection.
 *
 * @author Anil.Saldhana@jboss.org
 * @version $Revision: 81036 $
 */

public class JBossRMIAdaptorHelper
{
    protected MBeanServerConnection rmiserver = null;
    protected Logger log = Logger.getLogger(JBossRMIAdaptorHelper.class);

    /**
     * Constructor
     */
    public JBossRMIAdaptorHelper()
    {
    }

    /**
     * Constructor that takes a JNDI url
     *
     * @param jndiurl JNDI Url (jnp://localhost:1099)
     */
    public JBossRMIAdaptorHelper(String jndiurl)
    {
        this();
        try
        {
            //Set Some JNDI Properties
            Hashtable env = new Hashtable();
            env.put(Context.PROVIDER_URL, jndiurl);
            env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
            env.put(Context.URL_PKG_PREFIXES, "org.jnp.interfaces");
            getMBeanServer(new InitialContext(env));
        } catch (Exception e)
        {
            log.debug(e);
        }
    }

    /**
     * Constructor that takes a JNDI url
     *
     * @param ctx InitialContext constructed
     */
    public JBossRMIAdaptorHelper(InitialContext ctx)
    {
        this();
        getMBeanServer(ctx);
    }


    /**
     * Get the Metadata for the MBean
     *
     * @param oname ObjectName of the MBean
     * @return MBeanInfo about the MBean
     */
    public MBeanInfo getMBeanInfo(ObjectName oname)
    {
        /* Example:
           //Get the MBeanInfo for the Tomcat MBean
           ObjectName name = new ObjectName( "jboss.web:service=WebServer" );
        */
        MBeanInfo info = null;

        try
        {
            info = rmiserver.getMBeanInfo(oname);
        } catch (Exception e)
        {
            log.debug(e);
        }
        return info;
    }

    /**
     * Invoke an Operation on the MBean
     *
     * @param oname      ObjectName of the MBean
     * @param methodname Name of the operation on the MBean
     * @param pParams    Arguments to the operation
     * @param pSignature Signature for the operation.
     * @return result from the MBean operation
     * @throws Exception
     */
    public Object invokeOperation(ObjectName oname,
                                  String methodname, Object[] pParams,
                                  String[] pSignature)
            throws Exception
    {
        Object result = null;
        try
        {
            /* Example:
            //Stop the Tomcat Instance
            Object result = server.invoke(name, "stop",null,null);
            */
            result = rmiserver.invoke(oname, methodname, pParams, pSignature);
        } catch (Exception e)
        {
            log.debug(e);
        }

        return result;
    }

    private void getMBeanServer(InitialContext ctx)
    {
        if (ctx == null)
            throw new IllegalArgumentException("Initial Context passed is null");
        try
        {
            rmiserver = (MBeanServerConnection) ctx.lookup("jmx/invoker/RMIAdaptor");
        } catch (NamingException e)
        {
            log.debug(e);
        }
        if (rmiserver == null) log.debug("RMIAdaptor is null");

    }


}//end class