<%@page contentType="text/html"%>
<%
  // We really don't care about jsps in this test; this jsp is just here
  // to meet the demands of test superclass that wants to invoke it
  org.jboss.test.classloader.leak.clstore.ClassLoaderStore.getInstance().storeClassLoader("JSP", getClass().getClassLoader());
  org.jboss.test.classloader.leak.clstore.ClassLoaderStore.getInstance().storeClassLoader("JSP_TCCL", Thread.currentThread().getContextClassLoader());

%>
WEBAPP