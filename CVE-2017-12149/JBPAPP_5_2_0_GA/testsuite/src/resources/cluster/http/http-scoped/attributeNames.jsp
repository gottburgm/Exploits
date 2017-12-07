<%@page contentType="text/html"
   import="java.util.*"
%>

<% 
   Enumeration names = session.getAttributeNames();
   String list = "";
   while (names.hasMoreElements())
   {
   	//list.concat((String) names.nextElement());
%>
<%= names.nextElement() %>
<%
   }
%>

