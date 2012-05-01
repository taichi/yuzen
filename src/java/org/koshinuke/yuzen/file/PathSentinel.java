package org.koshinuke.yuzen.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.koshinuke._;
import org.koshinuke.yuzen.util.WatchServiceUtil;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * @author taichi
 */
public class PathSentinel {

	ConcurrentSkipListSet<PathEvent> events = new ConcurrentSkipListSet<>();

	WatchService watchService = WatchServiceUtil.newWatchService();

	EventBus workerBus;
	ExecutorService watcherPool;
	ExecutorService routerPool;

	public PathSentinel(@Nonnull EventBus workerBus) {
		this(workerBus, Executors.newFixedThreadPool(1), Executors
				.newFixedThreadPool(1));
	}

	public PathSentinel(@Nonnull EventBus workerBus,
			@Nonnull ExecutorService watcherPool,
			@Nonnull ExecutorService routerPool) {
		Objects.requireNonNull(workerBus);
		Objects.requireNonNull(watcherPool);
		Objects.requireNonNull(routerPool);
		this.workerBus = workerBus;
		this.watcherPool = watcherPool;
		this.routerPool = routerPool;
	}

	public void watch(String path) {
		this.watch(Paths.get(path));
	}

	public void watch(Path path) {
		WatchServiceUtil.watch(this.watchService, path);
	}

	public void watchAll(Path path) {
		WatchServiceUtil.watchAll(this.watchService, path);
	}

	@Subscribe
	public void seekPath(PathEvent event) {
		if (StandardWatchEventKinds.ENTRY_CREATE == event.getKind()) {
			Path path = event.getPath();
			if (Files.isDirectory(path)) {
				WatchServiceUtil.watchAll(this.watchService, path);
			}
		}
	}

	public void startUp() {
		this.workerBus.register(this);
		this.startWatcher();
		this.startRouter();
	}

	protected void startWatcher() {
		this.watcherPool.submit(new Callable<_>() {
			@Override
			public _ call() throws Exception {
				WatchKey key = PathSentinel.this.watchService.poll(10,
						TimeUnit.MILLISECONDS);
				if (key != null) {
					Path path = Path.class.cast(key.watchable());
					for (WatchEvent<?> i : key.pollEvents()) {
						WatchEvent.Kind<?> kind = i.kind();
						Path resolved = path.resolve(Path.class.cast(i
								.context()));
						DefaultPathEvent event = new DefaultPathEvent(kind,
								resolved);
						PathSentinel.this.events.add(event);
					}
				}
				key.reset();
				PathSentinel.this.watcherPool.submit(this);
				return _._;
			}
		});
	}

	protected void startRouter() {
		this.routerPool.submit(new Callable<_>() {
			@Override
			public _ call() throws Exception {
				for (Iterator<PathEvent> i = PathSentinel.this.events
						.iterator(); i.hasNext();) {
					PathEvent event = i.next();
					i.remove();
					PathSentinel.this.workerBus.post(event);
				}
				PathSentinel.this.routerPool.submit(this);
				return _._;
			}
		});
	}

	public void shutdown() {
		this.workerBus.unregister(this);
		this.events.clear();
		WatchServiceUtil.close(this.watchService);
		this.watcherPool.shutdownNow();
		this.routerPool.shutdownNow();
	}
}
