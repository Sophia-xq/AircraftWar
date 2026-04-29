package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 匹配大厅 Activity
 *
 * 流程：
 *   房主：输入昵称和房间名 → 创建房间 → 进入 GameActivity 等待
 *   客机：刷新房间列表 → 点击加入 → 进入 GameActivity
 *
 * 重要说明：
 *   Mac 上两个 Android 模拟器联机测试时：
 *   HTTP_SERVER = http://10.0.2.2:8080
 *   BattleServer = 10.0.2.2:9999
 *
 *   两个模拟器都应该连接 Mac 主机上的 10.0.2.2。
 *   不要使用 RankServer 返回的 hostIp，否则加入方可能连不到真正的 BattleServer。
 */
public class MatchLobbyActivity extends AppCompatActivity {

    // 模拟器访问 Mac 本机服务器用 10.0.2.2
    // 真机测试时，需要改成 Mac 在局域网中的 IP，例如 http://192.168.1.100:8080
    public static final String HTTP_SERVER = "http://10.0.2.2:8080";

    private static final int REFRESH_INTERVAL_MS = 3000;

    private EditText etPlayerName, etRoomName;
    private Button btnCreate, btnRefresh, btnBack;
    private TextView tvStatus;
    private ListView lvRooms;

    private final List<RoomSummary> roomList = new ArrayList<>();
    private RoomAdapter roomAdapter;

    private String myRoomId = null;
    private String myPlayerName = "玩家";
    private boolean waitingAsHost = false;

    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Runnable autoRefreshTask = new Runnable() {
        @Override
        public void run() {
            fetchRoomList();
            mainHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_lobby);

        etPlayerName = findViewById(R.id.et_player_name_lobby);
        etRoomName = findViewById(R.id.et_room_name);
        btnCreate = findViewById(R.id.btn_create_room);
        btnRefresh = findViewById(R.id.btn_refresh_rooms);
        btnBack = findViewById(R.id.btn_back_lobby);
        tvStatus = findViewById(R.id.tv_lobby_status);
        lvRooms = findViewById(R.id.lv_rooms);

        roomAdapter = new RoomAdapter();
        lvRooms.setAdapter(roomAdapter);

        btnCreate.setOnClickListener(v -> createRoom());
        btnRefresh.setOnClickListener(v -> fetchRoomList());
        btnBack.setOnClickListener(v -> finish());

        lvRooms.setOnItemClickListener((parent, view, position, id) -> {
            if (waitingAsHost) {
                Toast.makeText(this, "你已创建房间，等待对手加入", Toast.LENGTH_SHORT).show();
                return;
            }

            if (position >= 0 && position < roomList.size()) {
                joinRoom(roomList.get(position));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainHandler.post(autoRefreshTask);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mainHandler.removeCallbacks(autoRefreshTask);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (myRoomId != null) {
            deleteRoom(myRoomId);
        }
    }

    // ════════════════════════════════════════════════
    // 创建房间
    // ════════════════════════════════════════════════

    private void createRoom() {
        String playerName = etPlayerName.getText().toString().trim();
        String roomName = etRoomName.getText().toString().trim();

        if (playerName.isEmpty()) {
            playerName = "玩家";
        }

        if (roomName.isEmpty()) {
            Toast.makeText(this, "请输入房间名", Toast.LENGTH_SHORT).show();
            return;
        }

        myPlayerName = playerName;
        final String finalPlayerName = playerName;
        final String finalRoomName = roomName;

        tvStatus.setText("正在创建房间...");
        btnCreate.setEnabled(false);

        new Thread(() -> {
            try {
                String body = gson.toJson(new RoomCreateRequest(finalRoomName, finalPlayerName));
                String resp = postJson("/api/room/create", body);
                RoomCreateResponse result = gson.fromJson(resp, RoomCreateResponse.class);

                mainHandler.post(() -> {
                    if (result != null && result.success) {
                        myRoomId = result.roomId;
                        waitingAsHost = true;

                        tvStatus.setText(
                                "房间「" + finalRoomName + "」已创建，等待对手加入...\n房间ID: " + myRoomId
                        );
                        btnCreate.setEnabled(false);

                        Toast.makeText(this, "房间创建成功！等待对手...", Toast.LENGTH_SHORT).show();

                        startGameAsHost(finalPlayerName);
                    } else {
                        tvStatus.setText("创建失败，请重试");
                        btnCreate.setEnabled(true);
                        Toast.makeText(this, "创建房间失败", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    tvStatus.setText("网络错误：" + e.getMessage());
                    btnCreate.setEnabled(true);
                    Toast.makeText(this, "创建房间失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void startGameAsHost(String playerName) {
        String battleServerIp = extractIpFromUrl(HTTP_SERVER);

        Intent intent = new Intent(MatchLobbyActivity.this, GameActivity.class);
        intent.putExtra("difficulty", "normal");
        intent.putExtra("music_on", true);
        intent.putExtra("multiplayer", true);
        intent.putExtra("server_ip", battleServerIp);
        intent.putExtra("player_name", playerName);
        intent.putExtra("room_id", myRoomId);
        intent.putExtra("is_host", true);

        startActivity(intent);
    }

    // ════════════════════════════════════════════════
    // 刷新房间列表
    // ════════════════════════════════════════════════

    private void fetchRoomList() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(HTTP_SERVER + "/api/room/list")
                        .get()
                        .build();

                try (Response resp = http.newCall(request).execute()) {
                    if (!resp.isSuccessful() || resp.body() == null) {
                        return;
                    }

                    String json = resp.body().string();
                    Type listType = new TypeToken<List<RoomSummary>>() {}.getType();
                    List<RoomSummary> list = gson.fromJson(json, listType);

                    mainHandler.post(() -> {
                        roomList.clear();

                        if (list != null) {
                            roomList.addAll(list);
                        }

                        roomAdapter.notifyDataSetChanged();

                        if (roomList.isEmpty()) {
                            if (!waitingAsHost) {
                                tvStatus.setText("暂无房间，快去创建一个吧！");
                            }
                        } else {
                            if (!waitingAsHost) {
                                tvStatus.setText("点击房间加入对战");
                            }
                        }
                    });
                }

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (!waitingAsHost) {
                        tvStatus.setText("刷新失败：检查服务器是否启动");
                    }
                });
            }
        }).start();
    }

    // ════════════════════════════════════════════════
    // 加入房间
    // ════════════════════════════════════════════════

    private void joinRoom(RoomSummary room) {
        String playerName = etPlayerName.getText().toString().trim();

        if (playerName.isEmpty()) {
            playerName = "玩家";
        }

        final String finalPlayerName = playerName;

        tvStatus.setText("正在加入「" + room.roomName + "」...");

        new Thread(() -> {
            try {
                String body = gson.toJson(new RoomJoinRequest(room.roomId));
                String resp = postJson("/api/room/join", body);
                RoomJoinResponse result = gson.fromJson(resp, RoomJoinResponse.class);

                mainHandler.post(() -> {
                    if (result != null && result.success) {

                        /*
                         * 关键修改：
                         *
                         * 原来这里用的是：
                         *   String hostIp = result.hostIp;
                         *
                         * 但在 Mac + 两个 Android 模拟器测试时，
                         * RankServer 记录到的 hostIp 不一定是另一个模拟器能访问的地址。
                         *
                         * 所以这里和房主保持一致：
                         * 两个模拟器都连接 HTTP_SERVER 所在主机，也就是 10.0.2.2。
                         */
                        String battleServerIp = extractIpFromUrl(HTTP_SERVER);

                        tvStatus.setText("加入成功！正在连接对战服务器...");
                        Toast.makeText(this, "加入房间成功！", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MatchLobbyActivity.this, GameActivity.class);
                        intent.putExtra("difficulty", "normal");
                        intent.putExtra("music_on", true);
                        intent.putExtra("multiplayer", true);
                        intent.putExtra("server_ip", battleServerIp);
                        intent.putExtra("player_name", finalPlayerName);
                        intent.putExtra("is_host", false);

                        startActivity(intent);

                    } else {
                        String msg = result != null && result.message != null
                                ? result.message
                                : "加入失败";

                        tvStatus.setText(msg);
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    tvStatus.setText("网络错误：" + e.getMessage());
                    Toast.makeText(this, "加入失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // ════════════════════════════════════════════════
    // 删除房间
    // ════════════════════════════════════════════════

    public void deleteRoom(String roomId) {
        new Thread(() -> {
            try {
                postJson("/api/room/delete", "{\"roomId\":\"" + roomId + "\"}");
                System.out.println("[MatchLobby] 房间已删除: " + roomId);
            } catch (Exception ignored) {
            }
        }).start();

        myRoomId = null;
        waitingAsHost = false;

        mainHandler.post(() -> btnCreate.setEnabled(true));
    }

    // ════════════════════════════════════════════════
    // 工具方法
    // ════════════════════════════════════════════════

    private String postJson(String path, String bodyStr) throws IOException {
        RequestBody body = RequestBody.create(
                bodyStr,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(HTTP_SERVER + path)
                .post(body)
                .build();

        try (Response resp = http.newCall(request).execute()) {
            return resp.body() != null ? resp.body().string() : "{}";
        }
    }

    private String extractIpFromUrl(String url) {
        try {
            URL u = new URL(url);
            return u.getHost();
        } catch (Exception e) {
            return "10.0.2.2";
        }
    }

    // ════════════════════════════════════════════════
    // ListView Adapter
    // ════════════════════════════════════════════════

    class RoomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return roomList.size();
        }

        @Override
        public Object getItem(int pos) {
            return roomList.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MatchLobbyActivity.this)
                        .inflate(R.layout.item_room, parent, false);
            }

            RoomSummary room = roomList.get(position);

            TextView tvRoomName = convertView.findViewById(R.id.tv_room_name);
            TextView tvHostName = convertView.findViewById(R.id.tv_host_name);

            tvRoomName.setText(room.roomName);
            tvHostName.setText("房主：" + room.hostName);

            return convertView;
        }
    }

    // ════════════════════════════════════════════════
    // 数据模型
    // ════════════════════════════════════════════════

    static class RoomSummary {
        String roomId;
        String roomName;
        String hostName;
    }

    static class RoomCreateRequest {
        String roomName;
        String playerName;

        RoomCreateRequest(String roomName, String playerName) {
            this.roomName = roomName;
            this.playerName = playerName;
        }
    }

    static class RoomJoinRequest {
        String roomId;

        RoomJoinRequest(String roomId) {
            this.roomId = roomId;
        }
    }

    static class RoomCreateResponse {
        boolean success;
        String roomId;
        String message;
    }

    static class RoomJoinResponse {
        boolean success;
        String hostIp;
        String message;
    }
}