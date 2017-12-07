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
package org.jboss.test.cluster.mod_cluster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Mock httpd proxy that queues all received messages
 * @author Paul Ferraro
 */
@SuppressWarnings("boxing")
public class MockProxy implements Runnable
{
   private final BlockingQueue<Map.Entry<String, Map<String, String>>> requests = new LinkedBlockingQueue<Map.Entry<String, Map<String, String>>>();
   private final ConcurrentMap<String, BlockingQueue<Map.Entry<String, Map<String, String>>>> requestMap = new ConcurrentHashMap<String, BlockingQueue<Map.Entry<String, Map<String, String>>>>();
   
   private ServerSocket server;
   private final List<Thread> workers;
   private final int threads;
   
   private volatile boolean healthy = true;
   
   public MockProxy(int threads)
   {
      this.workers = new ArrayList<Thread>(threads);
      this.threads = threads;
   }
   
   public void run()
   {
      try
      {
         while (!Thread.currentThread().isInterrupted())
         {
            try
            {
               Socket socket = this.server.accept();
               socket.setSoTimeout(15000);
               
               try
               {
                  BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                  BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                  String line = reader.readLine();
                  
                  while ((line != null) && !Thread.currentThread().isInterrupted())
                  {
                     if (line.length() == 0)
                     {
                        line = reader.readLine();
                        continue;
                     }

                     String command = line;
                     System.out.println("Received command: " + command);
                     line = reader.readLine();
                     
                     while ((line != null) && (line.length() > 0))
                     {
                        // Ignore headers
                        line = reader.readLine();
                     }
                     
                     if (line != null)
                     {
                        line = reader.readLine();
                        System.out.println("Received body: " + line);
                     }

                     Map<String, String> parameters = new HashMap<String, String>();
                     
                     if (line != null)
                     {
                        if (line.length() > 0)
                        {
                           for (String parameter: line.split("&"))
                           {
                              String[] parts = parameter.split("=");
                              
                              String name = parts[0];
                              String value = URLDecoder.decode(parts[1], "UTF-8");
                              
                              parameters.put(name, value);
                           }
                           System.out.println("Parsed body: " + line);
                        }
                        
                        if (this.healthy)
                        {
                           writer.write("HTTP/1.0 200 OK");
                        }
                        else
                        {
                           writer.write("HTTP/1.0 500 ERROR");
                        }
                        
                        writer.newLine();
                        writer.newLine();
                        writer.flush();
                     }
                     
                     Map.Entry<String, Map<String, String>> request = Collections.singletonMap(command, parameters).entrySet().iterator().next();
                     
                     System.out.println("Received: " + request);
                     String jvmRoute = parameters.get("JVMRoute");

                     if (jvmRoute == null)
                     {
                        this.requests.add(request);
                     }
                     else
                     {
                        this.getRequests(jvmRoute).add(request);
                     }
                     Thread.yield();
                     if (line != null)
                     {
                        line = reader.readLine();
                     }
                  }
               }
               finally
               {
                  if (socket.isConnected())
                  {
                     try
                     {
                        socket.close();
                     }
                     catch (IOException e)
                     {
                        e.printStackTrace(System.err);
                     }
                  }
               }
            }
            catch (SocketException e)
            {
               System.out.println("Socket accept interrupted");
            }
            catch (SocketTimeoutException e)
            {
               System.out.println("Socket accept timeout");
            }
         }
      }
      catch (Throwable e)
      {
         e.printStackTrace(System.err);
      }
   }
   
   public void setHealty(boolean healthy)
   {
      this.healthy = healthy;
   }
   
   public BlockingQueue<Map.Entry<String, Map<String, String>>> getRequests()
   {
      return this.requests;
   }
   
   public BlockingQueue<Map.Entry<String, Map<String, String>>> getRequests(String jvmRoute)
   {
      BlockingQueue<Map.Entry<String, Map<String, String>>> newQueue = new LinkedBlockingQueue<Map.Entry<String, Map<String, String>>>();
      BlockingQueue<Map.Entry<String, Map<String, String>>> queue = this.requestMap.putIfAbsent(jvmRoute, newQueue);
      return (queue != null) ? queue : newQueue;
   }
   
   public int getPort()
   {
      return this.server.getLocalPort();
   }
   
   public void start() throws Exception
   {
      this.server = new ServerSocket(0, 200, null);
      this.server.setSoTimeout(15000);
      
      for (int i = 0; i < this.threads; ++i)
      {
         Thread thread = new Thread(this);
         
         this.workers.add(thread);
         
         thread.start();
      }
   }
   
   public void stop()
   {
      for (Thread worker: this.workers)
      {
         if (worker != null)
         {
            worker.interrupt();
         }
      }

      if (this.server != null)
      {
         try
         {
            this.server.close();
         }
         catch (IOException e)
         {
            e.printStackTrace(System.err);
         }
      }

      for (Thread worker: this.workers)
      {
         try
         {
            worker.join();
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
      }
      
      this.workers.clear();
      this.requestMap.clear();
      this.requests.clear();
   }
}