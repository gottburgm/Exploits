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
package org.jboss.test.jmx.mbean;

import java.io.IOException;
import java.net.URL;
 
import org.jboss.system.ServiceMBean;

//$Id: TempFileCreatorServiceMBean.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  MBean interface for a Temp File creator
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Jun 30, 2006
 *  @version $Revision: 81036 $
 */
public interface TempFileCreatorServiceMBean
extends ServiceMBean
{
   /**
    * Create a temporary file
    * @param filename File Name of the temp file
    * @param data Data to be written into the temp file
    * @return URL of the temp file just created
    * @throws IOException
    */
  public URL createTempFile(String filename, String data) throws IOException;
}
