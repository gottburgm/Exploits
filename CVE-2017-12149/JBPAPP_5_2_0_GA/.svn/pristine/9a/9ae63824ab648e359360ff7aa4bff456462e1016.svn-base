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
package org.jboss.tutorial.ee.service.impl;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;

import org.jboss.annotation.spring.Spring;
import org.jboss.spring.callback.SpringLifecycleInterceptor;
import org.jboss.tutorial.ee.service.Horoscope;
import org.jboss.tutorial.spring.WordsCreator;

/**
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
@Stateful
@Interceptors(SpringLifecycleInterceptor.class)
public class HoroscopeBean implements Horoscope, Serializable
{

   private static final long serialVersionUID = 2300669204640707036L;

   private Set<String> sentences = new TreeSet<String>();

   @Spring(jndiName = "spring-pojo", bean = "horoscopeSentenceCreator")
   private WordsCreator horoscopeCreator;

   public String getHoroscope(int month)
   {
      String sentence = horoscopeCreator.createWord();
      if (!sentences.add(sentence))
      {
         System.out.println("Repeating horoscope sentence.");
      }
      return sentence;
   }

   @Remove
   public void clear()
   {
      sentences.clear();
   }

}
