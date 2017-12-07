<%@page contentType="text/html"
   import="java.util.*"
   import="javax.servlet.ServletContext"
   import="org.jboss.test.cluster.web.aop.Person"
   import="org.jboss.test.cluster.web.aop.Address"
   import="org.jboss.test.cluster.web.DeserializationSensor"
%>

<%
   String isNew = session.isNew() ? "true" : "false";
   response.setHeader("X-SessionIsNew", isNew);
   DeserializationSensor sensor = (DeserializationSensor) session.getAttribute("TEST_DESER");
   boolean deserialized = (sensor != null && sensor.isDeserialized());
   response.setHeader("X-SessionDeserialzied", deserialized ? "true" : "false");
   
   Person joe = (Person)session.getAttribute("TEST_PERSON");
   Address addr = (Address)joe.getAddress();
   
   // Bind joe to the servlet context as well so it can be
   // accessed later without involving the session
   ServletContext ctx = getServletConfig().getServletContext();
   ctx.setAttribute("TEST_PERSON", joe);
%>

<%=joe.getName() %>
<%=addr.getZip() %>
<%=joe.getLanguages() %>
<%=joe.getFavoriteColors() %>
