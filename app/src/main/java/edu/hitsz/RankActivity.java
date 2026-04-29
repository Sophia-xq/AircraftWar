package edu.hitsz;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.hitsz.dao.GameRecord;
import edu.hitsz.dao.GameRecordDAOImpl;

public class RankActivity extends AppCompatActivity {

    private ListView lvRank;
    private Button btnBack;
    private Spinner spinnerDifficulty;
    private SwitchCompat switchRankMode;

    private GameRecordDAOImpl dao;
    private List<GameRecord> records = new ArrayList<>();
    private RankAdapter adapter;

    private volatile boolean isLoading = false;
    private volatile boolean isDestroyed = false;
    private boolean isFirstLoad = true;
    private boolean isOnlineMode = false;

    private String currentDifficulty = "normal";
    private final String[] difficultyValues = {"easy", "normal", "hard"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        lvRank            = findViewById(R.id.lv_rank);
        btnBack           = findViewById(R.id.btn_back);
        spinnerDifficulty = findViewById(R.id.spinner_difficulty);
        switchRankMode    = findViewById(R.id.switch_rank_mode);

        dao = GameRecordDAOImpl.getInstance(this);

        String initialDifficulty = getIntent().getStringExtra("initial_difficulty");
        if (initialDifficulty != null) currentDifficulty = initialDifficulty;

        int pos = Arrays.asList(difficultyValues).indexOf(currentDifficulty);
        spinnerDifficulty.setSelection(pos >= 0 ? pos : 1);

        switchRankMode.setOnCheckedChangeListener((btn, isChecked) -> {
            isOnlineMode = isChecked;
            Toast.makeText(this,
                    isChecked ? "切换到全球排行榜" : "切换到本地排行榜",
                    Toast.LENGTH_SHORT).show();
            loadData(currentDifficulty);
        });

        spinnerDifficulty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newDiff = difficultyValues[position];
                if (isFirstLoad) {
                    isFirstLoad = false;
                    loadData(currentDifficulty);
                    return;
                }
                if (!newDiff.equals(currentDifficulty)) {
                    currentDifficulty = newDiff;
                    loadData(newDiff);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        // 修复：不关闭单例数据库连接
    }

    private void loadData(String difficulty) {
        if (isLoading) return;
        isLoading = true;

        new Thread(() -> {
            List<GameRecord> result = new ArrayList<>();
            try {
                if (isOnlineMode) {
                    result = fetchOnlineRecords();
                } else {
                    result = dao.queryTopRecords(difficulty, 20);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 网络失败时降级到本地
                if (isOnlineMode) {
                    try { result = dao.queryTopRecords(difficulty, 20); }
                    catch (Exception ignored) {}
                    runOnUiThread(() -> {
                        if (!isDestroyed)
                            Toast.makeText(RankActivity.this, "网络请求失败，显示本地记录", Toast.LENGTH_SHORT).show();
                    });
                }
            } finally {
                // 修复：finally 保证锁一定释放
                isLoading = false;
            }

            final List<GameRecord> finalResult = result;
            runOnUiThread(() -> {
                if (isDestroyed) return;
                records = finalResult;
                if (adapter == null) {
                    adapter = new RankAdapter(this, records, (p, record) -> {
                        if (!isOnlineMode) deleteRecordAndRefresh(record);
                        else Toast.makeText(this, "无法删除全球排行记录", Toast.LENGTH_SHORT).show();
                    });
                    lvRank.setAdapter(adapter);
                } else {
                    adapter.updateData(records);
                }
            });
        }).start();
    }

    /**
     * 从 RankServer 拉取在线排行榜（GET /api/score/rank）
     *
     * 修复：服务器返回的字段是 playerName / score / difficulty / formattedTime
     * 用专门的 OnlineRecord POJO 解析，再转为 GameRecord，避免字段名不匹配
     */
    private List<GameRecord> fetchOnlineRecords() throws Exception {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("http://10.0.2.2:8080/api/score/rank")
                .get().build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new Exception("HTTP " + response.code());

            String json = response.body().string();
            Type listType = new TypeToken<List<OnlineRecord>>(){}.getType();
            List<OnlineRecord> onlineList = new Gson().fromJson(json, listType);

            List<GameRecord> out = new ArrayList<>();
            if (onlineList != null) {
                for (OnlineRecord r : onlineList) {
                    out.add(new GameRecord(
                            0,
                            r.playerName  != null ? r.playerName  : "?",
                            r.score,
                            0L,
                            r.formattedTime != null ? r.formattedTime : "",
                            r.difficulty  != null ? r.difficulty  : ""
                    ));
                }
            }
            return out;
        }
    }

    /** 与 RankServer.RecordEntity 字段完全对应 */
    private static class OnlineRecord {
        String playerName;
        int    score;
        String difficulty;
        String formattedTime;
    }

    private void deleteRecordAndRefresh(GameRecord record) {
        new Thread(() -> {
            try {
                dao.deleteRecord(record);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    if (!isDestroyed) Toast.makeText(RankActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                });
                return;
            }
            runOnUiThread(() -> {
                if (!isDestroyed) {
                    loadData(currentDifficulty);
                    Toast.makeText(RankActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}