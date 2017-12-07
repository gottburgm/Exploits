/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.console.manager.interfaces.impl;

import org.jboss.console.navtree.AppletBrowser;
import org.jboss.console.navtree.AppletTreeAction;
import org.jboss.console.navtree.TreeContext;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.general.DatasetChangeEvent;

import javax.management.ObjectName;
import java.util.ArrayList;

/**
 * <description>
 *
 * @see <related>
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 68694 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>3 janv. 2003 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */
public class GraphMBeanAttributeAction
        implements AppletTreeAction
{
   public class MBeanXYDataset extends AbstractXYDataset
   {

      private ArrayList data = new ArrayList();

      /**
       * Default constructor.
       */
      public MBeanXYDataset()
      {
      }

      public void clear()
      {
         data.clear();
         notifyListeners(new DatasetChangeEvent(this, this));
      }

      public void add(Object num)
      {
         data.add(num);
         notifyListeners(new DatasetChangeEvent(this, this));
      }

      /**
       * Returns the x-value for the specified series and item.  Series are numbered 0, 1, ...
       *
       * @param series  the index (zero-based) of the series.
       * @param item  the index (zero-based) of the required item.
       *
       * @return the x-value for the specified series and item.
       */
      public Number getX(int series, int item)
      {
          return (Number)item;
      }

      /**
       * Returns the y-value for the specified series and item.  Series are numbered 0, 1, ...
       *
       * @param series  the index (zero-based) of the series.
       * @param item  the index (zero-based) of the required item.
       *
       * @return the y-value for the specified series and item.
       */
      public Number getY(int series, int item)
      {
	return (Number)data.get(item);
      }

      /**
       * Returns the x-value for the specified series and item.  Series are numbered 0, 1, ...
       *
       * @param series  the index (zero-based) of the series.
       * @param item  the index (zero-based) of the required item.
       *
       * @return the x-value for the specified series and item.
       */
      public double getXValue(int series, int item)
      {
          return item;
      }

      /**
       * Returns the y-value for the specified series and item.  Series are numbered 0, 1, ...
       *
       * @param series  the index (zero-based) of the series.
       * @param item  the index (zero-based) of the required item.
       *
       * @return the y-value for the specified series and item.
       */
      public double getYValue(int series, int item)
      {
	double result = Double.NaN;
        Number x = (Number)data.get(item);
        if (x != null) {
            result = x.doubleValue();
        }
        return result;
      }

      /**
       * Returns the key for a series.  
       * <p>
       * If <code>series</code> is not within the specified range, the 
       * implementing method should throw an {@link IndexOutOfBoundsException} 
       * (preferred) or an {@link IllegalArgumentException}.
       *
       * @param series  the series index (in the range <code>0</code> to 
       *     <code>getSeriesCount() - 1</code>).
       *
       * @return The series key.
       */
      public Comparable getSeriesKey(int series){
          return "graph"; //Just better to keep it as "graph" for now
      }

      /**
       * Returns the number of series in the dataset.
       *
       * @return the number of series in the dataset.
       */
      public int getSeriesCount()
      {
         return 1;
      }

      /**
       * Returns the name of the series.
       *
       * @param series  the index (zero-based) of the series.
       *
       * @return the name of the series.
       */
      public String getSeriesName(int series)
      {
         return "y = " + attr;
      }

      /**
       * Returns the number of items in the specified series.
       *
       * @param series  the index (zero-based) of the series.
       * @return the number of items in the specified series.
       *
       */
      public int getItemCount(int series)
      {
         return data.size();
      }
   }

   public class UpdateThread implements Runnable
   {
      MBeanXYDataset data;
      TreeContext tc;

      public UpdateThread(MBeanXYDataset data, TreeContext tc)
      {
         this.data = data;
         this.tc = tc;
      }

      public void run()
      {
         while (true)
         {
            try
            {
               if (frame.isShowing())
               {
                  Object val = tc.getRemoteMBeanInvoker().getAttribute(targetObjectName, attr);
                  System.out.println("added value: " + val);
                  data.add(val);
               }
               Thread.sleep(1000);
            }
            catch (Exception ex)
            {
               ex.printStackTrace();
            }
         }
      }
   }


   protected ObjectName targetObjectName = null;
   protected String attr = null;
   protected transient ChartFrame frame = null;
   protected transient MBeanXYDataset dataset = null;

   public GraphMBeanAttributeAction()
   {
   }

   public GraphMBeanAttributeAction(ObjectName pName,
                                    String attr)
   {
      this.targetObjectName = pName;
      this.attr = attr;
   }

   public void doAction(TreeContext tc, AppletBrowser applet)
   {
      try
      {
         if (frame == null)
         {
            //tc.getRemoteMBeanInvoker ().invoke(targetObjectName, actionName, params, signature);
            dataset = new MBeanXYDataset();
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "JMX Attribute: " + attr, "count", attr, dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            UpdateThread update = new UpdateThread(dataset, tc);

            Thread thread = new Thread(update);
            thread.start();
            frame = new ChartFrame("JMX Attribute: " + attr, chart);
            frame.getChartPanel().setPreferredSize(new java.awt.Dimension(500, 270));
            frame.pack();
         }
         else
         {
            dataset.clear();
         }
         frame.show();
         frame.requestFocus();
      }
      catch (Exception displayed)
      {
         displayed.printStackTrace();
      }
   }

}
