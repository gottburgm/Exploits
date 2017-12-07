<%= javax.xml.datatype.DatatypeFactory.newInstance().getClass().getName() + "|" + javax.xml.parsers.DocumentBuilderFactory.newInstance().getClass().getName() + "|" + javax.xml.parsers.SAXParserFactory.newInstance().getClass().getName() + "|" + new org.apache.xerces.impl.Version().getVersion() + "|" + (new org.apache.xerces.impl.Version().getClass().getClassLoader() != null) %>

