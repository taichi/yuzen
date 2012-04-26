package org.koshinuke.yuzen.util;

/**
 * @author taichi
 */
public class FileUtil {

	public static String removeExtension(String path) {
		String result = path;
		int i = path.lastIndexOf('.');
		if (-1 < i) {
			result = path.substring(0, i);
		}
		return result;
	}
}
