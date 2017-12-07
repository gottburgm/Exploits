/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.jboss.web.tomcat.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.manager.Constants;
import org.apache.catalina.util.RequestUtil;

/**
 * This is a refactoring of the servlet to externalize
 * the output into a simple class. Although we could
 * use XSLT, that is unnecessarily complex.
 *
 * @author Peter Lin
 * @version $Revision$ $Date$
 */

public class StatusTransformer {


    // --------------------------------------------------------- Public Methods


    public static void setContentType(HttpServletResponse response, 
                                      int mode) {
        if (mode == 0){
            response.setContentType("text/html;charset="+Constants.CHARSET);
        } else if (mode == 1){
            response.setContentType("text/xml;charset="+Constants.CHARSET);
        }
    }


    /**
     * Process a GET request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    public static void writeHeader(PrintWriter writer, Object[] args, int mode) {
        if (mode == 0){
            // HTML Header Section
            writer.print(MessageFormat.format
                    (Constants.HTML_HEADER_SECTION, args));
        } else if (mode == 1){
            writer.write(Constants.XML_DECLARATION);
            writer.write
                (Constants.XML_STYLE);
            writer.write("<status>");
        }
    }


    /**
     * Write the header body. XML output doesn't bother
     * to output this stuff, since it's just title.
     * 
     * @param writer The output writer
     * @param args What to write
     * @param mode 0 means write 
     */
    public static void writeBody(PrintWriter writer, Object[] args, int mode) {
        if (mode == 0){
            writer.print(MessageFormat.format
                    (Constants.BODY_HEADER_SECTION, args));
        }
    }


    /**
     * Write the manager webapp information.
     * 
     * @param writer The output writer
     * @param args What to write
     * @param mode 0 means write
     */
    public static void writeManager1(PrintWriter writer, Object[] args, 
                                    int mode) {
        if (mode == 0){
            writer.print(MessageFormat.format(Constants.MANAGER_STATUS_SECTION1, args));
        }
    }


    /**
     * Write the manager webapp information.
     * 
     * @param writer The output writer
     * @param args What to write
     * @param mode 0 means write
     */
    public static void writeManager2(PrintWriter writer, Object[] args, 
                                    int mode) {
        if (mode == 0){
            writer.print(MessageFormat.format(Constants.MANAGER_STATUS_SECTION2, args));
        }
    }


    public static void writePageHeading(PrintWriter writer, Object[] args, 
                                        int mode) {
        if (mode == 0){
            writer.print(MessageFormat.format
                         (Constants.SERVER_HEADER_SECTION, args));
        }
    }


    public static void writeServerInfo(PrintWriter writer, Object[] args, 
                                       int mode){
        if (mode == 0){
            writer.print(MessageFormat.format(Constants.SERVER_ROW_SECTION, args));
        }
    }


    /**
     * 
     */
    public static void writeFooter(PrintWriter writer, int mode) {
        if (mode == 0){
            // HTML Tail Section
            writer.print(Constants.HTML_TAIL_SECTION);
        } else if (mode == 1){
            writer.write("</status>");
        }
    }


    /**
     * Write the OS state. Mode 0 will generate HTML.
     * Mode 1 will generate XML.
     */
    public static void writeOSState(PrintWriter writer, int mode) {
        long[] result = new long[16];
        boolean ok = false;
        try {
            String methodName = "info";
            Class paramTypes[] = new Class[1];
            paramTypes[0] = result.getClass();
            Object paramValues[] = new Object[1];
            paramValues[0] = result;
            Method method = Class.forName("org.apache.tomcat.jni.OS")
                .getMethod(methodName, paramTypes);
            method.invoke(null, paramValues);
            ok = true;
        } catch (Throwable t) {
            // Ignore
        }
        
        if (ok) {
            if (mode == 0){
                writer.print("<h3>OS</h3>");

                writer.print("<p>");
                writer.print(" <strong>Physical memory:</strong> ");
                writer.print(formatSize(new Long(result[0]), true));
                writer.print("<br><strong>Available memory:</strong> ");
                writer.print(formatSize(new Long(result[1]), true));
                writer.print("<br><strong>Total page file:</strong> ");
                writer.print(formatSize(new Long(result[2]), true));
                writer.print("<br><strong>Free page file:</strong> ");
                writer.print(formatSize(new Long(result[3]), true));
                writer.print("<br><strong>Memory load:</strong> ");
                writer.print(new Long(result[6]));
                writer.print("<br><strong>Process kernel time:</strong> ");
                writer.print(formatTime(new Long(result[11] / 1000), true));
                writer.print("<br><strong>Process user time:</strong> ");
                writer.print(formatTime(new Long(result[12] / 1000), true));
                writer.print("</p>");
            } else if (mode == 1){
            }
        }
        
    }
    
    
    /**
     * Write the VM state. Mode 0 will generate HTML.
     * Mode 1 will generate XML.
     */
    public static void writeVMState(PrintWriter writer, int mode)
        throws Exception {

        if (mode == 0){
            writer.print("<h3>JVM</h3>");

            writer.print("<p>");
            writer.print(" <strong>Free memory:</strong> ");
            writer.print(formatSize
                         (new Long(Runtime.getRuntime().freeMemory()), true));
            writer.print(" <br><strong>Total memory:</strong> ");
            writer.print(formatSize
                         (new Long(Runtime.getRuntime().totalMemory()), true));
            writer.print(" <br><strong>Max memory:</strong> ");
            writer.print(formatSize
                         (new Long(Runtime.getRuntime().maxMemory()), true));
            writer.print("</p>");
        } else if (mode == 1){
            writer.write("<jvm>");

            writer.write("<memory");
            writer.write(" free='" + Runtime.getRuntime().freeMemory() + "'");
            writer.write(" total='" + Runtime.getRuntime().totalMemory() + "'");
            writer.write(" max='" + Runtime.getRuntime().maxMemory() + "'/>");

            writer.write("</jvm>");
        }

    }


    /**
     * Write connector state.
     */
    public static void writeConnectorState(PrintWriter writer, 
                                           ObjectName tpName, String name,
                                           MBeanServer mBeanServer,
                                           Vector globalRequestProcessors,
                                           Vector requestProcessors, int mode)
        throws Exception {

        if (mode == 0) {
            writer.print("<h3>");
            writer.print(name);
            writer.print("</h3>");

            writer.print("<p>");
            writer.print(" <strong>Max threads:</strong> ");
            writer.print(getAttribute(mBeanServer, tpName, "maxThreads"));
            writer.print(" <br><strong>Current thread count:</strong> ");
            writer.print(getAttribute(mBeanServer, tpName, "currentThreadCount"));
            writer.print(" <br><strong>Current thread busy:</strong> ");
            writer.print(getAttribute(mBeanServer, tpName, "currentThreadsBusy"));
            try {
                Object value = getAttribute(mBeanServer, tpName, "keepAliveCount");
                writer.print(" <br><strong>Keeped alive sockets count:</strong> ");
                writer.print(value);
            } catch (Exception e) {
                // Ignore
            }
            
            ObjectName grpName = null;

            Enumeration enumeration = globalRequestProcessors.elements();
            while (enumeration.hasMoreElements()) {
                ObjectName objectName = (ObjectName) enumeration.nextElement();
                if (name.equals(objectName.getKeyProperty("name"))) {
                    grpName = objectName;
                }
            }

            if (grpName == null) {
                return;
            }

            writer.print(" <br><strong>Max processing time:</strong> ");
            writer.print(formatTime(getAttribute(mBeanServer, grpName, "maxTime"), false));
            writer.print(" <br><strong>Processing time:</strong> ");
            writer.print(formatTime(getAttribute(mBeanServer, grpName, "processingTime"), true));
            writer.print(" <br><strong>Request count:</strong> ");
            writer.print(getAttribute(mBeanServer, grpName, "requestCount"));
            writer.print(" <br><strong>Error count:</strong> ");
            writer.print(getAttribute(mBeanServer, grpName, "errorCount"));
            writer.print(" <br><strong>Bytes received:</strong> ");
            writer.print(formatSize(getAttribute(mBeanServer, grpName, "bytesReceived"), true));
            writer.print(" <br><strong>Bytes sent:</strong> ");
            writer.print(formatSize(getAttribute(mBeanServer, grpName, "bytesSent"), true));
            writer.print("</p>");

            writer.print("<table width=\"100%\" cellspacing=\"0\" class=\"tableStyle\">");
            writer.print("<thead><th colspan=\"7\">");
            writer.print(name);
            writer.print("</th></thead>");
            writer.print("<tr class=\"UnsortableTableHeader\"><td>Stage</td><td>Time</td><td>B Sent</td><td>B Received</td><td>Client</td><td>V. Host</td><td>Request</td></tr><tbody>");

            enumeration = requestProcessors.elements();
            boolean isHighlighted = false;
            String highlightStyle = null;
            while (enumeration.hasMoreElements()) {
                isHighlighted = !isHighlighted;
                if(isHighlighted) {
                    highlightStyle = "oddRow";
                } else {
                    highlightStyle = "evenRow";
                }
                ObjectName objectName = (ObjectName) enumeration.nextElement();
                if (name.equals(objectName.getKeyProperty("worker"))) {
                    writer.print("<tr class=\"");
                    writer.print(highlightStyle);
                    writer.print("\">");
                    writeProcessorState(writer, objectName, mBeanServer, mode);
                    writer.print("</tr>");
                }
            }

            writer.print("<caption align=\"bottom\">P: Parse and prepare request S: Service F: Finishing R: Ready K: Keepalive</caption></tbody></table>");

        } else if (mode == 1){
            writer.write("<connector name='" + name + "'>");

            writer.write("<threadInfo ");
            writer.write(" maxThreads=\"" + getAttribute(mBeanServer, tpName, "maxThreads") + "\"");
            writer.write(" currentThreadCount=\"" + getAttribute(mBeanServer, tpName, "currentThreadCount") + "\"");
            writer.write(" currentThreadsBusy=\"" + getAttribute(mBeanServer, tpName, "currentThreadsBusy") + "\"");
            writer.write(" />");

            ObjectName grpName = null;

            Enumeration enumeration = globalRequestProcessors.elements();
            while (enumeration.hasMoreElements()) {
                ObjectName objectName = (ObjectName) enumeration.nextElement();
                if (name.equals(objectName.getKeyProperty("name"))) {
                    grpName = objectName;
                }
            }

            if (grpName != null) {

                writer.write("<requestInfo ");
                writer.write(" maxTime=\"" + getAttribute(mBeanServer, grpName, "maxTime") + "\"");
                writer.write(" processingTime=\"" + getAttribute(mBeanServer, grpName, "processingTime") + "\"");
                writer.write(" requestCount=\"" + getAttribute(mBeanServer, grpName, "requestCount") + "\"");
                writer.write(" errorCount=\"" + getAttribute(mBeanServer, grpName, "errorCount") + "\"");
                writer.write(" bytesReceived=\"" + getAttribute(mBeanServer, grpName, "bytesReceived") + "\"");
                writer.write(" bytesSent=\"" + getAttribute(mBeanServer, grpName, "bytesSent") + "\"");
                writer.write(" />");

                writer.write("<workers>");
                enumeration = requestProcessors.elements();
                while (enumeration.hasMoreElements()) {
                    ObjectName objectName = (ObjectName) enumeration.nextElement();
                    if (name.equals(objectName.getKeyProperty("worker"))) {
                        writeProcessorState(writer, objectName, mBeanServer, mode);
                    }
                }
                writer.write("</workers>");
            }

            writer.write("</connector>");
        }

    }


    /**
     * Write processor state.
     */
    protected static void writeProcessorState(PrintWriter writer, 
                                              ObjectName pName,
                                              MBeanServer mBeanServer, 
                                              int mode)
        throws Exception {

        Integer stageValue = 
            (Integer) getAttribute(mBeanServer, pName, "stage");
        int stage = stageValue.intValue();
        boolean fullStatus = true;
        boolean showRequest = true;
        String stageStr = null;

        switch (stage) {

        case (1/*org.apache.coyote.Constants.STAGE_PARSE*/):
            stageStr = "P";
            fullStatus = false;
            break;
        case (2/*org.apache.coyote.Constants.STAGE_PREPARE*/):
            stageStr = "P";
            fullStatus = false;
            break;
        case (3/*org.apache.coyote.Constants.STAGE_SERVICE*/):
            stageStr = "S";
            break;
        case (4/*org.apache.coyote.Constants.STAGE_ENDINPUT*/):
            stageStr = "F";
            break;
        case (5/*org.apache.coyote.Constants.STAGE_ENDOUTPUT*/):
            stageStr = "F";
            break;
        case (7/*org.apache.coyote.Constants.STAGE_ENDED*/):
            stageStr = "R";
            fullStatus = false;
            break;
        case (6/*org.apache.coyote.Constants.STAGE_KEEPALIVE*/):
            stageStr = "K";
            fullStatus = true;
            showRequest = false;
            break;
        case (0/*org.apache.coyote.Constants.STAGE_NEW*/):
            stageStr = "R";
            fullStatus = false;
            break;
        default:
            // Unknown stage
            stageStr = "?";
            fullStatus = false;

        }

        if (mode == 0) {
            writer.write("<td class=\"first\"><strong>");
            writer.write(stageStr);
            writer.write("</strong></td>");

            if (fullStatus) {
                writer.write("<td>");
                writer.print(formatTime(getAttribute(mBeanServer, pName, "requestProcessingTime"), false));
                writer.write("</td>");
                writer.write("<td>");
                if (showRequest) {
                    writer.print(formatSize(getAttribute(mBeanServer, pName, "requestBytesSent"), false));
                } else {
                    writer.write("?");
                }
                writer.write("</td>");
                writer.write("<td>");
                if (showRequest) {
                    writer.print(formatSize(getAttribute(mBeanServer, pName, "requestBytesReceived"), 
                                            false));
                } else {
                    writer.write("?");
                }
                writer.write("</td>");
                writer.write("<td>");
                writer.print(filter(getAttribute(mBeanServer, pName, "remoteAddr")));
                writer.write("</td>");
                writer.write("<td nowrap>");
                writer.write(filter(getAttribute(mBeanServer, pName, "virtualHost")));
                writer.write("</td>");
                writer.write("<td nowrap>");
                if (showRequest) {
                    writer.write(filter(getAttribute(mBeanServer, pName, "method")));
                    writer.write(" ");
                    writer.write(filter(getAttribute(mBeanServer, pName, "currentUri")));
                    String queryString = (String) getAttribute(mBeanServer, pName, "currentQueryString");
                    if ((queryString != null) && (!queryString.equals(""))) {
                        writer.write("?");
                        writer.print(RequestUtil.filter(queryString));
                    }
                    writer.write(" ");
                    writer.write(filter(getAttribute(mBeanServer, pName, "protocol")));
                } else {
                    writer.write("?");
                }
                writer.write("</td>");
            } else {
                writer.write("<td>?</td><td>?</td><td>?</td><td>?</td><td>?</td><td>?</td>");
            }
        } else if (mode == 1){
            writer.write("<worker ");
            writer.write(" stage=\"" + stageStr + "\"");

            if (fullStatus) {
                writer.write(" requestProcessingTime=\"" 
                             + getAttribute(mBeanServer, pName, "requestProcessingTime") + "\"");
                writer.write(" requestBytesSent=\"");
                if (showRequest) {
                    writer.write("" + getAttribute(mBeanServer, pName, "requestBytesSent"));
                } else {
                    writer.write("0");
                }
                writer.write("\"");
                writer.write(" requestBytesReceived=\"");
                if (showRequest) {
                    writer.write("" + getAttribute(mBeanServer, pName, "requestBytesReceived"));
                } else {
                    writer.write("0");
                }
                writer.write("\"");
                writer.write(" remoteAddr=\"" 
                             + filter(getAttribute(mBeanServer, pName, "remoteAddr")) + "\"");
                writer.write(" virtualHost=\"" 
                             + filter(getAttribute(mBeanServer, pName, "virtualHost")) + "\"");

                if (showRequest) {
                    writer.write(" method=\"" 
                                 + filter(getAttribute(mBeanServer, pName, "method")) + "\"");
                    writer.write(" currentUri=\"" 
                                 + filter(getAttribute(mBeanServer, pName, "currentUri")) + "\"");

                    String queryString = (String) getAttribute(mBeanServer, pName, "currentQueryString");
                    if ((queryString != null) && (!queryString.equals(""))) {
                        writer.write(" currentQueryString=\"" 
                                     + RequestUtil.filter(queryString) + "\"");
                    } else {
                        writer.write(" currentQueryString=\"&#63;\"");
                    }
                    writer.write(" protocol=\"" 
                                 + filter(getAttribute(mBeanServer, pName, "protocol")) + "\"");
                } else {
                    writer.write(" method=\"&#63;\"");
                    writer.write(" currentUri=\"&#63;\"");
                    writer.write(" currentQueryString=\"&#63;\"");
                    writer.write(" protocol=\"&#63;\"");
                }
            } else {
                writer.write(" requestProcessingTime=\"0\"");
                writer.write(" requestBytesSent=\"0\"");
                writer.write(" requestBytesRecieved=\"0\"");
                writer.write(" remoteAddr=\"&#63;\"");
                writer.write(" virtualHost=\"&#63;\"");
                writer.write(" method=\"&#63;\"");
                writer.write(" currentUri=\"&#63;\"");
                writer.write(" currentQueryString=\"&#63;\"");
                writer.write(" protocol=\"&#63;\"");
            }
            writer.write(" />");
        }

    }


    /**
     * Write applications state.
     */
    public static void writeDetailedState(PrintWriter writer,
                                          MBeanServer mBeanServer, int mode)
        throws Exception {

        if (mode == 0){
            ObjectName queryHosts = new ObjectName("*:j2eeType=WebModule,*");
            Set hostsON = mBeanServer.queryNames(queryHosts, null);

            // Webapp list
            int count = 0;
            Iterator iterator = hostsON.iterator();
            while (iterator.hasNext()) {
                ObjectName contextON = (ObjectName) iterator.next();
                writer.print("<a name=\"" + (count++) + ".0\">");
                writeContext(writer, contextON, mBeanServer, mode);
            }

        } else if (mode == 1){
            // for now we don't write out the Detailed state in XML
        }

    }


    /**
     * Write applications state.
     */
    public static void writeAppList(PrintWriter writer,
                                          MBeanServer mBeanServer, int mode)
        throws Exception {

        if (mode == 0){
            ObjectName queryHosts = new ObjectName("*:j2eeType=WebModule,*");
            Set hostsON = mBeanServer.queryNames(queryHosts, null);

            // Navigation menu
            writer.print("<dt>Application list</dt>");

            int count = 0;
            Iterator iterator = hostsON.iterator();
            while (iterator.hasNext()) {
                writer.print("<dd>");
                ObjectName contextON = (ObjectName) iterator.next();
                String webModuleName = contextON.getKeyProperty("name");
                if (webModuleName.startsWith("//")) {
                    webModuleName = webModuleName.substring(2);
                }
                int slash = webModuleName.indexOf("/");
                if (slash == -1) {
                    count++;
                    continue;
                }

                writer.print("<a href=\"#" + (count++) + ".0\">");
                writer.print(webModuleName);
                writer.print("</a>");
                writer.print("</dd>");
            }

        } else if (mode == 1){
            // for now we don't write out the Detailed state in XML
        }

    }


    /**
     * Write context state.
     */
    protected static void writeContext(PrintWriter writer, 
                                       ObjectName objectName,
                                       MBeanServer mBeanServer, int mode)
        throws Exception {

        if (mode == 0){
            String webModuleName = objectName.getKeyProperty("name");
            String name = webModuleName;
            if (name == null) {
                return;
            }
            
            String hostName = null;
            String contextName = null;
            if (name.startsWith("//")) {
                name = name.substring(2);
            }
            int slash = name.indexOf("/");
            if (slash != -1) {
                hostName = name.substring(0, slash);
                contextName = name.substring(slash);
            } else {
                return;
            }

            ObjectName queryManager = new ObjectName
                (objectName.getDomain() + ":type=Manager,path=" + contextName 
                 + ",host=" + hostName + ",*");
            Set managersON = mBeanServer.queryNames(queryManager, null);
            ObjectName managerON = null;
            Iterator iterator2 = managersON.iterator();
            while (iterator2.hasNext()) {
                managerON = (ObjectName) iterator2.next();
            }

            ObjectName queryJspMonitor = new ObjectName
                (objectName.getDomain() + ":type=JspMonitor,WebModule=" +
                 webModuleName + ",*");
            Set jspMonitorONs = mBeanServer.queryNames(queryJspMonitor, null);

            // Special case for the root context
            if (contextName.equals("/")) {
                contextName = "";
            }

            writer.print("<h3>");
            writer.print(name);
            writer.print("</h3>");
            writer.print("</a>");

            writer.print("<p>");
            Object startTime = getAttribute(mBeanServer, objectName,
                                                        "startTime");
            writer.print(" <strong>Start time:</strong> " +
                         new Date(((Long) startTime).longValue()));
            writer.print(" <br><strong>Startup time:</strong> ");
            writer.print(formatTime(getAttribute(mBeanServer, objectName, "startupTime"), false));
            writer.print(" <br><strong>TLD scan time:</strong> ");
            writer.print(formatTime(getAttribute(mBeanServer, objectName, "tldScanTime"), false));
            if (managerON != null) {
                writeManager(writer, managerON, mBeanServer, mode);
            }
            if (jspMonitorONs != null) {
                writeJspMonitor(writer, jspMonitorONs, mBeanServer, mode);
            }
            writer.print("</p>");

            String onStr = objectName.getDomain() 
                + ":j2eeType=Servlet,WebModule=" + webModuleName + ",*";
            ObjectName servletObjectName = new ObjectName(onStr);
            Set set = mBeanServer.queryMBeans(servletObjectName, null);
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                ObjectInstance oi = (ObjectInstance) iterator.next();
                writeWrapper(writer, oi.getObjectName(), mBeanServer, mode);
            }

        } else if (mode == 1){
            // for now we don't write out the context in XML
        }

    }


    /**
     * Write detailed information about a manager.
     */
    public static void writeManager(PrintWriter writer, ObjectName objectName,
                                    MBeanServer mBeanServer, int mode)
        throws Exception {

        if (mode == 0) {
            writer.print(" <br><strong>Active sessions:</strong> ");
            writer.print(getAttribute(mBeanServer, objectName, "activeSessions"));
            writer.print(" <br><strong>Session count:</strong> ");
            writer.print(getAttribute(mBeanServer, objectName, "sessionCounter"));
            writer.print(" <br><strong>Max active sessions:</strong> ");
            writer.print(getAttribute(mBeanServer, objectName, "maxActive"));
            writer.print(" <br><strong>Rejected session creations:</strong> ");
            writer.print(getAttribute(mBeanServer, objectName, "rejectedSessions"));
            writer.print(" <br><strong>Expired sessions:</strong> ");
            writer.print(getAttribute(mBeanServer, objectName, "expiredSessions"));
            writer.print(" <br><strong>Longest session alive time:</strong> ");
            writer.print(formatSeconds(getAttribute(mBeanServer, 
                                                    objectName,
                                                    "sessionMaxAliveTime")));
            writer.print(" <br><strong>Average session alive time:</strong> ");
            writer.print(formatSeconds(getAttribute(mBeanServer, 
                                                    objectName,
                                                    "sessionAverageAliveTime")));
            writer.print(" <br><strong>Processing time:</strong> ");
            writer.print(formatTime(getAttribute(mBeanServer, objectName, "processingTime"), false));
        } else if (mode == 1) {
            // for now we don't write out the wrapper details
        }

    }


    /**
     * Write JSP monitoring information.
     */
    public static void writeJspMonitor(PrintWriter writer,
                                       Set jspMonitorONs,
                                       MBeanServer mBeanServer,
                                       int mode)
            throws Exception {

        int jspCount = 0;
        int jspReloadCount = 0;

        Iterator iter = jspMonitorONs.iterator();
        while (iter.hasNext()) {
            ObjectName jspMonitorON = (ObjectName) iter.next();
            Object obj = getAttribute(mBeanServer, jspMonitorON, "jspCount");
            jspCount += ((Integer) obj).intValue();
            obj = getAttribute(mBeanServer, jspMonitorON, "jspReloadCount");
            jspReloadCount += ((Integer) obj).intValue();
        }

        if (mode == 0) {
            writer.print(" <br><strong>JSPs loaded:</strong> ");
            writer.print(jspCount);
            writer.print(" <br><strong>JSPs reloaded:</strong> ");
            writer.print(jspReloadCount);
        } else if (mode == 1) {
            // for now we don't write out anything
        }
    }


    /**
     * Write detailed information about a wrapper.
     */
    public static void writeWrapper(PrintWriter writer, ObjectName objectName,
                                    MBeanServer mBeanServer, int mode)
        throws Exception {

        if (mode == 0) {
            String servletName = objectName.getKeyProperty("name");
            
            String[] mappings = (String[]) 
                mBeanServer.invoke(objectName, "findMappings", null, null);
            
            writer.print("<h2>");
            writer.print(servletName);
            if ((mappings != null) && (mappings.length > 0)) {
                writer.print(" [ ");
                for (int i = 0; i < mappings.length; i++) {
                    writer.print(mappings[i]);
                    if (i < mappings.length - 1) {
                        writer.print(" , ");
                    }
                }
                writer.print(" ] ");
            }
            writer.print("</h2>");
            
            writer.print("<p>");
            writer.print(" <strong>Processing time:</strong> ");
            writer.print(formatTime(getAttribute(mBeanServer, objectName, "processingTime"), true));
            writer.print(" <br><strong>Max time:</strong> ");
            writer.print(formatTime(getAttribute(mBeanServer, objectName, "maxTime"), false));
            writer.print(" <br><strong>Request count:</strong> ");
            writer.print(getAttribute(mBeanServer, objectName, "requestCount"));
            writer.print(" <br><strong>Error count:</strong> ");
            writer.print(getAttribute(mBeanServer, objectName, "errorCount"));
            writer.print(" <br><strong>Load time:</strong> ");
            writer.print(formatTime(getAttribute(mBeanServer, objectName, "loadTime"), false));
            writer.print(" <br><strong>Classloading time:</strong> ");
            writer.print(formatTime(getAttribute(mBeanServer, objectName, "classLoadTime"), false));
            writer.print("</p>");
        } else if (mode == 1){
            // for now we don't write out the wrapper details
        }

    }


    /**
     * Filter the specified message string for characters that are sensitive
     * in HTML.  This avoids potential attacks caused by including JavaScript
     * codes in the request URL that is often reported in error messages.
     *
     * @param obj The message string to be filtered
     */
    public static String filter(Object obj) {

        if (obj == null)
            return ("?");
        String message = obj.toString();

        char content[] = new char[message.length()];
        message.getChars(0, message.length(), content, 0);
        StringBuilder result = new StringBuilder(content.length + 50);
        for (int i = 0; i < content.length; i++) {
            switch (content[i]) {
            case '<':
                result.append("&lt;");
                break;
            case '>':
                result.append("&gt;");
                break;
            case '&':
                result.append("&amp;");
                break;
            case '"':
                result.append("&quot;");
                break;
            default:
                result.append(content[i]);
            }
        }
        return (result.toString());

    }


    /**
     * Display the given size in bytes, either as KB or MB.
     *
     * @param mb true to display megabytes, false for kilobytes
     */
    public static String formatSize(Object obj, boolean mb) {

        long bytes = -1L;

        if (obj instanceof Long) {
            bytes = ((Long) obj).longValue();
        } else if (obj instanceof Integer) {
            bytes = ((Integer) obj).intValue();
        }

        if (mb) {
            long mbytes = bytes / (1024 * 1024);
            long rest = 
                ((bytes - (mbytes * (1024 * 1024))) * 100) / (1024 * 1024);
            return (mbytes + "." + ((rest < 10) ? "0" : "") + rest + " MB");
        } else {
            return ((bytes / 1024) + " KB");
        }

    }


    /**
     * Display the given time in ms, either as ms or s.
     *
     * @param seconds true to display seconds, false for milliseconds
     */
    public static String formatTime(Object obj, boolean seconds) {

        long time = -1L;

        if (obj instanceof Long) {
            time = ((Long) obj).longValue();
        } else if (obj instanceof Integer) {
            time = ((Integer) obj).intValue();
        }

        if (seconds) {
            return ((((float) time ) / 1000) + " s");
        } else {
            return (time + " ms");
        }
    }


    /**
     * Formats the given time (given in seconds) as a string.
     *
     * @param obj Time object to be formatted as string
     *
     * @return String formatted time
     */
    public static String formatSeconds(Object obj) {

        long time = -1L;

        if (obj instanceof Long) {
            time = ((Long) obj).longValue();
        } else if (obj instanceof Integer) {
            time = ((Integer) obj).intValue();
        }

        return (time + " s");
    }
    
    public static Object getAttribute(MBeanServer mBeanServer, ObjectName name, String attribute) throws JMException
    {
       try
       {
          return mBeanServer.getAttribute(name, attribute);
       }
       catch (AttributeNotFoundException e)
       {
          // Try again, reversing case of the first letter of the attribute
          StringBuilder builder = new StringBuilder(attribute.length());
          char first = attribute.charAt(0);
          builder.append(Character.isLowerCase(first) ? Character.toUpperCase(first) : Character.toLowerCase(first));
          builder.append(attribute.substring(1));
          
          try
          {
             return mBeanServer.getAttribute(name, builder.toString());
          }
          catch (AttributeNotFoundException e2)
          {
             // Throw original exception
             throw e;
          }
       }
    }


}
