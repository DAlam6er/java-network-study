package com.beatboxfinal.headfirst.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BeatBoxServer
{
    // List of all the clients output streams to send message to when a message is received
    final List<ObjectOutputStream> clientOutputStreams = new ArrayList<>();

    public static void main(String[] args)
    {
        new BeatBoxServer().go();
    }

    public void go()
    {
        try (ServerSocket serverSock = new ServerSocket(4242))
        {
            ExecutorService threadPool = Executors.newCachedThreadPool();
            while (!serverSock.isClosed()) {
                Socket clientSocket = serverSock.accept();
                ObjectOutputStream out =
                    new ObjectOutputStream(clientSocket.getOutputStream());
                clientOutputStreams.add(out);

                /*
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
                 */
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                threadPool.execute(clientHandler);
                System.out.println("got a connection");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void tellEveryone(Object one, Object two)
    {
        // Send the message and the beat pattern to all the clients
        /*
        Iterator<ObjectOutputStream> it = clientOutputStreams.iterator();
        while (it.hasNext()) {
            try {
                ObjectOutputStream out = it.next();
                out.writeObject(one);
                out.writeObject(two);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
         */
        try {
            for (ObjectOutputStream clientOutputStream : clientOutputStreams) {
                clientOutputStream.writeObject(one);
                clientOutputStream.writeObject(two);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public class ClientHandler implements Runnable
    {
        private ObjectInputStream in;

        public ClientHandler(Socket clientSocket)
        {
            try {
                in = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run()
        {
            Object userName;
            Object beatSequence;

            try {
                while ((userName = in.readObject()) != null) {
                    beatSequence = in.readObject();
                    // beatSequence is actually a boolean array,
                    // but the server doesn't care about that
                    System.out.println("read two objects");
                    tellEveryone(userName, beatSequence);
                }
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }
}
