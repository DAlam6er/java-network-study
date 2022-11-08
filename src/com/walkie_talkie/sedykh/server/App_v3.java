package com.walkie_talkie.sedykh.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class App_v3
{
    public static void main(String[] args)
    {
        try (ServerSocket server = new ServerSocket(30333)) {
            while (true) {
                Socket sock = server.accept();
                new ClientThread(sock).startProcessing();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
