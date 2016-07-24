package com.techhounds.houndvision.web;

import java.io.PrintWriter;
import java.net.Socket;

public class DataIO {

    public final String HOSTNAME = "192.168.1.80";
    public final int PORT = 55868;

    private PrintWriter output;
    private static DataIO instance;

    public static DataIO getInstance() {
        return instance == null ? instance = new DataIO() : instance;
    }

    public static void clearInstance() {
        instance = null;
    }

    private DataIO() {

        try {
            Socket echoSocket = new Socket(HOSTNAME, PORT);
            output = new PrintWriter(echoSocket.getOutputStream(), true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String data) {
        if(output != null)
         output.println(data);
    }
}
