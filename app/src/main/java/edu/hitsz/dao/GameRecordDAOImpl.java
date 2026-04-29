package edu.hitsz.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


public class GameRecordDAOImpl implements GameRecordDAO {
    private static GameRecordDAOImpl instance;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    // 单例模式，保证全应用只有一个数据库连接
    public static synchronized GameRecordDAOImpl getInstance(Context context) {
        if (instance == null) {
            instance = new GameRecordDAOImpl(context.getApplicationContext());
        } else {
            // 修复：如果数据库被意外关闭，重新打开
            instance.ensureOpen();
        }
        return instance;
    }

    private GameRecordDAOImpl(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    // 修复：确保数据库连接是打开的
    private synchronized void ensureOpen() {
        if (db == null || !db.isOpen()) {
            db = dbHelper.getWritableDatabase();
        }
    }

    @Override
    public synchronized void insertRecord(GameRecord record) {
        ensureOpen();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NAME, record.getPlayerName());
        values.put(DatabaseHelper.COL_SCORE, record.getScore());
        values.put(DatabaseHelper.COL_DIFFICULTY, record.getDifficulty());
        values.put(DatabaseHelper.COL_TIME, System.currentTimeMillis());
        long id = db.insert(DatabaseHelper.TABLE_RECORDS, null, values);
        record.setId((int) id);
    }

    @Override
    public synchronized List<GameRecord> queryTopRecords(String difficulty, int limit) {
        ensureOpen();
        List<GameRecord> records = new ArrayList<>();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_RECORDS,
                null,
                DatabaseHelper.COL_DIFFICULTY + " = ?",
                new String[]{difficulty},
                null, null,
                DatabaseHelper.COL_SCORE + " DESC, " + DatabaseHelper.COL_TIME + " ASC",
                String.valueOf(limit)
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
                int score = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SCORE));
                String diff = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DIFFICULTY));
                long timeMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TIME));

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                        "yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
                String formattedTime = sdf.format(new java.util.Date(timeMillis));

                records.add(new GameRecord(id, name, score, timeMillis, formattedTime, diff));
            }
            cursor.close();
        }
        return records;
    }

    @Override
    public synchronized void deleteRecord(GameRecord record) {
        ensureOpen();
        db.delete(DatabaseHelper.TABLE_RECORDS,
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(record.getId())});
    }

    // 修复：单例不应该真正关闭数据库，close() 改为空实现或仅供 Application 级别调用
    // RankActivity 不再调用此方法
    @Override
    public synchronized void close() {
        // 单例生命周期与 App 一致，不在 Activity 里关闭
        // 如需在 App 退出时释放，可在 Application.onTerminate() 中调用 forceClose()
    }

    // 仅供 Application 级别真正销毁时使用
    public synchronized void forceClose() {
        if (db != null && db.isOpen()) {
            db.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
        instance = null;
    }
}