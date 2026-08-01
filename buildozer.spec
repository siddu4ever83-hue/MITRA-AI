[app]
title = MITRA AI
package.name = mitraai
package.domain = org.mitra
source.dir = .
source.include_exts = py,png,jpg,jpeg,kv,atlas,json,txt
version = 1.0
requirements = python3,kivy==2.2.1,requests
orientation = portrait
fullscreen = 0
android.api = 33
android.minapi = 21
android.ndk = 25b
android.build_tools_version = 33.0.0
android.archs = arm64-v8a
android.allow_backup = True
android.permissions = INTERNET
android.sdk_path = /home/runner/.buildozer/android/platform/android-sdk
android.ndk_path = /home/runner/.buildozer/android/platform/android-ndk-r25b

[buildozer]
log_level = 2
warn_on_root = 1
