<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
   import="org.jboss.test.cluster.web.DeserializationSensor"
%>
<%
   String isNew = session.isNew() ? "true" : "false";
   response.setHeader("X-SessionIsNew", isNew);
   DeserializationSensor sensor = (DeserializationSensor) session.getAttribute("TEST_DESER");
   boolean deserialized = (sensor != null && sensor.isDeserialized());
   response.setHeader("X-SessionDeserialzied", deserialized ? "true" : "false");
%>
<%=((Person)session.getAttribute("TEST_PERSON")).getName() %>
