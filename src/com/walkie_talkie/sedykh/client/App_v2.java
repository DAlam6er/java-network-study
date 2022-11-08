package com.walkie_talkie.sedykh.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class App_v2
{
    public static void main(String[] args)
    {
        try {
            Socket sock = new Socket("localhost", 30333);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String str;
            Scanner scanner = new Scanner(System.in);
            while (true) {
                str = scanner.nextLine();
                bw.write(str);
                bw.write("\n");
                bw.flush();
                // после того, как сервер получит "exit", он разорвет соедиение с клиентом
                if ("exit".equals(str)) break;

                str = br.readLine();
                System.out.printf("server replied: %s\n", str);
            }
            br.close();
            bw.close();
            sock.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
