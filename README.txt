
To run jythonroid on Android 2.2:

  wget http://github.com/downloads/rjolly/jythonroid/Jythonroid.apk
  adb install Jythonroid.apk

To build jythonroid on android sdk:

  android update project -t android-8 -p .
  cp ../../android-sdk-linux/platform-tools/lib/dx.jar libs/
  ant release

