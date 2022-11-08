package com.chat.tikhomirov.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server
{
    // порт, который будет слушать сервер
    static final int PORT = 3443;
    private final ArrayList<ClientHandler> clients = new ArrayList<>();

    public Server()
    {
        // сокет клиента - поток, который будет подключаться к серверу
        // по адресу и порту
        Socket clientSocket = null;
        //Серверный сокет
        //ServerSocket serverSocket = null;

        try (ServerSocket serverSocket = new ServerSocket(PORT))
        {
            //создаем серверный сокет на определенном порте
            System.out.println("Сервер стартует...");
            //запускаем бесконечный цикл
            while (!serverSocket.isClosed()) {
                //ждем подключений от сервера
                clientSocket = serverSocket.accept();
                //создаем обработчик который подключается к нашему серверу
                // this - это наш сервер
                ClientHandler client = new ClientHandler(
                    clientSocket,this);
                clients.add(client);
                //каждое подключение в новом потоке
                new Thread(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                //закрываем подключение
                clientSocket.close();
                System.out.println("Сервер остановлен");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // отправлять сообщения всем клиентам
    public void sendMessagesToAllClients(String msg)
    {
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    // удаляем клиента из коллекции при выходе из чата
    public void removeClient(ClientHandler client)
    {
        clients.remove(client);
    }
}
