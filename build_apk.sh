#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLE_BIN="/home/wdwi/.gradle/wrapper/dists/gradle-8.14.3-all/10utluxaxniiv4wxiphsi49nj/gradle-8.14.3/bin/gradle"

if [ -f "$DIR/android/gradlew" ]; then
    GRADLE_CMD="$DIR/android/gradlew"
else
    GRADLE_CMD="$GRADLE_BIN"
fi

cd "$DIR/android"
echo "Building Nexora Android APK..."
"$GRADLE_CMD" assembleDebug

cp "$DIR/android/app/build/outputs/apk/debug/app-debug.apk" "$DIR/Nexora.apk"
cp "$DIR/android/app/build/outputs/apk/debug/app-debug.apk" "$DIR/ControlPC.apk"
cp "$DIR/android/app/build/outputs/apk/debug/app-debug.apk" "$DIR/NexPC.apk"

echo "============================================="
echo "✅ Build Complete!"
echo "APK location: $DIR/Nexora.apk"
echo "Also copied to: $DIR/ControlPC.apk & $DIR/NexPC.apk"
echo "============================================="
