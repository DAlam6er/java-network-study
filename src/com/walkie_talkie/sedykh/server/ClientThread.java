package com.walkie_talkie.sedykh.server;

import java.io.*;
import java.net.Socket;

public class ClientThread implements Runnable
{
    private final Thread self;
    private final Socket sock;
    private BufferedReader reader;
    private BufferedWriter writer;
    // количество клиентов, будет увеличаться на единицу у каждого клиента
    private static int clientsNum;
    // уникальный идентификатор клиента
    private final int clientID;

    // класс потока должен знать КОГО он обрабатывает,
    // поэтому добавляет Socket в конструктор
    public ClientThread(Socket sock)
    {
        this.sock = sock;
        self = new Thread(this);
        clientID = clientsNum++;
    }

    public void startProcessing() throws IOException
    {
        reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
        self.start();
    }

    @Override
    public void run()
    {
        String str;
        try {
            while (true) {
                str = reader.readLine();
                if ("exit".equals(str)) break;

                System.out.printf("client-%s %s sent: %s\n",
                    clientID, sock.getInetAddress().getCanonicalHostName(),
                    str);

                writer.write("accepted\n");
                writer.flush();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            System.out.println("client-" + clientID + " closed connection");
        }
    }
}
