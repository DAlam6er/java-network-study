package com.dailyadvice.headfirst.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class DailyAdviceClient_v2
{
    public void go()
    {
        InetSocketAddress serverAddress = new InetSocketAddress("localhost", 5000);
        try (SocketChannel socketChannel = SocketChannel.open(serverAddress);
             Reader channelReader = Channels.newReader(socketChannel, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(channelReader))
        {
            String advice = reader.readLine();
            System.out.println("Сегодня ты должен: " + advice);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        DailyAdviceClient_v2 client = new DailyAdviceClient_v2();
        client.go();
    }
}
