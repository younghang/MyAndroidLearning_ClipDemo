# Build Freeze Notes

这个项目按“能编译就不动大版本”的方式维护。

## 当前冻结版本

- Gradle Wrapper: `gradle/wrapper/gradle-wrapper.properties`
  - `distributionUrl=...gradle-7.5-all.zip`
- Android Gradle Plugin: 根目录 `build.gradle`
  - `com.android.tools.build:gradle:7.4.2`
- App SDK: `app/build.gradle`
  - `compileSdk 33`
  - `targetSdkVersion 30`
  - `minSdkVersion 19`

这些是构建壳子；不要因为 Android Studio 提示就随手升级。

## 平时怎么编译

优先使用项目自带 wrapper：

```powershell
.\gradlew.bat :app:assembleDebug
```

如果依赖已经缓存好，也可以离线编译：

```powershell
.\gradlew.bat :app:assembleDebug --offline
```

## Android Studio 里避免自动升级

打开项目后，如果看到下面这些提示，默认不要点升级：

- Upgrade Android Gradle Plugin
- Upgrade Gradle
- Migrate to Version Catalog
- Update compileSdk / targetSdk
- Convert build files / migrate DSL

如果 Android Studio 还是改了文件，优先检查这些文件的 diff：

```powershell
git diff -- build.gradle gradle/wrapper/gradle-wrapper.properties app/build.gradle chart/build.gradle listview/build.gradle gradle.properties
```

只要这些文件没有被改到新大版本，源码一般不会被迫跟着大迁移。

## 版本概念

- Gradle: 构建工具本体，负责跑任务。
- Android Gradle Plugin: Android 打包插件，负责把 Java/XML/res 打成 APK。
- compileSdk: 用哪个 Android SDK 编译。通常可以比代码旧一些。
- targetSdk: 告诉系统“我适配到了哪个 Android 行为版本”。这个最容易影响运行行为。
- AndroidX/Material 等依赖: 代码里直接用的库。老项目不建议随意升大版本。

## 维护原则

- 不使用 `+`、`latest`、`SNAPSHOT` 这类浮动依赖版本。
- 依赖库能不升就不升。
- `targetSdkVersion` 先保持 `30`。
- Android 11 及以上如果文件读写出问题，只局部改存储相关代码。
- `.idea/`、`*.iml`、`caches/`、`daemon/`、`wrapper/` 这类本机生成文件不进 Git。
