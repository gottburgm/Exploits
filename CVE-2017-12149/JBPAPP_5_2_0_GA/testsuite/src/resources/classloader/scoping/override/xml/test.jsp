<%@ page language="java" %>
<%@ page import="org.apache.xerces.parsers.SAXParser,
                 org.apache.xerces.framework.XMLParser,
                 org.xml.sax.InputSource,
                 java.io.StringReader,
                 org.jboss.mx.loading.ClassLoaderUtils" %>

<html>
<head>
	<title>Use Case Testing Legacy XML Parser</title>
</head>

<!-- A jsp page that access classes in a legacy version of the xerces xml
parser to test override the xml parser used by the JBoss core layer

@version: $Revision: 18787 $
@author: Scott.Stark@jboss.org
@author: BCompton@watlow.com
-->
<body bgcolor="ffffff" text="000000" link="004488" vlink="800080" alink="df0029"
topmargin="0" leftmargin="0" bottommargin="0" marginwidth="0" marginheight="0" >


This web application is used to test if isolation of JBOSS xerces version is succesfull.<br>
The jboss-web.xml file has java2ClassLoadingCompliance=false
<%
	// run test
    try
    {
      // Create the legacy parser instance
      Object parser = new org.apache.xerces.parsers.SAXParser();
      StringBuffer info = new StringBuffer("Legacy SAXParser info: ");
      ClassLoaderUtils.displayClassInfo(parser.getClass(), info);
      out.println("<pre>");
      out.println(info);
      out.println("</pre>");
      if (parser instanceof org.apache.xerces.framework.XMLParser)
      {
         out.println("Test Successfull!!");
         out.println("<BR><BR> Was able to create a instance of SAXParser that extends XMLParser (this class doesn't exist in Jboss version of Xerces). class="+ parser.getClass());
         out.println("Test Successfull!!");
      }
      else
      {
         out.println("Test failed. The SAXParser that was created wasn't a subclass of XMLParser.  This means that the Isolation from Jboss's newer xerces parser was not succesfull (newer SAXParser class doesn't extend XMLParser).");
         throw new ServletException("SAXParser was not a org.apache.xerces.framework.XMLParser, "+info);
      }
    }
    catch (java.lang.VerifyError e)
    {
        out.println("Test failed. VerifyError has occured. Isolation from Jboss's newer xerces parser was not succesfull. Exception = "+ e);
        throw new ServletException("Test failed with VerifyError", e);
    }
    catch (Throwable e)
    {
      out.println("Test failed. Not sure why! Error = "+ e);
       throw new ServletException("Test failed with VerifyError", e);       
    }
%>

</body>
</html>
