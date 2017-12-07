<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
%>

<html>
<center>
<% 
   String str = session.getQueryString();
   int MAX = 0;
   if(str == null) {
       throw new RuntimeException("setPerfSession: invalid query string.");
   }
   MAX = Integer.parseInt(str);

   // Note: Don't change these. They are hard-coded in the test case as well!!
   String id=request.getSession().getId();
   session.setAttribute("TEST_ID",id); 
   // Set up session for perf test. Idea is to have a big hashmap per
   // session attribute.
   for(int i=0; i < MAX; i++) {
      Person ben=new Person("Let's make the person object a little bit bigger",
        i);
      String id = String.valueOf(i);
      session.setAttribute(i, ben);
   }
%>
<%=id%>

<h1><%=application.getServerInfo()%>:<%=request.getServerPort()%></h1>
</body>
</html>
