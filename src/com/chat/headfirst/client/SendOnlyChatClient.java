package com.chat.headfirst.client;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SendOnlyChatClient
{
    private JTextField outgoing;
    private PrintWriter writer;

    // Создаем GUI и подключаем слушатель для событий к кнопке отправки
    // Вызываем метод setUpNetworking()
    public void go()
    {
        setUpNetworking();

        outgoing = new JTextField(20);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        JPanel mainPanel = new JPanel();
        mainPanel.add(outgoing);
        mainPanel.add(sendButton);

        JFrame frame = new JFrame("Ludicrously Simple Chat Client");
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        frame.setSize(400, 100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Создаем сокет и PrintWriter, присваиваем его переменной writer
    public void setUpNetworking()
    {
        try {
            // Socket нельзя помещать в блок try-catch,
            // иначе соединение с сервером будет сразу же разорвано
            Socket sock = new Socket("127.0.0.1", 5000);
            writer = new PrintWriter(sock.getOutputStream());
            System.out.println("networking established");
            // Socket закрывать нельзя!
            //sock.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendMessage()
    {
        writer.println(outgoing.getText());
        writer.flush();

        outgoing.setText("");
        outgoing.requestFocus();
    }

    public static void main(String[] args)
    {
        new SendOnlyChatClient().go();
    }
}
