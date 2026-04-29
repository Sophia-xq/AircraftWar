package edu.hitsz;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化图片管理器（必须在加载图片前调用）
        ImageManager.init(this);
        // 创建 GameView，参数：context, 难度, 音乐开关
        GameView gameView = new GameView(this, "normal", true);
        setContentView(gameView);
    }
}