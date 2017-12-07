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
package org.jboss.security.integration.password;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.jboss.security.plugins.FilePassword;

/**
 * Command line tool to deal with passwords
 * @author Anil.Saldhana@redhat.com
 * @since Mar 26, 2009
 */
public class PasswordTool
{   
   private static PasswordMaskManagement pwm = null;
   
   public PasswordTool()
   {
      if(pwm == null)
      {
         pwm = new PasswordMaskManagement();
         ShutdownHook sh = new ShutdownHook(pwm);
         Runtime.getRuntime().addShutdownHook(sh); 
      }
   }
   public static void main(String[] args)
   {   
      System.out.println("**********************************");
      System.out.println("****  JBoss Password Tool********");
      System.out.println("**********************************");
      
      new PasswordTool();
      try
      {
         pwm.load();  
      }
      catch(Exception e)
      { 
         System.out.println("Error while trying to load data:"+e.getMessage());
         System.out.println("Maybe it does not exist and need to be created.");
      }
      Scanner in = new Scanner(System.in);
      
      while(true)
      { 
         String commandStr = "0: Encrypt Keystore Password " +
         		"1:Specify KeyStore " +
         		"2:Create Password  " +
         		"3: Remove a domain " +
         		"4:Enquire Domain " + 
         		"5:Exit";
         
         System.out.println(commandStr);
         int choice = in.nextInt();
         
         switch(choice)
         {
            case 0: //Encrypt Keystore Password
               System.out.println("Enter Keystore password");
               String passStr = in.next();
               String saltStr ="";
               do
               {
                  System.out.println("Enter Salt (String should be at least 8 characters)");
                  saltStr = in.next(); 
               }while(saltStr.length() < 8);
               
               System.out.println("Enter Iterator Count (integer value)");
               int iterationCount = in.nextInt();
               
               String ksPassFileName = PasswordMaskManagement.keystorePassEncFileName;
               String[] filePasswordArgs = new String[]
                                          {saltStr, iterationCount+""
                     , passStr, ksPassFileName};
               try
               {
                  //Check if password directory exists
                  File passwordDir = new File("password");
                  if(passwordDir.exists() == false)
                     passwordDir.mkdir();
                  
                  FilePassword.main(filePasswordArgs);
               }
               catch (Exception e1)
               {
                  throw new RuntimeException(e1);
               } 
               System.out.println("Keystore Password encrypted into " + ksPassFileName);
               break;
               
            case 1: //Specify keystore
               System.out.println("Enter Keystore location including the file name");
               String loc = in.next();
               System.out.println("Enter Keystore alias");
               String alias = in.next();
               
               try
               {
                  pwm.setKeyStoreDetails(loc, alias); 
               }
               catch(Exception e)
               {
                  System.out.println("Exception being raised. Try to first encrypt the keystore password.");
                  System.out.println("or check the keystore location."); 
               } 
               load();
               break;
            case 2:  //Create a password 
               if(pwm.keyStoreExists())
               {
                  System.out.println("Enter security domain:");
                  String domain = in.next();
                  System.out.println("Enter passwd:");
                  String p = in.next();
                  pwm.storePassword(domain, p.toCharArray()); 
                  System.out.println("Password created for domain:" + domain); 
               }
               else
                  System.out.println("Enter Keystore details first");
               break;
            case 3: //Remove a domain
               if(pwm.keyStoreExists())
               {
                  System.out.println("Enter security domain to be removed:");
                  String domainToRemove = in.next();
                  pwm.removePassword(domainToRemove); 
               }
               else
                  System.out.println("Enter Keystore details first");
               break;
            case 4: //Check if domain exists
               if(pwm.keyStoreExists())
               {
                  System.out.println("Enter security domain to enquire:");
                  String domainToEnquire = in.next();
                  System.out.println("Exists = " + pwm.exists(domainToEnquire));
               }
               else
                  System.out.println("Enter Keystore details first");
               break; 
            default: System.exit(0);
         }
      } 
   } 
   
   private static void load()
   {
      try
      {
          pwm.load();
      }
      catch(FileNotFoundException ignore)
      {
         
      }
      catch(Exception e)
      {
         e.printStackTrace();
      } 
   }
   
   /**
    * A shutdown hook that 
    * stores the password map 
    * onto the file
    * @author anil
    *
    */
   private class ShutdownHook extends Thread
   { 
      private PasswordMaskManagement pmm;

      public ShutdownHook(PasswordMaskManagement pmm)
      {
         this.pmm = pmm;
      }
      
      public void run()
      {
         try
         {
            System.out.println(getClass().getName() + " run called");
            pmm.store(); 
         }
         catch(Exception e)
         {
            throw new RuntimeException(e);
         }
      }
   }
}
