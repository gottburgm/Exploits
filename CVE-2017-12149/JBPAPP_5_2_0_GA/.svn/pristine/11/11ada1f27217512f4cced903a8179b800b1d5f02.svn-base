<%
   // Use a custom header to indicate if the session attribute was seen
   String TEST_HTTP = (String) session.getAttribute("TEST_HTTP");
   String flag = TEST_HTTP != null ? "true" : "false";
   response.setHeader("X-SawTestHttpAttribute", flag);
   String isNew = session.isNew() ? "true" : "false";
   response.setHeader("X-SessionIsNew", isNew);
%>
<h2>Server info : <%=application.getServerInfo()%>:<%=request.getServerPort()%></h2>

<p>Retrieve the session id from attribute:
<%= TEST_HTTP %></p>
