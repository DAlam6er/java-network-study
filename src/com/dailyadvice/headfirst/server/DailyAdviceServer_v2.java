package com.dailyadvice.headfirst.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class DailyAdviceServer_v2
{
    String[] adviceList = {
        "Ешьте меньшими порциями",
        "Купите облегающие джинсы. Нет, они не делают вас полнее.",
        "Два слова: не годится",
        "Будьте честнее хотя бы сегодня. Скажите своему начальнику все, " +
            "что вы *на самом деле* о нем думаете.",
        "Возможно, вам стоит подобрать другую причёску"
    };

    public void go()
    {
        System.out.println("Старт сервера...");
        // Приложение отслеживает клиентские запросы на порту 4242
        // на том же компьютере, где выполняется данный код
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open())
        {
            serverChannel.bind(new InetSocketAddress(5000));
            // сервер входит в постоянный цикл,
            // ожидая клиентских подключений и обслуживая их
            while (serverChannel.isOpen()) {
                // Метод accept() блокирует приложение до тех пор,
                // пока не поступит запрос
                SocketChannel clientChannel = serverChannel.accept();

                PrintWriter writer = new PrintWriter(
                    Channels.newOutputStream(clientChannel));
                System.out.println("This socket port is: " +
                    clientChannel.socket().getLocalPort());

                String advice = getAdvice();
                writer.println(advice);
                // Закрываем сокет - работа с клиентом закончена.
                writer.close();
                System.out.println(advice);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String getAdvice()
    {
        int random = (int) (Math.random() * adviceList.length);
        return adviceList[random];
    }

    public static void main(String[] args)
    {
        new DailyAdviceServer_v2().go();
    }
}
