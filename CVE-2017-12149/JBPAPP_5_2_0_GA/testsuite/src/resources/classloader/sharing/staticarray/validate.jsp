<%@page contentType="text/html"%>
<%@page import="java.util.Arrays"%>
<%@page import="org.jboss.test.util.Debug"%>

<%
   Integer[] array = (Integer[]) request.getAttribute("Sequencer.info");
   if(array == null)
      throw new IllegalStateException("No Sequencer.info attribute found");
   String expectedString = request.getParameter("Sequencer.info.expected");
   if(expectedString == null)
      throw new IllegalStateException("No Sequencer.info.expected parameter found");
   String[] expectedValues = expectedString.split(",");
   int[] expected = new int[array.length];
   StringBuffer check = new StringBuffer("Array mismatches: ");
   boolean matches = true;
   for(int n = 0; n < array.length; n ++)
   {
      expected[n] = Integer.parseInt(expectedValues[n]);
      if(array[n] != expected[n])
      {
         check.append("["+n+"] "+array[n]+"!="+expected[n]);
         check.append('\n');
         matches = false;
      }
   }
   if(matches == false)
   {
      StringBuffer classInfo = new StringBuffer();
      Debug.displayClassInfo(org.jboss.test.classloader.sharing.staticarray.common.Sequencer.class, classInfo);
      System.err.println(classInfo.toString());
      response.addHeader("X-Error", check.toString());
      response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, check.toString());
   }
%>
<h1>Sequencer.info Comparision</h1>
Sequencer.info = <%= Arrays.asList(array) %><br>
Sequencer.info.expected = <%= Arrays.asList(expected) %><br>
<pre>
<%= check %>
</pre>
