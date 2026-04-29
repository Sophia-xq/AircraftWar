package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class DifficultyActivity extends AppCompatActivity {

    private SwitchCompat switchMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        Button btnEasy   = findViewById(R.id.btn_easy);
        Button btnNormal = findViewById(R.id.btn_normal);
        Button btnHard   = findViewById(R.id.btn_hard);
        Button btnMulti  = findViewById(R.id.btn_multiplayer);
        Button btnRank   = findViewById(R.id.btn_rank);
        Button btnShop   = findViewById(R.id.btn_shop);
        switchMusic      = findViewById(R.id.switch_music);

        btnEasy.setOnClickListener(v   -> startGame("easy"));
        btnNormal.setOnClickListener(v -> startGame("normal"));
        btnHard.setOnClickListener(v   -> startGame("hard"));

        btnMulti.setOnClickListener(v ->
                startActivity(new Intent(this, MatchLobbyActivity.class)));

        btnRank.setOnClickListener(v -> {
            Intent i = new Intent(this, RankActivity.class);
            i.putExtra("initial_difficulty", "normal");
            startActivity(i);
        });

        btnShop.setOnClickListener(v ->
                startActivity(new Intent(this, ShopActivity.class)));
    }

    private void startGame(String difficulty) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("difficulty",   difficulty);
        intent.putExtra("music_on",     switchMusic.isChecked());
        intent.putExtra("multiplayer",  false);
        startActivity(intent);
    }
}