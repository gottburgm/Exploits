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
package org.jboss.tutorial.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.tutorial.ee.service.Horoscope;
import org.jboss.tutorial.ee.service.Randomizer;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
public class AppController extends MultiActionController
{

   private Randomizer randomizer;
   private Horoscope horoscope;

   public Randomizer getRandomizer()
   {
      return randomizer;
   }

   public void setRandomizer(Randomizer randomizer)
   {
      this.randomizer = randomizer;
   }

   public Horoscope getHoroscope()
   {
      return horoscope;
   }

   public void setHoroscope(Horoscope horoscope)
   {
      this.horoscope = horoscope;
   }

   public ModelAndView numberHandler(HttpServletRequest request, HttpServletResponse response)
         throws Exception
   {
      int radius = ServletRequestUtils.getRequiredIntParameter(request, "radius");
      return new ModelAndView("main", "number", randomizer.getNumber(radius));
   }

   public ModelAndView wordHandler(HttpServletRequest request, HttpServletResponse response)
         throws Exception
   {
      return new ModelAndView("main", "word", randomizer.getWord());
   }

   public ModelAndView horoscopeHandler(HttpServletRequest request, HttpServletResponse response)
         throws Exception
   {
      if (ServletRequestUtils.getStringParameter(request, "clear") != null)
      {
         horoscope.clear();
         return new ModelAndView("main");
      }
      else
      {
         int month = ServletRequestUtils.getRequiredIntParameter(request, "month");
         return new ModelAndView("main", "horoscope", horoscope.getHoroscope(month));
      }
   }

}
