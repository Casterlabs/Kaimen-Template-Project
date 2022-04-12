#!/bin/bash

# Reset/clear the dist folder.
rm -rf dist/*
mkdir -p dist

KAIMEN_VERSION="1a1b86c"
BUNDLE_ID="com.example.kaimenproject"
APP_NAME="Example Project"
APP_VERSION="1.0.0"
JAVA_VERSION="JAVA11" # This is the only version supported by Kaimen.

echo ""
echo "Compiling."
echo ""

mvn install

echo ""
echo "Completing packaging of application."
echo ""

# MacOSX
java -jar ProjectBuilder.jar -os MACOSX -arch AMD64 -wi WEBKIT -v "$APP_VERSION" -n "$APP_NAME" -jv "$JAVA_VERSION" -kv "$KAIMEN_VERSION" -cp target/App.jar -i icon.icns -id "$BUNDLE_ID"
echo ""

# Windows
java -jar ProjectBuilder.jar -os WINDOWS -arch AMD64 -wi CHROMIUM_EMBEDDED_FRAMEWORK -v "$APP_VERSION" -n "$APP_NAME" -jv "$JAVA_VERSION" -kv "$KAIMEN_VERSION" -cp target/App.jar -i icon.ico
echo ""

# Linux
java -jar ProjectBuilder.jar -os LINUX -arch AMD64 -wi CHROMIUM_EMBEDDED_FRAMEWORK -v "$APP_VERSION" -n "$APP_NAME" -jv "$JAVA_VERSION" -kv "$KAIMEN_VERSION" -cp target/App.jar
echo ""
