<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="org.apache.commons.logging.Log" %>

<%
final String PROPERTY = "org.apache.commons.logging.use_tccl";
String use_tccl = System.getProperty(PROPERTY);

Log log = LogFactory.getLog("JBPAPP-6523");
log.info("Successfully initialized commons logging...");
log.info(PROPERTY + "'s value: " + use_tccl);
%>

<html>
   <body>
      <strong>JBPAPP-6523 Test Web Application</strong>

      <p>This text should only show when the system property
      org.apache.commons.logging.use_tccl is set to false.</p>

      <p>The default value for this property is true.</p>

      <p>When set to true, this JSP should not compile.</p>

      <p><%= PROPERTY %>'s value: <%= use_tccl %></p>
   </body>
</html>