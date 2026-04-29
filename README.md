# ✈️ AircraftWar — Android 飞机大战

> A feature-rich Android shoot-em-up with real-time multiplayer, online leaderboard, coin shop, and particle effects.

---

## 📱 Screenshots

> *(游戏截图：主界面 / 排行榜 / 游戏中 / 金币商店 / 匹配大厅)*
<img width="343" height="752" alt="截屏2026-04-29 17 09 52" src="https://github.com/user-attachments/assets/5b95a79c-5b83-49b7-b867-dd9c5ff396e0" />
<img width="345" height="770" alt="截屏2026-04-29 17 10 56" src="https://github.com/user-attachments/assets/49742dee-67f7-4017-843e-5e86e91ae57d" />
<img width="350" height="758" alt="截屏2026-04-29 17 10 33" src="https://github.com/user-attachments/assets/272ff1d7-a27a-4a46-922e-dae86c0977e6" />
<img width="343" height="755" alt="截屏2026-04-29 17 11 07" src="https://github.com/user-attachments/assets/51f486e7-19dd-4748-be8f-ff6d435bb292" />
<img width="346" height="755" alt="截屏2026-04-29 17 11 20" src="https://github.com/user-attachments/assets/e9fa3009-5f11-4dc0-aca7-4bfcf3cc16bb" />

---

## 🎮 Features

### 核心玩法
- 三档难度：简单 / 普通 / 困难，敌机数量、生成速度、Boss 阈值各有不同
- 多种敌机类型：普通机 / 精英机 / 超级精英机 / Boss，击杀概率掉落道具
- 道具系统：血包、火力升级、超级火力、炸弹（一键清屏 + 粒子爆炸效果）

### 联机对战
- 匹配大厅：创建 / 刷新 / 加入房间，无需手动输入 IP
- 实时分数同步：双方分数实时显示在对方屏幕上
- 双方死亡后自动结算，显示胜负结果

### 排行榜
- 本地排行榜：SQLite 持久化存储，按难度筛选，支持删除记录
- 在线排行榜：HTTP 上传 / 拉取 Top 20，一键切换本地 / 全球视图
- 前三名金银铜徽章 + CardView 卡片式布局

### 金币商店
- 击杀不同敌机获得不同金币奖励
- 可购买开局 Buff：护盾（抵挡一次致命伤害）/ 双倍火力（持续 15 秒）
- 金币跨局持久化存储

### 视觉效果
- 粒子爆炸系统：敌机死亡时向四周散射彩色粒子，逐渐透明消失
- 护盾激活时英雄周围显示蓝色光圈
- 背景图 + 半透明遮罩的启动界面与排行榜界面

---

## 🏗️ Architecture

```
Android Client
├── GameView (SurfaceView)     游戏主循环、碰撞检测、粒子系统
├── MatchLobbyActivity         匹配大厅
├── RankActivity               本地 + 在线排行榜切换
├── ShopActivity               金币商店
└── dao/
    ├── GameRecordDAOImpl      本地排行榜（SQLite）
    └── CoinDAO                金币与 Buff 管理（SQLite）

Java Server (同一局域网)
├── BattleServer (:9999)       Socket 中转服务器（联机对战）
└── RankServer   (:8080)       HTTP 服务器（在线排行榜 + 房间管理）
    └── In-memory DB           房间状态管理（自动超时清理）
```

---

## 🚀 Quick Start

### 环境要求
- Android Studio Hedgehog 或以上
- Android SDK 33+
- Java 21
- 同一局域网内的电脑运行服务器

### 运行服务器

```bash
# 编译项目后，在两个终端窗口分别运行：

# 窗口 1 — 在线排行榜 + 房间管理
java -cp "MyServer/build/classes/java/main:~/.gradle/caches/.../gson-2.9.1.jar" \
     com.example.myserver.RankServer

# 窗口 2 — 联机对战中转
java -cp "MyServer/build/classes/java/main" \
     com.example.myserver.BattleServer
```

### 配置客户端

在 `MatchLobbyActivity.java` 第 17 行修改服务器地址：

```java
// 模拟器用 10.0.2.2，真机改为电脑局域网 IP
public static final String HTTP_SERVER = "http://10.0.2.2:8080";
```

### 运行 App
1. Android Studio 中 Run → AircraftWar（app module）
2. 选择模拟器或连接真机
3. 享受游戏 🎮

---

## 🛠️ Tech Stack

| 层级 | 技术 |
|------|------|
| UI | SurfaceView · CardView · ListView · SwitchCompat |
| 本地存储 | SQLite · SQLiteOpenHelper · DAO 模式 |
| 网络 | OkHttp · HttpURLConnection · Socket |
| 服务端 | Java HttpServer (com.sun.net) · ServerSocket |
| 序列化 | Gson |
| 架构模式 | 单例模式 · 工厂模式 · 观察者模式 · DAO 模式 |

---

## 📂 Project Structure

```
AircraftWar/
├── app/src/main/java/edu/hitsz/
│   ├── aircraft/          各类飞机实体
│   ├── bullet/            子弹实体
│   ├── prop/              道具实体
│   ├── factory/           工厂类
│   ├── effect/            粒子系统
│   ├── dao/               数据访问层
│   └── *.java             Activity & GameView
├── app/src/main/res/
│   ├── layout/            界面布局
│   └── drawable/          图片资源
└── MyServer/src/main/java/com/example/myserver/
    ├── BattleServer.java  Socket 服务器
    └── RankServer.java    HTTP 服务器
```

---

## 📄 License

This project is developed as a course assignment for **Software Construction Practice**, Harbin Institute of Technology (Shenzhen), Spring 2026.
