package com.dailyadvice.headfirst.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class DailyAdviceClient_v1
{
    public void go()
    {
        try (Socket sock = new Socket("127.0.0.1", 5000);
             InputStreamReader streamReader =
                 new InputStreamReader(sock.getInputStream());
             BufferedReader reader = new BufferedReader(streamReader))
        {
            String advice = reader.readLine();
            System.out.println("Сегодня ты должен: " + advice);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        DailyAdviceClient_v1 client = new DailyAdviceClient_v1();
        client.go();
    }
}
