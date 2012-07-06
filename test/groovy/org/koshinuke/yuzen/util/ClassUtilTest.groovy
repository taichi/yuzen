package org.koshinuke.yuzen.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author taichi
 */
class ClassUtilTest {

	@Test
	void getPackageName() {
		assert "org.koshinuke.yuzen.util" == ClassUtil.getPackageName(ClassUtil)
	}

	@Test
	void getPackagePath() {
		assert "org/koshinuke/yuzen/util" == ClassUtil.getPackagePath(ClassUtil)
	}
}
