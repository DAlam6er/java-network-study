package com.walkie_talkie.sedykh.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class App_v2
{
    public static void main(String[] args)
    {

        try (ServerSocket server = new ServerSocket(30333)) {
            // не зацикливаем, т.к. один раз соединяемся, не разрываем соединение после каждой передачи
            Socket sock = server.accept();

            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String str;
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            while (true) {
                str = br.readLine();
                if ("exit".equals(str)) break;
                System.out.printf("client %s sent: %s\n",
                    sock.getInetAddress().getCanonicalHostName(), str);

                bw.write("accepted\n");
                bw.flush();
            }
            br.close();
            bw.close();
            sock.close();
            System.out.println("client closed connection");
        } catch (IOException ex) {
            // порт занят, некорректно создан порт/сокет
            // сокет будет не создан
            System.out.println(ex.getMessage());
        }
    }
}
