<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="javax.jms.Topic"%>
<%@page import="java.net.URL"%>
<%@page import="javax.mail.Session"%>
<%@page import="javax.naming.NamingException"%>
<%@page import="javax.naming.InitialContext"%>
<%@page import="javax.naming.Context"%>
<%@page import="org.jboss.test.web.mock.EntityHome"%>
<%@page import="org.jboss.test.web.mock.StatelessSessionLocalHome"%>
<%@page import="org.jboss.test.web.mock.StatelessSessionHome"%>
<%@page import="javax.ejb.EJB"%>
<%@page import="javax.sql.DataSource"%>
<%@page import="javax.jms.Queue"%>
<%@page import="javax.jms.QueueConnectionFactory"%>
<%@page import="javax.annotation.Resource"%>

<%!
@Resource(name="jms/QueFactory", mappedName="java:/ConnectionFactory")
QueueConnectionFactory queueFactory;
@Resource(name="TestQueue", mappedName="MockQueueB")
Queue testQueue;
@Resource(name="mdr/ConsumesProducesJNDIName", mappedName="MockQueueA")
Queue refQueue;
@Resource(name="mail/DefaultMail", type=javax.mail.Session.class, mappedName="java:/Mail")
Session session;
@Resource(name="mdr/ConsumesLink", type=javax.jms.Queue.class, mappedName="MockQueueA")
Queue consumesLink;
@Resource(name="mdr/ProducesLink", type=javax.jms.Topic.class, mappedName="MockTopicA")
Topic producesLink;
@Resource(name="jdbc/DefaultDS", mappedName="java:/MockDS")
DataSource ds;
@EJB(name="ejb/bean3", beanInterface=StatelessSessionHome.class, 
      mappedName="jbosstest/ejbs/UnsecuredEJB")
StatelessSessionHome sshome;
@EJB(name="ejb/CtsBmp", beanInterface=EntityHome.class, 
      mappedName="jbosstest/ejbs/CtsBmp")
EntityHome entityHome;
@EJB(name="ejb/local/bean3", beanInterface=StatelessSessionLocalHome.class, 
      mappedName="jbosstest/ejbs/local/ENCBean1")
StatelessSessionLocalHome localHome;

@Resource(name="url/JBossHome", mappedName="http://www.jboss.org")
java.net.URL url;

@Resource(name="Ints/i0", mappedName="0")
Integer i0;
@Resource(name="Ints/i1", mappedName="1")
Integer i1;
@Resource(name="Floats/f0", mappedName="0.0")
Float f0;
@Resource(name="Floats/f1", mappedName="1.1")
Float f1;
@Resource(name="Strings/s0", mappedName="String0")
String s0;
@Resource(name="Strings/s1", mappedName="String1")
String s1;
@Resource(name="ejb/catalog/CatalogDAOClass", mappedName="com.sun.model.dao.CatalogDAOImpl")
String ejbName;
%>

<%
Context initCtx = new InitialContext();
Context myEnv = (Context) initCtx.lookup("java:comp/env");
// Test basic env values
Integer i = (Integer) myEnv.lookup("Ints/i0");
i = (Integer) initCtx.lookup("java:comp/env/Ints/i1");
Float f = (Float) myEnv.lookup("Floats/f0");
f = (Float) initCtx.lookup("java:comp/env/Floats/f1");
String s = (String) myEnv.lookup("Strings/s0");
s = (String) initCtx.lookup("java:comp/env/Strings/s1");
s = (String) initCtx.lookup("java:comp/env/ejb/catalog/CatalogDAOClass");

// do lookup on bean specified without ejb-link
Object ejb = initCtx.lookup("java:comp/env/ejb/bean3");
if ((ejb instanceof StatelessSessionHome) == false)
   throw new NamingException("ejb/bean3 is not a StatelessSessionHome");


ejb = initCtx.lookup("java:comp/env/ejb/CtsBmp");
if ((ejb instanceof EntityHome) == false)
   throw new NamingException("ejb/CtsBmp is not a EntityHome");

//lookup of local-ejb-ref bean specified without ejb-link
ejb = initCtx.lookup("java:comp/env/ejb/local/bean3");
if ((ejb instanceof StatelessSessionLocalHome) == false)
   throw new NamingException("ejb/local/bean3 is not a StatelessSessionLocalHome");

// JDBC DataSource
DataSource ds = (DataSource) myEnv.lookup("jdbc/DefaultDS");

// JavaMail Session
Session testSession = (Session) myEnv.lookup("mail/DefaultMail");

// QueueConnectionFactory
QueueConnectionFactory qf = (QueueConnectionFactory) myEnv.lookup("jms/QueFactory");

// URLs
URL home1 = (URL) myEnv.lookup("url/JBossHome");
URL home2 = (URL) initCtx.lookup("java:comp/env/url/JBossHome");
if( home1.equals(home2) == false )
   throw new NamingException("url/JBossHome != java:comp/env/url/JBossHome");

Object obj = myEnv.lookup("mdr/ConsumesLink");
if ((obj instanceof Queue) == false)
   throw new RuntimeException("mdr/ConsumesLink is not a javax.jms.Queue");
Queue queue = (Queue) obj;
if ("QUEUE.testQueue".equals(queue.getQueueName()))
   throw new RuntimeException("Excepted QUEUE.testQueue, got " + queue);

obj = myEnv.lookup("mdr/ProducesLink");
if ((obj instanceof Topic) == false)
   throw new RuntimeException("mdr/ProducesLink is not a javax.jms.Topic");
Topic topic = (Topic) obj;
if ("TOPIC.testTopic".equals(topic.getTopicName()))
   throw new RuntimeException("Excepted TOPIC.testTopic got " + topic);

obj = myEnv.lookup("mdr/ConsumesProducesJNDIName");
if ((obj instanceof Queue) == false)
   throw new RuntimeException("mdr/ConsumesProducesJNDIName is not a javax.jms.Queue");
queue = (Queue) obj;
if ("QUEUE.A".equals(queue.getQueueName()))
   throw new RuntimeException("Excepted QUEUE.A, got " + queue);
%>

test were okay.