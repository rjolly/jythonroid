
To run jythonroid on the android-8 emulator target (Android 2.2):

  adb -e install Jythonroid.apk
  adb -e shell
  cd /data/app
  dalvikvm -classpath org.python.util-1.apk org.python.util.jython [script.py]

To build jythonroid on android-sdk_r18:

  android update project -t android-8 -p jythonroid
  cd jythonroid
  ant release

