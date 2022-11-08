package com.chat.headfirst.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleChatServer
{
    private final List<PrintWriter> clientWriters = new ArrayList<>();

    public static void main(String[] args)
    {
        new SimpleChatServer().go();
    }

    public void go()
    {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        System.out.println("Server started.");
        try (ServerSocket serverSocket = new ServerSocket(5000))
        {
            while (!serverSocket.isClosed()) {
                System.out.println("Waiting for connection...");
                Socket clientSocket = serverSocket.accept();

                PrintWriter writer = new PrintWriter(
                    clientSocket.getOutputStream());
                clientWriters.add(writer);

                /*
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
                 */
                threadPool.submit(new ClientHandler(clientSocket));
                System.out.println("Connection established.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void tellEveryone(String message)
    {
        /*
        Iterator<Writer> it = clientOutputStreams.iterator();
        while (it.hasNext()) {
            System.out.println("inside tellEveryone()");
            try {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(message);
                writer.flush();
            } catch (NoSuchElementException ex) {
                ex.printStackTrace();
            }
        }
         */
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
            writer.flush();
        }
    }

    public class ClientHandler implements Runnable
    {
        BufferedReader reader;
        Socket clientSocket;

        public ClientHandler(Socket clientSocket)
        {
            this.clientSocket = clientSocket;
            try
            {
                // Open communication with client during init
                InputStreamReader isReader =
                    new InputStreamReader(clientSocket.getInputStream());
                reader = new BufferedReader(isReader);
                System.out.println("reader = " + reader);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run()
        {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println("client says: " + message);
                    tellEveryone(message);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("Thread stopped working.");
        }
    }
}
