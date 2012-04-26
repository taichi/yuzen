package org.koshinuke.yuzen.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author taichi
 */
class FileUtilTest {

	@Test
	void removeExtensionTest() {
		assert 'hoge' == FileUtil.removeExtension('hoge.moge')
		assert 'hoge' == FileUtil.removeExtension('hoge')
		assert '' == FileUtil.removeExtension('.moge')
	}
}
