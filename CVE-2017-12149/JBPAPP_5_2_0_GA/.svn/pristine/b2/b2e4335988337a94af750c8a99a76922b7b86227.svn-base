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
package org.jboss.test.cluster.httpsessionreplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection; 
import java.util.Properties;

import junit.framework.Test;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpRecoverableException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.JBossClusteredTestCase;
 
/**
 *
 * @see org.jboss.test.cluster.httpsessionreplication
 *
 * @author  <a href="mailto:anil.saldhana@jboss.com">Anil Saldhana</a>.
 * @version $Revision: 1.0 
 */
public class HttpSessionReplicationUnitTestCase 
extends JBossClusteredTestCase {
    /**
     * The Servernames should be configurable.
     */
	private String[] servernames= {"jnp://localhost:1099", "jnp://localhost:1199"};
	
	/**
	 * The main properties file that should be under src/resources/cluster
	 */
	private Properties prop = null;
	
	/**
	 * Denotes number of nodes in the cluster test
	 */
	private int numInstances = 0;
	
	public HttpSessionReplicationUnitTestCase (String name) {
	      super(name);
	      try{
	      	this.getPropertiesFile();
	         String numin = prop.getProperty("NumOfInstances");
			 numInstances = Integer.parseInt( numin );
			 if( numInstances < 2 ) fail( "Atleast two nodes needed");
			 
			 //Lets build up the jndi server urls now
			 this.setServerNames(servernames);
			}catch( Exception e){
				fail( e.getMessage());
			}
	}
	
	public static Test suite() throws Exception
	{
		//The following jar deployment is a dummy. 
	      Test t1 = JBossClusteredTestCase.getDeploySetup(HttpSessionReplicationUnitTestCase.class, 
	      		                    "httpsessionreplication.jar");
	      return t1;
	}
	
	/**
	 * Tests connection to the Apache Server.
	 * Note: We deal with just one Apache Server. We can bounce the different
	 * JBoss/Tomcat servers and Apache will loadbalance.
	 * @throws Exception
	 */
	public void testApacheConnection()
	throws Exception
	{
		getLog().debug("Enter testApacheConnection");
		try {
	        //	makeConnection( "http://localhost");
			this.makeConnection(prop.getProperty("ApacheUrl"));
		} catch (Exception e) {
	    }
	    getLog().debug("Exit testApacheConnection");
	}
	
	/**
	 * Main method that deals with the Http Session Replication Test
	 * @throws Exception
	 */
	public void testHttpSessionReplication() 
	throws Exception
	{
		String attr = "";
		getLog().debug("Enter testHttpSessionReplication");
		//First need to make a Http Connection to Apache and get the session id
		//Then bring down the first instance and make another call
		//Then check the session id or just see if the server has not returned an error
        //String urlname = "http://localhost/testsessionreplication.jsp";
        //String geturlname = "http://localhost/getattribute.jsp";
        
        String urlname = prop.getProperty("SetAttrUrl");
        String geturlname = prop.getProperty("GetAttrUrl");
        /*
	    makeConnection(urlname);
        getHttpText( urlname );
        
        //Get the Attribute set by testsessionreplication.jsp
        attr= getAttribute( geturlname );
        //Shut down the first instance
        shutDownInstance( "localhost:1099");
        //Give 30 seconds for things to stabilize.
        sleepThread(30*1000);//30 seconds
        if( !getAttribute(geturlname).equals(attr)) fail("Http Session Replication Failed");
        getLog().debug("Http Session Replication has happened");
        getLog().debug("Exit testHttpSessionReplication");
        */
        
//      Create an instance of HttpClient.
        HttpClient client = new HttpClient(); 

      // Create a method instance.
      HttpMethod method = new GetMethod(urlname);
      String str = makeGet( client, method );
      
      //Make a second connection
      method = new GetMethod(geturlname);

//    Get the Attribute set by testsessionreplication.jsp
      attr= makeGet( client,method );
//    Shut down the first instance
      //shutDownInstance( "localhost:1099");
      shutDownInstance( 1 );
      getLog().debug( "Brought down the first instance");
//    Give 30 seconds for things to stabilize.
      sleepThread(30*1000);//30 seconds
      
//    Make connection
      method = new GetMethod(geturlname);
      String attr2= makeGet( client,  method );
      if( ! attr2.equals(attr)) fail("Http Session Replication Failed");
      getLog().debug("Http Session Replication has happened");
      getLog().debug("Exit testHttpSessionReplication");
	}

	/**
	 * Reads in the properties file
	 */
	public void getPropertiesFile(){
	    prop = new Properties();
		try{
			java.net.URL url = ClassLoader.getSystemResource("cluster/cluster-test.properties");
		    prop.load( url.openStream()); 
		}catch( Exception e){
			fail("Need a properties file under src/resources/cluster:"+e.getMessage());
		}
	}
	
	/**
	 * Shuts down an instance of JBoss.
	 * @throws Exception
	 */
	private void shutDownInstance(int instancenum) 
	throws Exception
	{
		String command = getCommand(instancenum);
		
		getLog().debug("Going to execute:"+command);
        Process child = Runtime.getRuntime().exec(command);
        sleepThread( 10*1000 );
        getLog().debug("Process exit value="+child.exitValue());
     }
	
	/**
	 * Generate the command to run to shutdown a jboss node
	 * @param instancenum
	 * @return
	 */
	private String getCommand( int instancenum) {
		//String base="/Users/anil/jboss-head/build/output/jboss-4.0.0DR4";
		//String cpath = base+"/bin/shutdown.jar:"+base+"/client/jbossall-client.jar";
		//String command = "java  -server -Xms128m -Xmx128m -classpath "+" org.jboss.Shutdown -s "+jndiurl;
		//String command = base+"/bin/shutdown.sh -s "+jndiurl;
		String command = "";
		try{
			command = prop.getProperty("jboss.location") + prop.getProperty("ShutDownScript");
			command += "  -s " + "jnp://"+prop.getProperty("Instance"+instancenum+".host")+":"+
			                    prop.getProperty("Instance"+instancenum+".port.jndi");
		}catch( Exception e){
			fail( "getCommand Failed with:"+ e.getMessage());
		}
		
		return command;
	}

	/**
	 * Sleep for specified time
	 * @param millisecs
	 * @throws Exception
	 */
	private void sleepThread(long millisecs)
	throws Exception {
		Thread.sleep(millisecs);
	}
	
	/**
	 * Makes a HTTP Connection
	 * @param urlname
	 * @throws Exception
	 */
	private void makeConnection( String urlname )
	throws Exception
	{
		getLog().debug("Enter makeConnection");
		try {
	        // Step 1: Create URLConnection for URL
	        URL url = new URL(urlname);
	        URLConnection conn = url.openConnection();
	     
	        // List all the response headers from the server. 
	        for (int i=0; ; i++) {
	            String hname = conn.getHeaderFieldKey(i);
	            String hvalue = conn.getHeaderField(i);
	           
	            getLog().debug("hname="+hname+"::"+"value="+hvalue);
	            if (hname == null && hvalue == null) {
	                // No more headers
	                break;
	            }
	            if (hname == null) {
	            	   getLog().debug("Response from Apache="+hvalue);
	                // The header value contains the server's HTTP version
	            	   if( hvalue.indexOf("200") < 0 && hvalue.indexOf("301") < 0
	            	   		&& hvalue.indexOf("302") < 0) 
	            	   		fail(urlname+" Down");
	            	   break;
	            }     
	        }
	    } catch (Exception e) {
	    	 	getLog().debug(e); 
	    }
	}

	/**
	 * This method gets the response from the HTTP Server provided an URl
	 * @param urlname
	 */
     private void getHttpText( String urlname ){
        getLog().debug( getAttribute(urlname));
    }//end method
     
     /**
      * Returns the attribute set on the session
      * Refer to getattribute.jsp
      * @param urlname
      * @return
      */
    private String getAttribute( String urlname){
     	BufferedReader in = null;
        StringBuffer sb = new StringBuffer();
        try{
        		URL url = new URL(urlname);
            
             //Read all the text returned by the server 
             in = new BufferedReader(new InputStreamReader(url.openStream()));                    
        	String str; 
        	while ((str = in.readLine()) != null) {  
           	// str is one line of text; readLine() strips the newline character(s)  
        		sb.append(str);
        }   
        	getLog().debug(sb.toString());
        }catch( Exception e){
           getLog().debug( e);
        }finally{
             try{
                 in.close();
             }catch(Exception y){}
        }
        return sb.toString();
     }
     
     /**
      * Makes a http call to the jsp that retrieves the attribute stored on the 
      * session. When the attribute values mathes with the one retrieved earlier,
      * we have HttpSessionReplication.
      * Makes use of commons-httpclient library of Apache
     * @param client
     * @param method
     * @return session attribute
     * @throws IOException
     */
    private String makeGet( HttpClient client, HttpMethod method) throws IOException{
       //   	 Execute the method.
       int statusCode = -1;
     
        try {
           // execute the method.
           statusCode = client.executeMethod(method);
         } catch (HttpRecoverableException e) {
           System.err.println(
             "A recoverable exception occurred, retrying." + 
             e.getMessage());
         } catch (IOException e) {
           System.err.println("Failed to download file.");
           e.printStackTrace();
           System.exit(-1);
         }

       // Read the response body.
       byte[] responseBody = method.getResponseBody();

       // Release the connection.
       method.releaseConnection();

       // Deal with the response.
       // Use caution: ensure correct character encoding and is not binary data
       return new String(responseBody);
   }
    
    /* 
     * Override the method and do nothing. It fails when we run this testcase
     * because we have brought down instances. 
     * @see org.jboss.test.JBossTestCase#testServerFound()
     */
    public void testServerFound() throws Exception
	{ 
	}

}
