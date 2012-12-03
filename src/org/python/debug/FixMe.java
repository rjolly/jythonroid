package org.python.debug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexClassLoader;

import com.android.dx.dex.cf.CfOptions;
import com.android.dx.dex.cf.CfTranslator;
import com.android.dx.dex.file.ClassDefItem;
import com.android.dx.dex.file.DexFile;
import com.android.dx.dex.DexOptions;

/**
 * That's the main Code that fix the differences between the dalvik and standard
 * jvm
 * 
 * @author Administrator
 */
public class FixMe {
	public static String tmpdirpath = "/data/data/jackpal.androidterm/jythonroid/";
	public static boolean isinitialized = false;

	public static boolean initialize() {
		// create the tmp dir
		File tdp = new File(tmpdirpath);
		if (!tdp.exists()) {
			tdp.mkdir();
		} else {
			if (!tdp.isDirectory()) {
				return false;
			}
		}
		isinitialized = true;
		return true;
	}

	/**
	 * the Class.getDeclaringClass() is missing in Android, so it will try
	 * the official method, and use the fix code when failed
	 * 
	 * @param Class c
	 * @return Class cls
	 * @throws ClassNotFoundException
	 */
	public static Class getDeclaringClass(Class c)
			throws ClassNotFoundException {
		try {
			// this will work when google fix the bug
			Class result = c.getDeclaringClass();
			return result;
		} catch (Exception e) {
			String[] elements = c.getName().replace('.', '/').split("\\$");
			String name = elements[0];
			for (int i = 1; i < elements.length - 1; i++) {
				name += "$" + elements[i];
			}
			if (elements.length == 1) {
				return null;
			} else {
				return getClassByName(name);

			}
		}
	}

	/**
	 * get class by name from the default apk file
	 * 
	 * @param classname
	 * @return
	 */
	public static Class getClassByName(String classname) {
		try {
			return Class.forName(classname.replace('/', '.'));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * get a class by apk file name and class name need recursion as the
	 * Class instance can not get when the super class is not inferred;
	 * Example:
	 * <code>
	 * Class<c>=getClassByName("/tmp/fuck.apk","org/freehust/pystring");
	 * </code>
	 * 
	 * @param String
	 *            filename,String classname
	 * @return Class
	 */
	public static Class getClassByName(String filename, String classname) {
		try {
			DexClassLoader cl = new DexClassLoader(filename, tmpdirpath, null, ClassLoader.getSystemClassLoader());
			Class s = cl.loadClass(classname);
			return s;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(classname + " in " + filename, e);
		}
	}

	/**
	 * need recursion as the Class instance can not get when the super
	 * class is not infered;
	 * <code>
	 * method dothis():
	 * if Class.hasSuperClass:
	 *    dothis(Class.getSuperClass)
	 * else:
	 *    Class.forname();
	 * </code>
	 * 
	 * @param classname
	 * @return
	 * @throws ClassNotFoundException
	 */
	//    public static Class classForName(String classname)
	//            throws ClassNotFoundException {
	//        Class b = Class.forName(classname);
	//        Class tmp = b.getSuperclass();
	//        while (tmp != null) {
	//            tmp = tmp.getSuperclass();
	//        }
	//        return b;
	//    }
	/**
	 * get inner class by name example:
	 * getInnerClassByName(Shit,"org.freehust.Shit$shit");
	 * 
	 * @param c
	 * @param name
	 * @return Class
	 */
	public static Class getInnerClassByName(Class c, String name) {
		Class[] inners = c.getClasses();
		for (int i = 0; i < inners.length; i++) {
			if (inners[i].getName().equals(name)) {
				return inners[i];
			}
		}
		return null;
	}

	/**
	 * detect whether Class an Inner one
	 * 
	 * @param c
	 * @return boolean
	 */
	public static boolean isInnerClass(Class c) {
		String name = c.getName();
		if (name.contains("$")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * whether it is an outer class
	 * 
	 * @param c
	 * @return boolean
	 */
	public static boolean isOuterClass(Class c) {
		Class[] inners = c.getClasses();
		if (inners.length == 0) {
			return false;
		} else {
			return true;
		}
	}

	public static Class getClass(byte[] bytecode) {

		return null;
	}

	public static Class getClass(File apkFile) {
		return null;

	}

	private static String fixPath(String path) {
		if (File.separatorChar == '\\')
			path = path.replace('\\', '/');
		int index = path.lastIndexOf("/./");
		if (index != -1)
			return path.substring(index + 3);
		if (path.startsWith("./"))
			return path.substring(2);
		else
			return path;
	}

	/**
	 * return the dalvik Class object from the java bytecode stream
	 * @param String name
	 * @param byte[] data
	 * @return Class cls
	 * @throws IOException 
	 */
	public static Class getDexClass(String name, byte[] data)
			throws IOException {
		//translate the java bytecode to dalvik bytecode
		DexOptions opt = new DexOptions();
		DexFile outputDex = new DexFile(opt);
		CfOptions cf = new CfOptions();
		ClassDefItem clazz = CfTranslator.translate(fixPath(name.replace('.', '/') + ".class"), data, cf, opt);
		outputDex.add(clazz);
		//create the zip file name.apk
		String apkn = tmpdirpath + name + ".apk";
		File apk = new File(apkn);
		if (!apk.exists()) apk.createNewFile();
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(apk));
		ZipEntry classeszip = new ZipEntry("classes.dex");
		zos.putNextEntry(classeszip);
		outputDex.writeTo(zos, null, false);
		zos.closeEntry();
		zos.close();
		//load the name.apk file
		Class c = getClassByName(apkn, name.replace('.', '/'));
		return c;
	}

	public static Object newInstance(Constructor cst, Object[] objects) {
		Thread.currentThread().setContextClassLoader(
				ClassLoader.getSystemClassLoader());
		Class a = null;
		try {
			a = Class.forName("org.python.core.PyObject");
			Constructor cs = a.getConstructor(new Class[] {});
			cs.newInstance(new Object() {
			});
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//        DexClassLoader a=new DexClassLoader();
		//        try {
		//            Class o=a.findClass("org.python.core.PyObject");
		//            o.getClasses();
		//        } catch (ClassNotFoundException e) {
		//            // TODO Auto-generated catch block
		//            e.printStackTrace();
		//        }
		return a;
	}

	public static void resolveClass(Class c) {
		// TODO Auto-generated method stub
		//        DexClassLoader dcl=new DexClassLoader();
		//        dcl.resolveClass(c);
	}
}
