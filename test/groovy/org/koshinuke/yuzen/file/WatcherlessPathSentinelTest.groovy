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
		this.target.register([
					overflowed : {
						c = true
						latch.countDown()
					}
				] as PathEventListener )

		def event = makeEvent(StandardWatchEventKinds.OVERFLOW)
		assert this.target.add(event)
		assert latch.await(1, TimeUnit.SECONDS)
		assert c
		assert 0 == this.target.events.size()
	}

	@Test
	void consumeCreatedEvent() {
		CountDownLatch latch = new CountDownLatch(1)
		def c = false
		def e = null
		this.target.register([
					created : {
						c = true
						e = it
						latch.countDown()
					}
				] as PathEventListener )

		def event = makeEvent(StandardWatchEventKinds.ENTRY_CREATE)
		assert this.target.add(event)
		assert latch.await(1, TimeUnit.SECONDS)
		assert c
		assert event == e
		assert 0 == this.target.events.size()
	}

	@Test
	void consumeDeletedEvent() {
		CountDownLatch latch = new CountDownLatch(1)
		def c = false
		def e = null
		this.target.register([
					deleted : {
						c = true
						e = it
						latch.countDown()
					}
				] as PathEventListener )

		def event = makeEvent(StandardWatchEventKinds.ENTRY_DELETE)
		assert this.target.add(event)
		assert latch.await(1, TimeUnit.SECONDS)
		assert c
		assert event == e
		assert 0 == this.target.events.size()
	}

	@Test
	void consumeModifiedEvent() {
		CountDownLatch latch = new CountDownLatch(1)
		def c = false
		def e = null
		this.target.register([
					modified : {
						c = true
						e = it
						latch.countDown()
					}
				] as PathEventListener )

		def event = makeEvent(StandardWatchEventKinds.ENTRY_MODIFY)
		assert this.target.add(event)
		assert latch.await(1, TimeUnit.SECONDS)
		assert c
		assert event == e
		assert 0 == this.target.events.size()
	}

	@Test
	void keepEventOrdering() {
		CountDownLatch latch = new CountDownLatch(3);
		def act = []
		this.target.register({
			act.add it
			latch.countDown()
		} as PathEventListener)

		def exp = [
			makeEvent(StandardWatchEventKinds.ENTRY_DELETE),
			makeEvent(StandardWatchEventKinds.ENTRY_CREATE),
			makeEvent(StandardWatchEventKinds.ENTRY_MODIFY)
		]
		exp.each { target.add it }

		assert latch.await(1, TimeUnit.SECONDS)
		assert exp == act
	}

	def makeEvent(kind) {
		new DefaultPathEvent(kind, Paths.get("build/sentinel"))
	}
}
