
To run jythonroid on the android-8 emulator target (Android 2.2):

  adb -e push bin/Jythonroid.apk /data/app/Jythonroid.apk
  adb -e shell
  cd /data/app
  dalvikvm -classpath Jythonroid.apk org.python.util.jython

To build jythonroid on android-sdk_r06:

  android update project -t android-8 -p jythonroid
  cd jythonroid
  ant release

