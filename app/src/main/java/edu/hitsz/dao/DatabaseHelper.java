package edu.hitsz.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "game_records.db";
    // 升级到版本2：新增 coins 表
    private static final int DATABASE_VERSION = 2;

    // 原有：排行榜表
    public static final String TABLE_RECORDS = "records";
    public static final String COL_ID         = "id";
    public static final String COL_NAME       = "player_name";
    public static final String COL_SCORE      = "score";
    public static final String COL_TIME       = "record_time";
    public static final String COL_DIFFICULTY = "difficulty";

    // 新增：金币表（全局只有一行，player_id = "default"）
    public static final String TABLE_COINS    = "coins";
    public static final String COL_PLAYER_ID  = "player_id";
    public static final String COL_COINS      = "coins";

    // 新增：已购买道具表
    public static final String TABLE_BUFFS    = "buffs";
    public static final String COL_BUFF_NAME  = "buff_name";
    public static final String COL_BUFF_COUNT = "count";

    private static final String CREATE_RECORDS =
            "CREATE TABLE " + TABLE_RECORDS + " (" +
                    COL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME       + " TEXT NOT NULL, " +
                    COL_SCORE      + " INTEGER NOT NULL, " +
                    COL_TIME       + " INTEGER NOT NULL, " +
                    COL_DIFFICULTY + " TEXT NOT NULL)";

    private static final String CREATE_COINS =
            "CREATE TABLE " + TABLE_COINS + " (" +
                    COL_PLAYER_ID + " TEXT PRIMARY KEY, " +
                    COL_COINS     + " INTEGER NOT NULL DEFAULT 0)";

    private static final String CREATE_BUFFS =
            "CREATE TABLE " + TABLE_BUFFS + " (" +
                    COL_BUFF_NAME  + " TEXT PRIMARY KEY, " +
                    COL_BUFF_COUNT + " INTEGER NOT NULL DEFAULT 0)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_RECORDS);
        db.execSQL(CREATE_COINS);
        db.execSQL(CREATE_BUFFS);
        // 初始化金币记录
        db.execSQL("INSERT INTO " + TABLE_COINS + " VALUES('default', 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // 从版本1升级：只新增两张表，不删除已有排行榜数据
            db.execSQL(CREATE_COINS);
            db.execSQL(CREATE_BUFFS);
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_COINS + " VALUES('default', 0)");
        }
    }
}