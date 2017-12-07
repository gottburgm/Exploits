<%@page contentType="text/html"
   import="java.util.*"
   import="javax.servlet.ServletContext"
   import="org.jboss.test.cluster.web.aop.Person"
   import="org.jboss.test.cluster.web.aop.Address"
   import="org.jboss.test.cluster.web.DeserializationSensor"
%>

<%
   DeserializationSensor sensor = (DeserializationSensor) session.getAttribute("TEST_DESERIALIZATION");
   response.setHeader("X-SessionDeserialzied", sensor.isDeserialized() ? "true" : "false");
   
   Person joe = (Person)session.getAttribute("TEST_PERSON");
   Address addr = (Address)joe.getAddress();
%>

<%=joe.getName() %>
<%=addr.getZip() %>
<%=joe.getLanguages() %>
