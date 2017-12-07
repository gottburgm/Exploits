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
package javax.management.remote;

import java.net.MalformedURLException;
import java.io.Serializable;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class JMXServiceURL implements Serializable
{
   private static final long serialVersionUID = 8173364409860779292l;

   /**
    * Per spec, every jmx service url must start with this prefix string.
    */
   public static final String REQUIRED_URL_PREFIX = "service:jmx:";

   private static final int DEFAULT_PORT = 0; //TODO: -TME How to know what/if default port should be set to (JBREM-149)

   private String protocol = null;
   private String host = null;
   private int port = DEFAULT_PORT;
   private String urlPath = "";

   private transient String stringServiceURL = null;

   protected transient Logger log = Logger.getLogger(getClass());


   public JMXServiceURL(String serviceURL) throws MalformedURLException
   {
      if(serviceURL != null)
      {
         if(serviceURL.startsWith(REQUIRED_URL_PREFIX))
         {
            if(log.isTraceEnabled())
            {
               log.trace("Start parsing of jmx service url: " + serviceURL);
            }
            // begin parsing rest of the url, minus the prefix
            parseCoreURL(serviceURL.substring(REQUIRED_URL_PREFIX.length()));
         }
         else
         {
            throw new MalformedURLException("Can not create JMXServiceURL due to provided service url not starting with " +
                                            REQUIRED_URL_PREFIX + ".  URL String provided was: " + serviceURL);
         }
      }
      else
      {
         throw new NullPointerException("Can not create JMXServiceURL due to constructor parameter being null.");
      }

   }

   public JMXServiceURL(String protocol, String host, int port) throws MalformedURLException
   {
      this(protocol, host, port, null);
   }

   public JMXServiceURL(String protocol, String host, int port, String urlPath) throws MalformedURLException
   {
      setProtocol(protocol);
      setHost(host);
      setPort(port);
      setPath(urlPath);
   }

   /**
    * Will parse the service url with the jmx service prefix removed working from left to right.
    *
    * @param serviceURL
    */
   private void parseCoreURL(String serviceURL) throws MalformedURLException
   {
      // first look for protocol, which will end with '://'
      final String protocolSeperator = "://";
      int index = serviceURL.indexOf(protocolSeperator);
      if(index == -1)
      {
         log.error("Error parsing core jmx service url.  Could not find protocol within string " + serviceURL);
         throw new MalformedURLException("Error in parsing JMX service URL.  Does not properly define protocol.");
      }
      String invalidatedProtocol = serviceURL.substring(0, index);
      setProtocol(validateProtocol(invalidatedProtocol));

      // next is host and port
      String hostPlus = serviceURL.substring(index + protocolSeperator.length());

      String path = parseHostAndPort(hostPlus);

      if(path != null && path.length() > 0)
      {
         setPath(validatePath(path));
      }

      log.debug("Have parsed jmx service url into following values - protocol: " + getProtocol() +
                ", host: " + getHost() + ", port: " + getPort() + ", path: " + getURLPath());

   }

   private String validatePath(String path) throws MalformedURLException
   {
      if(!path.startsWith("/") && !path.startsWith(";"))
      {
         throw new MalformedURLException("Error parsing JMX service URL.  " +
                                         "The path did not begin with either a '/' or ';' character.");
      }
      else
      {
         return path;
      }
   }

   private void setPath(String path)
   {
      if(path == null)
      {
         this.urlPath = "";
      }
      else
      {
         this.urlPath = path;
      }
   }

   private String parseHostAndPort(String hostPlus)
         throws MalformedURLException
   {
      String pathString = null;

      int index = -1;
      // now check to see if is path seperator.  per spec, seperator can be either / or ;
      index = hostPlus.indexOf(';');
      index = index == -1 ? hostPlus.indexOf('/') : index;
      // capture path info is available
      if(index != -1)
      {
         setHost(null);
         pathString = hostPlus.substring(index);
      }
      hostPlus = index == -1 ? hostPlus : hostPlus.substring(0, index);

      if(hostPlus.length() != 0)
      {
         int endOfHostIndex = -1;

         // first need to check if host is IPv6, which will start with bracket
         if(hostPlus.charAt(0) == '[')
         {
            endOfHostIndex = hostPlus.lastIndexOf(']');
            if(endOfHostIndex == -1)
            {
               log.error("Could not process service url section " + hostPlus + " due to the host section " +
                         "starting with [ but no ending ] before the path seperator.");
               throw new MalformedURLException("Error parsing JMX service URL.  Host started with [, but did not have an end ].");
            }
            String ipv6Host = hostPlus.substring(1, endOfHostIndex);
            setHost(ipv6Host);
            endOfHostIndex++; // need to move index to next character after ]
         }
         else // should be dealing with IPv4 then
         {
            endOfHostIndex = hostPlus.indexOf(':');
            if(endOfHostIndex != -1)
            {
               // means that no host specified
               if(endOfHostIndex == 0)
               {
                  throw new MalformedURLException("Error parsing JMX service URL.  Port specified, but not host.");
               }
               else
               {
                  setHost(hostPlus.substring(0, endOfHostIndex));
               }
            }
            else
            {
               setHost(hostPlus);
            }
         }

         // now onto port, any leftover characters for port
         if(endOfHostIndex != -1 && hostPlus.length() > (endOfHostIndex))
         {
            String portString = hostPlus.substring(endOfHostIndex);

            if(portString.charAt(0) != ':')
            {
               log.error("Error parsing host and port of jmx service url.  Host and port must be seperated by ':'.  " +
                         "Offending serviceURL section is " + hostPlus);
               throw new MalformedURLException("Error in parsing JMX service URL.  Host and port must be seperated by ':'.");
            }
            else
            {
               if(getHost() == null || getHost().length() == 0)
               {
                  throw new MalformedURLException("Error parsing JMX service URL.  " +
                                                  "Can not have port specified without having a host specified as well.");
               }
               String invalidatedPort = portString.substring(1);
               setPort(validatePort(invalidatedPort));
            }
         }
      }

      return pathString;
   }

   private void setHost(String serviceHost)
   {
      if(serviceHost == null)
      {
         /*
         try
         {
            this.host = InetAddress.getLocalHost().getHostName();
         }
         catch(UnknownHostException e)
         {
            log.error("Error getting local host name to return as host.", e);
            throw new RuntimeException("Error getting local host name to return as JMX Service host.", e);
         }
         */
         this.host = null;
      }
      else
      {
         // check to see if is ipv6, which may start and end with bracket
         if(serviceHost.charAt(0) == '[' && serviceHost.charAt(serviceHost.length() - 1) == ']')
         {
            this.host = serviceHost.substring(1, serviceHost.length() - 1);
         }
         else
         {
            this.host = serviceHost;
         }
      }
   }

   private void setPort(int port)
   {
      this.port = port;
   }

   /**
    * Will convert from string to int.  If value is 0, will set return value to DEFAULT_PORT.
    *
    * @param invalidatedPort
    * @return
    * @throws MalformedURLException
    */
   private int validatePort(String invalidatedPort) throws MalformedURLException
   {
      int validatedPort = DEFAULT_PORT;

      try
      {
         validatedPort = Integer.parseInt(invalidatedPort);
         if(validatedPort == 0)
         {
            validatedPort = DEFAULT_PORT;
         }
      }
      catch(NumberFormatException e)
      {
         throw new MalformedURLException("Error converting JMX Service url port (" + invalidatedPort + ") to a numeric value.");
      }
      return validatedPort;
   }

   private void setProtocol(String protocol) throws MalformedURLException
   {

      this.protocol = validateProtocol(protocol);
   }

   private String validateProtocol(String protocol)
         throws MalformedURLException
   {
      if(protocol == null || protocol.length() == 0)
      {
         return "jmxmp";
      }

      // now check for valid ASCII characters
      for(int x = 0; x < protocol.length(); x++)
      {
         char c = protocol.charAt(x);
         // per spec, protocol must be one of the following:
         // +, -, 0-9, A-Z, or a-z
         if(c != 43 && c != 45 && !(c >= 48 && c <= 57) && !(c >= 65 && c <= 90) && !(c >= 97 && c <= 122))
         {
            throw new MalformedURLException("Error in parsing JMX service ULR protocol because " +
                                            "contains no valid character 0x" + Integer.toHexString(c));
         }
      }

      return protocol.toLowerCase();
   }

   public String getProtocol()
   {
      return protocol;
   }

   public String getHost()
   {
      return host;
   }

   public int getPort()
   {
      return port;
   }

   public String getURLPath()
   {
      return urlPath;
   }

   public String toString()
   {
      if(stringServiceURL == null)
      {
         // have to compose formal service url
         StringBuffer buffer = new StringBuffer(REQUIRED_URL_PREFIX);
         buffer.append(getProtocol());
         buffer.append("://");
         String host = getHost();
         if(host != null)
         {
            // have to brackets around if ipv6 (which should contain : somewhere)
            if(host.indexOf(':') != -1)
            {
               buffer.append("[");
               buffer.append(host);
               buffer.append("]");
            }
            else
            {
               buffer.append(host);
            }
         }
         int port = getPort();
         if(port > 0)
         {
            buffer.append(":");
            buffer.append(port);
         }
         buffer.append(getURLPath());
         stringServiceURL = buffer.toString();
      }
      return stringServiceURL;
   }

   public boolean equals(Object obj)
   {
      if(obj instanceof JMXServiceURL)
      {
         JMXServiceURL jsu = (JMXServiceURL) obj;
         if(getProtocol().equalsIgnoreCase(jsu.getProtocol()) &&
            getPort() == jsu.getPort())
         {
            String host = getHost();
            if(host == null)
            {
               if(jsu.getHost() != null)
               {
                  return false;
               }
               else
               {
                  String urlPath = getURLPath();
                  if(urlPath == null && jsu.getURLPath() != null)
                  {
                     return false;
                  }
                  else
                  {
                     return urlPath.equals(jsu.getURLPath());
                  }

               }
            }
            else
            {
               if(host.equals(jsu.getHost()))
               {
                  String urlPath = getURLPath();
                  if(urlPath == null && jsu.getURLPath() != null)
                  {
                     return false;
                  }
                  else
                  {
                     return urlPath.equals(jsu.getURLPath());
                  }
               }
               else
               {
                  return false;
               }
            }
         }
         else
         {
            return false;
         }
      }
      else
      {
         return false;
      }
   }

   public int hashCode()
   {
      return toString().hashCode();
   }
}
