package org.koshinuke.yuzen.util;

/**
 * @author taichi
 */
public class ClassUtil {

	public static String getPackagePath(Class<?> clazz) {
		Package p = clazz.getPackage();
		String name = "";

		if (p == null) {
			String s = clazz.getName();
			int i = s.lastIndexOf('.');
			name = s.substring(0, i);
		} else {
			name = p.getName();
		}
		return name.replace('.', '/');
	}

}
