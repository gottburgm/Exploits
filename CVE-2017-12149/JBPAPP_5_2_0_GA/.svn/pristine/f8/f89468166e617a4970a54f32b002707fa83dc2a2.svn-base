/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.console.plugins.helpers.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 */
public class NavigationTag extends TagSupport
{
	private ArrayList tabs = new ArrayList(10);
	private String selectedTabName;
	
	public int doStartTag() throws JspTagException
	{
		tabs.clear();
		return EVAL_BODY_INCLUDE;
	}

	//output the navigation table and the tabs, with the 'class' setting determining the 
	//selected tab. 
	public int doEndTag() throws JspTagException
	{
		try
		{
			pageContext.getOut().write(
				"<table width='100%' height='24' border='0' cellspacing='0' cellpadding='0'>");
			pageContext.getOut().write("<tr valign='bottom'>");
			for (int i = 0; i < tabs.size(); i++)
			{
				Tab tab = (Tab) tabs.get(i);
				String name = tab.getName();		
				
				pageContext.getOut().write("<td width='8' align='left' class='tabSpacer'>");
				pageContext.getOut().write("<p><img src='images/spacer.gif' width='8' height='24'></p>");
				pageContext.getOut().write("</td>");
				pageContext.getOut().write("<td align='left' nowrap class=");
				
				if (isSelected(tab))
				{					
					pageContext.getOut().write("'tab'>");
				}
				else
				{
					pageContext.getOut().write("'tabOff'>");
				}
				pageContext.getOut().write("<p>");
				if (tab.getHref() != null)
				{
					pageContext.getOut().write("<a href='" + tab.getHref() + "'>");
				}
				pageContext.getOut().write(name);
				if (tab.getHref() != null)
				{
					pageContext.getOut().write("</a>");
				}					
				pageContext.getOut().write("</p></td>");
			}
			//last spacer takes up rest of the space
			pageContext.getOut().write("<td width='100%' align='left' class='tabSpacer'><p>&nbsp;</p></td>");
			pageContext.getOut().write("</tr>");
			pageContext.getOut().write("</table>");
		}
		catch (IOException e)
		{
			throw new JspTagException(e.toString());
		}
		return EVAL_PAGE;
	}

	/**
	 * @param tab
	 * @return
	 */
	private boolean isSelected(Tab tab)
	{
		boolean selected = false;
		
		if (tab.isSelected())
		{
			selected = true;			
		}
		
		//navigation parent setting overrides if set			
		if (selectedTabName != null && !selectedTabName.equals("")) 
		{  			
			selected = tab.getName().equals(selectedTabName);
		}
		
		return selected;		
	}

	public final void setTabs(Tab tab)
	{
		tabs.add(tab);
	}

	/**
	 * @return
	 */
	public String getSelectedTabName()
	{
		return selectedTabName;
	}

	/**
	 * @param string
	 */
	public void setSelectedTabName(String string)
	{
		selectedTabName = string;
	}

}
