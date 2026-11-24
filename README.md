# AircraftWar2026 · 软件构造实践实验二

这是从 [Q-78/AircraftWar](https://github.com/Q-78/AircraftWar) 的 Windows/Swing 版迁移出的 Android Java 工程。当前固定为原工程的**简单模式**；难度选择页面、音效、排行榜、联网功能不在实验二范围内。

## 已迁移

- 原工程 `src/images` 下的图片已复制到 `app/src/main/res/drawable`，文件名按 Android 规则改为小写；绘制前由 `BitmapFactory.decodeResource` 加载一次。
- `MainActivity` 替代 Swing 入口，`BaseGame` 用 `SurfaceView` 子线程绘制，触屏代替鼠标。
- `GameWorld` 迁移原工程 `Game.action()` 的生成→射击→移动→碰撞→清理流程；保留 `AbstractFlyingObject.crash()` 的中心坐标与飞机纵向碰撞区规则。
- 简单模式沿用 512×768 逻辑尺寸、40 ms 更新周期、最多 4 架敌机、每 28 帧生成一次、每 18 帧英雄射击一次、敌机 70%/20%/10% 类型概率，并依据原工厂设置敌机速度与血量。英雄血量、子弹伤害、基础分数和连击加成也取自原工程。

本实验阶段没有接入原工程道具、技能、Boss 和持久化模块；图片虽已全部导入，游戏目前只绘制本阶段所需的背景、三种敌机、英雄机与子弹。

## 运行

在 Android Studio 打开本目录，使用 JDK 17 和已安装的 Android SDK；运行 `app`。最低 Android API 30。手指拖动英雄机，射击自动进行。

`apk/AircraftWar2026-debug.apk` 是使用本机 Android SDK 工具生成的调试包。工程当前没有 Gradle Wrapper，Android Studio 首次打开时需要获取 Gradle 和 Android Gradle Plugin 才能使用标准构建流程。本机网络未能完成该依赖下载，已用 SDK 工具验证资源与 Java 编译、APK 签名；已在 API 37 模拟器中安装并启动；触屏操控仍需人工验收。
