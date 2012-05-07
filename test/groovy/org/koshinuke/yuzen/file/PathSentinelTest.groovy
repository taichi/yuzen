package org.koshinuke.yuzen.file;

import static org.junit.Assert.*;

import java.io.File
import java.nio.file.StandardWatchEventKinds
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
		this.target = new PathSentinel()

		this.target.watchTree(this.rootDir.toPath())

		this.target.startUp()
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

	@Test
	void createAndDelete() {
		def act = []
		CountDownLatch latch = new CountDownLatch(2)
		this.target.register({
			act.add it
			latch.countDown()
		} as PathEventListener)

		def subDir = new File(this.rootDir, "aaa")
		subDir.mkdirs()
		subDir.deleteDir()

		assert latch.await(50, TimeUnit.MILLISECONDS)
		assert 3 == act.size()

		assert subDir.toPath() == act[0].path
		assert StandardWatchEventKinds.ENTRY_CREATE == act[0].kind
		assert subDir.toPath() == act[1].path
		assert StandardWatchEventKinds.ENTRY_MODIFY == act[1].kind
		assert subDir.toPath() == act[2].path
		assert StandardWatchEventKinds.ENTRY_DELETE == act[2].kind
	}

	@After
	void tearDown() {
		this.target.shutdown()
	}
}
