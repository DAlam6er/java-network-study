package com.walkie_talkie.sedykh.client;

import java.io.*;
import java.net.Socket;

public class App_v1
{
    public static void main(String[] args)
    {
        try {
            Socket sock = new Socket("localhost", 30333);
            // Обеспечиваем согласованность "клиент пишет - сервер читает"
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            // \n нужен, чтобы сообщить readline() сервера, что строка закончена
            bw.write("hello, server!\n");
            // мы передали всё, что хотели - принудительный сброс потока
            // без .flush() readline() клиента не начнется
            bw.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String str = br.readLine();
            System.out.printf("server replied: %s\n", str);

            bw.close();
            br.close();
            sock.close();
        } catch (IOException e) {
            // Соединение может не удасться (сервер не запущен - никто нас не слушает)
            System.out.println(e.getMessage());
        }
    }
}
