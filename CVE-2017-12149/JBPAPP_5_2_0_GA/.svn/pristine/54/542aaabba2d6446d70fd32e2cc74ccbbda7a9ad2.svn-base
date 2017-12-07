<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
%>

<% 
   String str = session.getQueryString();
   int MAX = 0;
   if(str == null) {
      throw new RuntimeException("modifyPerfSession: invalid query string");
   }
   long time = System.currentTimeMillis();
   Random rand = new Radnom(time);
   MAX = Integer.parseInt(str);
   i = rand.nextInt(MAX);

   // Note: The name are hard-coded in the test case as well!!!
   String id = String.valueOf(i);
   Person ben = (Person)session.getAttribute(id);
   ben.setAge(i+1);
%>
