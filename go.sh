#!/bin/sh
#ant install && adb shell 'am start -n your.package.name/.YourActivityName'
ant install && adb shell 'am start -n com.morelightmorelight.upfuckr/.upfuckr'
adb logcat
