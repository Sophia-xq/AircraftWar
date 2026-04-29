package edu.hitsz;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.hitsz.dao.GameRecord;
import edu.hitsz.dao.GameRecordDAOImpl;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;
    private String roomId;    // 联机时用：游戏结束后清理房间
    private boolean isHost;   // 是否是房主（只有房主负责删除房间）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String difficulty  = getIntent().getStringExtra("difficulty");
        boolean musicOn    = getIntent().getBooleanExtra("music_on", true);
        boolean multiplayer = getIntent().getBooleanExtra("multiplayer", false);
        String serverIp    = getIntent().getStringExtra("server_ip");
        roomId             = getIntent().getStringExtra("room_id");
        isHost             = getIntent().getBooleanExtra("is_host", false);

        if (difficulty == null) difficulty = "normal";

        ImageManager.init(this);
        gameView = new GameView(this, difficulty, musicOn);

        if (multiplayer && serverIp != null) {
            gameView.setMultiplayer(true, serverIp);
        }

        gameView.setGameOverListener((score, diff) ->
                runOnUiThread(() -> showGameOverDialog(score, diff, multiplayer)));

        setContentView(gameView);
    }

    private void showGameOverDialog(int score, String difficulty, boolean isMultiplayer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("游戏结束");
        builder.setMessage("您的得分：" + score);

        final EditText input = new EditText(this);
        input.setHint("请输入玩家名");
        builder.setView(input);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String playerName = input.getText().toString().trim();
            if (playerName.isEmpty()) playerName = "玩家";
            final String finalName = playerName;

            String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(new Date());
            final GameRecord record = new GameRecord(finalName, score,
                    System.currentTimeMillis(), difficulty);

            new Thread(() -> {
                // 1. 写本地数据库
                GameRecordDAOImpl.getInstance(GameActivity.this).insertRecord(record);

                // 2. 上传在线排行榜（静默失败）
                uploadToOnlineRank(finalName, score, difficulty, formattedTime);

                // 3. 房主清理房间
                if (isHost && roomId != null) {
                    cleanupRoom(roomId);
                }

                runOnUiThread(() -> {
                    if (gameView != null) gameView.pauseGame();
                    Intent intent = new Intent(GameActivity.this, RankActivity.class);
                    intent.putExtra("initial_difficulty", difficulty);
                    startActivity(intent);
                    finish();
                });
            }).start();
        });

        builder.setNegativeButton("取消", (dialog, which) -> {
            if (isHost && roomId != null) cleanupRoom(roomId);
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    /** 上传分数到 RankServer（HTTP POST），失败不影响主流程 */
    private void uploadToOnlineRank(String playerName, int score, String difficulty, String formattedTime) {
        try {
            java.net.URL url = new java.net.URL(MatchLobbyActivity.HTTP_SERVER + "/api/score/upload");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);

            String body = "{\"playerName\":\"" + playerName.replace("\"", "\\\"")
                    + "\",\"score\":" + score
                    + ",\"difficulty\":\"" + difficulty
                    + "\",\"formattedTime\":\"" + formattedTime + "\"}";

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("UTF-8"));
            }
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("[排行榜] 上传失败（可忽略）: " + e.getMessage());
        }
    }

    /** 通知服务器删除房间 */
    private void cleanupRoom(String roomId) {
        try {
            java.net.URL url = new java.net.URL(MatchLobbyActivity.HTTP_SERVER + "/api/room/delete");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            String body = "{\"roomId\":\"" + roomId + "\"}";
            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("UTF-8"));
            }
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("[房间] 删除失败（可忽略）: " + e.getMessage());
        }
    }

    @Override protected void onResume()  { super.onResume();  if (gameView != null) gameView.resumeGame(); }
    @Override protected void onPause()   { super.onPause();   if (gameView != null) gameView.pauseGame(); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameView != null) {
            gameView.releaseAudio();
            gameView.releaseNetwork();
            gameView = null;
        }
    }
}