package com.proxy_checker.tikhomirov;

import java.io.*;
import java.net.*;

public class ProxyChecker
{
    public static void main(String[] args)
    {
        try (FileInputStream fis = new FileInputStream("D:/ip.txt"))
        {
            int i;
            StringBuilder result = new StringBuilder();
            // считываем данные из файла FileInputStream
            while ((i = fis.read()) != -1) {
                // пропускаем код 13 (перенос каретки)
                if (i == 13) continue;
                // код 10 - перевод строки
                if (i == 10) {
                    //["202.152.51.44", "8080"]
                    String[] ipAndPort = result.toString().split(":");
                    result = new StringBuilder();
                    String ip = ipAndPort[0];
                    int port = Integer.parseInt(ipAndPort[1]);
                    CheckProxyThread thread = new CheckProxyThread(ip, port);
                    thread.start();
                // табуляция
                } else if (i == 9) {
                    result.append(":");
                } else {
                    result.append((char) i);
                }
            }
        } catch (IOException e) {
            System.out.println("File not found!");
        }
    }

    static class CheckProxyThread extends Thread
    {
        String ip;
        int port;

        public CheckProxyThread(String ip, int port)
        {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run()
        {
            // формируем порт и ip для "постучаться"
            Proxy proxy =
                new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));

            try {
                // адрес, куда будем стучаться
                URL url = new URL("https://vozhzhaev.ru/test.php");
                // подключение к web-серверу тук-тук
                URLConnection urlConnection = url.openConnection(proxy);
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine + " - работает");
                }

                FileOutputStream fos =
                    new FileOutputStream("D:/good_ip.txt");
                byte[] buffer = (ip + ":" + port + "\n").getBytes();
                fos.write(buffer);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                System.out.println(ip + " - не работает");
            }
        }
    }
}
