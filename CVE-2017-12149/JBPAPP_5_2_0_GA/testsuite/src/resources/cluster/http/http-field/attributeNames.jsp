<%@page contentType="text/html"
   import="java.util.*"
%>

<% 
   Enumeration names = session.getAttributeNames();
   while (names.hasMoreElements())
   {
%>

	<%= names.nextElement() %>

<%
   }
%>
