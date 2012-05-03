package org.koshinuke.yuzen.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.koshinuke._;
import org.koshinuke.yuzen.util.WatchServiceUtil;

/**
 * @author taichi
 */
public class PathSentinel {

	ConcurrentSkipListSet<PathEvent> events = new ConcurrentSkipListSet<>();

	CopyOnWriteArrayList<PathEventListener> listeners = new CopyOnWriteArrayList<>();

	Map<WatchEvent.Kind<?>, PathEventDispatcher> dispatchers = new HashMap<>();

	WatchService watchService = WatchServiceUtil.newWatchService();

	ScheduledExecutorService watcherPool;

	ExecutorService workerPool;

	public PathSentinel() {
		this(Executors.newScheduledThreadPool(1), Executors
				.newFixedThreadPool(1));
	}

	public PathSentinel(@Nonnull ScheduledExecutorService watcherPool,
			@Nonnull ExecutorService workerPool) {
		Objects.requireNonNull(watcherPool);
		Objects.requireNonNull(workerPool);
		this.watcherPool = watcherPool;
		this.workerPool = workerPool;
		this.setUpDispatchers();
	}

	public PathSentinel watch(@Nonnull String path) {
		Objects.requireNonNull(path);
		this.watch(Paths.get(path));
		return this;
	}

	public PathSentinel watch(@Nonnull Path path) {
		Objects.requireNonNull(path);
		WatchServiceUtil.watch(this.watchService, path);
		return this;
	}

	public PathSentinel watchAll(@Nonnull Path path) {
		Objects.requireNonNull(path);
		WatchServiceUtil.watchAll(this.watchService, path);
		return this;
	}

	public PathSentinel register(@Nonnull PathEventListener _) {
		Objects.requireNonNull(_);
		this.listeners.add(_);
		return this;
	}

	public PathSentinel unregister(@Nonnull PathEventListener _) {
		Objects.requireNonNull(_);
		this.listeners.remove(_);
		return this;
	}

	public void startUp() {
		this.startWatcher();
		this.startWorker();
	}

	protected void startWatcher() {
		this.watcherPool.schedule(new Callable<_>() {
			@Override
			public _ call() throws Exception {
				WatchKey key = PathSentinel.this.watchService.poll(5,
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
				return _._;
			}
		}, 5, TimeUnit.MILLISECONDS);
	}

	protected void startWorker() {
		this.addRecursivePathWatcher();
		this.workerPool.submit(new Callable<_>() {
			@Override
			public _ call() throws Exception {
				for (Iterator<PathEvent> i = PathSentinel.this.events
						.iterator(); i.hasNext();) {
					PathEvent event = i.next();
					i.remove();
					PathSentinel.this.dispatch(event);
				}
				PathSentinel.this.workerPool.submit(this);
				return _._;
			}
		});
	}

	protected void setUpDispatchers() {
		this.dispatchers.put(StandardWatchEventKinds.OVERFLOW,
				new PathEventDispatcher() {
					@Override
					public void dispatch(PathEventListener listener,
							PathEvent event) throws IOException {
						listener.overflowed();
					}
				});
		this.dispatchers.put(StandardWatchEventKinds.ENTRY_CREATE,
				new PathEventDispatcher() {
					@Override
					public void dispatch(PathEventListener listener,
							PathEvent event) throws IOException {
						listener.created(event);
					}
				});
		this.dispatchers.put(StandardWatchEventKinds.ENTRY_DELETE,
				new PathEventDispatcher() {
					@Override
					public void dispatch(PathEventListener listener,
							PathEvent event) throws IOException {
						listener.deleted(event);
					}
				});
		this.dispatchers.put(StandardWatchEventKinds.ENTRY_MODIFY,
				new PathEventDispatcher() {
					@Override
					public void dispatch(PathEventListener listener,
							PathEvent event) throws IOException {
						listener.modified(event);
					}
				});
	}

	protected void dispatch(PathEvent event) throws IOException {
		PathEventDispatcher dispatcher = this.dispatchers.get(event.getKind());
		if (dispatcher != null) {
			for (PathEventListener listener : this.listeners) {
				dispatcher.dispatch(listener, event);
			}
		}
	}

	protected void addRecursivePathWatcher() {
		this.register(new DefaultPathEventListener() {
			@Override
			public void created(PathEvent event) throws IOException {
				Path path = event.getPath();
				if (Files.isDirectory(path)) {
					WatchServiceUtil.watchAll(PathSentinel.this.watchService,
							path);
				}
			}
		});
	}

	public void shutdown() {
		this.listeners.clear();
		this.events.clear();
		WatchServiceUtil.close(this.watchService);
		this.watcherPool.shutdownNow();
		this.workerPool.shutdownNow();
	}
}
