package edu.hitsz.basic;

import android.graphics.Bitmap;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.ImageManager;


/**
 * 可飞行对象的父类（Android 版）
 *
 * @author hitsz
 */
public abstract class AbstractFlyingObject {

    // 屏幕尺寸（由 GameView 或 MainActivity 初始化时设置）
    private static int screenWidth = 1080;   // 默认值，实际会被覆盖
    private static int screenHeight = 1920;

    /**
     * 设置屏幕尺寸，必须在任何飞行对象使用前调用（例如在 GameView 的 surfaceChanged 中）
     */
    public static void setScreenSize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }

    // 坐标、速度
    protected int locationX;
    protected int locationY;
    protected int speedX;
    protected int speedY;

    // 图片（Android Bitmap）
    protected Bitmap image = null;

    // 宽高（根据图片自动获取）
    protected int width = -1;
    protected int height = -1;

    // 有效标记
    protected boolean isValid = true;

    public AbstractFlyingObject() {
    }

    public AbstractFlyingObject(int locationX, int locationY, int speedX, int speedY) {
        this.locationX = locationX;
        this.locationY = locationY;
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    /**
     * 根据速度移动，并处理边界（出界则消失，横向反弹）
     */
    public void forward() {
        locationX += speedX;
        locationY += speedY;

        // 横向边界：反弹（仅对敌机/英雄等有效，子弹可单独处理）
        if (locationX <= 0 || locationX >= screenWidth) {
            speedX = -speedX;
        }

        // 纵向出界：消失
        if (locationY <= -getHeight() || locationY >= screenHeight + getHeight()) {
            vanish();
        }

        // 横向出界：消失（例如子弹飞出屏幕左右）
        if (locationX <= -getWidth() || locationX >= screenWidth + getWidth()) {
            vanish();
        }
    }

    /**
     * 碰撞检测（逻辑与原版一致，仅数据类型适配）
     */
    public boolean crash(AbstractFlyingObject flyingObject) {
        int factor = this instanceof AbstractAircraft ? 2 : 1;
        int fFactor = flyingObject instanceof AbstractAircraft ? 2 : 1;

        int x = flyingObject.getLocationX();
        int y = flyingObject.getLocationY();
        int fWidth = flyingObject.getWidth();
        int fHeight = flyingObject.getHeight();

        return x + (fWidth + getWidth()) / 2 > locationX
                && x - (fWidth + getWidth()) / 2 < locationX
                && y + (fHeight / fFactor + getHeight() / factor) / 2 > locationY
                && y - (fHeight / fFactor + getHeight() / factor) / 2 < locationY;
    }

    // ----- getter / setter -----
    public int getLocationX() {
        return locationX;
    }

    public int getLocationY() {
        return locationY;
    }

    public void setLocation(double locationX, double locationY) {
        this.locationX = (int) locationX;
        this.locationY = (int) locationY;
    }

    public void setLocationX(int x) {
        this.locationX = x;
    }

    public void setLocationY(int y) {
        this.locationY = y;
    }

    public int getSpeedY() {
        return speedY;
    }

    /**
     * 获取图片（Android Bitmap）
     */
    public Bitmap getImage() {
        if (image == null) {
            image = ImageManager.get(this);
        }
        return image;
    }

    public int getWidth() {
        if (width == -1 && getImage() != null) {
            width = getImage().getWidth();
        }
        return width;
    }

    public int getHeight() {
        if (height == -1 && getImage() != null) {
            height = getImage().getHeight();
        }
        return height;
    }

    public boolean notValid() {
        return !isValid;
    }

    public void vanish() {
        isValid = false;
    }
}