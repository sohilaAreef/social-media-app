package com.socialmedia.utils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer implements Runnable {
    private final int port;
    private volatile boolean running = true;

    private final ConcurrentHashMap<Integer, PrintWriter> online = new ConcurrentHashMap<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public void stop() { running = false; }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {
            while (running) {
                Socket s = server.accept();
                new Thread(() -> handleClient(s)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        Integer userId = null;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

            String first = in.readLine();
            if (first == null || !first.startsWith("AUTH:")) return;

            userId = Integer.parseInt(first.substring(5).trim());
            online.put(userId, out);

            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("MSG:")) {
                    // MSG:<receiverId>:<text>
                    String rest = line.substring(4);
                    int idx = rest.indexOf(':');
                    if (idx <= 0) continue;

                    int receiverId = Integer.parseInt(rest.substring(0, idx));
                    String text = rest.substring(idx + 1);

                    PrintWriter receiverOut = online.get(receiverId);
                    if (receiverOut != null) {
                        receiverOut.println("INCOMING:" + userId + ":" + text);
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (userId != null) online.remove(userId);
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}