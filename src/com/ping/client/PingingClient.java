package com.ping.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.concurrent.TimeUnit;

public class PingingClient
{
    public static void main(String[] args)
    {
        InetSocketAddress server = new InetSocketAddress("127.0.0.1", 5000);
        try (SocketChannel chnl = SocketChannel.open(server)) {
            PrintWriter writer = new PrintWriter(Channels.newWriter(chnl, StandardCharsets.UTF_8));
            System.out.println("Network established");

            for (int i = 0; i < 10; i++) {
                String message = "ping " + i;
                writer.println(message);
                writer.flush();
                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM));
                System.out.println(currentTime + " Sent " + message);
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (InterruptedException | IOException ex) {
            ex.printStackTrace();
        }
    }
}
