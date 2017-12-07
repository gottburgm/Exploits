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

import javax.ejb.Stateless;

import org.jboss.annotation.spring.Spring;
import org.jboss.tutorial.ee.service.Randomizer;
import org.jboss.tutorial.spring.IntCreator;
import org.jboss.tutorial.spring.WordsCreator;

/**
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
@Stateless
public class RandomizerBean implements Randomizer
{

   private WordsCreator wordsCreator;

   @Spring(jndiName = "spring-pojo", bean = "stateIntCreator")
   private IntCreator intCreator;

   public WordsCreator getWordsCreator()
   {
      return wordsCreator;
   }

   @Spring(jndiName = "spring-pojo", bean = "staticWordsCreator")
   public void setWordsCreator(WordsCreator wordsCreator)
   {
      this.wordsCreator = wordsCreator;
   }

   public int getNumber(int radius)
   {
      return intCreator.createInt(radius);
   }

   public String getWord()
   {
      return getWordsCreator().createWord();
   }

}
