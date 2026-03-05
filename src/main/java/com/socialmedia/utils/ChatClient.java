package com.socialmedia.utils;

import java.io.*;
import java.net.Socket;
import java.util.function.BiConsumer;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listener;

    private BiConsumer<Integer, String> onIncoming; // (senderId, text)

    public void connect(int myUserId) throws Exception {
        socket = new Socket("127.0.0.1", 9090);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        out.println("AUTH:" + myUserId);

        listener = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("INCOMING:")) {
                        String rest = line.substring(9);
                        int idx = rest.indexOf(':');
                        int senderId = Integer.parseInt(rest.substring(0, idx));
                        String text = rest.substring(idx + 1);
                        if (onIncoming != null) onIncoming.accept(senderId, text);
                    }
                }
            } catch (Exception ignored) {}
        });
        listener.setDaemon(true);
        listener.start();
    }

    public void setOnIncoming(BiConsumer<Integer, String> handler) {
        this.onIncoming = handler;
    }

    public void send(int receiverId, String text) {
        if (out != null) out.println("MSG:" + receiverId + ":" + text);
    }
}