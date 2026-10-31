# 工时 · Miuix

一个用 Kotlin + Jetpack Compose 写的本地工时记录应用，UI 全部使用
[Miuix](https://github.com/compose-miuix-ui/miuix)（Compose Multiplatform 的 HyperOS 风格组件库）。

## 功能

| 页面 | 内容 |
| --- | --- |
| 计时 | 大标题时钟、开始/结束计时、项目名输入、今日总时长与目标进度、今日记录、补录对话框 |
| 记录 | 今天 / 本周 / 全部切换，合计与日均，按日期分组的记录列表，点击删除 |
| 设置 | 默认项目、每周目标（Slider 偏好组件）、动态取色、主题模式、CSV 导出、清空数据 |

- 计时状态落在 SharedPreferences，进程被杀后重开仍在计时
- 计时中自动保持屏幕常亮
- 主题支持跟随系统 / 浅色 / 深色，可叠加 Monet 动态取色
- 数据完全本地，不联网

## 技术栈

- Kotlin 2.4.21，AGP 9.5.0，Gradle 9.8.2
- `compileSdk = 37`（Miuix 0.9.4 的 AAR 元数据要求 `minCompileSdk=37`），`minSdk = 26`
- Miuix 0.9.4：`miuix-ui` / `miuix-icons` / `miuix-preference`

用到的 Miuix 组件：`Scaffold`、`TopAppBar`（大标题折叠）、`NavigationBar`、
`Card`、`TabRow`、`Button`、`TextButton`、`TextField`、`Icon`、
`LinearProgressIndicator`、`SmallTitle`、`HorizontalDivider`、`SnackbarHost`、
`OverlayDialog`、`MiuixTheme` + `ThemeController`，以及偏好组件
`SliderPreference`、`SwitchPreference`、`ArrowPreference`。

## 构建

用 Android Studio 直接打开工程目录，等待 Gradle Sync 后运行；或者：

```sh
./gradlew :app:assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

需要 JDK 17+ 与 Android SDK Platform 37。

## 代码结构

```
app/src/main/java/top/minis/worktime/
├── MainActivity.kt        入口
├── WorkEntry.kt           记录模型 + 时间格式化
├── WorkStore.kt           记录 / 计时状态 / 设置，SharedPreferences 持久化
└── ui/
    ├── Theme.kt           MiuixTheme + ThemeController
    ├── App.kt             Scaffold、大标题顶栏、底部导航、计时刷新
    ├── ClockScreen.kt     计时页
    ├── RecordsScreen.kt   记录页
    ├── SettingsScreen.kt  设置页
    └── Components.kt      记录卡片、空状态、补录对话框
```

## 说明

- 工程里的 Miuix 组件签名是逐条对照 `compose-miuix-ui/miuix` 0.9.4 源码核对的；
  但生成环境的 JVM 无法启动（PRoot 下 HotSpot 初始化失败），所以没有在这里实际跑过
  Gradle 构建。首次 Sync 若提示 AGP / Kotlin 版本可升级，按提示接受即可。
- 想改成别的业务（比如按客户、按项目计费），只需要换 `WorkEntry` 字段与
  `RecordsScreen` 的分组逻辑，UI 层不用动。
