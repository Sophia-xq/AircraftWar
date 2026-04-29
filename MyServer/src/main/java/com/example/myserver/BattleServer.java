package com.example.myserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BattleServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9999);
            System.out.println("联机对战服务器已启动，等待玩家连接...");

            while (true) {
                Socket player1 = serverSocket.accept();
                System.out.println("玩家1 已连接: " + player1.getInetAddress());

                Socket player2 = serverSocket.accept();
                System.out.println("玩家2 已连接: " + player2.getInetAddress());
                System.out.println("房间已满，开始对战！");

                // 修复：先分别给两个玩家发送 action:start，再启动转发线程
                // 必须在启动转发线程之前发送，避免和转发消息交错
                PrintWriter toPlayer1 = new PrintWriter(
                        new OutputStreamWriter(player1.getOutputStream(), "UTF-8"), true);
                PrintWriter toPlayer2 = new PrintWriter(
                        new OutputStreamWriter(player2.getOutputStream(), "UTF-8"), true);

                // 修复：action:start 发给各自的玩家（告知自己的游戏可以开始）
                toPlayer1.println("action:start");
                toPlayer2.println("action:start");

                // 启动双向转发：player1 发的消息 → player2，player2 发的消息 → player1
                new Thread(new ForwardTask(player1, toPlayer2, "1→2")).start();
                new Thread(new ForwardTask(player2, toPlayer1, "2→1")).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修复：ForwardTask 接收 PrintWriter 而不是 Socket
     * 避免重复创建 OutputStreamWriter 导致流状态不一致
     *
     * 读取 from 的输入，原封不动转发给 out
     */
    static class ForwardTask implements Runnable {
        private final Socket from;
        private final PrintWriter out;
        private final String label;

        public ForwardTask(Socket from, PrintWriter out, String label) {
            this.from  = from;
            this.out   = out;
            this.label = label;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(from.getInputStream(), "UTF-8"));
                String msg;
                while ((msg = in.readLine()) != null) {
                    out.println(msg);
                    System.out.println("[转发 " + label + "] " + msg);
                }
            } catch (IOException e) {
                System.out.println("[" + label + "] 玩家断开连接");
            }
        }
    }
}