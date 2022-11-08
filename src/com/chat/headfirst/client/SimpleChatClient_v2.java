package com.chat.headfirst.client;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleChatClient_v2
{
    public static final String SERVER_ADDRESS = "localhost";

    private JTextArea incoming;
    private JTextField outgoing;
    private BufferedReader reader;
    private PrintWriter writer;

    public static void main(String[] args)
    {
        new SimpleChatClient_v2().go();
    }

    // GUI setup, start another Thread
    public void go()
    {
        setUpNetworking();

        JScrollPane scroller = createScrollableTextArea();

        outgoing = new JTextField(20);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        JPanel mainPanel = new JPanel();
        mainPanel.add(scroller);
        mainPanel.add(outgoing);
        mainPanel.add(sendButton);

        // Добавляем экземпляр класса, реализующего интерфейс Runnable
        // Работа заключается в том, чтобы читать серверный поток, содержащий входные сообщения.
        // Используем SingleThreadExecutor(), т.к. это единственная задача.
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new IncomingReader());

        JFrame frame = new JFrame("Simple Chat Client");
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        frame.setSize(400, 350);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private JScrollPane createScrollableTextArea()
    {
        incoming = new JTextArea(15, 20);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(false);
        JScrollPane scroller = new JScrollPane(incoming);
        scroller.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroller;
    }

    private void setUpNetworking()
    {
        try {
            InetSocketAddress serverAddress =
                new InetSocketAddress(SERVER_ADDRESS, 5000);
            SocketChannel socketChannel = SocketChannel.open(serverAddress);

            reader = new BufferedReader(Channels.newReader(
                socketChannel, StandardCharsets.UTF_8));

            writer = new PrintWriter(Channels.newWriter(
                socketChannel, StandardCharsets.UTF_8));

            System.out.println("networking established");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage()
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
