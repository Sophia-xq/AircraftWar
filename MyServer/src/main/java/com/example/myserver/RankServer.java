package com.example.myserver;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP 服务器：同时提供排行榜接口 + 匹配大厅接口
 * 端口 8080
 *
 * 排行榜接口（原有）：
 *   POST /api/score/upload     上传分数
 *   GET  /api/score/rank       获取 Top20
 *
 * 匹配大厅接口（新增）：
 *   POST /api/room/create      创建房间  body: {"roomName":"xxx","playerName":"xxx"}
 *   GET  /api/room/list        获取房间列表
 *   POST /api/room/join        加入房间  body: {"roomId":"xxx"}  → 返回房主IP
 *   POST /api/room/delete      删除房间  body: {"roomId":"xxx"}
 */
public class RankServer {

    // ── 排行榜数据（内存） ──────────────────────────────────────────────
    private static final List<RecordEntity> db = Collections.synchronizedList(new ArrayList<>());

    // ── 房间数据（内存，线程安全） ─────────────────────────────────────
    // key = roomId (UUID 前8位)
    private static final Map<String, RoomEntity> rooms = new ConcurrentHashMap<>();

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 排行榜接口
        server.createContext("/api/score/upload", new UploadHandler());
        server.createContext("/api/score/rank",   new RankHandler());

        // 匹配大厅接口
        server.createContext("/api/room/create", new RoomCreateHandler());
        server.createContext("/api/room/list",   new RoomListHandler());
        server.createContext("/api/room/join",   new RoomJoinHandler());
        server.createContext("/api/room/delete", new RoomDeleteHandler());

        server.start();
        System.out.println("服务器已启动，监听端口 8080");
        System.out.println("  排行榜: POST /api/score/upload  |  GET /api/score/rank");
        System.out.println("  匹配厅: POST /api/room/create   |  GET /api/room/list");
        System.out.println("          POST /api/room/join      |  POST /api/room/delete");
    }

    // ══════════════════════════════════════════════════════════════════
    //  排行榜处理器（原有，不改）
    // ══════════════════════════════════════════════════════════════════

    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { send405(exchange); return; }
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
            RecordEntity record = gson.fromJson(reader, RecordEntity.class);
            if (record != null) {
                db.add(record);
                System.out.println("[排行榜] 新成绩: " + record.playerName + " - " + record.score);
            }
            sendJson(exchange, 200, "{\"status\":\"success\"}");
        }
    }

    static class RankHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) { send405(exchange); return; }
            List<RecordEntity> sorted = new ArrayList<>(db);
            sorted.sort((a, b) -> Integer.compare(b.score, a.score));
            List<RecordEntity> top20 = sorted.subList(0, Math.min(sorted.size(), 20));
            sendJson(exchange, 200, gson.toJson(top20));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  匹配大厅处理器（新增）
    // ══════════════════════════════════════════════════════════════════

    /**
     * POST /api/room/create
     * body: { "roomName": "战神房间", "playerName": "Alice" }
     *
     * 服务器自动从请求头中读取房主的真实 IP（remoteAddr）
     * 返回: { "success": true, "roomId": "abc12345" }
     */
    static class RoomCreateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { send405(exchange); return; }

            // 解析请求体
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
            RoomCreateRequest req = gson.fromJson(reader, RoomCreateRequest.class);
            if (req == null || req.roomName == null || req.roomName.isEmpty()) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"缺少 roomName\"}");
                return;
            }

            // 从连接信息中取房主的真实 IP
            String hostIp = exchange.getRemoteAddress().getAddress().getHostAddress();

            // 生成唯一 roomId
            String roomId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

            RoomEntity room = new RoomEntity();
            room.roomId     = roomId;
            room.roomName   = req.roomName;
            room.hostName   = req.playerName != null ? req.playerName : "玩家";
            room.hostIp     = hostIp;
            room.status     = "waiting"; // waiting / full
            room.createTime = System.currentTimeMillis();

            rooms.put(roomId, room);
            System.out.println("[匹配厅] 房间已创建: " + room.roomName + " (id=" + roomId + ", hostIp=" + hostIp + ")");

            sendJson(exchange, 200,
                    "{\"success\":true,\"roomId\":\"" + roomId + "\",\"message\":\"房间创建成功\"}");
        }
    }

    /**
     * GET /api/room/list
     * 返回所有状态为 "waiting" 的房间列表（等待玩家加入的）
     * 超过 5 分钟无人加入的房间自动清理
     */
    static class RoomListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) { send405(exchange); return; }

            // 清理超时房间（5分钟）
            long now = System.currentTimeMillis();
            rooms.entrySet().removeIf(e -> now - e.getValue().createTime > 5 * 60 * 1000L);

            // 只返回 waiting 状态的房间，不暴露 IP（安全起见）
            List<RoomSummary> list = new ArrayList<>();
            for (RoomEntity r : rooms.values()) {
                if ("waiting".equals(r.status)) {
                    RoomSummary s = new RoomSummary();
                    s.roomId   = r.roomId;
                    s.roomName = r.roomName;
                    s.hostName = r.hostName;
                    list.add(s);
                }
            }
            sendJson(exchange, 200, gson.toJson(list));
        }
    }

    /**
     * POST /api/room/join
     * body: { "roomId": "abc12345" }
     * 返回: { "success": true, "hostIp": "192.168.1.5" }
     *
     * 把房主 IP 告诉客机，客机拿到 IP 后直接连 BattleServer（9999端口）
     * 同时把房间状态改为 "full"，从列表中消失
     */
    static class RoomJoinHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { send405(exchange); return; }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
            RoomJoinRequest req = gson.fromJson(reader, RoomJoinRequest.class);
            if (req == null || req.roomId == null) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"缺少 roomId\"}");
                return;
            }

            RoomEntity room = rooms.get(req.roomId);
            if (room == null) {
                sendJson(exchange, 404, "{\"success\":false,\"message\":\"房间不存在或已过期\"}");
                return;
            }
            if ("full".equals(room.status)) {
                sendJson(exchange, 409, "{\"success\":false,\"message\":\"房间已满\"}");
                return;
            }

            // 标记为已满，不再出现在列表中
            room.status = "full";
            System.out.println("[匹配厅] 玩家加入房间: " + room.roomName + " hostIp=" + room.hostIp);

            sendJson(exchange, 200,
                    "{\"success\":true,\"hostIp\":\"" + room.hostIp + "\"}");
        }
    }

    /**
     * POST /api/room/delete
     * body: { "roomId": "abc12345" }
     * 游戏结束后房主调用，清理房间
     */
    static class RoomDeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { send405(exchange); return; }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
            RoomJoinRequest req = gson.fromJson(reader, RoomJoinRequest.class); // 复用，只用 roomId
            if (req != null && req.roomId != null) {
                rooms.remove(req.roomId);
                System.out.println("[匹配厅] 房间已删除: " + req.roomId);
            }
            sendJson(exchange, 200, "{\"success\":true}");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  工具方法
    // ══════════════════════════════════════════════════════════════════

    private static void sendJson(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private static void send405(HttpExchange exchange) throws IOException {
        sendJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
    }

    // ══════════════════════════════════════════════════════════════════
    //  数据模型
    // ══════════════════════════════════════════════════════════════════

    static class RecordEntity {
        String playerName;
        int    score;
        String difficulty;
        String formattedTime;
    }

    static class RoomEntity {
        String roomId;
        String roomName;
        String hostName;
        String hostIp;     // 只在服务器内部保存，不直接暴露给列表接口
        String status;     // "waiting" | "full"
        long   createTime;
    }

    static class RoomSummary {
        String roomId;
        String roomName;
        String hostName;
        // 注意：故意不含 hostIp，只有 join 接口才返回
    }

    static class RoomCreateRequest {
        String roomName;
        String playerName;
    }

    static class RoomJoinRequest {
        String roomId;
    }
}