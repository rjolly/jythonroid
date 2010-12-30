// Copyright (c) Corporation for National Research Initiatives

package org.python.core;

import java.io.IOException;
import java.security.SecureClassLoader;
import java.util.Vector;

import org.python.debug.FixMe;

/**
 * A java2 classloader for loading compiled python modules.
 */
class BytecodeLoader2 extends SecureClassLoader implements Loader {
    private Vector parents;

    public BytecodeLoader2() {
        this.parents = BytecodeLoader.init();
    }

    public void addParent(ClassLoader referent) {
        if (!this.parents.contains(referent)) {
            this.parents.add(0, referent);
        }
    }

    // override from abstract base class
    protected Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        return BytecodeLoader.findParentClass(this.parents, name);
    }

    public Class loadClassFromBytes(String name, byte[] data) {
    	//TODO bug fix number 4 Start Needed
        //Class c = defineClass(name, data, 0, data.length, this.getClass()
        //        .getProtectionDomain());
        //resolveClass(c);
        Class c=null;
		try {
			c = FixMe.getDexClass(name, data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //TODO bug fix number 4 End Needed
        BytecodeLoader.compileClass(c);
        return c;
    }

}
