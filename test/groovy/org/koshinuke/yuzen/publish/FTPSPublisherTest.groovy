package org.koshinuke.yuzen.publish;

import static org.junit.Assert.*;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author taichi
 */
class FTPSPublisherTest {

	FTPSTestingSupport ftps

	@Before
	void setUp() {
		ftps = new FTPSTestingSupport()
		ftps.initialize()
	}

	@After
	void tearDown() {
		ftps.dispose()
	}

	@Test
	void test() {
		FTPSPublisher target = new FTPSPublisher()
		target.host = "localhost"
		target.username = ftps.user.name
		target.password = ftps.user.password

		File root = new File("test/resources/taskconsumer/_contents")
		target.publish(root)

		File home = new File(ftps.ftpDir, ftps.user.homeDirectory)
		assert home.exists()

		assert new File(home, "profile.md").exists()
		assert new File(home, "entry/2012-04-29/staticcontents.md").exists()
	}
}
