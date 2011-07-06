#!/bin/sh
#ant install && adb shell 'am start -n your.package.name/.YourActivityName'
#ant install && adb shell 'am start -n com.morelightmorelight.upfuckr/.galleries' && adb logcat
ant install && adb shell 'am start -n com.android.gallery/com.android.camera.GalleryPicker' && adb logcat
