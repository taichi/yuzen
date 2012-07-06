package org.koshinuke.yuzen.util;

import java.io.File;
import java.nio.file.Path;

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

	public static String slashify(Path p) {
		return slashify(p.toString());
	}

	public static String slashify(String path) {
		String p = path;
		if (File.separatorChar != '/') {
			p = p.replace(File.separatorChar, '/');
		}
		return p;
	}

}
