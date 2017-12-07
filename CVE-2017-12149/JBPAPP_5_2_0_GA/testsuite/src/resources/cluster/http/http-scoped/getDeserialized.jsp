<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
   import="org.jboss.test.cluster.web.DeserializationSensor"
%>
<%
   DeserializationSensor sensor = (DeserializationSensor) session.getAttribute("TEST_DESERIALIZATION");
   response.setHeader("X-SessionDeserialzied", sensor.isDeserialized() ? "true" : "false");
%>
<%=((Person)session.getAttribute("TEST_PERSON")).getName() %>
