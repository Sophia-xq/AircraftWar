package edu.hitsz.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 金币 & 道具 DAO
 *
 * 使用方式（复用 GameRecordDAOImpl 的单例 db）：
 *   CoinDAO coinDAO = new CoinDAO(context);
 *   int coins = coinDAO.getCoins();
 *   coinDAO.addCoins(50);
 *   boolean ok = coinDAO.buyBuff("shield", 100);
 */
public class CoinDAO {

    // 道具价格常量
    public static final int PRICE_SHIELD    = 100;  // 开局护盾（一次性）
    public static final int PRICE_DOUBLE_GUN = 150; // 开局双倍火力

    // 各类敌机击杀金币奖励
    public static final int COIN_MOB        = 5;
    public static final int COIN_ELITE      = 15;
    public static final int COIN_SUPER_ELITE = 25;
    public static final int COIN_BOSS       = 80;

    private final SQLiteDatabase db;

    public CoinDAO(Context context) {
        db = new DatabaseHelper(context).getWritableDatabase();
    }

    /** 获取当前金币数 */
    public int getCoins() {
        Cursor c = db.query(DatabaseHelper.TABLE_COINS, null,
                DatabaseHelper.COL_PLAYER_ID + "=?",
                new String[]{"default"}, null, null, null);
        if (c != null && c.moveToFirst()) {
            int coins = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_COINS));
            c.close();
            return coins;
        }
        if (c != null) c.close();
        return 0;
    }

    /** 增加金币（击杀敌机时调用） */
    public synchronized void addCoins(int amount) {
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_COINS +
                " SET " + DatabaseHelper.COL_COINS + " = " + DatabaseHelper.COL_COINS + " + " + amount +
                " WHERE " + DatabaseHelper.COL_PLAYER_ID + " = 'default'");
    }

    /**
     * 购买道具
     * @param buffName 道具名称（"shield" / "double_gun"）
     * @param price    价格
     * @return true=购买成功，false=金币不足
     */
    public synchronized boolean buyBuff(String buffName, int price) {
        int current = getCoins();
        if (current < price) return false;

        // 扣除金币
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_COINS +
                " SET " + DatabaseHelper.COL_COINS + " = " + DatabaseHelper.COL_COINS + " - " + price +
                " WHERE " + DatabaseHelper.COL_PLAYER_ID + " = 'default'");

        // 增加道具数量
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_BUFF_NAME, buffName);
        cv.put(DatabaseHelper.COL_BUFF_COUNT, 1);
        db.insertWithOnConflict(DatabaseHelper.TABLE_BUFFS, null, cv,
                SQLiteDatabase.CONFLICT_IGNORE);
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_BUFFS +
                " SET " + DatabaseHelper.COL_BUFF_COUNT + " = " + DatabaseHelper.COL_BUFF_COUNT + " + 1" +
                " WHERE " + DatabaseHelper.COL_BUFF_NAME + " = '" + buffName + "'");
        return true;
    }

    /**
     * 消耗一个道具（进入游戏时检查并扣除）
     * @return true=成功消耗（有库存），false=没有该道具
     */
    public synchronized boolean consumeBuff(String buffName) {
        int count = getBuffCount(buffName);
        if (count <= 0) return false;
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_BUFFS +
                " SET " + DatabaseHelper.COL_BUFF_COUNT + " = " + DatabaseHelper.COL_BUFF_COUNT + " - 1" +
                " WHERE " + DatabaseHelper.COL_BUFF_NAME + " = '" + buffName + "'");
        return true;
    }

    /** 获取某道具的库存数量 */
    public int getBuffCount(String buffName) {
        Cursor c = db.query(DatabaseHelper.TABLE_BUFFS, null,
                DatabaseHelper.COL_BUFF_NAME + "=?",
                new String[]{buffName}, null, null, null);
        if (c != null && c.moveToFirst()) {
            int count = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_BUFF_COUNT));
            c.close();
            return count;
        }
        if (c != null) c.close();
        return 0;
    }
}