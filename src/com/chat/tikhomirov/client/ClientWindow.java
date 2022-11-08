package com.chat.tikhomirov.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientWindow
{
    // адрес сервера
    private static final String SERVER_HOST = "localhost";
    // порт
    private static final int SERVER_PORT = 3443;
    // клиентский сокет
    private Socket clientSocket;
    // входящее сообщение
    private Scanner inMessage;
    // исходящее сообщение
    private PrintWriter outMessage;

    private final JTextField jtfMessage;
    private final JTextField jtfName;
    private final JTextArea jTextAreaMessage;

    // имя клиента
    private String clientName = "";

    public ClientWindow()
    {
        try {
            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
            inMessage = new Scanner(clientSocket.getInputStream());
            outMessage = new PrintWriter(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Элементы формы
        JFrame theFrame = new JFrame("Чат v.1.0");
        // задаем настройки элементов на форме
        theFrame.setBounds(600, 300, 600, 500);
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.setLocationRelativeTo(null);

        jTextAreaMessage = new JTextArea();
        jTextAreaMessage.setEditable(false);
        jTextAreaMessage.setLineWrap(true);

        // scroll
        JScrollPane jsp = new JScrollPane(jTextAreaMessage);
        theFrame.getContentPane().add(BorderLayout.CENTER, jsp);

        // label с количеством клиентов в чате
        JLabel jlNumberOfClients = new JLabel(
            "Количество участников в чате");
        theFrame.getContentPane().add(BorderLayout.NORTH, jlNumberOfClients);

        BorderLayout layout = new BorderLayout();
        JPanel bottomPanel = new JPanel(layout);
        theFrame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);

        JButton jbSendMessage = new JButton("Отправить ");
        bottomPanel.add(BorderLayout.EAST, jbSendMessage);

        jtfMessage = new JTextField("Введите ваше сообщение");
        bottomPanel.add(BorderLayout.CENTER, jtfMessage);

        jtfName = new JTextField("Введите ваше имя");
        bottomPanel.add(BorderLayout.WEST, jtfName);

        // обработчик нажатия (слушатель)
        jbSendMessage.addActionListener(e ->
        {
            //если имя клиента и сообщения непустые,
            // то отправляем сообщение
            if (!jtfMessage.getText().trim().isEmpty() &&
                !jtfName.getText().trim().isEmpty())
            {
                clientName = jtfName.getText();
                sendMsg();
                // меняем фокус на поле с сообщением
                jtfMessage.grabFocus();
            }
        });
        //при фокусе поле очищается
        jtfMessage.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                jtfMessage.setText("");
            }
        });

        jtfName.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                jtfName.setText("");
            }
        });

        // в отдельном потоке начинаем работу с сервером
        new Thread(() ->
        {
            while (true) {
                // есть ли входящие сообщения
                if (inMessage.hasNext()) {
                    String inMes = inMessage.nextLine();
                    String clientInChat = "Клиентов в чате = ";
                    if (inMes.indexOf(clientInChat) == 0) {
                        jlNumberOfClients.setText(inMes);
                    } else {
                        // выводим сообщение
                        jTextAreaMessage.append(inMes);
                        // добавляем строку перехода
                        jTextAreaMessage.append("\n");
                    }
                }
            }
        }).start();

        theFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                super.windowClosing(e);
                try {
                    if (!clientName.isEmpty() &&
                        "Введите ваше имя: ".equals(clientName))
                    {
                        outMessage.println(clientName + " вышел из окна");
                    } else {
                        outMessage.println(
                            "Участник вышел из чата, не представшись");
                    }
                    // служебные сообщения сигнал, что клиент вышел из чата
                    outMessage.println("##session##end##");
                    outMessage.flush();
                    outMessage.close();
                    inMessage.close();
                    clientSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        theFrame.setVisible(true);
    }

    // отправка сообщения
    private void sendMsg()
    {
        // формируем сообщение
        String messageStr = jtfName.getText() + ": " + jtfMessage.getText();
        // отправляем
        outMessage.println(messageStr);
        outMessage.flush();
        jtfMessage.setText("");
    }
}
