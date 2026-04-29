package edu.hitsz.dao;

import java.util.List;

public interface GameRecordDAO {

    /** 插入一条游戏记录 */
    void insertRecord(GameRecord record);

    /** 按难度查询前若干条排行榜记录 */
    List<GameRecord> queryTopRecords(String difficulty, int limit);


    /** 删除指定记录 */
    void deleteRecord(GameRecord record);

    void close();

}
