<%--
/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
--%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<HTML>

<f:view>  
   <h:form id="form1">    
      
      <h:outputText value="Enter your name:" rendered="#{empty foo.text}" id="prompt"/>
      <h:outputText value="Hello #{foo.text}" rendered="#{!empty foo.text}" id="greeting"/><br/>
      
      <h:inputText value="#{foo.text}" id="input_foo_text">          
        <f:validateLength minimum="2"/>
      </h:inputText>
      <h:message for="input_foo_text" styleClass="errorMessage"/>
      <br/>
      <h:commandButton value="Submit" action="/index.jsp" id="submit_button"/>
      <h:commandButton value="Goodbye" action="/finalgreeting.jsp" id="goodbye_button"/>

   </h:form>
</f:view>

</HTML>
