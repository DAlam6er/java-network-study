package com.chat.tikhomirov.server;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

//реализуем интерфейс Runnable котороый позволяет работать с потоками
public class ClientHandler implements Runnable
{
    // создаем экземпляр нашего сервера
    private Server server;
    // исходящее сообщение
    private PrintWriter outMessage;
    // входящее сообщение
    private Scanner inMessage;
    // количество клиентов в чате
    private static int clientsCount = 0;

    public ClientHandler(Socket clientsSocket, Server server)
    {
        try {
            clientsCount++;
            this.server = server;
            // создаем клиентский сокет
            this.outMessage = new PrintWriter(
                clientsSocket.getOutputStream());
            this.inMessage = new Scanner(clientsSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        try {
            // сервер отправляет сообщение
            server.sendMessagesToAllClients(
                "Новый участник вошёл в чат");
            server.sendMessagesToAllClients(
                "Клиентов в чате: " + clientsCount);
            while (true) {
                // если от клиента пришло сообщение
                if (inMessage.hasNext()) {
                    String clientMessage = inMessage.nextLine();
                    // если клиент отправляет данное сообщение,
                    // то цикл прерывается и клиент выходит
                    if (clientMessage.equalsIgnoreCase(
                        "##session##end##")) {
                        break;
                    }
                    // выводим в консоль сообщение
                    System.out.println(clientMessage);
                    // отправляем данное сообщение всем клиентам
                    server.sendMessagesToAllClients(clientMessage);
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            this.close();
        }
    }

    // отправляем сообщение
    public void sendMsg(String msg)
    {
        outMessage.println(msg);
        outMessage.flush();
    }

    public void close()
    {
        // Удаляем клиента из списка
        server.removeClient(this);
        clientsCount--;
        server.sendMessagesToAllClients(
            "Клиентов в чате: " + clientsCount);
    }
}
