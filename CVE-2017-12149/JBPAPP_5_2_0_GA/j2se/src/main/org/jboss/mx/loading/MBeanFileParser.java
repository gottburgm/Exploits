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
package org.jboss.mx.loading;

import java.util.Set;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.ParseException;

/**
 * Interface that abstracts the access to different MBean loader parsers
 * (MLet file parsers, XML based parsers, etc.).
 *
 * @see javax.management.loading.MLet
 * @see org.jboss.mx.loading.MLetParser
 * @see org.jboss.mx.loading.MBeanLoader
 * @see org.jboss.mx.loading.XMLMBeanParser
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $
 *   
 */
public interface MBeanFileParser
{

   /**
    * Parses a file that describes the configuration of MBeans to load and
    * instantiate in the MBean server (for example, MLet text file).
    *
    * @see     org.jboss.mx.loading.MBeanElement
    *
    * @param   url   URL of the file
    * @return  a set of <tt>MBeanElement</tt> objects that contain the required
    *          information to load and register the MBean
    * @throws  ParseException if there was an error parsing the file
    */
   Set parseMBeanFile(URL url) throws ParseException;
   
   /**
    * Parses a file that describes the configuration of MBean to load and
    * instantiate in the MBean server (for example, MLet text file).
    *
    * @see     org.jboss.mx.loading.MBeanElement
    *
    * @param   url   URL of the file
    * @return  a set of <tt>MBeanElement</tt> objects that contain the required
    *          information to load and register the MBean
    * @throws  ParseException if there was an error parsing the file
    * @throws  MalformedURLException if the URL string was not valid
    */
   Set parseMBeanFile(String url) throws ParseException, MalformedURLException;
   
}
      



