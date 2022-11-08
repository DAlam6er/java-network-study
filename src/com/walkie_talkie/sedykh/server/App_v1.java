package com.walkie_talkie.sedykh.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class App_v1
{
    public static void main(String[] args)
    {
        // Вешаем пассивный сокет на порт 30333, который будет ждать установки соединения
        try (ServerSocket server = new ServerSocket(30333)) {
            // Установка соединения. accept() - блокирующий вызов
            // В случае, если подключение разорвалось, произойдёт выброс IOException()
            // В ином случае, пока кто-то не подключится, будем сидеть в accept()
            Socket sock = server.accept();
            // Вместо файла - поток, передаваемый по сети
            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            // readline() считывает данные до тех пор, пока не кончится строка
            // или не будет перенос курсора на новую строку
            String str = br.readLine();
            System.out.printf("client %s sent: %s\n",
                sock.getInetAddress().getCanonicalHostName(), str);

            // Передаем сообщение клиенту
            // Буферизация здесь обязательна!
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            // \n нужен, чтобы сообщить readline() клиента, что строка закончена, её можно читать
            bw.write("accepted\n");
            // мы передали всё, что хотели - принудительный сброс потока
            // без .flush() readline() клиента не начнется
            bw.flush();

            br.close();
            bw.close();
            sock.close();
        } catch (IOException ex) {
            // порт занят, некорректно создан порт/сокет
            // сокет будет не создан
            System.out.println(ex.getMessage());
        }
    }
}
