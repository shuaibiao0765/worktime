#!/data/data/com.termux/files/usr/bin/bash
# 在 Termux（Android 手机）里一键构建本项目的 debug APK。
# 用法：把工程目录放到 ~/miuix-worktime，然后 bash termux-build.sh

set -e

SDK="$HOME/android-sdk"
CMDLINE_VER="13114758"

echo "==> 1/5 安装依赖"
pkg update -y
pkg install -y openjdk-17 wget unzip

echo "==> 2/5 安装 Android 命令行工具"
if [ ! -d "$SDK/cmdline-tools/latest" ]; then
    mkdir -p "$SDK/cmdline-tools"
    cd /tmp
    wget -q "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_VER}_latest.zip" -O cmdline.zip
    unzip -q cmdline.zip -d "$SDK/cmdline-tools"
    mv "$SDK/cmdline-tools/cmdline-tools" "$SDK/cmdline-tools/latest"
fi

export ANDROID_HOME="$SDK"
export PATH="$SDK/cmdline-tools/latest/bin:$PATH"

echo "==> 3/5 接受许可并安装 platform 37"
yes | sdkmanager --licenses > /dev/null
sdkmanager "platforms;android-37" "platform-tools"

echo "==> 4/5 写入 local.properties"
cd "$HOME/miuix-worktime"
echo "sdk.dir=$ANDROID_HOME" > local.properties
chmod +x gradlew

echo "==> 5/5 开始构建（首次会下载 Gradle 与依赖，比较慢）"
./gradlew :app:assembleDebug --no-daemon

APK="app/build/outputs/apk/debug/app-debug.apk"
cp -f "$APK" "$HOME/storage/downloads/miuix-worktime.apk" 2>/dev/null \
    && echo "APK 已复制到 下载/miuix-worktime.apk" \
    || echo "APK 位于: $(pwd)/$APK"

echo "完成。"
