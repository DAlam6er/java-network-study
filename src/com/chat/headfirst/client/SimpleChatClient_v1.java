package com.chat.headfirst.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SimpleChatClient_v1
{
    JTextArea incoming;
    JTextField outgoing;
    BufferedReader reader;
    PrintWriter writer;

    public static void main(String[] args)
    {
        SimpleChatClient_v1 client = new SimpleChatClient_v1();
        client.go();
    }

    // GUI setup, start another Thread
    public void go()
    {
        JFrame frame = new JFrame("Simple Chat Client");
        JPanel mainPanel = new JPanel();

        incoming = new JTextArea(15, 20);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(false);

        JScrollPane qScroller = new JScrollPane(incoming);
        qScroller.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        outgoing = new JTextField(20);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());

        mainPanel.add(qScroller);
        mainPanel.add(outgoing);
        mainPanel.add(sendButton);

        setUpNetworking();

        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();

        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        frame.setSize(400, 350);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void setUpNetworking()
    {
        try {
            // используем сокет для получения входящего и исходящего потоков.
            // Исходящий поток уже задействован для отправки данных,
            // но теперь к нему добавился входящий поток,
            // поэтому наш объект Thread может получать сообщения от сервера.
            Socket sock = new Socket("127.0.0.1", 5000);
            InputStreamReader streamReader = new InputStreamReader(
                sock.getInputStream());
            reader = new BufferedReader(streamReader);
            writer = new PrintWriter(sock.getOutputStream());
            System.out.println("networking established");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class SendButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (!outgoing.getText().isEmpty() ||
                !("".equals(outgoing.getText())))
            {
                writer.println(outgoing.getText());
                writer.flush();
            }
            outgoing.setText("");
            outgoing.requestFocus();
        }
    }

    // Работа, которую выполняет поток.
    public class IncomingReader implements Runnable
    {
        String message;

        @Override
        public void run()
        {
            try {
                // пока ответ сервера не будет равен null
                // читаем за раз строку,
                // добавляем её в прокручиваемую текстовую область
                System.out.println("Поток запустился...");
                while ((message = reader.readLine()) != null) {
                    System.out.println("read " + message);
                    incoming.append(message + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Поток завершается...");
        }
    }
}
