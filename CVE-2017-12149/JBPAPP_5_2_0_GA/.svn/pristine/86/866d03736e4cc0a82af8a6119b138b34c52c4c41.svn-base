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
package org.jboss.web.jsf.integration.serialization;

import com.sun.faces.spi.SerializationProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.jboss.logging.Logger;
import org.jboss.serial.io.JBossObjectOutputStream;

/**
 * Provides interface between JSF RI and JBoss Serialization for better
 * performance of client-side state saving.
 *
 * @author Stan Silvert
 */
public class JBossSerializationProvider implements SerializationProvider 
{
    private static final Logger LOG = Logger.getLogger(JBossSerializationProvider.class);

    /**
     * No-arg constructor required.
     */
    public JBossSerializationProvider() 
    {
        LOG.info("Using JBoss Serialization for JavaServer Faces.");
    }
    
    /**
     * Create a fast ObjectInputStream using JBoss Serialization.
     */
    public ObjectInputStream createObjectInputStream(InputStream source) throws IOException {
        return new JBossFacesObjectInputStream(source);
    }

    /**
     * Create a fast ObjectOutputStream using JBoss Serialization.
     */
    public ObjectOutputStream createObjectOutputStream(OutputStream destination) throws IOException 
    {
        return new JBossObjectOutputStream(destination);
    }
    
}
