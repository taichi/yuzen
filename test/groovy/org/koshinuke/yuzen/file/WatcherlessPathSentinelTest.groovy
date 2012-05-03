package org.koshinuke.yuzen.file;

import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author taichi
 */
class WatcherlessPathSentinelTest {

	PathSentinel target

	@Before
	void setUp() {
		this.target = new PathSentinel() {
					@Override
					protected void startWatcher() {
						// do nothing for test
					}

					@Override
					protected void addRecursivePathWatcher() {
						// do nothing for test
					}
				}
		this.target.startUp()
	}

	@After
	void tearDown() {
		this.target.shutdown()
	}

	@Test
	void consumeOverflowEvent() {
		CountDownLatch latch = new CountDownLatch(1)
		def c = false
		this.target.register({
			overflowed : {
				c = true
				latch.countDown()
			}
		} as PathEventListener )

		def event = new DefaultPathEvent(StandardWatchEventKinds.OVERFLOW, Paths.get("build/sentinel"))
		assert this.target.events.add(event)
		assert latch.await(20, TimeUnit.MILLISECONDS)
		assert c
	}

	@Test
	void consumeCreatedEvent() {
		CountDownLatch latch = new CountDownLatch(1)
		def c = false
		def e = null
		this.target.register({
			created : {
				c = true
				e = it
				latch.countDown()
			}
		} as PathEventListener )

		def event = new DefaultPathEvent(StandardWatchEventKinds.ENTRY_CREATE, Paths.get("build/sentinel"))
		assert this.target.events.add(event)
		assert latch.await(20, TimeUnit.MILLISECONDS)
		assert c
		assert event == e
	}

	@Test
	void consumeDeletedEvent() {
		CountDownLatch latch = new CountDownLatch(1)
		def c = false
		def e = null
		this.target.register({
			deleted : {
				c = true
				e = it
				latch.countDown()
			}
		} as PathEventListener )

		def event = new DefaultPathEvent(StandardWatchEventKinds.ENTRY_DELETE, Paths.get("build/sentinel"))
		assert this.target.events.add(event)
		assert latch.await(20, TimeUnit.MILLISECONDS)
		assert c
		assert event == e
	}

	@Test
	void consumeModifiedEvent() {
		CountDownLatch latch = new CountDownLatch(1)
		def c = false
		def e = null
		this.target.register({
			modified : {
				c = true
				e = it
				latch.countDown()
			}
		} as PathEventListener )

		def event = new DefaultPathEvent(StandardWatchEventKinds.ENTRY_MODIFY, Paths.get("build/sentinel"))
		assert this.target.events.add(event)
		assert latch.await(20, TimeUnit.MILLISECONDS)
		assert c
		assert event == e
	}
}
