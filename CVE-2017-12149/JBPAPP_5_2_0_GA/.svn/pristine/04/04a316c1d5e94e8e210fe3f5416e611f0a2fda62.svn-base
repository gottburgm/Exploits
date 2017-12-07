<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.aop.Person"
   import="org.jboss.test.cluster.web.aop.Address"
%>

<% 
   // Note: The name are hard-coded in the test case as well!!!
   // POJO modify no need to do setAttribute again!
   Person ben = (Person)session.getAttribute("TEST_PERSON");
   ben.setName("Joe");
   ben.setAge(60);
   ben.getAddress().setZip(94086);

   List lang = new ArrayList();
   lang.add("English");
   lang.add("Holo");
   ben.setLanguages(lang);
   ben.addFavoriteColor("Red");
%>
