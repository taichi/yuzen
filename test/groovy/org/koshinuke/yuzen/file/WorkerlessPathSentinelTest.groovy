package org.koshinuke.yuzen.file;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author taichi
 */
class WorkerlessPathSentinelTest {

	PathSentinel target
	File rootDir

	@Before
	void setUp() {
		this.rootDir = new File('build/sentinel')
		if(rootDir.exists()) {
			FileUtils.delete(this.rootDir, FileUtils.RECURSIVE | FileUtils.SKIP_MISSING)
		}
		assert this.rootDir.mkdirs()

		this.target = new PathSentinel() {
					@Override
					protected void startWorker() {
						// do nothing for test
					}
				}
		this.target.watch(this.rootDir.toPath())
		this.target.startUp()
	}

	@After
	void tearDown() {
		this.target.shutdown()
	}

	@Test
	void createEventQueued() {
		assert target.events.isEmpty()
		File f = new File(rootDir, "c")
		assert f.mkdirs()
		Thread.sleep(20)
		assert 1 == target.events.size()
	}

	@Test
	void modifyEventQueued() {
		assert target.events.isEmpty()
		File f = new File(rootDir, "hoge.txt")
		f.text = "moge" + new Date()
		Thread.sleep(20)
		assert 2 == target.events.size()
	}

	@Test
	void duplicateEvent() {
		File f = new File(rootDir, "hoge.txt")
		f.text = "moge" + new Date()
		Thread.sleep(20)

		this.target.events.clear()

		3.times {
			f.text = "hogehoge" + new Date()
			Thread.sleep(20)
			assert 1 == target.events.size()
		}
	}
}
