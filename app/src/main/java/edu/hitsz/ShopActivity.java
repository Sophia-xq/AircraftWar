package edu.hitsz;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.dao.CoinDAO;

/**
 * 金币商店 Activity
 *
 * 从 DifficultyActivity 点「商店」按钮进入。
 * 展示当前金币数，可购买开局 Buff。
 * 布局文件：activity_shop.xml（见同目录）
 */
public class ShopActivity extends AppCompatActivity {

    private CoinDAO coinDAO;
    private TextView tvCoins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        coinDAO = new CoinDAO(this);

        tvCoins = findViewById(R.id.tv_coin_count);
        Button btnBuyShield    = findViewById(R.id.btn_buy_shield);
        Button btnBuyDoubleGun = findViewById(R.id.btn_buy_double_gun);
        Button btnBack         = findViewById(R.id.btn_back_shop);

        refreshCoins();

        btnBuyShield.setOnClickListener(v -> {
            boolean ok = coinDAO.buyBuff("shield", CoinDAO.PRICE_SHIELD);
            if (ok) {
                Toast.makeText(this,
                        "购买成功！下局开始时自动生效（库存: "
                                + coinDAO.getBuffCount("shield") + "）", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "金币不足！需要 " + CoinDAO.PRICE_SHIELD + " 金币", Toast.LENGTH_SHORT).show();
            }
            refreshCoins();
        });

        btnBuyDoubleGun.setOnClickListener(v -> {
            boolean ok = coinDAO.buyBuff("double_gun", CoinDAO.PRICE_DOUBLE_GUN);
            if (ok) {
                Toast.makeText(this,
                        "购买成功！下局开始时自动生效（库存: "
                                + coinDAO.getBuffCount("double_gun") + "）", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "金币不足！需要 " + CoinDAO.PRICE_DOUBLE_GUN + " 金币", Toast.LENGTH_SHORT).show();
            }
            refreshCoins();
        });

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCoins(); // 每次回到商店刷新金币显示
    }

    private void refreshCoins() {
        int coins = coinDAO.getCoins();
        int shieldCount    = coinDAO.getBuffCount("shield");
        int doubleGunCount = coinDAO.getBuffCount("double_gun");
        tvCoins.setText("💰 金币：" + coins);

        // 更新按钮文字，显示库存
        Button btnShield    = findViewById(R.id.btn_buy_shield);
        Button btnDoubleGun = findViewById(R.id.btn_buy_double_gun);
        btnShield.setText("开局护盾  " + CoinDAO.PRICE_SHIELD + "金币（库存:" + shieldCount + "）");
        btnDoubleGun.setText("双倍火力  " + CoinDAO.PRICE_DOUBLE_GUN + "金币（库存:" + doubleGunCount + "）");
    }
}