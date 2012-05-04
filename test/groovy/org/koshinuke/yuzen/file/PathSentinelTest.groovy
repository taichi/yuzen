package org.koshinuke.yuzen.file;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


import org.eclipse.jgit.util.FileUtils;
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author taichi
 */
class PathSentinelTest {

	PathSentinel target
	File rootDir

	@Before
	void setUp() {
		this.rootDir = new File('build/sentinel')
		if(rootDir.exists()) {
			FileUtils.delete(this.rootDir, FileUtils.RECURSIVE | FileUtils.SKIP_MISSING)
		}
		assert this.rootDir.mkdirs()
		target = new PathSentinel()

		target.watchTree(this.rootDir.toPath())

		target.startUp()
	}

	@Test
	void recursiveWatch() {
		def act = []
		CountDownLatch latch = new CountDownLatch(2)
		this.target.register([
					created : {
						act.add it.path
						latch.countDown()
					},
					modified: {
					}
				] as PathEventListener)
		def subDir = new File(this.rootDir,"aaa/bbb")
		subDir.mkdirs()
		assert latch.await(50, TimeUnit.MILLISECONDS)

		def exp = [
			subDir.parentFile.toPath(),
			subDir.toPath()
		]
		assert exp == act
	}

	@After
	void tearDown() {
		target.shutdown()
	}
}
