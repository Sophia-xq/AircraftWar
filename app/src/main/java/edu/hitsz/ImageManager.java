package edu.hitsz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.prop.BloodProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.FireProp;
import edu.hitsz.prop.SuperFireProp;

import java.util.HashMap;
import java.util.Map;

/**
 * Android 版图片管理器
 * 使用前必须调用 init(Context) 传入应用上下文
 */
public class ImageManager {

    private static Context appContext;
    private static final Map<String, Bitmap> CLASSNAME_IMAGE_MAP = new HashMap<>();

    // 静态图片字段，保持与原代码兼容
    public static Bitmap BACKGROUND_IMAGE;
    public static Bitmap BACKGROUND_IMAGE3;
    public static Bitmap BACKGROUND_IMAGE5;
    public static Bitmap HERO_IMAGE;
    public static Bitmap HERO_BULLET_IMAGE;
    public static Bitmap ENEMY_BULLET_IMAGE;
    public static Bitmap MOB_ENEMY_IMAGE;
    public static Bitmap ELITE_ENEMY_IMAGE;
    public static Bitmap SUPER_ELITE_ENEMY_IMAGE;
    public static Bitmap BOSS_ENEMY_IMAGE;
    public static Bitmap PROP_BLOOD_IMAGE;
    public static Bitmap PROP_BOMB_IMAGE;
    public static Bitmap PROP_BULLET_IMAGE;
    public static Bitmap PROP_BULLET_PLUS_IMAGE;

    /**
     * 必须在应用启动时调用（例如在 MainActivity.onCreate 中）
     * @param context 任意 Context
     */
    public static void init(Context context) {
        if (appContext != null) return;
        appContext = context.getApplicationContext();

        // 加载图片（资源 ID 需与你的 drawable 文件名匹配）
        BACKGROUND_IMAGE = loadBitmap(R.drawable.bg);
        BACKGROUND_IMAGE3 = loadBitmap(R.drawable.bg3);
        BACKGROUND_IMAGE5 = loadBitmap(R.drawable.bg5);
        HERO_IMAGE = loadBitmap(R.drawable.hero);
        MOB_ENEMY_IMAGE = loadBitmap(R.drawable.mob);
        ELITE_ENEMY_IMAGE = loadBitmap(R.drawable.elite);
        SUPER_ELITE_ENEMY_IMAGE = loadBitmap(R.drawable.elite_plus);      // 文件名 elite_plus.png
        BOSS_ENEMY_IMAGE = loadBitmap(R.drawable.boss);
        HERO_BULLET_IMAGE = loadBitmap(R.drawable.bullet_hero);
        ENEMY_BULLET_IMAGE = loadBitmap(R.drawable.bullet_enemy);
        PROP_BLOOD_IMAGE = loadBitmap(R.drawable.prop_blood);
        PROP_BOMB_IMAGE = loadBitmap(R.drawable.prop_bomb);
        PROP_BULLET_IMAGE = loadBitmap(R.drawable.prop_bullet);
        PROP_BULLET_PLUS_IMAGE = loadBitmap(R.drawable.prop_bullet_plus);

        // 建立类名到图片的映射
        CLASSNAME_IMAGE_MAP.put(HeroAircraft.class.getName(), HERO_IMAGE);
        CLASSNAME_IMAGE_MAP.put(MobEnemy.class.getName(), MOB_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EliteEnemy.class.getName(), ELITE_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(SuperEliteEnemy.class.getName(), SUPER_ELITE_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BossEnemy.class.getName(), BOSS_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(HeroBullet.class.getName(), HERO_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EnemyBullet.class.getName(), ENEMY_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BloodProp.class.getName(), PROP_BLOOD_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BombProp.class.getName(), PROP_BOMB_IMAGE);
        CLASSNAME_IMAGE_MAP.put(FireProp.class.getName(), PROP_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(SuperFireProp.class.getName(), PROP_BULLET_PLUS_IMAGE);
    }

    private static Bitmap loadBitmap(int resId) {
        if (appContext == null) {
            throw new IllegalStateException("ImageManager 未初始化，请先调用 init(Context)");
        }
        return BitmapFactory.decodeResource(appContext.getResources(), resId);
    }

    public static Bitmap get(String className) {
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static Bitmap get(Object obj) {
        if (obj == null) return null;
        return get(obj.getClass().getName());
    }

    /**
     * 原 load(String path) 在 Android 中不再支持文件路径读取。
     * 如果原代码中调用此方法加载背景图，请改用资源 ID。
     * 这里保留方法但抛出异常，避免编译错误。
     */
    @Deprecated
    public static Bitmap load(String path) {
        throw new UnsupportedOperationException("Android 中请使用资源 ID 加载图片，不要使用文件路径");
    }
}